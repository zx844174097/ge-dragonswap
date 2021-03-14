package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.sql.SqlServer;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolCreateBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.PushRemarkBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;
import cn.net.mugui.ge.DraGonSwap.util.AddressBindUtil;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolConfUtil;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolDescriptUtil;
import cn.net.mugui.ge.util.RedisUtil;

/**
 * 流动性凭证处理
 * 
 * @author Administrator
 *
 */
@AutoTask
@Task
public class DGCertTask extends TaskImpl {

	@Override
	public void run() {
		while (true) {
			try {
				handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Other.sleep(1000);
		}
	}

	@Autowired
	private DGConf sysConfApi;

	@Autowired
	private DGDao dao;

	@Autowired
	private DGSymbolDescriptUtil dgsymbolDes;

	@Autowired
	private AddressBindUtil addressbindutil;

	@Autowired
	private DGCertTokenOutTask dgCertTokenOutTask;

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private DGSymbolConfUtil dgSymbolConfUtil;

	@Autowired
	private DGCertRansomTask ransomTask;

	private void handle() {
		while (true) {
			String value = sysConfApi.getValue("dg_cert_handle_index");
			if (StringUtils.isBlank(value)) {
				sysConfApi.save("dg_cert_handle_index", value = "0", "流动性凭证处理index");
			}
			if (!Other.isInteger(value)) {
				return;
			}
			List<BlockTranBean> selectList = dao.selectList(BlockTranBean.class, Select.q(new BlockTranBean()).where(Where.q().gt("tran_id", value)));
			if (selectList.isEmpty()) {
				return;
			}
			for (BlockTranBean blockChainBean : selectList) {
				sysConfApi.setValue("dg_cert_handle_index", blockChainBean.getTran_id().toString());

				DGPriAddressBean dgPriAddressBean = new DGPriAddressBean().setAddress(blockChainBean.getTo());
				dgPriAddressBean = dao.select(dgPriAddressBean);
				if (dgPriAddressBean == null) {
					return;
				}

				DGSymbolBean dgSymbol = new DGSymbolBean().setDg_symbol_id(dgPriAddressBean.getDg_symbol_id());
				dgSymbol = dao.select(dgSymbol);
				if (dgSymbol.getSymbol_status() != DGSymbolBean.SYMBOL_STATUS_1) {
					return;
				}

				if (ransomTask.handle(blockChainBean, dgSymbol)) {
					continue;
				}
				Object redis = redisUtil.getRedis("wait_" + blockChainBean.getHash());
				if (redis == null) {
					continue;
				}
				PushRemarkBean remarkBean = PushRemarkBean.newBean(PushRemarkBean.class, redis);
				if (remarkBean.getType() != 1) {
					continue;
				}
				redisUtil.deleteRedis("wait_" + blockChainBean.getHash());

				try {
					dao.getSqlServer().setAutoCommit(false);
					handle(blockChainBean, remarkBean, dgSymbol);
					dao.getSqlServer().commit();
				} catch (Exception e) {
					e.printStackTrace();
					try {
						dao.getSqlServer().rollback();
						SqlServer.reback();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	private void handle(BlockTranBean blockChainBean, PushRemarkBean remarkBean, DGSymbolBean dgSymbol) throws SQLException, Exception {
		// 处理流动性凭证
		DGKeepBean dgKeepBean = new DGKeepBean();
		dgKeepBean.setHash_1(remarkBean.getRemark());
		dgKeepBean = dao.select(dgKeepBean);
		if (dgKeepBean == null) {// 第一手入金
			dgKeepBean = new DGKeepBean();
			dgKeepBean.setHash_1(blockChainBean.getHash());
			dgKeepBean.setBlock_1(blockChainBean.getBlock());
			dgKeepBean.setToken_1(blockChainBean.getToken());
			dgKeepBean.setKeep_type(DGKeepBean.keep_type_0);
			if (dao.select(dgKeepBean) != null) {
				return;
			}
			// 为用户创建流动性凭证
			String datumAddress = addressbindutil.toDatumAddress(blockChainBean.getFrom());
			dgKeepBean.setUser_address(datumAddress).setKeep_status(DGKeepBean.KEEP_STATUS_1);

			dgKeepBean.setDg_symbol(dgSymbol.getSymbol());
			dgKeepBean = dao.save(dgKeepBean);
			return;
		} else {// 第二手入金
			dgKeepBean.setHash_2(blockChainBean.getHash());
			dgKeepBean.setBlock_2(blockChainBean.getBlock());
			dgKeepBean.setToken_2(blockChainBean.getToken());
			if (dao.select(new DGKeepBean().setHash_1(remarkBean.getRemark()).setHash_2(blockChainBean.getHash())) != null) {
				return;
			}
			DGSymbolConfBean select5 = dgSymbolConfUtil.getByContract_address(blockChainBean.getToken());
			if (select5 == null) {
				dgKeepBean.setKeep_status(DGKeepBean.KEEP_STATUS_3);
				dao.updata(dgKeepBean);
				return;
			}
			BlockTranBean transactionBean = dao.select(new BlockTranBean().setHash(dgKeepBean.getHash_1()));
			if (dgSymbol.getBase_currency().equals(select5.getSymbol())) {// 第二手入金为基本币种
				dgKeepBean.setBase_num(blockChainBean.getNum());
				dgKeepBean.setQuotes_num(transactionBean.getNum());
			} else {
				dgKeepBean.setBase_num(transactionBean.getNum());
				dgKeepBean.setQuotes_num(blockChainBean.getNum());
			}
			SwapBean swapBean = manager.get(dgSymbol.getSymbol());
			// 判断比例是否正常
			DGSymbolCreateBean select2 = swapBean.create;
			if (select2.getBase_init_number().compareTo(BigDecimal.ZERO) <= 0) {// 交易对的第一次入金
				select2.setBase_init_number(dgKeepBean.getBase_num()).setQuote_init_number(dgKeepBean.getQuotes_num()).setCreate_address(dgKeepBean.getUser_address());
				// 初始比例
				select2.setCreate_init_price(select2.getQuote_init_number().divide(select2.getBase_init_number(), 8, BigDecimal.ROUND_DOWN));
				// 初始总量
				select2.setTotal_init_number(select2.getQuote_init_number().multiply(select2.getBase_init_number()));
				dao.updata(select2);
			}

			// BigDecimal divide =
			// dgKeepBean.getQuotes_keep_num().divide(dgKeepBean.getBase_keep_num(), 8,
			// BigDecimal.ROUND_DOWN);

			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal divide2 = null;
			if (symbol_des.getScale().compareTo(BigDecimal.ZERO) <= 0) {
				divide2 = dgKeepBean.getQuotes_num().multiply(new BigDecimal("2"));
			} else {
				divide2 = symbol_des.getScale().multiply(dgKeepBean.getBase_num()).add(dgKeepBean.getQuotes_num());
			}

//			BigDecimal base = BigDecimal.ZERO;
//			BigDecimal quotes = BigDecimal.ZERO;
//
//			if (divide.compareTo(select2.getCreate_init_price()) >= 0) {
//				base = dgKeepBean.getBase_keep_num();
//				quotes = dgKeepBean.getBase_keep_num().multiply(select2.getCreate_init_price());
//			} else {
//				quotes = dgKeepBean.getQuotes_keep_num();
//				DGSymbolConfBean select3 = dgSymbolConfUtil.get(dgSymbol.getBase_currency());
//				base = dgKeepBean.getQuotes_keep_num().divide(select2.getCreate_init_price(), select3.getPrecision(), BigDecimal.ROUND_HALF_UP);
//			}
//			BigDecimal divide2 = base.multiply(quotes).divide(new BigDecimal("8"));

			dgKeepBean.setToken_num(divide2);

			DGKeepBean last = dao.selectDESC(new DGKeepBean().setDg_symbol(dgKeepBean.getDg_symbol()).setKeep_status(DGKeepBean.KEEP_STATUS_7));
			BigDecimal last_big = BigDecimal.ZERO; 
			if (last != null) {
				last_big = last.getNow_out_cert_token_num();
			} else {
				last_big = BigDecimal.ZERO;
			}

			DGSymbolConfBean select = confUtil.getByContract_address(select2.getToken_address());

			dgKeepBean.setBlock_3(select.getBlock_name());
			dgKeepBean.setToken_3(select.getContract_address());
			dgKeepBean.setLast_out_cert_token_num(last_big);
			dgKeepBean.setNow_out_cert_token_num(last_big.add(dgKeepBean.getToken_num()));
			dgKeepBean.setKeep_status(DGKeepBean.KEEP_STATUS_2);
			dao.updata(dgKeepBean);
			// 更新持有总量
			dgsymbolDes.updateTotal(swapBean, dgKeepBean.getBase_num(), dgKeepBean.getQuotes_num());
			// 转出持有证明token
			dgCertTokenOutTask.add(dgKeepBean);

		}
	}

	@Autowired
	DGSymbolConfUtil confUtil;

	@Autowired
	private DSymbolManager manager;

}

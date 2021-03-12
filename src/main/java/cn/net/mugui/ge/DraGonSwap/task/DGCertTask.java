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
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolPriBean;
import cn.net.mugui.ge.DraGonSwap.bean.PushRemarkBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
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
			for (BlockTranBean blockChainBean : selectList) {
				sysConfApi.setValue("dg_cert_handle_index", blockChainBean.getTran_id().toString());

				Object redis = redisUtil.getRedis("wait_" + blockChainBean.getHash());
				if (redis == null) {
					continue;
				}
				PushRemarkBean remarkBean = PushRemarkBean.newBean(PushRemarkBean.class, redis);
				if (remarkBean.getType() != 1) {
					continue;
				}
				redisUtil.deleteRedis("wait_" + blockChainBean.getHash());
				DGPriAddressBean dgPriAddressBean = new DGPriAddressBean().setAddress(blockChainBean.getTo());
				dgPriAddressBean = dao.select(dgPriAddressBean);
				if (dgPriAddressBean == null) {
					return;
				}
				DGSymbolPriBean dgSymbolPriBean = new DGSymbolPriBean().setSymbol_pri_id(dgPriAddressBean.getSymbol_pri_id()).setType(DGSymbolPriBean.type_1);
				DGSymbolPriBean select4 = dao.select(dgSymbolPriBean);
				if (select4 == null) {
					return;
				}

				DGSymbolBean dgSymbol = new DGSymbolBean().setDg_symbol_id(select4.getDg_symbol_id());
				dgSymbol = dao.select(dgSymbol);
				if (dgSymbol.getSymbol_status() != DGSymbolBean.SYMBOL_STATUS_1) {
					return;
				}
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
		dgKeepBean.setAmount_one_hash(remarkBean.getRemark());
		dgKeepBean = dao.select(dgKeepBean);
		if (dgKeepBean == null) {// 第一手入金
			dgKeepBean = new DGKeepBean();
			dgKeepBean.setAmount_one_hash(blockChainBean.getHash());
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
			dgKeepBean.setAmount_two_hash(blockChainBean.getHash());
			if (dao.select(new DGKeepBean().setAmount_one_hash(remarkBean.getRemark()).setAmount_two_hash(blockChainBean.getHash())) != null) {
				return;
			}
			DGSymbolConfBean select5 = dgSymbolConfUtil.getByContract_address(blockChainBean.getToken());
			if (select5 == null) {
				dgKeepBean.setKeep_status(DGKeepBean.KEEP_STATUS_3);
				dao.updata(dgKeepBean);
				return;
			}
			BlockTranBean transactionBean = dao.select(new BlockTranBean().setHash(dgKeepBean.getAmount_one_hash()));
			if (dgSymbol.getBase_currency().equals(select5.getSymbol())) {// 第二手入金为基本币种
				dgKeepBean.setBase_keep_num(blockChainBean.getNum());
				dgKeepBean.setQuotes_keep_num(transactionBean.getNum());
			} else {
				dgKeepBean.setBase_keep_num(transactionBean.getNum());
				dgKeepBean.setQuotes_keep_num(blockChainBean.getNum());
			}
			// 判断比例是否正常
			DGSymbolCreateBean select2 = dao.select(new DGSymbolCreateBean().setDg_symbol_id(dgSymbol.getDg_symbol_id()));
			if (select2.getCreate_address() == null) {// 交易对的第一次入金
				select2.setBase_init_number(dgKeepBean.getBase_keep_num()).setQuote_init_number(dgKeepBean.getQuotes_keep_num()).setCreate_address(dgKeepBean.getUser_address());
				// 初始比例
				select2.setCreate_init_price(select2.getQuote_init_number().divide(select2.getBase_init_number(), 8, BigDecimal.ROUND_DOWN));
				// 初始总量
				select2.setTotal_init_number(select2.getQuote_init_number().multiply(select2.getBase_init_number()));
				dao.updata(select2);
			}

			BigDecimal divide = dgKeepBean.getQuotes_keep_num().divide(dgKeepBean.getBase_keep_num(), 8, BigDecimal.ROUND_DOWN);

			BigDecimal base = BigDecimal.ZERO;
			BigDecimal quotes = BigDecimal.ZERO;

			if (divide.compareTo(select2.getCreate_init_price()) >= 0) {
				base = dgKeepBean.getBase_keep_num();
				quotes = dgKeepBean.getBase_keep_num().multiply(select2.getCreate_init_price());
			} else {
				quotes = dgKeepBean.getQuotes_keep_num();
				DGSymbolConfBean select3 = dgSymbolConfUtil.get(dgSymbol.getBase_currency());
				base = dgKeepBean.getQuotes_keep_num().divide(select2.getCreate_init_price(), select3.getPrecision(), BigDecimal.ROUND_HALF_UP);
			}

			BigDecimal divide2 = base.multiply(quotes).divide(new BigDecimal("8"));

			dgKeepBean.setToken_keep_num(divide2);
			dgKeepBean.setKeep_status(DGKeepBean.KEEP_STATUS_2);
			dao.updata(dgKeepBean);
			// 更新持有总量
			dgsymbolDes.updateTotal(dgSymbol.getSymbol(), base, quotes);
			// 转出持有证明token
			dgCertTokenOutTask.outToken(dgKeepBean.getUser_address(), select2.getToken_address(), dgSymbol.getSymbol(), divide2, "TRX");

		}
	}

}

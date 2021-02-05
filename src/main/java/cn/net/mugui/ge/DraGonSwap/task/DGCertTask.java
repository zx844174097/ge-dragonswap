package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import com.hx.blockchain.bean.BlockChainBean;
import com.hx.blockchain.bean.BlockChainTransactionBean;
import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.base.TaskInterface;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.sql.SqlModeApi;
import com.mugui.sql.TableMode;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolCreateBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolPriBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.DraGonSwap.util.AddressBindUtil;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolDescriptUtil;
import cn.net.mugui.ge.sys.service.SysConfApi;

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
			Other.sleep(10000);
		}
	}

	@Reference
	private SysConfApi sysConfApi;

	@Reference(group = "ge")
	private SqlModeApi sqlModeApi;

	@Autowired
	private DGDao dao;

	@Autowired
	private DGSymbolDescriptUtil dgsymbolDes;

	@Autowired
	private AddressBindUtil addressbindutil;

	@Autowired
	private DGCertTokenOutTask dgCertTokenOutTask;

	private void handle() {
		while (true) {
			String value = sysConfApi.getValue("dg_cert_handle_index");
			if (StringUtils.isBlank(value)) {
				sysConfApi.save("dg_cert_handle_index", value = "", "流动性凭证处理index");
			}
			if (!Other.isInteger(value)) {
				return;
			}
			List<BlockChainTransactionBean> selectList = sqlModeApi.selectList(BlockChainTransactionBean.class, Select.q(new BlockChainTransactionBean()).where(Where.q().gt("bc_tran_id", value)));
			for (BlockChainTransactionBean blockChainBean : selectList) {
				sysConfApi.setValue("dg_cert_handle_index", blockChainBean.getBc_tran_id().toString());
				DGPriAddressBean dgPriAddressBean = new DGPriAddressBean().setAddress(blockChainBean.getTo_address());
				dgPriAddressBean = dao.select(dgPriAddressBean);
				if (dgPriAddressBean == null) {
					continue;
				}
				DGSymbolPriBean dgSymbolPriBean = new DGSymbolPriBean().setSymbol_pri_id(dgPriAddressBean.getSymbol_pri_id()).setType(DGSymbolPriBean.type_1);
				DGSymbolPriBean select4 = dao.select(dgSymbolPriBean);
				if (select4 == null) {
					continue;
				}

				DGSymbolBean dgSymbol = new DGSymbolBean().setDg_symbol_id(select4.getDg_symbol_id());
				dgSymbol = dao.select(dgSymbol);
				if (dgSymbol.getSymbol_status() != DGSymbolBean.SYMBOL_STATUS_1) {
					continue;
				}

				// 为用户创建流动性凭证
				DGKeepBean dgKeepBean = new DGKeepBean();
				String datumAddress = addressbindutil.toDatumAddress(blockChainBean.getFrom_address());
				dgKeepBean.setUser_address(datumAddress).setKeep_status(DGKeepBean.KEEP_STATUS_1);
				List<DGKeepBean> selectList2 = dao.selectList(dgKeepBean);
				boolean bool = true;
				for (DGKeepBean select : selectList2) {
					if (!bool) {
						continue;
					}
					// 检测到有部分入金

					// 得到交易池
					dgKeepBean.setDg_symbol(dgSymbol.getSymbol());
					select.setAmount_two_hash(blockChainBean.getReal_hash());

					BlockChainTransactionBean transactionBean = sqlModeApi.select(new BlockChainTransactionBean().setReal_hash(select.getAmount_one_hash()));

					if (dgSymbol.getBase_currency().equals(blockChainBean.getBc_type_name())) {// 第二手入金为基本币种
						select.setBase_keep_num(blockChainBean.getBc_amount());
						select.setQuotes_keep_num(transactionBean.getBc_amount());
					} else {
						select.setBase_keep_num(transactionBean.getBc_amount());
						select.setQuotes_keep_num(blockChainBean.getBc_amount());
					}

					dao.updata(select);

					// 判断比例是否正常
					DGSymbolCreateBean select2 = dao.select(new DGSymbolCreateBean().setDg_symbol_id(dgSymbol.getDg_symbol_id()));
					if (select2.getCreate_address() == null) {// 交易对的第一次入金
						select2.setBase_init_number(select.getBase_keep_num()).setQuote_init_number(select.getQuotes_keep_num()).setCreate_address(dgKeepBean.getUser_address());
						// 初始比例
						select2.setCreate_init_price(select2.getQuote_init_number().divide(select2.getBase_init_number(), 8, BigDecimal.ROUND_DOWN));
						// 初始总量
						select2.setTotal_init_number(select2.getQuote_init_number().multiply(select2.getBase_init_number()));
						dao.updata(select2);
					}

					BigDecimal divide = select.getQuotes_keep_num().divide(select.getBase_keep_num(), 8, BigDecimal.ROUND_DOWN);

					BigDecimal base = BigDecimal.ZERO;
					BigDecimal quotes = BigDecimal.ZERO;

					if (divide.compareTo(select2.getCreate_init_price()) >= 0) {
						base = select.getBase_keep_num();
						quotes = select.getBase_keep_num().multiply(select2.getCreate_init_price());
					} else {
						quotes = select.getQuotes_keep_num();
						DGSymbolConfBean select3 = dao.select(new DGSymbolConfBean().setSymbol(dgSymbol.getBase_currency()));
						base = select.getQuotes_keep_num().divide(select2.getCreate_init_price(), select3.getPrecision(), BigDecimal.ROUND_HALF_UP);
					}

					BigDecimal divide2 = base.multiply(quotes).divide(new BigDecimal("8"));

					select.setToken_keep_num(divide2);
					select.setKeep_status(DGKeepBean.KEEP_STATUS_2);
					dao.updata(select);
					// 更新持有总量
					dgsymbolDes.updateTotal(dgSymbol.getSymbol(), base, quotes);
					// 转出持有证明token
					dgCertTokenOutTask.outToken(select.getUser_address(), select2.getToken_address(), dgSymbol.getSymbol(), divide2, "TRX");
					bool = false;
				}
				if (bool) {
					// 第一手入金
					DGKeepBean select = new DGKeepBean().setUser_address(datumAddress).setKeep_status(DGKeepBean.KEEP_STATUS_1);
					select.setAmount_one_hash(blockChainBean.getReal_hash());
					select.setDg_symbol(blockChainBean.getBc_type_name());
					select = dao.save(select);
				}
			}
		}
	}

//	private DGSymbolBean getDGSymbol(DGKeepBean select, String address) {
//		DGSymbolBean select2 = dao.select(new DGSymbolBean().setBase_currency(select.getDg_symbol()).setQuote_currency(address));
//		if (select2 != null) {
//			return select2;
//		}
//		return dao.select(new DGSymbolBean().setBase_currency(address).setQuote_currency(select.getDg_symbol()));
//	}

}

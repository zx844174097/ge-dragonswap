package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.bean.SystemInFeeBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolConfUtil;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolDescriptUtil;

/**
 * 交易撮合
 * 
 * @author Administrator
 *
 */
@AutoTask
@Task()
public class DGTransferMatchTask extends TaskImpl {

	@Autowired
	private DGDao dao;

	LinkedHashMap<Integer, DGTranLogBean> match_list = new LinkedHashMap<>();

	@Override
	public void init() {
		super.init();
		dao.createTable(SystemInFeeBean.class);
		List<DGTranLogBean> selectList = dao.selectList(new DGTranLogBean().setLog_status(DGTranLogBean.log_status_4));
		for (DGTranLogBean tranLogBean : selectList) {
			match_list.put(tranLogBean.getTran_log_id(), tranLogBean);
		}
	}

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
	private DGTransferTokenOutTask transfer;

	@Autowired
	private DSymbolManager manager;
	@Autowired
	private DGSymbolDescriptUtil dgSymbolDescriptUtil;

	@Autowired
	private DGSymbolConfUtil dgSymbolConfUtil;

	private void handle() {
		long currentTimeMillis = System.currentTimeMillis();
		LinkedList<Integer> linkedList = new LinkedList<>(match_list.keySet());
		for (Integer key : linkedList) {
			DGTranLogBean bean = match_list.get(key);
			if (bean.getTran_log_create_time().getTime() + bean.getTo_limit_time() * 60 * 1000 < currentTimeMillis) {
				rollback(bean);
			}
		}
		linkedList = new LinkedList<>(match_list.keySet());
		for (Integer key : linkedList) {
			handle(match_list.get(key));

		}
		addNewLog();

	}

	/**
	 * 退钱
	 * 
	 * @param bean
	 */
	private void rollback(DGTranLogBean bean) {

		if (dao.select(new DGTranLogBean().setTran_log_id(bean.getTran_log_id())).getLog_status() != 4) {
			return;
		}

		bean.setLog_type(DGTranLogBean.log_type_1);
		bean.setTo_address(bean.getFrom_address());
		bean.setTo_block(bean.getFrom_block());
		bean.setTo_num(bean.getFrom_num());
		bean.setTo_token(bean.getFrom_token());
		bean.setTo_token_name(bean.getFrom_token_name());
		bean.setTran_log_create_time(new Date());
		transfer.add(bean);
	}

	@Autowired
	private KTranLineTask kTranLineTask;

	private BigDecimal system_fee_scale = new BigDecimal("0.5");

	private void handle(DGTranLogBean bean) {
		if (bean.getLog_type() != DGTranLogBean.log_type_0) {
			match_list.remove(bean.getTran_log_id());
			return;
		}
		SwapBean swapBean = manager.get(bean.getDg_symbol());
		System.out.println("handle 处理-》" + bean);
		BigDecimal bc_amount = bean.getFrom_num();
		BigDecimal fee_num = bean.getFee_num();
		bc_amount = bc_amount.subtract(fee_num);
		String[] split = bean.getDg_symbol().split("[/]");
		if (split[0].equals(bean.getFrom_token_name())) {// 基本币种

			DGSymbolConfBean dgSymbolConfBean = dgSymbolConfUtil.get(split[1]);

			BigDecimal bol = dgSymbolDescriptUtil.reckonInBase(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, bean.getTo_limit_num());
			if (bol!=null) {

				BigDecimal multiply = fee_num.multiply(system_fee_scale).setScale(dgSymbolConfBean.getPrecision(), BigDecimal.ROUND_UP).stripTrailingZeros();
				
				saveSystem_fee_scale(multiply, swapBean.symbol, bean.getTo_address(), dgSymbolConfUtil.get(split[0]));
				bean.setStart_base_total(swapBean.symbol_des.getBase_num());
				bean.setStart_quote_total(swapBean.symbol_des.getQuote_num());
				
				BigDecimal inBase = dgSymbolDescriptUtil.inBase(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, fee_num.subtract(multiply),bol);

				bean.setEnd_base_total(swapBean.symbol_des.getBase_num());
				bean.setEnd_quote_total(swapBean.symbol_des.getQuote_num());
				
				bean.setTo_num(inBase);
				bean.setScale(inBase.divide(bc_amount, 8, BigDecimal.ROUND_DOWN));
				dao.updata(bean);
				transfer.add(bean);
				kTranLineTask.add(bean);
				match_list.remove(bean.getTran_log_id());
			}
		} else {// 计价币种
			DGSymbolConfBean dgSymbolConfBean = dgSymbolConfUtil.get(split[0]);

			BigDecimal bol = dgSymbolDescriptUtil.reckonInQuote(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, bean.getTo_limit_num());

			if (bol!=null) {

				BigDecimal multiply = fee_num.multiply(system_fee_scale).setScale(dgSymbolConfBean.getPrecision(), BigDecimal.ROUND_UP);

				saveSystem_fee_scale(multiply, swapBean.symbol, bean.getTo_address(), dgSymbolConfUtil.get(split[1]));
			

				bean.setStart_base_total(swapBean.symbol_des.getBase_num());
				bean.setStart_quote_total(swapBean.symbol_des.getQuote_num());
				
				BigDecimal inQuote = dgSymbolDescriptUtil.inQuote(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, fee_num.subtract(multiply),bol);
			

				bean.setEnd_base_total(swapBean.symbol_des.getBase_num());
				bean.setEnd_quote_total(swapBean.symbol_des.getQuote_num());
				
				bean.setTo_num(inQuote);
				bean.setScale(bc_amount.divide(inQuote, 8, BigDecimal.ROUND_DOWN));
				dao.updata(bean);
				transfer.add(bean);
				kTranLineTask.add(bean);
				match_list.remove(bean.getTran_log_id());
			}
		}
	}

	// 系统手续费收入
	private void saveSystem_fee_scale(BigDecimal multiply, DGSymbolBean symbol, String string, DGSymbolConfBean dgSymbolConfBean) {
		SystemInFeeBean bean = new SystemInFeeBean();
		bean.setFee(multiply).setSymbol(symbol.getSymbol());
		bean.setName(dgSymbolConfBean.getSymbol());
		bean.setBlock(dgSymbolConfBean.getBlock_name()).setContract_address(dgSymbolConfBean.getContract_address());
		bean.setUser_address(string);
		bean = dao.save(bean);
	}

	private void addNewLog() {
		List<DGTranLogBean> selectList = dao.selectList(new DGTranLogBean().setLog_status(DGTranLogBean.log_status_0));
		for (DGTranLogBean bean : selectList) {
			if (match_list.get(bean.getTran_log_id()) != null) {
				continue;
			}
			bean.setLog_status(DGTranLogBean.log_status_4);
			dao.updata(bean);
			match_list.put(bean.getTran_log_id(), bean);
		}
	}

}

package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.sql.SqlServer;
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

	ConcurrentLinkedDeque<DGTranLogBean> match_list = new ConcurrentLinkedDeque<>();

	@Override
	public void init() {
		super.init();
		dao.createTable(SystemInFeeBean.class);
		List<DGTranLogBean> selectList = dao.selectList(new DGTranLogBean().setLog_status(DGTranLogBean.log_status_4));
		match_list.addAll(selectList);
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
		Iterator<DGTranLogBean> iterator = match_list.iterator();
		while (iterator.hasNext()) {
			DGTranLogBean bean = iterator.next();
			if (bean.getTran_log_create_time().getTime() + bean.getTo_limit_time() * 60 * 1000 < currentTimeMillis) {
				try {
					dao.getSqlServer().setAutoCommit(false);
					rollback(bean);
					dao.getSqlServer().commit();
				} catch (Exception e) {
					e.printStackTrace();
					try {
						dao.getSqlServer().rollback();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} finally {
					SqlServer.reback();
				}
				iterator.remove();
			}
		}
		iterator = match_list.iterator();
		while (iterator.hasNext()) {
			try {
				DGTranLogBean bean = iterator.next();
				dao.getSqlServer().setAutoCommit(false);
				handle(bean, iterator);
				dao.getSqlServer().commit();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					dao.getSqlServer().rollback();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} finally {
				SqlServer.reback();
			}
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
		transfer.add(bean);
	}

	@Autowired
	private KTranLineTask kTranLineTask;

	private BigDecimal system_fee_scale = new BigDecimal("0.5");

	private void handle(DGTranLogBean bean, Iterator<DGTranLogBean> iterator) throws SQLException, Exception {
		if (bean.getLog_type() != DGTranLogBean.log_type_0) {
			return;
		}
		SwapBean swapBean = manager.get(bean.getDg_symbol());
		BigDecimal bc_amount = bean.getFrom_num();
		BigDecimal fee_num = bean.getFee_num();
		bc_amount = bc_amount.subtract(fee_num);
		String[] split = bean.getDg_symbol().split("[/]");
		if (split[0].equals(bean.getFrom_token_name())) {// 基本币种

			DGSymbolConfBean dgSymbolConfBean = dgSymbolConfUtil.get(split[1]);

			boolean bol = dgSymbolDescriptUtil.reckonInBase(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, bean.getTo_limit_num());
			if (bol) {

				BigDecimal multiply = fee_num.multiply(system_fee_scale);

				saveSystem_fee_scale(multiply, swapBean.symbol, bean.getTo_address(), dgSymbolConfUtil.get(split[0]));

				BigDecimal inBase = dgSymbolDescriptUtil.inBase(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, multiply);
				bean.setTo_num(inBase);
				bean.setScale(inBase.divide(bc_amount, 8, BigDecimal.ROUND_DOWN));
				dao.updata(bean);
				dao.getSqlServer().commit();
				transfer.add(bean);
				kTranLineTask.add(bean);
				iterator.remove();
			}
		} else {// 计价币种
			DGSymbolConfBean dgSymbolConfBean = dgSymbolConfUtil.get(split[0]);

			boolean bol = dgSymbolDescriptUtil.reckonInQuote(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, bean.getTo_limit_num());

			System.out.println(bean + "验算：" + bol + "->" + bc_amount + dgSymbolConfBean.getPrecision() + bean.getTo_limit_num());
			if (bol) {

				BigDecimal multiply = fee_num.multiply(system_fee_scale);

				saveSystem_fee_scale(multiply, swapBean.symbol, bean.getTo_address(), dgSymbolConfUtil.get(split[1]));
				BigDecimal inQuote = dgSymbolDescriptUtil.inQuote(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, multiply);
				bean.setTo_num(inQuote);
				bean.setScale(bc_amount.divide(inQuote, 8, BigDecimal.ROUND_DOWN));
				dao.updata(bean);
				dao.getSqlServer().commit();
				transfer.add(bean);
				kTranLineTask.add(bean);
				iterator.remove();
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
			bean.setLog_status(DGTranLogBean.log_status_4);
			dao.updata(bean);
			match_list.add(bean);
		}
	}

}

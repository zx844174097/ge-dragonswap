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

import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
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
		
		while(iterator.hasNext()) {
			DGTranLogBean bean = iterator.next();
			if (bean.getTran_log_create_time().getTime() + bean.getTo_limit_time() * 60 * 1000 < currentTimeMillis) {
				rollback(bean);
				iterator.remove();
				continue;
			}
			try {
				dao.getSqlServer().setAutoCommit(false);
				handle(bean,iterator);
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
		
		addNewLog();
	}

	/**
	 * 退钱
	 * 
	 * @param bean
	 */
	private void rollback(DGTranLogBean bean) {
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

	private void handle(DGTranLogBean bean, Iterator<DGTranLogBean> iterator) throws SQLException, Exception {
		SwapBean swapBean = manager.get(bean.getDg_symbol());
		BigDecimal bc_amount = bean.getFrom_num();
		String[] split = bean.getDg_symbol().split("[/]");
		if (split[0].equals(bean.getFrom_token_name())) {// 基本币种

			DGSymbolConfBean dgSymbolConfBean = dgSymbolConfUtil.get(split[1]);

			boolean bol = dgSymbolDescriptUtil.reckonInBase(bc_amount, dgSymbolConfBean.getPrecision(), swapBean, bean.getTo_limit_num());
			if (bol) {
				BigDecimal inBase = dgSymbolDescriptUtil.inBase(bc_amount, dgSymbolConfBean.getPrecision(), swapBean);
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
			if (bol) {
				BigDecimal inQuote = dgSymbolDescriptUtil.inQuote(bc_amount, dgSymbolConfBean.getPrecision(), swapBean);
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

	private void addNewLog() {
		List<DGTranLogBean> selectList = dao.selectList(new DGTranLogBean().setLog_status(DGTranLogBean.log_status_0));
		for (DGTranLogBean bean : selectList) {
			bean.setLog_status(DGTranLogBean.log_status_4);
			dao.updata(bean);
			match_list.add(bean);
		}
	}

}

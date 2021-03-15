package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.util.Other;

import cn.hutool.core.date.DateUtil;
import cn.net.mugui.ge.DraGonSwap.bean.DGCertQuotes;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;

/**
 * 交易凭证K线图
 * 
 * @author Administrator
 *
 */
@AutoTask
@Task
public class KCertLineTask extends TaskImpl {
	@Override
	public void init() {
		super.init();
		dao.createTable(DGCertQuotes.class);
//		linkedList.addAll(dao.selectList(new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_7)));
	}

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

	private ConcurrentLinkedDeque<DGKeepBean> linkedList = new ConcurrentLinkedDeque<>();

	private void handle() {
		while (true) {

			DGKeepBean poll = linkedList.poll();
			if (poll == null) {
				synchronized (this) {
					poll = linkedList.poll();
					if (poll == null) {
						try {
							this.wait();
							continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			for (int i : DGCertQuotes.types) {
//				if (poll == null) {
//					List<DGSymbolBean> selectList = dao.selectList(new DGSymbolBean().setSymbol_status(DGSymbolBean.SYMBOL_STATUS_1));
//					for (DGSymbolBean dgSymbolBean : selectList) {
//						newQuotes(i, dgSymbolBean.getSymbol());
//					}
//				} else {
				DGCertQuotes newQuotes = newQuotes(i, poll.getDg_symbol());
				newQuotes.setCount(newQuotes.getCount() + 1);
				newQuotes.setCount_all(newQuotes.getCount_all() + 1);
				newQuotes.setCert_log_id_end(poll.getDg_keep_id());
				if (poll.getKeep_type() == DGKeepBean.keep_type_0) {
					newQuotes.setEnd_base_num(newQuotes.getEnd_base_num().add(poll.getBase_num()));
					newQuotes.setEnd_quote_num(newQuotes.getEnd_quote_num().add(poll.getQuotes_num()));
					newQuotes.setEnd_token_num(newQuotes.getEnd_token_num().add(poll.getToken_num()));
				} else {
					newQuotes.setEnd_base_num(newQuotes.getEnd_base_num().subtract(poll.getBase_num()));
					newQuotes.setEnd_quote_num(newQuotes.getEnd_quote_num().subtract(poll.getQuotes_num()));
					newQuotes.setEnd_token_num(newQuotes.getEnd_token_num().subtract(poll.getToken_num()));
				}
				if (newQuotes.getStart_token_num().compareTo(BigDecimal.ZERO) > 0) {
					BigDecimal divide = newQuotes.getEnd_token_num().subtract(newQuotes.getStart_token_num()).divide(newQuotes.getStart_token_num(), 8, BigDecimal.ROUND_DOWN);
					newQuotes.setScale(divide);
				}
				dao.updata(newQuotes);
//				}
			}
		}
	}

	@Autowired
	private DGDao dao;

	@Autowired
	private DGConf conf;

	private DGCertQuotes newQuotes(int i, String symbol) {
		long now_time = System.currentTimeMillis();
		DGCertQuotes dgQuotes = new DGCertQuotes().setType(i).setMarket(symbol);
		DGCertQuotes last = dao.selectDESC(dgQuotes);
		if (last == null || isNewQuotesTime(last.getCreate_time().getTime(), now_time, i)) {
			String[] split = dgQuotes.getMarket().split("[/]");
			dgQuotes.setSymbol_l(split[0]);
			dgQuotes.setSymbol_r(split[1]);

			if (last == null) {
				dgQuotes.setStart_base_num(BigDecimal.ZERO);
				dgQuotes.setStart_quote_num(BigDecimal.ZERO);
				dgQuotes.setStart_token_num(BigDecimal.ZERO);
				dgQuotes.setCert_log_id_start(0);
				dgQuotes.setCount_all(0);
			} else {
				dgQuotes.setStart_base_num(last.getEnd_base_num());
				dgQuotes.setStart_quote_num(last.getEnd_quote_num());
				dgQuotes.setStart_token_num(last.getEnd_token_num());
				dgQuotes.setCert_log_id_start(last.getCert_log_id_end());
				dgQuotes.setCount_all(last.getCount_all());
			}
			dgQuotes.setEnd_base_num(dgQuotes.getStart_base_num());
			dgQuotes.setEnd_quote_num(dgQuotes.getStart_quote_num());
			dgQuotes.setEnd_token_num(dgQuotes.getStart_token_num());
			dgQuotes.setCert_log_id_end(dgQuotes.getCert_log_id_start());
			dgQuotes.setCount(0);
			dgQuotes.setScale(BigDecimal.ZERO);
			dgQuotes.setCreate_time(new Date());
			dgQuotes = dao.save(dgQuotes);
			return dgQuotes;
		}
		return last;
	}

	/**
	 * 判断时间是否过时
	 * 
	 * @param last_time
	 * @param time
	 * @param i
	 * @return
	 */
	private boolean isNewQuotesTime(long last_time, long time, int i) {
		switch (i) {// 实时1 1day 1 week 1moth (1 2 3 4 )
		case 1:
			return true;
		case 2:
			if (time - DateUtil.beginOfDay(new Date(last_time)).getTime() > 24 * 1000 * 60 * 60) {
				return true;
			}
			break;
		case 3:
			if (time > DateUtil.offsetWeek(DateUtil.beginOfDay(new Date(last_time)), 1).getTime()) {
				return true;
			}
			break;
		case 4:
			if (time > DateUtil.offsetMonth(DateUtil.beginOfDay(new Date(last_time)), 1).getTime()) {
				return true;
			}
			break;

		}
		return false;
	}

	public void add(DGKeepBean dgKeepBean) {
		synchronized (this) {
			linkedList.add(dgKeepBean);
			this.notifyAll();
		}
	}

}

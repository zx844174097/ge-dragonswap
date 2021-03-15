package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.util.Other;

import cn.hutool.core.date.DateUtil;
import cn.net.mugui.ge.DraGonSwap.bean.DGQuotes;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;

/**
 * 交易k线图
 * 
 * @author Administrator
 *
 */
@AutoTask
@Task
public class KTranLineTask extends TaskImpl {

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

	private ConcurrentLinkedDeque<DGTranLogBean> linkedList = new ConcurrentLinkedDeque<>();

	private void handle() {
		while (true) {

			DGTranLogBean poll = linkedList.poll();
			if (poll == null) {
				synchronized (this) {
					poll = linkedList.poll();
					if (poll == null) {
						try {
							this.wait(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			for (int i : DGQuotes.types) {
				if (poll == null) {
					List<DGSymbolBean> selectList = dao.selectList(new DGSymbolBean().setSymbol_status(DGSymbolBean.SYMBOL_STATUS_1));
					for (DGSymbolBean dgSymbolBean : selectList) {
						 newQuotes(i, dgSymbolBean.getSymbol());
					}
				} else {
					DGQuotes newQuotes = newQuotes(i, poll.getDg_symbol());
					BigDecimal scale = poll.getScale();
					newQuotes.setQ_close(scale);
					if (newQuotes.getQ_high().compareTo(scale) < 0) {
						newQuotes.setQ_high(scale);
					}
					if (newQuotes.getQ_low().compareTo(scale) > 0) {
						newQuotes.setQ_low(scale);
					}
					newQuotes.setQ_count(newQuotes.getQ_count() + 1);
					if (poll.getFrom_token_name().equals(newQuotes.getQ_symbol_l())) {
						newQuotes.setQ_amount(poll.getFrom_num().add(newQuotes.getQ_amount()));
						newQuotes.setQ_vol(poll.getTo_num().add(newQuotes.getQ_vol()));
					} else {
						newQuotes.setQ_amount(poll.getTo_num().add(newQuotes.getQ_amount()));
						newQuotes.setQ_vol(poll.getFrom_num().add(newQuotes.getQ_vol()));
					}
					if (newQuotes.getTran_log_id_start() == null) {
						newQuotes.setTran_log_id_start(poll.getTran_log_id());
					}
					newQuotes.setTran_log_id_end(poll.getTran_log_id());
					dao.updata(newQuotes);
				}
			}
		}
	}

	@Autowired
	private DGDao dao;

	@Autowired
	private DGConf conf;

	private DGQuotes newQuotes(int i, String symbol) {
		long now_time = System.currentTimeMillis();
		DGQuotes dgQuotes = new DGQuotes().setQ_type(i).setQ_market(symbol);
		DGQuotes last = dao.selectDESC(dgQuotes);
		if (last == null || isNewQuotesTime(last.getQ_create_time().getTime(), now_time, i)) {
			BigDecimal close = BigDecimal.ZERO;
			if (last == null) {
				String value = conf.getValue(symbol + "_init_scale");
				if (StringUtils.isBlank(value)) {
					conf.save(symbol + "_init_scale", value = "0", "初始比例");
				}
				close = new BigDecimal(value);
			} else {
				close = last.getQ_close();
			}
			String[] split = dgQuotes.getQ_market().split("[/]");
			dgQuotes.setQ_symbol_l(split[0]);
			dgQuotes.setQ_symbol_r(split[1]); 
			dgQuotes.setQ_amount(BigDecimal.ZERO);
			dgQuotes.setQ_vol(BigDecimal.ZERO);
			dgQuotes.setQ_count(0);
			dgQuotes.setQ_close(close);
			dgQuotes.setQ_high(close);
			dgQuotes.setQ_low(close);
			dgQuotes.setQ_open(close);
			dgQuotes.setQ_create_time(new Date());
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
		switch (i) {// 5 30 60 1day 1moth (1 2 3 4 5 )
		case 1:
			if (time - last_time > 5 * 1000 * 60) {
				return true;
			}
			break;
		case 2:
			if (time - last_time > 30 * 1000 * 60) {
				return true;
			}
			break;
		case 3:
			if (time - last_time > 60 * 1000 * 60) {
				return true;
			}
			break;
		case 4:
			if (time - DateUtil.beginOfDay(new Date(last_time)).getTime() > 24 * 1000 * 60 * 60) {
				return true;
			}
			break;
		case 5:
			if (time > DateUtil.offsetMonth(DateUtil.beginOfDay(new Date(last_time)), 1).getTime()) {
				return true;
			}
			break;
		}
		return false;
	}

	public void add(DGTranLogBean tranLogBean) {
		synchronized (this) {
			linkedList.add(tranLogBean);
			this.notifyAll();
		}
	}
}

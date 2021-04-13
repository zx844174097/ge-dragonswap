package cn.net.mugui.ge.DraGonSwap.count;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskCycleImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.hutool.core.date.DateUtil;
import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;

/**
 * 用户出入金差值表处理器
 * 
 * @author Administrator
 *
 */
@Task()
@AutoTask
public class InAndOutTask extends TaskCycleImpl<DGTranLogBean> {

	@Autowired
	private DGConf conf;

	@Autowired
	private DGDao dao;

	@Override
	public void init() {
		super.init();
		dao.createTable(InAndOutBean.class);
//		List<DGTranLogBean> selectList = dao.selectList(DGTranLogBean.class,
//				Select.q(new DGTranLogBean()).where(Where.q(new DGTranLogBean().setLog_type(DGTranLogBean.log_type_0)).eq("log_status", 5).limit(999999)));
//		getCycleList().addAll(selectList);
	}

	@Override
	protected void handle(DGTranLogBean log) {
		InAndOutBean bean = new InAndOutBean().setAddress(log.getTo_address()).setSymbol(log.getDg_symbol());
		bean = dao.selectDESC(bean);
		if (bean == null || DateUtil.beginOfDay(log.getTran_log_create_time()).getTime() - DateUtil.beginOfDay(bean.getCreate_time()).getTime() >= 24 * 60 * 60 * 1000) {
			bean = new InAndOutBean().setAddress(log.getTo_address()).setCreate_time(log.getTran_log_create_time());
			bean.setSymbol(log.getDg_symbol());
			bean.setBase_diff(BigDecimal.ZERO);
			bean.setBase_in(BigDecimal.ZERO);
			bean.setBase_out(BigDecimal.ZERO);
			bean.setQuote_diff(BigDecimal.ZERO);
			bean.setQuote_in(BigDecimal.ZERO);
			bean.setQuote_out(BigDecimal.ZERO);
			bean = dao.save(bean);
		}
		if (log.getDg_symbol().indexOf(log.getFrom_token_name()) == 0) {// 基本币种进入
			bean.setBase_in(log.getFrom_num().add(bean.getBase_in()));
			bean.setQuote_out(log.getTo_num().add(bean.getQuote_out()));
		} else {// 计价币种入
			bean.setQuote_in(log.getFrom_num().add(bean.getQuote_in()));
			bean.setBase_out(log.getTo_num().add(bean.getBase_out()));
		}
		bean.setQuote_diff(bean.getQuote_in().subtract(bean.getQuote_out()));
		bean.setBase_diff(bean.getBase_in().subtract(bean.getBase_out()));
		dao.updata(bean);
	}
}

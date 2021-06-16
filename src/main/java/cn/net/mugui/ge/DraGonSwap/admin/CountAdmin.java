package cn.net.mugui.ge.DraGonSwap.admin;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mugui.Mugui;
import com.mugui.spring.base.Module;
import com.mugui.spring.net.authority.Authority;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.net.mugui.ge.DraGonSwap.count.InAndOutBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

/**
 * 交易对统计
 * 
 * @author Administrator
 *
 */
@Authority(true)
@Component
@Module(name = "symbol.admin", type = "count")
public class CountAdmin implements Mugui {

	@Autowired
	private DGDao dao;

	/**
	 * 进出差额日统计
	 * 
	 * @param bag
	 * @return
	 */
	public Message diff_day(NetBag bag) {
		InAndOutBean bean = InAndOutBean.newBean(InAndOutBean.class, bag.getData());
		if (StringUtils.isBlank(bean.getSymbol())) {
			return Message.error("请选择交易对");
		}
		Integer integer = bean.get().getInteger("day");
		DateTime offsetDay = DateUtil.beginOfDay(new Date());
		if (integer != null) {
			offsetDay = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -integer));
		}
		Select where = Select
				.q(" sum(base_in) as base_in ,sum(base_out) as base_out,sum(base_diff) as base_diff,sum(quote_in)  as quote_in,sum(quote_out) as quote_out,sum(quote_diff) as quote_diff ", bean)
				.where(Where.q(bean).between("create_time", DateUtil.formatDateTime(offsetDay), DateUtil.now()));

		return Message.ok(dao.select(InAndOutBean.class, where));
	}

	/**
	 * 进出差额 用户 日列表
	 * 
	 * @param bag
	 * @return
	 */
	public Message diff_day_list(NetBag bag) {
		InAndOutBean bean = InAndOutBean.newBean(InAndOutBean.class, bag.getData());
		if (StringUtils.isBlank(bean.getSymbol())) {
			return Message.error("请选择交易对");
		}
		Integer integer = bean.get().getInteger("day");
		DateTime offsetDay = DateUtil.beginOfDay(new Date());
		if (integer != null) {
			offsetDay = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), -integer));
		}
		Select where = Select.q(
				" symbol, address, sum(base_in) as base_in ,sum(base_out) as base_out,sum(base_diff) as base_diff,sum(quote_in)  as quote_in,sum(quote_out) as quote_out,sum(quote_diff) as quote_diff ",
				bean).where(Where.q(bean).ne("address", "TEWgz9NQp9S1H3B4fefhqjFJrGLFaBKyhe").ne("address", "TDG6CEjscmbWJfPBzYw4WRWXVtw2NGEYHx").between("create_time", DateUtil.formatDateTime(offsetDay), DateUtil.now()).groupBy("address"));
		return Message.ok(dao.selectArray(InAndOutBean.class, where));
	}

}

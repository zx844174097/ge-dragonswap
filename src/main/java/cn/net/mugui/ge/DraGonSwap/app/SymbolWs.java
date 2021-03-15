package cn.net.mugui.ge.DraGonSwap.app;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.Mugui;
import com.mugui.spring.base.Module;
import com.mugui.spring.net.authority.Authority;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.spring.net.dblistener.PageUtil;
import com.mugui.spring.net.websocket.WebSocket;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.net.mugui.ge.DraGonSwap.bean.DGCertQuotes;
import cn.net.mugui.ge.DraGonSwap.bean.DGQuotes;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

/**
 * 交易对websocket处理类
 * 
 * @author Administrator
 *
 */
@Module(name = "symbol", type = "ws")
@Authority(true)
@WebSocket(type = 0, blank = 2000)
public class SymbolWs implements Mugui {

	@Autowired
	private DGDao dao;

	private TimedCache<String, Integer> quotes_Cache = CacheUtil.newTimedCache(60000);

//	private static class TempBean {
//		Integer quotes_id;
//
////		String data;
//	}

	/**
	 * K线图
	 * 
	 * @param bag
	 * @return
	 */
	public Message kLine(NetBag bag) {
		DGQuotes newBean = DGQuotes.newBean(DGQuotes.class, bag.getData());
		if (StringUtils.isBlank(newBean.getQ_market())) {
			return Message.error("参数错误");
		}
		PageUtil.offsetPage(bag);
		Integer tempBean = quotes_Cache.get(bag.getSession());
		if (tempBean != null) {
			Integer quotes_id = tempBean;
			if (newBean.getQuotes_id()!=null&&newBean.getQuotes_id() > tempBean) {
				quotes_id = newBean.getQuotes_id();
			}
			newBean.setQuotes_id(null);
			Select where = Select.q(newBean).where(Where.q(newBean).ge("quotes_id", quotes_id));
			List<DGQuotes> selectList = dao.selectList(DGQuotes.class, where);
			tempBean = selectList.get(selectList.size() - 1).getQuotes_id();
			quotes_Cache.put(bag.getSession(), tempBean);
			return Message.ok(selectList);
		}
		List<DGQuotes> selectList = dao.selectListDESC(newBean);
		if (selectList.isEmpty()) {
			return Message.ok(selectList);
		}
//		tempBean = new TempBean();
		tempBean = selectList.get(0).getQuotes_id();
//		tempBean.data = bag.getData().toString();
		quotes_Cache.put(bag.getSession(), tempBean);
		return Message.ok(selectList);
	}

	/**
	 * K线图
	 * 
	 * @param bag
	 * @return
	 */
	public Message kCertLine(NetBag bag) {
		DGCertQuotes newBean = DGCertQuotes.newBean(DGCertQuotes.class, bag.getData());
		if (StringUtils.isBlank(newBean.getMarket())) {
			return Message.error("参数错误");
		}
		PageUtil.offsetPage(bag);
		Integer tempBean = quotes_Cache.get(bag.getSession());
		if (tempBean != null) {
			Integer quotes_id = tempBean;
			if (newBean.getQuotes_id()!=null&&newBean.getQuotes_id() > tempBean) {
				quotes_id = newBean.getQuotes_id();
			}
			newBean.setQuotes_id(null);
			Select where = Select.q(newBean).where(Where.q(newBean).ge("quotes_id", quotes_id));
			List<DGQuotes> selectList = dao.selectList(DGQuotes.class, where);
			tempBean = selectList.get(selectList.size() - 1).getQuotes_id();
			quotes_Cache.put(bag.getSession(), tempBean);
			return Message.ok(selectList);
		}
		List<DGCertQuotes> selectList = dao.selectListDESC(newBean);
		if (selectList.isEmpty()) {
			return Message.ok(selectList);
		}
		tempBean = selectList.get(0).getQuotes_id();
//		tempBean.data = bag.getData().toString();
		quotes_Cache.put(bag.getSession(), tempBean);
		return Message.ok(selectList);
	}
}

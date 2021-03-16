package cn.net.mugui.ge.DraGonSwap.cache;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mugui.spring.base.Cache;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.spring.net.cache.CacheModel;
import com.mugui.util.Other;

import cn.net.mugui.ge.util.SessionImpl;

@Cache(value = "symbol.method.kLine")
@Component
public class KlineCache implements CacheModel {

	private static long GlobalCache_time = 5000;
	private static ConcurrentHashMap<String, CacheBean> map = new ConcurrentHashMap<>();

	public class CacheBean {
		public CacheBean(Object data) {
			body = data;
			time = System.currentTimeMillis();
		}

		private long time;
		private Object body;
	}

	@Override
	public NetBag load(NetBag bag) {
		String str = String.valueOf(bag.getData());
		if (str.length() > 512) {
			str = str.substring(0, 512);
		}
		str = str.replaceAll("\\s*", "");
		String string = (bag.getFunc() + Other.MD5(str));
		CacheBean bean = null;
		if ((bean = map.get(string)) != null) {
			if (System.currentTimeMillis() - bean.time < GlobalCache_time) {
				bag.setData(bean.body);
				return null;
			}
			map.remove(string);
		}
		SessionImpl.getSession(bag).setAttribute("method:" + bag.getFunc(), string);
		return bag;
	}

	@Override
	public void save(Message message, NetBag bag) {
		if (message.getType() == Message.SUCCESS) {
			HttpSession session = SessionImpl.getSession(bag);
			if (session != null) {
				String string = (String) session.getAttribute("method:" + bag.getFunc());
				if (StringUtils.isNotBlank(string)) {
					map.put(string, new CacheBean(message));
				}
				session.removeAttribute("method:" + bag.getFunc());
			}
		}
	}

}

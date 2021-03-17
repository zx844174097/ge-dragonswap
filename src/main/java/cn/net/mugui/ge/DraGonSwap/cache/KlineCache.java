package cn.net.mugui.ge.DraGonSwap.cache;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.mugui.spring.base.Cache;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.spring.net.cache.CacheModel;
import com.mugui.util.Other;

import cn.hutool.cache.impl.TimedCache;
import cn.net.mugui.ge.util.SessionImpl;

@Cache(value = { "symbol.method.kLine", "symbol.method.kCertLine" })
@Component
public class KlineCache implements CacheModel {

	private static long GlobalCache_time = 5000;
	private static TimedCache<String, Message> map = new TimedCache<String, Message>(GlobalCache_time);

	@Override
	public NetBag load(NetBag bag) {
		String str = String.valueOf(bag.getData());
		if (str.length() > 512) {
			str = str.substring(0, 512);
		}
		str = str.replaceAll("\\s*", "");
		String string = (bag.getFunc() + Other.MD5(str));
		Message bean = null;

		if ((bean = map.get(string, false)) != null) {
			bag.setData(bean);
			return null;
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
					map.put(string, message);
					session.removeAttribute("method:" + bag.getFunc());
				}
			}
		}
	}

}

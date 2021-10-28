package cn.net.mugui.ge.DraGonSwap.cache;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mugui.spring.base.Cache;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.spring.net.cache.CacheModel;
import com.mugui.util.Other;

import cn.hutool.cache.impl.TimedCache;
import cn.net.mugui.ge.DraGonSwap.util.LockUtil;
import cn.net.mugui.ge.util.SessionImpl;

@Cache(value = { "symbol.method.kLine", "symbol.method.kCertLine" })
@Component
public class KlineCache implements CacheModel {

	private static long GlobalCache_time = 5000;
	private static TimedCache<String, Message> map = new TimedCache<String, Message>(GlobalCache_time);
	static {
		map.schedulePrune(60000);
	}

	private String getKey(NetBag bag) {
		return (bag.getFunc() + Other.MD5(bag.getData().toString()));
	}

	@Autowired
	LockUtil lockUtil;

	@Override
	public NetBag load(NetBag bag) {
		String key = getKey(bag);
		Message bean = null;
		lockUtil.lock(key);
		if ((bean = map.get(key, false)) != null) {
			bag.setData(bean);
			lockUtil.unlock(key);
			return null;
		}
//		SessionImpl.getSession(bag).setAttribute("method:" + bag.getFunc(), string);
		return bag;
	}

	@Override
	public void save(Message message, NetBag bag) {
		if (message.getType() == Message.SUCCESS) {
			HttpSession session = SessionImpl.getSession(bag);
			if (session != null) {
				String key = getKey(bag);
				map.put(key, message);
				lockUtil.unlock(key);
			}
		}
	}

}

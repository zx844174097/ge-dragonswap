package cn.net.mugui.ge.DraGonSwap.util;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class LockUtil {

	HashMap<String, ReentrantLock> map = new HashMap<>();

	public void lock(String str) {
		ReentrantLock bs = map.get(str);
		if (bs == null) {
			synchronized (map) {
				bs = map.get(str);
				if (bs == null) {
					map.put(str, bs = new ReentrantLock());
				}
			}
		}
		bs.lock();
	}

	public void unlock(String str) {
		ReentrantLock reentrantLock = map.get(str);
		reentrantLock.unlock();
	}
}

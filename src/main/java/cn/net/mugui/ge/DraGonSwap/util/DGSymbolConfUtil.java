package cn.net.mugui.ge.DraGonSwap.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.hutool.cache.impl.TimedCache;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@Component
public class DGSymbolConfUtil {

	@Autowired
	private DGDao dao;

	TimedCache<String, DGSymbolConfBean> newTimedCache = new TimedCache<String, DGSymbolConfBean>(60000);

	public DGSymbolConfBean get(String symbol) {
		DGSymbolConfBean dgSymbolConfBean = newTimedCache.get(symbol);
		if (dgSymbolConfBean == null) {
			synchronized (newTimedCache) {
				dgSymbolConfBean = newTimedCache.get(symbol);
				if (dgSymbolConfBean == null) {
					dgSymbolConfBean = dao.select(new DGSymbolConfBean().setSymbol(symbol));
					if (dgSymbolConfBean == null) {
						return null;
					}
					newTimedCache.put(symbol, dgSymbolConfBean);
				}
			}
		}
		return dgSymbolConfBean;
	}

	public DGSymbolConfBean getByContract_address(String contract_address) {
		DGSymbolConfBean dgSymbolConfBean = newTimedCache.get(contract_address);
		if (dgSymbolConfBean == null) {
			synchronized (newTimedCache) {
				dgSymbolConfBean = newTimedCache.get(contract_address);
				if (dgSymbolConfBean == null) {
					dgSymbolConfBean = dao.select(new DGSymbolConfBean().setContract_address(contract_address));
					if (dgSymbolConfBean == null) {
						return null;
					}
					newTimedCache.put(contract_address, dgSymbolConfBean);
				}
			}
		}
		return dgSymbolConfBean;
	}
}

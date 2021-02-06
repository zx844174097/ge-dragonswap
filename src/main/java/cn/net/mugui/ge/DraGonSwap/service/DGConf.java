package cn.net.mugui.ge.DraGonSwap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.net.mugui.ge.DraGonSwap.bean.DGConfBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@Component
public class DGConf {

	public Integer system_user_id() {
		return Integer.parseInt(getValue("system_user_id"));
	}

	@Autowired
	private DGDao systemDao;

	public void init() {
	}

	public String getValue(String index) {
		DGConfBean system = get(index);
		if (system == null)
			return null;
		return system.getValue();
	}

	public DGConfBean get(String index) {
		DGConfBean system = new DGConfBean();
		system.setKey(index);
		return systemDao.select(system);
	}

	public void setValue(String key, String value) {
		DGConfBean system = get(key);
		if (system == null) {
			system = new DGConfBean();
			system.setKey(key);
			system.setValue(value);
			system = systemDao.save(system);
		} else {
			system.setValue(value);
			systemDao.updata(system);
		}

	}

	public void save(DGConfBean systemConf) {
		DGConfBean temp = get(systemConf.getKey());
		if (temp == null) {
			systemDao.save(systemConf);
		} else {
			systemConf.setDg_conf_id(temp.getDg_conf_id());
			systemDao.updata(systemConf);
		}
	}

	public void save(String key, Object value, String extra) {
		DGConfBean systemConf = new DGConfBean();
		systemConf.setDetail(extra);
		systemConf.setKey(key);
		systemConf.setValue(value.toString());
		save(systemConf);
	}
}
package cn.net.mugui.ge.DraGonSwap.service;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import cn.net.mugui.ge.DraGonSwap.bean.DGQuotes;
import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@Service
public class DGService implements DGServiceApi {

	@Autowired
	private DGDao dao;

	@Override
	public JSONObject getSymbol(String symbol) {
		DGQuotes select = dao.selectDESC(new DGQuotes().setQ_type(4).setQ_market(symbol));
		DGTranLogBean selectDESC = dao.selectDESC(new DGTranLogBean().setDg_symbol(symbol)
				.setLog_type(DGTranLogBean.log_type_0).setLog_status(DGTranLogBean.log_status_5));
		if (select != null && selectDESC != null) {
			select.get().put("scale", selectDESC.getScale().stripTrailingZeros().toPlainString());
			return select.get();
		} else
			return null;
	}

}

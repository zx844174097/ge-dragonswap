package cn.net.mugui.ge.DraGonSwap.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.base.Manager;
import com.mugui.spring.net.auto.AutoManager;

import cn.net.mugui.ge.DraGonSwap.bean.DGAddressBindBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolCreateBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolPriBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockManager;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@AutoManager
public class DSymbolManager extends Manager<String, SwapBean> {

	@Autowired
	private DGDao dao;
	
	@Autowired
	private BlockManager BlockManager;

	@Override
	public boolean init(Object object) {
		super.init(object);
		dao.createTable(DGKeepBean.class);
		dao.createTable(DGSymbolCreateBean.class);
		dao.createTable(DGSymbolBean.class);
		dao.createTable(DGSymbolDescriptBean.class);
		dao.createTable(DGSymbolPriBean.class);
		dao.createTable(DGSymbolConfBean.class);
		dao.createTable(DGPriAddressBean.class);
		dao.createTable(DGAddressBindBean.class);
		BlockManager.init(object);
		List<DGSymbolBean> selectList = dao.selectList(new DGSymbolBean().setSymbol_status(DGSymbolBean.SYMBOL_STATUS_1));
		for (DGSymbolBean dgSymbolBean : selectList) {
			add(dgSymbolBean);
		}
		return true;
	}

	public void add(Integer dg_symbol_id) {
		add(dao.select(new DGSymbolBean().setDg_symbol_id(dg_symbol_id)));
	}

	public SwapBean get(Integer dg_symbol_id) {
		return get(dao.select(new DGSymbolBean().setDg_symbol_id(dg_symbol_id)).getSymbol());
	}

	@Autowired
	private DGPriAddressCache priAddressCache;

	public void add(DGSymbolBean dgSymbolBean) {
		SwapBean swapBean = new SwapBean();
		swapBean.swap_name = dgSymbolBean.getSymbol();
		swapBean.pri_cert = dao.select(new DGSymbolPriBean().setDg_symbol_id(dgSymbolBean.getDg_symbol_id()).setType(DGSymbolPriBean.type_1));
		swapBean.pri_tran = dao.select(new DGSymbolPriBean().setDg_symbol_id(dgSymbolBean.getDg_symbol_id()).setType(DGSymbolPriBean.type_0));
		swapBean.symbol = dgSymbolBean;
		swapBean.symbol_des = dao.select(new DGSymbolDescriptBean().setDg_symbol_id(dgSymbolBean.getDg_symbol_id()));
		add(swapBean.swap_name, swapBean);

		priAddressCache.init(null);

		List<DGSymbolConfBean> selectList = dao.selectList(new DGSymbolConfBean().setSymbol(dgSymbolBean.getBase_currency()));
		for (DGSymbolConfBean dgSymbolConfBean : selectList) {
			priAddressCache.get(dgSymbolBean.getSymbol() + "_0_" + dgSymbolConfBean.getBlock_name());
			priAddressCache.get(dgSymbolBean.getSymbol() + "_1_" + dgSymbolConfBean.getBlock_name());
		}

		selectList = dao.selectList(new DGSymbolConfBean().setSymbol(dgSymbolBean.getQuote_currency()));
		for (DGSymbolConfBean dgSymbolConfBean : selectList) {
			priAddressCache.get(dgSymbolBean.getSymbol() + "_0_" + dgSymbolConfBean.getBlock_name());
			priAddressCache.get(dgSymbolBean.getSymbol() + "_1_" + dgSymbolConfBean.getBlock_name());
		}
	}

	public void update(Integer dg_symbol_id) {
		SwapBean swapBean = get(dg_symbol_id);
		update(swapBean);
	}
	public void update(String dg_symbol) {
		SwapBean swapBean = get(dg_symbol);
		update(swapBean);
	}

	private void update(SwapBean swapBean) {
		swapBean.pri_cert = dao.select(new DGSymbolPriBean().setDg_symbol_id(swapBean.symbol.getDg_symbol_id()).setType(DGSymbolPriBean.type_1));
		swapBean.pri_tran = dao.select(new DGSymbolPriBean().setDg_symbol_id(swapBean.symbol.getDg_symbol_id()).setType(DGSymbolPriBean.type_0));
		swapBean.symbol_des = dao.select(new DGSymbolDescriptBean().setDg_symbol_id(swapBean.symbol.getDg_symbol_id()));
	}

}

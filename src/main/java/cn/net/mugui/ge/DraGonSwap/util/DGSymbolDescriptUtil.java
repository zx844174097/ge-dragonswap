package cn.net.mugui.ge.DraGonSwap.util;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;

@Component
public class DGSymbolDescriptUtil {

	@Autowired
	private DSymbolManager manager;

	@Autowired
	private DGDao dgDao;

	public void updateTotal(String dg_symbol, BigDecimal base, BigDecimal quotes) {
		SwapBean swapBean = manager.get(dg_symbol);
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			symbol_des.setBase_num(symbol_des.getBase_num().add(base));
			symbol_des.setQuote_num(symbol_des.getQuote_num().add(quotes));
			dgDao.updata(symbol_des);
		}
	}

	/**
	 * 入金基本币种
	 * 
	 * @param bc_amount
	 * @param select
	 * @param string
	 * @return 
	 */
	public BigDecimal inBase(BigDecimal bc_amount, int precision, String symbol) {
		SwapBean swapBean = manager.get(symbol);
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getBase_num().add(bc_amount);
			BigDecimal quote_num =  symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getQuote_num().subtract(quote_num);
			symbol_des.setBase_num(add);
			symbol_des.setQuote_num(quote_num);
			dgDao.updata(symbol_des);
			return subtract;
		}

	}
	/**
	 * 入金计价币种
	 * @param bc_amount
	 * @param precision
	 * @param symbol
	 * @return
	 */
	public BigDecimal inQuote(BigDecimal bc_amount, Integer precision, String symbol) {
		SwapBean swapBean = manager.get(symbol);
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getQuote_num().add(bc_amount);

			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getBase_num().subtract(quote_num);
			symbol_des.setQuote_num(add);
			symbol_des.setBase_num(quote_num);
			dgDao.updata(symbol_des);
			return subtract;
		}
	}

}

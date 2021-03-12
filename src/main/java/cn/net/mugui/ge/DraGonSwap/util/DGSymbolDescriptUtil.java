package cn.net.mugui.ge.DraGonSwap.util;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
			symbol_des.setScale(symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 8, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 8, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
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
	public BigDecimal inBase(BigDecimal bc_amount, int precision, SwapBean swapBean) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getBase_num().add(bc_amount);
			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getQuote_num().subtract(quote_num);
			symbol_des.setBase_num(add);
			symbol_des.setQuote_num(quote_num);
			symbol_des.setScale(add.divide(quote_num, 8, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(quote_num.divide(add, 8, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			dgDao.updata(symbol_des);
			return subtract;
		}
	}

	/**
	 * 入金基本币种的估算
	 * 
	 * @param bc_amount
	 * @param precision
	 * @param swapBean
	 * @return
	 */
	public boolean reckonInBase(BigDecimal bc_amount, int precision, SwapBean swapBean, BigDecimal limit_num) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getBase_num().add(bc_amount);
			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getQuote_num().subtract(quote_num);
			if (subtract.compareTo(limit_num) >= 0) {
				return true;
			}
			return false;
		}
	}

	/**
	 * 入金计价币种的估算
	 * 
	 * @param bc_amount
	 * @param precision
	 * @param swapBean
	 * @return
	 */
	public boolean reckonInQuote(BigDecimal bc_amount, int precision, SwapBean swapBean, BigDecimal limit_num) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getQuote_num().add(bc_amount);
			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getBase_num().subtract(quote_num);
			if (subtract.compareTo(limit_num) >= 0) {
				return true;
			}
			return false;
		}
	}

	/**
	 * 入金计价币种
	 * 
	 * @param bc_amount
	 * @param precision
	 * @param symbol
	 * @return
	 */
	public BigDecimal inQuote(BigDecimal bc_amount, Integer precision, SwapBean swapBean) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getQuote_num().add(bc_amount);

			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getBase_num().subtract(quote_num);
			symbol_des.setQuote_num(add);
			symbol_des.setBase_num(quote_num);
			symbol_des.setScale(add.divide(quote_num, 8, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(quote_num.divide(add, 8, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			dgDao.updata(symbol_des);
			return subtract;
		}
	}

}

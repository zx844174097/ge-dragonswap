package cn.net.mugui.ge.DraGonSwap.util;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@Component
public class DGSymbolDescriptUtil {

	@Autowired
	private DGDao dgDao;

	public void updateTotal(SwapBean swapBean, BigDecimal base, BigDecimal quotes) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			symbol_des.setBase_num(symbol_des.getBase_num().add(base));
			symbol_des.setQuote_num(symbol_des.getQuote_num().add(quotes));
			symbol_des.setTotal_num(symbol_des.getBase_num().multiply(symbol_des.getQuote_num()));
			symbol_des.setReverse_scale(symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setScale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			System.out.println("updateTotal->"+symbol_des);
			dgDao.updata(symbol_des);
		}
	}


	/**
	 * 入金基本币种
	 * 
	 * @param bc_amount
	 * @param fee_num
	 * @param select
	 * @param string
	 * @return
	 */
	public BigDecimal inBase(BigDecimal bc_amount, int precision, SwapBean swapBean, BigDecimal fee_num) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getBase_num().add(bc_amount);
			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getQuote_num().subtract(quote_num);
			symbol_des.setBase_num(add.add(fee_num));
			symbol_des.setQuote_num(quote_num);
			symbol_des.setTotal_num(symbol_des.getBase_num().multiply(symbol_des.getQuote_num()));
			symbol_des.setScale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			System.out.println("inBase->"+symbol_des);
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
	 * @param fee_num
	 * @param symbol
	 * @return
	 */
	public BigDecimal inQuote(BigDecimal bc_amount, Integer precision, SwapBean swapBean, BigDecimal fee_num) {
		synchronized (swapBean) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getQuote_num().add(bc_amount);

			BigDecimal base_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getBase_num().subtract(base_num);
			symbol_des.setQuote_num(add.add(fee_num));
			symbol_des.setBase_num(base_num);
			symbol_des.setTotal_num(symbol_des.getBase_num().multiply(symbol_des.getQuote_num()));
			symbol_des.setScale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			System.out.println("inQuote->"+symbol_des);
			dgDao.updata(symbol_des);
			return subtract;
		}
	}

}

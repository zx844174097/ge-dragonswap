package cn.net.mugui.ge.DraGonSwap.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@Component
public class DGSymbolDescriptUtil {

	@Autowired
	private DGDao dgDao;

	ConcurrentHashMap<Integer, byte[]> map = new ConcurrentHashMap<>();

	public byte[] getKey(Integer i) {
		byte[] bs = map.get(i);
		if (bs == null) {
			synchronized (map) {
				bs = map.get(i);
				if (bs == null) {
					map.put(i, bs = new byte[0]);
				}
			}
		}
		return bs;
	}

	public void updateTotal(SwapBean swapBean, BigDecimal base, BigDecimal quotes) {
		synchronized (getKey(swapBean.symbol.getDg_symbol_id())) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			System.out.println("updateTotal 前->" + symbol_des + " " + base + " " + quotes);
			symbol_des.setBase_num(symbol_des.getBase_num().add(base));
			symbol_des.setQuote_num(symbol_des.getQuote_num().add(quotes));
			symbol_des.setTotal_num(symbol_des.getBase_num().multiply(symbol_des.getQuote_num())
					.setScale(32, BigDecimal.ROUND_DOWN).stripTrailingZeros());
			symbol_des.setReverse_scale(
					symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setScale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			System.out.println("updateTotal->" + symbol_des);
			dgDao.updata(symbol_des);
		}
	}

	/**
	 * 入金基本币种
	 * 
	 * @param bc_amount
	 * @param fee_num
	 * @param bol
	 * @param select
	 * @param string
	 * @return
	 */
	public BigDecimal inBase(BigDecimal bc_amount, int precision, SwapBean swapBean, BigDecimal fee_num,
			BigDecimal bol) {
		synchronized (getKey(swapBean.symbol.getDg_symbol_id())) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			System.out.println("inBase 前->" + symbol_des + " " + bc_amount + " " + fee_num);
			BigDecimal add = symbol_des.getBase_num().add(bc_amount);

			BigDecimal quote_num = null;
			BigDecimal subtract = null;
			if (bol != null) {
				subtract=bol;
				quote_num=symbol_des.getQuote_num().subtract(bol);
			} else {
				quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_UP);
				subtract = symbol_des.getQuote_num().subtract(quote_num);
			}

			symbol_des.setBase_num(add.add(fee_num));
			symbol_des.setQuote_num(quote_num);
			symbol_des.setTotal_num(symbol_des.getBase_num().multiply(symbol_des.getQuote_num())
					.setScale(32, BigDecimal.ROUND_DOWN).stripTrailingZeros());
			symbol_des.setScale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(
					symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			System.out.println("inBase 后->" + symbol_des + " " + subtract);
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
	public BigDecimal reckonInBase(BigDecimal bc_amount, int precision, SwapBean swapBean, BigDecimal limit_num) {
		synchronized (getKey(swapBean.symbol.getDg_symbol_id())) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getBase_num().add(bc_amount);
			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getQuote_num().subtract(quote_num);
			if (subtract.compareTo(limit_num) >= 0) {
				limit_num=limit_num.setScale(precision, BigDecimal.ROUND_HALF_DOWN);
				if (limit_num.compareTo(new BigDecimal("0.000002")) <= 0) {
					return subtract.multiply(new BigDecimal("0.9")).setScale(precision,BigDecimal.ROUND_HALF_DOWN);
				}
				return limit_num;
			}
			return null;
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
	public BigDecimal reckonInQuote(BigDecimal bc_amount, int precision, SwapBean swapBean, BigDecimal limit_num) {
		synchronized (getKey(swapBean.symbol.getDg_symbol_id())) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			BigDecimal add = symbol_des.getQuote_num().add(bc_amount);
			BigDecimal quote_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_DOWN);
			BigDecimal subtract = symbol_des.getBase_num().subtract(quote_num);
			if (subtract.compareTo(limit_num) >= 0) {
				limit_num=limit_num.setScale(precision, BigDecimal.ROUND_HALF_DOWN);
				if (limit_num.compareTo(new BigDecimal("0.000002")) <= 0) {
					return subtract.multiply(new BigDecimal("0.9")).setScale(precision,BigDecimal.ROUND_HALF_DOWN);
				}
				return limit_num;
			}
			return null;
		}
	}

	/**
	 * 入金计价币种
	 * 
	 * @param bc_amount
	 * @param precision
	 * @param fee_num
	 * @param bol
	 * @param symbol
	 * @return
	 */
	public BigDecimal inQuote(BigDecimal bc_amount, Integer precision, SwapBean swapBean, BigDecimal fee_num,
			BigDecimal bol) {
		synchronized (getKey(swapBean.symbol.getDg_symbol_id())) {
			DGSymbolDescriptBean symbol_des = swapBean.symbol_des;
			System.out.println("inQuote 前->" + symbol_des + " " + bc_amount + " " + fee_num);
			BigDecimal add = symbol_des.getQuote_num().add(bc_amount);

			BigDecimal base_num = null;
			BigDecimal subtract = null;
			if (bol != null) {
				subtract=bol;
				base_num=symbol_des.getBase_num().subtract(bol);
			} else {
				base_num = symbol_des.getTotal_num().divide(add, precision, BigDecimal.ROUND_UP);
				subtract = symbol_des.getBase_num().subtract(base_num);
			}

			symbol_des.setQuote_num(add.add(fee_num));
			symbol_des.setBase_num(base_num);
			symbol_des.setTotal_num(symbol_des.getBase_num().multiply(symbol_des.getQuote_num())
					.setScale(32, BigDecimal.ROUND_DOWN).stripTrailingZeros());

			symbol_des.setScale(symbol_des.getQuote_num().divide(symbol_des.getBase_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setReverse_scale(
					symbol_des.getBase_num().divide(symbol_des.getQuote_num(), 18, BigDecimal.ROUND_DOWN));
			symbol_des.setSymbol_descript_update_time(new Date());
			System.out.println("inQuote 后->" + symbol_des + " " + subtract);
			dgDao.updata(symbol_des);
			return subtract;
		}
	}

}

package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolCreateBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolConfUtil;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolDescriptUtil;

/**
 * 凭证赎回任务
 * 
 * @author Administrator
 *
 */
@Component
public class DGCertRansomTask {

	@Autowired
	private DGDao dao;

	@Autowired
	private DSymbolManager manager;

	@Autowired
	private DGSymbolDescriptUtil descriptUtil;

	@Autowired
	private DGSymbolConfUtil confUtil;

	public boolean handle(BlockTranBean blockChainBean, DGSymbolBean dgSymbol) {
		SwapBean swapBean = manager.get(dgSymbol.getSymbol());
		DGSymbolCreateBean select = swapBean.create;
		if (select == null) {
			return false;
		}
		if (!select.getToken_address().equals(blockChainBean.getToken())) {
			return false;
		}
		BigDecimal num = blockChainBean.getNum();
		DGKeepBean dgKeepTranLogBean = new DGKeepBean().setDg_symbol(dgSymbol.getSymbol()).setToken_3(select.getToken_address()).setUser_address(blockChainBean.getFrom()).setToken_num(num);
		dgKeepTranLogBean.setKeep_type(DGKeepBean.keep_type_1);
		dgKeepTranLogBean.setHash_3(blockChainBean.getHash());
		dgKeepTranLogBean.setBlock_3(blockChainBean.getBlock());
		DGKeepBean last_dg_keep = dao.selectDESC(new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_7));
		BigDecimal now_out_cert_token_num = last_dg_keep.getNow_out_cert_token_num();

		BigDecimal divide = dgKeepTranLogBean.getToken_num().divide(now_out_cert_token_num, 32, BigDecimal.ROUND_DOWN);
	
		BigDecimal base = swapBean.symbol_des.getBase_num().multiply(divide);
		{// 基本币种处理
			DGSymbolConfBean dgSymbolConfBean = confUtil.get(dgSymbol.getBase_currency());
			base = base.setScale(dgSymbolConfBean.getPrecision());
			dgKeepTranLogBean.setBlock_1(dgSymbolConfBean.getBlock_name());
			dgKeepTranLogBean.setToken_1(dgSymbolConfBean.getContract_address());
			dgKeepTranLogBean.setBase_num(base);
		}

		BigDecimal quote = swapBean.symbol_des.getQuote_num().multiply(divide);
		{// 计价币种处理
			DGSymbolConfBean dgSymbolConfBean = confUtil.get(dgSymbol.getQuote_currency());
			quote = quote.setScale(dgSymbolConfBean.getPrecision());
			dgKeepTranLogBean.setBlock_2(dgSymbolConfBean.getBlock_name());
			dgKeepTranLogBean.setToken_2(dgSymbolConfBean.getContract_address());
			dgKeepTranLogBean.setQuotes_num(quote);
		}

		dgKeepTranLogBean.setLast_out_cert_token_num(now_out_cert_token_num);
		dgKeepTranLogBean.setNow_out_cert_token_num(now_out_cert_token_num.subtract(dgKeepTranLogBean.getToken_num()));


		descriptUtil.updateTotal(swapBean, base.negate(), quote.negate());
		dgKeepTranLogBean = dao.save(dgKeepTranLogBean);
		outTask.add(dgKeepTranLogBean);
		return true;
	}
	
	@Autowired
	private DGCertTokenOutTask outTask;

}

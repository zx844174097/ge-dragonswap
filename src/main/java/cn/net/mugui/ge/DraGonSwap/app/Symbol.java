package cn.net.mugui.ge.DraGonSwap.app;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mugui.Mugui;
import com.mugui.spring.base.Module;
import com.mugui.spring.net.authority.Authority;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.spring.net.dblistener.PageUtil;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;

import cn.net.mugui.ge.DraGonSwap.bean.DGAddressBindBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGQuotes;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.PushRemarkBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DGPriAddressCache;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.util.RedisUtil;

@Authority(true)
@Component
@Module(name = "symbol", type = "method")
public class Symbol implements Mugui {
	/**
	 * 创建一个交易对
	 * 
	 * @param bag
	 * @return
	 */
	public Message create(NetBag bag) {

		return Message.ok();
	}

	@Autowired
	private DSymbolManager manager;

	@Autowired
	private DGPriAddressCache priAddressCache;

	@Autowired
	private DGDao dao;

	/**
	 * 交易对列表
	 * 
	 * @param bag
	 * @return
	 */
	public Message list(NetBag bag) {
		JSONArray array = new JSONArray();

		for (DGSymbolBean dgSymbolBean : dao.selectList(new DGSymbolBean().setSymbol_status(DGSymbolBean.SYMBOL_STATUS_1))) {
			JSONObject jsonObject = dgSymbolBean.get();

			JSONArray select = dao.selectArray(new DGSymbolConfBean().setSymbol(dgSymbolBean.getBase_currency()));

			jsonObject.put("base", select);
			select = dao.selectArray(new DGSymbolConfBean().setSymbol(dgSymbolBean.getQuote_currency()));
			jsonObject.put("quotes", select);

			SwapBean swapBean = manager.get(dgSymbolBean.getSymbol());
			jsonObject.putAll(swapBean.symbol_des.get());
			jsonObject.put("token_address", swapBean.create.getToken_address());

			DGKeepBean dgKeepTranLogBean = new DGKeepBean().setDg_symbol(dgSymbolBean.getSymbol());
			DGKeepBean select2 = dao.select(dgKeepTranLogBean);
			if (select2 != null && select2.getNow_out_cert_token_num() != null) {
				jsonObject.put("now_out_cert_token_num", select2.getNow_out_cert_token_num());
			} else {
				jsonObject.put("now_out_cert_token_num", "0");
			}
			array.add(jsonObject);
		}
		return Message.ok(array);
	}

	public Message address(NetBag bag) {

		DGSymbolBean dgSymbolBean = DGSymbolBean.newBean(DGSymbolBean.class, bag.getData());
		if (StringUtils.isBlank(dgSymbolBean.getSymbol())) {
			return Message.error("参数错误");
		}
		String my_block = dgSymbolBean.get().getString("my_block");
		if (StringUtils.isBlank(my_block)) {
			return Message.error("参数错误");
		}
		String tran_address = priAddressCache.get(dgSymbolBean.getSymbol() + "_0_" + my_block);
		String cert_address = priAddressCache.get(dgSymbolBean.getSymbol() + "_1_" + my_block);
		JSONObject object = new JSONObject();
		object.put("tran_address", tran_address);
		object.put("cert_address", cert_address);
		return Message.ok(object);

	}

	/**
	 * 得到某交易对的基本描述
	 * 
	 * @param bag
	 * @return
	 */
	public Message base(NetBag bag) {
		DGSymbolBean dgSymbolBean = DGSymbolBean.newBean(DGSymbolBean.class, bag.getData());
		if (StringUtils.isBlank(dgSymbolBean.getSymbol())) {
			return Message.error("参数错误");
		}
		SwapBean swapBean = manager.get(dgSymbolBean.getSymbol());
		if (swapBean == null) {
			return Message.error("参数错误");
		}
		JSONObject jsonObject = swapBean.symbol.get();
		jsonObject.putAll(swapBean.symbol_des.get());
		DGQuotes select = dao.select(new DGQuotes().setQ_market(dgSymbolBean.getSymbol()).setQ_type(4));
		if (select != null) {
			jsonObject.putAll(select.get());
		}
		DGKeepBean dgKeepTranLogBean = new DGKeepBean().setDg_symbol(dgSymbolBean.getSymbol());
		DGKeepBean select2 = dao.select(dgKeepTranLogBean);
		if (select2 != null && select2.getNow_out_cert_token_num() != null) {
			jsonObject.put("now_out_cert_token_num", select2.getNow_out_cert_token_num());
		} else {
			jsonObject.put("now_out_cert_token_num", "0");
		}
		return Message.ok(jsonObject);
	}

	/**
	 * K线图
	 * 
	 * @param bag
	 * @return
	 */
	public Message kLine(NetBag bag) {
		DGQuotes newBean = DGQuotes.newBean(DGQuotes.class, bag.getData());
		if (StringUtils.isBlank(newBean.getQ_market())) {
			return Message.error("参数错误");
		}
		PageUtil.offsetPage(bag);
		Integer quotes_id = newBean.getQuotes_id();
		if (quotes_id != null) {
			newBean.setQuotes_id(null);
			Select where = Select.q(newBean).where(Where.q(newBean).ge("quotes_id", quotes_id));
			return Message.ok(dao.selectArray(DGQuotes.class, where));
		}
		return Message.ok(dao.selectArrayDESC(newBean));
	}

	@Autowired
	private BlockService blockService;

	/**
	 * 提交公钥
	 * 
	 * @param bag
	 * @return
	 */
	@Transactional
	public Message pushPub(NetBag bag) {
		JSONObject data = (JSONObject) bag.getData();
		String public_key_eth = data.getString("public_key_eth");
		String public_key_btc = data.getString("public_key_btc");
		if (StringUtils.isBlank(public_key_btc) || StringUtils.isBlank(public_key_eth)) {
			return Message.error("参数错误");
		}
		DGAddressBindBean dgAddressBindBean = new DGAddressBindBean();
		dgAddressBindBean.setPub(public_key_eth);
		if (dao.select(dgAddressBindBean) == null) {
			dgAddressBindBean.setBlock_name("Tron");
			String addressByPub = blockService.getAddressByPub(dgAddressBindBean.getBlock_name(), dgAddressBindBean.getPub().substring(2));
			dgAddressBindBean.setAddress(addressByPub);
			dgAddressBindBean.setDatum_address(addressByPub);
			dgAddressBindBean = dao.save(dgAddressBindBean);

			DGAddressBindBean ETH = new DGAddressBindBean();
			ETH.setPub(public_key_eth);
			ETH.setBlock_name("ETH");
			addressByPub = blockService.getAddressByPub(ETH.getBlock_name(), ETH.getPub().substring(2));
			ETH.setAddress(addressByPub);
			ETH.setDatum_address(dgAddressBindBean.getAddress());
			ETH = dao.save(ETH);

		}

		DGAddressBindBean BTC = new DGAddressBindBean();
		BTC.setPub(public_key_btc);
		if (dao.select(BTC) == null) {
			BTC.setBlock_name("BTC");
			String addressByPub = blockService.getAddressByPub(BTC.getBlock_name(), BTC.getPub());
			BTC.setAddress(addressByPub);
			BTC.setDatum_address(dgAddressBindBean.getAddress());
			BTC = dao.save(BTC);
		}
		return Message.ok();
	}

	@Autowired
	RedisUtil redis;

	/**
	 * 提交交易备注
	 * 
	 * @param bag
	 * @return
	 */
	@Transactional
	public Message pushRemark(NetBag bag) {
		PushRemarkBean newBean = PushRemarkBean.newBean(PushRemarkBean.class, bag.getData());
		String hash = newBean.getHash();
		if (StringUtils.isBlank(hash)) {
			return Message.error("参数错误");
		}
		switch (newBean.getType()) {// 备注类型 0: tran 1:cert
		case 0:
			if (newBean.getLimit_time() > 10000) {
				return Message.error("参数错误");
			}
//			BigDecimal setScale = newBean.getLimit_min().setScale(2, BigDecimal.ROUND_HALF_UP);
//			newBean.setLimit_min(setScale);
			if (newBean.getLimit_min().compareTo(BigDecimal.ZERO) <= 0) {
				return Message.error("参数错误");
			}
			redis.addRedisByTime("wait_" + hash, newBean.toString(), 3, TimeUnit.DAYS);
			break;
		case 1:
			if (StringUtils.isBlank(newBean.getRemark())) {
				return Message.error("参数错误");
			}

			PushRemarkBean pushRemarkBean = new PushRemarkBean().setType(1);
			pushRemarkBean.setHash(newBean.getRemark());
			pushRemarkBean.setRemark(newBean.getHash());
			redis.addRedisByTime("wait_" + newBean.getHash(), newBean.toString(), 3, TimeUnit.DAYS);
			redis.addRedisByTime("wait_" + pushRemarkBean.getHash(), pushRemarkBean.toString(), 3, TimeUnit.DAYS);
			break;
		default:
			return Message.error("参数错误");
		}
		return Message.ok();
	}
}

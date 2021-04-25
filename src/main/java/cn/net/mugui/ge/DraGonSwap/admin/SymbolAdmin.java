package cn.net.mugui.ge.DraGonSwap.admin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
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
import com.mugui.sql.SqlModeApi;
import com.mugui.wallet.controller.TronServiceApi;

import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolCreateBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolPriBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.user.entity.UserBindAddressBean;
import cn.net.mugui.ge.util.SessionImpl;

/**
 * 交易对后台管理
 * 
 * @author Administrator
 *
 */
@Authority(true)
@Component
@Module(name = "symbol.admin", type = "method")
public class SymbolAdmin implements Mugui {

	@Autowired
	private DGDao dao;

	@Reference
	private TronServiceApi tronServiceApi;

	@Reference(group = "ge")
	private SqlModeApi sql;

	/**
	 * 创建一个交易对
	 * 
	 * @param bag
	 * @return
	 */
	@Transactional
	public Message create(NetBag bag) {
		DGSymbolBean dcBean = DGSymbolBean.newBean(DGSymbolBean.class, bag.getData());
		dcBean.setSymbol(dcBean.getBase_currency() + "/" + dcBean.getQuote_currency());

		if (dao.select(new DGSymbolBean().setSymbol(dcBean.getSymbol())) != null) {
			return Message.error("该交易对已存在");
		}
		if (dao.select(
				new DGSymbolBean().setSymbol(dcBean.getQuote_currency() + "/" + dcBean.getBase_currency())) != null) {
			return Message.error("该交易对已存在");
		}
		DGSymbolCreateBean bean = DGSymbolCreateBean.newBean(DGSymbolCreateBean.class, bag.getData());
		if (bean.getCreate_address() == null) {
			UserBindAddressBean userBindAddressBean = new UserBindAddressBean().setUser_id(SessionImpl.getUserId());
			userBindAddressBean = sql.select(userBindAddressBean);
			bean.setCreate_address(userBindAddressBean.getAddress());
		} else {
			UserBindAddressBean userBindAddressBean = new UserBindAddressBean().setAddress(bean.getCreate_address());
			userBindAddressBean = sql.select(userBindAddressBean);
			if (userBindAddressBean == null) {
				return Message.error("参数错误");
			}
			bean.setCreate_address(userBindAddressBean.getAddress());
		}

		DGSymbolConfBean dgSymbolConfBean = new DGSymbolConfBean();
		dgSymbolConfBean.setSymbol(dcBean.getBase_currency());
		dgSymbolConfBean = dao.select(dgSymbolConfBean);
		if (dgSymbolConfBean == null) {
			dgSymbolConfBean = new DGSymbolConfBean().setPrecision(8);
		}
		bean.setQuote_init_number(BigDecimal.ZERO).setBase_init_number(BigDecimal.ZERO);
		bean.setCreate_init_price(BigDecimal.ZERO);
		bean.setTotal_init_number(BigDecimal.ZERO);
//		bean.setCreate_init_price(bean.getQuote_init_number().divide(bean.getBase_init_number(), dgSymbolConfBean.getPrecision(), BigDecimal.ROUND_HALF_UP));
//		bean.setTotal_init_number(bean.getBase_init_number().multiply(bean.getQuote_init_number()));
		DGSymbolPriBean priBean = DGSymbolPriBean.newBean(DGSymbolPriBean.class, bag.getData());
		if (StringUtils.isBlank(priBean.getPri())) {// 得到一个私钥
			String createMnemonic = (String) tronServiceApi.create();
			JSONObject parseObject = JSONObject.parseObject(createMnemonic);
			priBean.setPri(parseObject.getString("pri"));
		}
		priBean.setType(DGSymbolPriBean.type_0);
		DGSymbolBean save = dao.save(dcBean);
		priBean.setDg_symbol_id(save.getDg_symbol_id());
		priBean = dao.save(priBean);

		{// 流动性凭证私钥
			DGSymbolPriBean criBean = new DGSymbolPriBean();
			criBean.setDg_symbol_id(save.getDg_symbol_id());
			criBean.setType(DGSymbolPriBean.type_1);
//			String createMnemonic = (String) tronServiceApi.create();
//			JSONObject parseObject = JSONObject.parseObject(createMnemonic);
//			priBean.setPri(parseObject.getString("pri"));
			criBean.setPri(priBean.getPri());
			criBean = dao.save(criBean);
		}

		bean.setDg_symbol_id(save.getDg_symbol_id());
		bean = dao.save(bean);
		return Message.ok("创建成功");
	}

	/**
	 * 更新交易对状态
	 * 
	 * @param bag
	 * @return
	 */
	@Transactional
	public Message updateStatus(NetBag bag) {
		DGSymbolBean input_dg = DGSymbolBean.newBean(DGSymbolBean.class, bag.getData());
		if (input_dg.getDg_symbol_id() == null || input_dg.getSymbol_status() == null) {
			return Message.error("参数错误");
		}
		DGSymbolBean dgSymbolBean = dao.select(new DGSymbolBean().setDg_symbol_id(input_dg.getDg_symbol_id()));
		if (dgSymbolBean == null) {
			return Message.error("参数错误");
		}

		switch (input_dg.getSymbol_status()) {
		case DGSymbolBean.SYMBOL_STATUS_0:
		case DGSymbolBean.SYMBOL_STATUS_2:
		case DGSymbolBean.SYMBOL_STATUS_3:
			// 下架或者删除操作
			break;
		case DGSymbolBean.SYMBOL_STATUS_1:// 上架操作
			DGSymbolDescriptBean descriptBean = new DGSymbolDescriptBean()
					.setDg_symbol_id(dgSymbolBean.getDg_symbol_id());
			descriptBean = dao.select(descriptBean);
			if (descriptBean == null) {
				DGSymbolCreateBean createBean = new DGSymbolCreateBean()
						.setDg_symbol_id(dgSymbolBean.getDg_symbol_id());
				createBean = dao.select(createBean);
				if (createBean.getToken_address() == null) {
					createBean.setToken_address(input_dg.get().getString("token_address"));
					BigDecimal bigDecimal = input_dg.get().getBigDecimal("token_total_num");
					if (bigDecimal == null) {
						createBean.setToken_total_num(new BigDecimal("10000000000"));
					} else {
						createBean.setToken_total_num(bigDecimal);
					}
					dao.updata(createBean);
				}
				descriptBean = new DGSymbolDescriptBean().setDg_symbol_id(dgSymbolBean.getDg_symbol_id());
				descriptBean.setBase_num(BigDecimal.ZERO).setQuote_num(BigDecimal.ZERO)
						.setSymbol_descript_update_time(new Date());
				descriptBean.setTotal_num(BigDecimal.ZERO);

				// DGSymbolConfBean select = dao.select(new
				// DGSymbolConfBean().setSymbol(dgSymbolBean.getQuote_currency()));

				descriptBean.setScale(BigDecimal.ZERO);
				// select = dao.select(new
				// DGSymbolConfBean().setSymbol(dgSymbolBean.getBase_currency()));

				descriptBean.setReverse_scale(BigDecimal.ZERO);
				descriptBean = dao.save(descriptBean);
				bag.setRet_data(descriptBean.getDg_symbol_id());
				dgSymbolBean.setSymbol_status(DGSymbolBean.SYMBOL_STATUS_1);
				dao.updata(dgSymbolBean);
				manager.add(dgSymbolBean.getSymbol());
				return Message.ok("更新成功");
			}
			bag.setRet_data(descriptBean.getDg_symbol_id());
			dgSymbolBean.setSymbol_status(DGSymbolBean.SYMBOL_STATUS_1);
			dao.updata(dgSymbolBean);
			return Message.ok("已上架，无法上架");

		default:
			return Message.error("参数错误");
		}
		return Message.ok("更新成功");
	}

	@Autowired
	private DSymbolManager manager;

	/**
	 * 交易对列表
	 * 
	 * @param bag
	 * @return
	 */
	public Message list(NetBag bag) {
		List<DGSymbolBean> selectList = dao.selectList(new DGSymbolBean());
		JSONArray array = new JSONArray();
		for (DGSymbolBean symbolBean : selectList) {
			JSONObject json = symbolBean.toJson();
			json.putAll(dao.select(new DGSymbolCreateBean().setDg_symbol_id(symbolBean.getDg_symbol_id())).toJson());
			DGSymbolDescriptBean select = dao
					.select(new DGSymbolDescriptBean().setDg_symbol_id(symbolBean.getDg_symbol_id()));
			if (select != null) {
				json.putAll(select.toJson());
			}
			json.putAll(dao.select(new DGSymbolPriBean().setDg_symbol_id(symbolBean.getDg_symbol_id())).toJson());
			array.add(json);
		}
		return Message.ok(array);
	}

}

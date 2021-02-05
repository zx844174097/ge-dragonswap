package cn.net.mugui.ge.DraGonSwap.manager;

import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;

import com.hx.blockchain.manager.BCFactoryManagerApi;
import com.hx.blockchain.service.BlockChainServiceApi;
import com.mugui.spring.base.Manager;
import com.mugui.spring.net.auto.AutoManager;

import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolPriBean;
import cn.net.mugui.ge.DraGonSwap.bean.SwapBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
import cn.net.mugui.ge.DraGonSwap.block.BlockManager;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

/**
 * 私钥与地址绑定关系缓存
 * 
 * @author Administrator
 *
 */
@AutoManager
public class DGPriAddressCache extends Manager<String, String> {

	@Autowired
	private BlockManager blockmanager;

	@Autowired
	private DGDao dao;

	@Autowired
	private DSymbolManager DSymbolManager;

	/**
	 * key值说明<br>
	 * 0: 交易对名<br>
	 * 1: 秘钥类型<br>
	 * 2: 地址所属
	 * 
	 */
	@Override
	public String get(String key) {
		String string = super.get(key);
		if (StringUtils.isBlank(string)) {
			String[] split = key.split("[_]");
			SwapBean swapBean = DSymbolManager.get(split[0]);
			DGSymbolPriBean pri_tran = null;
			if (split[1].equals("0")) {
				pri_tran = swapBean.pri_tran;
			} else {
				pri_tran = swapBean.pri_cert;
			}
			DGPriAddressBean select = dao.select(new DGPriAddressBean().setSymbol_pri_id(pri_tran.getSymbol_pri_id()).setBlock_name(split[2]));
			if (select == null) {
				BlockHandleApi factory = blockmanager.get(split[2]);
				String address = factory.getAddressByPri(pri_tran.getPri());
				select = new DGPriAddressBean().setSymbol_pri_id(pri_tran.getSymbol_pri_id()).setBlock_name(split[2]);
				select.setAddress(address);
				select.setDg_symbol_id(pri_tran.getDg_symbol_id());
				select = dao.save(select);
			}
			add(key, string = select.getAddress());
		}
		return string;
	}
}

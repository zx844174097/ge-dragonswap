package cn.net.mugui.ge.DraGonSwap.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.net.mugui.ge.DraGonSwap.bean.DGAddressBindBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@Component
public class AddressBindUtil {

	@Autowired
	private DGDao dao;

	public String toDatumAddress(String address) {
		DGAddressBindBean select = dao.select(new DGAddressBindBean().setAddress(address));
		if (select == null) {
			return null;
		}
		return select.getDatum_address();
	}

	public String toBlockAddress(String address, String block) {
		String datumAddress = toDatumAddress(address);
		if (datumAddress == null) {
			return null;
		}
		if(block.equals("BNB")){
			block="ETH";
		}
		DGAddressBindBean select = dao.select(new DGAddressBindBean().setDatum_address(datumAddress).setBlock_name(block));
		if (select == null) {
			return null;
		}
		return select.getAddress();

	}
}

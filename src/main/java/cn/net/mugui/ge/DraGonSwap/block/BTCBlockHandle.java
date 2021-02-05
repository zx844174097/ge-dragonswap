package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.mugui.spring.net.bean.Message;

@Component
public class BTCBlockHandle  implements BlockHandleApi {

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String name() {
		return "BTC";
	}

	@Override
	public Object getSendTran(String pri, String to_address, BigDecimal amount, String token_address) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSucess(String hash) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Message broadcastTran(String send_msg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAddressByPri(String pri) {
		// TODO Auto-generated method stub
		return "未实现";
	}

}

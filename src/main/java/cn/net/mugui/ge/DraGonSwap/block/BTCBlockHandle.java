package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;

import com.mugui.spring.net.bean.Message;

import cn.net.mugui.ge.block.btc.BtcBlock;

@Component
public class BTCBlockHandle  implements BlockHandleApi {

	@Override
	public void init() {
		
	}

	@Override
	public String name() {
		return "BTC";
	}

	@Override
	public Object getSendTran(String pri, String to_address, BigDecimal amount, String token_address) throws Exception {
		String string = btcblock.signTran(to_address, pri, amount, token_address, null);
		return string;
	}

	@Override
	public boolean isSucess(String hash) throws Exception {
	
		return false;
	}

	@Override
	public Message broadcastTran(String send_msg) throws Exception {
		String transactionHash =  btcblock.getSendTransaction(send_msg);
		if (transactionHash == null) {
			return Message.error("交易失败");
		}
		return Message.ok(transactionHash, "交易成功");
	}


	BtcBlock btcblock=new BtcBlock();
	@Override
	public String getAddressByPri(String pri) {
		return btcblock.toAddress(pri);
	}

	@Override
	public String getAddressByPub(String pub) {
		return btcblock.toAddressByPub(pub);
	}

}

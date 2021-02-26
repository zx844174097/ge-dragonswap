package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;

import com.mugui.spring.net.bean.Message;

import cn.net.mugui.ge.block.eth.EthBlock;


@Component
public class ETHBlockHandle implements BlockHandleApi {

	EthBlock ethBlock = new EthBlock();

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "ETH";
	}

	@Override
	public Object getSendTran(String pri, String to_address, BigDecimal amount, String token_address) throws Exception {
		String string = ethBlock.signTran(to_address, pri, amount, token_address, null);
		return string;
	}

	@Override
	public boolean isSucess(String hash) throws Exception {
		Transaction transactionById = ethBlock.getTransactionById(hash);
		if (transactionById != null) {
			return true;
		}
		return false;
	}

	@Override
	public Message broadcastTran(String send_msg) throws Exception {
		EthSendTransaction ethSendTransaction = ethBlock.getEthSendTransaction(send_msg);
		String transactionHash = ethSendTransaction.getTransactionHash();
		if (ethSendTransaction.getError() != null || transactionHash == null) {
			return Message.error("交易失败");
		}
		return Message.ok(transactionHash, "交易成功");
	}

	@Override
	public String getAddressByPri(String pri) {
		return "0x" + Hex.toHexString(ethBlock.getECKey(pri).getAddress());
	}

	@Override
	public String getAddressByPub(String pub) {
		return ethBlock.getAddress(pub);
	}

}

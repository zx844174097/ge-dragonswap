package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;

import com.mugui.spring.net.bean.Message;

import cn.net.mugui.ge.block.Bnb.BNBBlock;

@Component
public class BNBBlockHandle implements BlockHandleApi {

	BNBBlock ethBlock = new BNBBlock();

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "BNB";
	}

	@Override
	public Object getSendTran(String pri, String to_address, BigDecimal amount, String token_address) throws Exception {
		String string = ethBlock.signTran(to_address, pri, amount, token_address, null);
		txids.set("0x"+Hex.toHexString(Hash.sha3(Hex.decode(string.substring(2)))));
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
	public Message broadcastTran(Object send_msg) throws Exception {
		EthSendTransaction ethSendTransaction = ethBlock.getEthSendTransaction(send_msg.toString());
		String transactionHash = ethSendTransaction.getTransactionHash();
		if (ethSendTransaction.getError() != null || transactionHash == null) {
			return Message.error("交易失败");
		}
		return Message.ok(transactionHash, "交易成功");
	}

	@Override
	public String getAddressByPri(String pri) {
		return ethBlock.getECKey(pri).getAddress();
	}

	@Override
	public String getAddressByPub(String pub) {
		return ethBlock.getAddress(pub);
	}

	@Override
	public Object getTran(long tran_index) {
		return ethBlock.getBlockByNumber(tran_index);
	}

	/**
	 * eth_blockNumber 最新块
	 */
	@Override
	public long getLastBlock() {
		return ethBlock.blockNumber();
	}

}

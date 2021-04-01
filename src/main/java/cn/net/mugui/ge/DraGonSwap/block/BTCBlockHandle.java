package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import org.bitcoinj.core.Transaction;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import com.mugui.spring.net.bean.Message;

import cn.net.mugui.ge.block.btc.BtcBlock;

@Component
public class BTCBlockHandle implements BlockHandleApi {

	@Override
	public void init() {

	}

	@Override
	public String name() {
		return "BTC";
	}

	@Override
	public Object getSendTran(String pri, String to_address, BigDecimal amount, String token_address) throws Exception {
		Transaction string = btcblock.signTran(to_address, pri, amount, token_address, null);
		txids.set(string.getHashAsString());
		return Hex.toHexString(string.bitcoinSerialize());
	}

	@Override
	public boolean isSucess(String hash) throws Exception {

		return false;
	}

	@Override
	public Message broadcastTran(Object send_msg) throws Exception {
		String transactionHash = btcblock.getSendTransaction(send_msg.toString());
		if (transactionHash == null) {
			return Message.error("交易失败");
		}
		return Message.ok(transactionHash, "交易成功");
	}

	BtcBlock btcblock = new BtcBlock();

	@Override
	public String getAddressByPri(String pri) {
		return btcblock.toAddress(pri);
	}

	@Override
	public String getAddressByPub(String pub) {
		return btcblock.toAddressByPub(pub);
	}

	@Override
	public Object getTran(long tran_index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastBlock() {
		// TODO Auto-generated method stub
		return 0;
	}

}

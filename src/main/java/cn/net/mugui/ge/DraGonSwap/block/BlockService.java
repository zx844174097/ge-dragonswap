package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mugui.spring.net.bean.Message;

@Component
public class BlockService {

	@Autowired
	private BlockManager manager;

	/**
	 * 得到待发送交易
	 * 
	 * @param block_name
	 * @param pri
	 * @param to_address
	 * @param amount
	 * @param token_address
	 * @return 待发送交易
	 */
	public Message getSendTran(String block_name, String pri, String to_address, BigDecimal amount, String token_address) {
		BlockHandleApi blockHandleApi = getBlockHandleApi(block_name);
		try {
			
			return Message.ok(blockHandleApi.getSendTran(pri, to_address, amount, token_address));
		} catch (Exception e) {
			e.printStackTrace();
			return Message.error(e.getMessage());
		}
	}

	private BlockHandleApi getBlockHandleApi(String block_name) {
		return manager.get(block_name);
	}

	/**
	 * 广播交易
	 * 
	 * @param block_name
	 * @param send_msg
	 * @return
	 */
	public Message broadcastTran(String block_name, String send_msg) {
		try {
			return getBlockHandleApi(block_name).broadcastTran(send_msg);
		} catch (Exception e) {
			e.printStackTrace();
			return Message.error(e.getMessage());
		}
	}

	public boolean isSucess(String block_name, String hash) {
		try {
			return getBlockHandleApi(block_name).isSucess(hash);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;

import com.mugui.spring.net.bean.Message;

/**
 * 区块链处理api
 * 
 * @author Administrator
 *
 */
public interface BlockHandleApi {

	/**
	 * 初始化
	 */
	void init();

	/**
	 * 处理器名字
	 * 
	 * @return
	 */
	String name();

	/**
	 * 得到一个待发送的交易
	 * 
	 * @param pri
	 * @param to_address
	 * @param amount
	 * @param token_address
	 * @return
	 * @throws Exception
	 */
	Object getSendTran(String pri, String to_address, BigDecimal amount, String token_address) throws Exception;

	/**
	 * 判断hash是否上链并且是否交易完成
	 * 
	 * @param hash
	 * @return
	 */
	boolean isSucess(String hash) throws Exception;

	/**
	 * 广播交易
	 * 
	 * @param send_msg
	 * @return
	 */

	Message broadcastTran(String send_msg)throws Exception;
	/**
	 * 通过私钥得到地址
	 * @param pri
	 * @return
	 */
	String getAddressByPri(String pri);

}

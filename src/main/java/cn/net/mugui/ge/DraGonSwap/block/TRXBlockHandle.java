package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Base58;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.crypto.ECKey;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Hash;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.util.HTTPUtil;

import cn.net.mugui.ge.block.tron.TRC20.ApiResult;
import cn.net.mugui.ge.block.tron.TRC20.ContractTransaction;
import cn.net.mugui.ge.block.tron.TRC20.Credential;
import cn.net.mugui.ge.block.tron.TRC20.TransferTransaction;
import cn.net.mugui.ge.block.tron.TRC20.TronApi;

@Component
public class TRXBlockHandle implements BlockHandleApi {

	@Override
	public void init() {

	}

	@Override
	public String name() {
		return "Tron";
	}

	private TronApi mainNet = new TronApi();

	private HashMap<String, TempBean> map = new HashMap<>();

	private static class TempBean {

		Credential credential;
		String address;
	}

	@Override
	public Object getSendTran(String pri, String to_address, BigDecimal amount, String contract_address) throws Exception {
		TempBean tempBean = map.get(pri);
		if (tempBean == null) {
			tempBean = new TempBean();
			tempBean.credential = Credential.fromPrivateKey(pri);
			tempBean.address = encode58Check(ECKey.fromPrivate(Hex.decode(pri)).getAddress());
		}
		Credential credential = tempBean.credential;
		if (StringUtils.isNotBlank(contract_address)) {

			ContractTransaction tokenSignMessage = null;
			tokenSignMessage = mainNet.getTokenSignMessage(contract_address, to_address, amount);
			String sign = credential.sign(Hash.sha3(tokenSignMessage.txId));
			tokenSignMessage.signature = new String[] { sign };
			return tokenSignMessage;

		} else {
			TransferTransaction tokenSignMessage = null;
			tokenSignMessage = mainNet.getTrxSignMessage(to_address, amount, tempBean.address);
			String sign = credential.sign(Hash.sha3(tokenSignMessage.txId));
			tokenSignMessage.signature = new String[] { sign };
			return tokenSignMessage;
		}
	}

	public String encode58Check(byte[] input) {
		byte[] hash0 = Hash.sha256(input);
		byte[] hash1 = Hash.sha256(hash0);
		byte[] inputCheck = new byte[input.length + 4+1];
		System.arraycopy(Hex.decode("41"), 0, inputCheck, 0, 1);
		System.arraycopy(input, 0, inputCheck, 1, input.length);
		System.arraycopy(hash1, 0, inputCheck, input.length+1, 4);
		return Base58.encode(inputCheck);
	}

	@Override
	public boolean isSucess(String hash) {
		String url = "https://api.trongrid.io/wallet/gettransactionbyid";
		JSONObject jsonObject = new JSONObject();
		if (StringUtils.isNoneBlank(hash)) {
			jsonObject.put("value", hash);
			jsonObject.put("visible", true);
		}
		String post = HTTPUtil.post(url, jsonObject.toJSONString());
		JSONObject jsonObject1 = JSONObject.parseObject(post);
		JSONArray ret = jsonObject1.getJSONArray("ret");
		JSONObject o = (JSONObject) ret.get(0);
		String contractRet = o.getString("contractRet");
		if (contractRet.equals("SUCCESS")) {
			return true;
		}
		throw new RuntimeException("交易失败，余额或能量不足" + hash);

	}

	@Override
	public Message broadcastTran(String send_msg) throws Exception {
		ContractTransaction fromJson = new Gson().fromJson(send_msg, ContractTransaction.class);
		ApiResult broadcastTransaction = mainNet.broadcastTransaction(fromJson);
		String txid = broadcastTransaction.txid;
		return Message.ok(txid);
	}

	@Override
	public String getAddressByPri(String pri) {
		TempBean tempBean = map.get(pri);
		if (tempBean == null) { 
			tempBean = new TempBean();
			tempBean.credential = Credential.fromPrivateKey(pri);
			tempBean.address = encode58Check(ECKey.fromPrivate(Hex.decode(pri)).getAddress());
		}
		return tempBean.address;
	}

}

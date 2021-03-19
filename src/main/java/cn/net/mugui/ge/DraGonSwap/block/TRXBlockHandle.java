package cn.net.mugui.ge.DraGonSwap.block;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Base58;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Hash;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.util.HTTPUtil;
import com.mugui.util.Other;

import cn.net.mugui.ge.block.tron.TRC20.Address;
import cn.net.mugui.ge.block.tron.TRC20.ApiResult;
import cn.net.mugui.ge.block.tron.TRC20.ContractTransaction;
import cn.net.mugui.ge.block.tron.TRC20.Credential;
import cn.net.mugui.ge.block.tron.TRC20.DeployContractTransaction;
import cn.net.mugui.ge.block.tron.TRC20.TransferTransaction;
import cn.net.mugui.ge.block.tron.TRC20.Trc20;
import cn.net.mugui.ge.block.tron.TRC20.TronApi;
import cn.net.mugui.ge.block.tron.TRC20.TronKit;

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
		TempBean tempBean = getTempBean(pri);

		Credential credential = tempBean.credential;
		if (StringUtils.isNotBlank(contract_address)) {

			ContractTransaction tokenSignMessage = null;
			tokenSignMessage = mainNet.getTokenSignMessage(tempBean.address, contract_address, to_address, amount);
			String sign = credential.sign(tokenSignMessage.txId);
			tokenSignMessage.signature = new String[] { sign };
			return tokenSignMessage;

		} else {
			TransferTransaction tokenSignMessage = null;
			tokenSignMessage = mainNet.getTrxSignMessage(to_address, amount, tempBean.address);
			String sign = credential.sign(tokenSignMessage.txId);
			tokenSignMessage.signature = new String[] { sign };
			return tokenSignMessage;
		}
	}

	private TempBean getTempBean(String pri) {
		TempBean tempBean = map.get(pri);
		if (tempBean == null) {
			tempBean = new TempBean();
			tempBean.credential = Credential.fromPrivateKey(pri);
			tempBean.address = tempBean.credential.getAddress().base58;
			map.put(pri, tempBean);
		}
		return tempBean;
	}

	public String encode58Check(byte[] input) {
		byte[] hash0 = Hash.sha256(input);
		byte[] hash1 = Hash.sha256(hash0);
		byte[] inputCheck = new byte[input.length + 4 + 1];
		System.arraycopy(Hex.decode("41"), 0, inputCheck, 0, 1);
		System.arraycopy(input, 0, inputCheck, 1, input.length);
		System.arraycopy(hash1, 0, inputCheck, input.length + 1, 4);
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
		if (ret == null) {
			return false;
		}
		JSONObject o = (JSONObject) ret.get(0);
		String contractRet = o.getString("contractRet");
		if (contractRet.equals("SUCCESS")) {
			return true;
		}
		throw new RuntimeException("交易失败，余额或能量不足" + hash);
	}

	@Override
	public Message broadcastTran(Object send_msg) throws Exception {
		ApiResult broadcastTransaction = mainNet.broadcastTransaction(send_msg);
		String txid = broadcastTransaction.txid;
		return Message.ok(txid);
	}

	@Override
	public String getAddressByPri(String pri) {
		TempBean tempBean = getTempBean(pri);
		return tempBean.address;
	}

	@Override
	public String getAddressByPub(String pub) {
		return Address.fromPublicKey(pub).base58;
	}

	@Override
	public Object getTran(long tran_index) {
		try {
			List<DeployContractTransaction> blockEvents = mainNet.getBlockEvents(tran_index);
			return blockEvents;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getLastBlock() {
		try {
			return ((long) mainNet.getLastNlock());
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public TronKit kit = new TronKit(mainNet, Credential.fromPrivateKey("8D9142B97B38F992B4ADF9FB3D0DD527B1F47BE113C6D0B5C32A0571EF1E7B5F"));
	private HashMap<String, BigInteger> d_map = new HashMap<>();
	
	public BigDecimal 转数额(BigInteger bigInteger, String contractAddress) {
		if(Other.isInteger(contractAddress)) {
			return new BigDecimal(bigInteger);
		}
		if(StringUtils.isBlank(contractAddress)) {
			return new BigDecimal(bigInteger).divide(new BigDecimal(1000000), 6, BigDecimal.ROUND_DOWN);
		}
		BigInteger decimals = d_map.get(contractAddress);
		if (decimals == null) {
			synchronized (map) {
				decimals = d_map.get(contractAddress);
				if (decimals == null) {
					try {
						Trc20 token = kit.trc20(contractAddress);
						decimals = token.decimals();
						d_map.put(contractAddress, decimals);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return new BigDecimal(bigInteger).divide(new BigDecimal(Math.pow(10, decimals.longValue())));
	}

}

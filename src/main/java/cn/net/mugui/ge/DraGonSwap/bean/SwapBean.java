package cn.net.mugui.ge.DraGonSwap.bean;

import com.mugui.bean.JsonBean;

import lombok.Getter;

public class SwapBean extends JsonBean {
	/**
	 * 交易池
	 */
	public String swap_name;
	/**
	 * 交易用私钥
	 */
	public DGSymbolPriBean pri_tran;
	/**
	 * 凭证用私钥
	 */
	public DGSymbolPriBean pri_cert;

	/**
	 * 交易对基本信息
	 */
	public DGSymbolBean symbol;

	/**
	 * 当前交易池信息
	 */
	public DGSymbolDescriptBean symbol_des;

}

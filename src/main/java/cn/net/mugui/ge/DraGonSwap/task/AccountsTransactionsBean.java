package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;

import com.mugui.bean.JsonBean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AccountsTransactionsBean extends JsonBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1141636845264535086L;

	private Integer accounts_transactions_id;

	private Integer block;

	private String hash;

	private String from_address;

	private String to_address;

	/**
	 * 交易类型
	 */
	private String type;

	private String fee;

	/**
	 * 时间戳
	 */
	private Long timestamp;
	/**
	 * 已创建
	 */
	public static final int status_0 = 0;
	/**
	 * 确认中
	 */
	public static final int status_1 = 0;
	/**
	 * 已确认
	 */
	public static final int status_2 = 0;

	private Integer status;

	private String result;

	private BigDecimal num;

	private String token_name;

	private String token_contract;

}

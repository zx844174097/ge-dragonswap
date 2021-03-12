package cn.net.mugui.ge.DraGonSwap.bean;

import java.math.BigDecimal;
import java.util.Date;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "dg_tran_log", KEY = "tran_log_id")
public class DGTranLogBean extends JsonBean {

	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer tran_log_id;

	/**
	 * 交易对
	 */
	@SQLField(NULL = false)
	private String dg_symbol;

	/**
	 * 来源公链名称
	 */
	@SQLField(NULL = false)
	private String from_block;
	/**
	 * 来源地址
	 */
	@SQLField(NULL = false)
	private String from_address;

	/**
	 * 来源币种
	 */
	@SQLField(NULL = false)
	private String from_token;

	/**
	 * 来源币种名称
	 */
	@SQLField(NULL = false)
	private String from_token_name;

	/**
	 * 来源数量
	 */
	@SQLField(NULL = false)
	private BigDecimal from_num;

	/**
	 * 来源hash
	 */
	@SQLField(NULL = false)
	private String from_hash;

	/**
	 * 去向公链
	 */
	@SQLField
	private String to_block;

	/**
	 * 去向地址
	 */
	@SQLField
	private String to_address;

	/**
	 * 去向token
	 */
	@SQLField
	private String to_token;

	/**
	 * 去向token名称
	 */
	@SQLField
	private String to_token_name;

	/**
	 * 去向数量
	 */
	@SQLField
	private BigDecimal to_num;

	/**
	 * 最小去向数量
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal to_limit_num;
 
	/**
	 * 交易限制时间
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private Long to_limit_time;

	/**
	 * 去向hash
	 */
	@SQLField
	private String to_hash;
	
	@SQLField(DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal fee_num;

	/**
	 * 兑换比例 （1个基本币种可兑换多少个计价币种）
	 */
	@SQLField
	private BigDecimal scale;

	/**
	 * 已创建
	 */
	public static final int log_status_0 = 0;

	/**
	 * 转账中
	 */
	public static final int log_status_1 = 1;

	/**
	 * 转账确认中
	 */
	public static final int log_status_2 = 2;

	/**
	 * 完成
	 */
	public static final int log_status_5 = 5;

	/**
	 * 未成功
	 */
	public static final int log_status_3 = 3;

	/**
	 * 交易进行中
	 */
	public static final int log_status_4 = 4;

	/**
	 * 状态
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private Integer log_status;
	/**
	 * 0 普通交易
	 */
	public static final int log_type_0 = 0;

	/**
	 * 1 退钱
	 */
	public static final int log_type_1 = 1;

	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private Integer log_type;

	/**
	 * 描述
	 */
	@SQLField
	private String log_detail;

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date tran_log_create_time;

}
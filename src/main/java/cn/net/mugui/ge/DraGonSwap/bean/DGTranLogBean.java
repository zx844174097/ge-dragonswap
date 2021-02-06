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
@SQLDB(TABLE = "dg_keep_log", KEY = "tran_log_id")
public class DGTranLogBean extends JsonBean {

	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer tran_log_id;

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
	private String to_block;

	/**
	 * 去向地址
	 */
	private String to_address;

	/**
	 * 去向token
	 */
	private String to_token;

	/**
	 * 去向token名称
	 */
	private String to_token_name;

	/**
	 * 去向数量
	 */
	private BigDecimal to_num;

	/**
	 * 去向hash
	 */
	private String to_hash;

	/**
	 * 兑换比例 （1个基本币种可兑换多少个计价币种）
	 */
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
	 * 已完成
	 */
	public static final int log_status_2 = 2;

	/**
	 * 未成功
	 */
	public static final int log_status_3 = 3;

	/**
	 * 状态
	 */
	@SQLField
	private Integer log_status;

	/**
	 * 描述
	 */
	@SQLField
	private String log_detail;

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date tran_log_create_time;

}
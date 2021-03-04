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
@SQLDB(TABLE = "dg_keep_log", KEY = "keep_log_id")
public class DGKeepTranLogBean extends JsonBean {

	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer keep_log_id;

	@SQLField(NULL = false)
	private String dg_symbol;

	@SQLField(NULL = false)
	private String block;

	/**
	 * 关联数量
	 */
	@SQLField(NULL = false)
	private BigDecimal amount;

	/**
	 * 关联hash
	 */
	@SQLField(NULL = false)
	private String hash;

	@SQLField
	private String to_address;

	/**
	 * 关联代币地址
	 */
	@SQLField(NULL = false)
	private String token_address;
	
	/**
	 * 关联代币名称
	 */
	@SQLField(NULL = false)
	private String token_name;

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
	 * 状态
	 */
	@SQLField(DEFAULT = true,DEFAULT_text = "0")
	private Integer log_status;

	/**
	 * 增加流动性，转出代币
	 */
	public static final int log_type_0 = 0;

	/**
	 * 日志类型
	 */
	@SQLField(NULL = false)
	private Integer log_type;

	/**
	 * 前一次流动性总量
	 */
	@SQLField(DATA_TYPE = "varchar(64)")
	private BigDecimal last_out_cert_token_num;
	/**
	 * 当前流动性总量
	 */
	@SQLField(DATA_TYPE = "varchar(64)")
	private BigDecimal now_out_cert_token_num;

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date keep_log_create_time;
	
}

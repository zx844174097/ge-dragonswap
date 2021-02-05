package cn.net.mugui.ge.DraGonSwap.bean;

import java.math.BigDecimal;

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
public class DGTranLogBean extends JsonBean {

	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer keep_log_id;



	@SQLField(NULL = false)
	private String dg_symbol;
	
	@SQLField(NULL=false)
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
	@SQLField
	private Integer log_status;


}
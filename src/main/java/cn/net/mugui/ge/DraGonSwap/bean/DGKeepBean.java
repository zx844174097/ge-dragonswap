package cn.net.mugui.ge.DraGonSwap.bean;

import java.math.BigDecimal;
import java.util.Date;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户流动性持有情况表
 * 
 * @author Administrator
 *
 */
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "dg_keep", KEY = "dg_keep_id")
public class DGKeepBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true, PRIMARY_KEY = true)
	private Integer dg_keep_id;

	/**
	 * 以tron地址为基准地址
	 */
	@SQLField(NULL = false)
	private String user_address;

	/**
	 * 交易对
	 */
	@SQLField()
	private String dg_symbol;

	/**
	 * 1号币种入金hash
	 */
	@SQLField(DATA_TYPE = "varchar(128)")
	private String amount_one_hash;

	/**
	 * 2号币种入金hash
	 */
	@SQLField(DATA_TYPE = "varchar(128)")
	private String amount_two_hash;

	/**
	 * 基本币种持有量
	 */
	@SQLField()
	private BigDecimal base_keep_num;

	/**
	 * 计价币种持有量
	 */
	@SQLField()
	private BigDecimal quotes_keep_num;

	/**
	 * 代币地址持有量
	 */
	@SQLField()
	private BigDecimal token_keep_num;

	/**
	 * 已返回的基本币种数量
	 */
	@SQLField( DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal recover_base_num;

	/**
	 * 已返回的计价币种数量
	 */
	@SQLField( DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal recover_quote_num;

	/**
	 * 已回收的代币币种持有数量
	 */
	@SQLField( DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal recover_token_num;

	/**
	 * 已创建
	 */
	public static final int KEEP_STATUS_0 = 0;

	/**
	 * 入金部分完成
	 */
	public static final int KEEP_STATUS_1 = 1;

	/**
	 * 入金完成，持有生效
	 */
	public static final int KEEP_STATUS_2 = 2;

	/**
	 * 入金未完成
	 */
	public static final int KEEP_STATUS_3 = 3;

	/**
	 * 资金池全额抽出
	 */
	public static final int KEEP_STATUS_4 = 4;

	/**
	 * 持有状态
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private Integer keep_status;

	/**
	 * 持有创建时间
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date keep_create_time;

	/**
	 * 持有更新时间
	 */
	@SQLField( DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date keep_update_time;

}

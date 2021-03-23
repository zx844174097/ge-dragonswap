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
	 * 基本量
	 */
	@SQLField()
	private BigDecimal base_num;

	/**
	 * 计价量
	 */
	@SQLField()
	private BigDecimal quotes_num;

	/**
	 * 代币量
	 */
	@SQLField()
	private BigDecimal token_num;
	/**
	 * 1号币种入金hash
	 */
	@SQLField(DATA_TYPE = "varchar(128)")
	private String hash_1;

	@SQLField(DATA_TYPE = "varchar(20)")
	private String block_1;
	@SQLField(DATA_TYPE = "varchar(64)")
	private String token_1;

	/**
	 * 2号币种入金hash
	 */
	@SQLField(DATA_TYPE = "varchar(128)")
	private String hash_2;

	@SQLField(DATA_TYPE = "varchar(20)")
	private String block_2;
	@SQLField(DATA_TYPE = "varchar(64)")
	private String token_2;
	/**
	 * 3号关联hash
	 */
	@SQLField(DATA_TYPE = "varchar(128)")
	private String hash_3;

	@SQLField(DATA_TYPE = "varchar(20)")
	private String block_3;
	@SQLField(DATA_TYPE = "varchar(64)")
	private String token_3;

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
	 * 代币转出中
	 */
	public static final int KEEP_STATUS_3 = 3;

	/**
	 * 代币转出完成
	 */
//	public static final int KEEP_STATUS_4 = 4;

	/**
	 * 资金池出金进行中
	 */
	public static final int KEEP_STATUS_5 = 5;

	/**
	 * 资金池出金已完成
	 */
//	public static final int KEEP_STATUS_6 = 6;
	public static final int KEEP_STATUS_7 = 7;

	/**
	 * 持有状态
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private Integer keep_status;

	/**
	 * 增加流动性，转出代币
	 */
	public static final int keep_type_0 = 0;
	/**
	 * 减少流动性，转入代币
	 */
	public static final int keep_type_1 = 1;

	/**
	 * 资金池出金失败
	 */
	public static final int keep_type_3 = 3;
	/**
	 * 流动性出金失败
	 */
	public static final int keep_type_2 = 2;
	/**
	 * 失败
	 */
	public static final int keep_type_4 = 4;
	/**
	 * 类型
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "0")
	private Integer keep_type;

	/**
	 * 前一次流动性总量
	 */
	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0", NULL = false)
	private BigDecimal last_out_cert_token_num;
	/**
	 * 当前流动性总量
	 */
	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0", NULL = false)
	private BigDecimal now_out_cert_token_num;

	/**
	 * 持有创建时间
	 */
	@SQLField(DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date keep_create_time;

}

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
@SQLDB(KEY = "cert_quotes_id", TABLE = "dg_cert_quotes")
public class DGCertQuotes extends JsonBean {

	/**
	 * 行情，每一分钟记录一次行情，调整为北京时间的时间戳，单位秒，并以此作为此K线柱的id
	 */
	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer quotes_id;

	/**
	 * 币币交易表读取开始的id
	 */
	@SQLField
	private Integer cert_log_id_start;

	/**
	 * 币币交易表读取结束的id
	 */
	@SQLField
	private Integer cert_log_id_end;
	/**
	 * 实时1 1day 1 week 1moth (1 2 3 4 )
	 */
	public static final int types[] = { 1, 2, 3,4 };

	/**
	 * 实时1 1day 1 week 1moth (1 2 3 4 )
	 */
	@SQLField
	private Integer type;

	/**
	 * 交易对
	 */
	@SQLField(DATA_TYPE = "varchar(16)")
	private String market;

	/**
	 * 交易对左币种id
	 */
	@SQLField(DATA_TYPE = "varchar(16)")
	private String symbol_l;

	/**
	 * 交易对右币种id
	 */
	@SQLField(DATA_TYPE = "varchar(16)")
	private String symbol_r;

	/**
	 * 以基础币种操作量
	 */
	@SQLField(DATA_TYPE = "varchar(32)",DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal start_base_num;

	@SQLField(DATA_TYPE = "varchar(32)",DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal end_base_num;

	/**
	 * 报价币种操作量
	 */
	@SQLField(DATA_TYPE = "varchar(32)",DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal start_quote_num;

	@SQLField(DATA_TYPE = "varchar(32)",DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal end_quote_num;

	/**
	 * 代币凭证操作量
	 */
	@SQLField(DATA_TYPE = "varchar(32)",DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal start_token_num;

	@SQLField(DATA_TYPE = "varchar(32)",DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal end_token_num;
	
	/**
	 * 变化率
	 */
	@SQLField(DEFAULT = true,DEFAULT_text = "0")
	private BigDecimal scale;

	/**
	 * 流动性操作次数
	 */
	@SQLField(DEFAULT = true,DEFAULT_text = "0")
	private Integer count;
	
	/**
	 * 合计总量
	 */
	@SQLField(DEFAULT = true,DEFAULT_text = "0")
	private Integer count_all;
	
	/**
	 * 行情创建时间
	 */
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date create_time;
}

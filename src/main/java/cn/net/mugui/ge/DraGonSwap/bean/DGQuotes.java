package cn.net.mugui.ge.DraGonSwap.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DG行情表<br>
 * 表名：dg_quotes 该实体类由木鬼提供的实体类生成工具自动生成。<br>
 * 
 * 
 * @author Mugui QQ:844174097
 */
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(KEY = "quotes_id", TABLE = "dg_quotes")
public class DGQuotes extends JsonBean {

	/**
	 * 行情，每一分钟记录一次行情，调整为北京时间的时间戳，单位秒，并以此作为此K线柱的id
	 */
	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer quotes_id;

	/**
	 * 币币交易表读取开始的id
	 */
	@SQLField
	private Integer tran_log_id_start;

	/**
	 * 币币交易表读取结束的id
	 */
	@SQLField
	private Integer tran_log_id_end;
	/**
	 * 5 30 60 1day 1moth (1 2 3 4 5 )
	 */
	public static final int types[] = { 1, 2, 3, 4, 5 };

	/**
	 * 5 30 60 1day 1moth (1 2 3 4 5 )
	 */
	@SQLField
	private Integer q_type;

	/**
	 * 交易对
	 */
	@SQLField(DATA_TYPE = "varchar(16)")
	private String q_market;

	/**
	 * 交易对左币种id
	 */
	@SQLField(DATA_TYPE = "varchar(16)")
	private String q_symbol_l;

	/**
	 * 交易对右币种id
	 */
	@SQLField(DATA_TYPE = "varchar(16)")
	private String q_symbol_r;

	/**
	 * 以基础币种计量的交易量
	 */
	@SQLField
	private BigDecimal q_amount;

	/**
	 * 以报价币种计量的交易量
	 */
	@SQLField
	private BigDecimal q_vol;

	/**
	 * 交易次数
	 */
	@SQLField
	private Integer q_count;

	/**
	 * 本阶段开盘价
	 */
	@SQLField
	private BigDecimal q_open;

	/**
	 * 本阶段收盘价
	 */
	@SQLField
	private BigDecimal q_close;

	/**
	 * 本阶段最低价
	 */
	@SQLField
	private BigDecimal q_low;

	/**
	 * 本阶段最高价
	 */
	@SQLField
	private BigDecimal q_high;

	/**
	 * 行情创建时间
	 */
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date q_create_time;

}

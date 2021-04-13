package cn.net.mugui.ge.DraGonSwap.count;

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
@SQLDB(TABLE = "in_and_out", KEY = "id")
public class InAndOutBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true, PRIMARY_KEY = true)
	private Integer id;

	/**
	 * 交易对
	 */
	@SQLField(NULL = false)
	private String symbol;
	
	/**
	 * 地址
	 */
	@SQLField
	private String address;

	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal base_in;

	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal base_out;

	/**
	 * 差值= 入币-出币
	 */
	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal base_diff;

	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal quote_in;

	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal quote_out;
	/**
	 * 差值= 入金-出金
	 */
	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal quote_diff;


	/**
	 * 创建时间
	 */
	@SQLField(NULL = false)
	private Date create_time;

}

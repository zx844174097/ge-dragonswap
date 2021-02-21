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
 * 交易对创建者信息
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(KEY = "symbol_create_id", TABLE = "dg_symbol_create")
public class DGSymbolCreateBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true, PRIMARY_KEY = true)
	private Integer symbol_create_id;

	@SQLField(NULL = false)
	private Integer dg_symbol_id;
	
	/**
	 * 创建者地址
	 */
	@SQLField(NULL = false)
	private String create_address;

	/**
	 * 合约代币地址,流动性凭证
	 */
	@SQLField()
	private String token_address;
	
	/**
	 * 合约总量
	 */
	@SQLField(DATA_TYPE = "varchar(64)",DEFAULT = true,DEFAULT_text = "10000000000")
	private BigDecimal token_total_num;
	
	/**
	 * 创建初始价格(1个基础币种价值多少报价币种)
	 */
	@SQLField
	private BigDecimal create_init_price;
	/**
	 * 基础币种数量
	 */
	@SQLField
	private BigDecimal base_init_number;

	/**
	 * 报价币种数量
	 */
	@SQLField
	private BigDecimal quote_init_number;
	/**
	 * 合计数量
	 */
	@SQLField( DATA_TYPE = "varchar(32)")
	private BigDecimal total_init_number;

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date create_time;

}

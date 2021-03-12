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
 * DraGonSwap 交易对
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "dg_symbol", KEY = "dg_symbol_id")
public class DGSymbolBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true, PRIMARY_KEY = true)
	private Integer dg_symbol_id;

	/**
	 * 交易对
	 */
	@SQLField(NULL = false)
	private String symbol;  

	/**
	 * 基础币种
	 */
	@SQLField(NULL = false)
	private String base_currency;
	
	
	/**
	 * 报价币种
	 */
	@SQLField(NULL = false)
	private String quote_currency;
	
	
	/**
	 * 已创建，未上架
	 */
	public static final int SYMBOL_STATUS_0 = 0;
	/**
	 * 已上架
	 */
	public static final int SYMBOL_STATUS_1 = 1;
	/**
	 * 已下架
	 */
	public static final int SYMBOL_STATUS_2 = 2;
	/**
	 * 已删除
	 */
	public static final int SYMBOL_STATUS_3 = 3;

	/**
	 * 交易对状态
	 */
	@SQLField(NULL = false,DEFAULT = true,DEFAULT_text = "0")
	private Integer symbol_status;

	/**
	 * 基础币种最小下单量
	 */
	@SQLField(NULL = false)
	private BigDecimal base_min_amt;
	/**
	 * 基础币种最大下单量
	 */
	@SQLField(NULL = false)
	private BigDecimal base_max_amt;
	/**
	 * 报价币种最小下单量
	 */
	@SQLField(NULL = false)
	private BigDecimal quote_min_amt;
	/**
	 * 报价币种最大下单量
	 */
	@SQLField(NULL = false)
	private BigDecimal quote_max_amt;
	
	/**
	 * 手续费比例
	 */
	@SQLField(DEFAULT = true,DEFAULT_text = "0.02")
	private BigDecimal fee_scale;
	
	@SQLField(NULL = false,DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date symbol_create_time;
	
	

}

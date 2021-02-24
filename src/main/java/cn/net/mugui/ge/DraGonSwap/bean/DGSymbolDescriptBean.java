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
 * 交易描述
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
@SQLDB(KEY = "symbol_descript_id", TABLE = "dg_symbol_descript")
@Getter
@Setter
@Accessors(chain = true)
public class DGSymbolDescriptBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true, PRIMARY_KEY = true)
	private Integer symbol_descript_id;
	/**
	 * 交易对id
	 */
	@SQLField(NULL = false)
	private Integer dg_symbol_id;
	/**
	 * 基本币种总量
	 */
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal base_num;

	/**
	 * 计价币种总量
	 */
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal quote_num;

	/**
	 * 合计数量
	 */
	@SQLField(NULL = false, DATA_TYPE = "varchar(32)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal total_num;
	/**
	 * 
	 * 此为展示用指导比例，不为用户兑换时实际比例
	 * 
	 * 1个基本币种可兑换多少个计价币种
	 */
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal scale;

	/**
	 * 此为展示用指导比例，不为用户兑换时实际比例
	 * 
	 * 1个计价币种可兑换多少基本币种
	 */
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal reverse_scale;
	
	@SQLField(DEFAULT=true,DEFAULT_text="0.01")
	private BigDecimal fee_scale;
	

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date symbol_descript_create_time;

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date symbol_descript_update_time;

}

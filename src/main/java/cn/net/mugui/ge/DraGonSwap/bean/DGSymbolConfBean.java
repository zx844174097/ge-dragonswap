package cn.net.mugui.ge.DraGonSwap.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * DG交易币种配置
 * @author Administrator
 *
 */
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "dg_symbol_conf",KEY = "dg_symbol_conf_id")
public class DGSymbolConfBean extends JsonBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SQLField(PRIMARY_KEY = true,AUTOINCREMENT = true)
	private Integer dg_symbol_conf_id;
	/**
	 * 币种名称
	 */
	@SQLField(NULL = false)
	private String symbol;
	
	@SQLField(DEFAULT = true,DEFAULT_text = "6")
	private Integer precision;
	
	/**
	 * 公链名称
	 */
	@SQLField(NULL = false)
	private String block_name;
	/**
	 * 合约地址
	 */
	@SQLField
	private String contract_address;
	
}

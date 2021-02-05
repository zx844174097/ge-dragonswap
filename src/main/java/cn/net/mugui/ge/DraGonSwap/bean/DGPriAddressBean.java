package cn.net.mugui.ge.DraGonSwap.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 币种地址关联表
 * @author Administrator
 *	
 */

@SuppressWarnings("serial")
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "dg_pri_address",KEY = "dg_pri_address_id")
public class DGPriAddressBean extends JsonBean {
		
	@SQLField(PRIMARY_KEY = true,AUTOINCREMENT = true)
	private Integer dg_pri_address_id;
	

	@SQLField(NULL = false)
	private Integer symbol_pri_id;

	/**
	 * 公链名称
	 */
	@SQLField(NULL = false)
	private String block_name;
	/**
	 * 地址
	 */
	@SQLField(NULL = false)
	private String address;
	
	/**
	 * 交易对id
	 */
	@SQLField(NULL = false)
	private Integer dg_symbol_id;
	
	
	
}

package cn.net.mugui.ge.DraGonSwap.bean;

import java.util.Date;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * 地址绑定与关系
 * @author Administrator
 *
 */

@Setter
@Getter

@Accessors(chain = true)
@SQLDB(TABLE = "dg_address_bind",KEY = "address_bind_id")
public class DGAddressBindBean extends JsonBean {
	
	
	@SQLField(AUTOINCREMENT = true,PRIMARY_KEY = true)
	private Integer address_bind_id;
	
	/**
	 * 基准地址
	 */
	@SQLField(NULL = false)
	private String datum_address;
	
	/**
	 * 地址
	 */
	@SQLField(NULL = false)
	private String address;
	/**
	 * 公链名称
	 */
	@SQLField(NULL = false)
	private String block_name;
	
	/**
	 * 公钥
	 */
	@SQLField(NULL = false,DATA_TYPE = "varchar(256)")
	private String pub;
	

	@SQLField(NULL = false,DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date create_time;
	
}

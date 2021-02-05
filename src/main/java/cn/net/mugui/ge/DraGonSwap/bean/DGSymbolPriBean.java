package cn.net.mugui.ge.DraGonSwap.bean;

import java.util.Date;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 交易对 私钥信息
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(KEY = "symbol_pri_id", TABLE = "dg_symbol_pri")
public class DGSymbolPriBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true, PRIMARY_KEY = true)
	private Integer symbol_pri_id;
	@SQLField(NULL = false)
	private Integer dg_symbol_id;
	/**
	 * 创建私钥
	 */
	@SQLField(NULL = false, DATA_TYPE = "varchar(128)")
	private String pri;
	/**
	 * 交易私钥
	 */
	public static int type_0=0;
	
	/**
	 * 流动性凭证检测私钥
	 */
	public static int type_1=1;
	
	/**
	 * 私钥类型
	 */
	@SQLField(NULL = false)
	private Integer type;

	
	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date pri_create_time;
}

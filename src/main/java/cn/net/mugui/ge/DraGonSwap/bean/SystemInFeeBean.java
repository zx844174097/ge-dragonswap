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
@SQLDB(KEY = "in_fee_id", TABLE = "in_fee")
public class SystemInFeeBean extends JsonBean {

	@SQLField(PRIMARY_KEY = true, AUTOINCREMENT = true)
	private Integer in_fee_id;

	@SQLField(DATA_TYPE = "varchar(32)")
	private String symbol;

	@SQLField
	private String user_address;
	
	@SQLField
	private String block;

	@SQLField
	private String contract_address;

	@SQLField
	private String name;

	@SQLField(DATA_TYPE = "varchar(64)", DEFAULT = true, DEFAULT_text = "0")
	private BigDecimal fee;

	@SQLField(NULL = false, DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date create_time;

}

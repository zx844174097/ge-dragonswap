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
 * 链上交易记录,只存储已完成的交易
 * @author Administrator
 *
 */
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(KEY = "tran_id",TABLE = "block_tran")
public class BlockTranBean extends JsonBean {
	
	@SQLField(AUTOINCREMENT = true,PRIMARY_KEY = true)
	private Integer tran_id;
	/**
	 * 区块主链名称
	 */
	@SQLField(DATA_TYPE = "varchar(12)")
	private String block;
	@SQLField(DATA_TYPE = "varchar(64)")
	private String from;
	@SQLField(DATA_TYPE = "varchar(64)")
	private String to;
	/**
	 * 合约地址
	 */
	@SQLField(DATA_TYPE = "varchar(64)")
	private String token;
	@SQLField(DATA_TYPE = "varchar(64)",UNIQUE = true)
	private String hash;
	@SQLField  
	private BigDecimal fee;
	@SQLField
	private BigDecimal num;
	
	
	/**
	 * 入库时间
	 */
	@SQLField(NULL = false,DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
	private Date time;
	
	
}

package cn.net.mugui.ge.DraGonSwap.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * DC 额外手续费  5%
 *
 */
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "dc_extra_fee",KEY = "dc_extra_fee_id")
public class DCExtraFeeBean  extends JsonBean {

    @SQLField(AUTOINCREMENT = true,PRIMARY_KEY = true)
    private String dc_extra_fee_id;

    /**
     * 来源地址
     */
    @SQLField(NULL = false)
    private String from_address;


    /**
     * 来源hash
     */
    @SQLField(DATA_TYPE = "varchar(64)",NULL = false)
    private String from_hash;


    /**
     * 币种
     */
    @SQLField(NULL = false,DATA_TYPE = "varchar(64)")
    private String token;

    @SQLField(NULL = false)
    private String block;

    /**
     * 去向地址
     */
    @SQLField(NULL = false)
    private String to_address;

    /**
     * 开始数量
     */
    @SQLField(NULL = false,DEFAULT=true, DEFAULT_text = "0")
    private BigDecimal start_num;

    /**
     * 手续费
     */
    @SQLField(NULL = false,DEFAULT=true, DEFAULT_text = "0")
    private BigDecimal fee_num;
    /**
     * 结束数量
     */
    @SQLField(NULL = false,DEFAULT=true, DEFAULT_text = "0")
    private BigDecimal end_num;
    /**
     * 交易hash
     */
    @SQLField(DATA_TYPE = "varchar(64)")
    private String to_hash;

    /**
     * 已创建
     */
    public static final int dc_extra_fee_status_0 = 0;

    /**
     * 转账中
     */
    public static final int dc_extra_fee_status_1 = 1;

    /**
     * 转账确认中
     */
    public static final int dc_extra_fee_status_2 = 2;

    /**
     * 完成
     */
    public static final int dc_extra_fee_status_5 = 5;

    /**
     * 未成功
     */
    public static final int dc_extra_fee_status_3 = 3;

    /**
     * 交易进行中
     */
    public static final int dc_extra_fee_status_4 = 4;


    /**
     * 重试
     */
    public static final int dc_extra_fee_status_6 = 6;

    @SQLField(DEFAULT=true, DEFAULT_text = "0")
    private Integer dc_extra_fee_status;

    @SQLField(NULL = false,DEFAULT = true, DEFAULT_text = "CURRENT_TIMESTAMP")
    private Date create_time;


    private Date success_time;
}

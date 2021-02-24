package cn.net.mugui.ge.DraGonSwap.bean;

import java.math.BigDecimal;
import java.util.Date;

import com.mugui.bean.JsonBean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class PushRemarkBean extends JsonBean{
	private String hash;
	
	/**
	 * 备注类型 0: tran 1:cert
	 */
	
	private Integer type;
	
	/**
	 * 数量限制
	 */
	private BigDecimal limit_min;
	
	/**
	 * 时间限制（分钟计）
	 */
	private long limit_time;
	
	private String remark;
	
}

package cn.net.mugui.ge.DraGonSwap.bean;

import com.mugui.bean.JsonBean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class BroadcastBean extends JsonBean {

	private String from_address;

	private String data;

	private String block;

}

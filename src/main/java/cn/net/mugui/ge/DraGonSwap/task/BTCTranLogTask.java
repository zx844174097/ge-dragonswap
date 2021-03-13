package cn.net.mugui.ge.DraGonSwap.task;

import java.util.List;

import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;

@AutoTask
@Task(blank = 1000*60*10,value = Task.CYCLE)
public class BTCTranLogTask extends DefaultTranLogTask{

	@Override
	protected String getName() {
		return "BTC";
	}

	@Override
	protected List<BlockTranBean> handle(Object tran) {
		return null;
	}

}

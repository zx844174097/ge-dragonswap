package cn.net.mugui.ge.DraGonSwap.task;

import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;

@AutoTask
@Task(blank = 1000*60,value = Task.CYCLE)
public class ETHTranLogTask extends DefaultTranLogTask {

	@Override
	public String getName() {
		return "ETH";
	}

	@Override
	protected BlockTranBean handle(Object tran) {
		return null;
	}

}

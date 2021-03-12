package cn.net.mugui.ge.DraGonSwap.task;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
import cn.net.mugui.ge.DraGonSwap.block.BlockManager;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;

public abstract class DefaultTranLogTask extends TaskImpl {

	protected abstract String getName();

	@Autowired
	private BlockManager blockManager;

	@Autowired
	private DGConf conf;

	@Override
	public void run() {
		BlockHandleApi blockHandleApi = blockManager.get(getName());
		String value = conf.getValue(getName() + "_tran_log_index");
		if (value == null) {
			conf.save(getName() + "_tran_log_index", value = "0", getName() + "区块交易扫描id");
		}
		if (!Other.isInteger(value)) {
			return;
		}
		long lastBlock = blockHandleApi.getLastBlock();
		if (lastBlock > Integer.parseInt(value)) {
			for (int i = Integer.parseInt(value) + 1; i <= lastBlock; i++) {
				conf.setValue(getName() + "_tran_log_index", i + "");
				Object tran = blockHandleApi.getTran(i);
				handle(tran);
			}
		}

	}

	protected abstract BlockTranBean handle(Object tran);

}

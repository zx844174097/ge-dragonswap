package cn.net.mugui.ge.DraGonSwap.task;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.util.Other;

import cn.hutool.core.thread.ThreadUtil;
import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
import cn.net.mugui.ge.DraGonSwap.block.BlockManager;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;

public abstract class DefaultTranLogTask extends TaskImpl {

	protected abstract String getName();

	@Autowired
	private BlockManager blockManager;

	@Autowired
	private DGConf conf;

	@Autowired
	private DGDao dao;

	ThreadPoolExecutor build = ThreadUtil.newExecutor(1, 5);

	@Override
	public void run() {
		BlockHandleApi blockHandleApi = blockManager.get(getName());
		String value = conf.getValue(getName() + "_tran_log_index");
		if (value == null) {
			conf.save(getName() + "_tran_log_index", value = "12023208", getName() + "区块交易扫描id");
		}
		if (!Other.isInteger(value)) {
			return;
		}
		long lastBlock = blockHandleApi.getLastBlock();
		if (lastBlock > Integer.parseInt(value)) {
			for (int i = Integer.parseInt(value) + 1; i <= lastBlock; i++) {
				if (build.getActiveCount() == build.getMaximumPoolSize()) {
					return;
				}
				build.execute(new TempRunnable(i, blockHandleApi));
				conf.setValue(getName() + "_tran_log_index", i + "");
			}
		}
	}

	private class TempRunnable implements Runnable {
		private Integer i;
		BlockHandleApi blockHandleApi;

		TempRunnable(Integer i, BlockHandleApi blockHandleApi) {
			this.i = i;
			this.blockHandleApi = blockHandleApi;
		}

		@Override
		public void run() {
			Object tran = null;
			do {
				tran = blockHandleApi.getTran(i);
			} while (tran == null);
			System.out.println(getName() + "->" + i);
			List<BlockTranBean> handle = handle(tran);
			for (BlockTranBean bean : handle) {
				dao.save(bean);
			}
		}

	}

	protected abstract List<BlockTranBean> handle(Object tran);

}

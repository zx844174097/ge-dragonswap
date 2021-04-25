package cn.net.mugui.ge.DraGonSwap.task;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.util.Other;

import cn.hutool.core.thread.ThreadUtil;
import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
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
	
	long time=0;
	@Override
	public void run() {
		if (System.currentTimeMillis() - time > 60000) {
			initListenerAddress();
			time = System.currentTimeMillis();
		}
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

	HashMap<String, String> map = new HashMap<>();

	protected void initListenerAddress() {
		HashMap<String, String> map = new HashMap<>();
		List<DGPriAddressBean> selectList = dao.selectList(new DGPriAddressBean().setBlock_name(getName()));
		for (DGPriAddressBean bean : selectList) {
			map.put(bean.getAddress(), "");
		}
		this.map = map;
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
				if (dao.select(new BlockTranBean().setHash(bean.getHash())) == null)
					dao.save(bean);
			}
		}

	}

	protected abstract List<BlockTranBean> handle(Object tran);

}

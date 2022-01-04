package cn.net.mugui.ge.DraGonSwap.task;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.sql.SqlServer;
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

	private TempRunnable[] tempRunnables = null;
	long time = 0;
	BlockHandleApi blockHandleApi = null;

	@Override
	public void init() {
		super.init();

		blockHandleApi = blockManager.get(getName());
		tempRunnables = new TempRunnable[5];
		for (int i = 0; i < tempRunnables.length; i++) {
			tempRunnables[i] = new TempRunnable(blockHandleApi);
			ThreadUtil.execAsync(tempRunnables[i]);
		}

	}

	@Override
	public void run() {
		if (System.currentTimeMillis() - time > 60000) {
			initListenerAddress();
			time = System.currentTimeMillis();
		}
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
				tempRunnables[i % 5].add(i);
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

		ConcurrentLinkedDeque<Integer> integers = new ConcurrentLinkedDeque<>();

		BlockHandleApi blockHandleApi;

		TempRunnable(BlockHandleApi blockHandleApi) {
			this.blockHandleApi = blockHandleApi;
		}

		public void add(Integer i) {
			synchronized (integers) {
				integers.add(i);
				integers.notifyAll();
			}
		}

		@Override
		public void run() {
			Thread.currentThread().setName("Thread-" + getName());
			while (true) {
				try {
					Integer poll = integers.poll();
					if (poll == null) {
						synchronized (integers) {
							poll = integers.poll();
							if (poll == null) {
								integers.wait();
								continue;
							}
						}
					}
					Run(poll);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					SqlServer.reback();
				}
			}
		}

		private void Run(Integer i) {
			Object tran = null;
			do {
				tran = blockHandleApi.getTran(i);
				if(tran==null) {
					Other.sleep(100);
					System.out.println(getName() + "->" + i);
				}
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

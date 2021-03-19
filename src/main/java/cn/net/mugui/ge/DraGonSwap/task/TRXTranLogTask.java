package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.util.RedisAccess;
import com.mugui.util.Other;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.thread.RejectPolicy;
import cn.hutool.core.util.HexUtil;
import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
import cn.net.mugui.ge.DraGonSwap.block.BlockManager;
import cn.net.mugui.ge.DraGonSwap.block.TRXBlockHandle;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;
import cn.net.mugui.ge.block.tron.TRC20.Address;
import cn.net.mugui.ge.block.tron.TRC20.DeployContractTransaction;
import cn.net.mugui.ge.block.tron.TRC20.DeployContractTransaction.Contract;

@AutoTask
@Task()
public class TRXTranLogTask extends TaskImpl {
	private static final String TRON_SCAN = "TRX_LOG";

	public ThreadPoolExecutor TRON_SCAN_TASK = ExecutorBuilder.create().setCorePoolSize(10).setWorkQueue(new LinkedBlockingQueue<>()).setMaxPoolSize(10)
			.setThreadFactory(new NamedThreadFactory(TRON_SCAN, false)).setHandler(RejectPolicy.CALLER_RUNS.getValue()).build();

	@Override
	public void init() {
		super.init();
		dao.createTable(BlockTranBean.class);
		redisClient = reactor.getRedisClient(5);
		blockHandleApi = (TRXBlockHandle) blockManager.get(getName());
		initListenerAddress();
		for (int i = 0; i < TRON_SCAN_TASK.getCorePoolSize(); i++) {
			TRON_SCAN_TASK.execute(new TRXTranLogRunnable(TRON_SCAN + "_" + i));
		}
	}

	private HashMap<String, String> map = new HashMap<>();

	private void initListenerAddress() {
		List<DGPriAddressBean> selectList = dao.selectList(new DGPriAddressBean().setBlock_name(getName()));
		for (DGPriAddressBean bean : selectList) {
			map.put(bean.getAddress(), "");
		}
	}

	TRXBlockHandle blockHandleApi = null;

	private class TRXTranLogRunnable implements Runnable {

		private String redis_key = null;

		public TRXTranLogRunnable(String string) {
			redis_key = string;
		}

		@Override
		public void run() {
			while (true) {
				try {
					handle();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Other.sleep(1000);
			}

		}

		public void handle() {
			while (true) {
				String leftPop = redisClient.opsForList().leftPop(redis_key);
				if (leftPop == null) {
					return;
				}
				System.out.println(leftPop);
				Object tran = blockHandleApi.getTran(Long.parseLong(leftPop));
				while (tran == null) {
					tran = blockHandleApi.getTran(Long.parseLong(leftPop));
					Other.sleep(1000);
				}
				List<BlockTranBean> handle = TRXTranLogTask.this.handle(tran);
				if (!handle.isEmpty()) {
					for (BlockTranBean tranBean : handle) {
						if (dao.select(new BlockTranBean().setHash(tranBean.getHash())) == null)
							dao.save(tranBean);
					}
				}
			}
		}

	}

	@Autowired
	private DGDao dao;

	public String getName() {
		return "Tron";
	}

	public String toBase58(String address) {
		return Address.encode("0x" + address);
	}

	protected List<BlockTranBean> handle(Object tran) {
		LinkedList<BlockTranBean> linkedList = new LinkedList<>();
		if (tran == null) {
			return linkedList;
		}
		List<DeployContractTransaction> blockEvents = (List<DeployContractTransaction>) tran;
		for (DeployContractTransaction event : blockEvents) {
			Contract[] clone = event.rawData.contract;
			for (Contract contract : clone) {
				String from = null;
				String to = null;
				BigInteger amount = null;
				String contractAddress = null;
				DeployContractTransaction.Value value = contract.parameter.value;
				if (contract.type.equals("TransferContract")) {// 普通转账
					amount = new BigInteger(value.amount + "");
					from = toBase58(value.ownerAddress);
					to = toBase58(value.toAddress);
				} else if (contract.type.equals("TriggerSmartContract")) {// trc20
					from = toBase58(value.ownerAddress);
					contractAddress = toBase58(value.contract_address);
					String data = value.data;
					if (data.startsWith("a9059cbb")) {
						BigInteger integer = new BigInteger(data.substring(8, 72), 16);
						String string = integer.toString(16);
						if (!string.startsWith("41")) {
							to = toBase58("41" + string);
						} else
							to = toBase58(string);
						amount = new BigInteger(data.substring(72), 16);
					} else {
						continue;
					}
				} else if (contract.type.equals("TransferAssetContract")) {// trc10
					from = toBase58(value.ownerAddress);
					contractAddress = HexUtil.decodeHexStr(value.asset_name);
					to = value.toAddress;
				} else {
					continue;
				}

				String string = map.get(to);
				if (string != null) {
					BigDecimal t = blockHandleApi.转数额(amount, contractAddress);
					linkedList.add(new BlockTranBean().setFrom(from).setTo(to).setToken(contractAddress).setNum(t).setHash(event.txId).setBlock(getName()));
				}
			}

		}
		return linkedList;
	}

	@Autowired
	private BlockManager blockManager;

	@Autowired
	private DGConf conf;

	@Autowired
	private RedisAccess reactor;

	StringRedisTemplate redisClient = null;

	@Override
	public void run() {
		while (true) {
			try {
				Run();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Other.sleep(1000);
		}

	}

	private void Run() {
		BlockHandleApi blockHandleApi = blockManager.get(getName());
		String value = conf.getValue(getName() + "_tran_log_index");
		if (value == null) {
			conf.save(getName() + "_tran_log_index", value = "28064527", getName() + "区块交易扫描id");
		}
		if (!Other.isInteger(value)) {
			return;
		}

		long lastBlock = blockHandleApi.getLastBlock();
		if (lastBlock <= Integer.parseInt(value)) {
			return;
		}
		int corePoolSize = TRON_SCAN_TASK.getCorePoolSize();
		for (int i = Integer.parseInt(value) + 1; i <= lastBlock; i++) {
			redisClient.opsForList().rightPush(TRON_SCAN + "_" + (i % corePoolSize), i + "");
		}
		conf.setValue(getName() + "_tran_log_index", lastBlock + "");
	}

}

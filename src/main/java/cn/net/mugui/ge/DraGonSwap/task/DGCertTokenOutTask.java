package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.google.gson.Gson;
import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.net.bean.Message;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.app.Symbol;
import cn.net.mugui.ge.DraGonSwap.bean.BroadcastBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;

/**
 * token持有证明转出
 * 
 * @author Administrator
 *
 */
@AutoTask
@Task
public class DGCertTokenOutTask extends TaskImpl {
	@Override
	public void init() {
		super.init();
		List<DGKeepBean> selectList = dao.selectList(
				new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_2).setKeep_type(DGKeepBean.keep_type_0));
		linkedList.addAll(selectList);
//		selectList = dao.selectList(new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_3).setKeep_type(DGKeepBean.keep_type_0));
//		linkedList.addAll(selectList);
		selectList = dao.selectList(
				new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_0).setKeep_type(DGKeepBean.keep_type_1));
		linkedList.addAll(selectList);
		retryRun();
	}

	@Scheduled(cron = "0 0/2 * * * ? ")
	public synchronized void retryRun() {
		List<DGKeepBean> selectList = dao.selectList(new DGKeepBean().setKeep_status(8));
		for (DGKeepBean bean : selectList) {
			if (bean.getKeep_type() == DGKeepBean.keep_type_0) {
				bean.setKeep_status(DGKeepBean.KEEP_STATUS_2);
			} else if (bean.getKeep_type() == DGKeepBean.keep_type_1) {
				bean.setKeep_status(DGKeepBean.KEEP_STATUS_0);
			}
			bean.setKeep_create_time(new Date());
			dao.updata(bean);
			add(bean);
		}
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

	@Autowired
	private BlockService blockservice;

	private void handle() {

		while (true) {
			DGKeepBean poll = linkedList.poll();
			if (poll == null) {
				synchronized (this) {
					poll = linkedList.poll();
					if (poll == null) {
						try {
							this.wait();
							continue;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (poll.getKeep_type() == DGKeepBean.keep_type_0) {
				switch (poll.getKeep_status()) {
				case DGKeepBean.KEEP_STATUS_2:
					send(poll);
					break;

				case DGKeepBean.KEEP_STATUS_3:
					
					Long temp_time = poll.get().getLong("temp_time");
					if (null == temp_time) {
						poll.get().put("temp_time", temp_time = System.currentTimeMillis());
					}
					if (System.currentTimeMillis() - poll.getKeep_create_time().getTime() < 3000) {
						add(poll);
						break;
					}

					// 判断交易是否成功
					if (isSucess(poll)) {
						poll.setKeep_status(DGKeepBean.KEEP_STATUS_7);
						dao.updata(poll);
						break;
					}
					if (System.currentTimeMillis() - poll.getKeep_create_time().getTime() > 60000) {
						poll.setKeep_status(DGKeepBean.KEEP_STATUS_7);
						poll.setKeep_type(DGKeepBean.keep_type_2);
						dao.updata(poll);
						break;
					}
					if (System.currentTimeMillis() - temp_time > 3000) {
						broadcastTran(poll);
						poll.get().put("temp_time", temp_time = System.currentTimeMillis());
					}
					break;
				default:
					break;
				}
			} else if (poll.getKeep_type() == DGKeepBean.keep_type_1) {
				if (poll.getBase_num().compareTo(BigDecimal.ZERO) <= 0
						|| poll.getQuotes_num().compareTo(BigDecimal.ZERO) <= 0) {
					poll.setKeep_status(DGKeepBean.KEEP_STATUS_7);
					poll.setKeep_type(DGKeepBean.keep_type_3);
					dao.updata(poll);
					continue;
				}
				switch (poll.getKeep_status()) {
				case DGKeepBean.KEEP_STATUS_0:
					sendFunds(poll);
					break;

				case DGKeepBean.KEEP_STATUS_5:
					if (System.currentTimeMillis() - poll.getKeep_create_time().getTime() < 5000) {
						add(poll);
						break;
					}
					// 判断交易是否成功
					if (isSucessFunds(poll)) {
						poll.setKeep_status(DGKeepBean.KEEP_STATUS_7);
						dao.updata(poll);
						break;
					}
					if (System.currentTimeMillis() - poll.getKeep_create_time().getTime() > 60000) {
						poll.setKeep_status(DGKeepBean.KEEP_STATUS_7);
						poll.setKeep_type(DGKeepBean.keep_type_3);
						dao.updata(poll);
						break;
					}
					broadcastTranFunds(poll);
					break;
				default:
					break;
				}
			}

		}
	}

	/**
	 * 广播资金池
	 * 
	 * @param poll
	 */
	private void broadcastTranFunds(DGKeepBean poll) {
		add(poll);
		Other.sleep(1000);
		BroadcastBean bean = new BroadcastBean().setBlock(poll.getBlock_1())
				.setData(gson.toJson(poll.get().get("broadcast1")))
				.setFrom_address(manager.get(poll.getDg_symbol()).pri_tran.getPri());
		symbol.getLinkedDeque().addLast(bean);
		bean = new BroadcastBean().setBlock(poll.getBlock_2()).setData(gson.toJson(poll.get().get("broadcast2")))
				.setFrom_address(manager.get(poll.getDg_symbol()).pri_tran.getPri());
		symbol.getLinkedDeque().addLast(bean);
//		try {
//			blockservice.broadcastTran(poll.getBlock_1(), poll.get().get("broadcast1"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		try {
//			blockservice.broadcastTran(poll.getBlock_2(), poll.get().get("broadcast2"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return;
	}

	/**
	 * 验证交易是否完成
	 * 
	 * @param poll
	 * @return
	 */
	private boolean isSucessFunds(DGKeepBean poll) {
		return blockservice.isSucess(poll.getBlock_1(), poll.getHash_1())
				&& blockservice.isSucess(poll.getBlock_2(), poll.getHash_2());
	}

	@Autowired
	private DGConf conf;

	/**
	 * 发送资金池
	 * 
	 * @param poll
	 */
	private void sendFunds(DGKeepBean poll) {
		add(poll);
		BigDecimal base_num = poll.getBase_num();
		String user_address = manager.get(poll.getDg_symbol()).pri_tran.getPri();

		String value = conf.getValue("error_back_dc");
		if (StringUtils.isBlank(value)) {
			conf.save("error_back_dc", value = "true", "错误交易回收DC");
		}

		if (value.equals("true") && poll.getUser_address().equals("TSv7E7Ybbq6DxWTfFKCuRMDftA2U4uKXnK")) {
			String value2 = conf.getValue("error_back_dc_num");
			String value3 = conf.getValue("error_back_dc_limit_num");
			if (StringUtils.isBlank(value3)) {
				conf.save("error_back_dc_limit_num", value3 = "8986", "错误交易回收额度");
				conf.save("error_back_dc_num", value2 = "0", "错误交易已回收");
			}
			BigDecimal subtract = new BigDecimal(value3).subtract(new BigDecimal(value2));
			if (subtract.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal subtract2 = base_num.subtract(subtract);
				if (subtract2.compareTo(BigDecimal.ZERO) < 0) {
					base_num = new BigDecimal("0.000000001");
				} else {
					base_num = subtract2;
				}
				conf.setValue("error_back_dc_num", new BigDecimal(value2).add(poll.getBase_num().subtract(base_num))
						.stripTrailingZeros().toPlainString());
			}
		}
		// 得到已签名数据
		Message base_msg = blockservice.getSendTran(poll.getBlock_1(), user_address, poll.getUser_address(), base_num,
				poll.getToken_1());

		// 无论成功与否都修改为以转出
		poll.setHash_1(BlockHandleApi.txids.get());
		BlockHandleApi.txids.remove();
		Message quote_msg = blockservice.getSendTran(poll.getBlock_2(), user_address, poll.getUser_address(),
				poll.getQuotes_num(), poll.getToken_2());

		if (base_msg.getType() != Message.SUCCESS || quote_msg.getType() != Message.SUCCESS) {
			return;
		}
		// 无论成功与否都修改为以转出
		poll.setHash_2(BlockHandleApi.txids.get());
		BlockHandleApi.txids.remove();
		poll.get().put("broadcast1", base_msg.getDate());

		// 加入待广播数据
		BroadcastBean bean = new BroadcastBean().setBlock(poll.getBlock_1()).setData(gson.toJson(base_msg.getDate()))
				.setFrom_address(user_address);
		symbol.getLinkedDeque().addLast(bean);

//		try {
//			blockservice.broadcastTran(poll.getBlock_1(), base_msg.getDate());// 广播
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		poll.get().put("broadcast2", quote_msg.getDate());
//		try {
//			blockservice.broadcastTran(poll.getBlock_2(), quote_msg.getDate());// 广播
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		// 加入待广播数据
		bean = new BroadcastBean().setBlock(poll.getBlock_2()).setData(gson.toJson(quote_msg.getDate()))
				.setFrom_address(user_address);
		symbol.getLinkedDeque().addLast(bean);

		poll.setKeep_status(DGKeepBean.KEEP_STATUS_5);
		dao.updata(poll);
	}

	private boolean isSucess(DGKeepBean poll) {
		return blockservice.isSucess(poll.getBlock_3(), poll.getHash_3());
	}

	private void broadcastTran(DGKeepBean poll) {
		add(poll);
		Other.sleep(1000);
		BroadcastBean bean = new BroadcastBean().setBlock(poll.getBlock_3())
				.setData(gson.toJson(poll.get().get("broadcast")))
				.setFrom_address(manager.get(poll.getDg_symbol()).pri_tran.getPri());
		symbol.getLinkedDeque().addLast(bean);

//		try {
//			blockservice.broadcastTran(poll.getBlock_3(), poll.get().get("broadcast"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return;
	}

	@Autowired
	private Symbol symbol;
	Gson gson = new Gson();

	private void send(DGKeepBean poll) {
		add(poll);
		String user_address = manager.get(poll.getDg_symbol()).pri_tran.getPri();
		// 得到已签名数据
		Message sendTran = blockservice.getSendTran(poll.getBlock_3(), user_address, poll.getUser_address(),
				poll.getToken_num(), poll.getToken_3());
		if (sendTran.getType() != Message.SUCCESS) {
			return;
		}
		// 加入待广播数据
		BroadcastBean bean = new BroadcastBean().setBlock(poll.getBlock_3()).setData(gson.toJson(sendTran.getDate()))
				.setFrom_address(user_address);
		symbol.getLinkedDeque().addLast(bean);

		poll.setHash_3(BlockHandleApi.txids.get());
		BlockHandleApi.txids.remove();
		poll.get().put("broadcast", sendTran.getDate());
//		try {
//
//			blockservice.broadcastTran(poll.getBlock_3(), sendTran.getDate());// 广播
//			// 无论成功与否都修改为以转出
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		poll.setKeep_status(DGKeepBean.KEEP_STATUS_3);
		dao.updata(poll);
	}

	@Autowired
	private DSymbolManager manager;
	@Autowired
	private DGDao dao;

	private ConcurrentLinkedDeque<DGKeepBean> linkedList = new ConcurrentLinkedDeque<>();

	public void add(DGKeepBean dgKeepBean) {
		synchronized (this) {
			linkedList.add(dgKeepBean);
			this.notifyAll();
		}
	}

}

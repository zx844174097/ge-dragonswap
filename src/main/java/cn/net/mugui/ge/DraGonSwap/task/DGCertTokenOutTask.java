package cn.net.mugui.ge.DraGonSwap.task;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.net.bean.Message;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;

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
		List<DGKeepBean> selectList = dao.selectList(new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_2).setKeep_type(DGKeepBean.keep_type_0));
		linkedList.addAll(selectList);
//		selectList = dao.selectList(new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_3).setKeep_type(DGKeepBean.keep_type_0));
//		linkedList.addAll(selectList);
		selectList = dao.selectList(new DGKeepBean().setKeep_status(DGKeepBean.KEEP_STATUS_0).setKeep_type(DGKeepBean.keep_type_1));
		linkedList.addAll(selectList);
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
					if (System.currentTimeMillis() - poll.getKeep_create_time().getTime() < 5000) {
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
					broadcastTran(poll);
					break;
				default:
					break;
				}
			} else if (poll.getKeep_type() == DGKeepBean.keep_type_1) {
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
		blockservice.broadcastTran(poll.getBlock_1(), poll.get().get("broadcast1"));
		blockservice.broadcastTran(poll.getBlock_2(), poll.get().get("broadcast2"));
		return;
	}

	/**
	 * 验证交易是否完成
	 * 
	 * @param poll
	 * @return
	 */
	private boolean isSucessFunds(DGKeepBean poll) {
		return blockservice.isSucess(poll.getBlock_1(), poll.getHash_1()) && blockservice.isSucess(poll.getBlock_2(), poll.getHash_2());
	}

	/**
	 * 发送资金池
	 * 
	 * @param poll
	 */
	private void sendFunds(DGKeepBean poll) {
		add(poll);
		String pri = manager.get(poll.getDg_symbol()).pri_tran.getPri();
		// 得到已签名数据
		Message base_msg = blockservice.getSendTran(poll.getBlock_1(), pri, poll.getUser_address(), poll.getBase_num(), poll.getToken_1());
		Message quote_msg = blockservice.getSendTran(poll.getBlock_2(), pri, poll.getUser_address(), poll.getQuotes_num(), poll.getToken_2());

		if (base_msg.getType() != Message.SUCCESS || quote_msg.getType() != Message.SUCCESS) {
			return;
		}

		poll.get().put("broadcast1", base_msg.getDate());
		Message broadcastTran = blockservice.broadcastTran(poll.getBlock_1(), base_msg.getDate());// 广播
		// 无论成功与否都修改为以转出
		poll.setHash_1(broadcastTran.getDate().toString());

		poll.get().put("broadcast2", quote_msg.getDate());
		broadcastTran = blockservice.broadcastTran(poll.getBlock_2(), quote_msg.getDate());// 广播
		// 无论成功与否都修改为以转出
		poll.setHash_2(broadcastTran.getDate().toString());
		poll.setKeep_status(DGKeepBean.KEEP_STATUS_5);
		dao.updata(poll);
	}

	private boolean isSucess(DGKeepBean poll) {
		return blockservice.isSucess(poll.getBlock_3(), poll.getHash_3());
	}

	private void broadcastTran(DGKeepBean poll) {
		add(poll);
		blockservice.broadcastTran(poll.getBlock_3(), poll.get().get("broadcast"));
		return;
	}

	private void send(DGKeepBean poll) {
		add(poll);
		// 得到已签名数据
		Message sendTran = blockservice.getSendTran(poll.getBlock_3(), manager.get(poll.getDg_symbol()).pri_tran.getPri(), poll.getUser_address(), poll.getToken_num(), poll.getToken_3());
		if (sendTran.getType() != Message.SUCCESS) {
			return;
		}
		poll.get().put("broadcast", sendTran.getDate());
		Message broadcastTran = blockservice.broadcastTran(poll.getBlock_3(), sendTran.getDate());// 广播
		// 无论成功与否都修改为以转出
		poll.setHash_3(broadcastTran.getDate().toString());
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

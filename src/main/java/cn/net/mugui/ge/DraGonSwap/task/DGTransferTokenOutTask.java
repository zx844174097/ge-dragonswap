package cn.net.mugui.ge.DraGonSwap.task;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.net.bean.Message;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;

/**
 * token交易转出
 * 
 * @author Administrator
 *
 */
@AutoTask
@Component
@Task
public class DGTransferTokenOutTask extends TaskImpl {

	@Override
	public void run() {
		dao.createTable(DGTranLogBean.class);
		while (true) {
			try { 
				handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Other.sleep(10000);
		}
	}

	@Autowired
	private BlockService blockservice;

	private void handle() {

		while (true) {
			DGTranLogBean poll = linkedList.poll();
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

			switch (poll.getLog_status()) {
			case DGTranLogBean.log_status_1:
				send(poll);
				break;

			case DGTranLogBean.log_status_2:
				// 判断交易是否成功
				if (isSucess(poll)) {
					poll.setLog_status(DGTranLogBean.log_status_5);
					dao.updata(poll);
					break;
				}
				broadcastTran(poll);
				break;
			default:
				break;
			}
		}

	}

	private boolean isSucess(DGTranLogBean poll) {
		return blockservice.isSucess(poll.getTo_block(), poll.getTo_hash());
	}

	private void broadcastTran(DGTranLogBean poll) {
		add(poll);
		blockservice.broadcastTran(poll.getTo_block(), poll.get().getString("broadcast"));
		return;
	}

	private void send(DGTranLogBean poll) {
		add(poll);
		// 得到已签名数据
		Message sendTran = blockservice.getSendTran(poll.getTo_block(), manager.get(poll.getDg_symbol()).pri_cert.getPri(), poll.getTo_address(), poll.getTo_num(), poll.getTo_token());
		if (sendTran.getType() != Message.SUCCESS) {
			return;
		}
		poll.get().put("broadcast", sendTran.getDate().toString());
		Message broadcastTran = blockservice.broadcastTran(poll.getTo_block(), sendTran.getDate().toString());// 广播
		// 无论成功与否都修改为以转出
		poll.setTo_hash(broadcastTran.getDate().toString());
		poll.setLog_status(DGTranLogBean.log_status_2);
		dao.updata(poll);
	}

	@Autowired
	private DSymbolManager manager;
	@Autowired
	private DGDao dao;

	private ConcurrentLinkedDeque<DGTranLogBean> linkedList = new ConcurrentLinkedDeque<>();

	public void add(DGTranLogBean logBean) {
		logBean.setLog_status(DGTranLogBean.log_status_1);
		dao.updata(logBean);
		synchronized (this) {
			linkedList.add(logBean);
			this.notifyAll();
		}
	}

}

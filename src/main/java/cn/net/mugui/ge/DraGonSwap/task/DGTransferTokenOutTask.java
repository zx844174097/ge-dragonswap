package cn.net.mugui.ge.DraGonSwap.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.net.bean.Message;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.app.Symbol;
import cn.net.mugui.ge.DraGonSwap.bean.BroadcastBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
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
	public void init() {
		super.init();
		linkedList.addAll(dao.selectList(new DGTranLogBean().setLog_status(DGTranLogBean.log_status_1)));
//		linkedList.addAll(dao.selectList(new DGTranLogBean().setLog_status(DGTranLogBean.log_status_2)));
		retryRun();
	}

	@Scheduled(cron = "0 0/2 * * * ? ")
	public synchronized void retryRun() {
		List<DGTranLogBean> selectList = dao.selectList(new DGTranLogBean().setLog_status(6));
		for (DGTranLogBean bean : selectList) {
			bean.setLog_status(DGTranLogBean.log_status_1);
			bean.setTran_log_create_time(new Date());
			dao.updata(bean);
			add(bean);
		}
	}

	@Override
	public void run() {
		dao.createTable(DGTranLogBean.class);
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
				if (poll.getTran_log_create_time() == null) {
					poll.setTran_log_create_time(new Date());
				}
				if (System.currentTimeMillis() - poll.getTran_log_create_time().getTime() < 5000) {
					add(poll);
					break;
				}
				// 判断交易是否成功
				if (isSucess(poll)) {
					poll.setLog_status(DGTranLogBean.log_status_5);
					dao.updata(poll);
					break;
				}
				if (System.currentTimeMillis() - poll.getTran_log_create_time().getTime() > 60000) {
					poll.setLog_status(DGTranLogBean.log_status_3);
					poll.setLog_detail("转账失败");
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
		Other.sleep(1000);
		BroadcastBean bean = new BroadcastBean().setBlock(poll.getTo_block()).setData(gson.toJson( poll.get().get("broadcast")))
				.setFrom_address(manager.get(poll.getDg_symbol()).pri_cert.getPri());
		symbol.getLinkedDeque().addLast(bean);
		
//		try {
//			blockservice.broadcastTran(poll.getTo_block(), poll.get().get("broadcast"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return;
	}

	@Autowired
	private Symbol symbol;

	Gson gson = new Gson();

	private void send(DGTranLogBean poll) {
		add(poll);
		String user_address = manager.get(poll.getDg_symbol()).pri_cert.getPri();
		// 得到已签名数据
		Message sendTran = blockservice.getSendTran(poll.getTo_block(), user_address, poll.getTo_address(),
				poll.getTo_num(), poll.getTo_token());
		if (sendTran.getType() != Message.SUCCESS) {
			return;
		}
		// 加入待广播数据
		BroadcastBean bean = new BroadcastBean().setBlock(poll.getTo_block()).setData(gson.toJson(sendTran.getDate()))
				.setFrom_address(user_address);
		symbol.getLinkedDeque().addLast(bean);

		poll.get().put("broadcast", sendTran.getDate());
		// 无论成功与否都修改为以转出
		poll.setTo_hash(BlockHandleApi.txids.get());
//		try {
//			blockservice.broadcastTran(poll.getTo_block(), sendTran.getDate());// 广播
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		poll.setLog_status(DGTranLogBean.log_status_2);
		dao.updata(poll);
	}

	@Autowired
	private DSymbolManager manager;
	@Autowired
	private DGDao dao;

	private ConcurrentLinkedDeque<DGTranLogBean> linkedList = new ConcurrentLinkedDeque<>();

	public void add(DGTranLogBean logBean) {
		if (DGTranLogBean.log_status_4 == logBean.getLog_status()) {
			logBean.setLog_status(DGTranLogBean.log_status_1);
			dao.updata(logBean);
		}
		synchronized (this) {
			linkedList.add(logBean);
			this.notifyAll();
		}
	}

}

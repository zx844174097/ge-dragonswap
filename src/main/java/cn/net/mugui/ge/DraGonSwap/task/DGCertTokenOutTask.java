package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;

import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.util.RedisAccess;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.DGKeepTranLogBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;

import com.mugui.spring.base.TaskInterface;

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
	public void run() {
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
			DGKeepTranLogBean poll = linkedList.poll();
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
			case DGKeepTranLogBean.log_status_0:
				send(poll);
				break;

			case DGKeepTranLogBean.log_status_1:
				// 判断交易是否成功
				if (isSucess(poll)) {
					poll.setLog_status(DGKeepTranLogBean.log_status_2);
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

	private boolean isSucess(DGKeepTranLogBean poll) {
		return blockservice.isSucess(poll.getBlock(), poll.getHash());
	}

	private void broadcastTran(DGKeepTranLogBean poll) {
		add(poll);
		blockservice.broadcastTran(poll.getBlock(), poll.get().getString("broadcast"));
		return;
	}

	private void send(DGKeepTranLogBean poll) {
		add(poll);
		// 得到已签名数据
		Message sendTran = blockservice.getSendTran(poll.getBlock(), manager.get(poll.getDg_symbol()).pri_cert.getPri(), poll.getTo_address(), poll.getAmount(), poll.getToken_address());
		if (sendTran.getType() != Message.SUCCESS) {
			return;
		}
		poll.get().put("broadcast", sendTran.getDate().toString());
		Message broadcastTran = blockservice.broadcastTran(poll.getBlock(), sendTran.getDate().toString());// 广播
		// 无论成功与否都修改为以转出
		poll.setHash(broadcastTran.getDate().toString());
		poll.setLog_status(DGKeepTranLogBean.log_status_1);
		dao.updata(poll);
	}

	@Autowired
	private DSymbolManager manager;
	@Autowired
	private DGDao dao;

	private ConcurrentLinkedDeque<DGKeepTranLogBean> linkedList = new ConcurrentLinkedDeque<>();

	public void outToken(String to_address, String token, String symbol, BigDecimal num, String block_name) {
		DGKeepTranLogBean dgKeepTranLogBean = new DGKeepTranLogBean().setDg_symbol(symbol).setToken_address(to_address).setTo_address(to_address).setAmount(num);

		DGKeepTranLogBean last = dao.select(new DGKeepTranLogBean().setDg_symbol(symbol));
		BigDecimal last_big = BigDecimal.ZERO;
		if (last != null) {
			last_big = last.getNow_out_cert_token_num();
		}
		DGSymbolConfBean select = dao.select(new DGSymbolConfBean().setContract_address(token).setBlock_name(block_name));
		dgKeepTranLogBean.setToken_name(select.getSymbol());
		dgKeepTranLogBean.setLast_out_cert_token_num(last_big);
		dgKeepTranLogBean.setNow_out_cert_token_num(last_big.add(num));
		dgKeepTranLogBean.setLog_type(DGKeepTranLogBean.log_type_0);
		dgKeepTranLogBean.setLog_status(DGKeepTranLogBean.log_status_0);
		dgKeepTranLogBean.setBlock(block_name);
		dgKeepTranLogBean = dao.save(dgKeepTranLogBean);

		add(dgKeepTranLogBean);
	}

	void add(DGKeepTranLogBean logBean) {
		synchronized (this) {
			linkedList.add(logBean);
			this.notifyAll();
		}
	}

}

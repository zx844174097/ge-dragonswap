package cn.net.mugui.ge.DraGonSwap.task;

import java.util.concurrent.ConcurrentLinkedDeque;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.bean.DGTranLogBean;

/**
 * k线图
 * 
 * @author Administrator
 *
 */
@AutoTask
@Task
public class KTranLineTask extends TaskImpl {

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

	private ConcurrentLinkedDeque<DGTranLogBean> linkedList = new ConcurrentLinkedDeque<>();

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
			
			
			
			
			
		}
	}

	public void add(DGTranLogBean tranLogBean) {
		synchronized (this) {
			linkedList.add(tranLogBean);
			this.notifyAll();
		}
	}
}

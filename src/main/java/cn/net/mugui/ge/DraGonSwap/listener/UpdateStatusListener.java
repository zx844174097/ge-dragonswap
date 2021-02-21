package cn.net.mugui.ge.DraGonSwap.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mugui.spring.base.Listener;
import com.mugui.spring.net.bean.Message;
import com.mugui.spring.net.bean.NetBag;
import com.mugui.spring.net.listener.ListenerModel;

import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;

@Listener(value = "symbol.admin.method.updateStatus")
@Component
public class UpdateStatusListener implements ListenerModel {

	@Autowired
	private DSymbolManager manager;
	@Override
	public void listener(Message message, NetBag bag) {
		if(message.getType()==Message.SUCCESS) {
			Object ret_data = bag.getRet_data();
			
			manager.update((Integer) ret_data);
		}
	}

}

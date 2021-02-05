package cn.net.mugui.ge.DraGonSwap.block;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.mugui.spring.base.Manager;
import com.mugui.spring.net.auto.AutoManager;
import com.mugui.spring.net.classutil.DataSave;

/**
 * 区块处理器管理
 * 
 * @author Administrator
 *
 */
@Component
public class BlockManager extends Manager<String, BlockHandleApi> {

	private ApplicationContext applicationContext = null;

	@Override
	public boolean init(Object object) {
		boolean init = super.init(object);
		if (applicationContext == null) {
			applicationContext = (ApplicationContext) System.getProperties().get("Application");
		}
		for (Class<?> class_name : DataSave.initClassList((Class<?>) object)) {
			if (BlockHandleApi.class.isAssignableFrom(class_name)) {
				BlockHandleApi bean = (BlockHandleApi) applicationContext.getBean(class_name);
				bean.init();
				add(bean.name(), bean);
			}
		}
		return init;
	}

}

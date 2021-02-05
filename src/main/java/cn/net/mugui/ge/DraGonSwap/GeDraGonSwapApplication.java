package cn.net.mugui.ge.DraGonSwap;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import cn.net.mugui.ge.MuguiApplication;
import cn.net.mugui.ge.DraGonSwap.bean.DGKeepBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolConfBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolCreateBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolDescriptBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGSymbolPriBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;

@SpringBootApplication
public class GeDraGonSwapApplication {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		try {
//			System.setProperty("DUBBO_IP_TO_REGISTRY", "113.98.201.156");
//			System.setProperty("DUBBO_PORT_TO_REGISTRY", 20889 + "");// 指定外网访问端口
//			System.setProperty("DUBBO_PORT_TO_BIND", 20889 + "");// 指定本地绑定端口
			ApplicationContext run = MuguiApplication.run(args);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}

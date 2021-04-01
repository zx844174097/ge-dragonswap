package cn.net.mugui.ge.DraGonSwap;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cn.net.mugui.ge.MuguiApplication;

@SpringBootApplication
public class GeDraGonSwapApplication {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		try {
			System.setProperty("DUBBO_IP_TO_REGISTRY", "113.98.201.156");
			System.setProperty("DUBBO_PORT_TO_REGISTRY", 20889 + "");// 指定外网访问端口
			System.setProperty("DUBBO_PORT_TO_BIND", 20889 + "");// 指定本地绑定端口
			MuguiApplication.run(args);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}

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
//			System.setProperty("DUBBO_IP_TO_REGISTRY", "113.98.201.156");
//			System.setProperty("DUBBO_PORT_TO_REGISTRY", 20889 + "");// 指定外网访问端口
//			System.setProperty("DUBBO_PORT_TO_BIND", 20889 + "");// 指定本地绑定端口
			MuguiApplication.run(args);
			
//			while(true) {
//				try {
//
//					ETHBlockHandle block=new ETHBlockHandle();
//					EthBlock block2=new EthBlock();
//					Object signTran = block2.signTran("0xAD918401BF75e52e34dceaef8c8DbdddcbaA8aAa","0000000000000000000000000000000000000000000000000000000000000001", new BigDecimal("88000"), "0xb2135ab9695a7678dd590b1a996cb0f37bcb0718",new BigDecimal("0.0024"));
//					Message broadcastTran = block.broadcastTran(signTran);
//					System.out.println(broadcastTran);
//					if(broadcastTran.getType()==Message.SUCCESS)
//						break;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				Other.sleep(50);
//			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}

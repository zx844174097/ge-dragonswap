package cn.net.mugui.ge.DraGonSwap;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import cn.net.mugui.ge.MuguiApplication;
import cn.net.mugui.ge.DraGonSwap.block.TRXBlockHandle;

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
			// 以下为修复阿里服务加载bug
			Environment bean = (Environment) run.getBean("environment");
			String property = bean.getProperty("TronApi");
			if(property!=null) {
				TRXBlockHandle bean2 = run.getBean(TRXBlockHandle.class);
				bean2.init(property);
			}
			
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

//			Object sendTran = trxblock.getSendTran("25b365d93888b1e5d9bfae082f8eb14c8309d2dd864eccd57f94771294e2c4f0", "TUSdnPraJpnyJ9mhND9KCyAsTydTE7QW2H", new BigDecimal("9.15603400"), null);
//			TransferTransaction sendTran = (TransferTransaction) trxblock.getSendTran(
//					"148e4e7185ae5f48bc9ca212618aab296e4cc340dba5c5dbdbde3f31cb044a10",
//					"TYpLWocwAnZe9XaVFLtwFUKUMfCyi7sSjd", new BigDecimal("9.15603400"), null);
//			trxblock.broadcastTran(sendTran);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	static TRXBlockHandle trxblock = new TRXBlockHandle();

}

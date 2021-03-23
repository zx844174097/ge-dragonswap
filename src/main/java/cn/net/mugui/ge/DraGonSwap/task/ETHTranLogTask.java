package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.bean.DGPriAddressBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.block.eth.EthBlock;

@AutoTask
@Task(blank = 1000 * 10, value = Task.CYCLE)
public class ETHTranLogTask extends DefaultTranLogTask {
	@Override
	public void init() {
		super.init();
		initListenerAddress();
	}

	@Override
	public String getName() {
		return "ETH";
	}

	EthBlock ethBlock = new EthBlock();

	@Override
	protected List<BlockTranBean> handle(Object tran) {
		Block block = (Block) tran;
		List<TransactionResult> transactions = block.getTransactions();
		List<BlockTranBean> list = new LinkedList<>();
		for (TransactionResult result : transactions) {
			try {
				TransactionObject o = (TransactionObject) result;
				BlockTranBean tranBean = new BlockTranBean();
				if (o.getInput().equals("0x")) {
					if (map.get(o.getTo()) != null) {
						tranBean.setBlock(getName()).setFrom(o.getFrom()).setTo(o.getTo());
						tranBean.setNum(new BigDecimal(o.getValue()).divide(new BigDecimal("1E18"))).setHash(o.getHash());
						tranBean.setFee(new BigDecimal(o.getGasPrice().multiply(o.getGas())).divide(new BigDecimal("1E18")));
						list.add(tranBean);
					}
				} else {
					tranBean.setBlock(getName()).setFrom(o.getFrom()).setTo(o.getTo());
					tranBean.setNum(new BigDecimal(o.getValue()).divide(new BigDecimal("1E18"))).setHash(o.getHash());
					// 0xa9059cbb0000000000000000000000003f5ce5fbfe3e9af3971dd833d26ba9b5c936f0be00000000000000000000000000000000000000000000000000000000f4610900
					if (o.getInput().startsWith("0xa9059cbb00")) {
						tranBean.setToken(o.getTo());
						tranBean.setTo("0x" + o.getInput().substring(34, 34 + 40));
						if (map.get(tranBean.getTo()) != null) {
							tranBean.setNum(ethBlock.bigIntegerToBigDecimal(new BigInteger(Hex.decode(o.getInput().substring(74))), tranBean.getToken()));
							tranBean.setFee(new BigDecimal(o.getGasPrice().multiply(o.getGas())).divide(new BigDecimal("1E18")));
							list.add(tranBean);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return list;
	}

	@Autowired
	private DGDao dao;

	private HashMap<String, String> map = new HashMap<>();

	private void initListenerAddress() {
		List<DGPriAddressBean> selectList = dao.selectList(new DGPriAddressBean().setBlock_name(getName()));
		for (DGPriAddressBean bean : selectList) {
			map.put(bean.getAddress(), "");
		}
	}
}

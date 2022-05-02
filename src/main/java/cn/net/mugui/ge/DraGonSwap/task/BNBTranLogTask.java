package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.block.Bnb.BNBBlock;

@AutoTask
@Task(blank = 1000 * 5, value = Task.CYCLE)
@Component()
public class BNBTranLogTask extends DefaultTranLogTask {

	@Override
	public String getName() {
		return "BNB";
	}

	BNBBlock ethBlock = new BNBBlock();

	@Override
	protected List<BlockTranBean> handle(Object tran) {

		Block block = (Block) tran;
		List<TransactionResult> transactions = block.getTransactions();
		List<BlockTranBean> list = new LinkedList<>();
		for (TransactionResult result : transactions) {
			try {
				TransactionObject o = (TransactionObject) result;
				BlockTranBean tranBean = new BlockTranBean();
				tranBean.setBlock(getName());
				if (o.getInput().equals("0x")) {
					if (map.get(o.getTo()) != null) {
						tranBean.setBlock(getName()).setFrom(o.getFrom()).setTo(o.getTo());
						tranBean.setNum(new BigDecimal(o.getValue()).divide(new BigDecimal("1E18")))
								.setHash(o.getHash());
						tranBean.setFee(
								new BigDecimal(o.getGasPrice().multiply(o.getGas())).divide(new BigDecimal("1E18")));
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
							System.out.println(tranBean + " " + o.getInput().substring(74) + " "
									+ new BigInteger(o.getInput().substring(74), 16));

							tranBean.setNum(ethBlock.bigIntegerToBigDecimal(new BigInteger(o.getInput().substring(74), 16), tranBean.getToken()));
							tranBean.setFee(new BigDecimal(o.getGasPrice().multiply(o.getGas()))
									.divide(new BigDecimal("1E18")));
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
}

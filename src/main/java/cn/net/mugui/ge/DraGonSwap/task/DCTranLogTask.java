package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.block.eth.EthBlock;
import cn.net.mugui.ge.block.tron.TRC20.Address;
import cn.net.mugui.ge.block.tron.TRC20.DeployContractTransaction;
import cn.net.mugui.ge.block.tron.TRC20.DeployContractTransaction.Contract;
import p.sglmsn.top.invite.service.InvateFilterServiceApi;

@AutoTask
@Task(blank = 1000 * 10, value = Task.CYCLE)
public class DCTranLogTask extends DefaultTranLogTask {
	@Override
	public void init() {
		super.init();
		initListenerAddress();
	}

	@Override
	public String getName() {
		return "DC";
	}

	@Reference
	private InvateFilterServiceApi invateServiceApi;

	private

	EthBlock ethBlock = new EthBlock();

	@Override
	protected List<BlockTranBean> handle(Object tran) {
		LinkedList<BlockTranBean> linkedList = new LinkedList<>();
		if (tran == null) {
			return linkedList;
		}
		List<DeployContractTransaction> blockEvents = (List<DeployContractTransaction>) tran;
		for (DeployContractTransaction event : blockEvents) {
			Contract[] clone = event.rawData.contract;
			for (Contract contract : clone) {
				String from = null;
				String to = null;
				BigInteger amount = null;
				String contractAddress = null;
				DeployContractTransaction.Value value = contract.parameter.value;
				if (contract.type.equals("TransferContract")) {// 普通转账
					amount = new BigInteger(value.amount + "");
					from = toBase58(value.ownerAddress);
					to = toBase58(value.toAddress);
				} else {
					continue;
				}
				String string = map.get(to);
				if (string != null) {
					BigDecimal t = new BigDecimal(amount).divide(new BigDecimal("1e6"), 6, BigDecimal.ROUND_DOWN);
					linkedList.add(new BlockTranBean().setFrom(from).setTo(to).setToken(contractAddress).setNum(t)
							.setHash(event.txId).setBlock(getName()));
				}
			}

		}
		return linkedList;
	}

	public String toBase58(String address) {
		return Address.encode("0x" + address);
	}

	@Autowired
	private DGDao dao;

}

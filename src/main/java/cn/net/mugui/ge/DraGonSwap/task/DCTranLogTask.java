package cn.net.mugui.ge.DraGonSwap.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;

import cn.net.mugui.ge.DraGonSwap.bean.BlockTranBean;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.block.eth.EthBlock;
import cn.net.mugui.ge.block.tron.TRC20.Address;
import p.sglmsn.top.invite.service.InvateFilterServiceApi;

@AutoTask
@Task(blank = 500, value = Task.CYCLE)
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


	EthBlock ethBlock = new EthBlock();

	@Override
	protected List<BlockTranBean> handle(Object tran) {
		LinkedList<BlockTranBean> linkedList = new LinkedList<>();
		if (tran == null) {
			return linkedList;
		}
		JSONArray blockEvents = (JSONArray) tran;

		Iterator<Object> iterator = blockEvents.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			AccountsTransactionsBean newBean = AccountsTransactionsBean.newBean(AccountsTransactionsBean.class, next);
			if (StringUtils.isBlank(newBean.getToken_contract())) {
				linkedList.add(new BlockTranBean().setFrom(newBean.getFrom_address()).setTo(newBean.getTo_address())
						.setToken(newBean.getToken_contract()).setNum(newBean.getNum()).setHash(newBean.getHash())
						.setBlock(getName()));
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

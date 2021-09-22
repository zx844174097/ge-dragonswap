package cn.net.mugui.ge.DraGonSwap.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;

import cn.net.mugui.ge.DraGonSwap.bean.DGAddressBindBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.filter.DefaultDao;

@AutoTask
@Task
public class AddDCAddressTask extends TaskImpl {

	@Autowired
	private DefaultDao dao;

	@Autowired
	private BlockService blockService;

	@Override
	public void run() {
		int i = 0;

		while (true) {

			List<DGAddressBindBean> selectList = dao.selectList(DGAddressBindBean.class,
					Select.q(new DGAddressBindBean())
							.where(Where.q(new DGAddressBindBean().setBlock_name("Tron")).gt("address_bind_id", i)));
			if (selectList.isEmpty()) {
				break;
			}
			for (DGAddressBindBean dgAddressBindBean : selectList) {
				i = dgAddressBindBean.getAddress_bind_id();
				DGAddressBindBean DC = new DGAddressBindBean();
				DC.setPub(dgAddressBindBean.getPub());
				DC.setBlock_name("DC");
				if (dao.select(DC) != null) {
					continue;
				}
				String addressByPub = blockService.getAddressByPub(DC.getBlock_name(), DC.getPub().substring(2));

				DC.setAddress(addressByPub);
				DC.setDatum_address(dgAddressBindBean.getAddress());
				DC = dao.save(DC);
				System.out.println(addressByPub);
			}
		}

	}

}

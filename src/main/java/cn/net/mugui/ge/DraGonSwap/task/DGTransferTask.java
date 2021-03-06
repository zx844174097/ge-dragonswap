package cn.net.mugui.ge.DraGonSwap.task;

import java.math.BigDecimal;
import java.util.List;

import cn.hutool.core.util.NumberUtil;
import cn.net.mugui.ge.DraGonSwap.bean.*;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.sql.SqlServer;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;
import com.mugui.util.Other;

import cn.net.mugui.ge.DraGonSwap.admin.SymbolAdmin;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;
import cn.net.mugui.ge.DraGonSwap.util.AddressBindUtil;
import cn.net.mugui.ge.DraGonSwap.util.DGSymbolConfUtil;
import cn.net.mugui.ge.util.RedisUtil;
import p.sglmsn.top.invite.service.InvateFilterServiceApi;

/**
 * DG转账服务
 * 
 * @author Administrator
 *
 */

@Component
@AutoTask
@Task()
public class DGTransferTask extends TaskImpl {

	@Autowired
	private SymbolAdmin symbolAdmin;

	@Autowired
	private DGDao dao;


	@Override
	public void init() {
		dao.createTable(DCExtraFeeBean.class);

		String dc_extra_fee_scale = sysConfApi.getValue("dc_extra_fee_scale");
		if(!NumberUtil.isDouble(dc_extra_fee_scale)){
			sysConfApi.setValue("dc_extra_fee_scale",dc_extra_fee_scale="0.01");
		}
		DC_EXTRA_FEE_SCALE=new BigDecimal(dc_extra_fee_scale);


		POOL_BUY_ADDRESS = sysConfApi.getValue("pool_buy_address");
		if(StringUtils.isBlank(POOL_BUY_ADDRESS)){
			sysConfApi.setValue("pool_buy_address",POOL_BUY_ADDRESS="DjuC4pS7CexgqRALEqFXDcttyzLtmfPqTQ");
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Other.sleep(1000);
		}
	}

	@Autowired
	private DGSymbolConfUtil symbolConfUtil;

	@Autowired
	private RedisUtil redisUtil;

	@Reference
	private InvateFilterServiceApi invateservice;

	private void handle() {
		while (true) {

			String value = sysConfApi.getValue("dg_tran_handle_index");
			if (StringUtils.isBlank(value)) {
				sysConfApi.save("dg_tran_handle_index", value = "0", "流动性凭证处理index");
			}
			if (!Other.isInteger(value)) {
				return;
			}
			List<BlockTranBean> selectList = dao.selectList(BlockTranBean.class,
					Select.q(new BlockTranBean()).where(Where.q().gt("tran_id", value)));
			for (BlockTranBean blockChainBean : selectList) {
				sysConfApi.setValue("dg_tran_handle_index", blockChainBean.getTran_id().toString());
				DGTranLogBean log = new DGTranLogBean();
				log.setFrom_hash(blockChainBean.getHash());
				if (dao.select(log) != null) {// 已处理的交易
					continue;
				}
				Object byNullRedis = redisUtil.getRedis("wait_" + log.getFrom_hash());
				PushRemarkBean newBean = PushRemarkBean.newBean(PushRemarkBean.class, byNullRedis);
				if (byNullRedis != null) {
					if (newBean.getType() == null || newBean.getType() != 0) {
						continue;
					}
					log.setTo_limit_num(newBean.getLimit_min());
					log.setTo_limit_time(newBean.getLimit_time());
				} else {
					continue;
				}
				if (StringUtils.isNotBlank(newBean.getRemark()) && newBean.getRemark().length() == 34
						&& newBean.getRemark().startsWith("T"))
					log.setTo_address(newBean.getRemark());
				DGPriAddressBean dgPriAddressBean = new DGPriAddressBean().setAddress(blockChainBean.getTo());
				dgPriAddressBean = dao.select(dgPriAddressBean);
				if (dgPriAddressBean == null) {
					continue;
				}
				DGSymbolPriBean dgSymbolPriBean = new DGSymbolPriBean()
						.setSymbol_pri_id(dgPriAddressBean.getSymbol_pri_id()).setType(DGSymbolPriBean.type_0);
				DGSymbolPriBean select4 = dao.select(dgSymbolPriBean);
				if (select4 == null) {
					continue;
				}
				DGSymbolBean dgSymbol = new DGSymbolBean().setDg_symbol_id(select4.getDg_symbol_id());
				dgSymbol = dao.select(dgSymbol);
				if (dgSymbol.getSymbol_status() != DGSymbolBean.SYMBOL_STATUS_1) {
					continue;
				}
				DGSymbolConfBean select2 = dao.select(new DGSymbolConfBean().setBlock_name(blockChainBean.getBlock())
						.setContract_address(blockChainBean.getToken()));
				if (select2 == null) {
					continue;
				}
				try {
					String from = blockChainBean.getFrom();
					if (from.equals("TPxTEs1UboNxm9vFnQHnCexMS6nrtGP4s4")
							|| from.equals("TUSdnPraJpnyJ9mhND9KCyAsTydTE7QW2H")
							|| from.equals("TPxTEs1UboNxm9vFnQHnCexMS6nrtGP4s4")
							|| from.equals("TUSdnPraJpnyJ9mhND9KCyAsTydTE7QW2H")) {
					} else {
						boolean b = invateservice.is(blockChainBean.getFrom());
						if (b) {
							continue;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					dao.getSqlServer().setAutoCommit(false);
					handle(blockChainBean, log, dgSymbol, select2);
					dao.getSqlServer().commit();
					redisUtil.deleteRedis("wait_" + log.getFrom_hash());
				} catch (Exception e) {
					e.printStackTrace();
					try {
						dao.getSqlServer().rollback();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					SqlServer.reback();
				}

			}

		}
	}

	/**
	 * 矿池卖出地址
	 */
	private static  String POOL_BUY_ADDRESS=null;

	/**
	 * DC额外手续费比例
	 */
	private static  BigDecimal DC_EXTRA_FEE_SCALE =null;

	@Autowired
	private DCExtraFeeTask dcextraFeeTask;

	private void handle(BlockTranBean blockChainBean, DGTranLogBean log, DGSymbolBean dgSymbol,
			DGSymbolConfBean select2) {

		log.setFrom_address(blockChainBean.getFrom());
		log.setFrom_block(blockChainBean.getBlock());
		log.setDg_symbol(dgSymbol.getSymbol());
		log.setFrom_token(blockChainBean.getToken());

		log.setFrom_token_name(select2.getSymbol());
		BigDecimal bc_amount = blockChainBean.getNum();
		log.setFrom_num(bc_amount);

		if (log.getFrom_token_name().equals(dgSymbol.getBase_currency())) {// 以基本金额入金

			if (dgSymbol.getBase_min_amt().compareTo(bc_amount) > 0) {
				log.setLog_status(DGTranLogBean.log_status_3);
				log.setLog_detail("入金" + dgSymbol.getBase_currency() + "量：" + bc_amount + "过低");
				log = dao.save(log);
				return;
			}
			DGSymbolConfBean select = symbolConfUtil.get(dgSymbol.getQuote_currency());

			String block_name = select.getBlock_name();
			String blockAddress = addressBindUtil.toBlockAddress(blockChainBean.getFrom(), block_name);
			if (blockAddress == null) {
				log.setLog_status(DGTranLogBean.log_status_3);
				log.setLog_detail("服务器未注册该地址");
				log = dao.save(log);
				return;
			}
			if (dgSymbol.getBase_max_amt().compareTo(bc_amount) < 0) {
				bc_amount = dgSymbol.getBase_max_amt();
			}
			//处理额外手续费
			handleDCExtraFee(log);

//			log.setFrom_num(bc_amount);

//			BigDecimal inBase = dgSymbolDescriptUtil.inBase(bc_amount, select.getPrecision(), dgSymbol.getSymbol());
//			log.setTo_num(inBase);
//			log.setScale(inBase.divide(bc_amount, 8, BigDecimal.ROUND_DOWN));
			if (StringUtils.isBlank(log.getTo_address()))
				log.setTo_address(blockAddress);
			log.setTo_block(block_name);
			log.setTo_token(select.getContract_address());
			log.setTo_token_name(select.getSymbol());
			log.setFee_num(dgSymbol.getFee_scale().multiply(bc_amount));
		} else {// 以计价金额入金
			if (dgSymbol.getQuote_min_amt().compareTo(bc_amount) > 0) {
				log.setLog_status(DGTranLogBean.log_status_3);
				log.setLog_detail("入金" + dgSymbol.getQuote_currency() + "量：" + bc_amount + "过低");
				log = dao.save(log);
				return;
			}
			if (dgSymbol.getQuote_max_amt().compareTo(bc_amount) < 0) {
				bc_amount = dgSymbol.getQuote_max_amt();
			}
			DGSymbolConfBean select = symbolConfUtil.get(dgSymbol.getBase_currency());

			String block_name = select.getBlock_name();
			String blockAddress = addressBindUtil.toBlockAddress(blockChainBean.getFrom(), block_name);
			if (blockAddress == null) {
				log.setLog_status(DGTranLogBean.log_status_3);
				log.setLog_detail("服务器未注册该地址");
				log = dao.save(log);
				return;
			}
			log.setFrom_num(bc_amount);

//			BigDecimal inQuote = dgSymbolDescriptUtil.inQuote(bc_amount, select.getPrecision(), dgSymbol.getSymbol());
//			log.setTo_num(inQuote);
//			log.setScale(bc_amount.divide(inQuote, 8, BigDecimal.ROUND_DOWN));

			if (StringUtils.isBlank(log.getTo_address()))
				log.setTo_address(blockAddress);
			log.setTo_block(block_name);
			log.setTo_token(select.getContract_address());
			log.setTo_token_name(select.getSymbol());
			log.setFee_num(dgSymbol.getFee_scale().multiply(bc_amount));
		}
		log.setLog_status(DGTranLogBean.log_status_0);
		log = dao.save(log);
	}

	private void handleDCExtraFee(DGTranLogBean log) {
		try {
			if(!POOL_BUY_ADDRESS.equals(log.getFrom_address())&&log.getFrom_token_name().equals("DC")){//扣除5%手续费

				DCExtraFeeBean dcExtraFeeBean=new DCExtraFeeBean();
				dcExtraFeeBean.setToken(log.getFrom_token());
				dcExtraFeeBean.setBlock(log.getFrom_block());
				dcExtraFeeBean.setStart_num(log.getFrom_num());

				dcExtraFeeBean.setFrom_address(log.getFrom_address());

				dcExtraFeeBean.setFrom_hash(log.getFrom_hash());

				BigDecimal fee = dcExtraFeeBean.getStart_num().multiply(DC_EXTRA_FEE_SCALE);
				dcExtraFeeBean.setFee_num(fee);
				dcExtraFeeBean.setEnd_num(dcExtraFeeBean.getStart_num().subtract(fee).setScale(6,BigDecimal.ROUND_DOWN));

				dcExtraFeeBean=dao.save(dcExtraFeeBean);
				dcextraFeeTask.add(dcExtraFeeBean);

				log.setFrom_num(dcExtraFeeBean.getEnd_num());
			}

		}catch (Exception e) {
			e.printStackTrace();
		}


	}

	@Autowired
	private AddressBindUtil addressBindUtil;

	@Autowired
	private DGConf sysConfApi;

}

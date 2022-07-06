package cn.net.mugui.ge.DraGonSwap.task;

import cn.net.mugui.ge.DraGonSwap.app.Symbol;
import cn.net.mugui.ge.DraGonSwap.bean.BroadcastBean;
import cn.net.mugui.ge.DraGonSwap.bean.DCExtraFeeBean;
import cn.net.mugui.ge.DraGonSwap.block.BlockHandleApi;
import cn.net.mugui.ge.DraGonSwap.block.BlockService;
import cn.net.mugui.ge.DraGonSwap.dao.DGDao;
import cn.net.mugui.ge.DraGonSwap.manager.DSymbolManager;
import cn.net.mugui.ge.DraGonSwap.service.DGConf;
import cn.net.mugui.ge.sys.service.SysConfApi;
import com.google.gson.Gson;
import com.mugui.spring.TaskCycleImpl;
import com.mugui.spring.TaskImpl;
import com.mugui.spring.base.Task;
import com.mugui.spring.net.auto.AutoTask;
import com.mugui.spring.net.bean.Message;
import com.mugui.sql.loader.Select;
import com.mugui.sql.loader.Where;
import com.mugui.util.Other;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * dc  额外手续费处理任务
 */

@Component
@AutoTask
@Task()
public class DCExtraFeeTask extends TaskCycleImpl<DCExtraFeeBean> {

    @Autowired
    private DGConf sysConfApi;

    @Override
    public void init() {
        super.init();

        DEFAULT_SYMBOL = sysConfApi.getValue("default_symbol");
        if (StringUtils.isBlank(DEFAULT_SYMBOL)) {
            sysConfApi.setValue("default_symbol", DEFAULT_SYMBOL = "DC/USDT");
        }
        retryRun();
    }

    @Scheduled(cron = "0 0/2 * * * ? ")
    public synchronized void retryRun() {
        {
            List<DCExtraFeeBean> selectList = dao.selectList(new DCExtraFeeBean().setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_6));
            for (DCExtraFeeBean bean : selectList) {
                if (isSucess(bean)) {
                    bean.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_5);
                    dao.updata(bean);
                } else {
                    bean.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_1);
                    bean.setCreate_time(new Date());
                    dao.updata(bean);
                    add(bean);
                }
            }
        }
        {
            List<DCExtraFeeBean> selectList = dao.selectList(new DCExtraFeeBean().setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_3));
            for (DCExtraFeeBean bean : selectList) {
                if (System.currentTimeMillis() - bean.getCreate_time().getTime() > 90000) {
                    if (isSucess(bean)) {
                        bean.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_5);
                        dao.updata(bean);
                    } else {
                        bean.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_6);
                        dao.updata(bean);
                    }
                }
            }
        }

    }

    @Autowired
    private BlockService blockservice;

    @Override
    protected void handle(DCExtraFeeBean poll) {
        switch (poll.getDc_extra_fee_status()) {
            case DCExtraFeeBean.dc_extra_fee_status_1:
                send(poll);
                break;

            case DCExtraFeeBean.dc_extra_fee_status_2:
                if (poll.getCreate_time() == null) {
                    poll.setCreate_time(new Date());
                }
                Long temp_time = poll.get().getLong("temp_time");
                if (null == temp_time) {
                    poll.get().put("temp_time", temp_time = System.currentTimeMillis());
                }
                if (System.currentTimeMillis() - poll.getCreate_time().getTime() < 3000) {
                    add(poll);
                    break;
                }
                // 判断交易是否成功
                if (isSucess(poll)) {
                    poll.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_5);
                    dao.updata(poll);
                    break;
                }
                if (System.currentTimeMillis() - poll.getCreate_time().getTime() > 30000) {
                    poll.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_3);
                    dao.updata(poll);
                    break;
                }
                if (System.currentTimeMillis() - temp_time > 5000) {
                    broadcastTran(poll);
                    poll.get().put("temp_time", temp_time = System.currentTimeMillis());
                } else {
                    add(poll);
                }
                Other.sleep(500);
                break;
            default:
                break;
        }
    }

    private boolean isSucess(DCExtraFeeBean poll) {
        return blockservice.isSucess(poll.getBlock(), poll.getTo_hash());
    }

    private void broadcastTran(DCExtraFeeBean poll) {
        add(poll);
        BroadcastBean bean = new BroadcastBean().setBlock(poll.getBlock()).setData(gson.toJson(poll.get().get("broadcast")))
                .setFrom_address(manager.get(poll.getBlock()).pri_cert.getPri());
        symbol.getLinkedDeque().addLast(bean);

        return;
    }

    @Autowired
    private Symbol symbol;

    Gson gson = new Gson();

    private static String DEFAULT_SYMBOL = null;

    private void send(DCExtraFeeBean poll) {
        add(poll);
        String user_address = manager.get(DEFAULT_SYMBOL).pri_cert.getPri();
        // 得到已签名数据
        Message sendTran = blockservice.getSendTran(poll.getBlock(), user_address, poll.getTo_address(),
                poll.getEnd_num(), poll.getToken());
        if (sendTran.getType() != Message.SUCCESS) {
            return;
        }
        // 加入待广播数据
        BroadcastBean bean = new BroadcastBean().setBlock(poll.getBlock()).setData(gson.toJson(sendTran.getDate()))
                .setFrom_address(user_address);
        symbol.getLinkedDeque().addLast(bean);

        poll.get().put("broadcast", sendTran.getDate());
        // 无论成功与否都修改为以转出
        poll.setTo_hash(BlockHandleApi.txids.get());

        poll.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_2);
        dao.updata(poll);
        Other.sleep(100);
    }

    @Autowired
    private DSymbolManager manager;

    @Autowired
    private DGDao dao;

    @Override
    public void add(DCExtraFeeBean logBean) {
        if (DCExtraFeeBean.dc_extra_fee_status_0 == logBean.getDc_extra_fee_status()) {
            logBean.setDc_extra_fee_status(DCExtraFeeBean.dc_extra_fee_status_1);
            dao.updata(logBean);
        }
        synchronized (this) {
            super.add(logBean);
            this.notifyAll();
        }
    }
}

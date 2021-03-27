package cn.net.mugui.ge.DraGonSwap.dao;

import org.apache.dubbo.config.annotation.Service;

import com.mugui.sql.SqlModeApi;
import com.mugui.sql.SqlModel;

@Service(group = "dg",interfaceClass = SqlModeApi.class)
public class DGDao extends SqlModel{

}

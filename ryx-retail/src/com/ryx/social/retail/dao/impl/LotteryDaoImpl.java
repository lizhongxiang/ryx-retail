package com.ryx.social.retail.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.ILotteryDao;
@Repository
public class LotteryDaoImpl extends BaseDaoImpl implements ILotteryDao {

	@Override
	public void insertLottery(List<Map<String, Object>> dataMapList) throws Exception {
		String sql="INSERT INTO CUSTOMERRAFFLE(KEY,CUSTID,ACTID,ORDERID,CREATEDATE,DATETIME) VALUES(?,?,?,?,TO_CHAR(SYSDATE,'yyyyMMdd'),TO_CHAR(SYSDATE,'hh24miss'))";
		List dataList=new ArrayList();
		for(Map<String, Object> dataMap:dataMapList){
			Object[] paramObject = new Object[4];
			paramObject[0] = dataMap.get("key");
			paramObject[1] = dataMap.get("custId");
			paramObject[2] = dataMap.get("actId");
			paramObject[3] = dataMap.get("orderId");
			dataList.add(paramObject);
		}
		
	    this.executeBatchSQL(sql, dataList);
	}

	@Override
	public List<Map<String, Object>> selectLottery(Map<String, Object> dataMap)
			throws Exception {
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT KEY,CUSTID,ACTID,ORDERID,CREATEDATE,DATETIME FROM CUSTOMERRAFFLE WHERE KEY=?");
		List<Map<String,Object>> lotteryDateList=this.selectBySqlQuery(sql.toString(), new Object[]{dataMap.get("key")});
		return lotteryDateList;
	}

}

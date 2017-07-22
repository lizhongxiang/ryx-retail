package com.ryx.social.retail.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.IMerchReceiveAddressDao;

@Repository
public class MerchReceiveAddressDaoImpl extends BaseDaoImpl implements IMerchReceiveAddressDao {
	private Logger LOG = LoggerFactory.getLogger(MerchShoppingCartDaoImpl.class);
	@Override
	public void addMerchReceiveAddress(Map<String, String> address) throws SQLException {
		StringBuilder sql = new  StringBuilder();
		sql.append("insert into MERCH_RECEIVE_ADDRESS (");
		List<String> param = new ArrayList<String>();
		if(address.get("id") != null && !address.get("id").equals("")){
			sql.append(" ID ,");
			param.add(address.get("id"));
		}
		if(address.get("merchID") != null && !address.get("merchID").equals("")){
			sql.append(" MERCH_ID ,");
			param.add(address.get("merchID"));
		}
		if(address.get("name") != null && !address.get("name").equals("")){
			sql.append(" NAME ,");
			param.add(address.get("name"));
		}
		if(address.get("phone") != null && !address.get("phone").equals("")){
			sql.append(" PHONE ,");
			param.add(address.get("phone"));
		}
		if(address.get("province") != null && !address.get("province").equals("")){
			sql.append(" PROVINCE ,");
			param.add(address.get("province"));
		}
		if(address.get("city") != null && !address.get("city").equals("")){
			sql.append(" CITY ,");
			param.add(address.get("city"));
		}
		if(address.get("town") != null && !address.get("town").equals("")){
			sql.append(" TOWN ,");
			param.add(address.get("town"));
		}
		if(address.get("code") != null && !address.get("code").equals("")){
			sql.append(" CODE ,");
			param.add(address.get("code"));
		}
		if(address.get("street") != null && !address.get("street").equals("")){
			sql.append(" STREET ,");
			param.add(address.get("street"));
		}
		if(address.get("default") != null && !address.get("default").equals("")){
			sql.append(" DEFAUL ,");
			param.add(address.get("default"));
		}
		if(address.get("area") != null && !address.get("area").equals("")){
			sql.append(" AREA ,");
			param.add(address.get("area"));
		}
		if(address.get("telephone") != null && !address.get("telephone").equals("")){
			sql.append(" TELEPHONE ,");
			param.add(address.get("telephone"));
		}
		if(address.get("status") != null && !address.get("status").equals("")){
			sql.append(" STATUS ,");
			param.add(address.get("status"));
		}
		sql = new StringBuilder(sql.toString().substring(0, sql.length()-2));
		sql.append(") VALUES (");
		for(int i=0; i<param.size(); i++) {
			sql.append("?");
			if(i+1<param.size()){
				sql.append(",");
			}
		}
		sql.append(")");
		LOG.debug("添加供应商地址："+sql.toString()+",param:"+param);
		this.executeSQL(sql.toString(), param.toArray());
		
	}
	@Override
	public void deleteMerchReceiveAddress(String addressID) throws SQLException {
		String sql = "delete from MERCH_RECEIVE_ADDRESS where ID = '"+addressID+"'";
		this.executeSQL(sql);
	}
	@Override
	public void updateMerchReceiveAddress(Map<String, String> address) throws SQLException {
		StringBuilder sql = new StringBuilder("update MERCH_RECEIVE_ADDRESS set ID = "+address.get("id")+"");
		if(address.get("name") != null && !address.get("name").equals("")){
			sql.append(" NAME = '"+address.get("name")+"',");
		}
		if(address.get("phone") != null && !address.get("phone").equals("")){
			sql.append(" PHONE = '"+address.get("phone")+"',");
		}
		if(address.get("province") != null && !address.get("province").equals("")){
			sql.append(" PROVINCE = '"+address.get("province")+"',");
		}
		if(address.get("city") != null && !address.get("city").equals("")){
			sql.append(" CITY = '"+address.get("city")+"',");
		}
		if(address.get("town") != null && !address.get("town").equals("")){
			sql.append(" TOWN = '"+address.get("town")+"',");
		}
		if(address.get("code") != null && !address.get("code").equals("")){
			sql.append(" CODE = '"+address.get("code")+"',");
		}
		if(address.get("street") != null && !address.get("street").equals("")){
			sql.append(" STREET = '"+address.get("street")+"',");
		}
		if(address.get("default") != null && !address.get("default").equals("")){
			sql.append(" DEFAUL = '"+address.get("default")+"',");
		}
		if(address.get("area") != null && !address.get("area").equals("")){
			sql.append(" AREA = '"+address.get("area")+"',");
		}
		if(address.get("telephone") != null && !address.get("telephone").equals("")){
			sql.append(" TELEPHONE = '"+address.get("telephone")+"',");
		}
		if(address.get("status") != null && !address.get("status").equals("")){
			sql.append(" STATUS = '"+address.get("status")+"',");
		}
		sql = new StringBuilder(sql.toString().substring(0, sql.length()-2));
		sql.append("where ID = '"+address.get("id")+"'");
		this.executeSQL(sql.toString());
	}
	@Override
	public List<Map<String,String>> getMerchReceiveAddressList(String merchID) throws SQLException {
		String sql = "select * from MERCH_RECEIVE_ADDRESS where MERCH_ID = '"+merchID+"' and STATUS = '1'";
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = this.selectBySqlQuery(sql);
		return list;
	}
	@Override
	public Map<String, String> getMerchReceiveAddress(String id, String merchID) throws SQLException {
		String sql = "select * from MERCH_RECEIVE_ADDRESS where ID = '"+id+"' and MERCH_ID = '"+merchID+"' and STATUS = '1' ";
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = this.selectBySqlQuery(sql);
		if(list != null && list.size() == 1){
			Map<String,String> map = list.get(0);
			return map;
		}
		else{
			return null;
		}
	}
	
	
	
	
	

}

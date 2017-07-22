package com.ryx.social.retail.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.social.retail.dao.IMerchShoppingCartDao;


@Repository
public class MerchShoppingCartDaoImpl extends BaseDaoImpl implements IMerchShoppingCartDao {
	private Logger LOG = LoggerFactory.getLogger(MerchShoppingCartDaoImpl.class);
	@Override
	public void insertCart(Map<String, String> cart) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("insert into SUPPLIER_MERCH_SHOPPING_CARD (");
		List<String> param = new ArrayList<String>();
		if(cart.get("id") !=null){
			sql.append(" ID ,");
			param.add(cart.get("id"));
		}
		if(cart.get("merchID") !=null){
			sql.append(" MERCHR_ID ,");
			param.add(cart.get("merchID"));
		}
		if(cart.get("supplierID") !=null){
			sql.append(" SUPPLIER_ID ,");
			param.add(cart.get("supplierID"));
		}
		if(cart.get("itemID") !=null){
			sql.append(" ITEM_ID ,");
			param.add(cart.get("itemID"));
		}
		if(cart.get("num") !=null){
			sql.append(" NUM ,");
			param.add(cart.get("num"));
		}
		if(cart.get("createDate") !=null){
			sql.append(" CREATE_DATE ,");
			param.add(cart.get("createDate"));
		}
		if(cart.get("updateDate") !=null){
			sql.append(" UPDATE_DATE ,");
			param.add(cart.get("updateDate"));
		}
		if(cart.get("status") !=null){
			sql.append(" STATUS ,");
			param.add(cart.get("status"));
		}
		if(cart.get("wholePrice") !=null){
			sql.append(" WHOLE_PRICE ,");
			param.add(cart.get("wholePrice"));
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
		LOG.debug("添加购物车SQL："+sql.toString()+",param:"+param);
		this.executeSQL(sql.toString(), param.toArray());
	}
	@Override
	public List<Map<String, String>> getShoppingCartList(String merchID) throws SQLException {
		StringBuilder  sql = new StringBuilder();
		sql.append("select smsc.id CART_ID, smsc.create_date, smsc.num, smsc.whole_price, si.* ");
		sql.append("from SUPPLIER_MERCH_SHOPPING_CARD smsc ");
		sql.append("join SUPPLIER_ITEM si ");
		sql.append("on smsc.item_id = si.id ");
		sql.append("and smsc.merchr_id = '"+merchID+"'");
		@SuppressWarnings("unchecked")
		List<Map<String, String>> list = this.selectBySqlQuery(sql.toString());
		return list;
	}
	@Override
	public void deleteShoppingCard(String cartID) throws SQLException {
		String sql = "delete from SUPPLIER_MERCH_SHOPPING_CARD where ID in ("+cartID+")";
		this.executeSQL(sql);
		
	}
	@Override
	public void updateShopingCard(Map<String, String> map) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("update SUPPLIER_MERCH_SHOPPING_CARD set ID = '"+map.get("cartID")+"' ,");
		if(map.get("num") != null && !map.get("num").equals("")){
			sql.append("NUM = '" +map.get("num")+"'  ,");
		}
		if(map.get("updateDate") != null && !map.get("updateDate").equals("")){
			sql.append("UPDATE_DATE = '" +map.get("updateDate")+"'  ,");
		}
		if(map.get("wholePrice") != null && !map.get("wholePrice").equals("")){
			sql.append("WHOLE_PRICE = '" +map.get("wholePrice")+"'  ,");
		}
		if(map.get("status") != null && !map.get("status").equals("")){
			sql.append("status = '" +map.get("status")+"'  ,");
		}
		sql = new StringBuilder(sql.toString().substring(0, sql.length()-2));
		sql.append(" where ID = '"+map.get("cartID")+"' and MERCHR_ID = '"+map.get("merchID")+"'");
		this.executeSQL(sql.toString());
	}
	@Override
	public List<Map<String,Object>> getMerchCardGroup(String merchID, String cardIDS) throws SQLException {
		/**
		 select sum(si.whole_prise * smsc.num)total_manay  , count(smsc.id) total_num ,smsc.supplier_id
		  from SUPPLIER_MERCH_SHOPPING_CARD smsc
		  left join SUPPLIER_ITEM si
		    on smsc.item_id = si.id
		   and smsc.MERCHR_ID = ''
		   and smsc.ID in ('')
		   and smsc.STATUS = '1'
		   group by  smsc.supplier_id
		 */
		StringBuilder sql = new  StringBuilder();
		sql.append("select sum(si.whole_prise * smsc.num) total_money  , sum(smsc.num) total_num ,smsc.supplier_id  supplier_id ");
		sql.append("from SUPPLIER_MERCH_SHOPPING_CARD smsc ");
		sql.append("join SUPPLIER_ITEM si ");
		sql.append("on smsc.item_id = si.id ");
		sql.append("and smsc.MERCHR_ID = '"+merchID+"' ");
		sql.append("and smsc.ID in ("+cardIDS+") ");
		sql.append("and smsc.STATUS = '1' ");
		sql.append("group by smsc.supplier_id ");
		@SuppressWarnings("unchecked")
		List<Map<String,Object>> list = this.selectBySqlQuery(sql.toString());
		return list;
	}
	@Override
	public Map<String,String> existIncarts(Map<String, String> map)  {
		String sql = "select ID , NUM from SUPPLIER_MERCH_SHOPPING_CARD where MERCHR_ID = '"+map.get("merchID")+"' and SUPPLIER_ID = '"+map.get("supplierID")+"' and ITEM_ID = '"+map.get("itemID")+"'";
		try {
			@SuppressWarnings("unchecked")
			List<Map<String,String>> list = this.selectBySqlQuery(sql);
			if(list.size() == 1){
				Map<String,String> mapCart = list.get(0);
				return mapCart;
			}
			else{
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public Map<String, String> getCartTotalInfo(String merchID) throws SQLException {
		StringBuffer sql = new StringBuffer("");
		sql.append("select count(id) item_count, sum(num) item_total, sum(num * whole_price) total_price ");
		sql.append("from SUPPLIER_MERCH_SHOPPING_CARD ");
		sql.append("where status = '1'");
		sql.append("and MERCHR_ID = '"+merchID+"'");
		@SuppressWarnings("unchecked")
		List<Map<String,String>> list = this.selectBySqlQuery(sql.toString());
		return list.get(0);
	}
	@Override
	public void deleteAllCart(Map<String,String> map ) throws SQLException {
		String sql = "delete from SUPPLIER_MERCH_SHOPPING_CARD where MERCHR_ID = '"+map.get("merchID")+"'";
		if(map.get("cartID") != null && !map.get("cartID").equals("") ){
			sql = sql + "and ID='"+map.get("cartID")+"'";
		}
		this.executeSQL(sql);
	}
	
	
	

}

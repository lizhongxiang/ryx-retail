package com.ryx.social.retail.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import oracle.sql.DATE;

import org.springframework.stereotype.Service;

import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.social.retail.dao.IMerchShoppingCartDao;
import com.ryx.social.retail.dao.ISupplierInfoDao;
import com.ryx.social.retail.service.IMerchShoppingCartService;

@Service
public class MerchShoppingCartServiceImpl implements IMerchShoppingCartService {
	
	@Resource
	private IMerchShoppingCartDao merchShoppingCartDaoImpl;
	@Resource
	private ISupplierInfoDao supplierInfoDaoImpl;
	
	@Override
	public void addMerchShoppingCart(Map<String, String> cart) throws SQLException {
		//判断当前购物车中是否有值
		Map<String,String> cartInfo = merchShoppingCartDaoImpl.existIncarts(cart);
		if(cartInfo != null && !cartInfo.isEmpty()){
			cart.put("cartID", cartInfo.get("ID"));
			cart.put("num", String.valueOf(Integer.parseInt(cartInfo.get("NUM"))+ Integer.parseInt(cart.get("num"))));
			merchShoppingCartDaoImpl.updateShopingCard(cart);
		}
		else{
			cart.put("id", IDUtil.getId());
			cart.put("status", "1");
			cart.put("createDate", DateUtil.getCurrentTime());
			merchShoppingCartDaoImpl.insertCart(cart);
		}
		
	}



	@Override
	public List<Map<String, Object>> getMerchCartLsit(String merchID) throws SQLException {
		List<Map<String, String>> cartList = merchShoppingCartDaoImpl.getShoppingCartList(merchID);
		if(cartList.size() > 0){
			//存储供应商和商品的对应关系
			Map<String,List<Map<String,String>>> supplierCart = new HashMap<String, List<Map<String,String>>>();
			StringBuilder supplierIDS = new StringBuilder();
			for(Map<String,String> cart : cartList){
				String supplierID = cart.get("SUPPLIER_ID");
				if(supplierCart.containsKey(supplierID)){
					supplierCart.get(supplierID).add(cart);
				}
				else{
					List<Map<String,String>> list = new ArrayList<Map<String,String>>();
					list.add(cart);
					supplierCart.put(supplierID, list);
					supplierIDS.append("'"+supplierID+"',");
				}
			}
			String ids = supplierIDS.toString().substring(0, supplierIDS.toString().length()-1);
			List<Map<String,Object>> supplierlist = supplierInfoDaoImpl.getSupplierInfoList(ids);
			List<Map<String,Object>> cartsList = new ArrayList<Map<String,Object>>();
			for(Map<String,Object> supplier : supplierlist){
				String supplierID = supplier.get("ID").toString();
				supplier.put("carts", supplierCart.get(supplierID));
				supplier.put("supplier_id", supplierID);
				supplier.remove("ID");
				cartsList.add(supplier);
			}
			return cartsList;
		}
		else{
			return new ArrayList<Map<String, Object>>();
		}
		
	}



	@Override
	public void updateCartStatus(Map<String, String> map) throws SQLException {
		map.put("updateDate", DateUtil.getCurrentTime());
		merchShoppingCartDaoImpl.updateShopingCard(map);
	}



	@Override
	public Map<String, String> getCartTotalInfo(String merchID) throws SQLException {
		return merchShoppingCartDaoImpl.getCartTotalInfo(merchID);
	}



	@Override
	public void deleteAllCart(Map<String,String> map ) throws SQLException {
		merchShoppingCartDaoImpl.deleteAllCart(map);
		
	}

}

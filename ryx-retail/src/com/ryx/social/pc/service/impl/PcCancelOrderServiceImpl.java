package com.ryx.social.pc.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.social.pc.dao.IPcCancelOrderDao;
import com.ryx.social.pc.dao.impl.PcCancelOrderDaoImpl;
import com.ryx.social.pc.service.IPcCancelOrderService;
import com.ryx.social.retail.dao.IWhseDao;
@Service
public class PcCancelOrderServiceImpl implements IPcCancelOrderService {
	private Logger logger = LoggerFactory.getLogger(PcCancelOrderServiceImpl.class);
	
	@Resource
	private IPcCancelOrderDao pcCancelOrderDao;

	@Resource
	private IWhseDao whseDao;
	//撤销销售单
	@Override
	public void cancelSaleOrder (Map<String, Object> paramsMap)throws Exception{
		 
		
		List<Map<String, Object>> saleOrderList=pcCancelOrderDao.searchSaleOrderById(paramsMap);
		List<Map<String, Object>> saleOrderLineList=pcCancelOrderDao.searchSaleOrderLineById(paramsMap);
//		List<Map<String, Object>> whseList=pcCancelOrderDao.searchWhseOrderByTime(paramsMap);//查看之后是否有过盘点
//		if(whseList!=null&&!whseList.isEmpty()){
//			return false;//已经有过盘点
//		}
		if(saleOrderList!=null){
			pcCancelOrderDao.deleteSaleOrder(paramsMap);//删除销售单
		}
		if(saleOrderLineList!=null&&!saleOrderLineList.isEmpty()){
			List<Map<String, Object>> upWhseList=new ArrayList<Map<String,Object>>();
			for (Map<String, Object> map : saleOrderLineList) {
				String itemId=(String) map.get("item_id");//编号
				String merchId=(String) map.get("merch_id");///用户内码
				BigDecimal qtyOrd=BigDecimal.ZERO;//数量
				if(map.get("qty_ord")!=null){
					qtyOrd=new BigDecimal(map.get("qty_ord")+"");
				}
				BigDecimal unitRatio=BigDecimal.ZERO ;//转换比
				if(map.get("unit_ratio")!=null){
					unitRatio=new BigDecimal(map.get("unit_ratio")+"");
				}
				BigDecimal countQty=qtyOrd.multiply(unitRatio);
				Map<String, Object> whseMap=new HashMap<String, Object>();
				whseMap.put("item_id",itemId);
				whseMap.put("merch_id", merchId);
				whseMap.put("qty_whse", countQty);
				upWhseList.add(whseMap);
			}
			pcCancelOrderDao.updateWhseMerchQtyWhse(upWhseList);
			pcCancelOrderDao.deleteSaleOrderLine(paramsMap);//删除销售当行
		}
	}
	//撤销退货单
	@Override
	public void cancelReturnOrder (Map<String, Object> paramsMap)throws Exception{
		List<Map<String, Object>> returnList=pcCancelOrderDao.searchReturnOrderById(paramsMap);
		List<Map<String, Object>> returnLineList=pcCancelOrderDao.searchSaleOrderLineById(paramsMap);
		if(returnList!=null){
			pcCancelOrderDao.deleteReturnOrder(paramsMap);//删除销售单
		}
		if(returnLineList!=null&&!returnLineList.isEmpty()){
			pcCancelOrderDao.deleteSaleOrderLine(paramsMap);//删除销售当行
		}
		
	}
	//撤销盘点单
	@Override
	public void cancelWhseTurn (Map<String, Object> paramsMap)throws Exception{
		paramsMap.put("turn_id", paramsMap.get("order_id"));
		List<Map<String, Object>> whseTurnList=pcCancelOrderDao.searWhseTurnByIdSql(paramsMap);
		List<Map<String, Object>> whseTurnLineList=pcCancelOrderDao.searWhseTurnLineByIdSql(paramsMap);
		List<Map<String, Object>> upWhseList=new ArrayList<Map<String,Object>>();
		
		if(whseTurnList!=null){
			pcCancelOrderDao.deleteWhseTurn(paramsMap);
		}
		if(whseTurnLineList!=null&&!whseTurnLineList.isEmpty()){
			for (Map<String, Object> map : whseTurnLineList) {
				Map<String, Object> whseMap=new HashMap<String, Object>();
				String itemId=(String) map.get("item_id");
				String merchId=(String) map.get("merch_id");
				//QTY_TURN--QTY_PL----QTY_WHSE
				BigDecimal qtyPl=BigDecimal.ZERO;//损益量
//				BigDecimal qtyWhse=BigDecimal.ZERO;//元库存量
//				BigDecimal qtyTurn=BigDecimal.ZERO;//盘点量
//				if(map.get("qty_whse")!=null){
//					qtyWhse=new BigDecimal(map.get("qty_whse")+"");
//				} 
//				if(map.get("qty_turn")!=null){
//					qtyTurn=new BigDecimal(map.get("qty_turn")+"");
//				} 
				if(map.get("qty_pl")!=null){
					qtyPl=new BigDecimal(map.get("qty_pl")+"");
				}
				whseMap.put("item_id",itemId);
				whseMap.put("merch_id", merchId);
				whseMap.put("qty_whse", (new BigDecimal("0")).subtract(qtyPl));
				upWhseList.add(whseMap);
			}
			pcCancelOrderDao.updateWhseMerchQtyWhse(upWhseList);
			pcCancelOrderDao.deleteWhseTurnLine(paramsMap);
		}
	}
	
	//采购单
	@Override
	public void cancelPuchOrder (Map<String, Object> paramsMap)throws Exception{
		List<Map<String, Object>> puchOrderList=pcCancelOrderDao.searPuchOrderByIdSql(paramsMap);
		List<Map<String, Object>> puchOrderLineList=pcCancelOrderDao.searPuchOrderLineByIdSql(paramsMap);
		if(puchOrderList!=null){
			pcCancelOrderDao.deletePuchOrder(paramsMap);//删除采购单
		}
		if(puchOrderLineList!=null&&!puchOrderLineList.isEmpty()){
			List<Map<String, Object>> upPuchList=new ArrayList<Map<String,Object>>();
			for (Map<String, Object> map : puchOrderLineList) {
				String itemId=(String) map.get("item_id");//编号
				String merchId=(String) map.get("merch_id");///用户内码
				BigDecimal qtyOrd=BigDecimal.ZERO;
				if(map.get("qty_ord")!=null){//QTY_ORD
					qtyOrd=new BigDecimal(map.get("qty_ord")+"");
				}			 
				Map<String, Object> puchMap=new HashMap<String, Object>();
				puchMap.put("item_id",itemId);
				puchMap.put("merch_id", merchId);
				puchMap.put("qty_whse", new BigDecimal("0").subtract(qtyOrd));
				upPuchList.add(puchMap);
			}
			pcCancelOrderDao.updateWhseMerchQtyWhse(upPuchList);
			pcCancelOrderDao.deletePuchOrderLine(paramsMap);//删除采购当行
		}
	}
}

package com.ryx.social.retail.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.StringUtil;
import com.ryx.social.retail.dao.IStatisticsDao;
import com.ryx.social.retail.util.SQLUtil;

@Repository
public class StatisticsDaoImpl extends BaseDaoImpl implements IStatisticsDao {
	
	Logger LOG = LoggerFactory.getLogger(StatisticsDaoImpl.class);
	
	/**
	 * 查询系统日结日期
	 */
	@Override
	public String selectSystemSettlement() throws Exception {
		return this.selectStringBySqlQuery("SELECT SETTLEMENT_DATE FROM SYSTEM_SETTLEMENT");
	}
	
	@Override
	public String selectWarehousingDate() throws Exception {
		return this.selectStringBySqlQuery("SELECT WAREHOUSING_DATE FROM SYSTEM_SETTLEMENT");
	}
	
	/**
	 * 更新系统日结日期
	 */
	@Override
	public void updateSystemSettlement(String settlementDate) throws Exception {
		this.executeSQL("UPDATE SYSTEM_SETTLEMENT SET SETTLEMENT_DATE = ?", new Object[] {settlementDate});
	}

	@Override
	public void updateWarehousingDate(String warehousingDate) throws Exception {
		this.executeSQL("UPDATE SYSTEM_SETTLEMENT SET WAREHOUSING_DATE = ?", new Object[] {warehousingDate});
	}
	
	public static final String insertMerchDailySettlementSql = initInsertMerchDailySettlementSql();
	private static String initInsertMerchDailySettlementSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO MERCH_DAILY_SETTLEMENT (SETTLEMENT_ID, MERCH_ID, SETTLEMENT_DATE, PROFIT_AMOUNT, LOSS_AMOUNT,");
		sb.append(" SALE_AMOUNT, SALE_LOSS, SALE_COUNT, PROFIT_AMOUNT_ONLINE, LOSS_AMOUNT_ONLINE, SALE_AMOUNT_ONLINE, SALE_LOSS_ONLINE,");
		sb.append(" SALE_COUNT_ONLINE, PROFIT_AMOUNT_OFFLINE, LOSS_AMOUNT_OFFLINE, SALE_AMOUNT_OFFLINE, SALE_LOSS_OFFLINE, SALE_COUNT_OFFLINE,");
		sb.append(" PURCH_AMOUNT, PURCH_QUANTITY, PURCH_ITEM_COUNT, PURCH_COUNT, WHSE_AMOUNT, WHSE_QUANTITY, WHSE_ITEM_COUNT,");
		sb.append(" WHSE_TURN_PROFIT_AMOUNT, WHSE_TURN_LOSS_AMOUNT, WHSE_TURN_PROFIT_QUANTITY, WHSE_TURN_LOSS_QUANTITY, WHSE_TURN_COUNT,");
		sb.append(" RETURN_AMOUNT, RETURN_AMOUNT_ONLINE, RETURN_AMOUNT_OFFLINE)");
		sb.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		return sb.toString();
	}
	/**
	 * 插入商户日结数据
	 */
	@Override
	public void insertMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug(" = * = * = * = * = StatisticsDaoImpl insertMerchDailySettlement settlementParam = * = * = * = * = \r\n" + settlementParam);
		List<Object> paramObject = new ArrayList<Object>();
		paramObject.add(settlementParam.get("settlement_id"));
		paramObject.add(settlementParam.get("merch_id"));
		paramObject.add(settlementParam.get("settlement_date"));
		if(settlementParam.containsKey("profit_amount")) {
			paramObject.add(settlementParam.get("profit_amount"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("loss_amount")) {
			paramObject.add(settlementParam.get("loss_amount"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_amount")) {
			paramObject.add(settlementParam.get("sale_amount"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_loss")) {
			paramObject.add(settlementParam.get("sale_loss"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_count")) {
			paramObject.add(settlementParam.get("sale_count"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("profit_amount_online")) {
			paramObject.add(settlementParam.get("profit_amount_online"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("loss_amount_online")) {
			paramObject.add(settlementParam.get("loss_amount_online"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_amount_online")) {
			paramObject.add(settlementParam.get("sale_amount_online"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_loss_online")) {
			paramObject.add(settlementParam.get("sale_loss_online"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_count_online")) {
			paramObject.add(settlementParam.get("sale_count_online"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("profit_amount_offline")) {
			paramObject.add(settlementParam.get("profit_amount_offline"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("loss_amount_offline")) {
			paramObject.add(settlementParam.get("loss_amount_offline"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_amount_offline")) {
			paramObject.add(settlementParam.get("sale_amount_offline"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_loss_offline")) {
			paramObject.add(settlementParam.get("sale_loss_offline"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("sale_count_offline")) {
			paramObject.add(settlementParam.get("sale_count_offline"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("purch_amount")) {
			paramObject.add(settlementParam.get("purch_amount"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("purch_quantity")) {
			paramObject.add(settlementParam.get("purch_quantity"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("purch_item_count")) {
			paramObject.add(settlementParam.get("purch_item_count"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("purch_count")) {
			paramObject.add(settlementParam.get("purch_count"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_amount")) {
			paramObject.add(settlementParam.get("whse_amount"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_quantity")) {
			paramObject.add(settlementParam.get("whse_quantity"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_item_count")) {
			paramObject.add(settlementParam.get("whse_item_count"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_turn_profit")) {
			paramObject.add(settlementParam.get("whse_turn_profit"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_turn_loss")) {
			paramObject.add(settlementParam.get("whse_turn_loss"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_turn_profit_quantity")) {
			paramObject.add(settlementParam.get("whse_turn_profit_quantity"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_turn_loss_quantity")) {
			paramObject.add(settlementParam.get("whse_turn_loss_quantity"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("whse_turn_count")) {
			paramObject.add(settlementParam.get("whse_turn_count"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("return_amount")) {
			paramObject.add(settlementParam.get("return_amount"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("return_amount_online")) {
			paramObject.add(settlementParam.get("return_amount_online"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		if(settlementParam.containsKey("return_amount_offline")) {
			paramObject.add(settlementParam.get("return_amount_offline"));
		} else {
			paramObject.add(BigDecimal.ZERO);
		}
		this.executeSQL(insertMerchDailySettlementSql, paramObject.toArray());
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------
	
	public void updateMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl updateMerchDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder("UPDATE MERCH_DAILY_SETTLEMENT SET");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLEqual(settlementParam, sqlBuilder, paramList, "merch_id", "settlement_date", "profit_amount", 
				"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online", 
				"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline", 
				"sale_count_offline", "purch_amount", "purch_quantity", "purch_item_count", "purch_count", "whse_amount", "whse_quantity", 
				"whse_item_count", "whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", 
				"whse_turn_count", "return_amount", "return_amount_online", "return_amount_offline");
		sqlBuilder.append(" WHERE");
		SQLUtil.initSQLEqual(settlementParam, sqlBuilder, paramList, "settlement_id");
		this.executeSQL(sqlBuilder.toString(), paramList.toArray());
	}
	
	public void updateMerchItemDailySettlement(List<Map<String, Object>> settlementParamList) throws Exception {
		LOG.debug("StatisticsDaoImpl updateMerchItemDailySettlement settlementParamList: " + settlementParamList);
		StringBuilder sqlBuilder = new StringBuilder("UPDATE MERCH_ITEM_DAILY_SETTLEMENT SET");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		int index= 0;
		for(Map<String, Object> settlementParam : settlementParamList) {
			List<Object> paramArray = new ArrayList<Object>();
			if(index++==0) {
				SQLUtil.initSQLEqual(settlementParam, sqlBuilder, paramArray, "merch_id", "settlement_date", "profit_amount", 
						"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online", 
						"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline", 
						"sale_count_offline", "purch_amount", "purch_quantity", "purch_item_count", "purch_count", "whse_amount", "whse_quantity", 
						"whse_item_count", "whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", 
						"whse_turn_count", "return_amount", "return_amount_online", "return_amount_offline");
				sqlBuilder.append(" WHERE");
				SQLUtil.initSQLEqual(settlementParam, sqlBuilder, paramArray, "settlement_id");
			} else {
				SQLUtil.initSQLEqual(settlementParam, sqlBuilder, paramArray, "merch_id", "settlement_date", "profit_amount", 
						"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online", 
						"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline", 
						"sale_count_offline", "purch_amount", "purch_quantity", "purch_item_count", "purch_count", "whse_amount", "whse_quantity", 
						"whse_item_count", "whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", 
						"whse_turn_count", "return_amount", "return_amount_online", "return_amount_offline", "settlement_id");
			}
			paramArrayList.add(paramArray.toArray());
		}
		this.executeBatchSQL(sqlBuilder.toString(), paramArrayList);
	}
	
	public void insertTemporaryMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl insertTemporaryMerchDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO TEMPORARY_MERCH_DAILY_SETTLEMENT");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLInsertValues(settlementParam, sqlBuilder, paramList, "settlement_id", "merch_id", "settlement_date", "profit_amount", 
				"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online", 
				"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline", 
				"sale_count_offline", "purch_amount", "purch_quantity", "purch_item_count", "purch_count", "whse_amount", "whse_quantity", 
				"whse_item_count", "whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", 
				"whse_turn_count", "return_amount", "return_amount_online", "return_amount_offline");
		this.executeSQL(sqlBuilder.toString(), paramList.toArray());
	}
	
	public void deleteTemporaryMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl deleteTemporaryMerchDailySettlement settlementParam: " + settlementParam);
		this.executeSQL("DELETE FROM TEMPORARY_MERCH_DAILY_SETTLEMENT WHERE SETTLEMENT_ID = ?", new Object[] {settlementParam.get("settlement_id")});
	}
	
	public void insertTemporaryMerchItemDailySettlement(List<Map<String, Object>> settlementParamList) throws Exception {
		LOG.debug("StatisticsDaoImpl insertTemporaryMerchItemDailySettlement settlementParamList: " + settlementParamList);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO TEMPORARY_MERCH_ITEM_DAILY_SETTLEMENT");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		int index = 0;
		for(Map<String, Object> settlementParam : settlementParamList) {
			List<Object> paramArray = new ArrayList<Object>();
			if(index++==0) {
				SQLUtil.initSQLInsertValues(settlementParam, sqlBuilder, paramArray, "settlement_id", "merch_id", "settlement_date", "profit_amount", 
						"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online", 
						"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline", 
						"sale_count_offline", "purch_amount", "purch_quantity", "purch_item_count", "purch_count", "whse_amount", "whse_quantity", 
						"whse_item_count", "whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", 
						"whse_turn_count", "return_amount", "return_amount_online", "return_amount_offline");
			} else {
				SQLUtil.initSQLEqual(settlementParam, paramArray, "settlement_id", "merch_id", "settlement_date", "profit_amount", 
						"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online", 
						"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline", 
						"sale_count_offline", "purch_amount", "purch_quantity", "purch_item_count", "purch_count", "whse_amount", "whse_quantity", 
						"whse_item_count", "whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", 
						"whse_turn_count", "return_amount", "return_amount_online", "return_amount_offline");
			}
			paramArrayList.add(paramArray.toArray());
		}
		this.executeBatchSQL(sqlBuilder.toString(), paramArrayList);
	}
	
	public void deleteTemporaryMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl deleteTemporaryMerchItemDailySettlement settlementParam: " + settlementParam);
		this.executeSQL("DELETE FROM TEMPORARY_MERCH_ITEM_DAILY_SETTLEMENT WHERE MERCH_ID = ?", new Object[] {settlementParam.get("merch_id")});
	}
	
	@Override
	public List<Map<String, Object>> resetMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl resetMerchDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder("select bmi.item_id,");
		sqlBuilder.append(" case when mids.item_bar is null then bmi.item_bar else mids.item_bar end item_bar,");
		sqlBuilder.append(" case when mids.item_name is null then bmi.item_name else mids.item_name end item_name,");
		sqlBuilder.append(" case when mids.unit_name is null then bmi.unit_name else mids.unit_name end unit_name,");
		sqlBuilder.append(" case when mids.item_kind_id is null then bmi.item_kind_id else mids.item_kind_id end item_kind_id,");
		sqlBuilder.append(" case when mids.cost is null then bmi.cost else mids.cost end cost,");
		sqlBuilder.append(" case when mids.pri1 is null then bmi.pri1 else mids.pri1 end pri1,");
		sqlBuilder.append(" case when mids.pri4 is null then bmi.pri4 else mids.pri4 end pri4,");
		sqlBuilder.append(" item_profit, item_sale_amount, item_sale_quantity, sale_id, sale_amount, sale_profit, sale_loss, sale_type,");
		sqlBuilder.append(" item_loss, item_return_amount, item_return_quantity, return_id, return_amount, return_loss, return_type, item_purch_amount, item_purch_quantity,");
		sqlBuilder.append(" purch_id, purch_amount, item_turn_amount, item_turn_quantity, turn_id, whse_turn_profit_amount, whse_turn_loss_amount, whse_turn_profit_quantity, whse_turn_loss_quantity, whse_quantity, whse_adjusted_quantity, whse_warn_quantity");
		sqlBuilder.append(" from base_merch_item bmi, (select a.item_id, a.whse_quantity, (a.return_quantity+a.purch_quantity+a.whse_turn_pl_quantity-a.sale_quantity) whse_adjusted_quantity, a.whse_warn_quantity, b.item_bar, b.item_name, b.unit_name, b.item_kind_id, b.cost, b.pri1, b.pri4");
		sqlBuilder.append(" from merch_item_daily_settlement a, (select * from merch_item_daily_settlement where merch_id=? and settlement_date=?) b");
		sqlBuilder.append(" where a.merch_id=b.merch_id(+) and a.item_id=b.item_id(+) and a.merch_id=? and a.settlement_date=?) mids,");
		sqlBuilder.append(" (select sol.item_id, NVL(SUM(PROFIT*QTY_ORD*UNIT_RATIO),0) item_profit, NVL(SUM(AMT_ORD),0) item_sale_amount, NVL(SUM(QTY_ORD*UNIT_RATIO),0) item_sale_quantity,");
		sqlBuilder.append(" so.order_id sale_id, so.amtys_ord_total sale_amount, so.amt_ord_profit sale_profit, so.amt_ord_loss sale_loss, so.order_type sale_type");
		sqlBuilder.append(" from sale_order so, sale_order_line sol ");
		sqlBuilder.append(" where so.order_id=sol.order_id and so.merch_id=? and so.order_date=? and so.status='03' and (so.note!='L1' or so.note is null)");
		sqlBuilder.append(" group by sol.item_id, so.order_id, so.amtys_ord_total, so.amt_ord_profit, so.amt_ord_loss, so.order_type) so,");
		sqlBuilder.append(" (select sol.item_id, NVL(SUM(PROFIT*QTY_ORD*UNIT_RATIO),0) item_loss, NVL(SUM(AMT_ORD),0) item_return_amount, NVL(SUM(QTY_ORD*UNIT_RATIO),0) item_return_quantity,");
		sqlBuilder.append(" ro.return_order_id return_id, ro.amt_return_total return_amount, ro.amt_return_loss return_loss, ro.order_type return_type");
		sqlBuilder.append(" from return_order ro, return_order_line sol");
		sqlBuilder.append(" where ro.return_order_id=sol.order_id and ro.merch_id=? and ro.return_order_date=?");
		sqlBuilder.append(" group by sol.item_id, ro.return_order_id, ro.amt_return_total, ro.amt_return_loss, ro.order_type) ro,");
		sqlBuilder.append(" (select pol.item_id, NVL(SUM(AMT_ORD),0) item_purch_amount, NVL(SUM(QTY_ORD*UNIT_RATIO),0) item_purch_quantity, ");
		sqlBuilder.append(" po.order_id purch_id, po.amt_purch_total purch_amount ");
		sqlBuilder.append(" from purch_order po, purch_order_line pol");
		sqlBuilder.append(" where po.order_id=pol.order_id and po.merch_id=? and po.voucher_date=?");
		sqlBuilder.append(" group by pol.item_id, po.order_id, po.amt_purch_total, po.qty_purch_total) po,");
		sqlBuilder.append(" (select wtl.item_id, NVL(SUM(AMT_PL),0) item_turn_amount, NVL(SUM(QTY_PL),0) item_turn_quantity, ");
		sqlBuilder.append(" wt.turn_id, wt.amt_profit whse_turn_profit_amount, wt.amt_loss whse_turn_loss_amount, wt.qty_profit whse_turn_profit_quantity, wt.qty_loss whse_turn_loss_quantity");
		sqlBuilder.append(" from whse_turn wt, whse_turn_line wtl");
		sqlBuilder.append(" where wt.turn_id=wtl.turn_id and wt.merch_id=? and wt.turn_date=?");
		sqlBuilder.append(" group by wtl.item_id, wt.turn_id, wt.amt_profit, wt.amt_loss, wt.qty_profit, wt.qty_loss) wt");
		sqlBuilder.append(" where bmi.merch_id=? and bmi.item_id=mids.item_id(+) and bmi.item_id=so.item_id(+) and bmi.item_id=ro.item_id(+) ");
		sqlBuilder.append(" and bmi.item_id=po.item_id(+) and bmi.item_id=wt.item_id(+)");
		List<Object> paramList = new ArrayList<Object>();
		String merchId = MapUtil.getString(settlementParam, "merch_id");
		String settlementDate = MapUtil.getString(settlementParam, "settlement_date");
		String nextDay = MapUtil.getString(settlementParam, "next_date", DateUtil.getPreviousDay(settlementDate, 1));
		// 当天日结参数, 提供当天的商品信息
		paramList.add(merchId);
		paramList.add(settlementDate);
		// 上一天(正向日结是昨天,反向日结是明天)日结参数, 提供上一天的库存量和库存调整量
		paramList.add(merchId);
		paramList.add(nextDay);
		// 当天销售参数
		paramList.add(merchId);
		paramList.add(settlementDate);
		// 当天退货参数
		paramList.add(merchId);
		paramList.add(settlementDate);
		// 当天采购参数
		paramList.add(merchId);
		paramList.add(settlementDate);
		// 当天盘点参数
		paramList.add(merchId);
		paramList.add(settlementDate);
		// 商品信息参数, 提供商户所有商品id
		paramList.add(merchId);
		SQLUtil.initSQLIn(settlementParam, sqlBuilder, paramList, "bmi.item_id");
		return this.selectBySqlQuery(sqlBuilder.toString(), paramList.toArray());
	}
	
	@Override
	public List<Map<String, Object>> resetMerchTodaySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl resetMerchDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder("select bmi.item_id, item_bar, item_name, unit_name, item_kind_id, cost, pri1, pri4,");
		sqlBuilder.append(" item_profit, item_sale_amount, item_sale_quantity, sale_id, sale_amount, sale_profit, sale_loss, sale_type,");
		sqlBuilder.append(" item_loss, item_return_amount, item_return_quantity, return_id, return_amount, return_loss, return_type, item_purch_amount, item_purch_quantity,");
		sqlBuilder.append(" purch_id, purch_amount, item_turn_amount, item_turn_quantity, turn_id, whse_turn_profit_amount, whse_turn_loss_amount, whse_turn_profit_quantity, whse_turn_loss_quantity, whse_quantity, whse_warn_quantity");
		sqlBuilder.append(" from (select a.item_id, item_bar, item_name, unit_name, item_kind_id, cost, pri1, pri4, b.qty_whse whse_quantity, b.qty_whse_warn whse_warn_quantity from base_merch_item a, whse_merch b where a.merch_id=b.merch_id and a.item_id=b.item_id and a.merch_id=?) bmi,");
		sqlBuilder.append(" (select sol.item_id, NVL(SUM(PROFIT*QTY_ORD*UNIT_RATIO),0) item_profit, NVL(SUM(AMT_ORD),0) item_sale_amount, NVL(SUM(QTY_ORD*UNIT_RATIO),0) item_sale_quantity,");
		sqlBuilder.append(" so.order_id sale_id, so.amtys_ord_total sale_amount, so.amt_ord_profit sale_profit, so.amt_ord_loss sale_loss, so.order_type sale_type");
		sqlBuilder.append(" from sale_order so, sale_order_line sol ");
		sqlBuilder.append(" where so.order_id=sol.order_id and so.merch_id=? and so.order_date=? and so.status='03' and (so.note!='L1' or so.note is null)");
		sqlBuilder.append(" group by sol.item_id, so.order_id, so.amtys_ord_total, so.amt_ord_profit, so.amt_ord_loss, so.order_type) so,");
		sqlBuilder.append(" (select sol.item_id, NVL(SUM(PROFIT*QTY_ORD*UNIT_RATIO),0) item_loss, NVL(SUM(AMT_ORD),0) item_return_amount, NVL(SUM(QTY_ORD*UNIT_RATIO),0) item_return_quantity,");
		sqlBuilder.append(" ro.return_order_id return_id, ro.amt_return_total return_amount, ro.amt_return_loss return_loss, ro.order_type return_type");
		sqlBuilder.append(" from return_order ro, return_order_line sol");
		sqlBuilder.append(" where ro.return_order_id=sol.order_id and ro.merch_id=? and ro.return_order_date=?");
		sqlBuilder.append(" group by sol.item_id, ro.return_order_id, ro.amt_return_total, ro.amt_return_loss, ro.order_type) ro,");
		sqlBuilder.append(" (select pol.item_id, NVL(SUM(AMT_ORD),0) item_purch_amount, NVL(SUM(QTY_ORD*UNIT_RATIO),0) item_purch_quantity, ");
		sqlBuilder.append(" po.order_id purch_id, po.amt_purch_total purch_amount ");
		sqlBuilder.append(" from purch_order po, purch_order_line pol");
		sqlBuilder.append(" where po.order_id=pol.order_id and po.merch_id=? and po.voucher_date=?");
		sqlBuilder.append(" group by pol.item_id, po.order_id, po.amt_purch_total, po.qty_purch_total) po,");
		sqlBuilder.append(" (select wtl.item_id, NVL(SUM(AMT_PL),0) item_turn_amount, NVL(SUM(QTY_PL),0) item_turn_quantity, ");
		sqlBuilder.append(" wt.turn_id, wt.amt_profit whse_turn_profit_amount, wt.amt_loss whse_turn_loss_amount, wt.qty_profit whse_turn_profit_quantity, wt.qty_loss whse_turn_loss_quantity");
		sqlBuilder.append(" from whse_turn wt, whse_turn_line wtl");
		sqlBuilder.append(" where wt.turn_id=wtl.turn_id and wt.merch_id=? and wt.turn_date=?");
		sqlBuilder.append(" group by wtl.item_id, wt.turn_id, wt.amt_profit, wt.amt_loss, wt.qty_profit, wt.qty_loss) wt");
		sqlBuilder.append(" where bmi.item_id=so.item_id(+) and bmi.item_id=ro.item_id(+) and bmi.item_id=po.item_id(+) and bmi.item_id=wt.item_id(+) ");
		List<Object> paramList = new ArrayList<Object>();
		String merchId = MapUtil.getString(settlementParam, "merch_id");
		String settlementDate = MapUtil.getString(settlementParam, "settlement_date");
		paramList.add(merchId);
		paramList.add(merchId);
		paramList.add(settlementDate);
		paramList.add(merchId);
		paramList.add(settlementDate);
		paramList.add(merchId);
		paramList.add(settlementDate);
		paramList.add(merchId);
		paramList.add(settlementDate);
		SQLUtil.initSQLIn(settlementParam, sqlBuilder, paramList, "bmi.item_id");
		return this.selectBySqlQuery(sqlBuilder.toString(), paramList.toArray());
	}
	
	@Override
	public void tempMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl tempMerchDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO MERCH_TEMP_SETTLEMENT");
		List<Object> paramList = new ArrayList<Object>();
		SQLUtil.initSQLInsertValues(settlementParam, sqlBuilder, paramList, "settlement_id", "merch_id", "settlement_date", "profit_amount",
				"loss_amount", "sale_amount", "sale_loss", "sale_count", "profit_amount_online", "loss_amount_online", "sale_amount_online",
				"sale_loss_online", "sale_count_online", "profit_amount_offline", "loss_amount_offline", "sale_amount_offline", "sale_loss_offline",
				"sale_count_offline", "purch_amount", "purch_quantity", "purch_count", "whse_amount", "whse_quantity", "whse_item_count",
				"whse_turn_profit_amount", "whse_turn_loss_amount", "whse_turn_profit_quantity", "whse_turn_loss_quantity", "whse_turn_count",
				"return_amount", "return_amount_online", "return_amount_offline", "purch_item_count");
		this.executeSQL(sqlBuilder.toString(), paramList.toArray());
	}
	
	@Override
	public void tempMerchItemDailySettlement(List<Map<String, Object>> settlementParamList) throws Exception {
		LOG.debug("StatisticsDaoImpl tempMerchItemDailySettlement settlementParamList: " + settlementParamList);
		StringBuilder sqlBuilder = new StringBuilder("INSERT INTO MERCH_ITEM_TEMP_SETTLEMENT");
		List<Object[]> paramArrayList = new ArrayList<Object[]>();
		int index = 0;
		for(Map<String, Object> settlementParam : settlementParamList) {
			List<Object> paramList = new ArrayList<Object>();
			if(index++==0) {
				SQLUtil.initSQLInsertValues(settlementParam, sqlBuilder, paramList, "settlement_id", "merch_id", "settlement_date", "item_id",
						"item_bar", "item_name", "item_kind_id", "unit_name", "cost", "pri1","pri2", "pri4", "discount", "profit_amount", "loss_amount",
						"sale_amount", "sale_quantity", "profit_amount_online","loss_amount_online", "sale_amount_online", "sale_quantity_online",
						"profit_amount_offline", "loss_amount_offline","sale_amount_offline", "sale_quantity_offline", "purch_amount", "purch_quantity",
						"whse_amount", "whse_quantity", "whse_turn_pl_amount", "whse_turn_pl_quantity", "return_amount", "return_amount_online",
						"return_amount_offline", "return_quantity", "return_quantity_online", "return_quantity_offline", "whse_warn_quantity");
			} else {
				SQLUtil.initSQLInsertValues(settlementParam, paramList, "settlement_id", "merch_id", "settlement_date", "item_id",
						"item_bar", "item_name", "item_kind_id", "unit_name", "cost", "pri1","pri2", "pri4", "discount", "profit_amount", "loss_amount",
						"sale_amount", "sale_quantity", "profit_amount_online","loss_amount_online", "sale_amount_online", "sale_quantity_online",
						"profit_amount_offline", "loss_amount_offline","sale_amount_offline", "sale_quantity_offline", "purch_amount", "purch_quantity",
						"whse_amount", "whse_quantity", "whse_turn_pl_amount", "whse_turn_pl_quantity", "return_amount", "return_amount_online",
						"return_amount_offline", "return_quantity", "return_quantity_online", "return_quantity_offline", "whse_warn_quantity");
			}
			paramArrayList.add(paramList.toArray());
		}
		this.executeBatchSQL(sqlBuilder.toString(), paramArrayList);
	}
	
	@Override
	public void cleanMerchTempSettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl cleanMerchDailySettlement settlementParam: " + settlementParam);
		this.executeSQL("DELETE FROM MERCH_TEMP_SETTLEMENT WHERE MERCH_ID=?", new Object[] {settlementParam.get("merch_id")});
	}
	
	@Override
	public void cleanMerchItemTempSettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl cleanMerchItemTempSettlement settlementParam: " + settlementParam);
		this.executeSQL("DELETE FROM MERCH_ITEM_TEMP_SETTLEMENT WHERE MERCH_ID=?", new Object[] {settlementParam.get("merch_id")});
	}
	
	@Override
	public void refreshMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl backupMerchDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("MERGE INTO MERCH_DAILY_SETTLEMENT A USING(SELECT * FROM MERCH_TEMP_SETTLEMENT WHERE MERCH_ID=? AND SETTLEMENT_DATE=?) B");
		sqlBuilder.append(" ON(A.MERCH_ID=B.MERCH_ID AND A.SETTLEMENT_DATE=B.SETTLEMENT_DATE) WHEN NOT MATCHED THEN");
		sqlBuilder.append(" INSERT (A.SETTLEMENT_ID, A.SETTLEMENT_DATE, A.MERCH_ID, A.PROFIT_AMOUNT, A.LOSS_AMOUNT, A.SALE_AMOUNT, A.SALE_LOSS, A.SALE_COUNT, A.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE, A.SALE_LOSS_ONLINE, A.SALE_COUNT_ONLINE, A.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE, A.SALE_LOSS_OFFLINE, A.SALE_COUNT_OFFLINE, A.PURCH_AMOUNT, A.PURCH_QUANTITY, A.PURCH_COUNT, A.WHSE_AMOUNT, A.WHSE_QUANTITY, A.WHSE_ITEM_COUNT, A.WHSE_TURN_PROFIT_AMOUNT, A.WHSE_TURN_LOSS_AMOUNT, A.WHSE_TURN_PROFIT_QUANTITY, A.WHSE_TURN_LOSS_QUANTITY, A.WHSE_TURN_COUNT, A.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE, A.PURCH_ITEM_COUNT)");
		sqlBuilder.append(" VALUES (B.SETTLEMENT_ID, B.SETTLEMENT_DATE, B.MERCH_ID, B.PROFIT_AMOUNT, B.LOSS_AMOUNT, B.SALE_AMOUNT, B.SALE_LOSS, B.SALE_COUNT, B.PROFIT_AMOUNT_ONLINE, B.LOSS_AMOUNT_ONLINE, B.SALE_AMOUNT_ONLINE, B.SALE_LOSS_ONLINE, B.SALE_COUNT_ONLINE, B.PROFIT_AMOUNT_OFFLINE, B.LOSS_AMOUNT_OFFLINE, B.SALE_AMOUNT_OFFLINE, B.SALE_LOSS_OFFLINE, B.SALE_COUNT_OFFLINE, B.PURCH_AMOUNT, B.PURCH_QUANTITY, B.PURCH_COUNT, B.WHSE_AMOUNT, B.WHSE_QUANTITY, B.WHSE_ITEM_COUNT, B.WHSE_TURN_PROFIT_AMOUNT, B.WHSE_TURN_LOSS_AMOUNT, B.WHSE_TURN_PROFIT_QUANTITY, B.WHSE_TURN_LOSS_QUANTITY, B.WHSE_TURN_COUNT, B.RETURN_AMOUNT, B.RETURN_AMOUNT_ONLINE, B.RETURN_AMOUNT_OFFLINE, B.PURCH_ITEM_COUNT)");
		sqlBuilder.append(" WHEN MATCHED THEN");
		sqlBuilder.append(" UPDATE SET A.PROFIT_AMOUNT=B.PROFIT_AMOUNT, A.LOSS_AMOUNT=B.LOSS_AMOUNT, A.SALE_AMOUNT=B.SALE_AMOUNT, A.SALE_LOSS=B.SALE_LOSS, A.SALE_COUNT=B.SALE_COUNT, A.PROFIT_AMOUNT_ONLINE=B.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE=B.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE=B.SALE_AMOUNT_ONLINE, A.SALE_LOSS_ONLINE=B.SALE_LOSS_ONLINE, A.SALE_COUNT_ONLINE=B.SALE_COUNT_ONLINE, A.PROFIT_AMOUNT_OFFLINE=B.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE=B.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE=B.SALE_AMOUNT_OFFLINE, A.SALE_LOSS_OFFLINE=B.SALE_LOSS_OFFLINE, A.SALE_COUNT_OFFLINE=B.SALE_COUNT_OFFLINE, A.PURCH_AMOUNT=B.PURCH_AMOUNT, A.PURCH_QUANTITY=B.PURCH_QUANTITY, A.PURCH_COUNT=B.PURCH_COUNT, A.WHSE_AMOUNT=B.WHSE_AMOUNT, A.WHSE_QUANTITY=B.WHSE_QUANTITY, A.WHSE_ITEM_COUNT=B.WHSE_ITEM_COUNT, A.WHSE_TURN_PROFIT_AMOUNT=B.WHSE_TURN_PROFIT_AMOUNT, A.WHSE_TURN_LOSS_AMOUNT=B.WHSE_TURN_LOSS_AMOUNT, A.WHSE_TURN_PROFIT_QUANTITY=B.WHSE_TURN_PROFIT_QUANTITY, A.WHSE_TURN_LOSS_QUANTITY=B.WHSE_TURN_LOSS_QUANTITY, A.WHSE_TURN_COUNT=B.WHSE_TURN_COUNT, A.RETURN_AMOUNT=B.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE=B.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE=B.RETURN_AMOUNT_OFFLINE, A.PURCH_ITEM_COUNT=B.PURCH_ITEM_COUNT");
		this.executeSQL(sqlBuilder.toString(), new Object[] {settlementParam.get("merch_id"), settlementParam.get("settlement_date")});
	}
	
	@Override
	public void refreshMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl refreshMerchItemDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("MERGE INTO MERCH_ITEM_DAILY_SETTLEMENT A USING(");
		sqlBuilder.append(" SELECT * FROM MERCH_ITEM_TEMP_SETTLEMENT WHERE MERCH_ID=? AND SETTLEMENT_DATE=?) B");
		sqlBuilder.append(" ON(A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID AND A.SETTLEMENT_DATE=B.SETTLEMENT_DATE)");
		sqlBuilder.append(" WHEN NOT MATCHED THEN");
		sqlBuilder.append(" INSERT (A.SETTLEMENT_ID, A.MERCH_ID, A.ITEM_ID, A.SETTLEMENT_DATE, A.ITEM_BAR, A.ITEM_NAME, A.ITEM_KIND_ID, A.UNIT_NAME, A.COST, A.PRI1, A.PRI2, A.PRI4, A.DISCOUNT, A.PROFIT_AMOUNT, A.LOSS_AMOUNT, A.SALE_AMOUNT, A.SALE_QUANTITY, A.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE, A.SALE_QUANTITY_ONLINE, A.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE, A.SALE_QUANTITY_OFFLINE, A.PURCH_AMOUNT, A.PURCH_QUANTITY, A.WHSE_AMOUNT, A.WHSE_QUANTITY, A.WHSE_TURN_PL_AMOUNT, A.WHSE_TURN_PL_QUANTITY, A.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE, A.RETURN_QUANTITY, A.RETURN_QUANTITY_ONLINE, A.RETURN_QUANTITY_OFFLINE, A.WHSE_WARN_QUANTITY)");
		sqlBuilder.append(" VALUES (B.SETTLEMENT_ID, B.MERCH_ID, B.ITEM_ID, B.SETTLEMENT_DATE, B.ITEM_BAR, B.ITEM_NAME, B.ITEM_KIND_ID, B.UNIT_NAME, B.COST, B.PRI1, B.PRI2, B.PRI4, B.DISCOUNT, B.PROFIT_AMOUNT, B.LOSS_AMOUNT, B.SALE_AMOUNT, B.SALE_QUANTITY, B.PROFIT_AMOUNT_ONLINE, B.LOSS_AMOUNT_ONLINE, B.SALE_AMOUNT_ONLINE, B.SALE_QUANTITY_ONLINE, B.PROFIT_AMOUNT_OFFLINE, B.LOSS_AMOUNT_OFFLINE, B.SALE_AMOUNT_OFFLINE, B.SALE_QUANTITY_OFFLINE, B.PURCH_AMOUNT, B.PURCH_QUANTITY, B.WHSE_AMOUNT, B.WHSE_QUANTITY, B.WHSE_TURN_PL_AMOUNT, B.WHSE_TURN_PL_QUANTITY, B.RETURN_AMOUNT, B.RETURN_AMOUNT_ONLINE, B.RETURN_AMOUNT_OFFLINE, B.RETURN_QUANTITY, B.RETURN_QUANTITY_ONLINE, B.RETURN_QUANTITY_OFFLINE, B.WHSE_WARN_QUANTITY)");
		sqlBuilder.append(" WHEN MATCHED THEN");
		sqlBuilder.append(" UPDATE SET A.ITEM_BAR=B.ITEM_BAR, A.ITEM_NAME=B.ITEM_NAME, A.ITEM_KIND_ID=B.ITEM_KIND_ID, A.UNIT_NAME=B.UNIT_NAME, A.COST=B.COST, A.PRI1=B.PRI1, A.PRI2=B.PRI2, A.PRI4=B.PRI4, A.DISCOUNT=B.DISCOUNT, A.PROFIT_AMOUNT=B.PROFIT_AMOUNT, A.LOSS_AMOUNT=B.LOSS_AMOUNT, A.SALE_AMOUNT=B.SALE_AMOUNT, A.SALE_QUANTITY=B.SALE_QUANTITY, A.PROFIT_AMOUNT_ONLINE=B.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE=B.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE=B.SALE_AMOUNT_ONLINE, A.SALE_QUANTITY_ONLINE=B.SALE_QUANTITY_ONLINE, A.PROFIT_AMOUNT_OFFLINE=B.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE=B.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE=B.SALE_AMOUNT_OFFLINE, A.SALE_QUANTITY_OFFLINE=B.SALE_QUANTITY_OFFLINE, A.PURCH_AMOUNT=B.PURCH_AMOUNT, A.PURCH_QUANTITY=B.PURCH_QUANTITY, A.WHSE_AMOUNT=B.WHSE_AMOUNT, A.WHSE_QUANTITY=B.WHSE_QUANTITY, A.WHSE_TURN_PL_AMOUNT=B.WHSE_TURN_PL_AMOUNT, A.WHSE_TURN_PL_QUANTITY=B.WHSE_TURN_PL_QUANTITY, A.RETURN_AMOUNT=B.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE=B.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE=B.RETURN_AMOUNT_OFFLINE, A.RETURN_QUANTITY=B.RETURN_QUANTITY, A.RETURN_QUANTITY_ONLINE=B.RETURN_QUANTITY_ONLINE, A.RETURN_QUANTITY_OFFLINE=B.RETURN_QUANTITY_OFFLINE, A.WHSE_WARN_QUANTITY=B.WHSE_WARN_QUANTITY");
		this.executeSQL(sqlBuilder.toString(), new Object[] {settlementParam.get("merch_id"), settlementParam.get("settlement_date")});
	}
	
	@Override
	public void backupMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl backupMerchDailySettlement settlementParam: " + settlementParam);//
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("MERGE INTO MERCH_BACKUP_SETTLEMENT A USING(SELECT * FROM MERCH_DAILY_SETTLEMENT WHERE MERCH_ID=? AND SETTLEMENT_DATE=?) B");
		sqlBuilder.append(" ON(A.MERCH_ID=B.MERCH_ID AND A.SETTLEMENT_DATE=B.SETTLEMENT_DATE) WHEN NOT MATCHED THEN");
		sqlBuilder.append(" INSERT (A.SETTLEMENT_ID, A.SETTLEMENT_DATE, A.MERCH_ID, A.PROFIT_AMOUNT, A.LOSS_AMOUNT, A.SALE_AMOUNT, A.SALE_LOSS, A.SALE_COUNT, A.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE, A.SALE_LOSS_ONLINE, A.SALE_COUNT_ONLINE, A.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE, A.SALE_LOSS_OFFLINE, A.SALE_COUNT_OFFLINE, A.PURCH_AMOUNT, A.PURCH_QUANTITY, A.PURCH_COUNT, A.WHSE_AMOUNT, A.WHSE_QUANTITY, A.WHSE_ITEM_COUNT, A.WHSE_TURN_PROFIT_AMOUNT, A.WHSE_TURN_LOSS_AMOUNT, A.WHSE_TURN_PROFIT_QUANTITY, A.WHSE_TURN_LOSS_QUANTITY, A.WHSE_TURN_COUNT, A.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE, A.PURCH_ITEM_COUNT)");
		sqlBuilder.append(" VALUES (B.SETTLEMENT_ID, B.SETTLEMENT_DATE, B.MERCH_ID, B.PROFIT_AMOUNT, B.LOSS_AMOUNT, B.SALE_AMOUNT, B.SALE_LOSS, B.SALE_COUNT, B.PROFIT_AMOUNT_ONLINE, B.LOSS_AMOUNT_ONLINE, B.SALE_AMOUNT_ONLINE, B.SALE_LOSS_ONLINE, B.SALE_COUNT_ONLINE, B.PROFIT_AMOUNT_OFFLINE, B.LOSS_AMOUNT_OFFLINE, B.SALE_AMOUNT_OFFLINE, B.SALE_LOSS_OFFLINE, B.SALE_COUNT_OFFLINE, B.PURCH_AMOUNT, B.PURCH_QUANTITY, B.PURCH_COUNT, B.WHSE_AMOUNT, B.WHSE_QUANTITY, B.WHSE_ITEM_COUNT, B.WHSE_TURN_PROFIT_AMOUNT, B.WHSE_TURN_LOSS_AMOUNT, B.WHSE_TURN_PROFIT_QUANTITY, B.WHSE_TURN_LOSS_QUANTITY, B.WHSE_TURN_COUNT, B.RETURN_AMOUNT, B.RETURN_AMOUNT_ONLINE, B.RETURN_AMOUNT_OFFLINE, B.PURCH_ITEM_COUNT)");
		sqlBuilder.append(" WHEN MATCHED THEN");
		sqlBuilder.append(" UPDATE SET A.SETTLEMENT_ID=B.SETTLEMENT_ID, A.PROFIT_AMOUNT=B.PROFIT_AMOUNT, A.LOSS_AMOUNT=B.LOSS_AMOUNT, A.SALE_AMOUNT=B.SALE_AMOUNT, A.SALE_LOSS=B.SALE_LOSS, A.SALE_COUNT=B.SALE_COUNT, A.PROFIT_AMOUNT_ONLINE=B.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE=B.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE=B.SALE_AMOUNT_ONLINE, A.SALE_LOSS_ONLINE=B.SALE_LOSS_ONLINE, A.SALE_COUNT_ONLINE=B.SALE_COUNT_ONLINE, A.PROFIT_AMOUNT_OFFLINE=B.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE=B.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE=B.SALE_AMOUNT_OFFLINE, A.SALE_LOSS_OFFLINE=B.SALE_LOSS_OFFLINE, A.SALE_COUNT_OFFLINE=B.SALE_COUNT_OFFLINE, A.PURCH_AMOUNT=B.PURCH_AMOUNT, A.PURCH_QUANTITY=B.PURCH_QUANTITY, A.PURCH_COUNT=B.PURCH_COUNT, A.WHSE_AMOUNT=B.WHSE_AMOUNT, A.WHSE_QUANTITY=B.WHSE_QUANTITY, A.WHSE_ITEM_COUNT=B.WHSE_ITEM_COUNT, A.WHSE_TURN_PROFIT_AMOUNT=B.WHSE_TURN_PROFIT_AMOUNT, A.WHSE_TURN_LOSS_AMOUNT=B.WHSE_TURN_LOSS_AMOUNT, A.WHSE_TURN_PROFIT_QUANTITY=B.WHSE_TURN_PROFIT_QUANTITY, A.WHSE_TURN_LOSS_QUANTITY=B.WHSE_TURN_LOSS_QUANTITY, A.WHSE_TURN_COUNT=B.WHSE_TURN_COUNT, A.RETURN_AMOUNT=B.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE=B.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE=B.RETURN_AMOUNT_OFFLINE, A.PURCH_ITEM_COUNT=B.PURCH_ITEM_COUNT");
		this.executeSQL(sqlBuilder.toString(), new Object[] {settlementParam.get("merch_id"), settlementParam.get("settlement_date")});
	}
	
	@Override
	public void backupMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl backupMerchItemDailySettlement settlementParam: " + settlementParam);
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("MERGE INTO MERCH_ITEM_BACKUP_SETTLEMENT A USING(");
		sqlBuilder.append(" SELECT * FROM MERCH_ITEM_DAILY_SETTLEMENT WHERE MERCH_ID=? AND SETTLEMENT_DATE=?) B");
		sqlBuilder.append(" ON(A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID AND A.SETTLEMENT_DATE=B.SETTLEMENT_DATE)");
		sqlBuilder.append(" WHEN NOT MATCHED THEN");
		sqlBuilder.append(" INSERT (A.SETTLEMENT_ID, A.MERCH_ID, A.ITEM_ID, A.SETTLEMENT_DATE, A.ITEM_BAR, A.ITEM_NAME, A.ITEM_KIND_ID, A.UNIT_NAME, A.COST, A.PRI1, A.PRI2, A.PRI4, A.DISCOUNT, A.PROFIT_AMOUNT, A.LOSS_AMOUNT, A.SALE_AMOUNT, A.SALE_QUANTITY, A.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE, A.SALE_QUANTITY_ONLINE, A.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE, A.SALE_QUANTITY_OFFLINE, A.PURCH_AMOUNT, A.PURCH_QUANTITY, A.WHSE_AMOUNT, A.WHSE_QUANTITY, A.WHSE_TURN_PL_AMOUNT, A.WHSE_TURN_PL_QUANTITY, A.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE, A.RETURN_QUANTITY, A.RETURN_QUANTITY_ONLINE, A.RETURN_QUANTITY_OFFLINE, A.WHSE_WARN_QUANTITY)");
		sqlBuilder.append(" VALUES (B.SETTLEMENT_ID, B.MERCH_ID, B.ITEM_ID, B.SETTLEMENT_DATE, B.ITEM_BAR, B.ITEM_NAME, B.ITEM_KIND_ID, B.UNIT_NAME, B.COST, B.PRI1, B.PRI2, B.PRI4, B.DISCOUNT, B.PROFIT_AMOUNT, B.LOSS_AMOUNT, B.SALE_AMOUNT, B.SALE_QUANTITY, B.PROFIT_AMOUNT_ONLINE, B.LOSS_AMOUNT_ONLINE, B.SALE_AMOUNT_ONLINE, B.SALE_QUANTITY_ONLINE, B.PROFIT_AMOUNT_OFFLINE, B.LOSS_AMOUNT_OFFLINE, B.SALE_AMOUNT_OFFLINE, B.SALE_QUANTITY_OFFLINE, B.PURCH_AMOUNT, B.PURCH_QUANTITY, B.WHSE_AMOUNT, B.WHSE_QUANTITY, B.WHSE_TURN_PL_AMOUNT, B.WHSE_TURN_PL_QUANTITY, B.RETURN_AMOUNT, B.RETURN_AMOUNT_ONLINE, B.RETURN_AMOUNT_OFFLINE, B.RETURN_QUANTITY, B.RETURN_QUANTITY_ONLINE, B.RETURN_QUANTITY_OFFLINE, B.WHSE_WARN_QUANTITY)");
		sqlBuilder.append(" WHEN MATCHED THEN");
		sqlBuilder.append(" UPDATE SET A.SETTLEMENT_ID=B.SETTLEMENT_ID, A.ITEM_BAR=B.ITEM_BAR, A.ITEM_NAME=B.ITEM_NAME, A.ITEM_KIND_ID=B.ITEM_KIND_ID, A.UNIT_NAME=B.UNIT_NAME, A.COST=B.COST, A.PRI1=B.PRI1, A.PRI2=B.PRI2, A.PRI4=B.PRI4, A.DISCOUNT=B.DISCOUNT, A.PROFIT_AMOUNT=B.PROFIT_AMOUNT, A.LOSS_AMOUNT=B.LOSS_AMOUNT, A.SALE_AMOUNT=B.SALE_AMOUNT, A.SALE_QUANTITY=B.SALE_QUANTITY, A.PROFIT_AMOUNT_ONLINE=B.PROFIT_AMOUNT_ONLINE, A.LOSS_AMOUNT_ONLINE=B.LOSS_AMOUNT_ONLINE, A.SALE_AMOUNT_ONLINE=B.SALE_AMOUNT_ONLINE, A.SALE_QUANTITY_ONLINE=B.SALE_QUANTITY_ONLINE, A.PROFIT_AMOUNT_OFFLINE=B.PROFIT_AMOUNT_OFFLINE, A.LOSS_AMOUNT_OFFLINE=B.LOSS_AMOUNT_OFFLINE, A.SALE_AMOUNT_OFFLINE=B.SALE_AMOUNT_OFFLINE, A.SALE_QUANTITY_OFFLINE=B.SALE_QUANTITY_OFFLINE, A.PURCH_AMOUNT=B.PURCH_AMOUNT, A.PURCH_QUANTITY=B.PURCH_QUANTITY, A.WHSE_AMOUNT=B.WHSE_AMOUNT, A.WHSE_QUANTITY=B.WHSE_QUANTITY, A.WHSE_TURN_PL_AMOUNT=B.WHSE_TURN_PL_AMOUNT, A.WHSE_TURN_PL_QUANTITY=B.WHSE_TURN_PL_QUANTITY, A.RETURN_AMOUNT=B.RETURN_AMOUNT, A.RETURN_AMOUNT_ONLINE=B.RETURN_AMOUNT_ONLINE, A.RETURN_AMOUNT_OFFLINE=B.RETURN_AMOUNT_OFFLINE, A.RETURN_QUANTITY=B.RETURN_QUANTITY, A.RETURN_QUANTITY_ONLINE=B.RETURN_QUANTITY_ONLINE, A.RETURN_QUANTITY_OFFLINE=B.RETURN_QUANTITY_OFFLINE, A.WHSE_WARN_QUANTITY=B.WHSE_WARN_QUANTITY");
		this.executeSQL(sqlBuilder.toString(), new Object[] {settlementParam.get("merch_id"), settlementParam.get("settlement_date")});
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------
	
	public static final String insertMerchItemDailySettlementSql = initInsertMerchItemDailySettlementSql();
	private static String initInsertMerchItemDailySettlementSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO MERCH_ITEM_DAILY_SETTLEMENT (SETTLEMENT_ID, MERCH_ID, ITEM_ID, SETTLEMENT_DATE, ITEM_BAR, ITEM_NAME, ITEM_KIND_ID,");
		sb.append(" UNIT_NAME, COST, PRI1, PRI2, PRI4, DISCOUNT, STATUS, PROFIT_AMOUNT, LOSS_AMOUNT, SALE_AMOUNT, SALE_QUANTITY,");
		sb.append(" PROFIT_AMOUNT_ONLINE, LOSS_AMOUNT_ONLINE, SALE_AMOUNT_ONLINE, SALE_QUANTITY_ONLINE,");
		sb.append(" PROFIT_AMOUNT_OFFLINE, LOSS_AMOUNT_OFFLINE, SALE_AMOUNT_OFFLINE, SALE_QUANTITY_OFFLINE,");
		sb.append(" PURCH_AMOUNT, PURCH_QUANTITY, WHSE_AMOUNT, WHSE_QUANTITY, WHSE_WARN_QUANTITY, WHSE_TURN_PL_AMOUNT, WHSE_TURN_PL_QUANTITY,");
		sb.append(" RETURN_AMOUNT, RETURN_AMOUNT_ONLINE, RETURN_AMOUNT_OFFLINE, RETURN_QUANTITY, RETURN_QUANTITY_ONLINE, RETURN_QUANTITY_OFFLINE)");
		sb.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		return sb.toString();
	}
	/**
	 * 插入商户商户日结数据
	 */
	@Override
	public void insertMerchItemDailySettlement(Collection<Map<String, Object>> settlementParamList) throws Exception {
		LOG.debug(" = * = * = * = * = StatisticsDaoImpl insertMerchItemDailySettlement settlementParamList = * = * = * = * = ");
		List<Object[]> paramArray = new ArrayList<Object[]>();
		for(Map<String, Object> settlementParamMap : settlementParamList) {
			List<Object> paramObject = new ArrayList<Object>();
			paramObject.add(settlementParamMap.get("settlement_id"));
			paramObject.add(settlementParamMap.get("merch_id"));
			paramObject.add(settlementParamMap.get("item_id"));
			paramObject.add(settlementParamMap.get("settlement_date"));
			paramObject.add(settlementParamMap.get("item_bar"));
			paramObject.add(settlementParamMap.get("item_name"));
			paramObject.add(settlementParamMap.get("item_kind_id"));
			paramObject.add(settlementParamMap.get("unit_name"));
			paramObject.add(settlementParamMap.get("cost"));
			paramObject.add(settlementParamMap.get("pri1"));
			paramObject.add(settlementParamMap.get("pri2"));
			paramObject.add(settlementParamMap.get("pri4"));
			paramObject.add(settlementParamMap.get("discount"));
			paramObject.add(MapUtil.getString(settlementParamMap, "status", "1"));
//			if(settlementParamMap.containsKey("profit_amount")) {
//				paramObject.add(settlementParamMap.get("profit_amount"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "profit_amount"));
//			if(settlementParamMap.containsKey("loss_amount")) {
//				paramObject.add(settlementParamMap.get("loss_amount"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "loss_amount"));
//			if(settlementParamMap.containsKey("sale_amount")) {
//				paramObject.add(settlementParamMap.get("sale_amount"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "sale_amount"));
//			if(settlementParamMap.containsKey("sale_quantity")) {
//				paramObject.add(settlementParamMap.get("sale_quantity"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "sale_quantity"));
//			if(settlementParamMap.containsKey("profit_amount_online")) {
//				paramObject.add(settlementParamMap.get("profit_amount_online"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "profit_amount_online"));
//			if(settlementParamMap.containsKey("loss_amount_online")) {
//				paramObject.add(settlementParamMap.get("loss_amount_online"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "loss_amount_online"));
//			if(settlementParamMap.containsKey("sale_amount_online")) {
//				paramObject.add(settlementParamMap.get("sale_amount_online"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "sale_amount_online"));
//			if(settlementParamMap.containsKey("sale_quantity_online")) {
//				paramObject.add(settlementParamMap.get("sale_quantity_online"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "sale_quantity_online"));
//			if(settlementParamMap.containsKey("profit_amount_offline")) {
//				paramObject.add(settlementParamMap.get("profit_amount_offline"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "profit_amount_offline"));
//			if(settlementParamMap.containsKey("loss_amount_offline")) {
//				paramObject.add(settlementParamMap.get("loss_amount_offline"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "loss_amount_offline"));
//			if(settlementParamMap.containsKey("sale_amount_offline")) {
//				paramObject.add(settlementParamMap.get("sale_amount_offline"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "sale_amount_offline"));
//			if(settlementParamMap.containsKey("sale_quantity_offline")) {
//				paramObject.add(settlementParamMap.get("sale_quantity_offline"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "sale_quantity_offline"));
//			if(settlementParamMap.containsKey("purch_amount")) {
//				paramObject.add(settlementParamMap.get("purch_amount"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "purch_amount"));
//			if(settlementParamMap.containsKey("purch_quantity")) {
//				paramObject.add(settlementParamMap.get("purch_quantity"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "purch_quantity"));
//			if(settlementParamMap.containsKey("whse_amount")) {
//				paramObject.add(settlementParamMap.get("whse_amount"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "whse_amount"));
//			if(settlementParamMap.containsKey("whse_quantity")) {
//				paramObject.add(settlementParamMap.get("whse_quantity"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "whse_quantity"));
//			if(settlementParamMap.containsKey("whse_warn_quantity")) {
//				paramObject.add(settlementParamMap.get("whse_warn_quantity"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "whse_warn_quantity"));
//			if(settlementParamMap.containsKey("whse_turn_pl")) {
//				paramObject.add(settlementParamMap.get("whse_turn_pl"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "whse_turn_pl"));
//			if(settlementParamMap.containsKey("whse_turn_pl_quantity")) {
//				paramObject.add(settlementParamMap.get("whse_turn_pl_quantity"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "whse_turn_pl_quantity"));
//			if(settlementParamMap.containsKey("return_amount")) {
//				paramObject.add(settlementParamMap.get("return_amount"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "return_amount"));
//			if(settlementParamMap.containsKey("return_amount_online")) {
//				paramObject.add(settlementParamMap.get("return_amount_online"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "return_amount_online"));
//			if(settlementParamMap.containsKey("return_amount_offline")) {
//				paramObject.add(settlementParamMap.get("return_amount_offline"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "return_amount_offline"));
//			if(settlementParamMap.containsKey("return_quantity")) {
//				paramObject.add(settlementParamMap.get("return_quantity"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "return_quantity"));
//			if(settlementParamMap.containsKey("return_quantity_online")) {
//				paramObject.add(settlementParamMap.get("return_quantity_online"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "return_quantity_online"));
//			if(settlementParamMap.containsKey("return_quantity_offline")) {
//				paramObject.add(settlementParamMap.get("return_quantity_offline"));
//			} else {
//				paramObject.add(BigDecimal.ZERO);
//			}
			paramObject.add(MapUtil.getBigDecimal(settlementParamMap, "return_quantity_offline"));
			paramArray.add(paramObject.toArray());
		}
		this.executeBatchSQL(insertMerchItemDailySettlementSql, paramArray);
	}
	
	public static final String selectMerchDailySettlementSql = initSelectMerchDailySettlementSql();
	private static String initSelectMerchDailySettlementSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SETTLEMENT_ID, MERCH_ID, SETTLEMENT_DATE, PROFIT_AMOUNT, LOSS_AMOUNT, SALE_AMOUNT, SALE_LOSS,");
		sb.append(" SALE_COUNT, PROFIT_AMOUNT_ONLINE, LOSS_AMOUNT_ONLINE, SALE_AMOUNT_ONLINE, SALE_LOSS_ONLINE, SALE_COUNT_ONLINE,");
		sb.append(" PROFIT_AMOUNT_OFFLINE, LOSS_AMOUNT_OFFLINE, SALE_AMOUNT_OFFLINE, SALE_LOSS_OFFLINE, SALE_COUNT_OFFLINE,");
		sb.append(" PURCH_AMOUNT, PURCH_QUANTITY, PURCH_COUNT, PURCH_ITEM_COUNT, WHSE_AMOUNT, WHSE_QUANTITY, WHSE_ITEM_COUNT, WHSE_TURN_PROFIT_AMOUNT,");
		sb.append(" WHSE_TURN_LOSS_AMOUNT, WHSE_TURN_PROFIT_QUANTITY, WHSE_TURN_LOSS_QUANTITY, WHSE_TURN_COUNT,");
		sb.append(" RETURN_AMOUNT, RETURN_AMOUNT_ONLINE, RETURN_AMOUNT_OFFLINE FROM MERCH_DAILY_SETTLEMENT WHERE MERCH_ID=?");
		return sb.toString();
	}
	/**
	 * 查询商户日结数据
	 */
	@Override
	public List<Map<String, Object>> selectMerchDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl selectMerchDailySettlement settlementParam: " + settlementParam);
		StringBuffer sqlBuffer = new StringBuffer(selectMerchDailySettlementSql);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(settlementParam.get("merch_id"));
		if(settlementParam.get("settlement_date")!=null) {
			sqlBuffer.append(" AND SETTLEMENT_DATE=?");
			paramList.add(settlementParam.get("settlement_date"));
		} else {
			if(settlementParam.get("settlement_date_ceiling")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE<=?");
				paramList.add(settlementParam.get("settlement_date_ceiling"));
			}
			if(settlementParam.get("settlement_date_floor")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE>=?");
				paramList.add(settlementParam.get("settlement_date_floor"));
			}
		}
		sqlBuffer.append(" ORDER BY SETTLEMENT_DATE ASC");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	public static final String searchMerchMonthlySettlementSql = initSearchMerchMonthlySettlementSql();
	private static String initSearchMerchMonthlySettlementSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, SUBSTR(SETTLEMENT_DATE, 0, 6) SETTLEMENT_MONTH, NVL(SUM(PROFIT_AMOUNT),0) PROFIT_AMOUNT,");
		sb.append(" NVL(SUM(LOSS_AMOUNT),0) LOSS_AMOUNT, NVL(SUM(SALE_AMOUNT),0) SALE_AMOUNT, NVL(SUM(SALE_LOSS),0) SALE_LOSS,");
		sb.append(" NVL(SUM(SALE_COUNT),0) SALE_COUNT, NVL(SUM(PROFIT_AMOUNT_ONLINE),0) PROFIT_AMOUNT_ONLINE,");
		sb.append(" NVL(SUM(LOSS_AMOUNT_ONLINE),0) LOSS_AMOUNT_ONLINE, NVL(SUM(SALE_AMOUNT_ONLINE),0) SALE_AMOUNT_ONLINE,");
		sb.append(" NVL(SUM(SALE_LOSS_ONLINE),0) SALE_LOSS_ONLINE, NVL(SUM(SALE_COUNT_ONLINE),0) SALE_COUNT_ONLINE,");
		sb.append(" NVL(SUM(PROFIT_AMOUNT_OFFLINE),0) PROFIT_AMOUNT_OFFLINE, NVL(SUM(LOSS_AMOUNT_OFFLINE),0) LOSS_AMOUNT_OFFLINE,");
		sb.append(" NVL(SUM(SALE_AMOUNT_OFFLINE),0) SALE_AMOUNT_OFFLINE, NVL(SUM(SALE_LOSS_OFFLINE),0) SALE_LOSS_OFFLINE,");
		sb.append(" NVL(SUM(SALE_COUNT_OFFLINE),0) SALE_COUNT_OFFLINE, NVL(SUM(PURCH_AMOUNT),0) PURCH_AMOUNT,");
		sb.append(" NVL(SUM(PURCH_QUANTITY),0) PURCH_QUANTITY, NVL(SUM(PURCH_COUNT),0) PURCH_COUNT,");
		sb.append(" NVL(SUM(PURCH_ITEM_COUNT),0) PURCH_ITEM_COUNT, NVL(SUM(WHSE_AMOUNT),0) WHSE_AMOUNT,");
		sb.append(" NVL(SUM(WHSE_QUANTITY),0) WHSE_QUANTITY, NVL(SUM(WHSE_ITEM_COUNT),0) WHSE_ITEM_COUNT,");
		sb.append(" NVL(SUM(WHSE_TURN_PROFIT_AMOUNT),0) WHSE_TURN_PROFIT_AMOUNT, NVL(SUM(WHSE_TURN_LOSS_AMOUNT),0) WHSE_TURN_LOSS_AMOUNT,");
		sb.append(" NVL(SUM(WHSE_TURN_PROFIT_QUANTITY),0) WHSE_TURN_PROFIT_QUANTITY, NVL(SUM(WHSE_TURN_LOSS_QUANTITY),0) WHSE_TURN_LOSS_QUANTITY, ");
		sb.append(" NVL(SUM(WHSE_TURN_COUNT),0) WHSE_TURN_COUNT, NVL(SUM(RETURN_AMOUNT),0) RETURN_AMOUNT,");
		sb.append(" NVL(SUM(RETURN_AMOUNT_ONLINE),0) RETURN_AMOUNT_ONLINE, NVL(SUM(RETURN_AMOUNT_OFFLINE),0) RETURN_AMOUNT_OFFLINE");
		sb.append(" FROM MERCH_DAILY_SETTLEMENT WHERE MERCH_ID=?");
		return sb.toString();
	}
	/**
	 * 查询商户月结数据
	 */
	@Override
	public List<Map<String, Object>> searchMerchMonthlySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl searchMerchMonthlySettlement settlementParam: " + settlementParam);
		StringBuffer sqlBuffer = new StringBuffer(searchMerchMonthlySettlementSql);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(settlementParam.get("merch_id"));
		if(settlementParam.get("settlement_date")!=null) {
			sqlBuffer.append(" AND SETTLEMENT_DATE=?");
			paramList.add(settlementParam.get("settlement_date"));
		} else {
			if(settlementParam.get("settlement_date_ceiling")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE<=?");
				paramList.add(settlementParam.get("settlement_date_ceiling"));
			}
			if(settlementParam.get("settlement_date_floor")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE>=?");
				paramList.add(settlementParam.get("settlement_date_floor"));
			}
		}
		sqlBuffer.append(" GROUP BY MERCH_ID, SUBSTR(SETTLEMENT_DATE, 0, 6) ORDER BY SUBSTR(SETTLEMENT_DATE, 0, 6) ASC");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	
	
	/**
	 * 查询商户商品日结数据 和 
	 * 通过item_kind_id,settlement_date分组
	 */
	public static final String selectSumMerchItemDailySettlementSql = initSelectSumMerchItemDailySettlementSql();
	private static String initSelectSumMerchItemDailySettlementSql() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SUBSTR(SETTLEMENT_DATE,1,6) SETTLEMENT_DATE,ITEM_KIND_ID,COUNT(*) COUNT, ");
//		sql.append("max(settlement_date||','||item_bar||','||item_name||','||UNIT_NAME||','||PRI1||','||pri4||','||cost) item_info, ");
		sql.append("SUM(PROFIT_AMOUNT) SUM_PROFIT_AMOUNT,SUM(LOSS_AMOUNT) SUM_LOSS_AMOUNT,SUM(SALE_AMOUNT) SUM_SALE_AMOUNT, ");
		sql.append("SUM(SALE_QUANTITY) SUM_SALE_QUANTITY,SUM(PROFIT_AMOUNT_ONLINE) SUM_PROFIT_AMOUNT_ONLINE,SUM(LOSS_AMOUNT_ONLINE) SUM_LOSS_AMOUNT_ONLINE, ");
		sql.append("SUM(SALE_AMOUNT_ONLINE) SUM_SALE_AMOUNT_ONLINE,SUM(SALE_QUANTITY_ONLINE) SUM_SALE_QUANTITY_ONLINE,SUM(PROFIT_AMOUNT_OFFLINE) SUM_PROFIT_AMOUNT_OFFLINE, ");
		sql.append("SUM(LOSS_AMOUNT_OFFLINE) SUM_LOSS_AMOUNT_OFFLINE,SUM(SALE_AMOUNT_OFFLINE) SUM_SALE_AMOUNT_OFFLINE,SUM(SALE_QUANTITY_OFFLINE) SUM_SALE_QUANTITY_OFFLINE, ");
		sql.append("SUM(PURCH_AMOUNT) SUM_PURCH_AMOUNT,SUM(PURCH_QUANTITY) SUM_PURCH_QUANTITY,SUM(WHSE_AMOUNT) SUM_WHSE_AMOUNT,SUM(WHSE_QUANTITY) SUM_WHSE_QUANTITY, ");
		sql.append("SUM(WHSE_TURN_PL_AMOUNT) SUM_WHSE_TURN_PL_AMOUNT,SUM(WHSE_TURN_PL_QUANTITY) SUM_WHSE_TURN_PL_QUANTITY,SUM(RETURN_AMOUNT) SUM_RETURN_AMOUNT, ");
		sql.append("SUM(RETURN_AMOUNT_ONLINE) SUM_RETURN_AMOUNT_ONLINE,SUM(RETURN_AMOUNT_OFFLINE) SUM_RETURN_AMOUNT_OFFLINE,SUM(RETURN_QUANTITY) SUM_RETURN_QUANTITY, ");
		sql.append("SUM(RETURN_QUANTITY_ONLINE) SUM_RETURN_QUANTITY_ONLINE,SUM(RETURN_QUANTITY_OFFLINE) SUM_RETURN_QUANTITY_OFFLINE,SUM(WHSE_WARN_QUANTITY) SUM_WHSE_WARN_QUANTITY ");
		sql.append("from MERCH_ITEM_DAILY_SETTLEMENT ");
		sql.append("where 1=1 ");
		sql.append("and merch_id=? ");
		return sql.toString();
	}
	
	@Override
	public List<Map<String, Object>> searchSumMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("SumMerchItemDailySettlement settlementParam: " + settlementParam);
		String merchId = MapUtil.getString(settlementParam, "merch_id");
		String startDate = MapUtil.getString(settlementParam, "settlement_date_floor");//开始时间
		String endDate = MapUtil.getString(settlementParam, "settlement_date_ceiling");//结束时间
		String timeinterval = MapUtil.getString(settlementParam, "time_interval");
		List<Object> list = new ArrayList<Object>();
		list.add(merchId);
		StringBuffer sql = new StringBuffer(selectSumMerchItemDailySettlementSql);
		sql.append("and settlement_date between ? and ? ");
		list.add(startDate);
		list.add(endDate);
		sql.append("GROUP BY ITEM_KIND_ID, SUBSTR(SETTLEMENT_DATE,1,6) ");
		sql.append("ORDER BY SETTLEMENT_DATE  ");
		String sqlStr = null;
		if(timeinterval.equals("day") || timeinterval.equals("week")){
			sqlStr = (sql.toString()).replaceAll("SUBSTR\\(SETTLEMENT_DATE,1,6\\)", "SETTLEMENT_DATE");
		}else{
			sqlStr = sql.toString();
		}
		LOG.debug("-------------"+sqlStr);
		LOG.debug("-------------"+list);
		return this.selectBySqlQuery(sqlStr, list.toArray());
	}
	
	
	
	/**
	 * 查询商户商品日结数据
	 */
	public static final String selectMerchItemDailySettlementSql = initSelectMerchItemDailySettlementSql();
	private static String initSelectMerchItemDailySettlementSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SETTLEMENT_ID, MERCH_ID, ITEM_ID, SETTLEMENT_DATE, ITEM_BAR, ITEM_NAME, NVL(ITEM_KIND_ID, '99') ITEM_KIND_ID,");
		sb.append(" UNIT_NAME, COST, PRI1, PRI2, PRI4, DISCOUNT, PROFIT_AMOUNT, LOSS_AMOUNT, SALE_AMOUNT, SALE_QUANTITY,");
		sb.append(" PROFIT_AMOUNT_ONLINE, LOSS_AMOUNT_ONLINE, SALE_AMOUNT_ONLINE, SALE_QUANTITY_ONLINE, PROFIT_AMOUNT_OFFLINE,");
		sb.append(" LOSS_AMOUNT_OFFLINE, SALE_AMOUNT_OFFLINE, SALE_QUANTITY_OFFLINE, PURCH_AMOUNT, PURCH_QUANTITY,");
		sb.append(" WHSE_AMOUNT, WHSE_QUANTITY, WHSE_WARN_QUANTITY, WHSE_TURN_PL_AMOUNT, WHSE_TURN_PL_QUANTITY, RETURN_AMOUNT,");
		sb.append(" RETURN_AMOUNT_ONLINE, RETURN_AMOUNT_OFFLINE, RETURN_QUANTITY, RETURN_QUANTITY_ONLINE, RETURN_QUANTITY_OFFLINE");
		sb.append(" FROM MERCH_ITEM_DAILY_SETTLEMENT WHERE MERCH_ID=?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> selectMerchItemDailySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl selectMerchItemDailySettlement settlementParam: " + settlementParam);
		StringBuffer sqlBuffer = new StringBuffer(selectMerchItemDailySettlementSql);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(settlementParam.get("merch_id"));
		if(settlementParam.get("item_id")!=null) {
			sqlBuffer.append(" AND ITEM_ID=?");
			paramList.add(settlementParam.get("item_id"));
		}
		if(settlementParam.get("settlement_date")!=null) {
			sqlBuffer.append(" AND SETTLEMENT_DATE=?");
			paramList.add(settlementParam.get("settlement_date"));
		} else {
			if(settlementParam.get("settlement_date_ceiling")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE<=?");
				paramList.add(settlementParam.get("settlement_date_ceiling"));
			}
			if(settlementParam.get("settlement_date_floor")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE>=?");
				paramList.add(settlementParam.get("settlement_date_floor"));
			}
		}
		sqlBuffer.append(" ORDER BY SETTLEMENT_DATE ASC");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	public static final String searchMerchItemMonthlySettlementSql = initSearchMerchItemMonthlySettlementSql();
	private static String initSearchMerchItemMonthlySettlementSql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT MERCH_ID, MAX(ITEM_ID) ITEM_ID, SUBSTR(SETTLEMENT_DATE, 0, 6) SETTLEMENT_MONTH, MAX(ITEM_BAR) ITEM_BAR, MAX(ITEM_NAME) ITEM_NAME, NVL(ITEM_KIND_ID, '99') ITEM_KIND_ID,");
		sb.append(" MAX(UNIT_NAME) UNIT_NAME, MAX(COST) COST, MAX(PRI1) PRI1, MAX(PRI2) PRI2, MAX(PRI4) PRI4, MAX(DISCOUNT) DISCOUNT, MAX(PROFIT_AMOUNT) PROFIT_AMOUNT, SUM(LOSS_AMOUNT) LOSS_AMOUNT, MAX(SALE_AMOUNT) SALE_AMOUNT, MAX(SALE_QUANTITY) SALE_QUANTITY,");
		sb.append(" MAX(PROFIT_AMOUNT_ONLINE) PROFIT_AMOUNT_ONLINE, MAX(LOSS_AMOUNT_ONLINE) LOSS_AMOUNT_ONLINE, MAX(SALE_AMOUNT_ONLINE) SALE_AMOUNT_ONLINE, MAX(SALE_QUANTITY_ONLINE) SALE_QUANTITY_ONLINE, MAX(PROFIT_AMOUNT_OFFLINE) PROFIT_AMOUNT_OFFLINE,");
		sb.append(" MAX(LOSS_AMOUNT_OFFLINE) LOSS_AMOUNT_OFFLINE, MAX(SALE_AMOUNT_OFFLINE) SALE_AMOUNT_OFFLINE, MAX(SALE_QUANTITY_OFFLINE) SALE_QUANTITY_OFFLINE, MAX(PURCH_AMOUNT) PURCH_AMOUNT, MAX(PURCH_QUANTITY) PURCH_QUANTITY,");
		sb.append(" MAX(WHSE_AMOUNT) WHSE_AMOUNT, MAX(WHSE_QUANTITY) WHSE_QUANTITY, MAX(WHSE_WARN_QUANTITY) WHSE_WARN_QUANTITY, MAX(WHSE_TURN_PL_AMOUNT) WHSE_TURN_PL_AMOUNT, MAX(WHSE_TURN_PL_QUANTITY) WHSE_TURN_PL_QUANTITY, MAX(RETURN_AMOUNT) RETURN_AMOUNT,");
		sb.append(" MAX(RETURN_AMOUNT_ONLINE) RETURN_AMOUNT_ONLINE, MAX(RETURN_AMOUNT_OFFLINE) RETURN_AMOUNT_OFFLINE, MAX(RETURN_QUANTITY) RETURN_QUANTITY, MAX(RETURN_QUANTITY_ONLINE) RETURN_QUANTITY_ONLINE, MAX(RETURN_QUANTITY_OFFLINE) RETURN_QUANTITY_OFFLINE ");
		sb.append(" FROM MERCH_ITEM_DAILY_SETTLEMENT WHERE MERCH_ID=?");
		
//		sb.append("SELECT MERCH_ID, SUBSTR(SETTLEMENT_DATE, 0, 6) SETTLEMENT_MONTH, ITEM_KIND_ID,");
//		sb.append(" NVL(SUM(PROFIT_AMOUNT),0) PROFIT_AMOUNT, NVL(SUM(LOSS_AMOUNT),0) LOSS_AMOUNT");
//		sb.append(" FROM MERCH_ITEM_DAILY_SETTLEMENT WHERE MERCH_ID=?");
		return sb.toString();
	}
	@Override
	public List<Map<String, Object>> searchMerchItemMonthlySettlement(Map<String, Object> settlementParam) throws Exception {
		LOG.debug("StatisticsDaoImpl searchMerchItemMonthlySettlement settlementParam: " + settlementParam);
		StringBuffer sqlBuffer = new StringBuffer(searchMerchItemMonthlySettlementSql);
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(settlementParam.get("merch_id"));
		if(settlementParam.get("settlement_date")!=null) {
			sqlBuffer.append(" AND SETTLEMENT_DATE=?");
			paramList.add(settlementParam.get("settlement_date"));
		} else {
			if(settlementParam.get("settlement_date_ceiling")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE<=?");
				paramList.add(settlementParam.get("settlement_date_ceiling"));
			}
			if(settlementParam.get("settlement_date_floor")!=null) {
				sqlBuffer.append(" AND SETTLEMENT_DATE>=?");
				paramList.add(settlementParam.get("settlement_date_floor"));
			}
		} 
		sqlBuffer.append(" GROUP BY MERCH_ID, ITEM_KIND_ID, SUBSTR(SETTLEMENT_DATE, 0, 6) ORDER BY SUBSTR(SETTLEMENT_DATE, 0, 6) ASC");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	@Override
	public List<Map<String, Object>> searchJxcReportTable(Map<String, Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(selectSaleReportTableSql);
		List<Object> objList=new ArrayList<Object>();
		
		String merchId = MapUtil.getString(saleMap, "merch_id");
		String itemKindId = MapUtil.getString(saleMap, "item_kind_id");
		String startDate = MapUtil.getString(saleMap, "start_date");
		String endDate = MapUtil.getString(saleMap, "end_date");
		
		objList.add(merchId);
		sql.append("and SETTLEMENT_DATE between ? and ? ");
		objList.add(startDate);
		objList.add(endDate);
		
		if( !StringUtil.isBlank(itemKindId) 
				&& !itemKindId.equals("99")
				&& !itemKindId.equals("00") ) {
			sql.append("and item_kind_id = ? ");
			objList.add(itemKindId);
		}else if( !StringUtil.isBlank(itemKindId) 
				&& itemKindId.equals("99") ){
			sql.append("and (item_kind_id is null or item_kind_id not in ('01','02','03','04','05','06','07','08')) ");
		}
		sql.append("group by item_id ");
		sql.append("having sum(SALE_QUANTITY) != 0 or SUM(PURCH_QUANTITY) !=0 or SUM(RETURN_QUANTITY) !=0 or SUM(WHSE_QUANTITY) !=0 ");
		return selectBySqlQuery(sql.toString(),objList.toArray());
	}
	
	//商品日结表格
	public static final String selectItemDailySettlementSql=initSelectItemDailySettlementSql();
	private static String initSelectItemDailySettlementSql(){
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT ITEM_ID, ");
		sql.append("SETTLEMENT_DATE, ITEM_BAR, ITEM_NAME, UNIT_NAME, PRI1, PRI4, COST, ");
		sql.append("PROFIT_AMOUNT,LOSS_AMOUNT, SALE_AMOUNT, ");
		sql.append("SALE_QUANTITY,  PROFIT_AMOUNT_ONLINE, LOSS_AMOUNT_ONLINE, ");
		sql.append("SALE_AMOUNT_ONLINE, SALE_QUANTITY_ONLINE, PROFIT_AMOUNT_OFFLINE, ");
		sql.append("LOSS_AMOUNT_OFFLINE,SALE_AMOUNT_OFFLINE, SALE_QUANTITY_OFFLINE, ");
		sql.append("PURCH_AMOUNT,PURCH_QUANTITY, WHSE_AMOUNT, WHSE_QUANTITY, ");
		sql.append("WHSE_TURN_PL_AMOUNT, WHSE_TURN_PL_QUANTITY, RETURN_AMOUNT, ");
		sql.append("RETURN_AMOUNT_ONLINE,RETURN_AMOUNT_OFFLINE, RETURN_QUANTITY, ");
		sql.append("RETURN_QUANTITY_ONLINE, RETURN_QUANTITY_OFFLINE, ");
		sql.append("WHSE_WARN_QUANTITY ");
		sql.append("FROM MERCH_ITEM_DAILY_SETTLEMENT ");
		sql.append("WHERE 1=1 ");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searchItemDailySettlement(Map<String, Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(selectItemDailySettlementSql);
		List<Object> objList=new ArrayList<Object>();
		
		String merchId = MapUtil.getString(saleMap, "merch_id");
		String itemKindId = MapUtil.getString(saleMap, "item_kind_id");
		String startDate = MapUtil.getString(saleMap, "start_date");
		String endDate = MapUtil.getString(saleMap, "end_date");
		String settlementDate = MapUtil.getString(saleMap, "settlement_date");
		String itemId = MapUtil.getString(saleMap, "item_id");
		sql.append("and MERCH_ID = ? ");
		objList.add(merchId);
		if(!StringUtil.isBlank(settlementDate)){
//			sql.append("and SETTLEMENT_DATE between ? and ? ");
			String[] settlementDateArr = settlementDate.split(",");
			if(settlementDateArr.length >= 1){
				sql.append("AND SETTLEMENT_DATE IN (?");
				objList.add(settlementDateArr[0]);
			}
			for (int i = 1; i < settlementDateArr.length; i++) {
				sql.append(",?");
				objList.add(settlementDateArr[i]);
			}
			sql.append(") ");
		}
		if(!StringUtil.isBlank(startDate)){
			sql.append("AND SETTLEMENT_DATE >= ? ");
			objList.add(startDate);
		}
		if(!StringUtil.isBlank(endDate)){
			sql.append("AND SETTLEMENT_DATE <= ? ");
			objList.add(endDate);
		}
		
		if(!StringUtil.isBlank(itemId)){
			String [] itemArr = itemId.split(",");
			if(itemArr.length >= 1){
				sql.append(" and item_id in (?");
				objList.add(itemArr[0]);
			}
			for (int i = 1; i < itemArr.length; i++) {
				sql.append(",?");
				objList.add(itemArr[i]);
			}
			sql.append(") ");
		}
		if( !StringUtil.isBlank(itemKindId) 
				&& !itemKindId.equals("99")
				&& !itemKindId.equals("00") ) {
			sql.append("and item_kind_id = ? ");
			objList.add(itemKindId);
		}else if( !StringUtil.isBlank(itemKindId) 
				&& itemKindId.equals("99") ){
			sql.append("and (item_kind_id is null or item_kind_id not in ('01','02','03','04','05','06','07','08')) ");
		}
//		sql.append("having sum(SALE_QUANTITY) != 0 or SUM(PURCH_QUANTITY) !=0 or SUM(RETURN_QUANTITY) !=0 or SUM(WHSE_QUANTITY) !=0 ");
		return selectBySqlQuery(sql.toString(),objList.toArray());
	}

	/**
	 * description 商户日结进销存利本月前几条最高利润
	 * @param topProfitParam
	 * @return
	 * */
	@Override
	public List<Map<String, Object>> selectMerchMonthMaxProfit(Map<String, Object> profitParam) throws Exception {
		LOG.debug("StatisticsDaoImpl selectMerchMonthTopProfit profitParam: " + profitParam);
		String merchId = MapUtil.getString(profitParam, "merch_id");
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append("SELECT ITEM_ID, ITEM_NAME, UNIT_NAME, PRI4,PROFIT_AMOUNT, ");
		sqlBuffer.append("SALE_QUANTITY / (CASE WHEN BIG_UNIT_RATIO = 0 THEN 1 ELSE BIG_UNIT_RATIO END ) SALE_QUANTITY ");
		sqlBuffer.append("FROM ( ");
		sqlBuffer.append("SELECT MIDS.ITEM_ID ITEM_ID, SUM(SALE_QUANTITY) SALE_QUANTITY, MAX(BMI.BIG_UNIT_RATIO) BIG_UNIT_RATIO, ");
		sqlBuffer.append("MAX(BMI.ITEM_NAME) ITEM_NAME, MAX(BMI.BIG_UNIT_NAME) UNIT_NAME, MAX(BMI.BIG_PRI4) PRI4, ");
		sqlBuffer.append("CASE WHEN SUM(PROFIT_AMOUNT) < 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) END PROFIT_AMOUNT ");
		sqlBuffer.append("FROM MERCH_ITEM_DAILY_SETTLEMENT MIDS, BASE_MERCH_ITEM BMI ");
		sqlBuffer.append("WHERE BMI.ITEM_ID = MIDS.ITEM_ID AND BMI.MERCH_ID = MIDS.MERCH_ID ");
		sqlBuffer.append("AND MIDS.MERCH_ID = ? ");
		
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(merchId);
		if(profitParam.get("settlement_date")!=null) {
			SQLUtil.initSQLEqual(profitParam, sqlBuffer, paramList, "settlement_date");
		} else {
			SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "settlement_date", "settlement_date_floor", "settlement_date_ceiling");
		}
		sqlBuffer.append(" GROUP BY MIDS.ITEM_ID ");
		sqlBuffer.append("HAVING SUM(SALE_QUANTITY)>0 ");
		sqlBuffer.append("AND((( CASE WHEN SUM(SALE_AMOUNT) = 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) / SUM(SALE_AMOUNT) END) >= 0.08 AND MAX(BMI.ITEM_KIND_ID) IN ('01','0102')) OR MAX(BMI.ITEM_KIND_ID) NOT IN ('01','0102')) ");
		sqlBuffer.append("ORDER BY PROFIT_AMOUNT DESC, MIDS.ITEM_ID DESC ");
		sqlBuffer.append(") MI ");
		sqlBuffer.append("WHERE");
		
		int rowLimit = MapUtil.getInt(profitParam, "num", 3);
		profitParam.put("rownum_floor", 1);
		profitParam.put("rownum_ceiling", rowLimit);
		SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "rownum", "rownum_floor", "rownum_ceiling");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	/**
	 * description    商户日结进销存利本月后几条最低利润
	 * @param topProfitParam
	 * @return  
	 * */
	@Override
	public List<Map<String, Object>> selectMerchMonthMinProfit(Map<String, Object> profitParam) throws Exception {
		LOG.debug("StatisticsDaoImpl selectMerchMonthMinProfit profitParam: " + profitParam);
		String merchId = MapUtil.getString(profitParam, "merch_id");
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append("SELECT ITEM_ID, ITEM_NAME, UNIT_NAME, PRI4,PROFIT_AMOUNT, ");
		sqlBuffer.append("SALE_QUANTITY / (CASE WHEN BIG_UNIT_RATIO = 0 THEN 1 ELSE BIG_UNIT_RATIO END ) SALE_QUANTITY ");
		sqlBuffer.append("FROM ( ");
		sqlBuffer.append("SELECT MIDS.ITEM_ID ITEM_ID, SUM(SALE_QUANTITY) SALE_QUANTITY, MAX(BMI.BIG_UNIT_RATIO) BIG_UNIT_RATIO, ");
		sqlBuffer.append("MAX(BMI.ITEM_NAME) ITEM_NAME, MAX(BMI.BIG_UNIT_NAME) UNIT_NAME, MAX(BMI.BIG_PRI4) PRI4, ");
		sqlBuffer.append("CASE WHEN SUM(PROFIT_AMOUNT) < 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) END PROFIT_AMOUNT ");
		sqlBuffer.append("FROM MERCH_ITEM_DAILY_SETTLEMENT MIDS, BASE_MERCH_ITEM BMI ");
		sqlBuffer.append("WHERE BMI.ITEM_ID = MIDS.ITEM_ID AND BMI.MERCH_ID = MIDS.MERCH_ID ");
		sqlBuffer.append("AND MIDS.MERCH_ID = ? ");
		
		List<Object> paramList = new ArrayList<Object>();
		paramList.add(merchId);
		if(profitParam.get("settlement_date")!=null) {
			SQLUtil.initSQLEqual(profitParam, sqlBuffer, paramList, "settlement_date");
		} else {
			SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "settlement_date", "settlement_date_floor", "settlement_date_ceiling");
		}
		sqlBuffer.append(" GROUP BY MIDS.ITEM_ID ");
		sqlBuffer.append("HAVING SUM(SALE_QUANTITY)> 0 ");
		sqlBuffer.append("AND((( CASE WHEN SUM(SALE_AMOUNT) = 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) / SUM(SALE_AMOUNT) END) >= 0.08 AND MAX(BMI.ITEM_KIND_ID) IN ('01','0102')) OR MAX(BMI.ITEM_KIND_ID) NOT IN ('01','0102')) ");

		sqlBuffer.append("ORDER BY PROFIT_AMOUNT, MIDS.ITEM_ID ");
		sqlBuffer.append(") MI ");
		sqlBuffer.append("WHERE");
		
		int rowLimit = MapUtil.getInt(profitParam, "num", 3);
		profitParam.put("rownum_floor", 1);
		profitParam.put("rownum_ceiling", rowLimit);
		SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "rownum", "rownum_floor", "rownum_ceiling");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	/**
	 * description    进销存利本月利润率最高商品
	 * @param profitParam   
	 * @return  
	 * */
	@Override
	public List<Map<String, Object>> selectMerchMonthMaxProfitRate(Map<String, Object> profitParam) throws Exception {
		LOG.debug("StatisticsDaoImpl selectMerchMonthMaxProfitRate profitParam: " + profitParam);
		String merchId = MapUtil.getString(profitParam, "merch_id");
		StringBuilder sqlBuffer = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		sqlBuffer.append("SELECT  ITEM_ID, ITEM_NAME, UNIT_NAME, PRI4, SALE_QUANTITY, SALE_AMOUNT, PROFIT_AMOUNT, ");
		sqlBuffer.append("(CASE WHEN PROFIT_RATE < 0 THEN 0 ELSE (CASE WHEN PROFIT_RATE > 0.50 THEN 0.50 ELSE PROFIT_RATE END) END ) *100 PROFIT_RATE ");
		sqlBuffer.append("FROM ( ");
		sqlBuffer.append("SELECT MIDS.ITEM_ID, MAX(BMI.ITEM_NAME) ITEM_NAME, MAX(BMI.BIG_UNIT_NAME) UNIT_NAME, MAX(BMI.BIG_PRI4) PRI4, ");
		sqlBuffer.append("CASE WHEN MAX(BMI.BIG_UNIT_RATIO) = 0 THEN SUM(SALE_QUANTITY) ELSE SUM(SALE_QUANTITY) / MAX(BMI.BIG_UNIT_RATIO) END SALE_QUANTITY, ");
		sqlBuffer.append("SUM(SALE_AMOUNT) SALE_AMOUNT, SUM(PROFIT_AMOUNT) PROFIT_AMOUNT, ");
		sqlBuffer.append("CASE WHEN SUM(SALE_AMOUNT) = 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) / SUM(SALE_AMOUNT) END PROFIT_RATE ");
		sqlBuffer.append("FROM MERCH_ITEM_DAILY_SETTLEMENT MIDS, BASE_MERCH_ITEM BMI ");
		sqlBuffer.append("WHERE BMI.MERCH_ID = MIDS.MERCH_ID AND BMI.ITEM_ID = MIDS.ITEM_ID ");
		sqlBuffer.append("AND MIDS.MERCH_ID = ? ");
		
		paramList.add(merchId);
		if(profitParam.get("settlement_date")!=null) {
			SQLUtil.initSQLEqual(profitParam, sqlBuffer, paramList, "settlement_date");
		} else {
			SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "settlement_date", "settlement_date_floor", "settlement_date_ceiling");
		}
		sqlBuffer.append(" GROUP BY MIDS.ITEM_ID  ");
		sqlBuffer.append("HAVING SUM(SALE_QUANTITY) > 0 ");
		sqlBuffer.append("AND((( CASE WHEN SUM(SALE_AMOUNT) = 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) / SUM(SALE_AMOUNT) END) >= 0.08 AND MAX(BMI.ITEM_KIND_ID) IN ('01','0102')) OR MAX(BMI.ITEM_KIND_ID) NOT IN ('01','0102')) ");
		sqlBuffer.append("ORDER BY PROFIT_RATE DESC, MIDS.ITEM_ID DESC ");
		sqlBuffer.append(") ");
		sqlBuffer.append("WHERE");
		
		int rowLimit = MapUtil.getInt(profitParam, "num", 3);
		profitParam.put("rownum_floor", 1);
		profitParam.put("rownum_ceiling", rowLimit);
		SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "rownum", "rownum_floor", "rownum_ceiling");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	
	/**
	 * description    进销存利本月利润率最低商品
	 * @param topProfitParam
	 * @return  
	 * */
	@Override
	public List<Map<String, Object>> selectMerchMonthMinProfitRate(Map<String, Object> profitParam) throws Exception {
		LOG.debug("StatisticsDaoImpl selectMerchMonthMinProfitRate profitParam: " + profitParam);
		String merchId = MapUtil.getString(profitParam, "merch_id");
		StringBuilder sqlBuffer = new StringBuilder();
		List<Object> paramList = new ArrayList<Object>();
		sqlBuffer.append("SELECT  ITEM_ID, ITEM_NAME, UNIT_NAME, PRI4, SALE_QUANTITY, SALE_AMOUNT, PROFIT_AMOUNT, ");
		sqlBuffer.append("(CASE WHEN PROFIT_RATE < 0 THEN 0 ELSE (CASE WHEN PROFIT_RATE > 0.50 THEN 0.50 ELSE PROFIT_RATE END) END ) *100 PROFIT_RATE ");
		sqlBuffer.append("FROM ( ");
		sqlBuffer.append("SELECT MIDS.ITEM_ID, MAX(BMI.ITEM_NAME) ITEM_NAME, MAX(BMI.BIG_UNIT_NAME) UNIT_NAME, MAX(BMI.BIG_PRI4) PRI4, ");
		sqlBuffer.append("CASE WHEN MAX(BMI.BIG_UNIT_RATIO) = 0 THEN SUM(SALE_QUANTITY) ELSE SUM(SALE_QUANTITY) / MAX(BMI.BIG_UNIT_RATIO) END SALE_QUANTITY, ");
		sqlBuffer.append("SUM(SALE_AMOUNT) SALE_AMOUNT, SUM(PROFIT_AMOUNT) PROFIT_AMOUNT, ");
		sqlBuffer.append("CASE WHEN SUM(SALE_AMOUNT) = 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) / SUM(SALE_AMOUNT) END PROFIT_RATE ");
		sqlBuffer.append("FROM MERCH_ITEM_DAILY_SETTLEMENT MIDS, BASE_MERCH_ITEM BMI ");
		sqlBuffer.append("WHERE BMI.MERCH_ID = MIDS.MERCH_ID AND BMI.ITEM_ID = MIDS.ITEM_ID ");
		sqlBuffer.append("AND MIDS.MERCH_ID = ? ");
		
		paramList.add(merchId);
		if(profitParam.get("settlement_date")!=null) {
			SQLUtil.initSQLEqual(profitParam, sqlBuffer, paramList, "settlement_date");
		} else {
			SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "settlement_date", "settlement_date_floor", "settlement_date_ceiling");
		}
		sqlBuffer.append(" GROUP BY MIDS.ITEM_ID ");
		sqlBuffer.append("HAVING SUM(SALE_QUANTITY) > 0 ");
		sqlBuffer.append("AND((( CASE WHEN SUM(SALE_AMOUNT) = 0 THEN 0 ELSE SUM(PROFIT_AMOUNT) / SUM(SALE_AMOUNT) END) >= 0.08 AND MAX(BMI.ITEM_KIND_ID) IN ('01','0102')) OR MAX(BMI.ITEM_KIND_ID) NOT IN ('01','0102')) ");
		sqlBuffer.append("ORDER BY PROFIT_RATE , MIDS.ITEM_ID  ");
		sqlBuffer.append(") ");
		sqlBuffer.append("WHERE");
		
		int rowLimit = MapUtil.getInt(profitParam, "num", 3);
		profitParam.put("rownum_floor", 1);
		profitParam.put("rownum_ceiling", rowLimit);
		SQLUtil.initSQLBetweenAnd(profitParam, sqlBuffer, paramList, "rownum", "rownum_floor", "rownum_ceiling");
		return this.selectBySqlQuery(sqlBuffer.toString(), paramList.toArray());
	}
	//销售详细报表
	public static final String selectSaleReportTableSql=getSaleReportTableSql();
	private static String getSaleReportTableSql(){
		StringBuffer sql=new StringBuffer();
		sql.append(" select item_id,");
		sql.append(" max(settlement_date||','||item_bar||','||item_name||','||UNIT_NAME||','||PRI1||','||pri4||','||cost) item_info,");
		sql.append("SUM(PROFIT_AMOUNT) SUM_PROFIT_AMOUNT,SUM(LOSS_AMOUNT) SUM_RETURN_PROFIT,SUM(SALE_AMOUNT) SUM_SALE_AMOUNT, ");
		sql.append("SUM(SALE_QUANTITY) SUM_SALE_QUANTITY,SUM(PROFIT_AMOUNT_ONLINE) SUM_PROFIT_AMOUNT_ONLINE,SUM(LOSS_AMOUNT_ONLINE) SUM_LOSS_AMOUNT_ONLINE, ");
		sql.append("SUM(SALE_AMOUNT_ONLINE) SUM_SALE_AMOUNT_ONLINE,SUM(SALE_QUANTITY_ONLINE) SUM_SALE_QUANTITY_ONLINE,SUM(PROFIT_AMOUNT_OFFLINE) SUM_PROFIT_AMOUNT_OFFLINE, ");
		sql.append("SUM(LOSS_AMOUNT_OFFLINE) SUM_LOSS_AMOUNT_OFFLINE,SUM(SALE_AMOUNT_OFFLINE) SUM_SALE_AMOUNT_OFFLINE,SUM(SALE_QUANTITY_OFFLINE) SUM_SALE_QUANTITY_OFFLINE, ");
		sql.append("SUM(PURCH_AMOUNT) SUM_PURCH_AMOUNT,SUM(PURCH_QUANTITY) SUM_PURCH_QUANTITY,SUM(WHSE_AMOUNT) SUM_WHSE_AMOUNT,SUM(WHSE_QUANTITY) SUM_WHSE_QUANTITY, ");
		sql.append("SUM(WHSE_TURN_PL_AMOUNT) SUM_WHSE_TURN_PL_AMOUNT,SUM(WHSE_TURN_PL_QUANTITY) SUM_WHSE_TURN_PL_QUANTITY,SUM(RETURN_AMOUNT) SUM_RETURN_AMOUNT, ");
		sql.append("SUM(RETURN_AMOUNT_ONLINE) SUM_RETURN_AMOUNT_ONLINE,SUM(RETURN_AMOUNT_OFFLINE) SUM_RETURN_AMOUNT_OFFLINE,SUM(RETURN_QUANTITY) SUM_RETURN_QUANTITY, ");
		sql.append("SUM(RETURN_QUANTITY_ONLINE) SUM_RETURN_QUANTITY_ONLINE,SUM(RETURN_QUANTITY_OFFLINE) SUM_RETURN_QUANTITY_OFFLINE, "); 
		sql.append("SUM(WHSE_WARN_QUANTITY) SUM_WHSE_WARN_QUANTITY,COUNT(*) ROW_COUNT ");
		sql.append("from MERCH_ITEM_DAILY_SETTLEMENT ");
		sql.append("where 1=1 ");
		sql.append("and merch_id=? ");
		
		
		sql.append("");
		return sql.toString();
	}
	@Override
	public List<Map<String , Object>> searchSaleReportTable(Map<String, Object> saleMap)throws Exception{
		StringBuffer sql=new StringBuffer(selectSaleReportTableSql);
		List<Object> objList=new ArrayList<Object>();
		
		String merchId = MapUtil.getString(saleMap, "merch_id");
		String itemKindId = MapUtil.getString(saleMap, "item_kind_id");
		String startDate = MapUtil.getString(saleMap, "start_date");
		String endDate = MapUtil.getString(saleMap, "end_date");
		
		objList.add(merchId);
		sql.append(" and SETTLEMENT_DATE between ? and ?");
		objList.add(startDate);
		objList.add(endDate);
		
		if( !StringUtil.isBlank(itemKindId) 
				&& !itemKindId.equals("99")
				&& !itemKindId.equals("00") ) {
			sql.append(" and item_kind_id = ?");
			objList.add(itemKindId);
		}else if( !StringUtil.isBlank(itemKindId) 
				&& itemKindId.equals("99") ){
			sql.append(" and (item_kind_id is null or item_kind_id not in ('01','02','03','04','05','06','07','08'))");
		}
		sql.append(" group by item_id");
		sql.append(" having sum(SALE_QUANTITY) != 0");
		return selectBySqlQuery(sql.toString(),objList.toArray());
	}
	
	public static final String selectBusinessHistorySql = initSelectBusinessHistorySql();
	private static String initSelectBusinessHistorySql() {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT NVL(SUM(PROFIT_AMOUNT),0) PROFIT_AMOUNT, NVL(SUM(SALE_ONLINE_AMOUNT),0) SALE_ONLINE_AMOUNT, NVL(SUM(SALE_ONLINE_COUNT),0) SALE_ONLINE_COUNT,");
		sb.append(" NVL(SUM(SALE_SHOP_AMOUNT),0) SALE_SHOP_AMOUNT, NVL(SUM(SALE_SHOP_COUNT),0) SALE_SHOP_COUNT,");
		sb.append(" NVL(SUM(SALE_TOTAL_AMOUNT),0) SALE_TOTAL_AMOUNT, NVL(SUM(SALE_TOTAL_COUNT),0) SALE_TOTAL_COUNT,");
		sb.append(" NVL(SUM(PURCH_AMOUNT),0) PURCH_AMOUNT, NVL(SUM(PURCH_COUNT),0) PURCH_COUNT, NVL(SUM(WHSE_AMOUNT),0) WHSE_AMOUNT, NVL(SUM(WHSE_COUNT),0) WHSE_COUNT,");
		sb.append(" NVL(SUM(WHSE_TURN_PROFIT),0) WHSE_TURN_PROFIT, NVL(SUM(WHSE_TURN_LOSS),0) WHSE_TURN_LOSS, NVL(SUM(WHSE_TURN_COUNT),0) WHSE_TURN_COUNT");
		sb.append(" FROM MANAGER_RECORD WHERE MERCH_ID = ? AND RECORD_DATE BETWEEN ? AND ?");
		return sb.toString();
	}
	@Override
	public Map<String, Object> getBusinessHistory(Map<String, Object> saleOrderParam) throws Exception {
		LOG.debug("StatisticsDaoImpl getSaleHistory saleOrderParam:" + saleOrderParam);
		String merchId = (String) saleOrderParam.get("merch_id");
		String startDate = (String) saleOrderParam.get("start_date");
		String endDate = (String) saleOrderParam.get("end_date");
		List<Map<String, Object>> saleHistoryResult = this.selectBySqlQuery(selectBusinessHistorySql, new Object[] {merchId, startDate, endDate});
		if(saleHistoryResult.size() == 1) {
			return saleHistoryResult.get(0);
		}
		return null;
	}
	
	// 查询进货记录
	public static final String selectPurchRecordsSql = initSelectPurchRecordsSql();
	private static String initSelectPurchRecordsSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT A.ITEM_ID, MAX(SETTLEMENT_DATE||','||A.ITEM_NAME) ITEM_INFO, MAX(C.BIG_BAR) BAR_CODE, ");
		sql.append("MAX(C.BIG_UNIT_NAME) UNIT_NAME, MAX(CASE WHEN B.MAX_PRI IS NULL THEN 0 ELSE B.MAX_PRI*C.BIG_UNIT_RATIO END) MAX_PRI, ");
		sql.append("MIN(CASE WHEN B.MIN_PRI IS NULL THEN B.AMT_ORD/B.QTY_ORD/C.BIG_UNIT_RATIO ELSE B.MIN_PRI*C.BIG_UNIT_RATIO END) MIN_PRI,SUM(A.SALE_QUANTITY/C.BIG_UNIT_RATIO) SALE_QUANTITY, ");
		sql.append("SUM(B.AMT_ORD)/SUM(B.QTY_ORD/C.BIG_UNIT_RATIO) AVG_PRI, SUM(B.QTY_ORD/C.BIG_UNIT_RATIO) PURCH_QUANTITY, ");
		sql.append("SUM(B.AMT_ORD) PURCH_AMOUNT, SUM(CASE WHEN B.PURCH_TIMES IS NULL THEN 0 ELSE B.PURCH_TIMES END) PURCH_TIMES, ");
		sql.append("(CASE WHEN SUM(A.SALE_QUANTITY)!=0 THEN SUM(B.QTY_ORD)/SUM(A.SALE_QUANTITY) ELSE 0 END) PURCH_SALE_RATIO ");
		sql.append("FROM MERCH_ITEM_DAILY_SETTLEMENT A LEFT JOIN ");
		sql.append("(SELECT T2.ITEM_ID, T1.VOUCHER_DATE, SUM(T2.AMT_ORD) AMT_ORD, SUM(T2.QTY_ORD) QTY_ORD, MAX(CASE WHEN T2.QTY_ORD!=0 THEN T2.AMT_ORD/T2.QTY_ORD ELSE 0 END) MAX_PRI, ");
		sql.append("MIN(CASE WHEN T2.QTY_ORD!=0 THEN T2.AMT_ORD/T2.QTY_ORD ELSE NULL END) MIN_PRI, SUM(T2.PURCH_TIMES) PURCH_TIMES FROM PURCH_ORDER T1, ");
		sql.append("(SELECT ORDER_ID, ITEM_ID, SUM(AMT_ORD) AMT_ORD, SUM(QTY_ORD*UNIT_RATIO) QTY_ORD, 1 PURCH_TIMES FROM PURCH_ORDER_LINE WHERE MERCH_ID = ? GROUP BY ORDER_ID, ITEM_ID) T2 ");
		sql.append("WHERE T1.ORDER_ID=T2.ORDER_ID AND T1.MERCH_ID=? AND T1.VOUCHER_DATE BETWEEN ? AND ? ");
		sql.append("GROUP BY T2.ITEM_ID, T1.VOUCHER_DATE) B ON A.ITEM_ID=B.ITEM_ID AND A.SETTLEMENT_DATE=B.VOUCHER_DATE, BASE_MERCH_ITEM C ");
		sql.append("WHERE A.MERCH_ID=C.MERCH_ID AND A.ITEM_ID=C.ITEM_ID ");
		sql.append("AND A.MERCH_ID=? AND A.SETTLEMENT_DATE BETWEEN ? AND ? AND A.PURCH_AMOUNT!=0 AND A.PURCH_QUANTITY!=0 ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> selectPurchRecords(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectPurchRecords paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder(selectPurchRecordsSql);
		List<Object> list = new ArrayList<Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		list.add(merchId);
		list.add(merchId);
		list.add(startDate);
		list.add(endDate);
		list.add(merchId);
		list.add(startDate);
		list.add(endDate);
		String minPurchAmount = MapUtil.getString(paramMap, "min_purch_amount");
		String maxPurchAmount = MapUtil.getString(paramMap, "max_purch_amount");
		
		SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_bar");
		
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sql.append(" AND ( A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sql.append(" OR A.ITEM_KIND_ID = ? ) ");
			list.add("01");
			list.add("02");
			list.add("03");
			list.add("04");
			list.add("05");
			list.add("06");
			list.add("07");
			list.add("08");
			list.add("0102");
			list.add("99");
		}else{
			SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_kind_id");
		}
		/*//luch查询
		String itemId = MapUtil.getString(paramMap, "item_id");
		if(paramMap.containsKey("item_id")){
//			sql.append(" AND (C.BIG_BAR LIKE ? ");
//			sql.append(" OR A.ITEM_NAME LIKE ?) ");
//			list.add(keyWord+"%");
//			list.add(keyWord+"%");
			
			String [] itemIdArr = itemId.split(",");
			if(itemIdArr.length >= 1){
				sql.append("AND C.ITEM_ID IN (?");
				list.add(itemIdArr[0]);
			}
			for (int i = 1; i < itemIdArr.length; i++) {
				sql.append(", ?");
				list.add(itemIdArr[i]);
			}
			sql.append(") ");
		}*/
		
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			StringBuilder keyWordSbl = new StringBuilder();
			keyWordSbl.append(" ");
			keyWordSbl.append(keyWord);
			keyWordSbl.append(" ");
			String newKeyWord = (keyWordSbl.toString().toUpperCase()).replaceAll("\\s+", "%");
			sql.append(" AND (C.ITEM_NAME LIKE ?  OR C.ITEM_BAR LIKE ? OR C.BIG_BAR LIKE ? OR C.SHORT_CODE LIKE ? OR C.SHORT_NAME LIKE ? ) ");
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
		}
		SQLUtil.initSQLIn(paramMap, sql, list, "c.status");
		sql.append("GROUP BY A.ITEM_ID ");
		if(!StringUtil.isBlank(minPurchAmount) || !StringUtil.isBlank(maxPurchAmount)){
			sql.append(" HAVING 1=1 ");
		}
		if(!StringUtil.isBlank(minPurchAmount)){
			sql.append("AND  SUM(B.AMT_ORD)/SUM(B.QTY_ORD/C.BIG_UNIT_RATIO)>=? ");
			list.add(minPurchAmount);
		}
		if(!StringUtil.isBlank(maxPurchAmount)){
			sql.append("AND  SUM(B.AMT_ORD)/SUM(B.QTY_ORD/C.BIG_UNIT_RATIO)<=? ");
			list.add(maxPurchAmount);
		}
		sql.append("ORDER BY A.ITEM_ID DESC");
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	//查询销售记录
	public static final String selectSalesRecordsSql = initSelectSalesRecordsSql();
	private static String initSelectSalesRecordsSql() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT A.ITEM_ID, MAX(SETTLEMENT_DATE||','||A.ITEM_NAME) ITEM_INFO, MAX(C.BIG_BAR) BAR_CODE, ");
		sql.append("MAX(C.BIG_UNIT_NAME) UNIT_NAME, ");
		sql.append("MAX(CASE WHEN B.MAX_PRI IS NULL THEN 0 ELSE B.MAX_PRI*C.BIG_UNIT_RATIO END) MAX_PRI, ");
		sql.append("MIN(CASE WHEN B.MIN_PRI IS NULL THEN 0 ELSE B.MIN_PRI*C.BIG_UNIT_RATIO END) MIN_PRI, ");
		sql.append("SUM(A.SALE_AMOUNT)/SUM(A.SALE_QUANTITY/C.BIG_UNIT_RATIO) AVG_PRI, SUM(A.SALE_QUANTITY/C.BIG_UNIT_RATIO) SALE_QUANTITY, ");
		sql.append("SUM(A.SALE_AMOUNT) SALE_AMOUNT, SUM(CASE WHEN B.SALE_TIMES IS NULL THEN 0 ELSE B.SALE_TIMES END) SALE_TIMES, ");
		sql.append("SUM(A.RETURN_QUANTITY/C.BIG_UNIT_RATIO) RETURN_QUANTITY, SUM(A.RETURN_AMOUNT) RETURN_AMOUNT ");
		sql.append("FROM MERCH_ITEM_DAILY_SETTLEMENT A LEFT JOIN ");
		sql.append("(SELECT T2.ITEM_ID, T1.ORDER_DATE, SUM(T2.AMT_ORD) AMT_ORD, MAX(T2.AMT_ORD/T2.QTY_ORD/T2.UNIT_RATIO) MAX_PRI, ");
		sql.append("MIN(T2.AMT_ORD/T2.QTY_ORD/T2.UNIT_RATIO) MIN_PRI, SUM(T2.SALE_TIMES) SALE_TIMES FROM SALE_ORDER T1, ");
		sql.append("(SELECT ORDER_ID, ITEM_ID, SUM(AMT_ORD) AMT_ORD, SUM(QTY_ORD) QTY_ORD, AVG(UNIT_RATIO) UNIT_RATIO, 1 SALE_TIMES FROM SALE_ORDER_LINE GROUP BY ORDER_ID, ITEM_ID) T2 ");
		sql.append("WHERE T1.ORDER_ID=T2.ORDER_ID AND T1.MERCH_ID = ? AND T1.ORDER_DATE BETWEEN ? AND ? ");
		sql.append("GROUP BY T2.ITEM_ID, T1.ORDER_DATE) B ON A.ITEM_ID=B.ITEM_ID AND A.SETTLEMENT_DATE=B.ORDER_DATE, BASE_MERCH_ITEM C ");
		sql.append("WHERE A.MERCH_ID=C.MERCH_ID AND A.ITEM_ID=C.ITEM_ID ");
		sql.append("AND A.MERCH_ID = ? AND A.SETTLEMENT_DATE BETWEEN ? AND ?  AND A.SALE_QUANTITY!=0 ");
		return sql.toString();
	};
	@Override
	public List<Map<String, Object>> selectSalesRecords(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectSalesRecords paramMap:"+paramMap);
		List<Object> list = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder(selectSalesRecordsSql);
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		list.add(merchId);
		list.add(startDate);
		list.add(endDate);
		list.add(merchId);
		list.add(startDate);
		list.add(endDate);
		SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_bar");
		
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sql.append(" AND ( A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sql.append(" OR A.ITEM_KIND_ID = ? ) ");
			list.add("01");
			list.add("02");
			list.add("03");
			list.add("04");
			list.add("05");
			list.add("06");
			list.add("07");
			list.add("08");
			list.add("0102");
			list.add("99");
		}else{
			SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_kind_id");
		}
		
		/*//luch查询
		String itemId = MapUtil.getString(paramMap, "item_id");
		if(paramMap.containsKey("item_id")){
//			sql.append(" AND (C.BIG_BAR LIKE ? ");
//			sql.append(" OR A.ITEM_NAME LIKE ?) ");
//			list.add(keyWord+"%");
//			list.add(keyWord+"%");
			
			String [] itemIdArr = itemId.split(",");
			if(itemIdArr.length >= 1){
				sql.append("AND C.ITEM_ID IN (?");
				list.add(itemIdArr[0]);
			}
			for (int i = 1; i < itemIdArr.length; i++) {
				sql.append(", ?");
				list.add(itemIdArr[i]);
			}
			sql.append(") ");
			
		}*/
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			StringBuilder keyWordSbl = new StringBuilder();
			keyWordSbl.append(" ");
			keyWordSbl.append(keyWord);
			keyWordSbl.append(" ");
			String newKeyWord = (keyWordSbl.toString().toUpperCase()).replaceAll("\\s+", "%");
			sql.append(" AND (C.ITEM_NAME LIKE ?  OR C.ITEM_BAR LIKE ? OR C.BIG_BAR LIKE ? OR C.SHORT_CODE LIKE ? OR C.SHORT_NAME LIKE ? ) ");
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
		}
		SQLUtil.initSQLIn(paramMap, sql, list, "c.status");
		sql.append(" GROUP BY A.ITEM_ID ");
		String minSaleAmount = MapUtil.getString(paramMap, "min_sale_amount");
		String maxSaleAmount = MapUtil.getString(paramMap, "max_sale_amount");
		if(!StringUtil.isBlank(minSaleAmount) || !StringUtil.isBlank(maxSaleAmount)){
			sql.append(" HAVING 1=1 ");
		}
		if(!StringUtil.isBlank(minSaleAmount)){
			sql.append("AND SUM(A.SALE_AMOUNT)/SUM(A.SALE_QUANTITY/C.BIG_UNIT_RATIO)>=? ");
			list.add(minSaleAmount);
		}
		if(!StringUtil.isBlank(maxSaleAmount)){
			sql.append("AND SUM(A.SALE_AMOUNT)/SUM(A.SALE_QUANTITY/C.BIG_UNIT_RATIO)<=? ");
			list.add(maxSaleAmount);
		}
		sql.append(" ORDER BY A.ITEM_ID DESC");
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	//查询库存记录
	public static final String selectWhseRecordsSql = initSelectWhseRecordsSql();
	private static String initSelectWhseRecordsSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT A.ITEM_ID, MAX(SETTLEMENT_DATE||','||A.ITEM_NAME) ITEM_INFO, MAX(B.BIG_BAR) BAR_CODE, MAX(B.BIG_UNIT_NAME) UNIT_NAME, ");
		sql.append("SUM(CASE WHEN SETTLEMENT_DATE= ? THEN WHSE_QUANTITY/B.BIG_UNIT_RATIO ELSE 0 END) BEGINING_WHSE_QUANTITY, ");
		sql.append("SUM(CASE WHEN SETTLEMENT_DATE= ? THEN WHSE_AMOUNT ELSE 0 END) BEGINING_WHSE_AMOUNT, ");
		sql.append("SUM(CASE WHEN SETTLEMENT_DATE= ? THEN WHSE_QUANTITY/B.BIG_UNIT_RATIO ELSE 0 END) ENDING_WHSE_QUANTITY, ");
		sql.append("SUM(CASE WHEN SETTLEMENT_DATE= ? THEN WHSE_AMOUNT ELSE 0 END) ENDING_WHSE_AMOUNT, ");
		sql.append("CASE WHEN SUM(SALE_QUANTITY)!=0 THEN SUM(CASE WHEN SETTLEMENT_DATE=? AND WHSE_QUANTITY>0 THEN WHSE_QUANTITY ELSE 0 END)/SUM(SALE_QUANTITY) ELSE 0 END WHSE_SALE_RATIO ");
		sql.append("FROM MERCH_ITEM_DAILY_SETTLEMENT A, BASE_MERCH_ITEM B ");
		sql.append("WHERE A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID ");
		sql.append("AND A.MERCH_ID = ? AND A.SETTLEMENT_DATE BETWEEN ? AND ? ");
		return sql.toString();
	}
	@Override
	public List<Map<String, Object>> selectWhseRecords(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectWhseRecords paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder(selectWhseRecordsSql);
		List<Object> list = new ArrayList<Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		String itemId = MapUtil.getString(paramMap, "item_id");
		list.add(startDate);
		list.add(startDate);
		list.add(endDate);
		list.add(endDate);
		list.add(endDate);
		list.add(merchId);
		list.add(startDate);
		list.add(endDate);
		SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_kind_id", "A.item_bar");
		if(paramMap.containsKey("item_id")){
//			sql.append(" AND (B.BIG_BAR LIKE ? ");
//			sql.append(" OR A.ITEM_NAME LIKE ?) ");
//			list.add(keyWord+"%");
//			list.add(keyWord+"%");
			
			String [] itemIdArr = itemId.split(",");
			if(itemIdArr.length >= 1){
				sql.append("AND C.ITEM_ID IN (?");
				list.add(itemIdArr[0]);
			}
			for (int i = 1; i < itemIdArr.length; i++) {
				sql.append(", ?");
				list.add(itemIdArr[i]);
			}
			sql.append(") ");
		}
		sql.append("GROUP BY A.ITEM_ID ORDER BY A.ITEM_ID DESC");
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	//查询利润记录
	public static final String selectProfitRecordsSql = initSelectProfitRecordsSql();
	private static String initSelectProfitRecordsSql() {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT A.ITEM_ID, MAX(SETTLEMENT_DATE||','||A.ITEM_NAME||','||B.BIG_BAR||','||B.BIG_UNIT_NAME) ITEM_INFO,");
		sql.append(" SUM(SALE_AMOUNT) SALE_AMOUNT, SUM(a.PROFIT_AMOUNT) PROFIT,");
		sql.append(" AVG(A.COST*B.BIG_UNIT_RATIO) AVG_COST, SUM(A.COST * SALE_QUANTITY) COST,");
		sql.append(" SUM(SALE_AMOUNT)/SUM(SALE_QUANTITY/CASE WHEN B.BIG_UNIT_RATIO = 0 THEN 1 ELSE B.BIG_UNIT_RATIO END ) AVG_PRI,");
		sql.append(" SUM(PROFIT_AMOUNT)/SUM(SALE_QUANTITY/CASE WHEN B.BIG_UNIT_RATIO = 0 THEN 1 ELSE B.BIG_UNIT_RATIO END ) AVG_PROFIT,");
		sql.append(" SUM(SALE_QUANTITY/CASE WHEN B.BIG_UNIT_RATIO = 0 THEN 1 ELSE B.BIG_UNIT_RATIO END ) SALE_QUANTITY,");
		sql.append(" CASE WHEN AVG(A.COST)!=0 THEN (SUM(PROFIT_AMOUNT)/SUM(SALE_QUANTITY)/AVG(A.COST)) ELSE 1 END PROFIT_COST_RATIO,");
		sql.append(" CASE WHEN SUM(SALE_AMOUNT) =0 THEN 0 ELSE (SUM(PROFIT_AMOUNT)/SUM(SALE_AMOUNT)) END PROFIT_SALE_RATIO");
		sql.append(" FROM MERCH_ITEM_DAILY_SETTLEMENT A, BASE_MERCH_ITEM B ");
		sql.append(" WHERE A.MERCH_ID=B.MERCH_ID AND A.ITEM_ID=B.ITEM_ID ");
		sql.append(" AND A.MERCH_ID = ? AND SETTLEMENT_DATE BETWEEN ? AND ? ");
		sql.append(" AND SALE_QUANTITY!=0 ");
		return sql.toString();
	}
	
	@Override
	public List<Map<String, Object>> selectProfitRecords(Map<String, Object> paramMap) throws Exception {
		LOG.debug("selectProfitRecords paramMap:"+paramMap);
		StringBuilder sql = new StringBuilder(selectProfitRecordsSql);
		List<Object> list = new ArrayList<Object>();
		String merchId = MapUtil.getString(paramMap, "merch_id");
		String startDate = MapUtil.getString(paramMap, "start_date");
		String endDate = MapUtil.getString(paramMap, "end_date");
		
		list.add(merchId);
		list.add(startDate);
		list.add(endDate);
		SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_bar");
		
		String itemKindId = MapUtil.getString(paramMap, "item_kind_id");
		if (itemKindId.equals("99")) {
//			SQLUtil.initSQLIn(paramMap, sqlBuilder, paramList, "a.item_kind_id");
			sql.append(" AND (A.ITEM_KIND_ID NOT IN (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			sql.append(" OR A.ITEM_KIND_ID = ? ) ");
			list.add("01");
			list.add("02");
			list.add("03");
			list.add("04");
			list.add("05");
			list.add("06");
			list.add("07");
			list.add("08");
			list.add("0102");
			list.add("99");
		}else{
			SQLUtil.initSQLEqual(paramMap, sql, list, "A.item_kind_id");
		}
		
		/*//loce查询
		String itemId = MapUtil.getString(paramMap, "item_id");
		if(paramMap.containsKey("item_id")){
//			sql.append(" AND (B.BIG_BAR LIKE ? ");
//			sql.append(" OR A.ITEM_NAME LIKE ?) ");
//			list.add(keyWord+"%");
//			list.add(keyWord+"%");
			
			String [] itemIdArr = itemId.split(",");
			if(itemIdArr.length >= 1){
				sql.append("AND B.ITEM_ID IN (?");
				list.add(itemIdArr[0]);
			}
			for (int i = 1; i < itemIdArr.length; i++) {
				sql.append(", ?");
				list.add(itemIdArr[i]);
			}
			sql.append(") ");
		}*/
		
		String keyWord = MapUtil.getString(paramMap, "keyword");
		if(!StringUtil.isBlank(keyWord)){
			StringBuilder keyWordSbl = new StringBuilder();
			keyWordSbl.append(" ");
			keyWordSbl.append(keyWord);
			keyWordSbl.append(" ");
			String newKeyWord = (keyWordSbl.toString().toUpperCase()).replaceAll("\\s+", "%");
			sql.append(" AND (B.ITEM_NAME LIKE ?  OR B.ITEM_BAR LIKE ? OR B.BIG_BAR LIKE ? OR B.SHORT_CODE LIKE ? OR B.SHORT_NAME LIKE ? ) ");
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			list.add(newKeyWord);
			
		}
		SQLUtil.initSQLIn(paramMap, sql, list, "b.status");
		sql.append(" GROUP BY A.ITEM_ID ");
		
		String minSaleAmount = MapUtil.getString(paramMap, "min_cost");
		String maxSaleAmount = MapUtil.getString(paramMap, "max_cost");
		if(!StringUtil.isBlank(minSaleAmount) || !StringUtil.isBlank(maxSaleAmount)){
			sql.append(" HAVING 1=1 ");
		}
		if(!StringUtil.isBlank(minSaleAmount)){
			sql.append("AND AVG(A.COST*B.BIG_UNIT_RATIO) >=? ");
			list.add(minSaleAmount);
		}
		if(!StringUtil.isBlank(maxSaleAmount)){
			sql.append("AND AVG(A.COST*B.BIG_UNIT_RATIO) <=? ");
			list.add(maxSaleAmount);
		}
		
		sql.append(" ORDER BY A.ITEM_ID DESC ");
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
//	public static void main(String[] args) {
//		String a = "南京  精品  ";
//		String b = a.replaceAll("\\s+", "%");
//		System.out.println(b);
//		b = b.toUpperCase();
//		System.out.println(b);
//	}
	
	/**
	 * 搜索卷烟日结数据-----潍坊
	 * 通过时间
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<Map<String, Object>> searchTobaccoDailyData(Map<String, Object> paramMap) throws Exception {
		LOG.debug("StatisticsDaoImpl searchTobaccoDailyData paramMap:"+paramMap);
		List<Object> list = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT BM.LICE_ID CIG_ID, MIDS.ITEM_ID BRAND_ID, MIDS.SALE_QUANTITY SALE_COUNT, ");
		sql.append("MIDS.SALE_AMOUNT SALE_MONEY, MIDS.WHSE_QUANTITY STOCK_COUNT, MIDS.WHSE_AMOUNT STOCK_MONEY ");
		sql.append("FROM MERCH_ITEM_DAILY_SETTLEMENT MIDS, BASE_SUPPLIER_TOBACCO_ITEM BSTI, BASE_MERCH BM ");
		sql.append("WHERE BSTI.ITEM_ID = MIDS.ITEM_ID AND MIDS.MERCH_ID = BM.MERCH_ID AND BSTI.SUPPLIER_ID=BM.CGT_COM_ID ");
		sql.append("AND BM.CGT_COM_ID = '10370701' ");
		sql.append("AND SETTLEMENT_DATE = ? ORDER BY CIG_ID, BRAND_ID");
		// 默认查前一天的数据
		list.add(MapUtil.getString(paramMap, "date", DateUtil.getPreviousDay(DateUtil.getToday(), 1)));
		
		return this.selectBySqlQuery(sql.toString(), list.toArray());
	}
	
	
	
}


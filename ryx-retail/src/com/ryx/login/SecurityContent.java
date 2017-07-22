package com.ryx.login;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.utils.Constants;
import com.ryx.framework.utils.HttpUtil;
import com.ryx.framework.utils.JsonUtil;
import com.ryx.framework.utils.MD5Util;
import com.ryx.framework.utils.MapUtil;
import com.ryx.login.identitificate.bean.SessionInfo;
import com.ryx.social.retail.util.RetailConfig;

@Component
@Scope("prototype")
public class SecurityContent {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContent.class);
	
	/* 原程序通过以下sql获取user对象
	 * select pu.*, bm.CGT_COM_ID from pub_user pu left join base_merch bm on pu.ref_id = bm.merch_id where bm.lice_id=? and length(pu.user_code)=12
	 * 通过以下sql取merch对象
	 * select MERCH_ID, MERCH_NAME, MANAGER, TELEPHONE, ADDRESS, BUSI_TIME, BUSI_SCOPE, LONGITUDE, LATITUDE, IS_HEAD, IS_SOCIAL, IS_ORD_CGT, LICE_ID, CGT_COM_ID, STATUS, IS_INIT, DELIVERY_TYPE, OPEN_TIME, CLOSE_TIME, INIT_DATE, INIT_TIME from base_merch where merch_id=?
	 */
	private static final String USER_AND_MERCH_SQL = "select pu.user_id, pu.user_code, pu.user_name, pu.password, pu.user_type, pu.email, pu.phone, pu.ref_id, pu.is_locked, pu.login_fail_num, pu.is_mrb, pu.note, pu.role_ids, bm.merch_id, bm.merch_name, bm.cgt_com_id, bm.lice_id, bm.is_init, bm.init_date, bm.init_time from (select t1.*, t2.role_ids from pub_user t1, pub_user_role t2 where t1.user_code=t2.user_code) pu left join base_merch bm on pu.ref_id=bm.merch_id where pu.user_code=?";
	
	private static final String ROLE_RESOURCE_SQL= "select prr.resources_id from pub_role_resources prr, pub_role pr where prr.role_id=pr.role_id and pr.is_locked='0'";
	
	/* 原程序通过以下sql取permission列表
	select case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_ID else pr1.RESOURCES_ID end resources_id, case when pr2.RESOURCES_NAME is not null then pr2.RESOURCES_NAME else pr1.RESOURCES_NAME end resources_name, case when pr2.RESOURCES_CHILDREN is not null then pr2.RESOURCES_CHILDREN else pr1.RESOURCES_CHILDREN end resources_children, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_PARENT_ID else pr1.RESOURCES_PARENT_ID end resources_parent_id, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_HASPAGE else pr1.RESOURCES_HASPAGE end resources_haspage, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_ORDER else pr1.RESOURCES_ORDER end resources_order, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_SERVICEFLAG else pr1.RESOURCES_SERVICEFLAG end resources_serviceflag, case when pr2.RESOURCES_ID is not null then '1' else '0' end multi from (select * from pub_resources where com_id='all') pr1 full join (select * from pub_resources where com_id=?) pr2 on pr1.RESOURCES_ID=pr2.RESOURCES_ID
	 */
	private static final String RESOURCES_SQL = "select case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_ID else pr1.RESOURCES_ID end resources_id, case when pr2.RESOURCES_NAME is not null then pr2.RESOURCES_NAME else pr1.RESOURCES_NAME end resources_name, case when pr2.RESOURCES_CHILDREN is not null then pr2.RESOURCES_CHILDREN else pr1.RESOURCES_CHILDREN end resources_children, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_PARENT_ID else pr1.RESOURCES_PARENT_ID end resources_parent_id, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_HASPAGE else pr1.RESOURCES_HASPAGE end resources_haspage, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_ORDER else pr1.RESOURCES_ORDER end resources_order, case when pr2.RESOURCES_ID is not null then pr2.RESOURCES_SERVICEFLAG else pr1.RESOURCES_SERVICEFLAG end resources_serviceflag, case when pr2.RESOURCES_ID is not null then '1' else '0' end multi from (select * from pub_resources where com_id='all') pr1 full join (select * from pub_resources where com_id=?) pr2 on pr1.RESOURCES_ID=pr2.RESOURCES_ID";
	private static final String LOCAL_TOKEN_SQL = "select ct.user_id, pu.user_code, bm.merch_id retail_id, bm.lice_id, bm.telephone, bm.cgt_com_id, bm.merch_name, bm.manager, pu.email, ct.tokensecret from cas_token ct, base_merch bm, pub_user pu where ct.merch_id=bm.merch_id(+) and ct.user_id=pu.user_id(+) and ct.tokensecret=?";
	
	private String challengeId;
	private String challengeValue;
	private String username;
	private String clerkId;
	private String password;
	private String token;
	private SessionInfo user;
	private boolean ok;
	private String code;
	private String message;

	public SecurityContent(HttpServletRequest request) {
		
		// 从cookie中取挑战码id和token
		Cookie[] cookies = request.getCookies();
		if(cookies!=null && cookies.length>0) {
			for(Cookie cookie: request.getCookies()) {
				String cookieName = cookie.getName();
				if(cookieName.equals(BaseSecurityFilter.CHALLENGE_COOKIE)) {
					challengeId = cookie.getValue();
				} else if(cookieName.equals(BaseSecurityFilter.TOKEN_COOKIE) && (token==null || "".equals(token))) {
					token = cookie.getValue();
				} else if(cookieName.equals("token") && (token==null || "".equals(token))) {
					token = cookie.getValue();
				}
			}
		}
		// 取token顺序: 1. cookie中的tokenSecret和token字段 2. 请求参数中的token和tokenSecret字段
		if(token==null || "".equals(token)) {
			token = request.getParameter("token");
			if(token==null || "".equals(token)) {
				token = request.getParameter("tokenSecret");
			}
		}
		
		// 从请求参数中取用户名 密码 挑战码
		username = request.getParameter(BaseSecurityFilter.USERNAME_PARAMETER);
		password = request.getParameter(BaseSecurityFilter.PASSWORD_PARAMETER);
		challengeValue = request.getParameter(BaseSecurityFilter.CHALLENGE_PARAMETER);
		// 从session中取SessionInfo
		HttpSession session = request.getSession();
		if(session!=null) user = (SessionInfo) session.getAttribute(BaseSecurityFilter.USER_SESSION);
	}
	
	public SecurityContent(boolean ok, String code, String message) {
		this.ok = ok;
		this.code = code;
		this.message = message;
	}
	
	
	/**
	 * 请求信息中是否有挑战码id和挑战码 如果没有 本对象中的code为"1002" message为"验证码错误"
	 * @return 请求信息中是否有挑战码id和挑战码
	 */
	public boolean isChallengeFine() {
		String challengeValue = ChallengeHolder.removeChanllenge(challengeId);
		boolean isChallengeFine = challengeValue!=null && challengeValue.equals(this.challengeValue);
		if(!isChallengeFine) {
			code = "1002";
			message = "验证码错误";
		}
		return isChallengeFine;
	}
	
	/**
	 * 请求信息中是否有用户名和密码 如果没有 本对象中的code为"1001" message为"用户名或密码为空"
	 * @return 请求信息中是否有用户名和密码
	 */
	public boolean usernameAndPasswordNotEmpty() {
		boolean areUsernameAndPasswordNotEmpty = StringUtils.hasText(username) && StringUtils.hasText(password);
		if(!areUsernameAndPasswordNotEmpty) {
			code = "1001";
			message = "用户名或密码为空";
		}
		return areUsernameAndPasswordNotEmpty;
	}
	
	/**
	 * 请求信息中是否有token 如果没有 本对象中的code为"1000" message为"令牌不存在"
	 * @return 请求信息中是否有token
	 */
	public boolean tokenNotEmpty() {
		boolean isTokenNotEmpty = StringUtils.hasText(token);
		if(!isTokenNotEmpty) {
			code = Constants.CODE_FAIL;
			message = "令牌不存在";
		}
		return isTokenNotEmpty;
	}
	
	public boolean hasUser() {
		return user!=null;
	}
	
	/**
	 * 向用户中心验证用户名密码 将验证结果存放在本对象和内部user对象中
	 * 并将请求参数中的username根据"_"分割成真正的username和clerkId
	 * <p>本对象的code和message根据返回信息中的code和msg赋值
	 */
	public void authenticateUsernamePasswordAndPackUsername() {
		packUsernameAndClerkId(); 
		Map<String, String> param = new HashMap<String, String>();
		param.put("login_name", StringUtils.hasText(clerkId) ? (username+"_"+clerkId) : username);
		param.put("user_pwd", MD5Util.getMD5Code(password+"{ryx}"));
		param.put("app_name", "tobacco");
		Map<String, String> params= new HashMap<String, String>();
		params.put("params", JsonUtil.map2json(param));
		String userResultString = HttpUtil.post(RetailConfig.getUcenterServer() + "/public/tobacco/login", params);
		// code msg result page 其中result是用户对象
		packResult(userResultString);
	}

	/**
	 * 将请求参数中的username根据"_"拆分成真正的username和clerkId
	 */
	private void packUsernameAndClerkId() {
		if(StringUtils.hasText(username)) {
			 String[] usernameAndClerkId = StringUtils.delimitedListToStringArray(username, "_");
			 if(usernameAndClerkId.length==2) {
				 username = usernameAndClerkId[0];
				 clerkId = usernameAndClerkId[1];
			 } else {
				 clerkId = "";
			 }
		}
	}
	
	public void authenticateLocalToken(BaseDaoImpl baseDao) {
		if(!tokenNotEmpty()) return;
		try {
			List<Map<String, Object>> tokens = baseDao.selectBySqlQuery(LOCAL_TOKEN_SQL, new Object[] {token});
			packResult(tokens);
		} catch (SQLException e) {
			LOGGER.error(" = * = * = * = * = * = * = 根据token获取本地用户信息失败 = * = * = * = * = * = * = ", e);
			code = Constants.CODE_FAIL;
			message = "根据token获取本地用户信息失败";
			ok = false;
		}
	}
	
	/**
	 * 根据本地token返回信息填充数据 包括
	 * <p>本对象的code, message, token, ok
	 * <p>内部user对象的userId, userCode, merchId, merchName, LiceId, comId, manager, phone, email
	 * @param callbackString 用户中心返回的json字符串
	 */
	private void packResult(List<Map<String, Object>> tokens) {
		if(tokens!=null && !tokens.isEmpty()) {
			Map<String, Object> merchInfo = tokens.get(0);
			user = new SessionInfo();

			user.setUserId(MapUtil.getString(merchInfo, "user_id"));
			user.setUserCode(MapUtil.getString(merchInfo, "user_code"));
			
			user.setMerchId(MapUtil.getString(merchInfo, "retail_id"));
			
			user.setLiceId(MapUtil.getString(merchInfo, "lice_id"));
			user.setPhone(MapUtil.getString(merchInfo, "telephone"));
			user.setComId(MapUtil.getString(merchInfo, "cgt_com_id"));
			user.setMerchName(MapUtil.getString(merchInfo, "merch_name"));
			user.setUserName(MapUtil.getString(merchInfo, "manager"));
			user.setEmail(MapUtil.getString(merchInfo, "email"));

			token = MapUtil.getString(merchInfo, "token");
			ok = true;
			code = Constants.CODE_SUCCESS;
			message = Constants.MSG_SUCCESS;
		} else {
			ok = false;
			code = Constants.CODE_FAIL;
			message = token+" 没有对应本地用户信息";
		}
	}
	
	/**
	 * 向用户中心验证令牌 将验证结果存放在本对象和内部user对象中
	 * <p>本对象的code和message根据返回信息中的code和msg赋值
	 */
	public void authenticateToken() {
		if(!tokenNotEmpty()) return;
		Map<String, String> param = new HashMap<String, String>();
		param.put("token", token);
		param.put("details", "1110000000");
		String userResultString = HttpUtil.post(RetailConfig.getUcenterServer() + "/ucenter/tobacco/getuserinfo", param);
		packResult(userResultString);
	}
	
	/**
	 * 根据用户中心返回信息填充数据 包括
	 * <p>本对象的code, message, token, ok
	 * <p>内部user对象的userId, userCode, merchId, merchName, LiceId, comId, manager, phone, email
	 * @param callbackString 用户中心返回的json字符串
	 */
	@SuppressWarnings("unchecked")
	private void packResult(String callbackString) {
		// 解析返回结果
		Map<String, Object> callback = JsonUtil.json2Map(callbackString);
		code = MapUtil.getString(callback, "code");
		ok = Constants.CODE_SUCCESS.equals(MapUtil.getString(callback, "code"));
		message = MapUtil.getString(callback, "msg");
		// 根据返回结果组装组员
		if(ok) {
			/* 外层结构
			 * {"token":"59aec883240df3a9202e5d279779419a","user_phone":"","user_code":"370112107467","user_id":"1000000000000035962","user_email":"","virtual_account":"1000000000","merch_info":{略},"relative_info":{略}}
			 */
			Map<String, Object> userInfo = MapUtil.get(callback, "result", Collections.EMPTY_MAP);
			/* relative_info内层结构
			 * {"retail_id":"1037010407467","pay_id_jn":"1000000000000035962","rtms_id":"1037010407467"}
			 */
			Map<String, Object> relativeInfo = MapUtil.get(userInfo, "relative_info", Collections.EMPTY_MAP);
			/* merch_info内层结构
			 * {"legal_card_id":"","cgt_com_id":"10370101","merch_name":"高新开发区孙村鑫航超市","note":"RTMS","province":"","user_id":"1000000000000035962","postcode":"","business_lice":"","lice_id":"370112107467","telephone":"","manager":"尹成莉","email":"","val_lice":"","city":"","address":"济南高新开发区孙村街道办事处住宅小区91号楼5单元102室","longitude":"117.174732","merch_type":"11","legal_name":"","latitude":"36.770005"}
			 */
			Map<String, Object> merchInfo = MapUtil.get(userInfo, "merch_info", Collections.EMPTY_MAP);
			
			// 用户数据可以不再从本地获取 但是需要关注一下店员的信息是否完整 #礼现 20150504
			// fillUserAndMerch方法中还是依赖本地的pub_user数据
			
			user = new SessionInfo();

			user.setUserId(MapUtil.getString(userInfo, "user_id"));
			user.setUserCode(MapUtil.getString(userInfo, "user_code"));
			token = MapUtil.getString(userInfo, "token");
			
			user.setMerchId(MapUtil.getString(relativeInfo, "retail_id"));
			
			user.setLiceId(MapUtil.getString(merchInfo, "lice_id"));
			user.setPhone(MapUtil.getString(merchInfo, "telephone"));
			user.setComId(MapUtil.getString(merchInfo, "cgt_com_id"));
			user.setMerchName(MapUtil.getString(merchInfo, "merch_name"));
			user.setUserName(MapUtil.getString(merchInfo, "manager"));
			user.setEmail(MapUtil.getString(merchInfo, "email"));
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public void fillUserAndMerch(BaseDaoImpl baseDao) throws SQLException {
		List<Map<String, Object>> userAndMerchList = baseDao.selectBySqlQuery(USER_AND_MERCH_SQL, new Object[] {user.getUserCode()});
		if(userAndMerchList.size()==1) {
			Map<String, Object> userAndMerch = userAndMerchList.get(0);
			// user数据
			user.setUserId(MapUtil.getString(userAndMerch, "user_id"));
			user.setUserCode(MapUtil.getString(userAndMerch, "user_code"));
			user.setUserName(MapUtil.getString(userAndMerch, "user_name"));
			user.setPassword(MapUtil.getString(userAndMerch, "password"));
			user.setUserType(MapUtil.getString(userAndMerch, "user_type"));
			user.setEmail(MapUtil.getString(userAndMerch, "email"));
			user.setPhone(MapUtil.getString(userAndMerch, "phone"));
			user.setRefId(MapUtil.getString(userAndMerch, "ref_id"));
			user.setLocked("1".equals(MapUtil.getString(userAndMerch, "is_locked")));
			user.setLoginFailNum(MapUtil.getInt(userAndMerch, "login_fail_num"));
			user.setMrb("1".equals(MapUtil.getString(userAndMerch, "is_mrb")));
			user.setNote(MapUtil.getString(userAndMerch, "note"));
			user.setRoleId(MapUtil.getString(userAndMerch, "role_ids"));
			// merch数据
			user.setMerchId(MapUtil.getString(userAndMerch, "merch_id"));
			user.setMerchName(MapUtil.getString(userAndMerch, "merch_name"));
			user.setComId(MapUtil.getString(userAndMerch, "cgt_com_id"));
			user.setLiceId(MapUtil.getString(userAndMerch, "lice_id"));
			user.setInit(MapUtil.getString(userAndMerch, "is_init"));
			user.setInitDate(MapUtil.getString(userAndMerch, "init_date"));
			user.setInitTime(MapUtil.getString(userAndMerch, "init_time"));
		} else {
			ok = false;
			code = "1001";
			message = "根据专卖证号获取用户信息失败";
		}
	}
	
	@SuppressWarnings("unchecked")
	public void fillPermission(BaseDaoImpl baseDao) throws SQLException {
		// 根据fillUserAndMerch方法中获取的role_id查询resource_id
		String roleIds = user.getRoleId();
		String roleIdSql = roleIds==null ? "" : roleIds.replaceAll(",", "','");
		List<Map<String, Object>> resourceIds = baseDao.selectBySqlQuery(ROLE_RESOURCE_SQL+" and prr.role_id in ('"+roleIdSql+"')");
		StringBuilder resourceIdSqlBuilder = new StringBuilder();
		int resourceIndex = 0;
		for(Map<String, Object> resourceId : resourceIds) {
			resourceIdSqlBuilder.append("'");
			resourceIdSqlBuilder.append(MapUtil.getString(resourceId, "resources_id").replaceAll(",", "','"));
			resourceIdSqlBuilder.append("'");
			if(resourceIndex++!=0) {
				resourceIdSqlBuilder.append(",");
			}
		}
		// 根据id查询resource信息
		List<Map<String, Object>> resourceList = baseDao.selectBySqlQuery(RESOURCES_SQL+" where pr1.resources_id in ("+resourceIdSqlBuilder+") or pr2.resources_id in ("+resourceIdSqlBuilder+") order by resources_order", new Object[] {user.getComId()});
		List<Map<String, Object>> resourceResult = new ArrayList<Map<String, Object>>();
		// 组装权限
		for(Map<String, Object> resource : resourceList) {
			Map<String, Object> resourceRow = new HashMap<String, Object>();
			resourceRow.put("module_id", resource.get("resources_id"));
			resourceRow.put("title", resource.get("resources_name"));
			resourceRow.put("multi", resource.get("multi"));
			resourceRow.put("haschildren", resource.get("resources_children"));
			resourceRow.put("haspage", resource.get("resources_haspage"));
			resourceRow.put("parent_id", resource.get("resources_parent_id"));
			resourceResult.add(resourceRow);
		}
		user.setPermission(resourceResult);
	}
	
	public String getChallenge() {
		return challengeId;
	}
	public String getUsername() {
		return username;
	}
	public String getClerkId() {
		return clerkId;
	}
	public String getPassword() {
		return password;
	}
	public String getToken() {
		return token;
	}
	public String getCode() {
		return code;
	}
	public boolean isActive() {
		boolean isActvie = user==null ? false : !user.isLocked();
		if(!isActvie) {
			code = "1002";
			message = "用户已被禁用";
		}
		return isActvie;
	}
	public boolean isOk() {
		return ok && isActive();
	}
	public String getMessage() {
		return message;
	}
	public SessionInfo getUser() {
		return user;
	}
	
}

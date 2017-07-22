package com.ryx.login;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.ryx.framework.jdbc.dao.impl.BaseDaoImpl;
import com.ryx.framework.utils.MapUtil;
import com.ryx.login.tool.SSOConfig;
import com.ryx.social.retail.util.RetailConfig;

@Component
public class SystemStaticParameters extends BaseDaoImpl implements ApplicationListener<ContextRefreshedEvent>, ServletContextAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SystemStaticParameters.class);
	private boolean isInit;
	
	@Resource
	private Properties serverProperties;
	private static final Map<String, String> servers = new HashMap<String, String>();
	private static String servletContextPath;
	private static String servletRealPath;
	
	public static Map<String, String> getServers() {
		return servers;
	}
	public static String getServer(String serverName) {
		return servers.get(serverName);
	}
	public static String getContextPath() {
		return servletContextPath;
	}
	public static String getRealPath() {
		return servletRealPath;
	}
	
	@Override
	public synchronized void onApplicationEvent(ContextRefreshedEvent event) {
		if(!isInit) {
			// 先改变初始化状态 即使后面程序报错也能保证不再初始化
			isInit = true;
			try {
				// 从数据库里查询服务器地址 赋值到map中
				List<Map<String, Object>> servers = super.selectBySqlQuery("select * from server_config");
				StringBuffer url = new StringBuffer("http://");
				int httpPrefixLength = url.length();
				for(Map<String, Object> server : servers) {
					url.append(MapUtil.getString(server, "ip"));
					url.append(":");
					url.append(MapUtil.getString(server, "port"));
					url.append(MapUtil.getString(server, "root"));
					SystemStaticParameters.servers.put(MapUtil.getString(server, "name"), url.toString());
					// 如果url长度大于"http://" 就将多余的字符删掉
					if(url.length()>=httpPrefixLength) {
						url.delete(httpPrefixLength, url.length());
					}
				}
			} catch (SQLException e) {
				LOGGER.error(" = * = * = * = * = * = * = 获取服务信息错误 = * = * = * = * = * = * = ", e, e);
			}
			RetailConfig.init(SystemStaticParameters.servers);
			RetailConfig.init(serverProperties);
			SSOConfig.init(serverProperties);
		}
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		servletRealPath = servletContext.getRealPath("/");
		servletContextPath = servletContext.getContextPath();
	}

}

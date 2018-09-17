package com.trade.sender.config;

import com.quqian.framework.data.sql.mysql.AbstractDriverConnectionProvider;
import com.quqian.framework.log.Logger;
import com.quqian.framework.resource.InitParameterProvider;
import com.quqian.framework.resource.ResourceAnnotation;
import com.quqian.p2p.variables.P2PConst;
import com.quqian.util.StringHelper;

import java.util.Properties;

@ResourceAnnotation
public class Master extends AbstractDriverConnectionProvider {
	public Master(InitParameterProvider parameterProvider, Logger logger) {
		super(parameterProvider, logger);
//		driver = "com.mysql.jdbc.Driver";
//		url = "jdbc:mysql://localhost:3306/S50";
//		properties = new Properties();
//		properties.setProperty("user", "root");
//		properties.setProperty("password", "123456");
//		properties.setProperty("useUnicode", "true");
//		properties.setProperty("characterEncoding", "UTF8");

		driver = parameterProvider.getInitParameter("driver");
		if (StringHelper.isEmpty(driver)) {
			driver = "com.mysql.jdbc.Driver";
		}
		url = parameterProvider.getInitParameter("jdbcUrl");
		properties = new Properties();
		properties.setProperty("user",
				parameterProvider.getInitParameter("user"));
		properties.setProperty("password",
				parameterProvider.getInitParameter("password"));
		properties.setProperty("useUnicode", "true");
		properties.setProperty("characterEncoding", "UTF8");
	}

	@Override
	public String getName() {
		return P2PConst.DB_MASTER_PROVIDER;
	}
}

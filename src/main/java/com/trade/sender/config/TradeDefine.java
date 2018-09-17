package com.trade.sender.config;

import com.quqian.framework.http.servlet.Rewriter;
import com.quqian.framework.http.session.authentication.AuthenticationException;
import com.quqian.framework.http.session.authentication.PasswordAuthentication;
import com.quqian.framework.service.ServiceSession;
import com.quqian.p2p.common.P2PSystemDefine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

public class TradeDefine extends P2PSystemDefine {

	@Override
	public String getGUID() {
		return null;
	}

	@Override
	public Rewriter getRewriter() {
		return null;
	}

	@Override
	public int readAccountId(ServiceSession serviceSession, String accountName,
	                         String password) throws AuthenticationException, SQLException {
		return 0;
	}

	@Override
	public void writeLog(HttpServletRequest request,
	                     HttpServletResponse response, ServiceSession serviceSession,
	                     PasswordAuthentication authentication, int accountId) {

	}

}

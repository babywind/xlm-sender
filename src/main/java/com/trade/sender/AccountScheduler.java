package com.trade.sender;

import com.quqian.framework.config.ConfigureProvider;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.service.ServiceProvider;
import com.quqian.framework.service.ServiceSession;
import com.trade.sender.service.XlmManage;

public class AccountScheduler extends Thread {

//	protected static int EXPIRES_TOKEN_TIME = 0;
	private final ResourceProvider resourceProvider;
	private final ConfigureProvider configureProvider;
	private final ServiceProvider serviceProvider;
	private transient boolean alive = true;

	public AccountScheduler(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.configureProvider = resourceProvider.getResource(ConfigureProvider.class);
		this.serviceProvider = resourceProvider.getResource(ServiceProvider.class);
	}

	@Override
	public void run() {
		while (alive) {
			try {
				createAccount();

				sleep(1000 * 60);
			} catch (InterruptedException e) {
				alive = false;
				break;
			}
		}
	}

	/**
	 *
	 */
	private void createAccount() {
		try (ServiceSession serviceSession = serviceProvider.createServiceSession()) {
			XlmManage manage = serviceSession.getService(XlmManage.class);
			serviceSession.openTransactions();
			try {
				manage.createAccount();

				serviceSession.commit();
			} catch (Exception e) {
				e.printStackTrace();
				serviceSession.rollback();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			resourceProvider.log(e);
		}
	}
}

package com.trade.sender.scheduler;

import com.quqian.framework.config.ConfigureProvider;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.service.ServiceProvider;
import com.quqian.framework.service.ServiceSession;
import com.trade.sender.entity.TradeInfo;
import com.trade.sender.entity.WithdrawInfo;
import com.trade.sender.service.XlmManage;

/**
 * 提币处理线程
 * @author xy
 */
public class WithdrawScheduler extends Thread {

//	protected static int EXPIRES_TOKEN_TIME = 0;
	private final ResourceProvider resourceProvider;
	private final ConfigureProvider configureProvider;
	private final ServiceProvider serviceProvider;
	private transient boolean alive = true;

	public WithdrawScheduler(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.configureProvider = resourceProvider.getResource(ConfigureProvider.class);
		this.serviceProvider = resourceProvider.getResource(ServiceProvider.class);
	}

	@Override
	public void run() {
		while (alive) {
			try {
				doWithdraw();

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
	private void doWithdraw() {
		try (ServiceSession serviceSession = serviceProvider.createServiceSession()) {
			XlmManage manage = serviceSession.getService(XlmManage.class);
			WithdrawInfo[] infos = manage.getWithdrawInfos();
			if (infos != null) {
				for (WithdrawInfo info : infos) {
					serviceSession.openTransactions();
					try {
						manage.withdraw(info);

						serviceSession.commit();
					} catch (Exception e) {
						e.printStackTrace();
						serviceSession.rollback();
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			resourceProvider.log(e);
		}
	}
}

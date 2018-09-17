package com.trade.sender;

import com.quqian.framework.config.achieve.DefaultConfigureProvider;
import com.quqian.framework.log.Logger;
import com.quqian.framework.resource.ResourceProvider;
import com.quqian.framework.resource.achieve.ResourceInitializer;
import com.quqian.framework.service.achieve.SimpleServiceProvider;
import com.quqian.framework.service.achieve.SimpleServiceProvider.SimpleServiceSession;
import com.trade.sender.config.Master;
import com.trade.sender.config.TradeDefine;
import com.trade.sender.scheduler.AccountScheduler;
import com.trade.sender.scheduler.TradeScheduler;
import com.trade.sender.scheduler.WithdrawScheduler;
import com.trade.sender.service.achieve.XlmManageImpl.XlmManageFactory;

import java.util.HashSet;
import java.util.Set;

public class Sender {

	public static void main(String... args) {
		Set<Class<?>> classes = new HashSet<>();
		classes.add(TradeDefine.class);
		classes.add(Master.class);
		classes.add(SimpleServiceProvider.class);
		classes.add(SimpleServiceSession.class);
		classes.add(DefaultConfigureProvider.class);

		classes.add(XlmManageFactory.class);

		ResourceProvider resourceProvider = new ResourceInitializer().initialize(classes, new Logger() {
			@Override
			public void log(String message) {
				System.out.println(message);
			}

			@Override
			public void log(Throwable throwable) {
				throwable.printStackTrace();
			}
		}, args);

		// 用户临时钱包创建
		new AccountScheduler(resourceProvider).start();

		// 接币处理线程
		new TradeScheduler(resourceProvider).start();

		// 提币处理线程
		new WithdrawScheduler(resourceProvider).start();
	}
}

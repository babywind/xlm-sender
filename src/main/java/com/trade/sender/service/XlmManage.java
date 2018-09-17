package com.trade.sender.service;

import com.quqian.framework.service.Service;
import com.trade.sender.entity.TradeInfo;

/**
 * @author xy
 */
public interface XlmManage extends Service {

	/**
	 * XLM转入热钱包
	 *
	 * @param l 用户转账记录
	 * @throws Throwable
	 */
	void transToHotWallet(TradeInfo l) throws Throwable;

	/**
	 * XLM转入冷钱包
	 *
	 * @throws Throwable
	 */
	void transToColdWallet() throws Throwable;

	/**
	 * 获取XLM转账记录
	 *
	 * @return TradeInfo[]
	 * @throws Throwable
	 */
	TradeInfo[] getTradeInfos() throws Throwable;

	/**
	 *
	 * @throws Exception
	 */
	void createAccount () throws Exception;
}

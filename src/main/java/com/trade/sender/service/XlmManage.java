package com.trade.sender.service;

import com.quqian.framework.service.Service;
import com.trade.sender.entity.TradeInfo;
import com.trade.sender.entity.WithdrawInfo;

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
	 * 创建用户临时钱包
	 *
	 * @throws Exception
	 */
	void createAccount () throws Exception;

	/**
	 * 获取用户提币信息
	 * @return
	 * @throws Exception
	 */
	WithdrawInfo[] getWithdrawInfos () throws Exception;

	/**
	 * 用户提币处理
	 * @param info
	 * @throws Exception
	 */
	void withdraw(WithdrawInfo info) throws Exception;
}

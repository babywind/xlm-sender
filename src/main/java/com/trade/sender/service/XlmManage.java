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
	 * @param tradeInfo 用户余额同步记录
	 * @throws Exception
	 */
	void transToHotWallet(TradeInfo tradeInfo) throws Exception;

	/**
	 * XLM转入冷钱包
	 *
	 * @throws Exception
	 */
	void transToColdWallet() throws Exception;

	/**
	 * 获取XLM余额同步记录
	 *
	 * @return TradeInfo[] 余额同步申请记录
	 * @throws Exception
	 */
	TradeInfo[] getTradeInfos() throws Exception;

	/**
	 * 创建用户临时钱包
	 *
	 * @throws Exception
	 */
	void createAccount () throws Exception;

	/**
	 * 获取用户提币信息
	 * @return 提币信息
	 * @throws Exception
	 */
	WithdrawInfo[] getWithdrawInfos () throws Exception;

	/**
	 * 用户提币处理
	 * @param info 提币信息
	 * @throws Exception
	 */
	void withdraw(WithdrawInfo info) throws Exception;
}

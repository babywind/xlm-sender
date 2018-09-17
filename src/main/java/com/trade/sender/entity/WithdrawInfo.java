package com.trade.sender.entity;

import com.quqian.p2p.common.enums.IsPass;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 用户提币信息
 *
 * @author xy
 */
public class WithdrawInfo {

	/**
	 * id
	 */
	public long id;

	/**
	 * 用户ID
	 */
	public long userId;

	/**
	 * 币ID
	 */
	public int bid;

	/**
	 * 转出数量
	 */
	public BigDecimal amount = new BigDecimal(0);
	/**
	 * 提现手续费
	 */
	public BigDecimal charge = new BigDecimal(0);

	/**
	 * 转出地址
	 */
	public String address;

	/**
	 * 状态
	 */
	public IsPass state;
	/**
	 * 转出时间
	 */
	public Timestamp tradeTime;

	/**
	 * 申请时间
	 */
	public Timestamp applyTime;

	/**
	 * 审核时间
	 */
	public Timestamp auditTime;
}

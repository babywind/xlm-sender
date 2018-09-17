package com.trade.sender.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class TradeInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 自增id
	 */
	public long id;

	/**
	 * 用户id
	 */
	public long userId;

	/**
	 * 用户钱包地址
	 */
	public String address;

	/**
	 * 用户私钥
	 */
	public String privateKey;

	/**
	 * 转入金额
	 */
	public BigDecimal amount = new BigDecimal(0);

	/**
	 * T6012_id 关联
	 */
	public long relaId;

}
package com.trade.sender.entity;

import com.quqian.p2p.common.enums.IsPass;

import java.io.Serializable;
import java.math.BigDecimal;

public class WalletEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 热钱包地址
	 */
	public String hwAddress;

	/**
	 * 热钱包私钥
	 */
	public String hwPrivateKey;

	/**
	 * 冷钱包地址
	 */
	public String cwAddress;

	/**
	 * 是否自动转入冷钱包
	 */
	public IsPass is;

	/**
	 * 大于多少自动转入
	 */
	public BigDecimal limit = new BigDecimal(0);

	/**
	 * 钱包地址
	 */
	public String address;

	/**
	 * 钱包私钥
	 */
	public String privateKey;

	/**
	 * 钱包服务器ip
	 */
	public String ip;

	/**
	 * 端口
	 */
	public String port;

	/**
	 * 服务器用户名
	 */
	public String serverName;

	/**
	 * 服务器密码
	 */
	public String serverPasswd;
}

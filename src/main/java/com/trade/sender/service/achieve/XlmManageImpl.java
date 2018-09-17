package com.trade.sender.service.achieve;

import com.quqian.framework.service.ServiceFactory;
import com.quqian.framework.service.ServiceResource;
import com.quqian.p2p.common.enums.IsPass;
import com.quqian.p2p.common.enums.XlbType;
import com.quqian.p2p.variables.P2PConst;
import com.quqian.util.MyCrypt;
import com.quqian.util.StringHelper;
import com.quqian.util.parser.BigDecimalParser;
import com.quqian.util.parser.EnumParser;
import com.trade.sender.entity.TradeInfo;
import com.trade.sender.entity.WalletEntity;
import com.trade.sender.entity.WithdrawInfo;
import com.trade.sender.service.AbstractManageService;
import com.trade.sender.service.XlmManage;
import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * @author xy
 */
public class XlmManageImpl extends AbstractManageService implements XlmManage {
	// 币简称
	final private String BJC = "XLM";
	// 币ID
	final private int BID = selectId(BJC);
	// 交易手续费XLM
	final static private BigDecimal CHANGE_FEE = new BigDecimal(0.00001);
	// 账户最小余额XLM
	final static private BigDecimal MIN_BALANCE = new BigDecimal(1.00000000);

	enum TRADE {TRANS_IN, TRANS_OUT}

	// stellar(XLM)服务
	private Server xlmServer;

	private XlmManageImpl(ServiceResource serviceResource) {
		super(serviceResource);

		Network.usePublicNetwork();

		try {
			WalletEntity xlmWallet = this.getWalletInfo();

			xlmServer = new Server(xlmWallet.ip);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public static class XlmManageFactory implements ServiceFactory<XlmManage> {
		@Override
		public XlmManage newInstance(ServiceResource serviceResource) {
			return new XlmManageImpl(serviceResource);
		}
	}

	private WalletEntity getWalletInfo() throws Throwable {
		return select(getConnection(P2PConst.DB_USER), (re) -> {
			WalletEntity b = new WalletEntity();
			while (re.next()) {
				b.hwAddress = re.getString(1);
				b.hwPrivateKey = re.getString(2);
				b.cwAddress = re.getString(3);
				b.limit = re.getBigDecimal(4);
				b.is = EnumParser.parse(IsPass.class, re.getString(5));
				b.ip = re.getString(6);
			}
			return b;
		}, "SELECT F12,F13,F14,F16,F15,F18 FROM T6013 WHERE F01=?", BID);
	}

	@Override
	public void transToHotWallet(TradeInfo trade) throws Throwable {
		String hash = "";
		String result = "";
		try {
			WalletEntity wallet = getWalletInfo();

			KeyPair source = KeyPair.fromSecretSeed(MyCrypt.myDecode(trade.privateKey));
			KeyPair dest = KeyPair.fromAccountId(wallet.hwAddress);

			// 查询用户钱包信息
			AccountResponse sourceAccount = xlmServer.accounts().account(source);
			// 用户钱包余额
			BigDecimal balance = getXlmBalance(sourceAccount);
			// 可以转入余额 = 用户余额 - 手续费 - 账户最小余额
			BigDecimal canTransBalance = balance.subtract(CHANGE_FEE).subtract(MIN_BALANCE);

			if (canTransBalance.compareTo(trade.amount) < 0) {
				throw new Exception("账户余额不足！");
			}

			DecimalFormat xlmFormat = new DecimalFormat("#.########");
			// 转入金额
			String transAmount = xlmFormat.format(trade.amount);

			// 构建转账事务
			Transaction transaction = new Transaction.Builder(sourceAccount)
					.addOperation(new PaymentOperation.Builder(dest, new AssetTypeNative(), transAmount).build())
					.build();
			// 事务签名
			transaction.sign(source);
			// 提交事务
			SubmitTransactionResponse transactionResponse = xlmServer.submitTransaction(transaction);
			if (transactionResponse.isSuccess()) {
				hash = transactionResponse.getHash();
				result = transactionResponse.getResultXdr();
			} else {
				result = "转入热钱包失败：" + transactionResponse.getResultXdr();
			}
		} catch (Exception e) {
			result = "转入热钱包错误：" + e.getMessage();
		}

		execute(getConnection(P2PConst.DB_USER), "UPDATE  T6012_3 SET F05=?,F06=CURRENT_TIMESTAMP(),F08=?,F09=? WHERE F01=?",
				IsPass.S, hash, 0, trade.id);
		execute(getConnection(P2PConst.DB_USER), "UPDATE  T6012_4 SET F03=? WHERE F01=?", result, trade.id);

	}

	@Override
	public void transToColdWallet() throws Throwable {
		WalletEntity wallet = getWalletInfo();
		if (IsPass.S == wallet.is && !StringHelper.isEmpty(wallet.cwAddress)) {
			KeyPair hotKP = KeyPair.fromSecretSeed(MyCrypt.myDecode(wallet.hwPrivateKey));
			KeyPair coldKP = KeyPair.fromAccountId(wallet.cwAddress);

			// 获取钱包信息 校验钱包是否存在
			AccountResponse hotWallet = xlmServer.accounts().account(hotKP);
			AccountResponse coldeWallet = xlmServer.accounts().account(coldKP);

			BigDecimal balance = getXlmBalance(hotWallet);

			BigDecimal canTransAmount = balance.subtract(CHANGE_FEE).subtract(MIN_BALANCE);
			if (canTransAmount.compareTo(wallet.limit) > 0) {

				DecimalFormat xlmFormat = new DecimalFormat("#.########");
				// 转入金额
				String transAmount = xlmFormat.format(canTransAmount);

				// 构建转账事务
				Transaction transaction = new Transaction.Builder(hotWallet)
						.addOperation(new PaymentOperation.Builder(coldKP, new AssetTypeNative(), transAmount).build())
						.build();
				// 事务签名
				transaction.sign(hotKP);
				// 提交事务
				SubmitTransactionResponse transactionResponse = xlmServer.submitTransaction(transaction);
				if (!transactionResponse.isSuccess()) {
					throw new Exception("转入冷钱包错误：" + transactionResponse.getResultXdr());
				}
			}
		}
	}

	@Override
	public TradeInfo[] getTradeInfos() throws Throwable {
		return selectAll(getConnection(P2PConst.DB_USER), (re) -> {
			ArrayList<TradeInfo> list = new ArrayList<>();

			while (re.next()) {
				TradeInfo s = new TradeInfo();
				s.id = re.getLong(1);
				s.amount = re.getBigDecimal(2);
				s.address = re.getString(3);
				s.privateKey = re.getString(4);
				s.relaId = re.getLong(5);
				s.userId = re.getLong(6);
				list.add(s);
			}
			return list.size() == 0 ? null : list.toArray(new TradeInfo[list.size()]);
		}, "SELECT T6012_3.F01,T6012_3.F04,T6.F03,T6.F04,T6.F01,T6.F02 FROM T6012_3 LEFT JOIN T6012_" + BJC
				+ " AS T6 ON T6.F01=T6012_3.F07 WHERE T6012_3.F05=? AND T6012_3.F10=? ", IsPass.F, BID);
	}

	@Override
	public void createAccount() throws Exception {

	}

	@Override
	public WithdrawInfo[] getWithdrawInfos() throws Exception {
		return new WithdrawInfo[0];
	}

	@Override
	public void withdraw(WithdrawInfo info) throws Exception {

	}

	/**
	 * get account xlm balance
	 *
	 * @param account AccountResponse
	 * @return xlm balance
	 */
	private BigDecimal getXlmBalance(AccountResponse account) {
		BigDecimal bal = new BigDecimal(0.00000000);

		for (AccountResponse.Balance b : account.getBalances()) {
			String type = b.getAssetType();
			if ("native".equals(type)) {
				bal = BigDecimalParser.parse(b.getBalance());
				break;
			}
		}

		return bal;
	}

	/**
	 * 更新并记录用户资产变动
	 *
	 * @param userId   用户ID
	 * @param bid      数字币ID
	 * @param amount   金额
	 * @param charge   手续费
	 * @param transTag TRANS_IN：转入平台(充值) TRANS_OUT：转出平台(提现)
	 * @throws Exception SQL EXCEPTION
	 */
	private void updateUserAsset(long userId, int bid, long amount, long charge, TRADE transTag) throws Exception {
		BigDecimal userBalance = selectBigDecimal(getConnection(P2PConst.DB_USER),
				"SELECT F04 + F05 FROM T6025 WHERE F02 = ? AND F03 = ? FOR UPDATE ", userId, bid
		);

		String tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		if (transTag == TRADE.TRANS_IN) {
			// 更新用户资产
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6025 (F02, F03, F04) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE F04 = F04 + ? ",
					userId, bid, amount, amount
			);

			// 用户虚拟币交易记
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6026 SET F02=?,F03=?,F04=?,F05=CURRENT_TIMESTAMP(),F06=?,F08=?,F09=?,F10=? ",
					userId, bid, XlbType.ZR, amount, "", userBalance, userBalance.add(new BigDecimal(amount))
			);

			// 转入到账记录
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6027 SET F02=?,F03=?,F04=?,F05=?,F06=?,F07=CURRENT_TIMESTAMP(),F08=?,F09=?,F10=?",
					userId, bid, amount, amount, "", "", IsPass.S, ""
			);

			String content = String.format("尊敬的用户，您于%s转入%s枚%d，感谢您的使用。", tradeTime, BJC, amount);
			sms(userId, "转入ONT", content);
		} else {
			// 更新用户资产
			execute(getConnection(P2PConst.DB_USER),
					"UPDATE T6025 SET F05 = F05 - ? WHERE F02 = ? AND F03 = ?",
					amount + charge, userId, bid
			);

			// 用户虚拟币交易记(提币数)
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6026 SET F02=?, F03=?, F04=?, F05=CURRENT_TIMESTAMP(), F06=?, F08=?, F09=?, F10=? ",
					userId, bid, XlbType.ZC, amount, "", userBalance, userBalance.subtract(new BigDecimal(amount))
			);

			// 用户虚拟币交易记(提币手续费)
			if (charge > 0) {
				execute(getConnection(P2PConst.DB_USER),
						"INSERT INTO T6026 SET F02=?, F03=?, F04=?, F05=CURRENT_TIMESTAMP(), F06=?, F08=?, F09=?, F10=? ",
						userId, bid, XlbType.TBSXF, charge, "", userBalance, userBalance.subtract(new BigDecimal(charge))
				);
			}

			String content = String.format("尊敬的用户，您于%s转出%s枚%d，感谢您的使用。", tradeTime, BJC, amount);
			sms(userId, "提币成功", content);
		}

	}

	private void sms (long userId, String title, String content) throws Exception {
		execute(getConnection(P2PConst.DB_USER),
				"INSERT INTO T6100 SET F02=?,F03=?,F04=?,F05=?,F06=CURRENT_TIMESTAMP()",
				userId, title, content, "WD");
	}

}
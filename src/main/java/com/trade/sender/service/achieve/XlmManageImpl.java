package com.trade.sender.service.achieve;

import com.quqian.framework.service.ServiceFactory;
import com.quqian.framework.service.ServiceResource;
import com.quqian.p2p.common.enums.IsPass;
import com.quqian.p2p.variables.P2PConst;
import com.quqian.util.MyCrypt;
import com.quqian.util.StringHelper;
import com.quqian.util.parser.BigDecimalParser;
import com.quqian.util.parser.EnumParser;
import com.trade.sender.entity.TradeInfo;
import com.trade.sender.entity.WalletEntity;
import com.trade.sender.service.AbstractManageService;
import com.trade.sender.service.XlmManage;
import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;


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

	enum TRADE { TRANS_IN,TRANS_OUT }

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
				b.r_qbdz = re.getString(1);
				b.r_sy = re.getString(2);
				b.l_qbdz = re.getString(3);
				b.count = re.getBigDecimal(4);
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
			KeyPair dest = KeyPair.fromAccountId(wallet.r_qbdz);

			// 查询用户钱包信息
			AccountResponse sourceAccount = xlmServer.accounts().account(source);
			// 用户钱包余额
			BigDecimal balance = getXlmBalance(sourceAccount);
			// 可以转入余额 = 用户余额 - 手续费 - 账户最小余额
			BigDecimal canTransBalance =  balance.subtract(CHANGE_FEE).subtract(MIN_BALANCE);

			if (canTransBalance.compareTo(trade.amount) < 0) {
				throw new Exception("账户余额不足！");
			}

			DecimalFormat xlmFormat = new DecimalFormat("#.########");
			// 转入金额
			String transAmount = xlmFormat.format(trade.amount) ;

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
		if (wallet.is != null && wallet.is == IsPass.S && !StringHelper.isEmpty(wallet.l_qbdz)) {
			KeyPair hotKP = KeyPair.fromSecretSeed(MyCrypt.myDecode(wallet.r_sy));
			KeyPair coldKP = KeyPair.fromAccountId(wallet.l_qbdz);

			// 获取钱包信息 校验钱包是否存在
			AccountResponse hotWallet = xlmServer.accounts().account(hotKP);
			AccountResponse coldeWallet = xlmServer.accounts().account(coldKP);

			BigDecimal balance = getXlmBalance(hotWallet);

			BigDecimal canTransAmount = balance.subtract(CHANGE_FEE).subtract(MIN_BALANCE);
			if (canTransAmount.compareTo(wallet.count) > 0) {

				DecimalFormat xlmFormat = new DecimalFormat("#.########");
				// 转入金额
				String transAmount = xlmFormat.format(canTransAmount) ;

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

	/**
	 * get account xlm balance
	 * @param account
	 * @return xlm balance
	 */
	private BigDecimal getXlmBalance (AccountResponse account) {
		BigDecimal bal = new BigDecimal(0.00000000);

		for (AccountResponse.Balance b: account.getBalances()) {
			String type = b.getAssetType();
			if ("native".equals(type)) {
				bal = BigDecimalParser.parse(b.getBalance());
				break;
			}
		}

		return bal;
	}
}
import com.quqian.util.StringHelper;
import org.junit.Before;
import org.junit.Test;
import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * test net:
 * https://horizon-testnet.stellar.org
 *
 * public net:
 * https://horizon.stellar.org
 */

public class TestDome {
	@Before
	public void testBefore() {
		Network.useTestNetwork();
//		Network.usePublicNetwork();
	}

	@Test
	public void testTx() throws IOException {
		Server server = new Server("https://horizon-testnet.stellar.org");

//		KeyPair source = KeyPair.fromSecretSeed("SAZID7YMR6NYF5UEJ64SZVCOLUVTR7TPJUB3YFUU2GXHHCUKNAO3C6P2");
		KeyPair source = KeyPair.fromSecretSeed("SCZL7CNIXO5M66MQJISVKOMJFO3RTQDIYEH2HNLF7HHYHKB3LUZKQCKE");
//		KeyPair destination = KeyPair.fromAccountId("GA2C5RFPE6GCKMY3US5PAB6UZLKIGSPIUKSLRB6Q723BM2OARMDUYEJ5");
//		KeyPair destination = KeyPair.fromSecretSeed("SAZID7YMR6NYF5UEJ64SZVCOLUVTR7TPJUB3YFUU2GXHHCUKNAO3C6P2");
		KeyPair destination = KeyPair.random();

// First, check to make sure that the destination account exists.
// You could skip this, but if the account does not exist, you will be charged the transaction fee when the transaction fails.
// It will throw HttpResponseException if account does not exist or there was another error.
		AccountResponse dest = server.accounts().account(destination);
		System.out.println("d balance:" + dest.getBalances()[0].getBalance());
// If there was no error, load up-to-date information on your account.
		AccountResponse sourceAccount = server.accounts().account(source);
//		System.out.println( sourceAccount.getKeypair().getSecretSeed() );
		System.out.println("sour address:" + sourceAccount.getKeypair().getAccountId());
		System.out.println("sour balance:" + sourceAccount.getBalances()[0].getBalance());

// Start building the transaction.
		Transaction transaction = new Transaction.Builder(sourceAccount)
				.addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "10").build())
				// A memo allows you to add your own metadata to a transaction. It's
				// optional and does not affect how Stellar treats the transaction.
				.addMemo(Memo.text("Test Transaction"))
				.build();
// Sign the transaction to prove you are actually the person sending it.
		transaction.sign(source);
//		transaction.sign(sourceAccount.getKeypair());

// And finally, send it off to Stellar!
		try {
			SubmitTransactionResponse response = server.submitTransaction(transaction);
			System.out.println("is Success:"+response.isSuccess());
			if ( response.isSuccess() ) {
                System.out.println(response.getHash());

                AccountResponse destAccount = server.accounts().account(destination);
                System.out.println("desc address:" + destAccount.getKeypair().getAccountId());
                System.out.println("desc balance:" + destAccount.getBalances()[0].getBalance());

            } else {
                System.out.println(response.getExtras().getResultCodes().getTransactionResultCode());
            }
		} catch (Exception e) {
			System.out.println("Something went wrong!");
			System.out.println(e.getMessage());
			// If the result is unknown (no response body, timeout etc.) we simply resubmit
			// already built transaction:
			// SubmitTransactionResponse response = server.submitTransaction(transaction);
		}


	}

	@Test
	public void testAcct () throws IOException {
		Server server = new Server("https://horizon-testnet.stellar.org");

		KeyPair source = KeyPair.fromSecretSeed("SCZANGBA5YHTNYVVV4C3U252E2B6P6F5T3U6MM63WBSBZATAQI3EBTQ4");
		KeyPair destination = KeyPair.fromAccountId("GA2C5RFPE6GCKMY3US5PAB6UZLKIGSPIUKSLRB6Q723BM2OARMDUYEJ5");

		AccountResponse d = server.accounts().account(destination);
		AccountResponse s = server.accounts().account(source);

		System.out.println("d balance:" + d.getBalances()[0].getBalance());
		System.out.println("d code:" + d.getBalances()[0].getAssetCode());
		System.out.println("d type:" + d.getBalances()[0].getAssetType());

		System.out.println("s balance:" + s.getBalances()[0].getBalance());
		System.out.println("s code:" + s.getBalances()[0].getAssetCode());
		System.out.println("s type:" + s.getBalances()[0].getAssetType());
	}

	@Test
	public void createAcct() throws IOException {
		String url = "https://horizon-testnet.stellar.org";
//		String url = "http://localhost:8000";
		Server server = new Server(url);

		KeyPair newKey = KeyPair.random();
		String address = newKey.getAccountId();
		String privateKey = String.valueOf(newKey.getSecretSeed());
		System.out.println("account id:" + address);
//		System.out.println("pk:" + newKey.getXdrSignerKey().toString());
		System.out.println("secret seed:" + privateKey);

//		AccountResponse newAcct = server.accounts().account(newKey);
//
//		System.out.println(newAcct.getKeypair().getAccountId());
//		System.out.println(newAcct.getKeypair().getXdrPublicKey().toString());
//		System.out.println(newAcct.getKeypair().getXdrSignerKey().toString());

		KeyPair myKey = KeyPair.fromSecretSeed(privateKey);

//		String friendbotUrl = String.format( "https://friendbot.stellar.org/?addr=%s", myKey.getAccountId());

//		String friendbotUrl = String.format( "https://horizon.stellar.org/?addr=%s", myKey.getAccountId());
		String friendbotUrl = String.format(url+ "/friendbot?addr=%s", myKey.getAccountId());

		System.out.println(friendbotUrl);

		InputStream response = new URL(friendbotUrl).openStream();
		String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
		System.out.println("SUCCESS! You have a new account :)\n" + body);

		AccountResponse account = server.accounts().account(myKey);
		System.out.println("Balances for account :" + myKey.getAccountId());
		// 创建测试钱包初始有10000xlm
		for (AccountResponse.Balance balance : account.getBalances()) {
			System.out.println(String.format(
					"Type: %s, Code: %s, Balance: %s",
					balance.getAssetType(),
					balance.getAssetCode(),
					balance.getBalance()));
		}
	}

	@Test
    public void createNewAccount () throws IOException {
        Server server = new Server("https://horizon-testnet.stellar.org");

        KeyPair source = KeyPair.fromSecretSeed("SCZL7CNIXO5M66MQJISVKOMJFO3RTQDIYEH2HNLF7HHYHKB3LUZKQCKE");
        KeyPair destination = KeyPair.random();

        AccountResponse sourceAccount = server.accounts().account(source);
        System.out.println("sour address:" + sourceAccount.getKeypair().getAccountId());
        System.out.println("sour balance:" + sourceAccount.getBalances()[0].getBalance());

        Transaction transaction = new Transaction.Builder(sourceAccount)
                .addOperation(new CreateAccountOperation.Builder(destination, "10").build())
                .addMemo(Memo.text("Create new account."))
                .build();
        transaction.sign(source);

        SubmitTransactionResponse response = server.submitTransaction(transaction);
        System.out.println("is Success:"+response.isSuccess());
        if ( response.isSuccess() ) {
            System.out.println(response.getHash());

            AccountResponse destAccount = server.accounts().account(destination);
            System.out.println("desc address:" + destAccount.getKeypair().getAccountId());
            System.out.println("desc balance:" + destAccount.getBalances()[0].getBalance());

        } else {
            System.out.println(response.getExtras().getResultCodes().getTransactionResultCode());
        }
    }
}

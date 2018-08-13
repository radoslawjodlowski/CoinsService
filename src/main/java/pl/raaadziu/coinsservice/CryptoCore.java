package pl.raaadziu.coinsservice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import pl.raaadziu.coinsservice.DTOs.*;

class CryptoCore {

    private Wallet wallet;
    private CryptoSqlApi api;
    private String accountName;
    private Integer actConnections;
    private CryptoData cd = new CryptoData();
    private Logger logger;

    private Instant nextTimeToRunFullTasks = Instant.now();
    private Instant nextTimeToCheckEmptyAddresses = Instant.now();
    private Integer checkAddressesPeriod = 10; //seconds

    private ArrayList<Transaction> newTransactionsBuffer = new ArrayList<>();

    CryptoCore(Wallet wallet, String accountName, CryptoSqlApi api)
    {
        this.wallet = wallet;
        this.api = api;
        this.accountName = accountName;
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger("CryptoCore" + wallet.getCID());
        logger.setLevel(Level.DEBUG);
        logger.info("Initialized CryptoCore on [" + wallet.getCID() + "] with account filter [" + accountName + "]");
    }

    private void checkInfo() throws SqlApiException
    {
        cd = api.getCryptoCoreData();
    }

    private void checkWallet() throws WalletException
    {
        Map<String,Object> info = wallet.getNetworkInfo();
        actConnections = (Integer) info.get("connections");
        logger.debug("Have " + actConnections + " connections to network wallet");
    }

    private void scanForNewTransactionsAndMoveToHot() throws WalletException, SqlApiException
    {
        TransactionsAndHash tnh = wallet.getTxSinceBlock(cd.getBlockHash(), accountName);
        for(int i = 0; i < tnh.getTransactions().size(); i++)
        {
            Transaction t = tnh.getTransactions().get(i);
            if (t.getCategory().equals("receive"))
            {
                if (!(newTransactionsBuffer.contains(t)))
                {
                    newTransactionsBuffer.add(t);
                    api.addHotInputTransaction(t.getAddress(), t.getTxId(), t.getAmount());
                    logger.info("Added IN transaction to tracking, " + t.getTxId());
                }
            }
        }

        Integer actBlockNumber = wallet.getBlockNumber(tnh.getHash());
        
        if (actBlockNumber > cd.getBlockNumber())
        {
            api.setBlockPoint(tnh.getHash(), actBlockNumber, actConnections);
            logger.info("Increased block number from " + cd.getBlockHash() + " to " + actBlockNumber);
            newTransactionsBuffer.clear();
            cd.setBlockHash(tnh.getHash());
            cd.setBlockNumber(actBlockNumber);
        }
    }

    private void trackHotInputTransactions() throws WalletException, SqlApiException
    {
        ArrayList<TrackedTransaction> trackedInputTransactions = api.getTrackingInputTransactions();
        Boolean go = false;
        for (TrackedTransaction t : trackedInputTransactions) {
            Integer confirmationsNumber = wallet.getConfirmationsNumber(t.getTxId());
            if (confirmationsNumber >= cd.getDepositConfirmations()) go = true;
            if (confirmationsNumber > t.getConfirmations()) {
                api.updateHotInputTransaction(t.getId(), confirmationsNumber, cd.getDepositConfirmations());
                logger.debug("(IN) Changed confirmation index to " + confirmationsNumber + " on TxId:" + t.getTxId());
            }
        }
        if (go)
        {
            api.runHotTransactions();
            logger.info("HOT procedure was launched");
        }
    }

    private void checkAndSendSignedTransaction() throws WalletException, SqlApiException, CryptoCoreException
    {
        ArrayList<SignedTransaction> st =  api.getSignedTransactions();
        if (st.isEmpty()) return;
        if (st.size() > 1) throw new CryptoCoreException("FATAL: The number of signed transactions is greater than 1 on " + wallet.getCID());

        SignedTransaction sst = st.get(0); // get first=one transaction
        wallet.sendRawTransaction(sst.getHex());
        logger.info("Sent signed transaction to network with TxID:" + sst.getId());
        api.commitSentTransactionStatus(sst.getId());
        logger.info(" and committed to database");
    }

    private BigDecimal calculateNetworkFee(String hexEncodedTx, BigDecimal feePerKB)
    {
        BigDecimal fee = new BigDecimal(hexEncodedTx.length());
        fee = fee.multiply(feePerKB);
        fee = fee.divide(new BigDecimal("2000"),8,RoundingMode.UP);
        return fee;
    }

    private void makeCommonPayments() throws WalletException, SqlApiException
    {
        ArrayList<TransactionToCommit> ttc =  api.getOutputTransactionsToCommit();
        ArrayList<Payment> payments = new ArrayList<>();
        if (ttc.isEmpty()) return;
        logger.info("(START) Making common payments");
        // TODO reduce TransactionToCommit & Payment to one object
        for (TransactionToCommit t : ttc) {
            Payment payment = new Payment(t.getAddress(), t.getAmount());
            payments.add(payment);
            logger.info(" add element " + payment);
        }
        NewTransaction newTransaction = wallet.makeCommonPaymentsTransaction(payments, cd.getNetworkFee(), accountName);
        BigDecimal fee = calculateNetworkFee(newTransaction.getHex(),cd.getNetworkFee());
        logger.info(" calculated network fee is " + fee);
        newTransaction = wallet.makeCommonPaymentsTransaction(payments, fee, accountName);
        api.addSignedTransaction(ttc.get(0).getSignId(), newTransaction.getHex(), newTransaction.getTxID());
    }

    private void trackHotOutputTransactions() throws WalletException, SqlApiException
    {
        ArrayList<TrackedTransaction> tt = api.getTrackingOutputTransactions();
        for (TrackedTransaction t : tt) {
            Integer confirmations = wallet.getConfirmationsNumber(t.getTxId());
            if (confirmations > t.getConfirmations()) {
                api.updateHotOutputTransaction(t.getId(), confirmations, cd.getDepositConfirmations());
                logger.debug("(OUT) Changed confirmation index to " + confirmations + " on TxId:" + t.getTxId());
            }
        }
    }

    private void fillEmptyAddresses() throws WalletException, SqlApiException
    {
        ArrayList<Integer> users =  api.getEmptyAddresses();
        for (Integer user : users) {
            String newAddress = wallet.getNewAddressByAccount(accountName);
            api.fillEmptyInputAddress(user, newAddress);
            logger.info("Add new address " + newAddress + " to user with id:" + user);
        }
    }

    void tryDoTasks()
    {
        Instant nowTime = Instant.now();
        if (nowTime.isAfter(nextTimeToRunFullTasks))
        {
            fullTasks();
            nextTimeToCheckEmptyAddresses = nextTimeToCheckEmptyAddresses.plusSeconds(checkAddressesPeriod);
        }
        if (nowTime.isAfter(nextTimeToCheckEmptyAddresses)) checkAddressesTask();
    }

    private void checkAddressesTask()
    {
        try{
            checkInfo();
            if (cd.isTryDoTasks()) fillEmptyAddresses();
        }catch(WalletException e)
        {
            logger.error("TASK FAILED due WalletException " + e);
        }catch(SqlApiException e)
        {
            logger.error("TASK FAILED due SqlApiException " + e);
        }
        nextTimeToCheckEmptyAddresses = nextTimeToCheckEmptyAddresses.plusSeconds(checkAddressesPeriod);
    }

    private void fullTasks()
    {
        try{
            checkInfo();
            if (cd.isTryDoTasks())
            {
                checkWallet();
                fillEmptyAddresses();
                scanForNewTransactionsAndMoveToHot();
                trackHotInputTransactions();
                checkAndSendSignedTransaction();
                makeCommonPayments();
                trackHotOutputTransactions();
                logger.info("Tasks done");
            }
        }catch(WalletException e)
        {
            logger.error("TASK FAILED due WalletException " + e);
        }catch(SqlApiException e)
        {
            logger.error("TASK FAILED due SqlApiException " + e);
        }catch(CryptoCoreException e)
        {
            logger.error("TASK FAILED due CryptoCoreException " + e);
        }
        nextTimeToRunFullTasks = nextTimeToRunFullTasks.plusSeconds(cd.getScanPeriod());
    }

}

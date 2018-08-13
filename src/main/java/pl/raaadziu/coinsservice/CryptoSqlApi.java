package pl.raaadziu.coinsservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pl.raaadziu.coinsservice.DTOs.CryptoConfig;
import pl.raaadziu.coinsservice.DTOs.CryptoData;
import pl.raaadziu.coinsservice.DTOs.SignedTransaction;
import pl.raaadziu.coinsservice.DTOs.TrackedTransaction;
import pl.raaadziu.coinsservice.DTOs.TransactionToCommit;


class CryptoSqlApi{

    private SqlApi sql;
    private String CID;

    CryptoSqlApi(SqlApi a, String cid)
    {
        sql = a;
        CID = cid;
    }

    static List<CryptoConfig> getCryptoConfig(SqlApi sql) throws SqlApiException
    {
        ArrayList<CryptoConfig> data = new ArrayList<>();
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_CONFIG_GET]");
        if (ll.isEmpty()) throw new SqlApiException("Crypto config is empty");
        for (Map<String, Object> o : ll) {
            CryptoConfig c = new CryptoConfig(o);
            data.add(c);
        }
        return data;
    }

    CryptoData getCryptoCoreData() throws SqlApiException
    {
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_CORE_GET] @CID='" + CID + "'");
        if (ll.size() != 1) throw new SqlApiException("Crypto data is empty");
        return new CryptoData(ll.get(0));
    }

    void setBlockPoint(String blockHash, Integer blockNumber, Integer connections) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_POINT_SET] @CID='" + CID + "', @nr='" + blockNumber + "', @hash='" + blockHash + "', @connections='" + connections + "';");
    }

    void addHotInputTransaction(String address, String TxId, BigDecimal amount) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_ADD_HOT] @CID='" + CID + "', @txid='" + TxId + "', @address='" + address + "', @amount='" + amount + "';");
    }

    void updateHotInputTransaction(Integer id, Integer confirmationsCount, Integer requiredConfirmations) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_HOT_IN_update] @id='" + id + "', @conf='" + confirmationsCount + "', @reqConf='" + requiredConfirmations + "';");
    }

    void updateHotOutputTransaction(Integer id, Integer confirmationsCount, Integer requiredConfirmations) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_HOT_OUT_update] @id='" + id + "', @conf='" + confirmationsCount + "', @reqConf='" + requiredConfirmations + "';");
    }

    void runHotTransactions() throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_HOT_run] @CID='" + CID + "';");
    }

    ArrayList<SignedTransaction> getSignedTransactions() throws SqlApiException
    {
        ArrayList<SignedTransaction> list = new ArrayList<>();
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_GET_TX_TO_SEND] @CID='" + CID + "'");
        for (Map<String, Object> o : ll)
        {
            list.add(new SignedTransaction((String) o.get("raw"), (Integer) o.get("id")));
        }
        return list;
    }

    void commitSentTransactionStatus(Integer id) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_COMMIT_TX_STATUS] @id='" + id + "';");
    }

    void addSignedTransaction(Integer id, String rawData, String TxId) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_set_sign_output] @tran_id='" + id + "', @raw='" + rawData + "', @txid='" + TxId + "';");
    }

    ArrayList<TransactionToCommit> getOutputTransactionsToCommit() throws SqlApiException
    {
        ArrayList<TransactionToCommit> list = new ArrayList<>();
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_GET_OUT_TO_COMMIT] @CID='" + CID + "';");
        for (Map<String, Object> o : ll)
        {
            list.add(new TransactionToCommit((Integer) o.get("id"), (Integer) o.get("sign_id"), (BigDecimal) o.get("amount"), (String) o.get("address")));
        }
        return list;
    }

    ArrayList<TrackedTransaction> getTrackingOutputTransactions() throws SqlApiException
    {
        ArrayList<TrackedTransaction> list = new ArrayList<>();
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_Select_Track_OUT] @CID='" + CID + "';");
        for (Map<String, Object> o : ll)
        {
            list.add(new TrackedTransaction((Integer) o.get("ID"), (Integer) o.get("conf"), (String) o.get("txid")));
        }
        return list;
    }

    ArrayList<TrackedTransaction> getTrackingInputTransactions() throws SqlApiException
    {
        ArrayList<TrackedTransaction> list = new ArrayList<>();
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_Select_Track_IN] @CID='" + CID + "';");
        for (Map<String, Object> o : ll)
        {
            list.add(new TrackedTransaction((Integer) o.get("ID"), (Integer) o.get("conf"), (String) o.get("txid")));
        }
        return list;
    }

    void fillEmptyInputAddress(Integer userId, String address) throws SqlApiException
    {
        sql.query("exec [coreBTC].[CRYPTO_FILL_ADR_IN] @CID='" + CID + "', @uid='" + userId + "', @adr='" + address + "';");
    }

    ArrayList<Integer> getEmptyAddresses() throws SqlApiException
    {
        ArrayList<Integer> list = new ArrayList<>();
        List<Map<String, Object>> ll = sql.query("exec [coreBTC].[CRYPTO_GET_EMPTY_ADR] @CID='" + CID + "';");
        for (Map<String, Object> o : ll)
        {
            list.add((Integer) o.get("USER_ID"));
        }
        return list;
    }
}


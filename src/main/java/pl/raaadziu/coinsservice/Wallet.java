package pl.raaadziu.coinsservice;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Base64;
import java.math.BigDecimal;

import org.json.JSONArray;
import org.json.JSONObject;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import pl.raaadziu.coinsservice.DTOs.*;


class Wallet {

    private String walletIp;
    private Integer walletPort;
    private String walletUser;
    private String walletPassword;
    private Integer requiredConfirmations;
    private Logger logger;
    private Integer incId = 0;

    private String CID;

    String getCID()
    {
        return CID;
    }

    private enum respCode {
        SUCCESS,
        ENCODING_ERROR,
        PROTOCOL_ERROR,
        TRANSPORT_ERROR,
        RPC_ERROR,
        MISMATCH_ID,
        ERROR
    }

    class reqResp
    {
        respCode code = respCode.ERROR;
        String message = "error";
    }

    Wallet(String ip, Integer port, String user, String password, Integer reqConfToPay, String CID)
    {
        walletIp = ip;
        walletPort = port;
        walletUser = user;
        walletPassword = password;
        requiredConfirmations = reqConfToPay;
        this.CID = CID;

        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("Wallet" + CID);
        logger.setLevel(Level.DEBUG);
        logger.info("Wallet object initialized !");
        logger.debug(ip + ":" + port + " " + user + ":" + password + " CID:" + CID + " Conf:" + reqConfToPay);
    }

    private reqResp rpcRequest(String req)
    {
        logger.debug("REQ: " + req);


        HttpClient client = HttpClientBuilder.create().build();
        byte[] bytes = (walletUser + ":" + walletPassword).getBytes();
        String encoding = Base64.getEncoder().encodeToString(bytes);
        HttpPost request = new HttpPost("http://" + walletIp + ":" + walletPort);
        request.setHeader("Authorization", "Basic " + encoding);
        StringEntity params;

        reqResp ret = new reqResp();
        try
        {
            params= new StringEntity(req);
            request.setEntity(params);
        }
        catch (UnsupportedEncodingException Ex)
        {
            ret.code = respCode.ENCODING_ERROR;
            ret.message = "Encoding error : " + Ex.toString();
            logger.error("Request preparing error: " + Ex.toString());
            return ret;
        }

        //TODO There is no check if the wallet returns an error caused by
        //TODO not adding the client's ip address to the trusted list

        try{
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            ret.message = result.toString();
            logger.debug("RESP RAW: " + ret.message);
            JSONObject obj = new JSONObject(result.toString());
            if (obj.isNull("error"))
            {
                if (obj.getString("id").equals(incId.toString()))
                    ret.code = respCode.SUCCESS;
                else
                    ret.code = respCode.MISMATCH_ID;
            }
            else
                ret.code = respCode.RPC_ERROR;

        } catch (ClientProtocolException e) {
            ret.message = "Fatal protocol violation: " + e.getMessage();
            ret.code = respCode.PROTOCOL_ERROR;

        } catch (IOException e) {
            ret.message = "Fatal transport error: " + e.getMessage();
            ret.code = respCode.TRANSPORT_ERROR;
        }
        finally {
            request.releaseConnection();
        }

        if (ret.code != respCode.SUCCESS) logger.error("Wallet response error with code " + ret.code + ": " + ret.message);
        return ret;
    }

    private String oneMethodQuery(String method)
    {
        return oneMethodQuery(method,null);
    }

    private String oneMethodQuery(String method, String value)
    {
        if (value != null)
            value = "\"" + value + "\"";
        else
            value = "";
        return rawQuery(method, "[" + value + "]");
    }

    private String rawQuery(String method, String value)
    {
        return "{\"method\":\"" + method + "\"," +
                "\"params\":" + value +  "," +
                "\"id\":\"" + (++incId) + "\","+
                "\"jsonrpc\":\"2.0\"}";
    }

    ArrayList<String> getAddressesByAccount(String account) throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("getaddressesbyaccount",account));
        if (r.code == respCode.SUCCESS)
            return stringToListByResult(r.message);
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }

    String sendRawTransaction(String hex) throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("sendrawtransaction",hex));
        if (r.code == respCode.SUCCESS)
            return stringToStringByResult(r.message);
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }


    String getNewAddressByAccount(String account) throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("getnewaddress",account));
        if (r.code == respCode.SUCCESS)
            return (stringToStringByResult(r.message));
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }

    Map<String,Object> listAccounts() throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("listaccounts"));
        if (r.code == respCode.SUCCESS)
            return stringToMapByResult(r.message);
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }

    private Map<String,Object> stringToMapByResult(String s)
    {
        JSONObject obj = new JSONObject(s);
        JSONObject o2 = obj.getJSONObject("result");
        return o2.toMap();
    }

    NewTransaction makeCommonPaymentsTransaction(ArrayList<Payment> payments, BigDecimal networkFee, String accountFiltered) throws WalletException
    {
        BigDecimal sumToPay = new BigDecimal("0");
        String loopAddress = "";
        BigDecimal loopAmount;
        BigDecimal countingAmount = new BigDecimal(0);
        JSONObject outputs = new JSONObject();
        JSONArray inputs = new JSONArray();

        logger.info("Run makeCommonPaymentsTransaction with filtered by (" + accountFiltered + ") and network Fee :" + networkFee);

        for (Payment a : payments) {
            outputs.put(a.getAddress(), a.getAmount());
            logger.debug(" with payment amount " + a.getAmount() + " to address " + a.getAddress());
            sumToPay = sumToPay.add(a.getAmount());
        }
        sumToPay = sumToPay.add(networkFee);
        logger.debug(" sum to pay is " + sumToPay);

        ArrayList<UnspentElement> xx = getUnspent(requiredConfirmations,accountFiltered);
        if (xx.isEmpty()) throw new WalletException("common payments error: no unspents");

        logger.debug("list of inputs is: ");
        for (UnspentElement e : xx) {
            JSONObject inputElement = new JSONObject();
            inputElement.put("TxId", e.getTxId());
            inputElement.put("vOut", e.getVOut());
            inputs.put(inputElement);
            logger.debug("  input " + inputElement.toString());
            loopAddress = e.getAddress();
            countingAmount = countingAmount.add(e.getAmount());

            if (countingAmount.compareTo(sumToPay) >= 0) break;
        }

        if (countingAmount.compareTo(sumToPay) < 0)
        {
            logger.error("FATAL: no available money");
            throw new WalletException("common payments error: no available money");
        }

        if (countingAmount.compareTo(sumToPay) > 0)
        {
            loopAmount = countingAmount.subtract(sumToPay);
            outputs.put(loopAddress,loopAmount);
            logger.debug("loop amount is " + loopAmount + " with address " + loopAddress);
        }

        JSONArray data = new JSONArray();
        data.put(inputs);
        data.put(outputs);

        reqResp r = rpcRequest(rawQuery("createrawtransaction",data.toString()));
        if (r.code != respCode.SUCCESS) throw new WalletException("request error", r.message, r.code.ordinal());
        JSONObject obj = new JSONObject(r.message);

        r = rpcRequest(oneMethodQuery("signrawtransaction", obj.getString("result")));
        if (r.code != respCode.SUCCESS) throw new WalletException("request error", r.message, r.code.ordinal());
        obj = new JSONObject(r.message);
        obj = obj.getJSONObject("result");
        String hexSignedTransaction = obj.getString("hex");
        if (!obj.getBoolean("complete")) throw new WalletException("signed transaction is not complete");

        r = rpcRequest(oneMethodQuery("decoderawtransaction", hexSignedTransaction));
        if (r.code != respCode.SUCCESS) throw new WalletException("request error", r.message, r.code.ordinal());
        obj = new JSONObject(r.message);
        obj = obj.getJSONObject("result");
        String txidOfNewTx = obj.getString("txid");

        logger.info("generated new transaction " + txidOfNewTx + " with raw data " + hexSignedTransaction);
        return new NewTransaction(hexSignedTransaction,txidOfNewTx);
    }

    private ArrayList<UnspentElement> getUnspent(Integer confirmations, String accountFiltered) throws WalletException
    {
        logger.info("Run getUnspent filtered by (" + accountFiltered + ") and req. conf :" + confirmations);
        ArrayList<UnspentElement> list = new ArrayList<>();
        reqResp r = rpcRequest(oneMethodQuery("listunspent"));
        if (r.code == respCode.SUCCESS)
        {
            JSONObject obj = new JSONObject(r.message);
            JSONArray a = obj.getJSONArray("result");
            for (int i=0;i<a.length();i++)
            {
                JSONObject o3 = a.getJSONObject(i);
                Integer c = o3.getInt("confirmations");
                if (o3.getBoolean("spendable") && c >= confirmations)
                {
                    UnspentElement element = new UnspentElement(o3);

                    if (accountFiltered == null || element.getAccount().equals(accountFiltered))
                    {
                        logger.debug("   filtered unspent element: {}",element);
                        list.add(element);
                    }
                }
            }
            return list;
        }
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }

    Integer getBlockNumber(String hash) throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("getblock",hash));
        if (r.code == respCode.SUCCESS)
        {
            JSONObject o1 = new JSONObject(r.message);
            JSONObject o2 = o1.getJSONObject("result");
            return o2.getBigInteger("height").intValue();
        }
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }

    Integer getConfirmationsNumber(String txid) throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("gettransaction",txid));
        if (r.code == respCode.SUCCESS)
        {
            JSONObject obj = new JSONObject(r.message);
            return obj.getJSONObject("result").getInt("confirmations");
        }
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }

    TransactionsAndHash getTxSinceBlock(String hash, String accountFiltered) throws WalletException
    {
        logger.info("Run getTxSinceBlock filtered by (" + accountFiltered + ") with hash:" +hash);
        TransactionsAndHash tt = getTxSinceBlock(hash);

        ArrayList<Transaction> filteredList = new ArrayList<>();
        for(int i = 0; i<tt.getTransactions().size(); i++)
        {
            Transaction t = tt.getTransactions().get(i);
            if (t.getAccount().equals(accountFiltered))
            {
                logger.debug("  ftx: {}",t);
                filteredList.add(t);
            }
        }
        tt.setTransactions(filteredList);
        return tt;

    }
    private TransactionsAndHash getTxSinceBlock(String hash) throws WalletException
    {
        ArrayList<Transaction> o = new ArrayList<>();
        TransactionsAndHash l = new TransactionsAndHash();
        l.setTransactions(o);
        logger.debug("   Run getTxSinceBlock (no filtered) with hash:" +hash);
        reqResp r = rpcRequest(oneMethodQuery("listsinceblock",hash));
        if (r.code == respCode.SUCCESS)
        {
            JSONObject obj = new JSONObject(r.message);
            JSONObject o2 = obj.getJSONObject("result");
            l.setHash(o2.getString("lastblock"));
            JSONArray a = o2.getJSONArray("transactions");
            for (int i=0;i<a.length();i++)
            {
                JSONObject o3 = a.getJSONObject(i);
                Transaction transaction = new Transaction(o3);
                logger.debug("      tx(" + i + "): {}",transaction);
                o.add(transaction);
            }
        }
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
        return l;
    }

    private String stringToStringByResult(String s)
    {
        JSONObject obj = new JSONObject(s);
        return obj.getString("result");
    }

    private ArrayList<String> stringToListByResult(String s)
    {
        ArrayList<String> list = new ArrayList<>();
        JSONObject obj = new JSONObject(s);
        JSONArray arr = obj.getJSONArray("result");
        for (int i = 0; i < arr.length(); i++)
        {
            list.add(arr.getString(i));
        }
        return list;
    }

    Map<String,Object> getNetworkInfo() throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("getnetworkinfo"));
        if (r.code == respCode.SUCCESS)
            return stringToMapByResult(r.message);
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }


    Map<String,Object> getInfo() throws WalletException
    {
        reqResp r = rpcRequest(oneMethodQuery("getinfo"));
        if (r.code == respCode.SUCCESS)
            return stringToMapByResult(r.message);
        else
            throw new WalletException("request error", r.message, r.code.ordinal());
    }
}


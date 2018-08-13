package pl.raaadziu.coinsservice.DTOs;

import org.json.JSONObject;

import java.util.Map;


public class CryptoConfig {
    private String CID;
    private String name;
    private String symbol;
    private String coreIP;
    private Integer corePort;
    private String coreUser;
    private String corePassword;
    private Integer DepositConfirmations;
    private String accountFilter;

    public CryptoConfig(Map<String, Object> o)
    {
        CID = (String)o.get("CID");
        name = (String) o.get("name");
        symbol = (String) o.get("symbol");
        coreIP = (String) o.get("coreIP");
        corePort = (Integer) o.get("corePort");
        coreUser = (String) o.get("coreUser");
        corePassword = (String) o.get("corePassword");
        DepositConfirmations = (Integer) o.get("DepositConfirmations");
        accountFilter = (String) o.get("accountFilter");
    }

    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        o.put("CID", CID);
        o.put("name", name);
        o.put("symbol", symbol);
        o.put("coreIP", coreIP);
        o.put("corePort", corePort);
        o.put("coreUser", coreUser);
        o.put("corePassword", corePassword);
        o.put("DepositConfirmations", DepositConfirmations);
        o.put("accountFilter", accountFilter);
        return o.toString();
    }

    public String getCID() {
        return CID;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCoreIP() {
        return coreIP;
    }

    public Integer getCorePort() {
        return corePort;
    }

    public String getCoreUser() {
        return coreUser;
    }

    public String getCorePassword() {
        return corePassword;
    }

    public Integer getDepositConfirmations() {
        return DepositConfirmations;
    }

    public String getAccountFilter() {
        return accountFilter;
    }
}

package pl.raaadziu.coinsservice.DTOs;

import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONObject;

public class CryptoData {
    private String CID;
    private String name;
    private String symbol;
    private Integer blockNumber;

    public String getCID() {
        return CID;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public BigDecimal getNetworkFee() {
        return networkFee;
    }

    public Integer getDepositConfirmations() {
        return depositConfirmations;
    }

    public boolean isTryDoTasks() {
        return tryDoJob;
    }

    public Integer getScanPeriod() {
        return scanPeriod;
    }

    public void setBlockNumber(Integer blockNumber) {
        this.blockNumber = blockNumber;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    private String blockHash;
    private BigDecimal networkFee;
    private Integer depositConfirmations;
    private boolean tryDoJob = false;
    private Integer scanPeriod = 300;

    public CryptoData()
    {

    }

    public CryptoData(Map<String, Object> o)
    {
        this.CID = CID;
        depositConfirmations = (Integer) o.get("DepositConfirmations");
        name = (String) o.get("name");
        symbol = (String) o.get("symbol");
        blockHash = (String) o.get("blockHash");
        networkFee = (BigDecimal) o.get("networkFee");
        blockNumber = (Integer) o.get("blockNumber");
        scanPeriod = (Integer) o.get("scanPeriod");
        String tr = (String) o.get("tryDoJob");
        if (tr != null && tr.equals("Y")) tryDoJob = true; else tryDoJob = false;
    }

    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        o.put("CID", CID);
        o.put("name", name);
        o.put("symbol", symbol);
        o.put("blockNumber", blockNumber);
        o.put("blockHash", blockHash);
        o.put("networkFee", networkFee);
        o.put("depositConfirmations", depositConfirmations);
        o.put("tryDoJob", tryDoJob);
        o.put("scanPeriod",scanPeriod);
        return o.toString();
    }
}

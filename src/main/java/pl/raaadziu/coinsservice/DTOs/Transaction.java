package pl.raaadziu.coinsservice.DTOs;

import java.math.BigDecimal;
import org.json.JSONObject;


public class Transaction {
    private String address;
    private String category;
    private BigDecimal amount;
    private String txId;
    private Integer confirmations;
    private String account;

    public Transaction(JSONObject o)
    {
        address = o.getString("address");
        category = o.getString("category");
        amount = o.getBigDecimal("amount");
        confirmations = o.getInt("confirmations");
        txId = o.getString("txid");
        account = o.getString("account");
    }
    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTxId() {
        return txId;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        o.put("address", address);
        o.put("category", category);
        o.put("amount", amount);
        o.put("account", account);
        o.put("txId", txId);
        o.put("confirmations", confirmations);
        return o.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null) return false;
        if (!(obj instanceof Transaction)) return false;
        Transaction tt = (Transaction)obj;
        return this.txId.equals(tt.txId);
    }

    @Override
    public int hashCode() {
        return this.txId.hashCode();
    }
}

package pl.raaadziu.coinsservice.DTOs;
import java.math.BigDecimal;
import org.json.JSONObject;

public class UnspentElement {
    private String txId;
    private Integer vOut;
    private Integer confirmations;
    private BigDecimal amount;
    private String address;
    private String account;

    public UnspentElement(JSONObject o)
    {
        address = o.getString("address");
        amount = o.getBigDecimal("amount");
        confirmations = o.getInt("confirmations");
        vOut = o.getInt("vout");
        txId = o.getString("txid");
        account = o.getString("account");
    }
    public String getTxId() {
        return txId;
    }

    public Integer getVOut() {
        return vOut;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAddress() {
        return address;
    }

    public String getAccount() {
        return account;
    }

    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        o.put("txId", txId);
        o.put("vOut", vOut);
        o.put("confirmations", confirmations);
        o.put("amount", amount);
        o.put("address", address);
        o.put("account", account);
        return o.toString();
    }
}

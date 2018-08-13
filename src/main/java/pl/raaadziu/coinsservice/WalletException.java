package pl.raaadziu.coinsservice;

import org.json.JSONObject;

public class WalletException extends Exception {

    private String message;
    private Integer innerCode = 0;
    private String innerMessage = "";

    WalletException(String message) {
        super(message);
    }

    WalletException(String message, String innerMessage, Integer innerCode) {
        super(message);
        this.message = message;
        this.innerCode = innerCode;
        this.innerMessage = innerMessage;
    }

    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        JSONObject o2 = new JSONObject();
        o2.put("message", message);
        o2.put("innerMessage", innerMessage);
        o2.put("innerCode", innerCode);
        o.put(this.getClass().getSimpleName(), o2);
        return o.toString();
    }
}
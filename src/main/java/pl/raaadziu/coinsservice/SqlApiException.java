package pl.raaadziu.coinsservice;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlApiException extends Exception {

    private String message;
    private Integer innerCode = 0;
    private String innerMessage = "";

    SqlApiException(String message) {
        super(message);
        Logger logger = LoggerFactory.getLogger("SqlDbLog");
        logger.error("Exception with message: " + message);
        this.message = message;
    }

    SqlApiException(String message, String innerMessage, Integer innerCode) {
        super(message);
        Logger logger = LoggerFactory.getLogger("SqlDbLog");
        logger.error("Exception with message: " + message + " innerMessage:" + innerMessage + " and innerCode:" + innerCode);
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
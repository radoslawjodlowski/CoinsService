package pl.raaadziu.coinsservice;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CryptoCoreException extends Exception {
    private String message;

    CryptoCoreException(String message)
    {
        super(message);
        Logger logger = LoggerFactory.getLogger("CryptoCoreException");
        logger.error("Exception with message: " + message);
        this.message = message;
    }

    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        JSONObject o2 = new JSONObject();
        o2.put("message", message);
        o.put(this.getClass().getSimpleName(), o2);
        return o.toString();
    }
}

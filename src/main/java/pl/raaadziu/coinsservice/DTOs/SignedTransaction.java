package pl.raaadziu.coinsservice.DTOs;

import org.json.JSONObject;


public class SignedTransaction
{
    private String hex;
    private Integer id;

    public SignedTransaction(String hex,Integer id)
    {
        this.hex = hex;
        this.id = id;
    }

    public String getHex() {
        return hex;
    }

    public Integer getId() {
        return id;
    }


    @Override
    public String toString()
    {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("hex", hex);
        return o.toString();
    }
}

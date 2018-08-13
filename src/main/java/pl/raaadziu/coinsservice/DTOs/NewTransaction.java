package pl.raaadziu.coinsservice.DTOs;

public class NewTransaction {
    private String hex;
    private String txID;

    public String getHex() {
        return hex;
    }

    public String getTxID() {
        return txID;
    }

    public NewTransaction(String hex,String txId)
    {
        this.hex = hex;
        this.txID = txId;

    }

    @Override
    public String toString()
    {
        return "TxID:" + txID + "\n hex:" + hex;
    }
}
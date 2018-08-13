package pl.raaadziu.coinsservice.DTOs;

public class TrackedTransaction {
    private Integer id;
    private String txId;
    private Integer confirmations;

    public TrackedTransaction(Integer id, Integer confirmations, String txId)
    {
        this.id = id;
        this.confirmations = confirmations;
        this.txId = txId;
    }

    public String getTxId() {
        return txId;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString()
    {
        return "id: " + id + " TxId:" + txId + " confirmations:" + confirmations;
    }
}


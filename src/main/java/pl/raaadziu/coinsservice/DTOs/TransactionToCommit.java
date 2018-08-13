package pl.raaadziu.coinsservice.DTOs;

import java.math.BigDecimal;

public class TransactionToCommit
{
    private Integer id;
    private Integer signId;
    private BigDecimal amount;
    private String address;

    public TransactionToCommit(Integer id, Integer signId, BigDecimal amount, String address)
    {
        this.id = id;
        this.signId = signId;
        this.address = address;
        this.amount = amount;
    }

    public Integer getId() {
        return id;
    }

    public Integer getSignId() {
        return signId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString()
    {
        return "id: " + id + " signId:" + signId + " address:" + address + " amount:" + amount;
    }
}
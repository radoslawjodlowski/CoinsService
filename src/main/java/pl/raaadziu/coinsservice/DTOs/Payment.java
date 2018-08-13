package pl.raaadziu.coinsservice.DTOs;

import java.math.BigDecimal;

public class Payment {
    private String address;
    private BigDecimal amount;

    public Payment(String address,BigDecimal amount)
    {
        this.address = address;
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString()
    {
        return "[" + address + ":" + amount + "]";
    }
}


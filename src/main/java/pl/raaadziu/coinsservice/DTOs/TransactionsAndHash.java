package pl.raaadziu.coinsservice.DTOs;

import java.util.ArrayList;


public class TransactionsAndHash {

    private ArrayList<Transaction> transactions;
    private String hash;

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder("Transactions:\n");
        for (Transaction t : transactions) {
            s.append(t.toString()).append("\n");
        }
        s.append("Last block hash: ").append(hash);
        return s.toString();
    }
}

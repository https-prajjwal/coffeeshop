package com.example.coffeeshop;
import jakarta.persistence.*;

import java.math.BigInteger;
import java.time.LocalDate;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer debit;
    private LocalDate date;
    private int user_id;
    private BigInteger card_num;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDebit() {
        return debit;
    }

    public void setDebit(Integer debit) {
        this.debit = debit;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public BigInteger getCard_num() {
        return card_num;
    }

    public void setCard_num(BigInteger card_num) {
        this.card_num = card_num;
    }
}

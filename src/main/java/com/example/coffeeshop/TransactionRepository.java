package com.example.coffeeshop;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository  extends JpaRepository<Transaction, Long> {
}

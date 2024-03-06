package com.example.coffeeshop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {
    @Autowired
    private OrdersRepository ordersRepository;

    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    public Optional<Orders> getOrdersById(Long id) {
        return ordersRepository.findById(id);
    }

    public Orders saveOrder(Orders orders) {
        return ordersRepository.save(orders);
    }

    public void deleteOrder(Long id) {
        ordersRepository.deleteById(id);
    }
}
package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.model.CartItem;
import com.example.demo.model.Order;
import com.example.demo.model.OrderDetail;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order checkout(String loginName, List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        Account account = accountRepository.findByLoginName(loginName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản đăng nhập"));

        Order order = new Order();
        order.setAccount(account);
        order.setCreatedAt(LocalDateTime.now());

        long totalAmount = 0;
        for (CartItem cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setProduct(productRepository.getReferenceById(cartItem.getProductId()));
            detail.setUnitPrice(cartItem.getPrice());
            detail.setQuantity(cartItem.getQuantity());
            detail.setLineTotal(cartItem.getTotalPrice());
            order.addDetail(detail);
            totalAmount += cartItem.getTotalPrice();
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }
}

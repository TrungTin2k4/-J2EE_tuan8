package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @PostMapping("/checkout")
    public String checkout(Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        if (cartService.getCartItems(session).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng đang trống.");
            return "redirect:/cart";
        }

        Order order = orderService.checkout(principal.getName(), cartService.getCartItems(session));
        cartService.clearCart(session);

        redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công.");
        redirectAttributes.addFlashAttribute("placedOrderId", order.getId());
        redirectAttributes.addFlashAttribute("placedOrderTotal", order.getTotalAmount());
        return "redirect:/cart";
    }
}

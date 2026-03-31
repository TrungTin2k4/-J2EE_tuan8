package com.example.demo.service;

import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    private static final String CART_SESSION_KEY = "cart";

    public void addToCart(Product product, int quantity, HttpSession session) {
        List<CartItem> cartItems = getCartItems(session);
        int safeQuantity = Math.max(quantity, 1);

        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(product.getId())) {
                cartItem.setQuantity(cartItem.getQuantity() + safeQuantity);
                session.setAttribute(CART_SESSION_KEY, cartItems);
                return;
            }
        }

        cartItems.add(new CartItem(product.getId(), product.getName(), product.getImage(), product.getPrice(), safeQuantity));
        session.setAttribute(CART_SESSION_KEY, cartItems);
    }

    public void updateQuantity(Long productId, int quantity, HttpSession session) {
        if (quantity <= 0) {
            removeFromCart(productId, session);
            return;
        }

        List<CartItem> cartItems = getCartItems(session);
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId().equals(productId)) {
                cartItem.setQuantity(quantity);
                break;
            }
        }
        session.setAttribute(CART_SESSION_KEY, cartItems);
    }

    public void removeFromCart(Long productId, HttpSession session) {
        List<CartItem> cartItems = getCartItems(session);
        cartItems.removeIf(cartItem -> cartItem.getProductId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cartItems);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public long getCartTotal(HttpSession session) {
        return getCartItems(session).stream()
                .mapToLong(CartItem::getTotalPrice)
                .sum();
    }

    public int getCartCount(HttpSession session) {
        return getCartItems(session).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @SuppressWarnings("unchecked")
    public List<CartItem> getCartItems(HttpSession session) {
        Object cart = session.getAttribute(CART_SESSION_KEY);
        if (cart instanceof List<?>) {
            return (List<CartItem>) cart;
        }

        List<CartItem> cartItems = new ArrayList<>();
        session.setAttribute(CART_SESSION_KEY, cartItems);
        return cartItems;
    }
}

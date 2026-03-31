package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.CartService;
import com.example.demo.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showCart(Model model, HttpSession session) {
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sản phẩm không tồn tại.");
            return buildProductRedirect(keyword, categoryId, sort, page);
        }

        cartService.addToCart(product, quantity, session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng.");
        return buildProductRedirect(keyword, categoryId, sort, page);
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        cartService.updateQuantity(productId, quantity, session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng.");
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable("productId") Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(productId, session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    private String buildProductRedirect(String keyword, Integer categoryId, String sort, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/products")
                .queryParam("page", Math.max(page, 0));

        if (StringUtils.hasText(keyword)) {
            builder.queryParam("keyword", keyword.trim());
        }
        if (categoryId != null) {
            builder.queryParam("categoryId", categoryId);
        }
        if (StringUtils.hasText(sort)) {
            builder.queryParam("sort", sort);
        }

        return "redirect:" + builder.build().encode().toUriString();
    }
}

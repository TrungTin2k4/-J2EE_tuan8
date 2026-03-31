package com.example.demo.config;

import com.example.demo.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalModelAttributes {
    @Autowired
    private CartService cartService;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpSession session, Authentication authentication) {
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isAuthenticated", isAuthenticated);

        if (!isAuthenticated) {
            model.addAttribute("cartCount", 0);
            model.addAttribute("isAdmin", false);
            model.addAttribute("isUser", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("currentRoleLabel", "GUEST");
            return;
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toSet());

        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isUser = roles.contains("ROLE_USER");

        model.addAttribute("cartCount", cartService.getCartCount(session));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isUser", isUser);
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("currentRoleLabel", isAdmin ? "ADMIN" : isUser ? "USER" : "AUTH");
    }
}

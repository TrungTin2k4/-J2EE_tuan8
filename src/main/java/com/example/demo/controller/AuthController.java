package com.example.demo.controller;

import com.example.demo.model.RegisterRequest;
import com.example.demo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    @Autowired
    private AccountService accountService;

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/products";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model, Authentication authentication) {
        if (isAuthenticated(authentication)) {
            return "redirect:/products";
        }

        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult result,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (isAuthenticated(authentication)) {
            return "redirect:/products";
        }

        if (accountService.existsByLoginName(registerRequest.getLoginName())) {
            result.rejectValue("loginName", "loginName.exists", "Tên đăng nhập đã tồn tại");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "confirmPassword.mismatch", "Mật khẩu xác nhận không khớp");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        accountService.registerUser(registerRequest.getLoginName(), registerRequest.getPassword());
        redirectAttributes.addFlashAttribute("successMessage",
                "Đăng ký thành công. Bạn có thể đăng nhập bằng tài khoản USER vừa tạo.");
        return "redirect:/login";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}

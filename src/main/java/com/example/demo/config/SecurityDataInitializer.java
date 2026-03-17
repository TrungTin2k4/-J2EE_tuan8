package com.example.demo.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Account;
import com.example.demo.model.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;

@Configuration
public class SecurityDataInitializer {

    @Bean
    public CommandLineRunner seedSecurityData(AccountRepository accountRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> createRole(roleRepository, "ROLE_ADMIN"));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> createRole(roleRepository, "ROLE_USER"));

            if (!accountRepository.existsByLoginName("admin")) {
                accountRepository.save(createAccount("admin", "admin123", passwordEncoder, adminRole));
            }

            if (!accountRepository.existsByLoginName("user")) {
                accountRepository.save(createAccount("user", "123456", passwordEncoder, userRole));
            }
        };
    }

    private Role createRole(RoleRepository roleRepository, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        return roleRepository.save(role);
    }

    private Account createAccount(String loginName, String rawPassword, PasswordEncoder passwordEncoder, Role role) {
        Account account = new Account();
        account.setLoginName(loginName);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setRoles(new HashSet<>(Set.of(role)));
        return account;
    }
}

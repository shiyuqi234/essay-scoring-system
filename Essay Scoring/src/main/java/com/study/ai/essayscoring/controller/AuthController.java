package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.config.JwtService;
import com.study.ai.essayscoring.dto.LoginRequest;
import com.study.ai.essayscoring.dto.RegisterRequest;
import com.study.ai.essayscoring.entity.UserAccount;
import com.study.ai.essayscoring.repository.UserAccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证与用户管理接口（注册 + JWT 登录）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> resp = new HashMap<>();
        if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
            resp.put("success", false);
            resp.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(resp);
        }

        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole());
        account.setStudentId(request.getRole().name().equals("STUDENT") ? request.getStudentId() : null);
        account.setDisplayName(request.getDisplayName());
        account.setEnabled(true);

        userAccountRepository.save(account);

        resp.put("success", true);
        resp.put("message", "注册成功");
        return ResponseEntity.ok(resp);
    }

    /**
     * 登录（用户名 + 密码，返回 JWT）
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> resp = new HashMap<>();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String token = jwtService.generateToken(account);

        resp.put("success", true);
        resp.put("token", token);
        resp.put("username", account.getUsername());
        resp.put("role", account.getRole().name());
        resp.put("displayName", account.getDisplayName());
        resp.put("studentId", account.getStudentId());
        return ResponseEntity.ok(resp);
    }

    /**
     * 登录状态检查（基于 JWT）
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        if (principal == null) {
            resp.put("authenticated", false);
            return ResponseEntity.ok(resp);
        }
        UserAccount account = userAccountRepository.findByUsername(principal.getName())
                .orElse(null);
        resp.put("authenticated", true);
        resp.put("username", principal.getName());
        if (account != null) {
            resp.put("role", account.getRole().name());
            resp.put("displayName", account.getDisplayName());
            resp.put("studentId", account.getStudentId());
        }
        return ResponseEntity.ok(resp);
    }
}


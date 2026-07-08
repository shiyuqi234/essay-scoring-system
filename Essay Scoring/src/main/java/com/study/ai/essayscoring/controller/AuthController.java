package com.study.ai.essayscoring.controller;

import com.study.ai.essayscoring.config.JwtService;
import com.study.ai.essayscoring.dto.ApiResponse;
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

/**
 * 认证与用户管理接口（注册 + JWT 登录）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:8080}")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userAccountRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名已存在"));
        }

        UserAccount account = new UserAccount();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole());
        account.setStudentId(request.getRole().name().equals("STUDENT") ? request.getStudentId() : null);
        account.setDisplayName(request.getDisplayName());
        account.setEnabled(true);

        userAccountRepository.save(account);

        return ResponseEntity.ok(ApiResponse.success("注册成功", null));
    }

    /**
     * 登录（用户名 + 密码，返回 JWT）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String token = jwtService.generateToken(account);

        return ResponseEntity.ok(ApiResponse.loginSuccess(
                token, account.getUsername(), account.getRole().name(),
                account.getDisplayName(), account.getStudentId()));
    }

    /**
     * 登录状态检查（基于 JWT）
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(ApiResponse.authStatus(false, null, null, null, null));
        }
        UserAccount account = userAccountRepository.findByUsername(principal.getName())
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.authStatus(
                true, principal.getName(),
                account != null ? account.getRole().name() : null,
                account != null ? account.getDisplayName() : null,
                account != null ? account.getStudentId() : null));
    }
}

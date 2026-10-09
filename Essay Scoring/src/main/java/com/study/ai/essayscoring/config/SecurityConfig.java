package com.study.ai.essayscoring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.ai.essayscoring.dto.ApiResponse;
import com.study.ai.essayscoring.entity.UserAccount;
import com.study.ai.essayscoring.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final Environment environment;

    private boolean isDev() {
        return environment.acceptsProfiles(Profiles.of("dev", "test"));
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                // JWT 通过 Authorization 头传递、会话无状态，不存在浏览器自动携带凭证的 CSRF 面
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    // ★ H2 控制台只在 dev 环境开放
                    if (isDev()) {
                        auth.requestMatchers("/h2-console", "/h2-console/**").permitAll();
                    }
                    auth
                            .requestMatchers("/", "/index.html", "/student.html", "/teacher.html",
                                    "/css/**", "/js/**", "/images/**").permitAll()
                            // 认证接口
                            .requestMatchers("/api/auth/**").permitAll()
                            // ★ /test/** 不再无脑开放；诊断接口只在 dev 放行
                            .requestMatchers("/test/**").access((authentication, context) ->
                                    new AuthorizationDecision(isDev()))
                            // 教师端接口仅教师访问
                            .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                            // 学生端接口需要登录（学生或教师均可）
                            .requestMatchers("/api/student/**").authenticated()
                            // 其他默认需要认证
                            .anyRequest().authenticated();
                })
                .headers(headers -> {
                    // ★ 只有 dev 为了 H2 控制台的 iframe 才放宽 frameOptions
                    if (isDev()) {
                        headers.frameOptions(frameOptions -> frameOptions.sameOrigin());
                    }
                    // ★ 恢复 nosniff —— 它和 H2 毫无关系，没有理由关闭
                    headers.contentTypeOptions(Customizer.withDefaults());
                })
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint(objectMapper)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 认证失败统一返回 401 + ApiResponse JSON（而不是默认的空 403）。
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.error("未登录或登录已过期，请重新登录")));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 抛标准 UsernameNotFoundException —— 它是 AuthenticationException 体系的一员，
     * 能被认证链路正确处理，而不是变成 400。
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserAccount account = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
            return User.withUsername(account.getUsername())
                    .password(account.getPassword())
                    .roles(account.getRole().name())
                    .disabled(!Boolean.TRUE.equals(account.getEnabled()))
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

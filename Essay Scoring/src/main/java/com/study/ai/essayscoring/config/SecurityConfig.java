package com.study.ai.essayscoring.config;

import com.study.ai.essayscoring.entity.UserAccount;
import com.study.ai.essayscoring.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 完全禁用 CSRF（H2 控制台需要）
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // H2 控制台（开发环境使用）- 必须放在最前面
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/h2-console").permitAll()
                        // 静态资源和首页
                        .requestMatchers("/", "/index.html", "/student.html", "/teacher.html",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        // 认证接口
                        .requestMatchers("/api/auth/**").permitAll()
                        // 测试接口
                        .requestMatchers("/test/**").permitAll()
                        // 教师端接口仅教师访问
                        .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        // 学生端接口需要登录（学生或教师均可）
                        .requestMatchers("/api/student/**").authenticated()
                        // 其他默认需要认证
                        .anyRequest().authenticated()
                )
                // 允许 H2 控制台的框架选项（用于开发环境）
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                        .contentTypeOptions(contentTypeOptions -> contentTypeOptions.disable())
                )
                // 只在非 H2 控制台路径上应用 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            UserAccount account = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            UserDetails user = User.withUsername(account.getUsername())
                    .password(account.getPassword())
                    .roles(account.getRole().name())
                    .disabled(!Boolean.TRUE.equals(account.getEnabled()))
                    .build();
            return user;
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}


package com.study.ai.essayscoring.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 跳过 H2 控制台和测试接口（不需要 JWT 处理）
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String requestPath = path.startsWith(contextPath) ? path.substring(contextPath.length()) : path;

        if (requestPath.startsWith("/h2-console") || requestPath.startsWith("/test")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // ★ 增加了 userDetails.isEnabled() —— 被禁用的账号立即失效
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())
                        && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            // ★ token 非法/过期/签名错误：只清空上下文，不抛异常，
            //   交给后面的授权环节返回标准 401，而不是 500。
            log.debug("JWT 校验失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (AuthenticationException e) {
            // 用户不存在等认证异常，同样降级为「未认证」
            log.debug("认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        // ⚠️ 只捕获 JwtException / AuthenticationException，其他异常（如数据库故障）必须继续抛出，
        //    否则会把系统故障伪装成「未登录」。

        filterChain.doFilter(request, response);
    }
}

package com.stu.helloserver.config;

import com.stu.helloserver.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 注入JWT过滤器
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 开启全局CORS配置
                .cors(cors -> cors.configure(http))
                // 关闭CSRF防护（前后端分离场景）
                .csrf(csrf -> csrf.disable())
                // 配置会话管理策略：无状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 配置接口访问规则
                .authorizeHttpRequests(auth -> auth
                        // 放行注册接口 POST /api/users
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        // 放行登录接口 POST /api/users/login
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        // 其他所有请求都必须先认证
                        .anyRequest().authenticated()
                )
                // 把JWT过滤器加到UsernamePasswordAuthenticationFilter之前
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                // 关闭Spring Security默认的表单登录
                .formLogin(form -> form.disable())
                // 关闭HTTP Basic认证
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
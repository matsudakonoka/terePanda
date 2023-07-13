package com.cnpc.epai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@SpringBootApplication
@MapperScan("com.cnpc.epai.core.workscene.mapper,com.cnpc.epai.core.worktask.mapper")
@ComponentScan(basePackages="com.cnpc.epai")
@EntityScan({"com.cnpc.epai.**.domain"})
@EnableDiscoveryClient//增加注解
@EnableResourceServer //增加注解
public class StartApplication  extends ResourceServerConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(StartApplication.class, args);
    }

    @Override  //增加方法
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/swagger-ui.html",
                        "/research/**",
                        "/api/**",
                        "/core/**",
                        "/v2/api-docs",
                        "/swagger-resources",
                        "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .headers().frameOptions().sameOrigin();
    }
}
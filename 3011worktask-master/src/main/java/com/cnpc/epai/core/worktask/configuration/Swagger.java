package com.cnpc.epai.core.worktask.configuration;

import com.cnpc.epai.common.configuration.EpaiConfig;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Description: 任务Swagger
 * @author 王淼
 * @version 1.0.0
 * @date  2018/4/20
 */
@Configuration
@EnableSwagger2
public class Swagger {

    @Autowired
    private EpaiConfig epaiConfig;
    /**
     * 生成api，可以定义多个组，比如本类中定义把test和demo区分开了
     *
     * @return bean
     */
    @Bean
    public Docket carApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName(epaiConfig.getService_prefix()).genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false).forCodeGeneration(true).pathMapping("/")// api测试请求地址，最终调用接口后会和paths拼接在一起
                .select().paths(PathSelectors.regex(epaiConfig.getService_prefix() + "/.*"))// 过滤的接口
                .build().securitySchemes(Lists.newArrayList(apiKey()));
    }

    @Bean
    public SecurityConfiguration securityInfo() {
        return new SecurityConfiguration(null, null, null, null, "", ApiKeyVehicle.HEADER,"Authorization","");
    }

    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }
}

/**  
 * @Title: MybatisConfig.java 
 * @Package com.cnpc.epai.objdataset.configuration 
 * @Description: TODO 
 * @author cuijiaqi
 * @date 2021年8月27日 下午3:58:56 
 * {修改记录：cuijiaqi 2021年8月27日 下午3:58:56}
*/

package com.cnpc.epai.core.worktask.configuration;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    /**
     * Map 结果处理，Key 以驼峰格式显示
     *
     * @return
     */
 /*   @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            configuration.setObjectWrapperFactory(new MybatisMapWrapperFactory());
            configuration.setCallSettersOnNulls(true);
            configuration.setMapUnderscoreToCamelCase(false);
            configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        };
    }*/

    /**
     * 分页处理 - 不加此配置，分页会失效
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor().setCountSqlParser(new JsqlParserCountOptimize(true));
    }
}


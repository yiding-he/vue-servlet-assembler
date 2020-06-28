package com.hyd.vueservletassembler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ServletComponentScan
public class VueServletAssemblerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VueServletAssemblerApplication.class, args);
    }

    @Bean
    public VueAssembler vueAssembler() {
        return new VueAssembler(
            new VueAssemblerConfiguration()
        );
    }

    @Bean
    public ServletRegistrationBean<VueAssemblerServlet> exampleServletBean(
        VueAssembler vueAssembler, VueAssemblerServlet servlet
    ) {
        ServletRegistrationBean<VueAssemblerServlet> bean = new ServletRegistrationBean<>(
            servlet, vueAssembler.getConfiguration().getUrlPattern()
        );
        bean.setLoadOnStartup(1);
        return bean;
    }
}

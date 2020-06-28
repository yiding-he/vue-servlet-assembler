package com.hyd.vueservletassembler;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动组装 vue 的类
 * 使用方法：
 * 1、在 SpringBoot 项目中加上 {@code @Import(VueAssembler.class)} ；
 * 2、在 resources/public/vue 目录下编写组件，每个组件一个子目录，内含 template.html 和 script.js 两个文件；
 * 3、页面中的所有组件要包含在 {@code <div id="app">...</div>} 中
 * 4、在页面的 body 末尾引入 {@code <script src="vue"></script>}
 *
 * gzip 压缩交给 servlet 容器来做。
 *
 * TODO: 尚未支持缓存
 */
@Slf4j
public class VueAssembler {

    /**
     * 默认编码
     */
    @Getter
    @Setter
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * 浏览器请求路径
     */
    @Getter
    @Setter
    private String urlPattern = "/vue/*";

    /**
     * 源码资源所在目录（resources 下）
     */
    @Getter
    @Setter
    private String resourcePathPrefix = "public/vue";

    /////////////////////////////////////////////////////////////// 处理浏览器请求

    public class Servlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/javascript");
            assemble(resp.getOutputStream());
        }
    }

    @Bean
    public ServletRegistrationBean<Servlet> exampleServletBean() {
        ServletRegistrationBean<Servlet> bean =
            new ServletRegistrationBean<>(new Servlet(), urlPattern);
        bean.setLoadOnStartup(1);
        return bean;
    }

    /////////////////////////////////////////////////////////////// 组装 Vue 脚本

    public void assemble(ServletOutputStream outputStream) throws IOException {

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + resourcePathPrefix + "/**");

        Map<String, Resource> scriptMappings = new HashMap<>();
        Map<String, Resource> templateMappings = new HashMap<>();

        for (Resource resource : resources) {
            File file = resource.getFile();
            if (resource.getFilename() == null || !file.isFile()) {
                continue;
            }

            String componentName = file.getParentFile().getName();

            if (resource.getFilename().endsWith(".html")) {
                templateMappings.put(componentName, resource);
            }
            if (resource.getFilename().endsWith(".js")) {
                scriptMappings.put(componentName, resource);
            }
        }

        for (Map.Entry<String, Resource> entry : scriptMappings.entrySet()) {
            String script = getResourceContent(entry.getValue());
            String template = getResourceContent(templateMappings.get(entry.getKey()));
            String componentAssembled = assemble0(script, template);
            outputResource(componentAssembled, outputStream);
        }

        outputResource("new Vue({el: '#app'});", outputStream);
    }

    private void outputResource(String content, ServletOutputStream outputStream) throws IOException {
        StreamUtils.copy(content, charset, outputStream);
    }

    private String getResourceContent(Resource resource) throws IOException {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), charset);
        } catch (FileNotFoundException e) {
            log.warn("resource {} not found.", resource);
            return "";
        }
    }

    private String assemble0(String script, String template) {
        return script.replace("{{template}}", template);
    }
}

package com.hyd.vueservletassembler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VueAssembler {

    private VueAssemblerConfiguration configuration;

    private Charset charset = StandardCharsets.UTF_8;

    public VueAssembler(VueAssemblerConfiguration configuration) {
        this.configuration = configuration;
    }

    public VueAssemblerConfiguration getConfiguration() {
        return configuration;
    }

    ///////////////////////////////////////////////////////////////

    public void assemble(ServletOutputStream outputStream) throws IOException {

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + configuration.getResourcePathPrefix() + "/**");

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

    ///////////////////////////////////////////////////////////////

    private String assemble0(String script, String template) {
        return script.replace("{{template}}", template);
    }
}

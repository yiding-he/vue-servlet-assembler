package com.hyd.vueservletassembler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(VueAssembler.class)
public class VueServletAssemblerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VueServletAssemblerApplication.class, args);
    }

}

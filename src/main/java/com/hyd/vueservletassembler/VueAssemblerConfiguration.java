package com.hyd.vueservletassembler;

import lombok.Data;

@Data
public class VueAssemblerConfiguration {

    private String urlPattern = "/vue/*";

    private String resourcePathPrefix = "public/vue";
}

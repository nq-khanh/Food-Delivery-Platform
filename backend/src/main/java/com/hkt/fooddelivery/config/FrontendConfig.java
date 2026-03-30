package com.hkt.fooddelivery.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "app.frontend")
@Component
@Getter
@Setter
public class FrontendConfig {
    private Map<String, String> urls;
}

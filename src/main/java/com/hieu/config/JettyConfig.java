package com.hieu.config;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyConfig {

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyAcceptQueueCustomizer() {
        return factory -> factory.addServerCustomizers(server -> {
            for (Connector connector : server.getConnectors()) {
                if (connector instanceof ServerConnector serverConnector) {
                    serverConnector.setAcceptQueueSize(1000);
                }
            }
        });
    }
}
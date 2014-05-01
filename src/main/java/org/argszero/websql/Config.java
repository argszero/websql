package org.argszero.websql;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.Filter;


/**
 * Created by shaoaq on 14-5-1.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan("org.argszero.websql")
@EnableScheduling
public class Config {
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory() {
            @Override
            public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {
                TomcatEmbeddedServletContainer embeddedServletContainer = (TomcatEmbeddedServletContainer) super.getEmbeddedServletContainer(initializers);
                Connector connector = embeddedServletContainer.getTomcat().getConnector();
                connector.setURIEncoding("UTF-8");
                return embeddedServletContainer;
            }
        };
        return factory;
    }

    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("utf-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }
}

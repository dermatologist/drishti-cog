package com.canehealth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("org.gtri.hdap.mdata")
@EnableJpaRepositories({"org.gtri.hdap.mdata.jpa.repository", "com.canehealth.repository"})
public class FhirQLServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FhirQLServerApplication.class, args);
    }
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(FhirQLServerApplication.class);
//    }

}

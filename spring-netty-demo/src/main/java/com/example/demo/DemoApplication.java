package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

//io.gitlab.leibnizhu
//@EnableWebMvc
@SpringBootApplication(scanBasePackages = {"com.example.demo"})
@ServletComponentScan(basePackageClasses = DemoApplication.class)
public class DemoApplication {


    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoApplication.class, args);
        //new SpringApplicationBuilder(DemoApplication.class).web(false).run(args);

    }

}

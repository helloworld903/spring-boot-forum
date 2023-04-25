package com.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	       //将物理地址upload下的文件映射到/upload下
	       //访问的时候就直接访问http://localhost:9000/upload/文件名
	       registry.addResourceHandler("/upload/**")
	          .addResourceLocations("file:"+System.getProperty("user.dir")+"/upload/");
	  }	
}

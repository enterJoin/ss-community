package com.atlxw.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class ImageFileConfig implements WebMvcConfigurer {
    //最新的WebMvcConfigurationSupport会覆盖默认配置

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        //定义到硬盘
        //用户头像的资源映射
        registry.addResourceHandler(ImageResourceMapper.headPhotoUrlSuffix + "**")
                .addResourceLocations("file:" + ImageResourceMapper.getHeadPhotoFileLocationByEnvironment());

        //文章、提问、回答的内容图片的静态资源映射
        registry.addResourceHandler(ImageResourceMapper.contentPhotoUrlSuffix + "**")
                .addResourceLocations("file:" + ImageResourceMapper.getContentPhotoFileLocationByEnvironment());
    }
}

package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentImage {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 图片的生成名字
     */
    private String name;
    /**
     * 图片的物理路径
     */
    private String path;
    /**
     * 图片的URL
     */
    private String url;
    /**
     * 上传时间
     */
    private Timestamp uploadtime;
}
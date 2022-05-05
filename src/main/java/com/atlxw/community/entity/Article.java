package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 文章标题
     */
    private String title;
    /**
     * 文章正文，Markdown格式文本
     */
    private String content;
    /**
     * 发布者用户ID
     */
    private Integer author;
    /**
     * 文章发布的时间
     */
    private Timestamp submit_time;
    /**
     * 上次修改的时间
     */
    private Timestamp last_update_time;
    /**
     * 文章被浏览的次数
     */
    private Integer browse_count;
}

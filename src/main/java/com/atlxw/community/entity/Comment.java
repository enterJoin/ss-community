package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论发表者的ID
     */
    private Integer commentator;
    /**
     * 被评论的文章或者回答ID
     */
    private Integer to_comment;
    /**
     * 被评论的对象类型：0(代表回答), 1(代表文章)
     */
    private Integer to_comment_type;
    /**
     * 评论提交的时间
     */
    private Timestamp comment_time;
}

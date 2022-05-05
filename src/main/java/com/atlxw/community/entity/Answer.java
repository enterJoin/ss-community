package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 问题的ID
     */
    private Integer which_question;
    /**
     * 回答者的ID
     */
    private Integer answerer;
    /**
     * 回答的正文, Markdown格式的文本
     */
    private String content;
    /**
     * 提交回答的时间
     */
    private Timestamp answer_time;
    /**
     * 上次修改的时间
     */
    private Timestamp last_update_time;
    /**
     * 回答被浏览次数
     */
    private Integer browse_count;

}

package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 问题的标题
     */
    private String title;
    /**
     * 问题的详情 可为空
     */
    private String detail;
    /**
     * 提问者的用户的ID
     */
    private Integer questioner;
    /**
     * 提交的时间
     */
    private Timestamp submit_time;
    /**
     * 上次修改的时间
     */
    private Timestamp last_update_time;
    /**
     * 问题被浏览的次数
     */
    private Integer browse_count;
}

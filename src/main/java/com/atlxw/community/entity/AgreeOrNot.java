package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreeOrNot {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 被评价的对象的ID
     */
    private Integer to_evaluate;
    /**
     * 被评价的对象类别：0(回答), 1(文章), 2(问题), 3(文章评论), 4(回答评论)
     */
    private Integer evaluate_type;
    /**
     * 0表示不同意(踩), 1表示同意(顶)
     */
    private boolean agreeornot;
}

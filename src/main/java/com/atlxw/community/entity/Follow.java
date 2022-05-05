package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Follow {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 被关注着的ID
     */
    private Integer to_follow;
    /**
     * 关注者的ID
     */
    private Integer folllower;
}

package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class userCollection {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 收藏的问题、回答或文章ID
     */
    private Integer collection_id;
    /**
     * 收藏着的用户ID
     */
    private Integer collector;
    /**
     * 收藏类型: 0(代表回答), 1(代表文章), 2(代表问题)
     */
    private Integer collection_type;
}

package com.atlxw.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface EvaluateDao {
    /**
     * 对指定evaluate_id且类型为evaluate_type 进行踩
     * @param evaluate_id   对应类型的id
     * @param evaluate_type 指定的文本的类型  0(代表回答)  1(代表文章)  2(代表问题)  3(代表评论)
     * @return
     */
    int dislike(@Param("evaluate_id") int evaluate_id, @Param("evaluate_type") int evaluate_type);

    /**
     * 对指定evaluate_id且类型为evaluate_type 进行赞
     * @param evaluate_id
     * @param evaluate_type
     * @return
     */
    int like(int evaluate_id, int evaluate_type);
}

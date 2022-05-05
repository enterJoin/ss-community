package com.atlxw.community.dao;

import com.atlxw.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CommentDao {
    /**
     *在agreeornot表中获得类型为文章评论并且是踩的  对应的comment_id的记录总和
     * @param comment_id
     * @return
     */
    Integer getDislikeCount(Integer comment_id);

    /**
     *在agreeornot表中获得类型为文章评论并且是赞的  对应的comment_id的记录总和
     * @param comment_id
     * @return
     */
    Integer getLikeCount(Integer comment_id);

    /**
     * 插入一条新的评论
     * @param newComment
     * @return
     */
    int comment(Comment newComment);

    /**
     * 获取从howManyToShowAtOneTime开始所有的评论   按照时间排序（由大到小）
     * @param howManyToShowAtOneTime
     * @param to_comment
     * @param to_comment_type
     * @return
     */
    List<Comment> getRemainAllComments(@Param("howManyToShowAtOneTime") int howManyToShowAtOneTime, @Param("to_comment") int to_comment, @Param("to_comment_type") int to_comment_type);

    /**
     * 获得从0到howManyToShowAtOneTime的所有评论  按照时间顺序（由大到小）
     * @param howManyToShowAtOneTime
     * @param to_comment
     * @param to_comment_type
     * @return
     */
    List<Comment> getTopComments(@Param("howManyToShowAtOneTime") int howManyToShowAtOneTime, @Param("to_comment") int to_comment, @Param("to_comment_type") int to_comment_type);

    /**
     * 获得被评论的类型为to_comment_type并且id为to_comment的总的被评论数
     * @param to_comment
     * @param to_comment_type
     * @return
     */
    Integer getCommentCountOf(@Param("to_comment") int to_comment, @Param("to_comment_type") int to_comment_type);

    /**
     * 根据用户id来获取所有该用户写的评论
     * 只需要 只需要 comment_time, content, to_comment_type, to_comment 四个字段
     * @param commentator   用户id
     * @return
     */
    List<Comment> getCommentsBy(int commentator);
}

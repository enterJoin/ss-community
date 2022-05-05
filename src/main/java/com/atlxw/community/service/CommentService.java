package com.atlxw.community.service;

import javax.servlet.http.HttpServletRequest;

public interface CommentService {
    String handleNewComment(Integer to_comment, Integer to_comment_type, String content, HttpServletRequest request);

    String getTopComment(int to_comment, int to_comment_type);

    String getRemainAllComment(int to_comment, int to_comment_type);

    String likeOrDislike(int commentId, int likeOrDislike);

    /**
     * 根据用户的id = commentator，来获取该用户所有的评论
     * @param commentator  评论者的id（用户id）
     * @return
     */
    String getCommentsBy(int commentator);
}

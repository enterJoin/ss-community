package com.atlxw.community.service;

import javax.servlet.http.HttpServletRequest;

public interface AnswerService {
    /**
     * 给id为which_question的问题中插入新的回答
     * @param which_question   问题id
     * @param content          回答的内容
     * @param request          请求对象
     * @return
     */
    String insertNewAnswer(int which_question, String content, HttpServletRequest request);


    /**
     * 获得最新的回答
     * @param which_question  问题id
     * @param pageIndex       当前请求的页数
     * @param request         请求对象
     * @return
     */
    String getSomeAnswerNew(int which_question, int pageIndex, HttpServletRequest request);

    /**
     * 获得最热的回答
     * @param which_question  问题id
     * @param pageIndex       当前请求的页数
     * @param request         请求对象
     * @return
     */
    String getSomeAnswerPopular(int which_question, int pageIndex, HttpServletRequest request);

    /**
     * 获得该问题的总回答数
     * @param which_question   问题id
     * @return
     */
    String getTotalPageCount(Integer which_question);

    /**
     * 获得具体的回答  包括用户的id, nickname, head_photo_url
     * @param answerId   回答的id
     * @param request    request对象  可获得用户的id
     * @return
     */
    String getSpecificAnswerById(Integer answerId, HttpServletRequest request);

    /**
     * 对回答的id为answerId进行点赞或者踩
     * @param answerId       回答的id
     * @param likeOrDislike  0表示点踩  1表示点赞
     * @return
     */
    String likeOrDislike(int answerId, int likeOrDislike);

    /**
     * 根据用户id来获取该用户所写的所有的回答
     * @param authorId
     * @return
     */
    String getAnswersWriteBy(int authorId);

    /**
     * 根据用户的id来获取 用户收藏的回答 在collection表中type = 0
     * @param collector
     * @return
     */
    String getAnswersCollectedBy(int collector);

    /**
     * 根据回答的id来修改回答
     * @param editId
     * @param request
     * @return
     */
    String getAnswerToEdit(int editId, HttpServletRequest request);

    /**
     * 修改回答的内容
     * @param editId
     * @param newContent
     * @param request
     * @return
     */
    String updateAnswer(int editId, String newContent, HttpServletRequest request);

    /**
     * 根据回答的id来删除该回答以及该回答所有相关的记录
     * @param answerId
     * @param request
     * @return
     */
    String deleteAnswer(int answerId, HttpServletRequest request);
}

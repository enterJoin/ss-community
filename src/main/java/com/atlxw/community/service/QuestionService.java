package com.atlxw.community.service;

import javax.servlet.http.HttpServletRequest;

public interface QuestionService {
    /**
     * 添加一个问题（在数据库中插入）
     * @param title    问题的标题
     * @param detail   问题的内容
     * @param request  request对象
     * @return
     */
    String insertNewQuestion(String title, String detail, HttpServletRequest request);

    /**
     * 获得最新的问题  根据提交时间来排序
     * @param page
     * @param request
     * @return
     */
    String getSomeQuestionNew(Integer page, HttpServletRequest request);

    /**
     * 获得最热的问题  根据browse_count来排序
     * @param page
     * @param request
     * @return
     */
    String getSomeQuestionPopular(Integer page, HttpServletRequest request);

    /**
     * 得到问题的总页数
     * @return
     */
    String getTotalPageCount();

    /**
     * 根据questionId来展示详细的question信息和用户信息、点赞数、收藏数等
     * @param questionId
     * @param request
     * @return
     */
    String getSpecificQuestionById(int questionId, HttpServletRequest request);

    /**
     * 给问题点赞或者踩
     * @param questionId     问题的id
     * @param likeOrDislike  1表示点赞  0表示踩
     * @return
     */
    String likeOrDislike(int questionId, int likeOrDislike);

    /**
     * 根据用户id来获取该用户所有的提出的问题
     * @param questioner
     * @return
     */
    String getQuestionsAskedBy(int questioner);

    /**
     * 根据用户的id来获取用户收藏的问题
     * @param collector  用户id
     * @return
     */
    String getQuestionsCollectedBy(int collector);

    /**
     * 修改id为editId的问题
     * @param editId
     * @return
     */
    String getQuestionToEdit(int editId, HttpServletRequest request);

    /**
     * 修改问题数据库的记录
     * @param editId
     * @param newTitle
     * @param newDetail
     * @param request
     * @return
     */
    String updateQuestion(int editId, String newTitle, String newDetail, HttpServletRequest request);

    /**
     * 删除问题
     * @param questionId
     * @param request
     * @return
     */
    String deleteQuestion(int questionId, HttpServletRequest request);
}

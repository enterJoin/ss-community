package com.atlxw.community.service;

import javax.servlet.http.HttpServletRequest;

public interface ArticleService {
    String insertNewArticle(String title, String content, HttpServletRequest request);

    String getTotalPageCount();

    String getSomeArticleNew(int page, HttpServletRequest request);

    String getSomeArticlePopular(int page, HttpServletRequest request);

    String likeOrDislike(int parseInt, int parseInt1);

    String getSpecificArticleById(int articleId, HttpServletRequest request);

    /**
     * 获取用户id为authorId写的文章
     * @param authorId   用户id
     * @return
     */
    String getArticlesWriteBy(int authorId);

    /**
     * 根据用户的id来获取其收藏的文本（回答、文章、问题）
     * @param collector
     * @return
     */
    String getArticlesCollectedBy(int collector);

    /**
     * 获得文章的id，从而得到文章的其它属性  然后返回即可
     * @param editId
     * @param request
     * @return
     */
    String getArticleToEdit(int editId, HttpServletRequest request);

    /**
     * 根据articleId来删除文章的id
     * @param articleId
     * @param request
     * @return
     */
    String deleteArticle(Integer articleId, HttpServletRequest request);

    /**
     * 根据id来更新文章的信息
     * @param editId
     * @param newTitle
     * @param newContent
     * @param request
     * @return
     */
    String updateArticle(int editId, String newTitle, String newContent, HttpServletRequest request);
}

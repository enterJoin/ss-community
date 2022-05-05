package com.atlxw.community.dao;

import com.atlxw.community.entity.Article;
import com.atlxw.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface ArticleDao {
    /**
     * 插入新文章
     * @param article
     * @return
     */
    int insertNewArticle(Article article);

    /**
     * 得到一共有多少个文章
     * @return
     */
    double getAllArticleCount();

    /**
     * 按照最新的日期 获得指定页数和大小的文章对象！
     * @param start
     * @param count
     * @return
     */
    List<Article> getSomeArticleNew(@Param("start") int start, @Param("count") int count);

    /**
     * 根据文章id获取作者信息
     * @param id
     * @return
     */
    List<User> getAuthorOf(Integer id);

    /**
     * 根据id获取评论数量
     * @param id
     * @return
     */
    int getCommentCount(Integer id);

    /**
     * 获得所有文章的内容
     * @return
     */
    List<String> getAllArticleContent();

    /**
     * 在collection表中根据article_id获得一共有多少人收藏了该article
     * @param article_id
     * @return
     */
    Integer getCollectionCount(Integer article_id);

    /**
     * 在agreeornot表中获得类型为文章并且是踩的  对应的article_id的记录总和
     * @param article_id
     * @return
     */
    Integer getDislikeCount(Integer article_id);

    /**
     * 在agreeornot表中获得类型为文章并且是赞的  对应的article_id的记录总和
     * @param article_id
     * @return
     */
    Integer getLikeCount(Integer article_id);

    /**
     * 按照热度(点赞数) 获得指定页数和大小的文章对象！
     * @param start
     * @param count
     * @return
     */
    List<Article> getSomeArticlePopular(@Param("start") int start, @Param("count") int count);

    /**
     * 将文章id为articleId的文章的浏览次数 + 1
     * @param articleId
     */
    void beBrowsed(int articleId);

    /**
     * 获取id为userId的用户写了多少篇文章
     * @param userId
     * @return
     */
    Integer getArticleCountWriteBy(Integer userId);

    /**
     * 根据文章id返回文章对象
     * @param articleId
     * @return
     */
    List<Article> getArticleById(int articleId);

    /**
     * 只需要获得 id、title、submit_time、last_update_time即可
     * @param authorId  用户id
     * @return
     */
    List<Article> getArticlesWriteBy(int authorId);

    /**
     * 写这条回答的作者的nickname
     * where条件为 to_comment = #{to_comment} and to_comment_type = 1
     * @param to_comment  被评论的id
     * @return
     */
    List<String> getAuthorNicknameByArticleId(Integer to_comment);

    /**
     * 根据用户的id来获取用户所写的文章
     * collection表中where条件为: collection_type = 1 and collector = #{collector}
     * article表中只需要 id, last_update_time, submit_time, title 字段
     * @param collector 用户id
     * @return
     */
    List<Article> getArticlesCollectedBy(int collector);

    /**
     * 这个dao方法里面包含多个sql语句
     * 不仅要删除这篇文章，还要涉及到该文章的收藏、评论、点赞、踩的信息都删除
     * @param articleId
     */
    void deleteArticleAndAssociations(Integer articleId);

    /**
     * 哪找给的新信息更新文章
     * @param articleId
     * @param newTitle
     * @param newContent
     * @return
     */
    int updateArticle(int articleId, String newTitle, String newContent);

    /**
     * 根据title或者content来匹配
     * @param q
     * @return
     */
    List<Map<String, Object>> searchArticleInNaturalLanguageMode(String q);
}

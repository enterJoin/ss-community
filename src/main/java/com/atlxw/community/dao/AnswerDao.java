package com.atlxw.community.dao;

import com.atlxw.community.entity.Answer;
import com.atlxw.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface AnswerDao {
    /**
     * 获取answer表中所有的content的内容
     * @return
     */
    List<String> getAllAnswerContent();

    /**
     * 根据collection表中的collection_id字段判断有多少人收藏了该回答answer
     * @param answer_id
     * @return
     */
    Integer getCollectionCount(Integer answer_id);

    /**
     * 在agreeornot表中获得类型为回答并且是踩的  对应的answer_id的记录总和
     * @param answer_id
     * @return
     */
    Integer getDislikeCount(Integer answer_id);

    /**
     * 在agreeornot表中获得类型为回答并且是赞的  对应的answer_id的记录总和
     * @param answer_id
     * @return
     */
    Integer getLikeCount(Integer answer_id);

    /**
     * 获取id为userId的用户有多少的回答次数
     * @param userId
     * @return
     */
    Integer getAnswerCountByUserId(Integer userId);

    /**
     * 根据answer的id来获取对象
     * @param to_comment   Answer对象id
     * @return
     */
    List<Answer> getAnswerById(Integer to_comment);

    /**
     * 插入一条回答记录
     * @param answer
     * @return
     */
    int insertNewAnswer(Answer answer);

    /**
     * 根据答案提交的时间 来排序
     * @param which_question   问题的id
     * @param start            回答的起始索引
     * @param count            回答的个数
     * @return
     */
    List<Answer> getSomeAnswerNew(int which_question, int start, int count);

    /**
     * 根据答案的热度 来排序
     * @param which_question   问题的id
     * @param start            回答的起始索引
     * @param count            回答的个数
     * @return
     */
    List<Answer> getSomeAnswerPopular(int which_question, int start, int count);

    /**
     * 获得id为which_question的问题的总的回答次数
     * @param which_question   指定问题的id
     * @return
     */
    int getAllAnswerCountOf(int which_question);

    /**
     * 根据回答的id来获得用户的信息  只需要获得用户的id, nickname, head_photo_url三个属性即可
     * @param answerId   回答的id
     * @return
     */
    List<User> getAnswererOf(Integer answerId);

    /**
     * 根据answerId来获取该回答的评论数量
     * @param answerId   回答的id
     * @return
     */
    int getCommentCount(Integer answerId);

    /**
     * 将该answererId对应的浏览数 + 1
     * @param answererId  回答的id
     */
    void beBrowsed(Integer answererId);

    /**
     * 根据回答id来获取问题的标题
     * @param answerId
     * @return
     */
    String getQuestionTitleByAnswerId(Integer answerId);

    /**
     * 根据回答的id获得该问题的总回答次数
     * @param answerId
     * @return
     */
    Integer getQuestionAnswerCountByAnswerId(Integer answerId);

    /**
     * 根据用户id来获取该用户所写的所有的回答
     * 只需要获得 id, content, which_question, answer_time, last_update_time 字段
     * @param answerer
     * @return
     */
    List<Answer> getAnswersWriteBy(int answerer);

    /**
     * 写这条回答的作者的nickname
     * where条件为 to_comment = #{to_comment} and to_comment_type = 0
     * @param to_comment  被评论的id
     * @return
     */
    List<String> getAnswererNicknameByAnswerId(Integer to_comment);

    /**
     * 根据用户的id来获得该用户收藏的回答
     * 在answer表中需要查询的字段为: id, content, which_question, answer_time, last_update_time
     * 在collection表中的条件为: collection_type = 0 and collector = #{collector}
     * @param collector
     * @return
     */
    List<Answer> getAnswersCollectedBy(int collector);

    /**
     * 根据回答的id来更新内容
     * @param editId
     * @param newContent
     * @return
     */
    int updateAnswer(int editId, String newContent);

    /**
     * 删除id为answerId的回答 并且删除所有和它相关的东西、评论、点赞或踩、收藏
     * @param answerId
     */
    void deleteAnswerAndAssociations(int answerId);

    /**
     * 根据content来进行搜索
     * @param q
     * @return
     */
    List<Map<String, Object>> searchAnswerInNaturalLanguageMode(String q);
}

package com.atlxw.community.dao;

import com.atlxw.community.entity.Question;
import com.atlxw.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface QuestionDao {
    /**
     * 获得所有问题的详情
     * @return
     */
    List<String> getAllQuestionDetail();

    /**
     * 在collection表中根据question_id来获得应该有多少人收藏了该问题
     * @param question_id
     * @return
     */
    Integer getCollectionCount(Integer question_id);

    /**
     * 在agreeornot表中获得类型为问题并且是踩的  对应的question_id的记录总和
     * @param question_id
     * @return
     */
    Integer getDislikeCount(Integer question_id);

    /**
     * 在agreeornot表中获得类型为问题并且是赞的  对应的question_id的记录总和
     * @param evaluate_id
     * @return
     */
    Integer getLikeCount(Integer evaluate_id);

    /**
     * 插入一个Question对象
     * @param question
     * @return
     */
    int insertNewQuestion(Question question);

    /**
     * 获得所有的问题个数
     * @return
     */
    int getAllQuestionCount();

    /**
     * 根据热度（浏览量） 由大到小  来获取指定个数的问题
     * @param start
     * @param count
     * @return
     */
    List<Question> getSomeQuestionPopular(@Param("start") int start, @Param("count") int count);

    /**
     * 根据提交时间  由近到远 来获取指定个数的问题
     * @param start
     * @param count
     * @return
     */
    List<Question> getSomeQuestionNew(@Param("start") int start, @Param("count") int count);

    /**
     * 根据问题ID获得User的信息
     * @param id
     * @return
     */
    List<User> getQuestionerOf(Integer id);

    /**
     * 根据问题的ID来获取该问题的回答数量
     * @param id
     * @return
     */
    int getAnswerCount(Integer id);

    /**
     * 将id为questionId问题的浏览次数 + 1
     * @param questionId
     */
    Integer beBrowsed(int questionId);

    /**
     * 根据questionId来获取Question对象
     * @param questionId
     * @return
     */
    List<Question> getQuestionById(int questionId);

    /**
     * 根据用户来获取所有该用户提问的问题，只需要 id, title, submit_time, last_update_time 这四个字段
     * @param questioner  用户id
     * @return
     */
    List<Question> getQuestionsAskedBy(int questioner);

    /**
     * collection表中的where条件为: collection_type = 2 and collector = #{collector}
     * question表中需要查询的字段为: id, title, last_update_time, submit_time
     * @param collector
     * @return
     */
    List<Question> getQuestionsCollectedBy(int collector);

    /**
     * 按照questionId来修改问题的title和detail
     * @param questionId
     * @param newTitle
     * @param newDetail
     * @return
     */
    int updateQuestion(int questionId, String newTitle, String newDetail);

    /**
     * 删除这个问题以及这个问题关联的一切  回答、收藏、点赞和踩、回答的回复、回答的回复的点赞和踩
     * @param questionId
     * @return
     */
    int deleteQuestionAndAssociations(int questionId);

    /**
     * 根据title或者detail来搜索
     * @param q
     * @return
     */
    List<Map<String, Object>> searchQuestionInNaturalLanguageMode(String q);
}

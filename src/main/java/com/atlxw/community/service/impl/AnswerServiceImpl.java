package com.atlxw.community.service.impl;

import com.atlxw.community.dao.AnswerDao;
import com.atlxw.community.dao.CollectionDao;
import com.atlxw.community.dao.FollowDao;
import com.atlxw.community.entity.Answer;
import com.atlxw.community.entity.Article;
import com.atlxw.community.entity.User;
import com.atlxw.community.scheduled.CleanUnusedContentImageSchedule;
import com.atlxw.community.service.AnswerService;
import com.atlxw.community.service.RedisService;
import com.atlxw.community.service.UserService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.TokenUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnswerServiceImpl implements AnswerService {
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private FollowDao followDao;
    @Autowired
    private CollectionDao collectionDao;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    private static final int howManyForEachPage = 3;//每页显示几篇回答
    @Override
    public String insertNewAnswer(int which_question, String content, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");
        //获取当前用户登录的id
        Integer uid = TokenUtils.getUid(request);
        if(uid != null){    //如果说用户确实是登录的状态，那么才可以回答
            Answer answer = new Answer(null,
                    which_question,
                    uid,
                    content,
                    new Timestamp(System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()),
                    0);
            if(answerDao.insertNewAnswer(answer) == 1){   //如果问题插入成功  受影响的行为1
                resultMap.put("success","true");
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getSomeAnswerNew(int which_question, int pageIndex, HttpServletRequest request) {
        return getSomeAnswer(which_question, 0, pageIndex);
    }


    @Override
    public String getSomeAnswerPopular(int which_question, int pageIndex, HttpServletRequest request) {
        return getSomeAnswer(which_question, 1, pageIndex);
    }

    private String getSomeAnswer(int which_question, int type, int pageIndex) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Answer> someAnswer = null;

        //防止用户请求非法页面   根据问题的id来获得回答的个数，来确定一共有多少页
        int totalPageCount = answerDao.getAllAnswerCountOf(which_question) / howManyForEachPage + 1;
        if(pageIndex > totalPageCount){   //如果请求的页数比最大页数还要大   那么就为最大页数
            pageIndex = totalPageCount;
        }
        if(pageIndex < 1){                //如果请求的页数比1还要小  那么就为1
            pageIndex = 1;
        }

        if(type == 0){    //如果是获得最新的问题
            someAnswer = answerDao.getSomeAnswerNew(which_question, (pageIndex - 1) * howManyForEachPage, howManyForEachPage);
        } else if(type == 1){     //如果是获得最热的问题
            someAnswer = answerDao.getSomeAnswerPopular(which_question, (pageIndex - 1) * howManyForEachPage, howManyForEachPage);
        }

        if(someAnswer != null){       //如果获得的回答不为空
            for(Answer answer : someAnswer){     //遍历每一个回答
                //每个问题需要装载的内容都要放在这个map里面，比如user的信息，问题的收藏、点赞数等
                Map<String, Object> eachAnswer = new HashMap<>();
                //首先放入回答的主体对象
                eachAnswer.put("answer", answer);
                //得到写这个问题的回答的作者   根据问题ID获得User的信息
                List<User> answerers = answerDao.getAnswererOf(answer.getId());
                System.out.println("-------------------------------" + answerers + "--------------------------------------");

                if(answerers.size() == 1){        //如果查出来获得这个问题的作者有且只有一个
                    User answerer = answerers.get(0);   //那么就获得这个回答问题的用户
                    //获得用户的id,nickname,head_photo_url
                    eachAnswer.put("answererId", answerer.getId());
                    eachAnswer.put("answererNickName", answerer.getNickname());
                    eachAnswer.put("answererHeadPhotoUrl", answerer.getHead_photo_url());
                }

                //获得收藏次数
                int collectionCount = redisService.getCollectionCount(answer.getId(), 0);
                eachAnswer.put("collectionCount", collectionCount);

                //获取点赞次数
                int likeCount = redisService.getEvaluateCount(answer.getId(),0, 1);
                eachAnswer.put("likeCount", likeCount);

                //回答评论数量
                int commentCount = answerDao.getCommentCount(answer.getId());
                eachAnswer.put("commentCount", commentCount);

                //尝试查找该回答中是否有图片链接，如果有就加入到结果集中，将在网页里显示预览图
                List<String> imagesOfContent = CleanUnusedContentImageSchedule.analyzeContent(answer.getContent());
                if(imagesOfContent != null && imagesOfContent.size() > 0){
                    eachAnswer.put("previewImageUrl", imagesOfContent.get(0));
                } else {
                    eachAnswer.put("previewImageUrl", "");
                }

                //将这个map集成了用户信息、问题信息、收藏、点赞数、第一个图片的集合放到List中
                resultList.add(eachAnswer);
            }
        }
        try {
            return JacksonUtils.obj2json(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public String getTotalPageCount(Integer which_question) {
        Map<String, Integer> resultMap = new HashMap<>();
        double pageCount = Math.ceil(answerDao.getAllAnswerCountOf(which_question) / (double)howManyForEachPage );

        resultMap.put("totalPageCount", (int)pageCount);
        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getSpecificAnswerById(Integer answerId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        //获取多有的articles对象，但正常情况下只有一个
        List<Answer> answers = answerDao.getAnswerById(answerId);

        //如果文章不存在或者不为1个的话，那么就直接返回
        if(answers == null || answers.size() != 1) return null;

        Answer answer = answers.get(0);  //获得这个文章对象
        Integer answererId = answer.getAnswerer();  //获得该文章的作者的id

        if(answererId != null){   //如果作者ID不为空的话
            try{
                resultMap.putAll(userService.getUserInfoToShowAside(answererId));
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

            resultMap.put("answer", answer);   //放置文章对象
            resultMap.put("commentCount", answerDao.getCommentCount(answererId));   //放置总评论数
            resultMap.put("collectionCount", redisService.getCollectionCount(answererId, 0));   //放置总收藏数，1位类型(文章)
            resultMap.put("likeCount", redisService.getEvaluateCount(answererId, 0, 1));  //放置总点赞数
            //存放问题的id
            resultMap.put("questionId", answer.getWhich_question());
            //存放问题的标题
            resultMap.put("questionTitle", answerDao.getQuestionTitleByAnswerId(answerId));
            //存放该回答所属的问题的总回答次数
            resultMap.put("questionAnswerCount", answerDao.getQuestionAnswerCountByAnswerId(answerId));

            //在用户未登录的情况下，默认未关注、未收藏
            resultMap.put("isAlreadyFollow", "false");
            resultMap.put("isAlreadyCollect", "false");

            //判断用户是否登陆，如果登录，需要判断用户是否已经关注作者、是否已经收藏文章
            Integer loginUid = TokenUtils.getUid(request);    //获取当前用户的id
            if(loginUid != null){    //如果用户是已登录的话
                //如果用户已经关注了这个作者的话
                if(followDao.isAlreadyFollow(loginUid, answererId) >= 1){
                    resultMap.put("isAlreadyFollow", "true");
                }
                //如果该用户已经收藏过这个文章
                if(collectionDao.isAlreadyCollect(answererId, loginUid, 1) >= 1){
                    resultMap.put("isAlreadyCollect", "true");
                }
            }
            //将该answer对应的浏览数 + 1
            answerDao.beBrowsed(answererId);
        }

        return JacksonUtils.mapToJson(resultMap);

    }

    @Override
    public String likeOrDislike(int answerId, int likeOrDislike) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");

        //如果likeOrDislike是正常的数值
        if(likeOrDislike == 0 || likeOrDislike == 1){
            redisService.likeOrDislike(answerId, 0, likeOrDislike);
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getAnswersWriteBy(int answerer) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的文章
        List<Answer> answers = answerDao.getAnswersWriteBy(answerer);

        //保存每个回答的 id, content, which_question, answer_time, last_update_time, 还有对应问题的标题
        for(Answer answer : answers){
            Map<String, Object> eachAnswer = new HashMap<>();
            eachAnswer.put("answerId", answer.getId());
            eachAnswer.put("answerContent", answer.getContent());
            eachAnswer.put("questionId", answer.getWhich_question());
            eachAnswer.put("lastUpdateTime",
                    answer.getLast_update_time() == null ? answer.getAnswer_time() : answer.getLast_update_time());
            eachAnswer.put("questionTitle", answerDao.getQuestionTitleByAnswerId(answer.getId()));

            resultList.add(eachAnswer);   //在resultList当中加入这个文章的这三个属性
        }

        //将这些文章按照最近修改时间排序
        resultList.sort((a1, a2) -> {
            Timestamp t1 = (Timestamp) a1.get("lastUpdateTime");
            Timestamp t2 = (Timestamp) a2.get("lastUpdateTime");
            return t2.compareTo(t1);
        });

        try {
            return JacksonUtils.obj2json(resultList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAnswersCollectedBy(int collector) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的文章
        List<Answer> answers = answerDao.getAnswersCollectedBy(collector);

        //保存每个回答的 id, content, which_question, answer_time, last_update_time, 还有对应问题的标题
        for(Answer answer : answers){
            Map<String, Object> eachAnswer = new HashMap<>();
            eachAnswer.put("answerId", answer.getId());
            eachAnswer.put("answerContent", answer.getContent());
            eachAnswer.put("questionId", answer.getWhich_question());
            eachAnswer.put("lastUpdateTime",
                    answer.getLast_update_time() == null ? answer.getAnswer_time() : answer.getLast_update_time());
            eachAnswer.put("questionTitle", answerDao.getQuestionTitleByAnswerId(answer.getId()));

            resultList.add(eachAnswer);   //在resultList当中加入这个文章的这三个属性
        }

        //将这些文章按照最近修改时间排序  降序
        resultList.sort((a1, a2) -> {
            Timestamp t1 = (Timestamp) a1.get("lastUpdateTime");
            Timestamp t2 = (Timestamp) a2.get("lastUpdateTime");
            return t2.compareTo(t1);
        });

        try {
            return JacksonUtils.obj2json(resultList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String getAnswerToEdit(int editId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");
        resultMap.put("reason","找不到此文章！");

        Integer uid = TokenUtils.getUid(request);
        List<Answer> answers = answerDao.getAnswerById(editId);

        //如果对应的回答有且只有一个
        if(answers != null && answers.size() == 1){
            //并且  当前用户已登录并且当前登录的用户就是这个作者
            if(uid != null && uid.equals(answers.get(0).getAnswerer())){
                Answer answer = answers.get(0);
                resultMap.put("answerId", answer.getId());
                resultMap.put("questionId", answer.getWhich_question());
                resultMap.put("questionTitle", answerDao.getQuestionTitleByAnswerId(answer.getId()));
                resultMap.put("content", answer.getContent());
                resultMap.put("success", "true");
                resultMap.remove("reason");
            } else {
                resultMap.put("reason", "无权限修改！");
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String updateAnswer(int editId, String newContent, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");

        Integer uid = TokenUtils.getUid(request);
        List<Answer> answers = answerDao.getAnswerById(editId);
        //如果对应的回答有且只有一个 并且  当前用户已登录并且当前登录的用户就是这个作者
        if(answers != null && answers.size() == 1 && uid != null && uid.equals(answers.get(0).getAnswerer())){
            if(newContent != null){   //如果内容不为空的话  就更新内容
                if(answerDao.updateAnswer(editId, newContent) == 1){    //如果数据库也更新成功的话
                    resultMap.put("success","true");
                }
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Transactional
    @Override
    public String deleteAnswer(int answerId, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");

        Integer uid = TokenUtils.getUid(request);
        List<User> users = answerDao.getAnswererOf(answerId);

        //如果该回答的用户有且只有一个 并且 当前登录的用户和这个回答的作者是一样的！
        if(users != null && users.size() == 1 && uid != null && uid.equals(users.get(0).getId())){
            answerDao.deleteAnswerAndAssociations(answerId);
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }
}

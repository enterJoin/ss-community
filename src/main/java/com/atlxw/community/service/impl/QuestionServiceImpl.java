package com.atlxw.community.service.impl;

import com.atlxw.community.dao.CollectionDao;
import com.atlxw.community.dao.FollowDao;
import com.atlxw.community.dao.QuestionDao;
import com.atlxw.community.entity.Article;
import com.atlxw.community.entity.Question;
import com.atlxw.community.entity.User;
import com.atlxw.community.scheduled.CleanUnusedContentImageSchedule;
import com.atlxw.community.service.QuestionService;
import com.atlxw.community.service.RedisService;
import com.atlxw.community.service.UserService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.TokenUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private FollowDao followDao;
    @Autowired
    private CollectionDao collectionDao;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    private static final int howManyForEachPage = 3;//每页显示几个问题

    @Transactional
    @Override
    public String insertNewQuestion(String title, String detail, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");
        //获得当前提问题的用户的ID
        Integer questioner = TokenUtils.getUid(request);

        //如果标题或者用户ID为空的话，那么就直接返回
        if(title == null || questioner == null) return JacksonUtils.mapToJson(resultMap);

        //创造Question对象
        Question question = new Question(null,
                title,
                detail,
                questioner,
                new Timestamp(System.currentTimeMillis()),
                new Timestamp(System.currentTimeMillis()),
                0);
        //如果插入成功
        if(questionDao.insertNewQuestion(question) == 1){   //如果插入成功
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getSomeQuestionNew(Integer page, HttpServletRequest request) {
        return getSomeQuestion(0, page);
    }

    @Override
    public String getSomeQuestionPopular(Integer page, HttpServletRequest request) {
        return getSomeQuestion(1, page);
    }

    @Override
    public String getTotalPageCount() {
        //向上取整
        double pageCount = Math.ceil(questionDao.getAllQuestionCount() / (double)howManyForEachPage);
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("totalPageCount",(int)pageCount);
        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getSpecificQuestionById(int questionId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Question> questions = questionDao.getQuestionById(questionId);

        //如果问题不是有且只有一个，那么直接返回空
        if(questions == null || questions.size() != 1) return null;

        Question question = questions.get(0);
        //获得问题的提问者的id
        Integer questionerId = question.getQuestioner();

        if(questionerId != null){     //如果作者存在  那么就找到需要顺带显示的用户信息，比如用户昵称、头像
            try {
                resultMap.putAll(userService.getUserInfoToShowAside(questionerId));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            resultMap.put("question", question);
            resultMap.put("answerCount", questionDao.getAnswerCount(questionId));
            resultMap.put("collectionCount", redisService.getCollectionCount(questionId, 2));
            resultMap.put("likeCount", redisService.getEvaluateCount(questionId, 2, 1));

            //在用户未登录的情况下 都是未关注未收藏
            resultMap.put("isAlreadyFollow", "false");
            resultMap.put("isAlreadyCollect", "false");

            //获取用户登录的id
            Integer loginUid = TokenUtils.getUid(request);
            if(loginUid != null){       //如果用户是在登录的状态
                if(followDao.isAlreadyFollow(loginUid,questionId) >= 1){   //看是否已经关注了作者
                    resultMap.put("isAlreadyFollow", "true");
                }
                //如果已经收藏了这个问题
                if(collectionDao.isAlreadyCollect(questionId,loginUid,2) >= 1){
                    resultMap.put("isAlreadyCollect", "true");
                }
            }

            //将该问题的浏览次数 + 1
            questionDao.beBrowsed(questionId);
        }
        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String likeOrDislike(int questionId, int likeOrDislike) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");

        if(likeOrDislike == 0 || likeOrDislike == 1){
            //id为questionId，类型为2(表示问题)，likeOrDislike表示点赞还是踩
            redisService.likeOrDislike(questionId, 2, likeOrDislike);
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getQuestionsAskedBy(int questioner) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的文章
        List<Question> questions = questionDao.getQuestionsAskedBy(questioner);

        for(Question question : questions){
            Map<String, Object> eachQuestion = new HashMap<>();
            eachQuestion.put("questionId", question.getId());
            eachQuestion.put("lastUpdateTime",
                    question.getLast_update_time() == null ? question.getSubmit_time() : question.getLast_update_time());
            eachQuestion.put("title", question.getTitle());

            if(questionDao.getAnswerCount(question.getId()) >= 1){   //如果该问题有回答
                eachQuestion.put("hasAnswer", "true");
            } else {     //如果该问题没有回答
                eachQuestion.put("hasAnswer", "false");
            }

            resultList.add(eachQuestion);   //在resultList当中加入这个文章的这三个属性
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
    public String getQuestionsCollectedBy(int collector) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的文章  collection_type = 2
        List<Question> questions = questionDao.getQuestionsCollectedBy(collector);

        //需要填充的字段为: id, title, last_update_time, submit_time
        for(Question question : questions){
            Map<String, Object> eachQuestion = new HashMap<>();
            eachQuestion.put("questionId", question.getId());
            eachQuestion.put("lastUpdateTime",
                    question.getLast_update_time() == null ? question.getSubmit_time() : question.getLast_update_time());
            eachQuestion.put("title", question.getTitle());

            resultList.add(eachQuestion);   //在resultList当中加入这个文章的这三个属性
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
    public String getQuestionToEdit(int editId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");
        resultMap.put("reason","找不到此问题！");

        List<Question> questions = questionDao.getQuestionById(editId);

        if(questions != null && questions.size() == 1){      //如果改文章有且仅有一个
            Integer uid = TokenUtils.getUid(request);      //获取当前用户登录的id

            if(uid != null && uid.equals(questions.get(0).getQuestioner())){   //如果当前用户已登录并且就是提问者
                resultMap.put("questionId",questions.get(0).getId());
                resultMap.put("title",questions.get(0).getTitle());
                resultMap.put("detail",questions.get(0).getDetail());
                resultMap.put("success","true");
                resultMap.remove("reason");
            } else {       //如果不是此用户或者根本也没有登录
                resultMap.put("reason", "无权限修改！");
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String updateQuestion(int editId, String newTitle, String newDetail, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");

        Integer uid = TokenUtils.getUid(request);
        List<Question> questions = questionDao.getQuestionById(editId);

        //如果这个问题有且只有一个  并且提问者就是为当前登录的用户
        if(questions != null && questions.size() == 1 && uid != null && uid.equals(questions.get(0).getQuestioner())){
            //如果标题和内容都合法的话
            if(newTitle != null && newTitle.length() < 50 && newDetail != null) {
                if(questionDao.updateQuestion(editId, newTitle, newDetail) == 1){   //如果数据库都修改成功的话
                    resultMap.put("success","true");
                }
            }

        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Transactional
    @Override
    public String deleteQuestion(int questionId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");

        Integer uid = TokenUtils.getUid(request);
        List<Question> questions = questionDao.getQuestionById(questionId);

        if(questions != null && questions.size() == 1 && uid != null && uid.equals(questions.get(0).getQuestioner())){
            questionDao.deleteQuestionAndAssociations(questionId);    //如果数据库修改成功
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 根据类型type来获得最新或者最热的问题
     * @param type  0表示获得最新的问题  1表示获得最热的问题
     * @param page  表示当前的是第几页
     * @return
     */
    private String getSomeQuestion(int type, Integer page) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Question> someQuestion = null;
        //防止用户请求非法页面
        int totalPageCount = questionDao.getAllQuestionCount() / howManyForEachPage + 1;
        if(page > totalPageCount){   //如果请求的页数比最大页数还要大   那么就为最大页数
            page = totalPageCount;
        }
        if(page < 1){                //如果请求的页数比1还要小  那么就为1
            page = 1;
        }

        if(type == 0){    //如果是获得最新的问题
            someQuestion = questionDao.getSomeQuestionNew((page - 1) * howManyForEachPage, howManyForEachPage);
        } else if(type == 1){     //如果是获得最热的问题
            someQuestion = questionDao.getSomeQuestionPopular((page - 1) * howManyForEachPage, howManyForEachPage);
        }

        if(someQuestion != null){     //如果返回的问题不为空的话
            for(Question question: someQuestion){    //轮流遍历这些问题
                //每个问题需要装载的内容都要放在这个map里面，比如user的信息，问题的收藏、点赞数等
                Map<String, Object> eachQuestion = new HashMap<>();
                //首先放入问题主体对象
                eachQuestion.put("question", question);
                //得到提出这个问题的作者   根据问题ID获得User的信息
                List<User> questioners = questionDao.getQuestionerOf(question.getId());

                if(questioners.size() == 1){   //如果说该提问者有且只有一个
                    User questioner = questioners.get(0);
                    //获得用户的id,nickname,head_photo_url
                    eachQuestion.put("questionerId", questioner.getId());
                    eachQuestion.put("questionerNickName", questioner.getNickname());
                    eachQuestion.put("questionerHeadPhotoUrl", questioner.getHead_photo_url());
                }

                //获得收藏次数
                int collectionCount = redisService.getCollectionCount(question.getId(), 2);
                eachQuestion.put("collectionCount", collectionCount);

                //获取点赞次数
                int likeCount = redisService.getEvaluateCount(question.getId(),2, 1);
                eachQuestion.put("likeCount", likeCount);

                //获取回答数量
                int answerCount = questionDao.getAnswerCount(question.getId());
                eachQuestion.put("answerCount", answerCount);

                //尝试查找问题是否有图片链接，如果有就加入到结果集中，将在网页里显示预览图
                List<String> imagesOfDetail = CleanUnusedContentImageSchedule.analyzeContent(question.getDetail());
                if(imagesOfDetail != null && imagesOfDetail.size() > 0){
                    eachQuestion.put("previewImageUrl", imagesOfDetail.get(0));
                } else {
                    eachQuestion.put("previewImageUrl", "");
                }

                //将这个map集成了用户信息、问题信息、收藏、点赞数、第一个图片的集合放到List中
                resultList.add(eachQuestion);
            }
        }

        try {
            return JacksonUtils.obj2json(resultList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}

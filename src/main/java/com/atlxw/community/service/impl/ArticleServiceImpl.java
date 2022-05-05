package com.atlxw.community.service.impl;

import com.atlxw.community.dao.ArticleDao;
import com.atlxw.community.dao.CollectionDao;
import com.atlxw.community.dao.FollowDao;
import com.atlxw.community.entity.Article;
import com.atlxw.community.entity.User;
import com.atlxw.community.scheduled.CleanUnusedContentImageSchedule;
import com.atlxw.community.service.ArticleService;
import com.atlxw.community.service.RedisService;
import com.atlxw.community.service.SearchService;
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
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    @Autowired
    private FollowDao followDao;

    @Autowired
    private CollectionDao collectionDao;


    private static final int howManyForEachPage = 3; //每页显示3篇文章

    /**
     * 保存新的文章
     * @param title
     * @param content
     * @param request
     * @return
     */
    @Transactional
    @Override
    public String insertNewArticle(String title, String content, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        map.put("success", "false");

        Integer uid = TokenUtils.getUid(request);   //获取当前的登录用户的id

        if(uid != null){   //如果uid不为空的话
            //检验数据的合法性，如果合法的话
            if(title != null && title.length() <50 && content != null){
                //创建新文章对象
                Article article = new Article(null,
                        title,
                        content,
                        uid,
                        new Timestamp(System.currentTimeMillis()),
                        new Timestamp(System.currentTimeMillis()),
                        0);

                //将这个新文章对象 插入到数据库中
                if(articleDao.insertNewArticle(article) == 1){
                    map.put("success", "true");
                }
            }
        }
        return JacksonUtils.mapToJson(map);
    }

    /**
     * 获得页数为多少
     * @return
     */
    @Override
    public String getTotalPageCount() {
        Map<String, Integer> resultMap = new HashMap<>();
        //向上取整
        int totalCount = (int) Math.ceil(articleDao.getAllArticleCount() / howManyForEachPage);

        resultMap.put("totalPageCount", totalCount);

        return JacksonUtils.mapToJson(resultMap);

    }

    /**
     * 根据发布时间获得罪行的文章
     * @param page
     * @param request
     * @return
     */
    @Override
    public String getSomeArticleNew(int page, HttpServletRequest request) {
        return getSomeArticle(0, page);
    }

    /**
     * 根据浏览量获得最新的的文章
     * @param page
     * @param request
     * @return
     */
    @Override
    public String getSomeArticlePopular(int page, HttpServletRequest request) {
        return getSomeArticle(1, page);
    }

    /**
     * 根据文章ID对文章进行赞还是踩
     * @param articleId       文章ID
     * @param likeOrDislike   0表示踩  1表示赞
     * @return
     */
    @Override
    public String likeOrDislike(int articleId, int likeOrDislike) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");

        if(likeOrDislike == 0 || likeOrDislike == 1){
            //1代表的是文章类型！
            redisService.likeOrDislike(articleId, 1, likeOrDislike);
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 根据articleId来获取到该文章的所有对象
     * @param articleId  文章id
     * @param request    request对象
     * @return
     */
    @Override
    public String getSpecificArticleById(int articleId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        //获取多有的articles对象，但正常情况下只有一个
        List<Article> articles = articleDao.getArticleById(articleId);

        //如果文章不存在或者不为1个的话，那么就直接返回
        if(articles == null || articles.size() != 1) return null;

        Article article = articles.get(0);  //获得这个文章对象
        Integer authorId = article.getAuthor();  //获得该文章的作者的id

        if(authorId != null){   //如果作者ID不为空的话
            try{
                resultMap.putAll(userService.getUserInfoToShowAside(authorId));
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

            resultMap.put("article", article);   //放置文章对象
            resultMap.put("commentCount", articleDao.getCommentCount(articleId));   //放置总评论数
            resultMap.put("collectionCount", redisService.getCollectionCount(articleId, 1));   //放置总收藏数，1位类型(文章)
            resultMap.put("likeCount", redisService.getEvaluateCount(articleId, 1, 1));  //放置总点赞数

            //在用户未登录的情况下，默认未关注、未收藏
            resultMap.put("isAlreadyFollow", "false");
            resultMap.put("isAlreadyCollect", "false");

            //判断用户是否登陆，如果登录，需要判断用户是否已经关注作者、是否已经收藏文章
            Integer loginUid = TokenUtils.getUid(request);    //获取当前用户的id
            if(loginUid != null){    //如果用户是已登录的话
                //如果用户已经关注了这个作者的话
                if(followDao.isAlreadyFollow(loginUid, authorId) >= 1){
                    resultMap.put("isAlreadyFollow", "true");
                }
                //如果该用户已经收藏过这个文章
                if(collectionDao.isAlreadyCollect(articleId, loginUid, 1) >= 1){
                    resultMap.put("isAlreadyCollect", "true");
                }
            }
        }

        //被浏览次数 + 1
        articleDao.beBrowsed(articleId);

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getArticlesWriteBy(int authorId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的文章
        List<Article> articles = articleDao.getArticlesWriteBy(authorId);

        for(Article article : articles){
            Map<String, Object> eachArticle = new HashMap<>();
            eachArticle.put("articleId", article.getId());
            eachArticle.put("lastUpdateTime",
                    article.getLast_update_time() == null ? article.getSubmit_time() : article.getLast_update_time());
            eachArticle.put("title", article.getTitle());

            resultList.add(eachArticle);   //在resultList当中加入这个文章的这三个属性
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
    public String getArticlesCollectedBy(int collector) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的文章 type = 1
        List<Article> articles = articleDao.getArticlesCollectedBy(collector);

        //只需要填充 id, last_update_time, submit_time, title
        for(Article article : articles){
            Map<String, Object> eachArticle = new HashMap<>();
            eachArticle.put("articleId", article.getId());
            eachArticle.put("lastUpdateTime",
                    article.getLast_update_time() == null ? article.getSubmit_time() : article.getLast_update_time());
            eachArticle.put("title", article.getTitle());


            resultList.add(eachArticle);   //在resultList当中加入这个文章的这三个属性
        }

        //将这些文章按照最近修改时间排序  降序排序
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
    public String getArticleToEdit(int editId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");
        resultMap.put("reason","找不到此文章！");

        List<Article> articles = articleDao.getArticleById(editId);
        if(articles != null && articles.size() == 1){   //如果得到的articles有且只有一个
            Article article = articles.get(0);
            //再次进行身份确认
            if(TokenUtils.getUid(request).equals(article.getAuthor())){
                resultMap.put("articleId", article.getId());
                resultMap.put("title", article.getTitle());
                resultMap.put("content", article.getContent());
                resultMap.put("success", "true");
                resultMap.remove("reason");
            } else {
                resultMap.put("reason","无权限修改！");
            }
        }

        return JacksonUtils.mapToJson(resultMap);

    }

    @Override
    @Transactional
    public String deleteArticle(Integer articleId, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");

        Integer curUserId = TokenUtils.getUid(request);         //获取当前登录用户id
        List<User> authors = articleDao.getAuthorOf(articleId); //获取作者的id

        //如果作者id有且只有一个并且该作者id就是当前登录的Id
        if(authors != null && authors.size() == 1 && authors.get(0).getId().equals(curUserId)){
            //因为要进行的操作比较多 所以最好需要进行实物操作
            articleDao.deleteArticleAndAssociations(articleId);
            resultMap.put("success", "true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String updateArticle(int editId, String newTitle, String newContent, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");

        Integer loginUid = TokenUtils.getUid(request);
        List<Article> articles = articleDao.getArticleById(editId);

        //如果查到文章有且只有一个 并且 文章的作者和当前登录用户的id是一样的话
        if(articles != null && articles.size() == 1 && articles.get(0).getAuthor().equals(loginUid)){
            //如果信标机不为空并且长度要小于50 并且新的内容也不为空
            if(newTitle != null && newTitle.length() < 50 && newContent != null){
                //那么就更新文章即可
                if (articleDao.updateArticle(editId, newTitle, newContent) == 1) {
                    resultMap.put("success", "true");
                }
            }
        }

        return JacksonUtils.mapToJson(resultMap);

    }

    /**
     * 根据type来判断是获得最新的文章还是获得最受欢迎的文章
     * @param type: 0(最新)  1(最受欢迎)
     * @param page
     * @return
     */
    private String getSomeArticle(int type, int page) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Article> articles = null;

        //防止用户请求非法的页面
        int totalPageCount = (int) (articleDao.getAllArticleCount() / howManyForEachPage + 1);
        if(page > totalPageCount){   //如果页号大于页数最多的页面，那么就是最后一个页面
            page = totalPageCount;
        } else if(page < 1){         //如果页号小于1，那么就赋值1
            page = 1;
        }

        //根据类型获得最新还是最热的文章
        if(type == 0){     //如果类型为0，那么获取最新的文章
            //(page - 1) * howManyForEachPage为当页显示的起始记录，howManyForEachPage为要显示多少条记录
            articles = articleDao.getSomeArticleNew((page - 1) * howManyForEachPage, howManyForEachPage);
        } else if(type == 1){        //如果类型为1，那么获得最热的文章
            articles = articleDao.getSomeArticlePopular((page - 1) * howManyForEachPage, howManyForEachPage);
        }

        //非空判断
        if(articles != null){
            for(Article article: articles){
                Map<String, Object> eachArticle = new HashMap<>();
                //装入文章的这个对象
                eachArticle.put("article", article);
                //文章作者相关的信息
                List<User> users = articleDao.getAuthorOf(article.getId());
                if(users.size() == 1){
                    User user = users.get(0);
                    eachArticle.put("authorId", user.getId());
                    eachArticle.put("authorNickName", user.getNickname());
                    eachArticle.put("authorHeadPhotoUrl", user.getHead_photo_url());
                }

                //获得收藏次数
                int collectionCount = redisService.getCollectionCount(article.getId(), 1);
                eachArticle.put("collectionCount", collectionCount);

                //获得点赞次数
                int likeCount = redisService.getEvaluateCount(article.getId(), 1, 1);
                eachArticle.put("likeCount", likeCount);
                //获得评论数量
                int commentCount = articleDao.getCommentCount(article.getId());
                eachArticle.put("commentCount", commentCount);

                //尝试查找文章内容是否有图片链接，如果有就加入到结果集当中，将在页面显示预览图
                List<String> imagesOfContent = CleanUnusedContentImageSchedule.analyzeContent(article.getContent());
                if(imagesOfContent != null && imagesOfContent.size() > 0){
                    eachArticle.put("previewImageUrl", imagesOfContent.get(0));
                } else {
                    eachArticle.put("previewImageUrl", "");
                }

                //浏览次数白喊在文章主体对象中
                //加入结果集
                resultList.add(eachArticle);
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

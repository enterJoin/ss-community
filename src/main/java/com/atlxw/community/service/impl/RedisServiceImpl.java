package com.atlxw.community.service.impl;

import com.atlxw.community.dao.*;
import com.atlxw.community.service.RedisService;
import com.atlxw.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private CollectionDao collectionDao;
    @Autowired
    private EvaluateDao evaluateDao;

    private static final String[] evaluateTypeMapping = {"answer", "article", "question", "comment"};

    private static final String[] collectionTypeMapping = {"answer", "article", "question"};

    private static final long CACHE_EXPIRE_DURATION = TimeUnit.DAYS.toSeconds(1);//缓存过期时间一天


    @Override
    public Integer getCollectionCount(Integer id, int type) {
        if(type < 0 || type > 2) return null;

        Object countStr = redisUtil.hget("collection:" + collectionTypeMapping[type], id + "");
        Integer res;

        if(countStr == null){
            res = getCollectionCountFromDatabaseAndFlushCache(id, type);
        } else {
            res = Integer.parseInt(countStr + "");
        }

        return res;
    }

    /**
     * 因缓存中没有  就调用数据库中的方法来获取数据，并且查到之后放到redis中
     * @param collection_id       收藏对象的ID
     * @param collection_type     收藏对象的类型  0(回答)  1(文章)  2(问题)，对应的值都在collectionList中，用于区分不同的hash的键
     * @return
     */
    private Integer getCollectionCountFromDatabaseAndFlushCache(Integer collection_id, int collection_type) {
        Integer countVal = null;
        //使用switch语句判断是哪种类型的收藏，然后就知道去哪个表查询，然后再根据collection_id判断收藏数是多少
        switch(collectionTypeMapping[collection_type]){
            case "answer": {
                countVal = answerDao.getCollectionCount(collection_id);
                break;
            }
            case "article": {
                countVal = articleDao.getCollectionCount(collection_id);
                break;
            }
            case "question": {
                countVal = questionDao.getCollectionCount(collection_id);
                break;
            }
        }

        //如果对应存储的该hash类型的键值对已存在，那么就往该hash的值里面添加键值对即可
        if(redisUtil.getExpire("collection:" + collectionTypeMapping[collection_type]) > 0){
            //存储的类型为: <collection:collectionType: {<id:val>, <id:val>}>  collectionType可为该数组中的任何一个
            redisUtil.hset("collection:" + collectionTypeMapping[collection_type], collection_id + "", countVal);
        } else {      //如果对应的hash的键不存在！那么创建该hash键值对，一定要加上过期时间
            redisUtil.hset("collection:" + collectionTypeMapping[collection_type],
                    collection_id + "",
                    countVal,
                    CACHE_EXPIRE_DURATION);
        }

        return countVal;
    }


    //TODO  增加分布式锁解决缓存击穿
    @Override
    public Integer getEvaluateCount(Integer id, int type, int likeOrDislike) {
        if(type < 0 || type > 3 || likeOrDislike < 0 || likeOrDislike > 1) return null;

        Object countStr = redisUtil.hget("evaluate:" + evaluateTypeMapping[type], id + ":" + likeOrDislike);
        Integer res;

        if(countStr == null){        //如果缓存中没有，那么就去数据库中取，并且方法缓存中
            res = getEvaluateCountFromDatabaseAndFlushCache(id, type, likeOrDislike);
        } else {                    //如果缓存中存在 那么就直接放入结果中
            res = Integer.parseInt(countStr + "");
        }

        return res;
    }


    /**
     * 从数据库中查询指定id的指定类型的文章(article,comment...)的赞或者踩的数量
     * @param evaluate_id       评价对象的ID
     * @param evaluate_type     评价对象的类型  0(代表回答)  1(代表文章)  2(代表问题)  3(代表评论)
     * @param likeOrDislike     0(代表踩)  1(代表赞)
     * @return
     */
    private Integer getEvaluateCountFromDatabaseAndFlushCache(Integer evaluate_id, int evaluate_type, int likeOrDislike) {
        Integer countVal = null;
        switch(evaluateTypeMapping[evaluate_type]){
            case "answer": {
                countVal = likeOrDislike == 0 ? answerDao.getDislikeCount(evaluate_id) :
                                                answerDao.getLikeCount(evaluate_id);
                break;
            }
            case "article": {
                countVal = likeOrDislike == 0 ? articleDao.getDislikeCount(evaluate_id) :
                                                articleDao.getLikeCount(evaluate_id);
                break;
            }
            case "question": {
                countVal = likeOrDislike == 0 ? questionDao.getDislikeCount(evaluate_id) :
                                                questionDao.getLikeCount(evaluate_id);
                break;
            }
            case "comment": {
                countVal = likeOrDislike == 0 ? commentDao.getDislikeCount(evaluate_id) :
                                                commentDao.getLikeCount(evaluate_id);
                break;
            }
        }
        //如果要要存放在redis中的该对象的hash类型的key存在，那么就直接在该hash键值对的值里面添加值
        if(redisUtil.getExpire("evaluate" + evaluateTypeMapping[evaluate_type]) > 0){
            redisUtil.hset("evaluate" + evaluateTypeMapping[evaluate_type], evaluate_id + ":" + likeOrDislike, countVal);
        } else {          //如果对应的hash键值对不存在，那么就需要创建并且设置过期时间
            redisUtil.hset("evaluate" + evaluateTypeMapping[evaluate_type],
                    evaluate_id + ":" + likeOrDislike,
                    countVal,
                    CACHE_EXPIRE_DURATION);
        }

        return countVal;
    }




    //TODO 添加分布式锁解决缓存击穿  同时也存在: 在判断缓存有没有和给hash的其中某个值 + 1操作，判断缓存有之后，缓存就失效了！
    @Override
    public Integer likeOrDislike(int evaluate_id, int evaluate_type, int likeOrDislike) {
        if(evaluate_type < 0 || evaluate_type > 3 || likeOrDislike < 0 || likeOrDislike > 1) return null;

        //插入mysql
        int insertCount = likeOrDislike == 0 ? evaluateDao.dislike(evaluate_id, evaluate_type):
                                               evaluateDao.like(evaluate_id, evaluate_type);

        if(insertCount == 1){      //如果数据库插入成功
            //如果这里缓存没有过期，再自增1
            if(redisUtil.getExpire("evaluate" + evaluateTypeMapping[evaluate_type]) > 0){
                redisUtil.hincr("evaluate:" + evaluateTypeMapping[evaluate_type], evaluate_id + ":" + likeOrDislike, 1);
            } else {         //如果这里缓存过期了
                getEvaluateCountFromDatabaseAndFlushCache(evaluate_id, evaluate_type, likeOrDislike);
            }

            return 1;
        }
        //插入失败就返回null
        return null;
    }


    @Override
    public String toggleCollect(Integer collection_id, Integer collection_type, Integer collector) {
        if(collection_type < 0 || collection_type > 2 || collector < 0) return null;

        //如果用户已经收藏了
        if(collectionDao.isAlreadyCollect(collection_id, collector, collection_type) >= 1){
            //那么就更新数据库，取消收藏
            if(collectionDao.cancel(collection_id, collector, collection_type) == 1){
                //更新数据库完毕之后，就要更新缓存
                //如果缓存中有 那么就进行减1操作
                if(redisUtil.getExpire("collection" + evaluateTypeMapping[collection_type]) > 0){
                    redisUtil.hdecr("collection:" + collectionTypeMapping[collection_type],
                            collection_id + "", 1);
                } else {    //如果缓存中没有，那么就去数据库中查并且刷新缓存
                    getCollectionCountFromDatabaseAndFlushCache(collection_id, collection_type);
                }
                return "cancel";
            }
        } else {         //如果用户没有收藏 那么就收藏
            //若更新数据库成功
            if(collectionDao.collect(collection_id, collector, collection_type) == 1){
                //那么就修改缓存
                //如果缓存中有 那么就进行加1操作
                if(redisUtil.getExpire("collection" + evaluateTypeMapping[collection_type]) > 0){
                    redisUtil.hincr("collection:" + collectionTypeMapping[collection_type],
                            collection_id + "", 1);
                } else {    //如果缓存中没有，那么就去数据库中查并且刷新缓存
                    getCollectionCountFromDatabaseAndFlushCache(collection_id, collection_type);
                }

                return "collect";
            }
        }

        return null;

    }


}

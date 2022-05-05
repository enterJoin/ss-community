package com.atlxw.community.service;

public interface RedisService {

    /**
     * 获取收藏数
     * @param id   收藏对象的ID
     * @param type    收藏对象的类型  0(代表回答)  1(代表文章)  2(代表问题)
     * @return
     */
    Integer getCollectionCount(Integer id, int type);


    /**
     * 获取制定评价类型的赞或者踩的数量
     * @param id         评价对象的ID
     * @param type       评价对象的类型  0(代表回答)  1(代表文章)  2(代表问题)  3(代表评论)
     * @param likeOrDislike    0(代表踩)  1(代表赞)
     * @return
     */
    Integer getEvaluateCount(Integer id, int type, int likeOrDislike);
    /**
     * 赞或者踩  对指定的文本内容(文章、回答、问题、评论)的指定id，进行点赞或者踩
     * @param evaluate_id       指定的id
     * @param evaluate_type     指定的文本的类型  0(代表回答)  1(代表文章)  2(代表问题)  3(代表评论)
     * @param likeOrDislike     要进行的是点赞还是踩的操作，0(踩) 1(赞)
     * @return
     */
    Integer likeOrDislike(int evaluate_id, int evaluate_type, int likeOrDislike);
    /**
     * 收藏或取消收藏的操作
     * @param collection_id      收藏对象的ID
     * @param collection_type    收藏对象的类型   0(回答)  1(文章)  2(问题)
     * @param collector          收藏者的id
     * @return
     */
    String toggleCollect(Integer collection_id, Integer collection_type, Integer collector);
}

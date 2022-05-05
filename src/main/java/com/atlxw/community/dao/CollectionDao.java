package com.atlxw.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface CollectionDao {
    /**
     * 查询id为collector的用户是否对类型为collection_type并且文本id为collection_id的内容进行收藏
     * @param collection_id     收藏对象的ID(文本ID)
     * @param collector         收藏者的id(用户ID)
     * @param collection_type   收藏对象的类型   0(回答)  1(文章)  2(问题)
     * @return
     */
    Integer isAlreadyCollect(@Param("collection_id") Integer collection_id, @Param("collector") Integer collector, @Param("collection_type") Integer collection_type);

    /**
     * 删除id为collector的用户是否对类型为collection_type并且文本id为collection_id的内容进行收藏的记录
     * @param collection_id     收藏对象的ID(文本ID)
     * @param collector         收藏者的id(用户ID)
     * @param collection_type   收藏对象的类型   0(回答)  1(文章)  2(问题)
     * @return
     */
    int cancel(@Param("collection_id") Integer collection_id, @Param("collector") Integer collector, @Param("collection_type") Integer collection_type);

    /**
     * 添加id为collector的用户是否对类型为collection_type并且文本id为collection_id的内容的记录
     * @param collection_id     收藏对象的ID(文本ID)
     * @param collector         收藏者的id(用户ID)
     * @param collection_type   收藏对象的类型   0(回答)  1(文章)  2(问题)
     * @return
     */
    int collect(@Param("collection_id") Integer collection_id, @Param("collector") Integer collector, @Param("collection_type") Integer collection_type);

}

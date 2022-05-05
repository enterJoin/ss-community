package com.atlxw.community.dao;

import com.atlxw.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface FollowDao {
    /**
     * 判断id为to_follow的用户是否关注了id为follower的用户
     * @param to_follow  观察者的id
     * @param follower  用户的id
     * @return
     */
    Integer isAlreadyFollow(@Param("to_follow") Integer to_follow, @Param("follower") Integer follower);

    /**
     * 获取用户id为authorId的用户一共有多少人关注
     * @param authorId
     * @return
     */
    Integer getFollowerCount(Integer authorId);

    /**
     * 删除这条关注的记录
     * @param follower
     * @param to_follow
     * @return
     */
    int cancel(@Param("follower") Integer follower, @Param("to_follow") int to_follow);

    /**
     * 插入关注的记录
     * @param follower
     * @param to_follow
     * @return
     */
    int follow(@Param("follower") Integer follower, @Param("to_follow") int to_follow);

    /**
     * 获取用户id为follower关注的用户
     * 获取的属性为: id, nickname, head_photo_url
     * @param follower
     * @return
     */
    List<User> getFollowsOf(int follower);

    /**
     * 获取用户id为follower被关注的用户
     * 获取的属性为: id, nickname, head_photo_url
     * @param to_follow
     * @return
     */
    List<User> getFollowersOf(int to_follow);
}

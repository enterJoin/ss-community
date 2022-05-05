package com.atlxw.community.dao;

import com.atlxw.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface UserDao {
    /**
     * 检测邮箱是否存在 返回存在的个数
     * @param emailAddress
     * @return
     */
    int emailExistsCheck(@Param("emailAddress") String emailAddress);

    /**
     * 检查用户名是否存在 返回存在的个数
     * @param username
     * @return
     */
    int usernameExistsCheck(@Param("username") String username);

    /**
     * 检查昵称是否存在 返回存在的个数
     * @param nickname
     * @return
     */
    int nicknameExistsCheck(@Param("nickname") String nickname);

    /**
     * 注册用户 返回受影响的行数
     * @param user
     * @return
     */
    int register(User user);

    /**
     * 获得user表中值最大的键！
     * @return
     */
    Integer getAutoIncrement();

    /**
     * 使用邮箱 + 密码来进行登录
     * @param username
     * @param password
     * @return
     */
    List<User> loginWithEmail(@Param("username") String username, @Param("password") String password);

    /**
     * 使用用户名 + 密码来进行操作
     * @param username
     * @param password
     * @return
     */
    List<User> loginWithUsername(@Param("username") String username, @Param("password") String password);

    /**
     * 通过id获取用户
     * @param id
     * @return
     */
    List<User> getUser(@Param("id") int id);

    /**
     * 修改登录信息(登录IP以及登陆时间)
     * @param id
     * @param lastLoginTime
     * @param lastLoginIP
     * @return
     */
    int updateLoginInfoById(@Param("id") Integer id, @Param("lastLoginTime") Timestamp lastLoginTime, @Param("lastLoginIP") String lastLoginIP);

    /**
     * 修改用户的头像信息
     * @param uid
     * @param headPhotoPath
     * @param headPhotoUrl
     * @param headPhotoName
     * @return
     */
    int updateHeadPhotoInfo(@Param("uid") Integer uid, @Param("headPhotoPath") String headPhotoPath, @Param("headPhotoUrl") String headPhotoUrl, @Param("headPhotoName") String headPhotoName);

    /**
     * 修改用户的昵称
     * @param id
     * @param newNickname
     * @return
     */
    int updateNickname(@Param("id") Integer id, @Param("newNickname") String newNickname);

    /**
     * 修改用户的座右铭
     * @param id
     * @param newMotto
     * @return
     */
    int updateMotto(@Param("id") Integer id, @Param("newMotto") String newMotto);

    /**
     * 根据邮箱来修改用户的密码
     * @param email
     * @param newPassword
     * @return
     */
    int updatePassword(@Param("email") String email, @Param("newPassword") String newPassword);


    /**
     * 根据nickname或者username来进行搜索
     * @param q
     * @return
     */
    List<Map<String, Object>> searchUserByFollowersCount(String q);

    /**
     * 根据searchStr搜索用户  并且不能有自身！
     * @param searchStr  搜索的名称（usernamae或者nickname中匹配）
     * @param uid        自身登录的用户
     * @return
     */
    List<User> findUsersLikely(@Param("searchStr") String searchStr, @Param("uid") Integer uid);
}

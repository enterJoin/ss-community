package com.atlxw.community.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface UserService {
    String doRegister(HttpServletRequest request, HttpServletResponse response);

    String handleRegisterMailRequest(HttpServletRequest request, HttpServletResponse response);

    String checkVerifyCode(HttpServletRequest request, HttpServletResponse response);

    String doLogin(String username, String password, HttpServletRequest request, HttpServletResponse response);

    String getUser(String uid, HttpServletRequest request);

    String handleHeadPhotoUpdate(Integer id, MultipartFile newHeadPhotoImage, HttpServletRequest request);

    String updateNickname(String uid, String newNickname, HttpServletRequest request);

    String updateMotto(String uid, String newMotto, HttpServletRequest request);

    String handleFindPassword(String email, HttpServletRequest request, HttpServletResponse response);

    String checkFindPasswordVerifyCode(String code, HttpServletRequest request);

    String handlePasswordChangeRequest(String newPassword, HttpServletRequest request);

    Map<String,Object> getUserInfoToShowAside(Integer authorId) throws Exception;

    Map<String, Object> getUserInfoOfComment(Integer commentator) throws Exception;

    /**
     * 获取需要访问的用户的基本信息
     * @param visitUid   要访问的用户的id
     * @param request    请求对象
     * @return
     */
    String getVisitUserBasicInfo(int visitUid, HttpServletRequest request);

    /**
     * 当前用户关注id为to_follow的用户 或者是取消关注
     * @param to_follow   即将要被关注的用户
     * @param request
     * @return
     */
    String toggleFollow(int to_follow, HttpServletRequest request);

    /**
     * 得到当前登录的用户关注其它的用户的列表，自己是跟随者
     * @param follower
     * @return
     */
    String getFollowsOf(int follower);

    /**
     * 自己是被关注着  获得别人关注自己的人
     * @param to_follow  被关注者(自己)
     * @return
     */
    String getFollowersOf(int to_follow);
}

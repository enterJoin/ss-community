package com.atlxw.community.controller;

import com.atlxw.community.service.UserService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.TokenUtils;
import com.auth0.jwt.interfaces.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usr")
public class UserController {
    @Autowired
    private UserService userService;
    /**
     * 1、post usr/getUser
     * 2、post usr/getVisitUserBasicInfo
     * 3、post usr/toggleFollow
     * 4、post usr/getFollowersOf
     * 5、post usr/getFollowsOf
     */

    /**
     * 注册用户用到的方法
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/register")
    public String register(HttpServletRequest request, HttpServletResponse response){
        return userService.doRegister(request, response);
    }

    /**
     * 发送邮箱用到的方法
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/sendRegisterEmail")
    public String sendRegisterEmail(HttpServletRequest request, HttpServletResponse response){
        return userService.handleRegisterMailRequest(request, response);
    }

    /**
     * 邮箱验证用到的方法
     */
    @PostMapping("/checkVerifyCode")
    public String checkVerifyCode(HttpServletRequest request, HttpServletResponse response){
        return userService.checkVerifyCode(request, response);
    }

    /**
     * 用户登录的方法 包括审核账号密码等步骤
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        HttpServletResponse response){
        return userService.doLogin(username, password, request, response);
    }

    /**
     * 验证用户的登录情况！
     * @param request
     * @return
     */
    @PostMapping("/tryJWTverify")
    public String tryVerifyLoginState(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        Map<String, Claim> tokenMap;
        Map<String, String> res = new HashMap<>();

        res.put("islogin", "false");
        if(cookies != null && cookies.length != 0){
            for(Cookie cookie: cookies){
                if("token".equals(cookie.getName())){    //遍历找到名为token的Cookie
                    //token验证成功 说明用户登录了！
                    if((tokenMap = TokenUtils.getTokenInfo(cookie.getValue())) != null){
                        //覆盖之前的false
                        res.put("islogin", "true");
                        res.put("uid", tokenMap.get("uid").asInt() + "");
                        break;  //直接跳出循环即可
                    }
                }
            }

        }

        return JacksonUtils.mapToJson(res);
    }

    /**
     * 获取用户 看用户是否存在
     * @param uid
     * @param request
     * @return
     */
    @PostMapping("/getUser")
    public String getUser(@RequestParam("uid") String uid, HttpServletRequest request){
        return userService.getUser(uid, request);
    }

    /**
     * 修改用户头像
     * @param newHeadPhotoImage
     * @param request
     * @return
     */
    @PostMapping("/updateHeadPhoto")
    public String updateHeadPhoto(@RequestParam("newHeadPhotoImage") MultipartFile newHeadPhotoImage,
                                  HttpServletRequest request){
        //因为是流的形式传递文件，无法直接获取表单数据，因此先将用户的ID获取
        Integer id = TokenUtils.getUid(request);

        return userService.handleHeadPhotoUpdate(id, newHeadPhotoImage, request);
    }

    /**
     * 修改用户的昵称
     * @param request
     * @param uid
     * @param newNickname
     * @return
     */
    @PostMapping("/updateNickname")
    public String updateNickname(HttpServletRequest request,
                                 @RequestParam("uid") String uid,
                                 @RequestParam("newNickname") String newNickname){
        return userService.updateNickname(uid, newNickname, request);
    }

    /**
     * 修改用户的座右铭
     * @return
     */
    @PostMapping("/updateMotto")
    public String updateMotto(@RequestParam("uid") String uid,
                              @RequestParam("newMotto") String newMotto,
                              HttpServletRequest request){
        return userService.updateMotto(uid, newMotto, request);
    }

    /**
     * 用于发送找回密码的邮箱
     * @param email
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/findPassword")
    public String findPassword(@RequestParam("email") String email,
                               HttpServletRequest request,
                               HttpServletResponse response){
        return userService.handleFindPassword(email, request, response);
    }

    /**
     * 找回密码的时候的邮箱验证
     * @param code
     * @param request
     * @return
     */
    @PostMapping("/findPasswordVerifyCode")
    public String findPasswordVerifyCode(@RequestParam("code") String code,
                                         HttpServletRequest request){
        return userService.checkFindPasswordVerifyCode(code, request);
    }

    /**
     * 修改新的密码 用于找回密码
     * @return
     */
    @PostMapping("/doChangePassword")
    public String doChangePassword(@RequestParam("newPassword") String newPassword,
                                   HttpServletRequest request){
        return userService.handlePasswordChangeRequest(newPassword, request);
    }

    /**
     * 获取需要访问的用户的基本信息
     * @param visitUid   要访问的用户的id
     * @param request    请求对象
     * @return
     */
    @PostMapping("/getVisitUserBasicInfo")
    public String getVisitUserBasicInfo(@RequestParam("visitUid") String visitUid,
                                        HttpServletRequest request){
        try {
            return userService.getVisitUserBasicInfo(Integer.parseInt(visitUid), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 当前用户关注id为to_follow的用户 或者是取消关注
     * @param to_follow   即将要被关注的用户
     * @param request
     * @return
     */
    @PostMapping("/toggleFollow")
    public String toggleFollow(@RequestParam("to_follow") String to_follow,
                               HttpServletRequest request){
        try {
            return userService.toggleFollow(Integer.parseInt(to_follow), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到当前登录的用户关注其它的用户的列表，自己是跟随者
     * @param follower   关注别人的人(自己)
     * @return
     */
    @PostMapping("/getFollowsOf")
    public String getFollowsOf(@RequestParam("follower") String follower){
        try {
            return userService.getFollowsOf(Integer.parseInt(follower));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 自己是被关注着  获得别人关注自己的人
     * @param to_follow  被关注者(自己)
     * @return
     */
    @PostMapping("/getFollowersOf")
    public String getFollowersOf(@RequestParam("to_follow") String to_follow){
        try {
            return userService.getFollowersOf(Integer.parseInt(to_follow));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}

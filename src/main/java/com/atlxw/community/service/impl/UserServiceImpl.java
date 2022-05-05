package com.atlxw.community.service.impl;

import com.atlxw.community.config.ImageResourceMapper;
import com.atlxw.community.constant.UserConst;
import com.atlxw.community.dao.AnswerDao;
import com.atlxw.community.dao.ArticleDao;
import com.atlxw.community.dao.FollowDao;
import com.atlxw.community.dao.UserDao;
import com.atlxw.community.entity.User;
import com.atlxw.community.io.CustomIO;
import com.atlxw.community.service.UserService;
import com.atlxw.community.utils.*;
import com.atlxw.community.verify.UserVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserVerifier userVerifier;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CustomIO customIO;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private FollowDao followDao;

    /**
     * 注册用户
     * @param request
     * @param response
     * @return
     */
    @Override
    @Transactional
    public String doRegister(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultMap = new HashMap<>();
        //获得邮箱地址
        String emailAddress = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if("email".equals(cookie.getName())){
                emailAddress = (String) redisUtil.get(cookie.getValue());
            }
        }

        if(emailAddress == null){      //如果邮箱地址为空
            resultMap.put("success", "false");
            resultMap.put("reason", "未进行邮箱验证或其他不正规原因！");
        } else if(userDao.emailExistsCheck(emailAddress) > 0){    //如果数据库中已经存在该邮箱
            resultMap.put("success", "false");
            resultMap.put("reason", "邮箱地址已被注册！");
        } else if(request.getParameter("username") == null || userDao.usernameExistsCheck(request.getParameter("username")) > 0){
            //如果用户名为空或者用户名在用户表中已经存在
            resultMap.put("success", "false");
            resultMap.put("reason", "用户名已被注册！");
        } else if(request.getParameter("nickname") == null || userDao.nicknameExistsCheck(request.getParameter("nickname")) > 0){
            //如果昵称为空 或者已经被注册
            resultMap.put("success", "false");
            resultMap.put("reason", "昵称已被注册！");
        } else {
            User user = new User(null, request.getParameter("username"),
                    request.getParameter("password"),
                    emailAddress,
                    request.getParameter("nickname"),
                    "这个人有点懒，没有留下个性签名哦~",
                    null,
                    null,
                    null,
                    new Timestamp(System.currentTimeMillis()),
                    null,
                    new Timestamp(System.currentTimeMillis()),
                    null);
            //后端也要验证输入格式的合法性，如果验证通过的话
            if(userVerifier.userRegisterVerify(user)){
                //对用户密码进行sha256加密
                user.setEncrypted_password(Sha256.getSHA256(user.getEncrypted_password()));
                //存入数据库
                if(userDao.register(user) == 1){
                    resultMap.put("success", "true");
                }

                //实现注册完成自动登录，签发token并加入返回体，并且将其存放cookie的token放在response中
                response.addCookie(signToken(userDao.getAutoIncrement()));
            } else {   //如果用户验证失败
                resultMap.put("success", "false");
                resultMap.put("reason", "未经授权访问接口！");
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 发送邮件准备进行短信验证
     * @param request
     * @param response
     * @return
     */
    @Override
    @Transactional
    public String handleRegisterMailRequest(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultMap = new HashMap<>();

        String targetEmail = request.getParameter("emailinput");

        if(userDao.emailExistsCheck(targetEmail) > 0){   //如果该邮件在用户表里面已经有了！
            resultMap.put("success", "false");
            resultMap.put("reason", "邮件地址已被注册！");
        } else if (! userVerifier.emailFormatVerify(targetEmail)){    //如果邮件不合法！
            resultMap.put("success", "false");
            resultMap.put("reason", "邮件地址不合法！");
        } else {
            //随机生成一个6位的整数
            String code = (int) (Math.random() * 900000 + 100000) + "";

            //request.getSession().setAttribute("registerEmailVerifyNumber", code);  多线程存在问题！
            //将该字符串保存到redis中，并且通过cookie来访问，避免多线程发生的问题

            //将验证码code存放在redis中，key就为prefix + email的形式，时长设置为15*60  15分钟 TODO
            redisUtil.set(UserConst.USER_CODE_PREFIX + targetEmail, code, 15 * 60);

            //新建StringBuilder来存放消息，这样会更快！
            StringBuilder content = new StringBuilder();
            content.append("您的验证码是: <h2>").
                    append(code).
                    append("</h2><br/> 请在十分钟之内完成验证操作！<br/>").
                    append("请勿把验证码告诉他人<br/><br/>").
                    append("爽爽社区祝您生活愉快！！！");

            try {
                //即可开始发送邮件！
                EmailSender.sendEmail("感谢您注册爽爽社区！", content.toString(), targetEmail);

                //暂时将用户的邮箱保存在redis中，时长为一天，要是用户一天都还没注册完成，那么就将用户的邮箱信息给清除掉
                putIntoRedisAndSetCookie(request, response, targetEmail, "email", UserConst.USER_EMAIL_PREFIX, 24 * 60 * 60);

                //返回成功
                resultMap.put("success", "true");
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("success","false");
                resultMap.put("reason","抱歉，暂时无法发送邮件！请联系管理员。");
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 验证邮箱验证码是否正确
     * @param request
     * @param response
     * @return
     */
    @Override
    public String checkVerifyCode(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultMap = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        //获取发送的验证码
        String oldCode = null;
        for(Cookie cookie : cookies){          //遍历所有的Cookie 知道找到key为code的cookie即可
            if("email".equals(cookie.getName())){
                String email = (String) redisUtil.get(cookie.getValue());   //获得发送的验证码
                oldCode = (String) redisUtil.get(UserConst.USER_CODE_PREFIX + email);
            }
        }

        //得到页面上的用户输入的验证码
        String newCode = request.getParameter("verifycodeinput");
        //看这两个验证码是否相同
        if(oldCode != null && newCode.equals(oldCode)){     //如果oldCode不为空并且新旧code相等的话
            resultMap.put("success", "true");
        } else {
            resultMap.put("success","false");
            resultMap.put("reason","验证码或邮箱不匹配，可重试，或出现bug，可联系管理员");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 用户登录的方法 包括账号密码的审核
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @Override
    public String doLogin(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultMap = new HashMap<>();
        //登录失败就直接返回resultMap
        resultMap.put("success", "false");
        //如果该用户没有被锁定
        if(request.getSession().getAttribute("lockTime") == null){
            List<User> u;
            boolean isLoginSuccess = false;
            //通过用户名中包不包含@来判断是用户名还是邮箱
            if(username.contains("@")){    //如果包含 那么就是邮箱
                //如果当前邮箱在user表中有且只存在一条记录，并且密码也符合，那么就是登陆成功
                if((u = userDao.loginWithEmail(username, Sha256.getSHA256(password))).size() == 1){
                    isLoginSuccess = true;
                }
            } else {    //如果不包含@，那么就是用户名
                //如果当前用户名在user表中有且只存在一条记录，并且密码也符合，那么就是登陆成功
                if((u = userDao.loginWithUsername(username, Sha256.getSHA256(password))).size() == 1){
                    isLoginSuccess = true;  //那么登录成功
                }
            }

            //接下来判断成功和失败的结果
            if(isLoginSuccess){    //如果符合条件，能够登陆成功
                resultMap.put("success", "true");
                redisUtil.del("failCount");   //将redis中存放的failCount删除，因为已经登陆成功了
                response.addCookie(signToken(u.get(0).getId()));   //要传入用户的ID
            } else {     //如果用户名密码不符合要求，那么将错误计数 + 1
                //设定错误计数，超过5次那么就会禁止登录30s
                long failCount = redisUtil.incr("failCount", 1);

                //如果说失败的次数达到了5次的话，那么就将锁定登录10s！
                if(failCount >= 5){
                    //错误超过5次，记录时间并锁定
                    redisUtil.set("lockTime", "v", 10);
                    //返回时间 让前端也等待30s
                    resultMap.put("timeToWait","10");
                    //清空错误计数
                    redisUtil.del("failCount");
                }
            }
        }
        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 获取用户 看用户是否存在
     * @param uid
     * @param request
     * @return
     */
    @Override
    public String getUser(String uid, HttpServletRequest request) {
        try{
            List<User> userList = userDao.getUser(Integer.parseInt(uid));
            if(userList.size() > 1) throw new Exception("存在重名用户！");
            else if(updateLoginInfo(request, uid) == 0 || userList.size() == 0) throw new Exception("用户不存在！");
            else return JacksonUtils.obj2json(userList.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 修改用户的头像
     * @param uid
     * @param newHeadPhotoImage
     * @param request
     * @return
     */
    @Transactional
    @Override
    public String handleHeadPhotoUpdate(Integer uid, MultipartFile newHeadPhotoImage, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success", "false");   //首先假装是失败的结果
        //如果uid不为空并且和当前用户的id是相同的
        if(uid != null && uid.equals(TokenUtils.getUid(request))){
            List<User> users = userDao.getUser(uid);
            if(users.size() == 1){    //如果这个id的用户有且只有一个用户
                //如果该用户之前有头像,之前的路径不为空并且也不为""
                if(users.get(0).getHead_photo_path() != null && ! "".equals(users.get(0).getHead_photo_path())){
                    //那么就将这个旧的头像从服务器中删除
                    File toRemove = new File(users.get(0).getHead_photo_path());
                    //如果删除失败了，那么就返回
                    if(! toRemove.delete()) return JacksonUtils.mapToJson(resultMap);
                }

                //生成文件名并保存
                Map<String, String> saveRes = customIO.daSave(ImageResourceMapper.getHeadPhotoFileLocationByEnvironment(),
                        newHeadPhotoImage,
                        UserConst.supportSuffix);

                if(saveRes != null){
                    //图片的路径(文件路径)
                    String headPhotoPath = saveRes.get("filePath");
                    //图片的URL(http://xxx:xx/xxx/xxx)
                    String headPhotoUrl = RequestUtils.getProjectRootUrl(request) + ImageResourceMapper.headPhotoUrlSuffix.substring(1) + saveRes.get("name");
                    //图片的名称
                    String headPhotoName = saveRes.get("name");

                    if(userDao.updateHeadPhotoInfo(uid, headPhotoPath, headPhotoUrl, headPhotoName) == 1){
                        resultMap.put("success", "true");    //如果返回的是一条记录受影响，那么就成功
                    }
                }
            }
        }
        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 修改用户的昵称
     * @param uid
     * @param newNickname
     * @param request
     * @return
     */
    @Override
    public String updateNickname(String uid, String newNickname, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success", "false");   //首先假装是失败的结果
        if(uid == null) return JacksonUtils.mapToJson(resultMap);
        Integer id = Integer.parseInt(uid);

        //如果说当前传过来的id和用户的id是相等的话，并且更新nickName之后返回的记录数也为1
        if(id.equals(TokenUtils.getUid(request)) && userDao.updateNickname(id, newNickname) == 1){
            resultMap.put("success", "true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 修改用户的座右铭
     * @param uid
     * @param newMotto
     * @param request
     * @return
     */
    @Transactional
    @Override
    public String updateMotto(String uid, String newMotto, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success", "false");   //首先假装是失败的结果
        if(uid == null) return JacksonUtils.mapToJson(resultMap);
        Integer id = Integer.parseInt(uid);

        //如果说当前传过来的id和用户的id是相等的话，并且更新nickName之后返回的记录数也为1
        if(id.equals(TokenUtils.getUid(request)) && userDao.updateMotto(id, newMotto) == 1){
            resultMap.put("success", "true");
        }

        return JacksonUtils.mapToJson(resultMap);

    }

    /**
     * 用于找回密码的功能实现
     * @param email
     * @param request
     * @return
     */
    @Override
    public String handleFindPassword(String email, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success", "false");   //首先假装是失败的结果

        //如果密码格式合格 并且密码在数据库中存在且只有一条记录存在
        if(userVerifier.emailFormatVerify(email) && userDao.emailExistsCheck(email) == 1){
            //随机生成六位数字
            int autoGeneratedNumber = (int)(Math.random() * 900000 + 100000);

            //将此时的用的邮箱也保存在redis中，同时也在Cookie中存放键
            putIntoRedisAndSetCookie(request, response, email, "find-password-email", UserConst.USER_FIND_PASSWORD_PREFIX, 15 * 60);

            //将这六位数字保存到redis中  有效期是15分钟
            redisUtil.set(UserConst.USER_FIND_PASSWORD_PREFIX + email, autoGeneratedNumber, 15 * 60);

            try {
                StringBuilder content = new StringBuilder();
                content.append("用户您好，您正在试图找回账户")
                       .append(email)
                       .append("的密码，<br />您的验证码是：<br /><h2>")
                       .append(autoGeneratedNumber)
                       .append("</h2><br />请您在15分钟内在网页输入验证码以进行后续操作。请不要把验证码告诉他人。")
                       .append("<br /><br />爽爽祝您生活愉快");


                EmailSender.sendEmail("爽爽社区 找回密码", content.toString(), email);

                resultMap.put("success","true");
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("reason","抱歉，邮件暂时无法发送");
            }
        } else {
            resultMap.put("reason","邮件地址不合法或未注册");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 检查找回密码的验证码是否正确
     * @param code
     * @param request
     * @return
     */
    @Override
    public String checkFindPasswordVerifyCode(String code, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success", "false");   //首先假装是失败的结果

        //首先得到邮箱
        String email = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if("find-password-email".equals(cookie.getName())){    //如果说找到那个cookie了
                email = (String) redisUtil.get(cookie.getValue());   //获得之前发送验证码的邮箱
                break;
            }
        }
        if(email == null) return JacksonUtils.mapToJson(resultMap);

        String oldCode = String.valueOf(redisUtil.get(UserConst.USER_FIND_PASSWORD_PREFIX + email));
        System.out.println(oldCode);
        //比较用户输入的验证码和之前发送的验证码
        if(oldCode != null && code.equals(oldCode)){
            resultMap.put("success", "true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 用于修改密码
     * @param newPassword
     * @param request
     * @return
     */
    @Override
    @Transactional
    public String handlePasswordChangeRequest(String newPassword, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success", "false");   //首先假装是失败的结果

        String email = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies){
            if("find-password-email".equals(cookie.getName())){    //如果用key为email的cookie
                email = (String) redisUtil.get(cookie.getValue());    //得到email
            }
        }

        //如果email为空或者格式错误 再检查一次！
        if(email == null || ! userVerifier.emailFormatVerify(email)) return JacksonUtils.mapToJson(resultMap);

        if(userDao.emailExistsCheck(email) == 1 && userDao.updatePassword(email, Sha256.getSHA256(newPassword)) == 1){    //如果右键存在并且修改成功
            resultMap.put("success", "true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    /**
     * 获取要展示在文章或者回答展示页右边的作者信息
     * 包括：用户ID、昵称、个性签名、头像URL、回答数、文章数、粉丝数
     * @param authorId
     * @return
     */
    @Override
    public Map<String, Object> getUserInfoToShowAside(Integer authorId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<User> users = userDao.getUser(authorId);
        if(users != null && users.size() == 1){    //如果id为authorId的用户有且只有一个
            User user = users.get(0);   //获取当前的用户
            Integer answerCount = answerDao.getAnswerCountByUserId(authorId);   //获取该用户的回答次数
            Integer articleCount = articleDao.getArticleCountWriteBy(authorId);   //获取该作者写了多少文章
            Integer followerCount = followDao.getFollowerCount(authorId);       //获取有多少人关注了该用户

            resultMap.put("authorId", authorId);
            resultMap.put("headPhotoUrl", user.getHead_photo_url());
            resultMap.put("nickname", user.getNickname());
            resultMap.put("motto", user.getMotto());
            resultMap.put("answerCount", answerCount);
            resultMap.put("articleCount", articleCount);
            resultMap.put("followerCount", followerCount);
        } else {
            throw new Exception("用户不存在或存在重名用户！");
        }

        return resultMap;
    }

    /**
     * 获得评论者的信息，包括评论者的ID、头像、昵称
     * @return Map对象，包括评论者的ID、头像、昵称
     */
    @Override
    public Map<String, Object> getUserInfoOfComment(Integer userId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<User> users = userDao.getUser(userId);
        if(users != null && users.size() == 1){    //如果该用户id有且只有一个
            User user = users.get(0);
            resultMap.put("authorId", userId);
            resultMap.put("headPhotoUrl", user.getHead_photo_url());
            resultMap.put("nickname", user.getNickname());
        } else {
            throw new Exception("存在重名用户或用户不存在！");
        }

        return resultMap;
    }

    @Override
    public String getVisitUserBasicInfo(int visitUid, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();

        List<User> users = userDao.getUser(visitUid);   //通过用户id来获取用户的对象
        if(users != null && users.size() == 1){       //如果该id有且仅有对应一个user
            User user = users.get(0);
            resultMap.put("visitUserId", visitUid);
            resultMap.put("username", user.getUsername());
            resultMap.put("motto", user.getMotto());
            resultMap.put("headPhotoUrl", user.getHead_photo_url());
            resultMap.put("nickname", user.getNickname());
            resultMap.put("registerTime", user.getRegister_time());
            //用户未登录情况下，默认未关注
            resultMap.put("isAlreadyFollow","false");
            Integer loginUid = TokenUtils.getUid(request);
            if(loginUid != null){
                if(followDao.isAlreadyFollow(visitUid, loginUid) >= 1){
                    resultMap.put("isAlreadyFollow","true");
                }
            }
        } else {
            throw new RuntimeException("存在重名用户或用户不存在！");
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String toggleFollow(int to_follow, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");
        Integer follower = TokenUtils.getUid(request);
        if(follower != null && !follower.equals(to_follow)) {//不允许自己关注自己
            if(followDao.isAlreadyFollow(to_follow,follower) >= 1) { //如果说用户已经关注了，那么就取消关注
                if(followDao.cancel(follower, to_follow) == 1) {   //如果取消关注成功
                    resultMap.put("success","true");
                    resultMap.put("type","cancel");
                }
            } else {                                                 //如果说用户没有关注 那么就进行关注
                if(followDao.follow(follower, to_follow) == 1) {     //如果关注成功
                    resultMap.put("success","true");
                    resultMap.put("type","follow");
                }
            }
        }
        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getFollowsOf(int follower) {
        //首先查找所有的id为follower关注的用户，只需要获得id, headPhotoUrl, nickname即可
        List<User> to_follows = followDao.getFollowsOf(follower);
        List<Map<String, Object>> resultList = new ArrayList<>();

        if(to_follows != null && to_follows.size() > 0){
            for(User user: to_follows){
                Map<String, Object> eachFollowed = new HashMap<>();
                eachFollowed.put("followerId", user.getId());
                eachFollowed.put("headPhotoUrl", user.getHead_photo_url());
                eachFollowed.put("nickname", user.getNickname());
                resultList.add(eachFollowed);
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
    public String getFollowersOf(int to_follow) {
        //首先查找所有的id为to_follow被关注的用户，只需要获得id, headPhotoUrl, nickname即可
        List<User> to_follows = followDao.getFollowersOf(to_follow);
        List<Map<String, Object>> resultList = new ArrayList<>();

        if(to_follows != null && to_follows.size() > 0){
            for(User user: to_follows){
                Map<String, Object> eachFollowed = new HashMap<>();
                eachFollowed.put("followerId", user.getId());
                eachFollowed.put("headPhotoUrl", user.getHead_photo_url());
                eachFollowed.put("nickname", user.getNickname());
                resultList.add(eachFollowed);
            }
        }
        try {
            return JacksonUtils.obj2json(resultList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 在客户端登录郭之后通过id来更新用户的登录信息，包括最后登录的IP和时间
     * @param request
     * @param uid
     * @return
     */
    private int updateLoginInfo(HttpServletRequest request, String uid) {
        Integer id = Integer.parseInt(uid);
        //获取现在的时间作为最后登录的时间
        Timestamp lastLoginTime = new Timestamp(System.currentTimeMillis());
        //获取现在的用户IP作为最后的登录IP
        String lastLoginIP = RequestUtils.getIPAddress(request);

        return userDao.updateLoginInfoById(id, lastLoginTime, lastLoginIP);
    }

    /**
     * 签发用户token，返回对应的Cookie
     */
    private Cookie signToken(Integer userId){
        //1、签发token，就是创建该用户的JWT
        String token = TokenUtils.sign(userId);
        //2、token信息是要存放到Cookie中
        Cookie cookie = new Cookie("token", token);
        //3、设置该Cookie的有效期为一周！单位为s
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setPath("/");

        return cookie;
    }

    /**
     * 将指定的值存放到redis中，并且也设置对应的Cookie
     * @param response
     * @param redisValue
     * @param cookieKey
     * @param time
     */
    private void putIntoRedisAndSetCookie(HttpServletRequest request, HttpServletResponse response, String redisValue, String cookieKey, String prefix, int time){
        Cookie[] cookies = request.getCookies();
        Cookie targetCookie = null;
        if (cookies != null) {       //注意这里要判断cookies是否为Null
            for(Cookie cookie : cookies){
                if(cookieKey.equals(cookie.getName())){     //如果当前这个Cookie存在
                    targetCookie = cookie;
                }
            }
        }

        String cookieValue = null;
        if(targetCookie != null){          //如果要存放的Cookie存在，那么就获得当前的CookieValue，免得再生成一个UUID
            cookieValue = targetCookie.getValue();
        } else {        //如果当前Cookie不存在
            //利用一个UUID生成一个永不重复的key
            String keyCode = UUID.randomUUID().toString().trim();
            cookieValue = prefix + keyCode;
        }

        //以免Cookie和Redis中的该键值对突然过期，还需要重新设置一遍！时间也从头再来！
        //在redis中存放 <前缀 + key, code>，并且设置过期时间，单位是秒
        redisUtil.set(cookieValue, redisValue, time);
        //另外再存放在cookie中，这样用户访问就可以直接获取了
        Cookie cookie = new Cookie(cookieKey, cookieValue);
        cookie.setPath("/");
        cookie.setMaxAge(time);    //并且设置该Cookie的过期时间，也为s
        response.addCookie(cookie);   //加到响应中
    }
}

package com.atlxw.community.service.impl;

import com.atlxw.community.dao.FollowDao;
import com.atlxw.community.dao.InviteDao;
import com.atlxw.community.dao.QuestionDao;
import com.atlxw.community.dao.UserDao;
import com.atlxw.community.entity.Invite;
import com.atlxw.community.entity.User;
import com.atlxw.community.service.InviteService;
import com.atlxw.community.service.UserService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.TokenUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

@Service
public class InviteServiceImpl implements InviteService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private InviteDao inviteDao;

    @Autowired
    private FollowDao followDao;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionDao questionDao;

    @Override
    public String getSearchUserAndInvitationState(int questionId, String searchStr, HttpServletRequest request) {
        Integer uid = TokenUtils.getUid(request);
        if(uid == null) return null;    //如果当前用户未登录  那么就直接返回null

        List<Map<String, Object>> resultList = new ArrayList<>();  //存放最终结果

        //首先查出搜索的所有用户
        List<User> searchUsers = userDao.findUsersLikely(searchStr, uid);
        //根据问题的id，和当前登录用户的id，还有查找的被邀请者的id  来查询所有的邀请关系
        //只需要用到inviter和be_invited字段即可
        List<Invite> invitationState = inviteDao.getSearchUsersInvitationStateOf(questionId, uid, searchStr);
        //遍历每一个用户
        searchUsers.forEach(searchUser -> {
            //存放每一个用户的 userId, headPhotoUrl, nickname, alreadyInvited 的值
            Map<String, Object> eachSearchUserMap = new HashMap<>();
            eachSearchUserMap.put("userId", searchUser.getId());
            eachSearchUserMap.put("headPhotoUrl", searchUser.getHead_photo_url());
            eachSearchUserMap.put("nickname", searchUser.getNickname());
            eachSearchUserMap.put("alreadyInvited", "false");    //首先赋予邀请状态为false

            invitationState.forEach(state -> {
               //已经被邀请的条件是: 被邀请者是当前搜索用户，并且邀请者是登录用户
                if(state.getBe_invited().equals(searchUser.getId()) && state.getInviter().equals(uid)){
                    eachSearchUserMap.put("alreadyInvited", "true");
                }
            });

            resultList.add(eachSearchUserMap);   //添加到最终结果中
        });

        try {
            return JacksonUtils.obj2json(resultList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getFollowsAndInivitationState(int questionId, HttpServletRequest request) {
        Integer uid = TokenUtils.getUid(request);
        if(uid == null) return null;    //如果当前用户未登录  那么就直接返回null

        List<Map<String, Object>> resultList = new ArrayList<>();  //存放最终结果

        //首先查出当前用户已经关注了的用户
        List<User> searchUsers = followDao.getFollowsOf(uid);
        //根据问题的id，和当前登录用户的id，还有查找的被邀请者的id  来查询所有的邀请关系
        //只需要用到inviter和be_invited字段即可
        List<Invite> invitationState = inviteDao.getFollowsInvitationStateOf(questionId, uid);
        //遍历每一个用户
        searchUsers.forEach(searchUser -> {
            //存放每一个用户的 userId, headPhotoUrl, nickname, alreadyInvited 的值
            Map<String, Object> eachSearchUserMap = new HashMap<>();
            eachSearchUserMap.put("followId", searchUser.getId());
            eachSearchUserMap.put("headPhotoUrl", searchUser.getHead_photo_url());
            eachSearchUserMap.put("nickname", searchUser.getNickname());
            eachSearchUserMap.put("alreadyInvited", "false");    //首先赋予邀请状态为false

            invitationState.forEach(state -> {
                //已经被邀请的条件是: 被邀请者是当前关注用户，并且邀请者是登录用户
                if(state.getBe_invited().equals(searchUser.getId()) && state.getInviter().equals(uid)){
                    eachSearchUserMap.put("alreadyInvited", "true");
                }
            });

            resultList.add(eachSearchUserMap);   //添加到最终结果中
        });

        try {
            return JacksonUtils.obj2json(resultList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String invite(int questionId, int be_invited, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");
        Integer uid = TokenUtils.getUid(request);

        if(uid != null && ! uid.equals(be_invited)){   //只有当前状态为已登录并且被邀请的用户不是自己，才能继续执行
            Invite invite = new Invite(null, uid, be_invited, questionId, 0, new Timestamp(System.currentTimeMillis()));
            if(inviteDao.isAlreadyInvited(invite) == 0){   //如果要被邀请的用户没有被邀请，才能邀请他
                if(inviteDao.invite(invite) == 1){    //如果插入成功！那么就装入true
                    resultMap.put("success","true");
                }
            }
        }

        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getInvitationsOf(HttpServletRequest request) {
        Integer be_invited = TokenUtils.getUid(request);    //获取当前正在登录用户的id
        if(be_invited == null) return null;            //如果当前没有用户登录  那么就返回null

        List<Invite> myInvitations = inviteDao.getMyInvitation(be_invited);   //获取该用户的所有邀请

        if(myInvitations == null && myInvitations.size() <= 0) return null;    //如果获得的所有邀请没有邀请 返回

        List<Map<String, Object>> resultList = new ArrayList<>();   //创建结果集合
        //使用set集合保存所有邀请者，所有问题，确保相同的对象值查询一次
        Set<Integer> myInvitationInviterIds = new HashSet<>();
        Set<Integer> myInvitationQuestionIds = new HashSet<>();

        myInvitations.forEach((invitation) -> {      //遍历该所有邀请的集合 并且将邀请者的id和问题的id分别存放到两个set中
            myInvitationInviterIds.add(invitation.getInviter());
            myInvitationQuestionIds.add(invitation.getWhich_question());
        });

        //只需要知道邀请者的id, head_photo_url, nickname
        Map<Integer, Map<String, Object>> inviterInfos = new HashMap<>();
        myInvitationInviterIds.forEach((inviter) -> {
            try {       //借用UserService的方法！
                inviterInfos.put(inviter, userService.getUserInfoOfComment(inviter));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //放入问题的标题信息title
        Map<Integer, String> invitationQuestionTitles = new HashMap<>();
        myInvitationQuestionIds.forEach((question) -> {
            try {//找根据问题的id找到问题的标题
                invitationQuestionTitles.put(question, questionDao.getQuestionById(question).get(0).getTitle());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //填充结果集: 邀请记录的id，邀请者的id，问题的id，问题标题title，邀请时间，是否已读
        myInvitations.forEach((invitation) -> {
            Map<String, Object> eachInvitation = new HashMap<>();
            eachInvitation.put("inviteId", invitation.getId());
            eachInvitation.put("inviter", inviterInfos.get(invitation.getInviter()));
            eachInvitation.put("questionId",invitation.getWhich_question());
            eachInvitation.put("questionTitle", invitationQuestionTitles.get(invitation.getWhich_question()));
            eachInvitation.put("inviteTime", invitation.getInvite_time());
            eachInvitation.put("isRead",invitation.getIs_read());
            resultList.add(eachInvitation);
        });

        //未读消息放在前面
        resultList.sort((r1,r2) -> {
            Integer r1IsRead = Integer.parseInt(r1.get("isRead") + "");
            Integer r2IsRead = Integer.parseInt(r2.get("isRead") + "");
            return r1IsRead.compareTo(r2IsRead);
        });

        try {
            return JacksonUtils.obj2json(resultList);   //此时返回结果即可
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String readInvites(List<Integer> readInviteIds, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");

        if(TokenUtils.isLogin(request)) {     //如果用户是已登录的状态
            readInviteIds.forEach((inviteId) -> {
                inviteDao.readInvite(inviteId);       //读取这些邀请！
            });
            resultMap.put("success","true");
        }
        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getHowManyNotReadInvitationOf(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");
        Integer be_invited = TokenUtils.getUid(request);

        if(be_invited != null){   //如果当前登录的用户不为空  才能继续看
            resultMap.put("howManyNotReadInvitation", inviteDao.getHowManyNotReadInvitationOf(be_invited));
            resultMap.put("success","true");
        }

        return JacksonUtils.mapToJson(resultMap);
    }
}

package com.atlxw.community.service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface InviteService {
    /**
     * 搜索用户和以及对应的邀请状态
     * @param questionId
     * @param searchStr
     * @param request
     * @return
     */
    String getSearchUserAndInvitationState(int questionId, String searchStr, HttpServletRequest request);

    /**
     * 获取当前问题的作者关注的人的邀请状态
     * @param questionId  问题的id
     * @param request     request对象
     * @return
     */
    String getFollowsAndInivitationState(int questionId, HttpServletRequest request);

    /**
     * 邀请其它用户来回答问题  在invite表中插入记录
     * @param questionId   问题的id
     * @param be_invited   被邀请用户的id
     * @param request      request对象  来获取当前用户的id
     * @return
     */
    String invite(int questionId, int be_invited, HttpServletRequest request);

    /**
     * 查询当前登录用户的被邀请的记录
     * @param request
     * @return
     */
    String getInvitationsOf(HttpServletRequest request);

    /**
     * 读取邀请信息
     * @param readInviteIds  邀请信息的数组
     * @param request
     * @return
     */
    String readInvites(List<Integer> readInviteIds, HttpServletRequest request);

    /**
     * 获取当前登录的用户有多少没有读取的邀请！
     * @param request
     * @return
     */
    String getHowManyNotReadInvitationOf(HttpServletRequest request);
}

package com.atlxw.community.controller;

import com.atlxw.community.service.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/invite")
public class InviteController {
    @Autowired
    private InviteService inviteService;

    /**
     * 搜索用户和以及对应的邀请状态
     * @param questionId
     * @param searchStr
     * @param request
     * @return
     */
    @PostMapping("/getSearchUserAndInvitationState")
    public String getSearchUserAndInvitationState(@RequestParam("questionId") String questionId,
                                                  @RequestParam("searchStr") String searchStr,
                                                  HttpServletRequest request){
        try {
            return inviteService.getSearchUserAndInvitationState(Integer.parseInt(questionId), searchStr, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前问题的作者关注的人的邀请状态
     * @param questionId  问题的id
     * @param request     request对象
     * @return
     */
    @PostMapping("/getFollowsAndInivitationState")
    public String getFollowsAndInivitationState(@RequestParam("questionId") String questionId,
                                                HttpServletRequest request){
        try {
            return inviteService.getFollowsAndInivitationState(Integer.parseInt(questionId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 邀请其它用户来回答问题  在invite表中插入记录
     * @param questionId   问题的id
     * @param be_invited   被邀请用户的id
     * @param request      request对象  来获取当前用户的id
     * @return
     */
    @PostMapping("/doInvite")
    public String doInvite(@RequestParam("questionId") String questionId,
                           @RequestParam("be_invited") String be_invited,
                           HttpServletRequest request){
        try {
            return inviteService.invite(Integer.parseInt(questionId), Integer.parseInt(be_invited), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询当前登录用户所有被邀请记录
     * @param request
     * @return
     */
    @PostMapping("/getInvitationsOf")
    public String getInvitationsOf(HttpServletRequest request){
        return inviteService.getInvitationsOf(request);
    }

    /**
     * 读取邀请信息
     * @param readInviteIds  邀请信息的数组
     * @param request
     * @return
     */
    @PostMapping("/readInvites")
    public String readInvites(@RequestBody List<Integer> readInviteIds,
                              HttpServletRequest request){
        return inviteService.readInvites(readInviteIds, request);
    }


    /**
     * 获取当前登录的用户有多少没有读取的邀请！
     * @param request
     * @return
     */
    @PostMapping("/getHowManyNotReadInvitationOf")
    public String getHowManyNotReadInvitationOf(HttpServletRequest request){
        return inviteService.getHowManyNotReadInvitationOf(request);
    }
}

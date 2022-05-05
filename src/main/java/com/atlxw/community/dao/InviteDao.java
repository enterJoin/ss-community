package com.atlxw.community.dao;

import com.atlxw.community.entity.Invite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface InviteDao {
    /**
     * 根据问题的id，和当前登录用户的id，还有查找的被邀请者的id
     * 只需要查询inviter和be_invited即可！
     * @param questionId  问题的id
     * @param uid         登录用户的id
     * @param searchStr   跟着这个字符串查询用户
     * @return
     */
    List<Invite> getSearchUsersInvitationStateOf(@Param("questionId") int questionId, @Param("uid") Integer uid, @Param("searchStr") String searchStr);

    /**
     * 获取当前登录用户uid关注的用户的邀请状态（有没有被邀请）
     * @param questionId   问题id
     * @param uid          当前登录用户的id
     * @return
     */
    List<Invite> getFollowsInvitationStateOf(@Param("questionId") int questionId, @Param("uid") Integer uid);

    /**
     * 查看该invite对象有没有相关存在的记录！
     * @param invite
     * @return
     */
    int isAlreadyInvited(Invite invite);

    /**
     * 将该invite对象插入到对应的数据库中
     * @param invite
     * @return
     */
    int invite(Invite invite);

    /**
     * 获取id为be_invited的用户所有被邀请的记录  全部字段都要查询
     * @param be_invited  被邀请的用户id
     * @return
     */
    List<Invite> getMyInvitation(Integer be_invited);

    /**
     * 读取id为inviteId的邀请
     * @param inviteId  邀请的id
     */
    void readInvite(Integer inviteId);

    /**
     * 获取该用户id为be_invited的被邀请者  有多少条被邀请的信息是未读的！
     * @param be_invited
     * @return
     */
    Integer getHowManyNotReadInvitationOf(Integer be_invited);
}

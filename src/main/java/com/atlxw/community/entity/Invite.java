package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invite {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 邀请者的ID
     */
    private Integer inviter;
    /**
     * 被邀请者的ID
     */
    private Integer be_invited;
    /**
     * 被邀请回答的问题的ID
     */
    private Integer which_question;
    /**
     * 是否已读: 0(未读), 1(已读)
     */
    private Integer is_read;
    /**
     * 邀请发出的时间
     */
    private Timestamp invite_time;
}

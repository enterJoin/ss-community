package com.atlxw.community.controller;

import com.atlxw.community.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 添加新的评论
     * @param to_comment       被评论的文章或者回答的ID
     * @param to_comment_type  被评论的文本的类型  0(回答)  1(文章)
     * @param content          被评论的内容
     * @param request
     * @return
     */
    @PostMapping("/insertComment")
    public String insertComment(@RequestParam("to_comment") String to_comment,
                                @RequestParam("to_comment_type") String to_comment_type,
                                @RequestParam("content") String content,
                                HttpServletRequest request){
        return commentService.handleNewComment(
                Integer.parseInt(to_comment),
                Integer.parseInt(to_comment_type),
                content,
                request
        );
    }

    /**
     * 获得前面几个评论
     * @param to_comment
     * @param to_comment_type
     * @return
     */
    @GetMapping("/getTopComments")
    public String getTopComments(@RequestParam("to_comment") String to_comment,
                                 @RequestParam("to_comment_type") String to_comment_type){
        return commentService.getTopComment(Integer.parseInt(to_comment),
                Integer.parseInt(to_comment_type));
    }


    /**
     * 获得所有的评论
     * @param to_comment
     * @param to_comment_type
     * @return
     */
    @GetMapping("/getRemainAllComments")
    public String getRemainAllComments(@RequestParam("to_comment") String to_comment,
                                       @RequestParam("to_comment_type") String to_comment_type){
        return commentService.getRemainAllComment(Integer.parseInt(to_comment),
                Integer.parseInt(to_comment_type));
    }

    /**
     * 给评论点赞和踩
     * @param commentId
     * @param likeOrDislike
     * @return
     */
    @GetMapping("/evaluate")
    public String evaluate(@RequestParam("commentId") String commentId,
                           @RequestParam("likeOrDislike") String likeOrDislike){
        try {
            return commentService.likeOrDislike(Integer.parseInt(commentId), Integer.parseInt(likeOrDislike));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户的id = commentator，来获取该用户所有的评论
     * @param commentator  评论者的id（用户id）
     * @return
     */
    @GetMapping("/getCommentsBy")
    public String getCommentsBy(@RequestParam("commentator") String commentator){
        try {
            return commentService.getCommentsBy(Integer.parseInt(commentator));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}

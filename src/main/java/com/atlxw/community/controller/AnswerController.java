package com.atlxw.community.controller;

import com.atlxw.community.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/answer")
public class AnswerController {
    @Autowired
    private AnswerService answerService;

    /**
     * 插入一个新的回答
     * @param which_question  问题的id
     * @param content         问题回答的内容
     * @param request         请求对象
     * @return
     */
    @PostMapping("/newAnswer")
    public String insertNewAnswer(@RequestParam("which_question") String which_question,
                                  @RequestParam("content") String content,
                                  HttpServletRequest request) {
        return answerService.insertNewAnswer(Integer.parseInt(which_question), content, request);
    }

    /**
     * 获得最新的回答  按照回答的提交时间排序
     * @param which_question   问题的id
     * @param pageIndex        当前请求显示的页数
     * @param request          request对象
     * @return
     */
    @GetMapping("/getSomeAnswerNew")
    public String getSomeAnswerNew(@RequestParam("which_question") String which_question,
                                   @RequestParam("pageIndex") String pageIndex,
                                   HttpServletRequest request){
        try {
            return answerService.getSomeAnswerNew(Integer.parseInt(which_question), Integer.parseInt(pageIndex), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得最热的回答  按照回答的被浏览次数排序
     * @param which_question   问题的id
     * @param pageIndex        当前请求显示的页数
     * @param request          request对象
     * @return
     */
    @GetMapping("/getSomeAnswerPopular")
    public String getSomeAnswerPopular(@RequestParam("which_question") String which_question,
                                       @RequestParam("pageIndex") String pageIndex,
                                       HttpServletRequest request){
        try {
            return answerService.getSomeAnswerPopular(Integer.parseInt(which_question), Integer.parseInt(pageIndex), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获得某个问题回答的总页面
     * @param which_question  问题ID
     * @return
     */
    @GetMapping("/getTotalPageCount")
    public String getTotalPageCount(@RequestParam("which_question") Integer which_question) {
        return answerService.getTotalPageCount(which_question);
    }


    /**
     * 获得具体的回答对象
     * @param answerId    回答的id
     * @param request     请求对象
     * @return
     */
    @GetMapping("/getAnswer")
    public String getAnswer(@RequestParam("answerId") String answerId,
                            HttpServletRequest request){
        return answerService.getSpecificAnswerById(Integer.parseInt(answerId), request);
    }

    /**
     * 对回答的id为answerId进行点赞或者踩
     * @param answerId       回答的id
     * @param likeOrDislike  0表示点踩  1表示点赞
     * @return
     */
    @GetMapping("/evaluate")
    public String likeOrDislike(@RequestParam("answerId") String answerId,
                           @RequestParam("likeOrDislike") String likeOrDislike){
        try {
            return answerService.likeOrDislike(Integer.parseInt(answerId), Integer.parseInt(likeOrDislike));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 根据用户id来获取该用户所写的所有的回答
     * @param authorId
     * @return
     */
    @GetMapping("/getAnswersWriteBy")
    public String getAnswersWriteBy(@RequestParam("authorId") String authorId){
        try {
            return answerService.getAnswersWriteBy(Integer.parseInt(authorId));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户的id来获取 用户收藏的回答 在collection表中type = 0
     * @param collector
     * @return
     */
    @GetMapping("/getAnswersCollectedBy")
    public String getAnswersCollectedBy(@RequestParam("collector") String collector){
        try {
            return answerService.getAnswersCollectedBy(Integer.parseInt(collector));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据回答的id来修改回答
     * @param editId
     * @param request
     * @return
     */
    @PostMapping("/getAnswerToEdit")
    public String getAnswerToEdit(@RequestParam("editId") String editId,
                                  HttpServletRequest request){
        try {
            return answerService.getAnswerToEdit(Integer.parseInt(editId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 修改回答的内容
     * @param editId
     * @param newContent
     * @param request
     * @return
     */
    @PostMapping("/updateAnswer")
    public String updateAnswer(@RequestParam("editId") String editId,
                               @RequestParam("newContent") String newContent,
                               HttpServletRequest request){
        try {
            return answerService.updateAnswer(Integer.parseInt(editId), newContent, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据回答的id来删除该回答以及该回答所有相关的记录
     * @param answerId
     * @param request
     * @return
     */
    @PostMapping("/deleteAnswer")
    public String deleteAnswer(@RequestParam("answerId") String answerId,
                               HttpServletRequest request){
        try {
            return answerService.deleteAnswer(Integer.parseInt(answerId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}

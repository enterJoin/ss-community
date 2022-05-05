package com.atlxw.community.controller;

import com.atlxw.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    /**
     * 新插入一个问题
     * @return
     */
    @PostMapping("/newQuestion")
    public String newQuestion(@RequestParam("title") String title,
                              @RequestParam("detail") String detail,
                              HttpServletRequest request){
        return questionService.insertNewQuestion(title, detail, request);
    }

    /**
     * 获取最新的问题（按照提交时间排序）
     * @param pageIndex
     * @param request
     * @return
     */
    @GetMapping("/getSomeQuestionNew")
    public String getSomeQuestionNew(@RequestParam("pageIndex") String pageIndex,
                                     HttpServletRequest request){
        try {
            Integer index = Integer.parseInt(pageIndex);
            return questionService.getSomeQuestionNew(index, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取热度最高的问题（按照浏览量排序）
     * @param pageIndex
     * @param request
     * @return
     */
    @GetMapping("getSomeQuestionPopular")
    public String getSomeQuestionPopular(@RequestParam("pageIndex") String pageIndex,
                                         HttpServletRequest request){
        try {
            Integer page = Integer.parseInt(pageIndex);
            return questionService.getSomeQuestionPopular(page, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到总页数
     * @return
     */
    @GetMapping("/getTotalPageCount")
    public String getTotalPageCount(){
        return questionService.getTotalPageCount();
    }

    /**
     * 根据问题的id来获得固定的问题的信息，单个页面显示该问题
     * @return
     */
    @GetMapping("/getQuestion")
    public String getQuestion(@RequestParam("questionId") String questionId,
                              HttpServletRequest request){
        try {
            return questionService.getSpecificQuestionById(Integer.parseInt(questionId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 给id为questionId进行点赞或者踩
     * @param questionId
     * @param likeOrDislike
     * @return
     */
    @GetMapping("/evaluate")
    public String evaluate(@RequestParam("questionId") String questionId,
                           @RequestParam("likeOrDislike") String likeOrDislike){
        try {
            return questionService.likeOrDislike(Integer.parseInt(questionId),
                    Integer.parseInt(likeOrDislike));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得该用户提出的问题
     * @param authorId
     * @return
     */
    @GetMapping("/getQuestionsAskedBy")
    public String getQuestionsAskedBy(@RequestParam("authorId") String authorId){
        try {
            return questionService.getQuestionsAskedBy(Integer.parseInt(authorId));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户的id来获取用户收藏的问题
     * @param collector  用户id
     * @return
     */
    @GetMapping("/getQuestionsCollectedBy")
    public String getQuestionsCollectedBy(@RequestParam("collector") String collector){
        try {
            return questionService.getQuestionsCollectedBy(Integer.parseInt(collector));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 修改id为editId的问题
     * @param editId
     * @return
     */
    @PostMapping("/getQuestionToEdit")
    public String getQuestionToEdit(@RequestParam("editId") String editId,
                                    HttpServletRequest request){
        try {
            return questionService.getQuestionToEdit(Integer.parseInt(editId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 修改问题
     * @param editId
     * @param newTitle
     * @param newDetail
     * @param request
     * @return
     */
    @PostMapping("/updateQuestion")
    public String updateQuestion(@RequestParam("editId") String editId,
                                 @RequestParam("newTitle") String newTitle,
                                 @RequestParam("newDetail") String newDetail,
                                 HttpServletRequest request){
        try {
            return questionService.updateQuestion(Integer.parseInt(editId), newTitle, newDetail, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除问题
     * @param questionId
     * @param request
     * @return
     */
    @PostMapping("/deleteQuestion")
    public String deleteQuestion(@RequestParam("questionId") String questionId,
                                 HttpServletRequest request){
        try {
            return questionService.deleteQuestion(Integer.parseInt(questionId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}

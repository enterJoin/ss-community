package com.atlxw.community.controller;

import com.atlxw.community.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    /**
     * 保存新的文章
     * @param title
     * @param content
     * @param request
     * @return
     */
    @PostMapping("/newArticle")
    public String newArticle(@RequestParam("title") String title,
                             @RequestParam("content") String content,
                             HttpServletRequest request){
        return articleService.insertNewArticle(title, content, request);
    }

    /**
     * 获得总页码是多少页
     * @return
     */
    @GetMapping("/getTotalPageCount")
    public String getTotalPageCount(){
        return articleService.getTotalPageCount();
    }

    /**
     * 按照页数获得最新发布的文章
     * @return
     */
    @GetMapping("/getSomeArticleNew")
    public String getSomeArticleNew(@RequestParam("pageIndex") String pageIndex,
                                    HttpServletRequest request){
        try {
            int page = Integer.parseInt(pageIndex);
            return articleService.getSomeArticleNew(page, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得浏览次数最多的文章
     * @param pageIndex
     * @param request
     * @return
     */
    @GetMapping("/getSomeArticlePopular")
    public String getSomeArticlePopular(@RequestParam("pageIndex") String pageIndex,
                                        HttpServletRequest request){
        try {
            int page = Integer.parseInt(pageIndex);
            return articleService.getSomeArticlePopular(page, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 记录用户对文章的点赞和踩的操作
     * @param articleId       文章ID
     * @param likeOrDislike   1为赞   0位踩
     * @return
     */
    @GetMapping("/evaluate")
    public String evaluate(@RequestParam("articleId") String articleId,
                           @RequestParam("likeOrDislike") String likeOrDislike){
        try {
            return articleService.likeOrDislike(Integer.parseInt(articleId), Integer.parseInt(likeOrDislike));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("getArticle")
    public String getArticle(@RequestParam("articleId") String articleId,
                             HttpServletRequest request) {
        return articleService.getSpecificArticleById(Integer.parseInt(articleId), request);
    }

    /**
     * 获取用户id为authorId写的文章
     * @param authorId   用户id
     * @return
     */
    @GetMapping("/getArticlesWriteBy")
    public String getArticlesWriteBy(@RequestParam("authorId") String authorId){
        try {
            return articleService.getArticlesWriteBy(Integer.parseInt(authorId));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据用户的id来获取其收藏文章  注意就只是文章而已！
     * @param collector
     * @return
     */
    @GetMapping("/getArticlesCollectedBy")
    public String getArticlesCollectedBy(@RequestParam("collector") String collector){
        try {
            return articleService.getArticlesCollectedBy(Integer.parseInt(collector));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得文章的id，从而得到文章的其它属性  然后返回即可
     * @param editId
     * @param request
     * @return
     */
    @PostMapping("/getArticleToEdit")
    public String getArticleToEdit(@RequestParam("editId") String editId,
                                   HttpServletRequest request){
        try {
            return articleService.getArticleToEdit(Integer.parseInt(editId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据articleId来删除文章的id
     * @param articleId
     * @param request
     * @return
     */
    @PostMapping("/deleteArticle")
    public String deleteArticle(@RequestParam("articleId") String articleId,
                                HttpServletRequest request){
        try {
            return articleService.deleteArticle(Integer.parseInt(articleId), request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 更新文章的信息
     * @param editId
     * @param newTitle
     * @param newContent
     * @param request
     * @return
     */
    @PostMapping("/updateArticle")
    public String updateArticle(@RequestParam("editId") String editId,
                                @RequestParam("newTitle") String newTitle,
                                @RequestParam("newContent") String newContent,
                                HttpServletRequest request){
        try {
            return articleService.updateArticle(Integer.parseInt(editId), newTitle, newContent, request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}

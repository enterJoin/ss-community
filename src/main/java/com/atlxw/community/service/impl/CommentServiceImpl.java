package com.atlxw.community.service.impl;

import com.atlxw.community.dao.AnswerDao;
import com.atlxw.community.dao.ArticleDao;
import com.atlxw.community.dao.CommentDao;
import com.atlxw.community.entity.Answer;
import com.atlxw.community.entity.Comment;
import com.atlxw.community.service.CommentService;
import com.atlxw.community.service.RedisService;
import com.atlxw.community.service.UserService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.TokenUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.*;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    private static final int howManyToShowAtOneTime = 5;//一次性显示多少评论，更多评论需要用户手动点击“查看所有评论”
    /**
     * 添加新的评论
     * @param to_comment         被评论的文本的id（文章或回复）
     * @param to_comment_type    被评论的文本的类型  0(回答)  1(文章)
     * @param content
     * @param request
     * @return
     */
    @Transactional
    @Override
    public String handleNewComment(Integer to_comment, Integer to_comment_type, String content, HttpServletRequest request) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");
        //查看文章内容是否合法
        if(content != null && content.length() > 0 && content.length() <= 200){
            Integer uid = TokenUtils.getUid(request);
            if(uid != null){        //如果用户已登录
                if(to_comment_type.equals(1)){    //如果被评论的文本是文章
                    //查看对应的文章是否存在
                    if(articleDao.getArticleById(to_comment).size() == 1){
                        //构造评论对象
                        Comment newComment = new Comment(null, content, uid, to_comment, to_comment_type, new Timestamp(System.currentTimeMillis()));
                        //执行插入到数据库的方法
                        if(commentDao.comment(newComment) == 1){   //如果插入成功
                            resultMap.put("success", "true");
                            resultMap.put("type", "article");
                        }
                    }
                } else if(to_comment_type.equals(0)){    //如果被评论的对象是回答
                    if(answerDao.getAnswerById(to_comment).size() == 1){
                        //构造评论对象
                        Comment newComment = new Comment(null, content, uid, to_comment, to_comment_type, new Timestamp(System.currentTimeMillis()));
                        //执行插入到数据库的方法
                        if(commentDao.comment(newComment) == 1){   //如果插入成功
                            resultMap.put("success", "true");
                            resultMap.put("type", "answer");
                        }
                    }
                }
            }
        }

        return JacksonUtils.mapToJson(resultMap);

    }

    /**
     * 获得最前面的几个评论
     * @param to_comment        被评论的文章或回答的id
     * @param to_comment_type   被评论的文本的类型    0(回答)  1(文章)
     * @return
     */
    @Override
    public String getTopComment(int to_comment, int to_comment_type) {
        return getComments(to_comment, to_comment_type, false);
    }

    /**
     * 获得所有的评论
     * @param to_comment        被评论的文章或回答的id
     * @param to_comment_type   被评论的文本的类型    0(回答)  1(文章)
     * @return
     */
    @Override
    public String getRemainAllComment(int to_comment, int to_comment_type) {
        return getComments(to_comment, to_comment_type, true);
    }

    /**
     * 给评论点赞或者踩
     * @param commentId
     * @param likeOrDislike
     * @return
     */
    @Override
    public String likeOrDislike(int commentId, int likeOrDislike) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("success","false");
        if(likeOrDislike == 0 || likeOrDislike == 1){
            redisService.likeOrDislike(commentId, 3, likeOrDislike);
            resultMap.put("success","true");
        }
        return JacksonUtils.mapToJson(resultMap);
    }

    @Override
    public String getCommentsBy(int commentator) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //获得所有该用户写的评论
        List<Comment> comments = commentDao.getCommentsBy(commentator);
        if(comments == null || comments.size() == 0){   //如果该用户没有评论  那么直接返回
            try {
                return JacksonUtils.obj2json(resultList);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }

        //使用set来避免重复的数据查询
        Set<String> commentTo_Set = new HashSet<>();
        for(Comment comment : comments){   //遍历每个Comment
            //该属性格式为1(0)-id     0为回答，1为文章，id就为被评论的回答或者文章的id
            commentTo_Set.add(comment.getTo_comment_type() + "-" + comment.getTo_comment());
        }

        //执行查询，结果保存在两个不同的Map中，分别对应回答的评论和文章的评论
        Map<Integer, String> answerCommentToWords = new HashMap<>();   //回答评论
        Map<Integer, String> articleCommentToWords = new HashMap<>();  //文章评论

        for(String s : commentTo_Set){   //将刚才存放type-id的set集合进行遍历
            Integer to_comment_id = Integer.parseInt(s.substring(2));
            if(s.charAt(0) == '0'){          //如果该类型为回答的评论
                //根据回答的记录的id来获得  写这条回答的作者的nickname
                List<String> answerNicknames = answerDao.getAnswererNicknameByAnswerId(to_comment_id);
                //如果该nickname有且仅有一个！
                if(answerNicknames != null && answerNicknames.size() == 1){
                    //那么将这个nickname记录下来
                    StringBuilder sb = new StringBuilder(answerNicknames.get(0));
                    if(sb.length() > 10){          //如果nickname的长度比10还要大
                        sb.delete(8, sb.length()); //那么就删除8到10这一部分
                        sb.append("...");
                    }
                    sb.append("的回答");
                    answerCommentToWords.put(to_comment_id, sb.toString());   //添加到回答的那个map中
                }
            } else if(s.charAt(0) == '1'){   //如果该类型为文章的评论
                //根据文章的记录的id来获得   这个文章的作者的nickname
                List<String> authorNicknames = articleDao.getAuthorNicknameByArticleId(to_comment_id);
                if(authorNicknames != null && authorNicknames.size() == 1){   //如果这个集合有且只有一个
                    StringBuilder sb = new StringBuilder();
                    if(sb.length() > 10){          //如果nickname的长度比10还要大
                        sb.delete(8, sb.length()); //那么就删除8到10这一部分
                        sb.append("...");
                    }
                    sb.append("的文章");
                    articleCommentToWords.put(to_comment_id, sb.toString());
                }
            }
        }

        //填充结果集  只需要 comment_time, content, to_comment_type, to_comment
        for(Comment comment : comments){
            //首先填充评论时间、评论内容、评论的类型
            Map<String, Object> eachComment = new HashMap<>();
            eachComment.put("commentTime", comment.getComment_time());
            eachComment.put("commentContent", comment.getContent());
            eachComment.put("commentType", comment.getTo_comment_type());

            //根据评论的类型来填充被评论的文本（文章，回答）的 id 以及 作者的nickname信息
            if(comment.getTo_comment_type().equals(0)){           //回答评论
                eachComment.put("commentTo", answerCommentToWords.get(comment.getTo_comment()));
                eachComment.put("clickId", comment.getTo_comment());
            } else if(comment.getTo_comment_type().equals(1)){    //文章评论
                eachComment.put("commentTo", articleCommentToWords.get(comment.getTo_comment()));
                eachComment.put("clickId", comment.getTo_comment());
            } else {
                return null;
            }
            //添加到List集合中
            resultList.add(eachComment);
        }

        //返回结果集即可
        try {
            return JacksonUtils.obj2json(resultList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 真正的获得所有的评论
     * @param to_comment        被评论的文章或回答的id
     * @param to_comment_type   被评论的文本的类型    0(回答)  1(文章)
     * @param needRemainAll     表示是否要显示所有的评论
     * @return
     */
    private String getComments(int to_comment, int to_comment_type, boolean needRemainAll) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        List<Comment> comments = null;
        resultMap.put("success", "false");
        if(needRemainAll){         //如果需要保留所有的评论
            comments = commentDao.getRemainAllComments(howManyToShowAtOneTime, to_comment, to_comment_type);
        } else {
            comments = commentDao.getTopComments(howManyToShowAtOneTime, to_comment, to_comment_type);
        }
        //使用Set来代替List，多个评论如果是同一个用户发布的，可以减少查询次数
        Set<Integer> commentators = new HashSet<>();
        for(Comment c : comments){             //获取所有的评论发布者
            commentators.add(c.getCommentator());
        }

        //这个Map记录了每个评论者的用户对象
        Map<Integer, Map<String, Object>> commentatorInfos = new HashMap<>();
        try {
            for(Integer commentator : commentators){
                //获得每个用户需要在评论里面展示的信息
                commentatorInfos.put(commentator, userService.getUserInfoOfComment(commentator));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return JacksonUtils.mapToJson(resultMap);
        }

        //填充结果List
        for(Comment comment : comments){
            Map<String, Object> eachComment = new HashMap<>();
            //评论者的信息，评论发表者的id
            eachComment.put("commentator", commentatorInfos.get(comment.getCommentator()));
            //评论主体，就是评论对象
            eachComment.put("comment", comment);
            //获取点赞次数
            eachComment.put("likeCount", redisService.getEvaluateCount(comment.getId(), 3, 1));
            //获取踩的次数
            eachComment.put("dislikeCount", redisService.getEvaluateCount(comment.getId(), 3, 0));
            //加入结果List中
            resultList.add(eachComment);
        }
        if(! needRemainAll){    //如果不需要保留所有的评论
            resultMap.put("isThatAll", "false");
            //如果总评论数小于或者等于howManyToShowAtOneTime规定展示的评论数，那么就是所有的评论
            if(commentDao.getCommentCountOf(to_comment, to_comment_type) <= howManyToShowAtOneTime){
                resultMap.put("isThatAll", "true");
            }
        }
        resultMap.put("success", "true");
        //将每个评论包括其用户对象存放到结果的resultMap中
        resultMap.put("comments", resultList);
        return JacksonUtils.mapToJson(resultMap);
    }
}

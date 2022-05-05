package com.atlxw.community.scheduled;

import com.atlxw.community.dao.AnswerDao;
import com.atlxw.community.dao.ArticleDao;
import com.atlxw.community.dao.ImageDao;
import com.atlxw.community.dao.QuestionDao;
import com.atlxw.community.entity.ContentImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 考虑如下业务场景：
 * 1、用户上传图片后，又觉得不需要这个图片，于是在Markdown正文里删除了图片链接
 * 2、用户后期编辑文章，删除了之前上传的图片的连接
 */
//TODO  不是一下子就扫描所有的文章 而是扫描部分的文章，模仿Redis的定期删除
@Component
public class CleanUnusedContentImageSchedule {
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private AnswerDao answerDao;
    @Autowired
    private ImageDao imageDao;

    @Transactional
    @Scheduled(cron = "0 0 0/6 * * ? ")   //从0小时开始，每6小时执行一次清理
    public void clean(){
        //所有图片对象的集合
        List<ContentImage> allContentImage = imageDao.getAllImage();
        //从数据库的文章里截取出来的图片资源URL片段组成的集合
        Map<String, Integer> contentImageUrlsFromArticleWords = new HashMap<>();
        //待删除的路径集合
        List<String> toDelete = new ArrayList<>();

        //所有文章的内容
        List<String> allArticleContent = articleDao.getAllArticleContent();
        //把所有文章里的所有URL加入到Map中
        for(String s : allArticleContent){
            List<String> eachContentUrls = analyzeContent(s);
            if(eachContentUrls != null){
                for(String eachContentUrl : eachContentUrls){
                    contentImageUrlsFromArticleWords.put(eachContentUrl, contentImageUrlsFromArticleWords.getOrDefault(eachContentUrl, 0) + 1);
                }
            }
        }

        //所有问题的详情
        List<String> allQuestionDetail = questionDao.getAllQuestionDetail();
        //把所有问题里的所有URL加入到Map中
        for(String s : allQuestionDetail){
            List<String> eachContentUrls = analyzeContent(s);
            if(eachContentUrls != null){
                for(String eachContentUrl : eachContentUrls){
                    contentImageUrlsFromArticleWords.put(eachContentUrl, contentImageUrlsFromArticleWords.getOrDefault(eachContentUrl, 0) + 1);
                }
            }
        }

        //所有回答的正文
        List<String> allAnswerContent = answerDao.getAllAnswerContent();
        //把所有回答里的所有URL加入到Map中
        for(String s : allAnswerContent){
            List<String> eachContentUrls = analyzeContent(s);
            if(eachContentUrls != null){
                for(String eachContentUrl : eachContentUrls){
                    contentImageUrlsFromArticleWords.put(eachContentUrl, contentImageUrlsFromArticleWords.getOrDefault(eachContentUrl, 0) + 1);
                }
            }
        }

        //遍历数据库中存的图片对象集合
        for(ContentImage contentImage : allContentImage){
            //如果Map里不存在此图片，说明该图片不再被引用
            if(contentImageUrlsFromArticleWords.get(contentImage.getUrl()) == null){
                //添加到待删除的集合中
                toDelete.add(contentImage.getPath());
                //删除数据库中的记录
                if(imageDao.deleteImageInfo(contentImage) <= 0){
                    throw new RuntimeException();
                }
            }
        }

        //最后清理文件
        doClean(toDelete);
    }

    /**
     * 分析每一篇文章的全文内容，找出图片的URL并添加到结果集中
     *
     * Markdown的图片语法示例：
     * ![](http://mtk.pub:8080/save/1584247263843-6.jpg)
     * [![test](http://mtk.pub:8080/save/1584247263843-6.jpg "test")](http://mtk.pub:8080/search.html "test")
     * 66![](dddd)6666![]6 ![](http://mtk.pub:8080/save/1584247263926-2.jpg) dsfasdfasdfasd
     * @param content
     * @return
     */
    public static List<String> analyzeContent(String content){
        if(content == null || content.length() == 0) return null;
        List<String> res = new ArrayList<>();
        for(int i = 0; i < content.length(); i++){
            //捕捉"!["
            if(i < content.length() - 1 && content.charAt(i) == '!' && content.charAt(i + 1) == '['){
                int j = i + 2;
                //寻找下一个']'
                while(j < content.length() && content.charAt(j) != ']') j++;
                j++;
                if(j + 1 < content.length()){
                    int k = j + 1;
                    while(k < content.length()){
                        if(content.charAt(k) == ')' || content.charAt(k) == ' '){
                            //找到可能的URl目标
                            String possibleUrl = content.substring(j + 1, k);
                            //进行进一步的正则表达式的判断
                            //必须是URl
                            Pattern p = Pattern.compile("^(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]$");
                            Matcher m = p.matcher(possibleUrl);

                            //如果正则表达式判断正确
                            if(m.matches()){
                                res.add(possibleUrl);
                            }
                            break;
                        } else {
                            k++;
                        }
                    }
                }

            }
        }

        return res;
    }

    /**
     * 执行清除操作，删除filePaths路径中的文件即可！
     * @param filePaths
     * @return
     */
    private boolean doClean(List<String> filePaths){
        for(String filePath : filePaths){
            File file = new File(filePath);
            if(file.exists()){
                return file.delete();
            }
        }

        return false;
    }
}

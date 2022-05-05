package com.atlxw.community.service.impl;

import com.atlxw.community.dao.AnswerDao;
import com.atlxw.community.dao.ArticleDao;
import com.atlxw.community.dao.QuestionDao;
import com.atlxw.community.dao.UserDao;
import com.atlxw.community.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private AnswerDao answerDao;

    @Override
    public Map<String, List<Map<String, String>>> searchPreview(String q) {
        Map<String, List<Map<String, String>>> result = new HashMap<>();

        List<Map<String, Object>> sumList = new ArrayList<>();

        //自然语言搜索，使用到mysql自带的ngram解析器，自动按照匹配度从高到低排序
        //需要对MATCH的列建立联合全文索引，并声明WITH PARSER ngram，在my.ini配置文件中[mysqld]下配置ngram_token_size=2
        List<Map<String, Object>> userSearchList = userDao.searchUserByFollowersCount(q);      //根据关注的人最多的顺序搜索
        List<Map<String, Object>> articleSearchList = articleDao.searchArticleInNaturalLanguageMode(q);
        List<Map<String, Object>> questionSearchList = questionDao.searchQuestionInNaturalLanguageMode(q);
        List<Map<String, Object>> answerSearchList = answerDao.searchAnswerInNaturalLanguageMode(q);

        sumList.addAll(userSearchList);
        sumList.addAll(articleSearchList);
        sumList.addAll(questionSearchList);
        sumList.addAll(answerSearchList);

        //按照匹配度进行降序排序
        sumList.sort((s1, s2) -> {
            Double d1 = (double) s1.get("score");
            Double d2 = (double) s2.get("score");
            return d1.compareTo(d2);
        });

        //取前七个结果
        List<Map<String, String>> resultList = new ArrayList<>();
        for(int i = 0; i < 7; i++){
            Map<String, String> eachRes = new HashMap<>();    //key需要有 name: (nickname, title, title, content)
            Map<String, Object> unit;       //unit的key需要有 user(id, nickname)  article(title, id)  question(title, id)  answer(content, id)
            //如果sumList存放的所有查询出来的对象都没有i个的话 就不用执行了
            if(sumList.size() > i && (unit = sumList.get(i)) != null){
                switch(unit.get("type") + ""){   //如果该对象的类型时user的话
                    case "user": {
                        eachRes.put("name", reduceLength(unit.get("nickname") + ""));  //放入用户名称
                        eachRes.put("description", "用户");
                        eachRes.put("html_url", "user.html?visitUid=" + unit.get("id"));
                        break;
                    }
                    case "article": {
                        eachRes.put("name", reduceLength(unit.get("title") + ""));
                        eachRes.put("description", "文章");
                        eachRes.put("html_url", "showArticle.html?articleId=" + unit.get("id"));
                        break;
                    }
                    case "question": {
                        eachRes.put("name", reduceLength(unit.get("title") + ""));
                        eachRes.put("description", "问题");
                        eachRes.put("html_url", "showQuestion.html?questionId=" + unit.get("id"));
                        break;
                    }
                    case "answer": {
                        eachRes.put("name", reduceLength(unit.get("content") + ""));
                        eachRes.put("description", "回答");
                        eachRes.put("html_url", "showAnswer.html?answerId=" + unit.get("id"));
                        break;
                    }
                }
                resultList.add(eachRes);
            } else {     //没有i个就直接跳出
                break;
            }
        }
        result.put("items", resultList);
        return result;
    }

    private String reduceLength(String s){
        if(s.length() > 20){
            s = s.substring(0, 20) + "...";
        }

        return s;
    }
}

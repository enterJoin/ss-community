package com.atlxw.community.service.impl;

import com.atlxw.community.dao.CollectionDao;
import com.atlxw.community.service.CollectionService;
import com.atlxw.community.service.RedisService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class CollectionServiceImpl implements CollectionService {
    @Autowired
    private CollectionDao collectionDao;
    @Autowired
    private RedisService redisService;

    @Override
    public String toggleCollect(int collection_id, int collection_type, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success","false");
        Integer collector = TokenUtils.getUid(request);  //获取当前登录的id

        String res = null;
        if(collector != null){
            if((res = redisService.toggleCollect(collection_id, collection_type, collector)) != null);
            resultMap.put("success","true");
            resultMap.put("type", res);
        }

        return JacksonUtils.mapToJson(resultMap);
    }
}

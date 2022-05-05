package com.atlxw.community.service;

import javax.servlet.http.HttpServletRequest;

public interface CollectionService {
    /**
     * 添加收藏记录  指定的类型和指定的id
     * @param collection_id    收藏的Id
     * @param collection_type  收藏的类型
     * @param request
     * @return
     */
    String toggleCollect(int collection_id, int collection_type, HttpServletRequest request);
}

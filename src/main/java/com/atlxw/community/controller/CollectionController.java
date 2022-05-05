package com.atlxw.community.controller;

import com.atlxw.community.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/collection")
public class CollectionController {
    @Autowired
    private CollectionService collectionService;

    /**
     * 添加收藏记录  指定的类型和指定的id
     * @param collection_id    收藏的Id
     * @param collection_type  收藏的类型
     * @param request
     * @return
     */
    @PostMapping("/toggleCollection")
    public String toggleCollection(@RequestParam("collection_id") String collection_id,
                                   @RequestParam("collection_type") String collection_type,
                                   HttpServletRequest request){
        try {
            return collectionService.toggleCollect(Integer.parseInt(collection_id),
                    Integer.parseInt(collection_type),
                    request);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}

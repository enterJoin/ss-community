package com.atlxw.community.controller;

import com.atlxw.community.service.ContentImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @RestController: 返回json对象
 * @RequestMapping("/contentImage"): 表示匹配到/contentImage路径
 */
@RestController
@RequestMapping("/contentImage")
public class ContentImageController {

    @Autowired
    private ContentImageService contentImageService;

    /**
     * 用于处理文章内容的图片上传
     * @return
     */
    @PostMapping("/uploadImage")
    public String uploadImage(@RequestParam("editormd-image-file") MultipartFile file,
                              HttpServletRequest request){
        return contentImageService.handleUploadImage(request, file);
    }
}

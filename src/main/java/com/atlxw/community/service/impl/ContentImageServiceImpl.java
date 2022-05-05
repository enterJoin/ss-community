package com.atlxw.community.service.impl;

import com.atlxw.community.config.ImageResourceMapper;
import com.atlxw.community.dao.ImageDao;
import com.atlxw.community.entity.ContentImage;
import com.atlxw.community.io.ContentImageIO;
import com.atlxw.community.service.ContentImageService;
import com.atlxw.community.utils.JacksonUtils;
import com.atlxw.community.utils.RequestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContentImageServiceImpl implements ContentImageService {
    @Autowired
    private ImageDao imageDao;
    @Autowired
    private ContentImageIO contentImageIO;

    /**
     * 用于处理文章中的图片上传
     * @param request
     * @param file
     * @return
     */
    @Override
    public String handleUploadImage(HttpServletRequest request, MultipartFile file) {
        Map<String, Object> responseJson = new HashMap<>();
        String saveRes = contentImageIO.saveImageFile(file);

        if(saveRes.equals("写入失败")){    //如果saveImageFile方法写入失败
            responseJson.put("success", 0);
        } else {       //这里是写入成功，此时的saveRes代表的就是生成的文件名
            String path = ImageResourceMapper.getContentPhotoFileLocationByEnvironment() + saveRes;   //得到存放文章里面的图片的物理路径
            //获得外部访问的URL
            String url = RequestUtils.getProjectRootUrl(request) + ImageResourceMapper.contentPhotoUrlSuffix.substring(1) + saveRes;
            //创建图片的对象
            ContentImage contentImage = new ContentImage(null, saveRes, path, url, new Timestamp(System.currentTimeMillis()));

            if(imageDao.insertImageInfo(contentImage) > 0){      //如果数据库插入成功，那么按照前端返回要求
                responseJson.put("success", 1);
                responseJson.put("message", "上传成功");
                responseJson.put("url", url);
            } else {          //数据库插入失败 那么就返回失败
                responseJson.put("success", 0);
            }
        }

        try {
            return JacksonUtils.obj2json(responseJson);
        } catch (JsonProcessingException e) {    //如果转化成为JSON类型转化失败
            e.printStackTrace();
        }

        return null;
    }
}

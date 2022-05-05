package com.atlxw.community.dao;

import com.atlxw.community.entity.ContentImage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ImageDao {
    /**
     * 插入文章中的图片记录
     * @param contentImage
     * @return
     */
    Integer insertImageInfo(ContentImage contentImage);

    /**
     * 得到所有的图片对象(MarkDown文本里面的图片)
     * @return
     */
    List<ContentImage> getAllImage();

    /**
     * 删除对应的图片记录
     * @param contentImage
     * @return
     */
    Integer deleteImageInfo(ContentImage contentImage);
}

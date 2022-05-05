package com.atlxw.community.io;

import com.atlxw.community.utils.PictureUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CustomIO {
    private static final long MAX_ALLOWED_SIZE = 1048576L;//最大允许1MB的图片

    /**
     * 用来将用户上传的图片或其他文件保存在本地硬盘上
     * 若保存成功，会返回一个Map，包含带生成文件名的物理路径和生成文件名
     * 若保存失败，返回null
     * @param whereToSave    希望存放的物理路径
     * @param multipartFile  用户上传的文件
     * @param supportSuffix  允许的文件后缀
     * @return
     */
    public Map<String, String> daSave(String whereToSave, MultipartFile multipartFile, List<String> supportSuffix){
        Map<String, String> res = null;
        //获取文件名
        String originalName = multipartFile.getOriginalFilename();
        if(originalName != null){
            //获取文件最后一个.的索引位置
            int dotIndex = originalName.lastIndexOf(".");
            if(dotIndex != -1){
                //获得文件名的后缀 包括.！！！
                String suffix = originalName.substring(dotIndex);
                //如果该文件后缀支持的话
                if(supportSuffix.contains(suffix)){
                    //生成一个文件名
                    String autoGenerateName = UUID.randomUUID().toString() + suffix;
                    //创建即将要存放的文件对象
                    File toSave = new File(whereToSave + autoGenerateName);

                    if(! toSave.exists()){   //如果路径或者文件不存在
                        if(! toSave.mkdirs()){    //如果创建目录失败 那么就直接返回false
                            return null;
                        }
                    }

                    //执行存储
                    try {
                        multipartFile.transferTo(toSave);
                        //若用户上传的文件过大，那么就需要进行压缩处理
                        if(toSave.length() > MAX_ALLOWED_SIZE){
                            //重新生成一个新的图片文件名称，表示压缩后的图片
                            autoGenerateName = UUID.randomUUID().toString() + suffix;

                            //创建压缩图片文件
                            File lowQualityImage = new File(whereToSave + autoGenerateName);

                            //压缩至500KB左右
                            PictureUtils.lowQuality(whereToSave + toSave.getName(), whereToSave + lowQualityImage.getName(), 512);

                            //删除未压缩的图片文件
                            if(! toSave.delete()){   //如果删除失败
                                return null;
                            }

                            //替换压缩后的图片文件
                            toSave = lowQualityImage;
                        }
                        //图片保存成功！！！
                        res = new HashMap<>();
                        res.put("filePath", whereToSave + autoGenerateName);
                        res.put("name", autoGenerateName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return res;
    }
}

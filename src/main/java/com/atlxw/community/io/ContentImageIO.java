package com.atlxw.community.io;

import com.atlxw.community.config.ImageResourceMapper;
import com.atlxw.community.utils.PictureUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

@Component
public class ContentImageIO {
    //最大允许的上传图片的大小
    private static final long maxAllowedSize = 1048576L;//1MB

    /**
     * 存储内容的图片文件到本地磁盘
     * 如果失败会返回写入失败，成功的话会返回写入成功的新文件名
     * @param file 用户上传的图片
     * @return     文件名或失败信息
     */
    public String saveImageFile(MultipartFile file){
        int dotIndex = Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf(".");
        File newImage = null;

        if(dotIndex != -1){
            //将文件的后缀传入该方法中
            String autoGenerateFilename = generateFilename(file.getOriginalFilename().substring(dotIndex));
            //注意此时新的文件对象
            newImage = new File(ImageResourceMapper.getContentPhotoFileLocationByEnvironment() + autoGenerateFilename);

            System.out.println(newImage.getName());
            //第一次创建需要生成目录
            if(! newImage.exists()){       //如果这个文件不存在
                if(! newImage.mkdirs()){   //如果创建目录失败
                    return "写入失败";
                }
            }

            try{
                //上传文件
                file.transferTo(newImage);
                //若用户上传的文件图片过大，则需要进行压缩处理
                if(newImage.length() > maxAllowedSize){
                    //重新生成一个新的图片文件名称，表示压缩后的图片，创建压缩后的文件名
                    autoGenerateFilename = generateFilename(file.getOriginalFilename().substring(dotIndex));

                    //创建压缩图片文件
                    File lowQualityImage = new File(ImageResourceMapper.getContentPhotoFileLocationByEnvironment() + autoGenerateFilename);

                    //压缩到500KB左右
                    PictureUtils.lowQuality(ImageResourceMapper.getContentPhotoFileLocationByEnvironment() + newImage.getName(), ImageResourceMapper.getContentPhotoFileLocationByEnvironment() + lowQualityImage.getName(), 512);

                    //删除未压缩的图片文件
                    if(! newImage.delete()) return "写入失败";   //如果删除失败 那么写入失败

                    newImage = lowQualityImage;
                }
            } catch (Exception e){
                e.printStackTrace();
                return "写入失败";
            }
        }

        return newImage == null ? "写入失败" : newImage.getName();

    }

    /**
     * 使用UUID确保文件名的唯一性
     * @param suffix 文件后缀
     * @return 随机生成的名字
     */
    private synchronized String generateFilename(String suffix) {
        return UUID.randomUUID().toString() + suffix;
    }
}

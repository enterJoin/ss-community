package com.atlxw.community.utils;

import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

public class PictureUtils {

    /**
     * 压缩图片同时保存图片在对应的路径上
     * @param source
     * @param target
     * @param targetSize
     * @throws IOException
     */
    public static void lowQuality(String source, String target, long targetSize) throws IOException {
        File f = new File(source);
        //不允许图片大小过低或者过高，阈值为250KB ~ 5MB
        if(targetSize < 250 || targetSize > 5120) return;
        //如果文件存在并且容量大于需求容量
        if(f.exists() && (f.length() > targetSize << 10)){  //左移10位，KB转化为字节
            float v = (float)(targetSize << 10) / f.length();
            Thumbnails.of(source).scale(1.00f).outputQuality(v).toFile(target);
        }
    }
}

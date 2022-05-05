package com.atlxw.community.config;

/**
 * 静态资源映射类 按照自己的环境配置即可！
 */
public class ImageResourceMapper {
    //用户头像的存储映射

    //要在浏览器显示的URL尾段，类似http://localhost/image/1.jpg
    public static final String headPhotoUrlSuffix = "/headPhotoImage/";
    //Window本地文件夹，注意路径不能省略最后一个/
    public static final String headPhotoFileLocation = "F:\\shuangshuang-community\\image\\headPhotoImage\\";
    //Linux环境本地文件夹
    public static final String linuxHeadPhotoFileLocation = "/opt/image/ss-community/headPhotoImage/";

    //文章里的图片的存储映射

    //要在浏览器显示的URL尾段，类似http://localhost/image/1.jpg
    public static final String contentPhotoUrlSuffix = "/contentPhotoImage/";
    //本地文件夹，注意路径不能省略最后一个/
    public static final String contentPhotoFileLocation = "F:\\shuangshuang-community\\image\\contentPhotoImage\\";
    //Linux环境本地文件夹
    public static final String linuxContentPhotoFileLocation = "/opt/image/ss-community/contentPhotoImage/";

    /**
     * 获取当前操作系统的环境
     * @return
     */
    public static String getEnvironment(){
        if(System.getProperty("os.name").toLowerCase().contains("linux")){
            return "linux";
        } else if(System.getProperty("os.name").toLowerCase().contains("windows")){
            return "windows";
        } else {
            return System.getProperty("os.name");
        }
    }

    /**
     * 得到用户头像图片本地的文件路径名
     * @return
     */
    public static String getHeadPhotoFileLocationByEnvironment(){
        if("linux".equals(getEnvironment())){
            return linuxHeadPhotoFileLocation;
        } else if("windows".equals(getEnvironment())){
            return headPhotoFileLocation;
        } else {
            throw new RuntimeException("Unsupported Running Environment");
        }
    }

    /**
     * 得到文章图片内容的本地文件地址
     * @return
     */
    public static String getContentPhotoFileLocationByEnvironment(){
        if("linux".equals(getEnvironment())){
            return linuxContentPhotoFileLocation;
        } else if("windows".equals(getEnvironment())){
            return contentPhotoFileLocation;
        } else {
            throw new RuntimeException("Unsupported Running Environment");
        }
    }
}

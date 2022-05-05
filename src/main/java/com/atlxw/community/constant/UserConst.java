package com.atlxw.community.constant;

import java.util.ArrayList;
import java.util.List;

public class UserConst {
    /**
     * 用户在redis中存放邮箱的前缀
     */
    public static final String USER_EMAIL_PREFIX = "user-email-";

    /**
     * 用户在redis中存放注册时的验证码的前缀
     */
    public static final String USER_CODE_PREFIX = "user-register-code-";

    /**
     * 保存图片允许的后缀名
     */
    public static List<String> supportSuffix = new ArrayList<>(5);

    /**
     * 用户在redis中存放找回密码的验证码和邮箱的前缀(都是这个)
     * 只不过邮箱为: prefix + UUID
     * 验证码为: prefix + email
     */
    public static final String USER_FIND_PASSWORD_PREFIX = "user-find-password-code-";

    static{
        //设置图片允许的后缀名
        supportSuffix.add(".jpg");
        supportSuffix.add("jpeg");
        supportSuffix.add(".png");
        supportSuffix.add(".gif");
        supportSuffix.add(".ico");
    }
}

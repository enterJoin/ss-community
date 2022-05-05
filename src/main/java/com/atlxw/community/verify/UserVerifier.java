package com.atlxw.community.verify;

import com.atlxw.community.entity.User;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserVerifier {
    /**
     * 判断用户名和名称是否合法
     * @param user
     * @return
     */
    public boolean userRegisterVerify(User user){
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9]{4,16}$");
        Pattern nicknamePattern = Pattern.compile("^[\\u4e00-\\u9fa5_a-zA-Z0-9]{2,10}$");

        boolean usernamePatternMatches = usernamePattern.matcher(user.getUsername()).matches();
        boolean nicknamePatternMatches = nicknamePattern.matcher(user.getNickname()).matches();

        return usernamePatternMatches && nicknamePatternMatches;
    }

    /**
     * 验证邮箱是否合格
     * @param email
     * @return
     */
    public boolean emailFormatVerify(String email){
        Pattern emailPattern = Pattern.compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");
        return emailPattern.matcher(email).matches();
    }
}

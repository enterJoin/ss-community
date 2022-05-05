package com.atlxw.community.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenUtils {
    private static final long EXPIRE_TIME_DAY = 24 * 60 * 60 * 1000; //一天
    private static final String TOKEN_SECRET = "matskqabbs000106";   //密钥盐

    /**
     * 签名生成
     * @param id
     * @return
     */
    public static String sign(Integer id){
        try {
            Date date = new Date(System.currentTimeMillis() + (EXPIRE_TIME_DAY) * 7);//自动登录一周

            //使用私钥和加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);

            //设置头部信息
            Map<String, Object> header = new HashMap<>(4);
            header.put("typ", "JWT");
            header.put("alg", "HS256");
            //返回token字符串
            return JWT.create()
                    .withHeader(header)             //第一部分：头部(header)
                    .withClaim("uid", id)     //第二部分：荷载(payload)
                    .withExpiresAt(date)            //设置该token的过期时间
                    .sign(algorithm);               //第三部分：标签
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取token信息
     * @param token
     * @return
     */
    public static Map<String, Claim> getTokenInfo(String token){
        DecodedJWT jwt;

        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            jwt = verifier.verify(token);
            return jwt.getClaims();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 查看用户是否登录：
     *      遍历Cookie，看是否有key为token的 --> 查询成功，验证token，验证通过就返回true
     * @param request
     * @return
     */
    public static boolean isLogin(HttpServletRequest request){
        //客户端每次同源请求会自动带上cookies
        Cookie[] cookies = request.getCookies();
        boolean isVerified = false;     //设置标志看用户是否登录
        if(cookies != null && cookies.length != 0){
            for(Cookie cookie : cookies){
                //如果说此时这个cookie的key就是token，那么就进行解密，看是否登录成功！
                if(cookie.getName().equals("token")){
                    try {
                        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
                        //生成JWT验证对象
                        JWTVerifier verifier = JWT.require(algorithm).build();
                        //验证token是否合法
                        verifier.verify(cookie.getValue());
                        //将验证通过设置为true
                        isVerified = true;
                        break;
                    } catch (IllegalArgumentException e) {
                        isVerified = false;
                    }
                }

            }
        }

        return isVerified;
    }

    /**
     * 从reequest中直接获取uid
     * @return
     */
    public static Integer getUid(HttpServletRequest request){
        Integer uid = null;
        //客户端每次同源请求会自动带上cookies
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length != 0){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals("token")){
                    try {
                        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
                        JWTVerifier verifier = JWT.require(algorithm).build();
                        Map<String, Claim> claims = verifier.verify(cookie.getValue()).getClaims();
                        for(Map.Entry<String, Claim> entry : claims.entrySet()){
                            if("uid".equals(entry.getKey())){
                                uid = entry.getValue().asInt();
                                break;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return uid;
    }

}

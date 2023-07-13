package com.cnpc.epai.core.worktask.util;

import com.cnpc.epai.common.util.SpringManager;
import com.cnpc.epai.common.util.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @ClassName: JwtUtil
 * @Description:
 * @Author
 * @Date 2022/11/25
 * @Version 1.0
 */

public class JwtUtil {

    String PUBLIC_KEY = SpringManager.getConfig().getProperty("security.oauth2.resource.jwt.keyValue");

    /**
     * 获取有效载荷
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token){
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(getPublicKeyFromString(PUBLIC_KEY))
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return claims;
    }

    /*
     * @Description 公钥解码
     * @Param key
     * @return PublicKey
     */
    private  PublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
        String publicKeyPEM = key.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        byte[] keyBytes = Base64.getMimeDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /*
     * @Description 根据token 获取用户id
     * @Param token
     * @return String
     */
    public User getUserFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String displayName = (String) claims.get("display_name");
        String userId = (String) claims.get("user_id");
        return User.builder().userId(userId)
                .displayName(displayName)
                .build();

    }
}

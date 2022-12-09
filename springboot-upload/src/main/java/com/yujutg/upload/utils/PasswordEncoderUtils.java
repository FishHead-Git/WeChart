package com.yujutg.upload.utils;

import java.nio.charset.Charset;
import java.util.Base64;

public class PasswordEncoderUtils {

    public static String encoder(String code){
        return Base64.getEncoder().encodeToString(code.getBytes());
    }

    public static byte[] decoder(String code){
        return Base64.getDecoder().decode(code);
    }
    public static String decoderStr(String code){
        return new String(Base64.getDecoder().decode(code), Charset.defaultCharset());
    }

}

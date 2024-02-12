package com.enrich.authn.util;

import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

@Component
public class Base64Util {

    public static String encode(String plainText) {
        byte[] encodedBytes = Base64Utils.encode(plainText.getBytes());
        return new String(encodedBytes);
    }

    public static String decode(String encodedText) {
        byte[] decodedBytes = Base64Utils.decode(encodedText.getBytes());
        return new String(decodedBytes);
    }


}

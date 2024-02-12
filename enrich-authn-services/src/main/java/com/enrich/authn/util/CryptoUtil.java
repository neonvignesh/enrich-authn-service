package com.enrich.authn.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CryptoUtil {

	private final com.enrich.authn.provider.AuthnServiceProvider AuthnServiceProvider;
	@Bean
	@Lazy
	  public <T> T decrypt(String encryptedData,Class<T> clazz) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		  String decodedData =  RSAUtil.decrypt(encryptedData, AuthnServiceProvider.getPrivatekey());
	        return new Gson().fromJson(decodedData, clazz);
	    }
}

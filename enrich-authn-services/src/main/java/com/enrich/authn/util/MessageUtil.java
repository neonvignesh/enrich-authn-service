package com.enrich.authn.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.enrich.authn.pojo.AccessTokenMessage;
import com.enrich.authn.pojo.CommonMessage;
import com.enrich.authn.pojo.CommonMessageJkey;
import com.enrich.authn.pojo.ErrorMessage;

@Component
public class MessageUtil {
	public static List<CommonMessage> getMessage(String message) {
		CommonMessage commonMessage = new CommonMessage();
		commonMessage.setMessage(message);
		List<CommonMessage> commonMessageList = new ArrayList<>();
		commonMessageList.add(commonMessage);
		return commonMessageList;
	}

	public static List<ErrorMessage> getErrorMessage(String message) {
		ErrorMessage commonMessage = new ErrorMessage();
		commonMessage.setError_message(message);
		List<ErrorMessage> commonMessageList = new ArrayList<>();
		commonMessageList.add(commonMessage);
		return commonMessageList;
	}

	public static List<CommonMessageJkey> getMessageJkey(String jKey) {
		CommonMessageJkey commonMessage = new CommonMessageJkey();
		commonMessage.setMessage("jKey Generated");
		commonMessage.setJKey(jKey);
		List<CommonMessageJkey> commonMessageList = new ArrayList<>();
		commonMessageList.add(commonMessage);
		return commonMessageList;
	}

	public static List<AccessTokenMessage> getAccessTokenMsg(String message,String token) {
		AccessTokenMessage accessTokenMessage = new AccessTokenMessage();
		accessTokenMessage.setMessage(message);
		accessTokenMessage.setAccess_token(token);
		List<AccessTokenMessage> commonMessageList = new ArrayList<>();
		commonMessageList.add(accessTokenMessage);
		return commonMessageList;
	}
}

package com.enrich.authn.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.util.encoders.Hex;

public final class CommonUtil {

	public static final String YES = "YES";
	public static final String NO = "NO";
	public static final String Y = "Y";
	public static final String newline = System.getProperty("line.separator");

	// hashing keys
	public static String hashData(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] digest = md.digest(data.getBytes());
		String hashedData = Hex.toHexString(digest);
		return hashedData;
	}

	public static String cleanLog(String template, Object... params) {
		return String.format(template, params);
	}

	// hashing password 1000 iterations
	public static String hashDataPasswordIteration(String data)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] digest = md.digest(data.getBytes());

		for (int i = 1; i <= 999; i++) {
			digest = md.digest(digest);
		}
		String hashedData = Hex.toHexString(digest);
		return hashedData;
	}

	// OTP Generator
	public static String otpGenerator() {
		String numbers = "0123456789";
		String otpVALTag = "";
		Random rndm_method = new Random();
		char[] otp = new char[4];
		for (int i = 0; i < 4; i++) {
			otp[i] = numbers.charAt(rndm_method.nextInt(numbers.length()));
			otpVALTag = otpVALTag + otp[i];
		}
		return otpVALTag;
	}

	public static String DateFormate(String dateFormat) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formatDateTime = now.format(format);
		return dateFormat;

	}

	public static String SystemCurrentDate() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String formatDateTime = now.format(format);
		return formatDateTime;
	}

	public static String SimpleDateFormat(String dateFormat) {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		// DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
		// Locale.ENGLISH);
		Date date = (Date) format.parse(dateFormat);
		return dateFormat;
	}

	public static String urlEncodeUTF8(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(String.format("%s=%s", entry.getKey().toString(), entry.getValue().toString()));
		}
		return sb.toString();
	}

	// Password_kambala
	public static String KambalaUserPasswordData(String data)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String sha256hexStr = DigestUtils.sha256Hex(data);
		return sha256hexStr;
	}

	public static String KambalaSubUserTokenCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		String jKey = "";
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals("usertoken")) {
					jKey = cookies[i].getValue();
				}
			}
		}
		return jKey;
	}

	public static String KambalaUserIdCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		String userId = "";
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals("userId")) {
					userId = cookies[i].getValue();
				}

			}
		}
		return userId;
	}
}

package com.enrich.authn.util;

import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;
import com.enrich.authn.constants.Constants;
import com.enrich.authn.exception.AccessDeniedException;
import com.enrich.authn.pojo.LoginAuthnRequest;
import com.enrich.authn.pojo.LoginRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

		public static String generateJwt(LoginAuthnRequest loginAuthnRequest) {
	
	        long milliTime = System.currentTimeMillis();
	        long expiryTime = milliTime + Constants.EXPIRY_DURATION * 1000;
	        Date currentTime = new Date();
	        Date issuedAt = new Date(milliTime);
	        Date expiryAt = new Date(expiryTime);
			Claims claims =Jwts.claims()
					.setIssuer(Constants.ISSUER_VALUE)
					.setIssuedAt(issuedAt)
					.setExpiration(expiryAt);
			claims.put(Constants.SUBJECT_ID,loginAuthnRequest.getUser_id().toUpperCase() );
			claims.put(Constants.PARTNER_CHANNEL,loginAuthnRequest.getPartner_channel());
			claims.put(Constants.PARTNER_CODE,loginAuthnRequest.getPartner_code() );
			claims.put(Constants.USER_ID,loginAuthnRequest.getUser_id().toUpperCase() );
			claims.put(Constants.LAST_VALIDATED_TIME,currentTime );
			claims.put(Constants.ISSUER_ID,Constants.ISSUER_VALUE );
	
			return Jwts.builder()
					.setClaims(claims).signWith(SignatureAlgorithm.HS256, Constants.SECRET_KEY).compact();
		}
		
		
		
		public static String generateMobile(LoginRequest loginRequest) {
			
	        long milliTime = System.currentTimeMillis();
	        long expiryTime = milliTime + Constants.EXPIRY_DURATION * 1000;
	        Date currentTime = new Date();
	        Date issuedAt = new Date(milliTime);
	        Date expiryAt = new Date(expiryTime);
			Claims claims =Jwts.claims()
					.setIssuer(Constants.ISSUER_VALUE)
					.setIssuedAt(issuedAt)
					.setExpiration(expiryAt);
			claims.put(Constants.SUBJECT_ID,loginRequest.getUser_id().toUpperCase() );
			claims.put(Constants.PARTNER_CHANNEL,loginRequest.getPartner_channel());
			claims.put(Constants.PARTNER_CODE,loginRequest.getPartner_code() );
			claims.put(Constants.USER_ID,loginRequest.getUser_id().toUpperCase() );
			claims.put(Constants.LAST_VALIDATED_TIME,currentTime );
			claims.put(Constants.ISSUER_ID,Constants.ISSUER_VALUE );
	
			return Jwts.builder()
					.setClaims(claims).signWith(SignatureAlgorithm.HS256, Constants.SECRET_KEY).compact();
		}
		public static  String generateNewAccessToken(Claims existingClaims, Long exp) {
			Instant now = Instant.now();
			Date currentTime = new Date();
			Claims newClaims = Jwts.claims(existingClaims);
			String jwtUserId = newClaims.get(Constants.USER_ID, String.class);
			System.out.println("jwtUserId"+jwtUserId);
			newClaims.put(Constants.SUBJECT_ID,jwtUserId.toUpperCase() );
			newClaims.put(Constants.PARTNER_CHANNEL,Constants.CLIENT);
			newClaims.put(Constants.PARTNER_CODE,Constants.ENRICH );
			newClaims.put(Constants.USER_ID,jwtUserId.toUpperCase() );
			newClaims.put(Constants.LAST_VALIDATED_TIME,currentTime );
			newClaims.put(Constants.ISSUER_ID,Constants.ISSUER_VALUE );
			
			Instant expirationInstant = Instant.ofEpochSecond(exp);
			String newAccessToken = Jwts.builder().setClaims(newClaims).setIssuedAt(Date.from(now))
					.setExpiration(Date.from(expirationInstant))
					.signWith(SignatureAlgorithm.HS256, Constants.SECRET_KEY).compact();
			return newAccessToken;	
		}
		

	public static Claims verify(String jwtToken) {
	    try {
	        Claims claims = Jwts.parser().setSigningKey(Constants.SECRET_KEY).parseClaimsJws(jwtToken).getBody();
	        Date expiration = claims.getExpiration();
	        Date currentTime = new Date();
	       /*
	        if (expiration != null && expiration.before(currentTime)) {
	        	
	            throw new ExpiredJwtException(null, claims, Constants.JWT_EXPIRED);
	        }       
	       */
	        return claims;
  
	    } catch (ExpiredJwtException e) {
	        throw new AccessDeniedException(Constants.JWT_EXPIRED);
	    } catch (Exception e) {
	        throw new AccessDeniedException(Constants.ACCESS_DENIED);
	    }
   
	}
}

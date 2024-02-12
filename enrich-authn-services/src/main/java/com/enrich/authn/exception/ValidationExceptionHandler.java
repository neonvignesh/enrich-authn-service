package com.enrich.authn.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.enrich.authn.util.MessageUtil;
import com.enrich.authn.util.StandardResponseUtil;


@ControllerAdvice
public class ValidationExceptionHandler {
	

	  @ExceptionHandler(MethodArgumentNotValidException.class)
	  public ResponseEntity<?> notValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
	    List<String> errors = new ArrayList<>();
	    //MessageUtil.getErrorMessage(ex.getAllErrors());
	    ex.getAllErrors().forEach(err -> errors.add(err.getDefaultMessage()));
	    StandardResponseUtil.prepareBadRequestResponseList(errors);
	    Map<String, List<String>> result = new HashMap<>();
	    result.put("error_message", errors);
	    return new ResponseEntity<>(StandardResponseUtil.prepareBadRequestResponseList(MessageUtil.getErrorMessage(result.toString())), HttpStatus.BAD_REQUEST);
	  }
}

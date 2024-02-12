package com.enrich.authn.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class StandardMessageResponse {
	
    private boolean success;
    private String systemMessage;
    private String systemMessageType;
    
    private List<?> data = new ArrayList<>();

}

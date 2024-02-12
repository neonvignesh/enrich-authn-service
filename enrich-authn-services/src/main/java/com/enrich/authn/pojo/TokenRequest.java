package com.enrich.authn.pojo;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class TokenRequest {
	
	
	@NotNull(message = "Validity must not be null")
    @Pattern(regexp = "^(?!0000$)\\d+$", message = "Invalid validity")
    @Min(value = 1, message = "Validity must be at least 1")
	 private long validity;

}

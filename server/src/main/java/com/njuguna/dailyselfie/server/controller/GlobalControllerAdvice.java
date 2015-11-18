package com.njuguna.dailyselfie.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerAdvice {

	@ResponseBody
	@ExceptionHandler(FeatureNotImplementedException.class)
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	String featureNotImplementedException(FeatureNotImplementedException ex) {
		return ex.getMessage();
	}

}

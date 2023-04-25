package com.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MyErrorController{	
	@GetMapping("/error")
	public String handleError(HttpServletRequest request) {
		return "error";
	}
}

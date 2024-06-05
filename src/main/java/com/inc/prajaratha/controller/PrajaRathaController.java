package com.inc.prajaratha.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrajaRathaController {
	
	@GetMapping("/")
	public String loadHome() {
		return "home";
	}
	
	@GetMapping("/signup")
	public String loadSignup() {
		return "signup";
	}
	
	@GetMapping("/login")
	public String loadlogin() {
		return "login";
	}
}

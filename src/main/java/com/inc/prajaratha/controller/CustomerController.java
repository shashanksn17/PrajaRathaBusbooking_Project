package com.inc.prajaratha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.inc.prajaratha.dto.Customer;
import com.inc.prajaratha.service.CustomerService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {
	
	@Autowired
	Customer customer;
	
	@Autowired
	CustomerService customerService;

	@GetMapping("/signup")
	public String loadSignup(ModelMap map) {
		map.put("customer", customer);
		return "customersignup.html";
	}

	@PostMapping("/signup")
	public String signup(@Valid Customer customer, BindingResult result, HttpSession session) {
		return customerService.signup(customer, result, session);
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam int id, @RequestParam int otp, HttpSession session) {
		return customerService.verifyOtp(id, otp, session);
	}
	
	@GetMapping("/send-otp/{id}")
	public String loadOtpPage(@PathVariable int id, ModelMap map) {
		map.put("id", id);
		return "customer-otp";
	}

	@GetMapping("/resend-otp/{id}")
	public String resendOtp(@PathVariable int id, HttpSession session) {
		return customerService.resendOtp(id, session);
	}

	@GetMapping("/view-bookings")
	public String viewPastBookings(HttpSession session, ModelMap map) {
		return customerService.viewbookings(session, map);
	}
}

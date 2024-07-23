package com.inc.prajaratha.service;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.inc.prajaratha.dao.AgencyDao;
import com.inc.prajaratha.dao.CustomerDao;
import com.inc.prajaratha.dto.Customer;
import com.inc.prajaratha.dto.TripOrder;
import com.inc.prajaratha.helper.AES;
import com.inc.prajaratha.helper.MailSendingHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class CustomerService {
	
	@Autowired
	CustomerDao customerDao;

	@Autowired
	MailSendingHelper mailSendingHelper;

	@Autowired
	AgencyDao agencyDao;

	public String signup(@Valid Customer customer, BindingResult result, HttpSession session) {
		if (!customer.getPassword().equals(customer.getCpassword()))
			result.rejectValue("cpassword", "error.cpassword", "* Password and Confirm Password Should be Matching");
		if (customerDao.checkEmail(customer.getEmail()) || customerDao.checkEmail(customer.getEmail()))
			result.rejectValue("email", "error.email", "* Email Should be Unique");
		if (customerDao.checkMobile(customer.getMobile()) || customerDao.checkMobile(customer.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Mobile Number Should be Unique");

		if (result.hasErrors())
			return "customersignup.html";
		else {
			customerDao.deleteIfExists(customer);
			customer.setCpassword(AES.encrypt(customer.getCpassword(), "123"));
			customer.setPassword(AES.encrypt(customer.getPassword(), "123"));
			customer.setOtp(new Random().nextInt(100000, 1000000));
			System.out.println("Otp - " + customer.getOtp());
			if (mailSendingHelper.sendEmail(customer)) {
				customerDao.save(customer);
				session.setAttribute("successMessage", "Otp Sent Success");
				return "redirect:/customer/send-otp/" + customer.getId() + "";
			} else {
				session.setAttribute("failMessage", "Sorry Not able to send OTP");
				return "redirect:/customer/signup";
			}

		}
	}

	public String verifyOtp(int id, int otp, HttpSession session) {
		Customer customer = customerDao.findById(id);
		if (customer.getOtp() == otp) {
			customer.setStatus(true);
			customerDao.save(customer);
			session.setAttribute("successMessage", "Otp Verified Success, You can Lgin Now");
			return "redirect:/login";
		} else {
			session.setAttribute("failMessage", "Invalid Otp, Try Again");
			return "redirect:/customer/send-otp/" + customer.getId() + "";
		}
	}


	public String resendOtp(int id, HttpSession session) {
		Customer customer = customerDao.findById(id);
		customer.setOtp(new Random().nextInt(100000, 1000000));
		System.out.println("Otp - " + customer.getOtp());
		if (mailSendingHelper.sendEmail(customer)) {
			customerDao.save(customer);
			session.setAttribute("successMessage", "Otp Re-Sent Success");
			return "redirect:/customer/send-otp/" + customer.getId() + "";
		} else {
			session.setAttribute("failMessage", "Failed to Send Otp");
			return "redirect:/customer/send-otp/" + customer.getId() + "";
		}
	}

	
	public String viewbookings(HttpSession session, ModelMap map) {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer == null) {
			session.setAttribute("failMessage", "First Login to Book");
			return "redirect:/login";
		} else {
			List<TripOrder> orders=customer.getTripOrders();
			if(orders.isEmpty()) {
				session.setAttribute("failMessage", "No Bookings Yet");
				return "redirect:/";
			}
			else {
				map.put("orders", orders);
				return "view-bookings.html";
			}
		}
	}

}
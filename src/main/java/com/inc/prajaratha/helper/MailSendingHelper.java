package com.inc.prajaratha.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.inc.prajaratha.dto.Agency;
import com.inc.prajaratha.dto.Customer;

import jakarta.mail.internet.MimeMessage;

@Service
public class MailSendingHelper {

	@Autowired
	JavaMailSender mailSender;

	@Autowired
	TemplateEngine engine;

	public boolean sendEmail(Agency agency) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		try {

			helper.setFrom("snshashank17@gmail.com", "Prajaratha");
			helper.setTo(agency.getEmail());
			helper.setSubject("Account creation OTP");

			Context context = new Context();
			context.setVariable("obj", agency);

			String template = null;
			try {
				template = engine.process("email-template.html", context);
			} catch (Exception e) {
				e.printStackTrace();
			}
			helper.setText(template, true);

			mailSender.send(message);
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public boolean sendEmail(Customer customer) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		try {

			helper.setFrom("snshashank17@gmail.com", "Prajaratha");
			helper.setTo(customer.getEmail());
			helper.setSubject("Account creation OTP");

			Context context = new Context();
			context.setVariable("agency", customer);

			String template = null;
			try {
				template = engine.process("email-template.html", context);
			} catch (Exception e) {
				e.printStackTrace();
			}
			helper.setText(template, true);

			mailSender.send(message);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
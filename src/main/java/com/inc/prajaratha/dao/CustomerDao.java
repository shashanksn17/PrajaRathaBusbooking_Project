package com.inc.prajaratha.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.inc.prajaratha.dto.Customer;
import com.inc.prajaratha.repository.CustomerRepository;

import jakarta.validation.Valid;

@Repository
public class CustomerDao {

	@Autowired
	CustomerRepository customerRepository;

	public boolean checkEmail(String email) {
		return customerRepository.existsByEmailAndStatusTrue(email);
	}

	public boolean checkMobile(long mobile) {
		return customerRepository.existsByMobileAndStatusTrue(mobile);
	}

	public void deleteIfExists(@Valid Customer customer) {
			if (findByMobile(customer.getMobile()) != null) {
				delete(findByMobile(customer.getMobile()));
			}
			if (findByEmail(customer.getEmail()) != null) {
				delete(findByEmail(customer.getEmail()));
			}

		}
	
	public Customer findByEmail(String email) {
		return customerRepository.findByEmail(email);
	}

	public Customer findByMobile(long mobile) {
		return customerRepository.findByMobile(mobile);
	}
	
	public void delete(Customer customer) {
		customerRepository.delete(customer);
	}

	public void save(Customer customer) {
		customerRepository.save(customer);
		
	}


	public Customer findById(int id) {
		return customerRepository.findById(id).orElseThrow();
	}
}
	

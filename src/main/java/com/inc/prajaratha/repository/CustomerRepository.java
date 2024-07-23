package com.inc.prajaratha.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inc.prajaratha.dto.Agency;
import com.inc.prajaratha.dto.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

	Customer findByEmail(String email);

	Customer findByMobile(long mobile);

	boolean existsByEmailAndStatusTrue(String email);

	boolean existsByMobileAndStatusTrue(long mobile);

}

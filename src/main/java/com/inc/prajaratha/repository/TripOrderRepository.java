package com.inc.prajaratha.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inc.prajaratha.dto.TripOrder;

public interface TripOrderRepository extends JpaRepository<TripOrder, Integer> {

}
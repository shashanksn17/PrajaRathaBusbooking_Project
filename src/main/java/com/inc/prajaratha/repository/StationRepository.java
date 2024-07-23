package com.inc.prajaratha.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inc.prajaratha.dto.Station;

public interface StationRepository extends JpaRepository<Station, Integer> {

	List<Station> findByName(String from);

}

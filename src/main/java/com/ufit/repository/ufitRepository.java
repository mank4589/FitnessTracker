package com.ufit.repository;

import com.ufit.model.ufit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ufitRepository extends JpaRepository<ufit, Long> {
}
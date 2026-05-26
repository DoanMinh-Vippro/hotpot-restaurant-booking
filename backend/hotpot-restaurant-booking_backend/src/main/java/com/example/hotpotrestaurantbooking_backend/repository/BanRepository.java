package com.example.hotpotrestaurantbooking_backend.repository;


import com.example.hotpotrestaurantbooking_backend.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanRepository extends JpaRepository<Ban,Integer> {
}
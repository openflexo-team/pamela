package com.example.securingweb.persistence.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.securingweb.persistence.model.User;
import com.example.securingweb.persistence.model.UserLocation;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    UserLocation findByCountryAndUser(String country, User user);

}

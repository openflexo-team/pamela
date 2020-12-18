package com.example.securingweb.persistence.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.securingweb.persistence.model.NewLocationToken;
import com.example.securingweb.persistence.model.UserLocation;

public interface NewLocationTokenRepository extends JpaRepository<NewLocationToken, Long> {

    NewLocationToken findByToken(String token);

    NewLocationToken findByUserLocation(UserLocation userLocation);

}

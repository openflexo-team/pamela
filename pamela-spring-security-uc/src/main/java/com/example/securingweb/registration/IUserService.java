package com.example.securingweb.registration;

import com.example.securingweb.exceptions.UserAlreadyExistException;
import com.example.securingweb.persistence.model.User;

public interface IUserService {
    User registerNewUserAccount(UserDto userDto)
            throws UserAlreadyExistException;
    void saveRegisteredUser(User user);
}
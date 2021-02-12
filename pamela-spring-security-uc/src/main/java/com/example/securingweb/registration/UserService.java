package com.example.securingweb.registration;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.securingweb.exceptions.UserAlreadyExistException;
import com.example.securingweb.persistence.dao.UserRepository;
import com.example.securingweb.persistence.model.Role;
import com.example.securingweb.persistence.model.User;


@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository repository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public User registerNewUserAccount(UserDto userDto)
            throws UserAlreadyExistException {
    

        if (emailExist(userDto.getEmail())) {
            throw new UserAlreadyExistException(
                    "There is an account with that email address: "
                            +  userDto.getEmail());
        }
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setRoles(Arrays.asList(new Role("ROLE_USER")));
        return repository.save(user);
    }
    private boolean emailExist(String email) {
        return repository.findByEmail(email) != null;
    }
    
    @Override
    public void saveRegisteredUser(final User user) {
        repository.save(user);
    }


    @PostMapping("/user/registration")
    public ModelAndView registerUserAccount
            (@ModelAttribute("user") @Valid UserDto userDto,
             HttpServletRequest request, Errors errors) {

        /*try {
            User registered = userService.registerNewUserAccount(userDto);
        } catch (UserAlreadyExistException uaeEx) {
            mav.addObject("message", "An account for that username/email already exists.");
            return mav;
        }*/

        // rest of the implementation
    	
    	return null;
    }
}
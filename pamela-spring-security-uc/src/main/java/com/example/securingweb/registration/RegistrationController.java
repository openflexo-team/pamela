package com.example.securingweb.registration;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.View;
//import org.springframework.web.servlet.view.RedirectView;

import com.example.securingweb.authentication.AuthManagerService;
import com.example.securingweb.authentication.SessionInfo;
//import com.example.securingweb.exceptions.UserAlreadyExistException;
import com.example.securingweb.persistence.model.User;

@Controller
public class RegistrationController {

	@Autowired
	private IUserService userService;

	private String userName;

	public RegistrationController() {
		super();
	}

	@GetMapping("/register")
	public String showRegistrationForm(WebRequest request, Model model) {

		UserDto userDto = new UserDto();
		model.addAttribute("user", userDto);
		return "register";
	}

	@ModelAttribute(value = "myEntity")
	public UserDto newEntity() {
		return new UserDto();
	}

	@PostMapping("/doRegister")
	public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto, HttpServletRequest request,
			Errors errors) {

		userName = userDto.getFirstName();
		User registered = userService.registerNewUserAccount(userDto);
		System.out.println(registered.getFirstName() + " is now registered");
		System.out.println("SESSION_INFO=" + request.getSession().getAttribute(SessionInfo.SESSION_INFO).toString());
		System.out.println("Et aussi: " + SessionInfo.getCurrentSessionInfo());

		return new ModelAndView("successRegister", "user", userDto);
	}

	@RequestMapping(value = "/displayName", method = RequestMethod.GET)
	@ResponseBody
	public String displayNameAfterRegister() {
		return userName;
	}

	@GetMapping("/login")
	public String login(Model model, String error, String logout) {
		if (error != null)
			model.addAttribute("error", "Your username and password is invalid.");

		if (logout != null)
			model.addAttribute("message", "You have been logged out successfully.");

		return "login";
	}

}
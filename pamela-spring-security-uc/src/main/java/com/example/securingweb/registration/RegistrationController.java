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

	@Autowired
	private AuthManagerService authManagerService;

	// @Autowired
	// private Prout prout;

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

	/*
	 * public ModelAndView registerUserAccount(
	 * 
	 * @ModelAttribute("user") @Valid UserDto userDto, HttpServletRequest request,
	 * Errors errors) {
	 * 
	 * }
	 */

	@ModelAttribute(value = "myEntity")
	public UserDto newEntity() {
		return new UserDto();
	}

	/*
	 * @RequestMapping(value = "/doRegister", method = RequestMethod.POST) public
	 * View action(Model model, @ModelAttribute("myEntity") User myEntity) { // save
	 * the entity or do whatever you need
	 * 
	 * return new RedirectView("/user/ranks"); }
	 */
	/*
	 * @RequestMapping(value = "/successRegister", method = RequestMethod.GET)
	 * public ModelAndView getRanks(Model model, HttpServletRequest request) {
	 * String view = "the-view-name"; return new ModelAndView(view, "command",
	 * model); }
	 */

	@PostMapping("/doRegister")
	public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid UserDto userDto, HttpServletRequest request,
			Errors errors) {

		userName = userDto.getFirstName();
		// System.out.println(userService.repository);
		User registered = userService.registerNewUserAccount(userDto);
		System.out.println(registered.getFirstName() + " is now registered");

		// Prout prout = new Prout();
		// Prout prout = authManagerService.getProut();
		// System.out.println("Prout = " + prout + " " + prout.getCreated());

		// request.getSession().setAttribute("PROUT", prout);

		System.out.println("SESSION_INFO=" + request.getSession().getAttribute(SessionInfo.SESSION_INFO).toString());

		System.out.println("Et aussi: " + SessionInfo.getCurrentSessionInfo());

		// prout.message = registered.getFirstName() + " is now registered";

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
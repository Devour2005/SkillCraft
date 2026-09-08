package com.skillcraft.gateway.controller;

import com.skillcraft.gateway.dto.RegisterForm;
import com.skillcraft.gateway.dto.RegisterRequest;
import com.skillcraft.gateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebAuthController {

	private final AuthService authService;

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		if (!model.containsAttribute("form")) {
			model.addAttribute("form", new RegisterForm());
		}
		return "register";
	}

	@PostMapping("/register")
	public String register(@ModelAttribute("form") RegisterForm form, Model model, RedirectAttributes redirectAttributes) {
		if (isBlank(form.getEmail()) || isBlank(form.getFirstName()) || isBlank(form.getLastName())) {
			model.addAttribute("error", "Please fill in all fields");
			return "register";
		}
		if (form.getPassword() == null || form.getPassword().length() < 8) {
			model.addAttribute("error", "Password must be at least 8 characters long");
			return "register";
		}
		if (!form.getPassword().equals(form.getConfirmPassword())) {
			model.addAttribute("error", "Passwords do not match");
			return "register";
		}

		try {
			authService.register(new RegisterRequest(form.getEmail(), form.getPassword(), form.getFirstName(), form.getLastName()));
		} catch (IllegalArgumentException ex) {
			model.addAttribute("error", ex.getMessage());
			return "register";
		}

		redirectAttributes.addFlashAttribute("registered", true);
		return "redirect:/login";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

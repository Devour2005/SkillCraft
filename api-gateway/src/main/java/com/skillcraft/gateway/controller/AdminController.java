package com.skillcraft.gateway.controller;

import com.skillcraft.gateway.domain.UserRole;
import com.skillcraft.gateway.dto.AdminUpdateCourseRequest;
import com.skillcraft.gateway.dto.AdminUpdateUserRequest;
import com.skillcraft.gateway.dto.AdminUserView;
import com.skillcraft.gateway.service.ManagementApiClient;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminController {

	private static final List<UserRole> STAFF_ROLES = List.of(UserRole.MANAGER, UserRole.ACCOUNTANT, UserRole.ADMIN);

	private final ManagementApiClient managementApiClient;

	@GetMapping("/admin")
	public String index() {
		return "redirect:/admin/students";
	}

	@GetMapping("/admin/students")
	public String students(Model model, Authentication authentication) {
		model.addAttribute("tab", "students");
		model.addAttribute("users", managementApiClient.getUsersByRoles(authentication, List.of(UserRole.STUDENT)));
		return "admin";
	}

	@GetMapping("/admin/teachers")
	public String teachers(Model model, Authentication authentication) {
		model.addAttribute("tab", "teachers");
		model.addAttribute("users", managementApiClient.getUsersByRoles(authentication, List.of(UserRole.TEACHER)));
		return "admin";
	}

	@GetMapping("/admin/staff")
	public String staff(Model model, Authentication authentication) {
		model.addAttribute("tab", "staff");
		model.addAttribute("users", managementApiClient.getUsersByRoles(authentication, STAFF_ROLES));
		return "admin";
	}

	@GetMapping("/admin/courses")
	public String courses(Model model, Authentication authentication) {
		model.addAttribute("tab", "courses");
		model.addAttribute("courses", managementApiClient.getAllCourses(authentication));
		return "admin";
	}

	@GetMapping("/admin/users/{id}/edit")
	public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
		AdminUserView user = managementApiClient.getUser(authentication, id);
		model.addAttribute("editUser", user);
		model.addAttribute("tab", tabForRole(user.role()));
		return "admin-user-edit";
	}

	@PostMapping("/admin/users/{id}")
	public String updateUser(@PathVariable Long id,
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam(required = false) String phone,
			@RequestParam(name = "isActive", defaultValue = "false") boolean isActive,
			@RequestParam String returnTab,
			Authentication authentication,
			RedirectAttributes redirectAttributes) {
		try {
			managementApiClient.updateUser(authentication, id, new AdminUpdateUserRequest(firstName, lastName, phone, isActive));
		} catch (RestClientResponseException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getResponseBodyAsString());
		}
		return "redirect:/admin/" + returnTab;
	}

	@GetMapping("/admin/courses/{id}/edit")
	public String editCourseForm(@PathVariable Long id, Model model, Authentication authentication) {
		model.addAttribute("editCourse", managementApiClient.getCourse(authentication, id));
		model.addAttribute("teachers", managementApiClient.getUsersByRoles(authentication, List.of(UserRole.TEACHER)));
		model.addAttribute("tab", "courses");
		return "admin-course-edit";
	}

	@PostMapping("/admin/courses/{id}")
	public String updateCourse(@PathVariable Long id,
			@RequestParam String title,
			@RequestParam(required = false) String description,
			@RequestParam Long teacherId,
			@RequestParam BigDecimal price,
			@RequestParam(name = "isArchived", defaultValue = "false") boolean isArchived,
			Authentication authentication,
			RedirectAttributes redirectAttributes) {
		try {
			managementApiClient.updateCourse(authentication, id, new AdminUpdateCourseRequest(title, description, teacherId, price, isArchived));
		} catch (RestClientResponseException ex) {
			redirectAttributes.addFlashAttribute("error", ex.getResponseBodyAsString());
		}
		return "redirect:/admin/courses";
	}

	private String tabForRole(UserRole role) {
		return switch (role) {
			case STUDENT -> "students";
			case TEACHER -> "teachers";
			case MANAGER, ACCOUNTANT, ADMIN -> "staff";
		};
	}
}

package com.skillcraft.gateway.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * The admin panel is the only thing behind form login, and it's restricted to
 * ADMIN/MANAGER (see WebSecurityConfig). A plain {@code defaultSuccessUrl("/admin")}
 * would authenticate any role and only then bounce them into a raw 403 from
 * /admin's access check. Checking the role here instead lets a non-staff account
 * get a clear message on the login page rather than a Whitelabel error page.
 */
@Component
public class AdminPanelAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		boolean canAccessAdminPanel = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_MANAGER"));

		if (canAccessAdminPanel) {
			response.sendRedirect(request.getContextPath() + "/admin");
			return;
		}

		SecurityContextHolder.clearContext();
		request.getSession().invalidate();
		response.sendRedirect(request.getContextPath() + "/login?denied");
	}
}

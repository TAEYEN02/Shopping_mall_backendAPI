package com.backend.shopping.security;

import java.io.IOException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider tokenProvider;
	private final CustomUserDetailsService userDtailService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)throws ServletException, IOException{
		
		String jwt = getJwtFromRequest(request);
		
		if(StringUtils.hasText(jwt)&&tokenProvider.validateToken(jwt)) {
			String username = tokenProvider.getUsernameFromToken(jwt);
			
			UserDetails userDetails = userDtailService.
		}

	}

}

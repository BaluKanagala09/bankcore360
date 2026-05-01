package org.cts.transactionservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads trusted HTTP headers injected by the API Gateway after JWT validation.
 * Builds the SecurityContext principal (AuthenticatedUser) from these headers.
 *
 * Expected headers (set by API Gateway):
 *   X-User-Id    -> userId (Long)
 *   X-Account-Id -> accountId (Long) — the customer's primary account
 *   X-Branch-Id  -> branchId (Long)
 *   X-Roles      -> comma-separated roles e.g. "CUSTOMER" or "BRANCH_MANAGER,ADMIN"
 */
@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userIdHeader   = request.getHeader("X-User-Id");
        String accountIdHeader = request.getHeader("X-Account-Id");
        String branchIdHeader  = request.getHeader("X-Branch-Id");
        String rolesHeader     = request.getHeader("X-Roles");

        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(rolesHeader)) {
            try {
                Long userId    = Long.parseLong(userIdHeader);
                Long accountId = StringUtils.hasText(accountIdHeader) ? Long.parseLong(accountIdHeader) : null;
                Long branchId  = StringUtils.hasText(branchIdHeader)  ? Long.parseLong(branchIdHeader)  : null;

                List<String> roles = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                AuthenticatedUser principal = new AuthenticatedUser(userId, accountId, branchId, roles);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (NumberFormatException e) {
                // Invalid headers — treat as unauthenticated
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}


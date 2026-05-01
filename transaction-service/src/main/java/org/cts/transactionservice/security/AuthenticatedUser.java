package org.cts.transactionservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Represents the authenticated principal extracted from gateway-injected HTTP headers.
 * This object is stored as the principal in the SecurityContext.
 */
@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private final Long userId;
    private final Long accountId;
    private final Long branchId;
    private final List<String> roles;
}


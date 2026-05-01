package org.cts.transactionservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class BranchAccessDeniedException extends RuntimeException {
    public BranchAccessDeniedException(String message) {
        super(message);
    }
}


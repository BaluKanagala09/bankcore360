package org.cts.accountservice.exception;

public class KycNotApprovedException extends RuntimeException{
    public KycNotApprovedException(String message) {
        super(message);
    }

    public KycNotApprovedException() {
        super("KYC is not approved.Please complete your KYC verification before proceeding.");
    }
}

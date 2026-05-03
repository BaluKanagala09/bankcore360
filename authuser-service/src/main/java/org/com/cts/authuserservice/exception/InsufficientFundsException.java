package org.com.cts.authuserservice.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
    public InsufficientFundsException(Double available,Double required){
        super(String.format("Insufficient funds.Available: %.2f,Required: %.2f",available,required));
    }

}

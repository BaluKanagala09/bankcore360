package org.cts.transactionservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.cts.transactionservice.exception.AccountNotFoundException;
import org.cts.transactionservice.exception.BranchNotFoundException;

// A custom error decoder to handle feign errors gracefully
// Error Decoder is used to convert HTTP errors to exceptions
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        if(response.status() == 404){
            if(methodKey.contains("Account")){
                return new AccountNotFoundException("Account not found from Account Service");
            }
            else if(methodKey.contains("Branch")){
                return new BranchNotFoundException("Branch not found from Branch Service");
            }
        }

        if(response.status() == 500){
            return new RuntimeException("Internal Server Error from external service");
        }
        return new RuntimeException("Error calling external service: "+ response.reason());

    }
}

package org.cts.accountservice.controller;


import lombok.RequiredArgsConstructor;
import org.cts.accountservice.dto.*;
import org.cts.accountservice.service.AccountService;
import org.cts.accountservice.utils.ApiResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> openAccount(
            @RequestBody AccountRequest request,
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created",
                        accountService.openAccount(request, email)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Success",
                        accountService.findById(id))
        );
    }
}
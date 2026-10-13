package com.servicesphere.customer;

import com.servicesphere.common.ApiResponse;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.customer.dto.AccountCreateRequest;
import com.servicesphere.customer.dto.AccountDto;
import com.servicesphere.customer.dto.ContactDto;
import com.servicesphere.customer.dto.Customer360Dto;
import com.servicesphere.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Customer Management", description = "CRM, accounts, contacts, and Customer 360")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "List customer contacts (Staff only)")
    public ResponseEntity<ApiResponse<PagedResponse<ContactDto>>> getCustomers(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<ContactDto> contacts = customerService.getContacts(accountId, query, page, size);
        return ResponseEntity.ok(ApiResponse.success(contacts));
    }

    @GetMapping("/customers/{id}/360")
    @Operation(summary = "Get Customer 360 unified overview")
    public ResponseEntity<ApiResponse<Customer360Dto>> getCustomer360(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Customer360Dto dto = customerService.getCustomer360(id, principal);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "List company accounts (Staff only)")
    public ResponseEntity<ApiResponse<PagedResponse<AccountDto>>> getAccounts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<AccountDto> accounts = customerService.getAccounts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping("/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Create a company account (Staff only)")
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        AccountDto account = customerService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Account created", account));
    }
}

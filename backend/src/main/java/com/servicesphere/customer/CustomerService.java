package com.servicesphere.customer;

import com.servicesphere.cases.CaseRepository;
import com.servicesphere.cases.CaseStatus;
import com.servicesphere.cases.SupportCase;
import com.servicesphere.cases.dto.CaseDto;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.customer.dto.AccountCreateRequest;
import com.servicesphere.customer.dto.AccountDto;
import com.servicesphere.customer.dto.ContactDto;
import com.servicesphere.customer.dto.Customer360Dto;
import com.servicesphere.security.Role;
import com.servicesphere.security.UserPrincipal;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;

    public CustomerService(
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            UserRepository userRepository,
            CaseRepository caseRepository
    ) {
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.caseRepository = caseRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AccountDto> getAccounts(String query, int page, int size) {
        Page<Account> accounts = accountRepository.searchAccounts(query, PageRequest.of(page, size, Sort.by("name")));
        return PagedResponse.of(accounts.map(AccountDto::fromEntity));
    }

    @Transactional
    public AccountDto createAccount(AccountCreateRequest request) {
        Account account = new Account(request.getName().trim(), request.getIndustry(), request.getTier());
        account.setWebsite(request.getWebsite());
        account.setPhone(request.getPhone());
        return AccountDto.fromEntity(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ContactDto> getContacts(Long accountId, String query, int page, int size) {
        Page<Contact> contacts = contactRepository.searchContacts(accountId, query, PageRequest.of(page, size, Sort.by("id")));
        return PagedResponse.of(contacts.map(ContactDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public Customer360Dto getCustomer360(Long customerId, UserPrincipal principal) {
        // Enforce customer ownership boundary
        if (principal.getRole() == Role.CUSTOMER && !principal.getId().equals(customerId)) {
            throw new AccessDeniedException("You do not have permission to view other customer records");
        }

        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Optional<Contact> contactOpt = contactRepository.findByUserId(customerId);

        List<SupportCase> allCases = caseRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        long totalCases = allCases.size();
        long openCases = allCases.stream()
                .filter(c -> c.getStatus() == CaseStatus.OPEN || c.getStatus() == CaseStatus.IN_PROGRESS || c.getStatus() == CaseStatus.WAITING_FOR_CUSTOMER)
                .count();
        long resolvedCases = allCases.stream()
                .filter(c -> c.getStatus() == CaseStatus.RESOLVED || c.getStatus() == CaseStatus.CLOSED)
                .count();

        long withinSlaCount = allCases.stream()
                .filter(c -> !c.isSlaEscalated())
                .count();

        double slaCompliance = totalCases > 0 ? ((double) withinSlaCount / totalCases) * 100.0 : 100.0;

        Customer360Dto dto = new Customer360Dto();
        dto.setCustomerId(user.getId());
        dto.setName(user.getFullName());
        dto.setEmail(user.getEmail());

        if (contactOpt.isPresent()) {
            Contact contact = contactOpt.get();
            dto.setPhone(contact.getPhone());
            dto.setJobTitle(contact.getJobTitle());
            dto.setAccount(AccountDto.fromEntity(contact.getAccount()));
        }

        dto.setTotalCases(totalCases);
        dto.setOpenCases(openCases);
        dto.setResolvedCases(resolvedCases);
        dto.setSlaCompliancePercentage(Math.round(slaCompliance * 10.0) / 10.0);
        dto.setRecentCases(allCases.stream().limit(10).map(CaseDto::fromEntity).collect(Collectors.toList()));

        return dto;
    }
}

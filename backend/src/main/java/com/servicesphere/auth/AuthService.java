package com.servicesphere.auth;

import com.servicesphere.auth.dto.AuthResponse;
import com.servicesphere.auth.dto.LoginRequest;
import com.servicesphere.auth.dto.RegisterRequest;
import com.servicesphere.auth.dto.UserResponse;
import com.servicesphere.common.exception.BadRequestException;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.customer.Account;
import com.servicesphere.customer.AccountRepository;
import com.servicesphere.customer.Contact;
import com.servicesphere.customer.ContactRepository;
import com.servicesphere.security.JwtTokenProvider;
import com.servicesphere.security.Role;
import com.servicesphere.security.UserPrincipal;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String identifier = loginRequest.getEmail().trim().toLowerCase();
        if ("admin".equals(identifier) || "vivek".equals(identifier)) {
            identifier = "admin@servicesphere.io";
        } else if ("agent".equals(identifier) || "sarah".equals(identifier)) {
            identifier = "agent.sarah@servicesphere.io";
        } else if ("customer".equals(identifier) || "john".equals(identifier) || "john.doe@acme.com".equals(identifier)) {
            identifier = "john@acme.com";
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        identifier,
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return new AuthResponse(
                token,
                principal.getId(),
                principal.getEmail(),
                principal.getFirstName(),
                principal.getLastName(),
                principal.getRole()
        );
    }

    @Transactional
    public AuthResponse registerCustomer(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email address already exists");
        }

        // Strict role enforcement: Public self-registration ALWAYS creates CUSTOMER role
        User user = new User(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName().trim(),
                request.getLastName().trim(),
                Role.CUSTOMER
        );
        user = userRepository.save(user);

        // Manage contact and company account association if provided
        Account account = null;
        if (StringUtils.hasText(request.getCompanyName())) {
            String company = request.getCompanyName().trim();
            account = accountRepository.findByName(company)
                    .orElseGet(() -> accountRepository.save(new Account(company, "General", "STANDARD")));
        }

        Contact contact = new Contact(user, account, request.getJobTitle(), request.getPhone());
        contactRepository.save(contact);

        // Generate immediate authenticated session
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));
        return UserResponse.fromUser(user);
    }
}

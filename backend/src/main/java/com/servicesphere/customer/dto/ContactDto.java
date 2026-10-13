package com.servicesphere.customer.dto;

import com.servicesphere.customer.Contact;
import java.time.Instant;

public class ContactDto {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Long accountId;
    private String accountName;
    private String jobTitle;
    private String phone;
    private Instant createdAt;

    public ContactDto() {}

    public static ContactDto fromEntity(Contact contact) {
        ContactDto dto = new ContactDto();
        dto.setId(contact.getId());
        if (contact.getUser() != null) {
            dto.setUserId(contact.getUser().getId());
            dto.setEmail(contact.getUser().getEmail());
            dto.setFirstName(contact.getUser().getFirstName());
            dto.setLastName(contact.getUser().getLastName());
            dto.setFullName(contact.getUser().getFullName());
        }
        if (contact.getAccount() != null) {
            dto.setAccountId(contact.getAccount().getId());
            dto.setAccountName(contact.getAccount().getName());
        }
        dto.setJobTitle(contact.getJobTitle());
        dto.setPhone(contact.getPhone());
        dto.setCreatedAt(contact.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

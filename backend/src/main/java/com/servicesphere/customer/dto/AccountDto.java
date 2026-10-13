package com.servicesphere.customer.dto;

import com.servicesphere.customer.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class AccountDto {
    private Long id;
    private String name;
    private String industry;
    private String tier;
    private String website;
    private String phone;
    private Instant createdAt;

    public AccountDto() {}

    public static AccountDto fromEntity(Account account) {
        if (account == null) return null;
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setIndustry(account.getIndustry());
        dto.setTier(account.getTier());
        dto.setWebsite(account.getWebsite());
        dto.setPhone(account.getPhone());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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

package com.servicesphere.security;

public enum Role {
    ADMIN,
    AGENT,
    CUSTOMER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}

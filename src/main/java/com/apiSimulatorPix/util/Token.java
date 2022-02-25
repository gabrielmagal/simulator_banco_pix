package com.apiSimulatorPix.util;

import javax.enterprise.context.ApplicationScoped;

import org.keycloak.KeycloakSecurityContext;

@ApplicationScoped
public class Token {

    private String userName;

    public Token (KeycloakSecurityContext context) {
        this.userName = context.getToken().getPreferredUsername();
    }

    public String getToken() {
        return userName;
    }
}

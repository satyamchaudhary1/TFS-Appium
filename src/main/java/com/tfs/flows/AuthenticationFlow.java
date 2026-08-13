package com.tfs.flows;

import com.tfs.config.ConfigManager;
import com.tfs.pages.AuthPage;

public class AuthenticationFlow {
    private final AuthPage authPage = new AuthPage();

    public void loginAsStandardUser() {
        authPage.enterEmail(ConfigManager.require("standard.user.email"));
        authPage.enterPassword(ConfigManager.require("standard.user.password"));
        authPage.tapLogin();
    }
}

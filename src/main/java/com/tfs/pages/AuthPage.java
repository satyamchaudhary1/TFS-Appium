package com.tfs.pages;

public class AuthPage extends BasePage {
    public boolean isLoginScreenDisplayed() { return isDisplayed("login.screen"); }
    public void enterEmail(String email) { type("login.email", email); }
    public void enterPassword(String password) { type("login.password", password); }
    public void tapLogin() { tap("login.submit"); }
}

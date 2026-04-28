package com.les.jakebooks.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * LoginPage - Represents the login form at /login
 * Handles email/password authentication via Spring Security
 */
public class LoginPage extends BasePage {
    private static final By EMAIL_INPUT = By.id("email");
    private static final By SENHA_INPUT = By.id("senha");
    private static final By LOGIN_BUTTON = By.className("btn-login");
    private static final By ERROR_ALERT = By.className("alert-danger");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Fills email and password fields and submits login form.
     *
     * @param email customer email
     * @param senha customer password
     */
    public void login(String email, String senha) {
        safeType(EMAIL_INPUT, email);
        safeType(SENHA_INPUT, senha);
        safeClick(LOGIN_BUTTON);
    }

    /**
     * Returns true if error message is displayed (login failed).
     */
    public boolean isErrorMessageDisplayed() {
        return isElementDisplayed(ERROR_ALERT);
    }

    /**
     * Gets the error message text.
     */
    public String getErrorMessage() {
        return getText(ERROR_ALERT);
    }

    /**
     * Verifies that we are on the login page.
     */
    public boolean isOnLoginPage() {
        return getCurrentUrl().contains("/login");
    }
}

package com.les.jakebooks.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * CheckoutPage - Represents the checkout form at /checkout
 * Handles address selection, payment method selection, and order finalization
 */
public class CheckoutPage extends BasePage {
    // Address selection
    private static final By ADDRESS_RADIOS = By.name("enderecoId");
    private static final By ADDRESS_CARD = By.className("card");

    // Payment tabs
    private static final By COUPON_TAB = By.id("cupom-tab");
    private static final By CARDS_TAB = By.id("cartoes-tab");
    private static final By COUPON_INPUT = By.id("codigoCupom");

    // Card payment inputs
    private static final By CARD_VALUE_INPUTS = By.className("valorCartao");
    private static final By PAGINATION_JSON_INPUT = By.id("pagamentoJson");

    // Terms and submit
    private static final By TERMS_CHECKBOX = By.id("concordo");
    private static final By FINALIZE_BUTTON = By.id("btnFinalizarCompra");
    private static final By CHECKOUT_FORM = By.id("formCheckout");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the checkout page to fully load.
     */
    public void waitForPageLoad() {
        waitForPresence(CHECKOUT_FORM);
    }

    /**
     * Selects the first available address.
     */
    public void selectFirstAddress() {
        List<WebElement> radios = driver.findElements(ADDRESS_RADIOS);
        if (!radios.isEmpty()) {
            WebElement firstRadio = radios.get(0);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView(true);", firstRadio);
            firstRadio.click();
        }
    }

    /**
     * Selects an address by its display name.
     * Example: "Casa" or "Trabalho"
     */
    public void selectAddressByName(String addressName) {
        List<WebElement> labels = driver.findElements(By.tagName("label"));
        for (WebElement label : labels) {
            if (label.getText().contains(addressName)) {
                WebElement radio = label.findElement(By.name("enderecoId"));
                radio.click();
                return;
            }
        }
    }

    /**
     * Gets the list of available addresses.
     */
    public List<WebElement> getAvailableAddresses() {
        return driver.findElements(ADDRESS_RADIOS);
    }

    /**
     * Checks if address section has error (no addresses available).
     */
    public boolean hasAddressError() {
        return driver.findElements(By.xpath("//div[contains(text(), 'não tem endereços')]")).size() > 0;
    }

    /**
     * Switches to the coupon tab and enters a coupon code.
     */
    public void applyCoupon(String couponCode) {
        WebElement couponTab = driver.findElement(COUPON_TAB);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", couponTab);
        safeClick(COUPON_TAB);
        safeType(COUPON_INPUT, couponCode);
    }

    /**
     * Switches to the cards tab.
     */
    public void switchToCardsTab() {
        // Scroll tab into view before clicking
        WebElement cardTab = driver.findElement(CARDS_TAB);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", cardTab);
        safeClick(CARDS_TAB);
        waitForPresence(CARD_VALUE_INPUTS);
    }

    /**
     * Gets the number of available payment cards.
     */
    public int getAvailableCardsCount() {
        return driver.findElements(CARD_VALUE_INPUTS).size();
    }

    /**
     * Enters payment amount for a specific card (by index).
     * Example: enterCardPayment(0, "150.00")
     */
    public void enterCardPayment(int cardIndex, String amount) {
        List<WebElement> cardInputs = driver.findElements(CARD_VALUE_INPUTS);
        if (cardIndex < cardInputs.size()) {
            WebElement input = cardInputs.get(cardIndex);
            input.clear();
            input.sendKeys(amount);

            // Trigger the onchange event to update pagamentoJson
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", input);

            // Also explicitly call calcularTotal() to ensure pagamentoJson is updated
            js.executeScript("if (typeof calcularTotal === 'function') { calcularTotal(); }");
        }
    }

    /**
     * Checks if there's an error about missing cards.
     */
    public boolean hasCardsError() {
        return driver.findElements(By.xpath("//div[contains(text(), 'não tem cartões')]")).size() > 0;
    }

    /**
     * Accepts the terms and conditions checkbox.
     */
    public void acceptTermsAndConditions() {
        WebElement checkbox = waitForElement(TERMS_CHECKBOX);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    /**
     * Gets the final order total from the summary section.
     */
    public String getFinalTotal() {
        By totalLocator = By.id("totalFinal");
        return getText(totalLocator);
    }

    /**
     * Checks if finalize button is enabled.
     */
    public boolean isFinalizarButtonEnabled() {
        return waitForElement(FINALIZE_BUTTON).isEnabled();
    }

    /**
     * Finalizes the order by submitting the checkout form.
     */
    public void finalizeOrder() {
        // Ensure JavaScript execution for validation
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Submit the form
        safeClick(FINALIZE_BUTTON);

        // Wait for redirect to pedidos or error page
        wait.until(ExpectedConditions.urlContains("/pedidos"));
    }

    /**
     * Verifies we are on the checkout page.
     */
    public boolean isOnCheckoutPage() {
        return getCurrentUrl().contains("/checkout");
    }

    /**
     * Gets error message if displayed.
     */
    public String getErrorMessage() {
        try {
            By errorLocator = By.className("alert-danger");
            return getText(errorLocator);
        } catch (Exception e) {
            return "";
        }
    }
}

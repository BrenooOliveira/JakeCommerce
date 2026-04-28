package com.les.jakebooks.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * OrderDetailsPage - Represents the order confirmation page at /pedidos/{id}
 * Verifies successful order creation and payment approval
 */
public class OrderDetailsPage extends BasePage {
    private static final By ORDER_ID_TITLE = By.tagName("h1");
    private static final By STATUS_BADGE = By.className("badge");
    private static final By ORDER_TOTAL = By.xpath("//strong[contains(text(), 'Total')]");
    private static final By SUCCESS_MESSAGE = By.className("alert-success");

    public OrderDetailsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the order details page to load.
     */
    public void waitForPageLoad() {
        waitForPresence(ORDER_ID_TITLE);
    }

    /**
     * Gets the order ID from the page title.
     */
    public String getOrderId() {
        String url = getCurrentUrl();
        // Extract ID from URL like /pedidos/123
        String[] parts = url.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return "";
    }

    /**
     * Gets the order status (should be "EM PROCESSAMENTO" for successful orders).
     */
    public String getOrderStatus() {
        try {
            return getText(STATUS_BADGE);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Verifies the order status is "EM_PROCESSAMENTO".
     */
    public boolean isOrderProcessing() {
        String status = getOrderStatus();
        return status.contains("EM_PROCESSAMENTO") || status.contains("EM PROCESSAMENTO");
    }

    /**
     * Gets the order total value.
     */
    public String getOrderTotal() {
        try {
            return getText(ORDER_TOTAL);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Checks if success message is displayed.
     */
    public boolean isSuccessMessageDisplayed() {
        return isElementDisplayed(SUCCESS_MESSAGE);
    }

    /**
     * Gets the success message text.
     */
    public String getSuccessMessage() {
        try {
            return getText(SUCCESS_MESSAGE);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Verifies we are on the order details page.
     */
    public boolean isOnOrderDetailsPage() {
        return getCurrentUrl().contains("/pedidos/");
    }
}

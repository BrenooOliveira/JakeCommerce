package com.les.jakebooks.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * CarrinhoPage - Represents the cart view at /carrinho
 * Handles viewing cart items, modifying quantities, and proceeding to checkout
 *
 * Note: Cart items are displayed in a TABLE layout
 */
public class CarrinhoPage extends BasePage {
    private static final By CART_TABLE = By.tagName("table");
    private static final By CART_ITEMS_ROWS = By.cssSelector("table tbody tr:not(:has(td[colspan]))");
    // Button with class btn-success and onclick containing /checkout
    private static final By CHECKOUT_BUTTON = By.cssSelector("button.btn-success");
    private static final By SUCCESS_MESSAGE = By.className("alert-success");
    private static final By EMPTY_CART_MESSAGE = By.xpath("//p[contains(text(), 'carrinho está vazio')]");

    public CarrinhoPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Waits for the cart page to fully load.
     */
    public void waitForPageLoad() {
        waitForPresence(CART_TABLE);
    }

    /**
     * Returns true if at least one item is in the cart.
     * Checks if there are any TR elements with actual cart items (excluding empty state).
     */
    public boolean hasCartItems() {
        try {
            // Get all rows in tbody
            java.util.List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));

            // If there's only 1 row with colspan=5, cart is empty
            if (rows.size() == 1) {
                WebElement singleRow = rows.get(0);
                String colspan = singleRow.getAttribute("colspan");
                return colspan == null || colspan.isEmpty();
            }

            // If more than one row, then we have items
            return rows.size() > 1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the number of items currently in the cart.
     */
    public int getCartItemCount() {
        try {
            java.util.List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            // Filter out empty state row
            return (int) rows.stream()
                    .filter(row -> row.getAttribute("colspan") == null)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Gets the title of the first item in the cart.
     */
    public String getFirstItemTitle() {
        try {
            WebElement firstRow = driver.findElement(By.cssSelector("table tbody tr:not(:has(td[colspan]))"));
            // Title is in a <p> tag with class "fw-bold" inside the first <td>
            WebElement titleElement = firstRow.findElement(By.cssSelector("p.fw-bold"));
            return titleElement.getText();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Gets the subtotal value from the cart item row.
     */
    public String getFirstItemSubtotal() {
        try {
            WebElement firstRow = driver.findElement(By.cssSelector("table tbody tr:not(:has(td[colspan]))"));
            java.util.List<WebElement> cells = firstRow.findElements(By.tagName("td"));
            // Last td contains the subtotal (before the remove button column)
            if (cells.size() >= 4) {
                return cells.get(3).getText(); // Fourth column is subtotal
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    /**
     * Proceeds to checkout page.
     */
    public void proceedToCheckout() {
        safeClick(CHECKOUT_BUTTON);
        waitForUrlContains("/checkout");
    }

    /**
     * Returns true if success message is displayed (item added).
     */
    public boolean isSuccessMessageDisplayed() {
        return isElementDisplayed(SUCCESS_MESSAGE);
    }

    /**
     * Waits to ensure we're on the cart view page.
     */
    public boolean isOnCarrinhoPage() {
        return getCurrentUrl().contains("/carrinho");
    }
}


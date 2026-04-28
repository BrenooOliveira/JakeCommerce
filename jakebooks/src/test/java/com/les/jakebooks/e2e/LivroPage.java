package com.les.jakebooks.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * LivroPage - Represents the book listing page at /livros
 * Handles searching for books and adding them to cart
 *
 * Note: Books are displayed in a TABLE layout, not cards
 */
public class LivroPage extends BasePage {
    // Book table and rows
    private static final By BOOKS_TABLE = By.className("table");
    private static final By TABLE_ROWS = By.cssSelector("table tbody tr");

    // Add to cart buttons
    private static final By ADD_TO_CART_BUTTON = By.cssSelector(".btn-outline-success[title='Adicionar ao carrinho']");

    public LivroPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigates to the books page.
     */
    public void navigateToBooks(String baseUrl) {
        navigateTo(baseUrl + "/livros");
        waitForPageLoad();
    }

    /**
     * Waits for the books page to load.
     */
    public void waitForPageLoad() {
        // Wait for the books table to be visible
        waitForPresence(BOOKS_TABLE);
    }

    /**
     * Gets the number of books currently displayed in the table.
     * Excludes the empty state message row if present.
     */
    public int getVisibleBooksCount() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        // Filter out empty state row (which has colspan="6")
        return (int) rows.stream()
                .filter(row -> row.findElements(By.xpath(".//td[@colspan]")).isEmpty())
                .count();
    }

    /**
     * Gets the title of the first book in the table.
     * Title is in the second column (index 1) of the first row.
     */
    public String getFirstBookTitle() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (!rows.isEmpty()) {
            // Get all td elements from first row
            List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
            if (cells.size() > 1) {
                return cells.get(1).getText();  // Title is in second column
            }
        }
        return "";
    }

    /**
     * Gets the price of the first book in the table.
     * Price is in the fourth column (index 3) of the first row.
     */
    public String getFirstBookPrice() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (!rows.isEmpty()) {
            List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
            if (cells.size() > 3) {
                // Extract price text from badge element in fourth column
                WebElement priceCell = cells.get(3);
                return priceCell.getText();
            }
        }
        return "";
    }

    /**
     * Finds a book by title and returns true if found.
     */
    public boolean findBookByTitle(String title) {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        for (WebElement row : rows) {
            try {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() > 1) {
                    String cellTitle = cells.get(1).getText();
                    if (cellTitle.contains(title)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // Continue searching
            }
        }
        return false;
    }

    /**
     * Adds the first book to cart by clicking the add to cart button in the first row.
     */
    public void addFirstBookToCart() {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        if (!rows.isEmpty()) {
            WebElement firstRow = rows.get(0);
            try {
                // Find the add to cart button in the first row
                WebElement addButton = firstRow.findElement(By.cssSelector("button[title='Adicionar ao carrinho']"));

                // Scroll to button if needed
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView(true);", addButton);

                addButton.click();
            } catch (Exception e) {
                // Try alternative selector
                WebElement addButton = firstRow.findElement(By.cssSelector("button.btn-outline-success"));
                JavascriptExecutor js = (JavascriptExecutor) driver;
                js.executeScript("arguments[0].scrollIntoView(true);", addButton);
                addButton.click();
            }
        }
    }

    /**
     * Adds a specific book to cart by finding it by title.
     */
    public void addBookToCartByTitle(String title) {
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        for (WebElement row : rows) {
            try {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() > 1) {
                    String cellTitle = cells.get(1).getText();
                    if (cellTitle.contains(title)) {
                        WebElement addButton = row.findElement(By.cssSelector("button[title='Adicionar ao carrinho']"));
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("arguments[0].scrollIntoView(true);", addButton);
                        addButton.click();
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue searching
            }
        }
    }

    /**
     * Searches for a book by title using the filter form.
     */
    public void searchBook(String searchTerm) {
        By searchInput = By.id("titulo");
        safeType(searchInput, searchTerm);
        // Submit search - find and click the search button
        By searchButton = By.xpath("//button[contains(text(), 'Buscar')]");
        safeClick(searchButton);
    }
}


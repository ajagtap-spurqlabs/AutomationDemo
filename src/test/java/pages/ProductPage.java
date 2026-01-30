package pages;

import com.microsoft.playwright.Page;

public class ProductPage {

    private Page page;

    public ProductPage(Page page) {
        this.page = page;
    }

    public void addToCart() {
        // Register a one-time dialog handler before clicking so the dialog is accepted reliably
        page.onceDialog(dialog -> dialog.accept());
        page.locator("//a[text()='Add to cart']").first().click();
        // Give time for dialog to be displayed and handled
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }

    // kept for backward compatibility but prefer addToCart() which handles dialog
    public void acceptAlert() {
        try {
            page.onDialog(dialog -> dialog.accept());
        } catch (Exception ignored) {}
    }
}

package pages;

import com.microsoft.playwright.Page;

public class CartPage {

    private Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    // Check if product exists in cart
    public boolean isProductPresent() {
        page.waitForSelector("#tbodyid tr");
        return page.locator("#tbodyid tr").count() > 0;
    }

    // Delete product from cart
    public void deleteProduct() {
        page.locator("//a[text()='Delete']").first().click();
        page.waitForTimeout(2000);
    }

    // Check if cart is empty
    public boolean isCartEmpty() {
        return page.locator("#tbodyid tr").count() == 0;
    }
}

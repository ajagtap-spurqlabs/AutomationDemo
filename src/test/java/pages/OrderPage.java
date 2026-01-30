package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.ScreenshotOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class OrderPage {

    private final Page page;

    // Locators
    private final String placeOrderBtn = "//button[text()='Place Order']";
    private final String placeOrderAlt = "button[data-target='#orderModal']";
    private final String placeOrderText = "text=Place Order";
    private final String name = "#name";
    private final String country = "#country";
    private final String city = "#city";
    private final String card = "#card";
    private final String month = "#month";
    private final String year = "#year";
    private final String purchaseBtn = "//button[text()='Purchase']";
    private final String confirmationPopup = ".sweet-alert";
    private final String okBtn = "//button[text()='OK']";

    public OrderPage(Page page) {
        this.page = page;
    }

    public void clickPlaceOrder() {
        // Try multiple selectors to click the Place Order button reliably
        try {
            if (page.locator(placeOrderBtn).count() > 0) {
                page.locator(placeOrderBtn).first().click();
                return;
            }
        } catch (Exception e) {
            System.out.println("clickPlaceOrder: primary selector failed: " + e.getMessage());
        }

        try {
            if (page.locator(placeOrderAlt).count() > 0) {
                page.locator(placeOrderAlt).first().click();
                return;
            }
        } catch (Exception e) {
            System.out.println("clickPlaceOrder: alternate selector failed: " + e.getMessage());
        }

        try {
            page.click(placeOrderText);
        } catch (Exception e) {
            System.out.println("clickPlaceOrder: text selector failed: " + e.getMessage());
        }
    }

    public void fillOrderForm(
            String userName,
            String userCountry,
            String userCity,
            String cardNumber,
            String expMonth,
            String expYear
    ) {
        // Wait for the name field in the modal to appear before filling
        try {
            page.waitForSelector(name, new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            System.out.println("fillOrderForm: name field not visible within timeout: " + e.getMessage());
        }

        try { page.fill(name, userName); } catch (Exception e) { System.out.println("fillOrderForm: name fill: " + e.getMessage()); }
        try { page.fill(country, userCountry); } catch (Exception e) { System.out.println("fillOrderForm: country fill: " + e.getMessage()); }
        try { page.fill(city, userCity); } catch (Exception e) { System.out.println("fillOrderForm: city fill: " + e.getMessage()); }
        try { page.fill(card, cardNumber); } catch (Exception e) { System.out.println("fillOrderForm: card fill: " + e.getMessage()); }
        try { page.fill(month, expMonth); } catch (Exception e) { System.out.println("fillOrderForm: month fill: " + e.getMessage()); }
        try { page.fill(year, expYear); } catch (Exception e) { System.out.println("fillOrderForm: year fill: " + e.getMessage()); }
    }

    public void clickPurchase() {
        // Click Purchase and wait for any of the known confirmation dialog selectors
        try {
            page.locator(purchaseBtn).click();
        } catch (Exception e) {
            System.out.println("clickPurchase: clicking purchase failed: " + e.getMessage());
        }
        String combinedSelectors = ".sweet-alert, .swal2-container, div[role='dialog']";
        try {
            page.waitForSelector(combinedSelectors, new Page.WaitForSelectorOptions().setTimeout(7000));
        } catch (Exception ex) {
            // last resort: brief sleep to allow UI to settle
            System.out.println("clickPurchase: confirmation selector not found within timeout: " + ex.getMessage());
            try { Thread.sleep(700); } catch (InterruptedException ie) { System.out.println("clickPurchase: sleep interrupted: " + ie.getMessage()); }
        }
    }

    public boolean isOrderSuccessful() {
        try {
            // Reuse getConfirmationText for robust detection
            String text = getConfirmationText();
            return text != null && !text.trim().isEmpty();
        } catch (Exception e) {
            System.out.println("isOrderSuccessful: error while detecting confirmation: " + e.getMessage());
            return false;
        }
    }

    public String getConfirmationText() {
        String[] selectors = new String[]{".sweet-alert h2", ".swal2-title", ".swal-title", "div[role='dialog'] h2", ".sweet-alert", ".swal2-container"};
        for (String selector : selectors) {
            try {
                if (page.locator(selector).count() > 0) {
                    String text = page.locator(selector).first().textContent();
                    if (text != null && !text.trim().isEmpty()) {
                        return text.trim();
                    }
                }
            } catch (Exception e) {
                System.out.println("getConfirmationText: probe selector failed (" + selector + "): " + e.getMessage());
            }
        }
        // Fallback: try to get any dialog text
        try {
            if (page.locator(confirmationPopup).count() > 0) {
                String text = page.locator(confirmationPopup).first().textContent();
                if (text != null && !text.trim().isEmpty()) return text.trim();
            }
        } catch (Exception e) {
            System.out.println("getConfirmationText: fallback probe failed: " + e.getMessage());
        }

        // If still null, save debug snapshot to target/ for investigation
        saveDebugSnapshot();
        return null;
    }

    public void clickOk() {
        try {
            if (page.locator(okBtn).count() > 0) {
                page.locator(okBtn).first().click();
                return;
            }
        } catch (Exception e) {
            System.out.println("clickOk: OK button selector failed: " + e.getMessage());
        }

        // Alternative swal2 selector
        try {
            if (page.locator(".swal2-confirm").count() > 0) {
                page.locator(".swal2-confirm").first().click();
            }
        } catch (Exception e) {
            System.out.println("clickOk: swal2-confirm selector failed: " + e.getMessage());
        }
    }

    private void saveDebugSnapshot() {
        String ts = String.valueOf(Instant.now().toEpochMilli());
        Path outDir = Paths.get("target", "order-debug");
        try {
            Files.createDirectories(outDir);
            // save HTML
            String html = "";
            try { html = page.content(); } catch (Exception e) { System.out.println("saveDebugSnapshot: failed to get page content: " + e.getMessage()); }
            Path htmlPath = outDir.resolve("page-" + ts + ".html");
            try { Files.writeString(htmlPath, html, StandardCharsets.UTF_8); } catch (IOException e) { System.out.println("saveDebugSnapshot: failed to write HTML: " + e.getMessage()); }
            // save screenshot
            Path imgPath = outDir.resolve("screenshot-" + ts + ".png");
            try { page.screenshot(new ScreenshotOptions().setPath(imgPath)); } catch (Exception e) { System.out.println("saveDebugSnapshot: failed to save screenshot: " + e.getMessage()); }
            System.out.println("Saved debug snapshot to: " + outDir.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to save debug snapshot: " + e.getMessage());
        }
    }
}

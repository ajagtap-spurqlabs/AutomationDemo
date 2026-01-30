package pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;

public class HomePage {

    private Page page;

    /* ================= LOCATORS ================= */

    // Home
    private String loginLink = "#login2";
    private String signupLink = "#signin2";

    // Login modal
    private String loginUsername = "#loginusername";
    private String loginPassword = "#loginpassword";
    private String loginButton = "#logInModal button:has-text('Log in')";
    private String logoutLink = "#logout2";

    // Signup modal
    private String signupUsername = "#sign-username";
    private String signupPassword = "#sign-password";
    private String signupButton = "#signInModal button:has-text('Sign up')";

    // Welcome message after login
    private String welcomeUser = "#nameofuser";

    /* ================= CONSTRUCTOR ================= */

    public HomePage(Page page) {
        this.page = page;
    }

    /* ================= HOME ================= */

    public void openApplication(String url) {
        page.navigate(url);
    }

    public boolean isLoginVisible() {
        return page.isVisible(loginLink);
    }

    /* ================= SIGN UP ================= */

    public void clickSignup() {
        page.click(signupLink);
    }

    public boolean signup(String username, String password) {
        // Fill fields
        page.fill(signupUsername, username);
        page.fill(signupPassword, password);

        // Capture dialog message when signup triggers an alert and accept it
        final String[] dialogMessage = new String[1];
        page.onceDialog(dialog -> {
            dialogMessage[0] = dialog.message();
            dialog.accept();
        });

        page.click(signupButton);

        // give some time for dialog to appear and be handled
        page.waitForTimeout(1500);

        // Return true when we received a dialog indicating success or that user exists
        if (dialogMessage[0] != null) {
            String msg = dialogMessage[0].toLowerCase();
            return msg.contains("sign up successful") || msg.contains("user") || msg.contains("already");
        }

        return false;
    }

    /* ================= LOGIN ================= */

    public void clickLogin() {
        page.click(loginLink);
    }

    // Updated login method for dynamic username
    public void login(String username, String password) {
        clickLogin();
        // wait for login modal fields to be present
        try {
            page.waitForSelector(loginUsername, new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            // fallback short sleep
            page.waitForTimeout(1000);
        }

        // Try login attempt(s)
        for (int attempt = 1; attempt <= 2; attempt++) {
            page.fill(loginUsername, username);
            page.fill(loginPassword, password);
            page.click(loginButton);

            // wait for welcome message to appear (login to complete)
            try {
                page.waitForSelector(welcomeUser, new Page.WaitForSelectorOptions().setTimeout(7000));
            } catch (Exception e) {
                // ignore and retry if attempts left
            }

            if (isLoginSuccessful()) {
                return; // success
            }

            // retry: re-open login modal briefly
            if (attempt == 1) {
                page.waitForTimeout(500);
                clickLogin();
            }
        }
    }

    // Check if login is successful (Welcome message appears)
    public boolean isLoginSuccessful() {
        try {
            // Wait briefly for the welcome element to appear
            page.waitForSelector(welcomeUser, new Page.WaitForSelectorOptions().setTimeout(5000));
            String text = page.innerText(welcomeUser);
            System.out.println("Welcome text after login check: " + text);
            return page.isVisible(welcomeUser) && text != null && !text.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // Return welcome text if present
    public String getWelcomeText() {
        try {
            if (page.isVisible(welcomeUser)) {
                return page.innerText(welcomeUser);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    // Optional: check if logout link is visible
    public boolean isLogoutVisible() {
        return page.isVisible(logoutLink);
    }
    /* ================= CART / PRODUCT ================= */

    // Select product from home page
    public void selectProduct(String productName) {
        page.locator("//a[text()='" + productName + "']").click();
    }

    // Open cart page
    public void openCart() {
        page.locator("#cartur").click();
    }

}

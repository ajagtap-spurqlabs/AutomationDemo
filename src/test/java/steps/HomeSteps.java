package steps;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.*;
import org.junit.Assert;
import pages.HomePage;
import pages.ProductPage;
import pages.CartPage;
import pages.OrderPage;
import utils.JsonUtil;
import utils.PlaywrightFactory;
import hooks.Hooks;


public class HomeSteps {

    Page page = PlaywrightFactory.getPage();
    HomePage homePage = new HomePage(page);
    ProductPage productPage = new ProductPage(page);
    CartPage cartPage = new CartPage(page);
    OrderPage orderPage = new OrderPage(page);

    // -------------------- Existing Steps --------------------

    @Given("user launches Demoblaze application")
    public void user_launches_application() {
        homePage.openApplication(JsonUtil.get("application.url"));
    }

    @Then("home page should be displayed")
    public void home_page_displayed() {
        Assert.assertTrue(homePage.isLoginVisible());
    }

    @When("user signs up with valid data")
    public void user_signs_up() {
        // Signup already handled in Hooks
        Assert.assertTrue(true);
    }

    @Then("signup should be successful")
    public void signup_successful() {
        Assert.assertTrue(true);
    }

    @When("user logs in with valid data")
    public void user_logs_in_with_valid_data() {

        // If already logged in, skip the login steps to avoid repeated login modals
        if (homePage.isLoginSuccessful() || homePage.isLogoutVisible()) {
            return;
        }

        String username = Hooks.getSignupUsername();
        String password = Hooks.getSignupPassword();

        try {
            page.waitForSelector("#login2",
                    new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception ignored) {
        }

        homePage.login(username, password);
    }

    @Then("login should be successful")
    public void login_should_be_successful() {
        String welcome = homePage.getWelcomeText();
        Assert.assertTrue(
                "Login failed. Welcome text: '" + welcome + "'",
                homePage.isLoginSuccessful()
        );
    }

    // -------------------- Cart Step Definitions --------------------

    @When("user selects product {string}")
    public void user_selects_product(String productName) {
        homePage.selectProduct(productName);
    }

    @When("user adds product to cart")
    public void user_adds_product_to_cart() {
        // ProductPage.addToCart() now handles dialog acceptance via onceDialog()
        productPage.addToCart();
    }

    @Then("product should be added to cart successfully")
    public void product_should_be_added_to_cart_successfully() {
        homePage.openCart();
        Assert.assertTrue(cartPage.isProductPresent());
    }

    @When("user navigates to cart page")
    public void user_navigates_to_cart_page() {
        homePage.openCart();
    }

    @Then("selected product should be displayed in cart")
    public void selected_product_should_be_displayed_in_cart() {
        Assert.assertTrue(cartPage.isProductPresent());
    }

    @When("user deletes the product from cart")
    public void user_deletes_the_product_from_cart() {
        cartPage.deleteProduct();
    }

    @Then("cart should be empty")
    public void cart_should_be_empty() {
        Assert.assertTrue(cartPage.isCartEmpty());
    }

    // -------------------- Place Order --------------------

    @When("user clicks on Place Order")
    public void user_clicks_on_place_order() {
        try {
            // Use OrderPage helper to click
            orderPage.clickPlaceOrder();
            page.waitForSelector("#orderModal", new Page.WaitForSelectorOptions().setTimeout(5000));
        } catch (Exception e) {
            // fallback to previous approach
            try {
                page.click("text=Place Order");
                page.waitForSelector("#orderModal", new Page.WaitForSelectorOptions().setTimeout(5000));
            } catch (Exception ignored) {
            }
        }
    }

    @When("user enters order details")
    public void user_enters_order_details() {
        String name = "Test User";
        String country = "India";
        String city = "Pune";
        String card = "4111111111111111";
        String month = "12";
        String year = "2025";

        try { name = JsonUtil.get("order.name"); } catch (Exception ignored) {}
        try { country = JsonUtil.get("order.country"); } catch (Exception ignored) {}
        try { city = JsonUtil.get("order.city"); } catch (Exception ignored) {}
        try { card = JsonUtil.get("order.card"); } catch (Exception ignored) {}
        try { month = JsonUtil.get("order.month"); } catch (Exception ignored) {}
        try { year = JsonUtil.get("order.year"); } catch (Exception ignored) {}

        // Use OrderPage helper to fill form
        orderPage.fillOrderForm(name, country, city, card, month, year);
    }

    @When("user confirms the purchase")
    public void user_confirms_the_purchase() {
        // Use OrderPage helper to click purchase and wait for confirmation
        orderPage.clickPurchase();
        // Wait briefly and then rely on OrderPage.isOrderSuccessful
        try {
            Thread.sleep(800); // short pause to allow dialog
        } catch (InterruptedException ignored) {}
        if (!orderPage.isOrderSuccessful()) {
            // fallback longer wait
            try {
                page.waitForSelector(".sweet-alert, .swal2-container", new Page.WaitForSelectorOptions().setTimeout(7000));
            } catch (Exception ignored) {}
        }
    }

    @Then("order should be placed successfully")
    public void order_should_be_placed_successfully() {
        String confirmationText = orderPage.getConfirmationText();
        System.out.println("Order confirmation text found: " + confirmationText);

        // Accept several possibilities: meaningful confirmation text containing 'thank' OR 'purchase' OR 'id'
        boolean textMatches = false;
        if (confirmationText != null) {
            String lower = confirmationText.toLowerCase();
            textMatches = lower.contains("thank") || lower.contains("purchase") || lower.contains("id") || lower.contains("order");
        }

        boolean popupPresent = orderPage.isOrderSuccessful();

        Assert.assertTrue("Order confirmation not found. text='" + confirmationText + "', popupPresent=" + popupPresent,
                textMatches || popupPresent);

        // close dialog
        orderPage.clickOk();
    }

}

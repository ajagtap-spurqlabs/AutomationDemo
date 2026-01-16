package steps;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import pages.HomePage;
import utils.PlaywrightFactory;

public class HomeSteps {

    Page page = PlaywrightFactory.getPage();
    HomePage homePage = new HomePage(page);

    @Given("user launches Demoblaze application")
    public void user_launches_demoblaze_application() {
        homePage.openApplication("https://www.demoblaze.com/");
    }

    @Then("user should see Login option on home page")
    public void user_should_see_login_option_on_home_page() {
        Assert.assertTrue(homePage.isLoginVisible());
    }
}

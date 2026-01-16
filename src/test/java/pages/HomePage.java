package pages;

import com.microsoft.playwright.Page;

public class HomePage {

    Page page;
    private String loginLink = "#login2";

    public HomePage(Page page) {
        this.page = page;
    }

    public void openApplication(String url) {
        page.navigate(url);
    }

    public boolean isLoginVisible() {
        return page.isVisible(loginLink);
    }
}

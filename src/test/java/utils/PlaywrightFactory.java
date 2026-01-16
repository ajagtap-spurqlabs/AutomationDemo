package utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class PlaywrightFactory {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    public static synchronized void initBrowser() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            context = browser.newContext();
            page = context.newPage();
        }
    }

    public static synchronized Page getPage() {
        if (page == null) {
            initBrowser();
        }
        return page;
    }

    public static synchronized void closeBrowser() {
        if (playwright != null) {
            try {
                playwright.close();
            } finally {
                page = null;
                context = null;
                browser = null;
                playwright = null;
            }
        }
    }
}


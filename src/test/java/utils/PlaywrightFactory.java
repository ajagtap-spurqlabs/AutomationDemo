package utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlaywrightFactory {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    private static final Path STORAGE_PATH = Paths.get("target", "storageState.json");

    public static synchronized void initBrowser() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            try {
                if (Files.exists(STORAGE_PATH)) {
                    // create context using persisted storage state (pass Path, not String)
                    context = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(STORAGE_PATH));
                } else {
                    context = browser.newContext();
                }
            } catch (Exception e) {
                // fallback to default context
                context = browser.newContext();
            }
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

    // Save screenshot to the given file path (absolute or relative to project root)
    public static synchronized String takeScreenshot(String relativeFilePath) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            // Ensure browser/page are initialized
            if (page == null) initBrowser();
            page.screenshot(new Page.ScreenshotOptions().setPath(path));
            return path.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to take screenshot: " + e.getMessage(), e);
        }
    }

    // Persist current context storage state to given path
    public static synchronized void saveStorageState(Path path) {
        try {
            if (context == null) initBrowser();
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            // Use Playwright API to save storage state
            context.storageState(new BrowserContext.StorageStateOptions().setPath(path));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save storage state: " + e.getMessage(), e);
        }
    }
}

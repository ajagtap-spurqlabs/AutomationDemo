package hooks;

import io.cucumber.java.*;
import pages.HomePage;
import utils.PlaywrightFactory;
import utils.ExtentManager;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Hooks {

    private static boolean userCreated = false;
    private static String signupUsername;
    private static String signupPassword = "test123";

    // ExtentReports (from ExtentManager)
    private static ExtentReports extent = ExtentManager.getInstance();
    private static ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    private static final Path createdUserFile = Paths.get("target", "created-user.txt");
    private static final Path storageFile = Paths.get("target", "storageState.json");

    public static String getSignupUsername() {
        return signupUsername;
    }

    public static String getSignupPassword() {
        return signupPassword;
    }

    @BeforeAll
    public static synchronized void createUserOnce() {
        // If a username was persisted from a previous run, reuse it to avoid repeated signups
        if (userCreated) return;

        try {
            if (Files.exists(createdUserFile)) {
                try {
                    String persisted = new String(Files.readAllBytes(createdUserFile), StandardCharsets.UTF_8).trim();
                    if (!persisted.isEmpty()) {
                        signupUsername = persisted;
                        userCreated = true;
                        System.out.println("Using persisted user: " + signupUsername);

                        // If storage state doesn't exist, try to login and persist it so future runs skip login
                        if (!Files.exists(storageFile)) {
                            try {
                                HomePage homePage = new HomePage(PlaywrightFactory.getPage());
                                homePage.openApplication("https://www.demoblaze.com/");
                                homePage.login(signupUsername, signupPassword);
                                if (homePage.isLoginSuccessful()) {
                                    PlaywrightFactory.saveStorageState(storageFile);
                                    System.out.println("Saved storage state after reusing persisted user");
                                }
                            } catch (Exception e) {
                                System.out.println("Failed to restore storage state for persisted user: " + e.getMessage());
                            }
                        }

                        return;
                    }
                } catch (IOException e) {
                    System.out.println("Failed to read persisted user file: " + e.getMessage());
                }
            }

            // Create a dynamic username and attempt signup
            signupUsername = "user_" + System.currentTimeMillis();

            HomePage homePage = new HomePage(PlaywrightFactory.getPage());
            homePage.openApplication("https://www.demoblaze.com/");

            boolean created = false;
            int attempts = 0;
            while (!created && attempts < 3) {
                attempts++;
                try {
                    homePage.clickSignup();
                    created = homePage.signup(signupUsername, signupPassword);
                } catch (Exception e) {
                    System.out.println("Signup attempt " + attempts + " failed: " + e.getMessage());
                }

                if (!created) {
                    try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                }
            }

            if (created) {
                try {
                    Files.createDirectories(createdUserFile.getParent());
                    Files.write(createdUserFile, signupUsername.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Dynamic user created and persisted: " + signupUsername);
                } catch (IOException e) {
                    System.out.println("Failed to persist created username: " + e.getMessage());
                }

                // After signup, log in and save storage state so subsequent scenarios/runs are logged in
                try {
                    homePage.login(signupUsername, signupPassword);
                    if (homePage.isLoginSuccessful()) {
                        PlaywrightFactory.saveStorageState(storageFile);
                        System.out.println("Saved storage state after creating user");
                    }
                } catch (Exception e) {
                    System.out.println("Failed to login/persist storage after signup: " + e.getMessage());
                }
            } else {
                System.out.println("Warning: Could not create dynamic user after attempts; proceeding without persisted user.");
            }

            userCreated = true; // mark as created to avoid further attempts in same JVM

        } catch (Exception e) {
            System.out.println("createUserOnce failed: " + e.getMessage());
            // Ensure we don't keep retrying endlessly
            userCreated = true;
        }
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        ExtentTest test = extent.createTest(scenario.getName());
        currentTest.set(test);
    }

    @After
    public void afterScenario(Scenario scenario) {

        ExtentTest test = currentTest.get();

        if (test != null) {
            if (scenario.isFailed()) {
                try {
                    String safeName = scenario.getName().replaceAll("[^a-zA-Z0-9-_]", "_");
                    String screenshotPath =
                            "target/screenshots/" + safeName + "_" + System.currentTimeMillis() + ".png";

                    String savedPath = PlaywrightFactory.takeScreenshot(screenshotPath);

                    test.fail(
                            "Scenario Failed",
                            MediaEntityBuilder.createScreenCaptureFromPath(savedPath).build()
                    );
                } catch (Exception e) {
                    test.fail("Screenshot capture failed: " + e.getMessage());
                }
            } else {
                test.pass("Scenario Passed");
            }
        }

        currentTest.remove();
    }

    @AfterAll
    public static void tearDown() {
        try {
            extent.flush();
        } catch (Exception e) {
            System.out.println("Failed to flush Extent report: " + e.getMessage());
        }

        PlaywrightFactory.closeBrowser();
    }
}

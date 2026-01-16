package hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import utils.PlaywrightFactory;

public class Hooks {

    @Before
    public void setUp() {
        PlaywrightFactory.initBrowser();
    }

    @After
    public void tearDown() {
        PlaywrightFactory.closeBrowser();
    }
}

package BiDiScriptDomain;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openqa.selenium.By;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.Assert;

public class DomMutationTest {

	static protected RemoteWebDriver driver;
	static protected LogInspector logInspector;

	private String webPage = "https://the-internet.herokuapp.com/dynamic_controls";

	@BeforeTest
	public void setup() {
		var options = new FirefoxOptions();
		options.enableBiDi();
		driver = new FirefoxDriver(options);
	}

	@AfterTest
	public void teardown() {
		driver.quit();
	}

	@Test
	public void domMutationEventsTest() throws InterruptedException, TimeoutException, ExecutionException {
		var script = driver.script();

		// Ensure we have the preload script already loaded.
		script.addDomMutationHandler(dm -> {
		});

		driver.get(webPage);

		var form = driver.findElement(By.id("input-example"));
		var input = form.findElement(By.tagName("input"));

		var latch = new CountDownLatch(1);
		script.addDomMutationHandler(dm -> {
			if (input.equals(dm.getElement()) && "disabled".equals(dm.getAttributeName())) {
				System.err.println("The element's disabled attribute has changed");
				latch.countDown();
			}
		});

		var button = form.findElement(By.tagName("button"));
		button.click();
		Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));

		Assert.assertTrue(input.isEnabled());
	}
}

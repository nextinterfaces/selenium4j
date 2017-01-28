package org.roussev.selenium4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;

/**
 * 
 * <p>
 * @author Atanas Roussev (http://www.roussev.org)
 */
public abstract class MainTestCase extends TestCase {

	private static Logger logger = Logger.getLogger(MainTestCase.class);

	private static SeleniumThreadLocal<DriverSelenium> threadLocalSelenium = new SeleniumThreadLocal<DriverSelenium>();

	private final Map<String, String> suiteContext = new HashMap<String, String>();

    private static StringBuffer verificationErrors = new StringBuffer();

	@Override
	public void setUp() throws Exception {
		logger.debug("Setting up " + this + " ...");

		// // A "base url", used by selenium to resolve relative URLs
		// String baseUrl = "http://www.google.com";

		// String packageName = this.getClass().getPackage().getName();
		// String masterPackage = packageName.substring(
		// 0,
		// packageName.indexOf('.'));
		// Class allSuiteClass = Class.forName(masterPackage + ".AllSuite");
		// Method m = allSuiteClass.getMethod("getContext", new Class[] {});
		// Map<String, String> resultMap = (Map<String, String>) m.invoke(
		// allSuiteClass.newInstance(),
		// new Object[] {});
		// suiteContext.putAll(resultMap);
		//
		// logger.info("Setup complete for " + this + " with context: " +
		// suiteContext);
	}

	public String get(String key) {
		return Utilities.getContextValue(key, suiteContext);
	}

	public static void main(String[] args) {
	}

	// @Override
	// public void tearDown() throws Exception {
	// logger.debug("tearDown " + this + " with context: " + suiteContext);
	// closeSeleniumSession();
	// logger.debug("tearDown complete for " + this);
	// }

	protected boolean dontCloseBrowserOnMessage(String text) {
		if (session().isTextPresent(text)) {
			threadLocalSelenium.setInError(true);
			return true;
		}
		return false;
	}

	protected void dontCloseBrowser() {
		threadLocalSelenium.setInError(true);
	}

	protected static void startSeleniumSession(String driver, String webSite) {
		logger.debug("starting SeleniumSession... ");

		WebDriver webDriver = null;

		if (FirefoxDriver.class.getSimpleName().equals(driver)) {
			webDriver = new FirefoxDriver();
			
		} else if (ChromeDriver.class.getSimpleName().equals(driver)) {
			webDriver = new ChromeDriver();
			
		} else if (HtmlUnitDriver.class.getSimpleName().equals(driver)) {
			webDriver = new HtmlUnitDriver();
			
		} else if (InternetExplorerDriver.class.getSimpleName().equals(driver)) {
			webDriver = new InternetExplorerDriver();
		} else if (PhantomJSDriver.class.getSimpleName().equals(driver)) {
			webDriver = new PhantomJSDriver();
		} else {
			throw new UnsupportedOperationException("Driver '" + driver
					+ "' is not supported.");
		}

		webDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		
		WebDriverBackedSelenium selenium = new WebDriverBackedSelenium(webDriver, webSite);

		DriverSelenium driverSelenium = new DriverSelenium();
		driverSelenium.setDriver(webDriver);
		driverSelenium.setSelenium(selenium);
		threadLocalSelenium.set(driverSelenium);

		logger.debug("SeleniumSession started.");
	}

	protected static void closeSeleniumSession() throws Exception {
		logger.debug("closing SeleniumSession... ");
		if (null != session()) {
			if (!threadLocalSelenium.isInError()) {
				session().stop();
				((DriverSelenium)threadLocalSelenium.get()).getDriver().close();
			}
			resetSession();
		}
		logger.debug("SeleniumSession closed. ");
	}

	protected static Selenium session() {
		return ((DriverSelenium)threadLocalSelenium.get()).getSelenium();
	}

	protected static void resetSession() {
		logger.debug("resetting SeleniumSession... ");
		threadLocalSelenium.setInError(false);
		threadLocalSelenium.set(null);
		logger.debug("SeleniumSession reset ");
	}

	/** ********** Inner class ********* */
	private static class DriverSelenium {
		private Selenium selenium;
		private WebDriver driver;
		public Selenium getSelenium() {
			return selenium;
		}
		public void setSelenium(Selenium selenium) {
			this.selenium = selenium;
		}
		public WebDriver getDriver() {
			return driver;
		}
		public void setDriver(WebDriver driver) {
			this.driver = driver;
		}
		
		
	}
	
	private static class SeleniumThreadLocal<T> extends ThreadLocal<T> {
		private boolean isInError;

		public boolean isInError() {
			return isInError;
		}

		public void setInError(boolean isInError) {
			this.isInError = isInError;
		}
	}
	/** ********** Inner class ********* */

    private static String throwableToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }
    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(Object expected, Object actual) {
        try {
            com.thoughtworks.selenium.SeleneseTestBase.assertEquals(expected, actual);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertEquals, but fails at the end of the test (during tearDown) */
    public void verifyEquals(boolean expected, boolean actual) {
        try {
            com.thoughtworks.selenium.SeleneseTestBase.assertEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertTrue, but fails at the end of the test (during tearDown) */
    public void verifyTrue(boolean b) {
        try {
            com.thoughtworks.selenium.SeleneseTestBase.assertTrue(b);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertFalse, but fails at the end of the test (during tearDown) */
    public void verifyFalse(boolean b) {
        try {
            com.thoughtworks.selenium.SeleneseTestBase.assertFalse(b);
        } catch (Error e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /**
     * Asserts that two string arrays have identical string contents (fails at the end of the test,
     * during tearDown)
     */
    public void verifyEquals(String[] expected, String[] actual) {
        String comparisonDumpIfNotEqual = verifyEqualsAndReturnComparisonDumpIfNot(expected, actual);
        if (comparisonDumpIfNotEqual != null) {
            verificationErrors.append(comparisonDumpIfNotEqual);
        }
    }

    /** Asserts that two booleans are not the same */
    public static void assertNotEquals(boolean expected, boolean actual) {
        com.thoughtworks.selenium.SeleneseTestBase.assertNotEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    /** Asserts that two objects are not the same (compares using .equals()) */
    public static void assertNotEquals(Object expected, Object actual) {
        if (expected == null) {
            com.thoughtworks.selenium.SeleneseTestBase.assertFalse("did not expect null to be null", actual == null);
        } else if (expected.equals(actual)) {
            fail("did not expect (" + actual + ") to be equal to (" + expected + ")");
        }
    }

    /** Like assertNotEquals, but fails at the end of the test (during tearDown) */
    public void verifyNotEquals(Object expected, Object actual) {
        try {
            com.thoughtworks.selenium.SeleneseTestBase.assertNotEquals(expected, actual);
        } catch (AssertionError e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /** Like assertNotEquals, but fails at the end of the test (during tearDown) */
    public void verifyNotEquals(boolean expected, boolean actual) {
        try {
            com.thoughtworks.selenium.SeleneseTestBase.assertNotEquals(Boolean.valueOf(expected), Boolean.valueOf(actual));
        } catch (AssertionError e) {
            verificationErrors.append(throwableToString(e));
        }
    }

    /**
     * Asserts that there were no verification errors during the current test, failing immediately if
     * any are found
     */
    public void checkForVerificationErrors() {
        String verificationErrorString = verificationErrors.toString();
        clearVerificationErrors();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    /** Clears out the list of verification errors */
    public void clearVerificationErrors() {
        verificationErrors = new StringBuffer();
    }

    /** checks for verification errors and stops the browser */
    public void tearDown() throws Exception {
        try {
            checkForVerificationErrors();
        } finally {
            if (threadLocalSelenium.get().selenium != null) {
                threadLocalSelenium.get().selenium.stop();
            }
        }
    }

    private static String stringArrayToString(String[] sa) {
        StringBuffer sb = new StringBuffer("{");
        for (int j = 0; j < sa.length; j++) {
            sb.append(" ").append("\"").append(sa[j]).append("\"");
        }
        sb.append(" }");
        return sb.toString();
    }
    private static String verifyEqualsAndReturnComparisonDumpIfNot(String[] expected, String[] actual) {
        boolean misMatch = false;
        if (expected.length != actual.length) {
            misMatch = true;
        }
        for (int j = 0; j < expected.length; j++) {
            if (!com.thoughtworks.selenium.SeleneseTestBase.seleniumEquals(expected[j], actual[j])) {
                misMatch = true;
                break;
            }
        }
        if (misMatch) {
            return "Expected " + stringArrayToString(expected) + " but saw "
                    + stringArrayToString(actual);
        }
        return null;
    }

    // New lines get stored by the Selenium IDE as <br />  and these don't play nice.  Nor can you easily write java source with literal \n in it.
    // only solution seems to be to do a runtime replacement
    private static String replaceBr(String text) {
        text = text.replaceAll("<br ?/>", "\n");

        return text;
    }

}

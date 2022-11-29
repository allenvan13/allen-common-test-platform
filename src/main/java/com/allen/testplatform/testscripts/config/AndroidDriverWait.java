package com.allen.testplatform.testscripts.config;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Sleeper;

import java.time.Clock;
import java.time.Duration;

/**
 * A specialization of {@link FluentWait} that uses WebDriver instances.
 */
public class AndroidDriverWait extends FluentWait<AndroidDriver> {

    private final WebDriver driver;

    /**
     * Wait will ignore instances of NotFoundException that are encountered (thrown) by default in
     * the 'until' condition, and immediately propagate all others.  You can add more to the ignore
     * list by calling ignoring(exceptions to add).
     *
     * @param driver The WebDriver instance to pass to the expected conditions
     * @param timeout The timeout when an expectation is called
     * @see org.openqa.selenium.support.ui.WebDriverWait#ignoring(java.lang.Class)
     */
    public AndroidDriverWait(AndroidDriver driver, Duration timeout) {
        this(
                driver,
                timeout,
                Duration.ofMillis(DEFAULT_SLEEP_TIMEOUT),
                Clock.systemDefaultZone(),
                Sleeper.SYSTEM_SLEEPER);
    }

    /**
     * Wait will ignore instances of NotFoundException that are encountered (thrown) by default in
     * the 'until' condition, and immediately propagate all others.  You can add more to the ignore
     * list by calling ignoring(exceptions to add).
     *
     * @param driver The WebDriver instance to pass to the expected conditions
     * @param timeout The timeout in seconds when an expectation is called
     * @param sleep The duration in milliseconds to sleep between polls.
     * @see org.openqa.selenium.support.ui.WebDriverWait#ignoring(java.lang.Class)
     */
    public AndroidDriverWait(AndroidDriver driver, Duration timeout, Duration sleep) {
        this(driver, timeout, sleep, Clock.systemDefaultZone(), Sleeper.SYSTEM_SLEEPER);
    }

    /**
     * @param driver the WebDriver instance to pass to the expected conditions
     * @param clock used when measuring the timeout
     * @param sleeper used to make the current thread go to sleep
     * @param timeout the timeout when an expectation is called
     * @param sleep the timeout used whilst sleeping
     */
    public AndroidDriverWait(
            AndroidDriver driver, Duration timeout, Duration sleep, Clock clock, Sleeper sleeper) {
        super(driver, clock, sleeper);
        withTimeout(timeout);
        pollingEvery(sleep);
        ignoring(NotFoundException.class);
        this.driver = driver;
    }

    @Override
    protected RuntimeException timeoutException(String message, Throwable lastException) {
        WebDriver exceptionDriver = driver;
        TimeoutException ex = new TimeoutException(message, lastException);
        ex.addInfo(WebDriverException.DRIVER_INFO, exceptionDriver.getClass().getName());
        while (exceptionDriver instanceof WrapsDriver) {
            exceptionDriver = ((WrapsDriver) exceptionDriver).getWrappedDriver();
        }
        if (exceptionDriver instanceof RemoteWebDriver) {
            RemoteWebDriver remote = (RemoteWebDriver) exceptionDriver;
            if (remote.getSessionId() != null) {
                ex.addInfo(WebDriverException.SESSION_ID, remote.getSessionId().toString());
            }
            if (remote.getCapabilities() != null) {
                ex.addInfo("Capabilities", remote.getCapabilities().toString());
            }
        }
        throw ex;
    }
}
package com.tfs.pages;

import com.tfs.driver.DriverManager;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;

public abstract class BasePage {
    protected WebDriverWait waitDriver() {
        return new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(20));
    }

    protected WebElement element(String locatorKey) {
        return waitDriver().until(ExpectedConditions.visibilityOfElementLocated(LocatorRepository.by(locatorKey)));
    }

    protected void tap(String locatorKey) { element(locatorKey).click(); }

    protected void type(String locatorKey, String value) {
        WebElement e = element(locatorKey);
        e.clear();
        e.sendKeys(value);
    }

    protected boolean isDisplayed(String locatorKey) {
        try { return element(locatorKey).isDisplayed(); }
        catch (RuntimeException e) { return false; }
    }

    protected String text(String locatorKey) { return element(locatorKey).getText(); }
}

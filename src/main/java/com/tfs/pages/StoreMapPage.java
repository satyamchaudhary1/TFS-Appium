package com.tfs.pages;

import com.tfs.driver.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** TC008 page object: search postcode, print first default store, go back and close app. */
public class StoreMapPage extends BasePage {

    public record StoreInfo(
            String name,
            String address,
            String postcode,
            String distance,
            String hours,
            boolean hoursArrowClickable,
            boolean callButtonClickable,
            boolean directionsButtonClickable) {
    }

    private record TextSnapshot(String text, int x, int y) {
    }

    private static final String APP_PACKAGE = "com.thefragranceshop";
    private static final Duration WAIT = Duration.ofSeconds(30);

    private static final By HOME_MAP_ACCESSIBILITY = AppiumBy.accessibilityId("Store Map");
    private static final By HOME_MAP_FALLBACK = By.xpath(
            "//*[@content-desc='Map' or @content-desc='Location' "
                    + "or @content-desc='Store Locator' or @text='Map']");

    private static final By LOCATION_PERMISSION_ALLOW = By.id(
            "com.android.permissioncontroller:id/permission_allow_foreground_only_button");
    private static final By LOCATION_PERMISSION_ALLOW_TEXT =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.Button\")"
                            + ".text(\"While using the app\")");
    private static final By LOCATION_ACCURACY_PRECISE = By.id(
            "com.android.permissioncontroller:id/permission_location_accuracy_radio_fine");

    private static final By SEARCH_FIELD = By.xpath(
            "//android.widget.EditText[@hint='Search']");
    private static final By GOOGLE_MAP = AppiumBy.accessibilityId("Google Map");
    private static final By LOCATION_TITLE = By.xpath(
            "//android.widget.TextView[@text='Location']");
    private static final By ADDRESS_ELEMENTS = By.xpath(
            "//android.widget.TextView[starts-with(@text,'Address:')]");

    /*
     * Search field's parent contains the location-pin button. The actual Back
     * button is the clickable sibling immediately before that parent.
     */
    private static final By MAP_BACK_BUTTON = By.xpath(
            "//android.widget.EditText[@hint='Search']"
                    + "/ancestor::android.view.ViewGroup[1]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");

    private static final Pattern UK_POSTCODE = Pattern.compile(
            "(?i)^[A-Z]{1,2}\\d[A-Z\\d]?\\s*\\d[A-Z]{2}$");

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public StoreMapPage() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, WAIT);
    }

    public boolean isHomePageDisplayedQuickly() {
        try {
            String source = driver.getPageSource();
            int matches = 0;
            if (source.contains("Women's")) matches++;
            if (source.contains("Men's")) matches++;
            if (source.contains("Gift Sets")) matches++;
            if (source.contains("New In")) matches++;
            return matches >= 2;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void openMapFromHome() {
        if (clickFirstDisplayed(HOME_MAP_ACCESSIBILITY, HOME_MAP_FALLBACK)) return;

        Dimension size = driver.manage().window().getSize();
        driver.executeScript("mobile: clickGesture", Map.of(
                "x", (int) (size.getWidth() * 0.835),
                "y", (int) (size.getHeight() * 0.070)));
    }

    public boolean allowLocationPermissionIfDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(current ->
                    !current.findElements(LOCATION_PERMISSION_ALLOW).isEmpty()
                            || !current.findElements(LOCATION_PERMISSION_ALLOW_TEXT).isEmpty()
                            || !current.findElements(SEARCH_FIELD).isEmpty());

            List<WebElement> idButtons = driver.findElements(LOCATION_PERMISSION_ALLOW);
            List<WebElement> textButtons = driver.findElements(LOCATION_PERMISSION_ALLOW_TEXT);
            if (idButtons.isEmpty() && textButtons.isEmpty()) {
                System.out.println("[INFO] Location permission already granted.");
                return false;
            }

            List<WebElement> preciseOptions = driver.findElements(LOCATION_ACCURACY_PRECISE);
            if (!preciseOptions.isEmpty() && preciseOptions.get(0).isDisplayed()
                    && !preciseOptions.get(0).isSelected()) {
                preciseOptions.get(0).click();
            }

            WebElement allowButton = !idButtons.isEmpty() ? idButtons.get(0) : textButtons.get(0);
            System.out.println("[ACTION] Clicking 'While using the app'.");
            allowButton.click();
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(LOCATION_PERMISSION_ALLOW));
            System.out.println("[PASS] Location permission allowed.");
            return true;
        } catch (RuntimeException exception) {
            throw new AssertionError("Unable to allow location permission.", exception);
        }
    }

    public boolean isMapScreenDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_FIELD));
            wait.until(ExpectedConditions.presenceOfElementLocated(GOOGLE_MAP));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void searchByPostcode(String postcode) {
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_FIELD));
        search.click();
        search.clear();
        search.sendKeys(postcode);
        System.out.println("[ACTION] Postcode entered: " + postcode);

        try {
            driver.executeScript("mobile: performEditorAction", Map.of("action", "search"));
        } catch (RuntimeException exception) {
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
        }

        try {
            driver.hideKeyboard();
        } catch (RuntimeException ignored) {
            // Keyboard was already closed.
        }

        try {
            waitForFirstStore(Duration.ofSeconds(25));
        } catch (RuntimeException firstAttemptException) {
            search = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_FIELD));
            search.click();
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
            try {
                driver.hideKeyboard();
            } catch (RuntimeException ignored) {
                // Keyboard was already closed.
            }
            waitForFirstStore(Duration.ofSeconds(25));
        }
        System.out.println("[PASS] Default first store displayed for postcode: " + postcode);
    }

    private void waitForFirstStore(Duration timeout) {
        new WebDriverWait(driver, timeout).until(current ->
                current.findElements(LOCATION_TITLE).stream().anyMatch(WebElement::isDisplayed)
                        && current.findElements(ADDRESS_ELEMENTS).stream()
                        .anyMatch(WebElement::isDisplayed));
    }

    public boolean isStoreDetailsSheetDisplayed() {
        return driver.findElements(LOCATION_TITLE).stream().anyMatch(WebElement::isDisplayed)
                && driver.findElements(ADDRESS_ELEMENTS).stream().anyMatch(WebElement::isDisplayed);
    }

    /** Returns only the first store visible by default. No drag or scroll is performed. */
    public StoreInfo getFirstVisibleStore() {
        waitForFirstStore(WAIT);
        List<TextSnapshot> texts = visibleTexts();
        TextSnapshot address = texts.stream()
                .filter(item -> item.text().startsWith("Address:"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("First store address was not visible."));

        String name = texts.stream()
                .filter(item -> item.y() < address.y())
                .filter(item -> address.y() - item.y() < 350)
                .sorted(Comparator.comparingInt(TextSnapshot::y).reversed())
                .map(TextSnapshot::text)
                .filter(this::isPossibleStoreName)
                .findFirst()
                .orElse("");

        String postcode = firstTextAfter(texts, address.y(),
                value -> UK_POSTCODE.matcher(value).matches());
        String distance = firstTextAfter(texts, address.y(),
                value -> value.toLowerCase(Locale.ROOT).endsWith("miles"));
        String hours = texts.stream()
                .filter(item -> item.y() > address.y())
                .map(TextSnapshot::text)
                .filter(this::isHoursText)
                .reduce((left, right) -> left + " " + right)
                .orElse("");

        int controlCount = countFirstStoreControls(address.y());
        return new StoreInfo(
                name, address.text(), postcode, distance, hours,
                controlCount >= 3, controlCount >= 3, controlCount >= 3);
    }

    public boolean hasRequiredFirstStoreData(StoreInfo store) {
        return store != null
                && !store.name().isBlank()
                && !store.address().isBlank()
                && !store.postcode().isBlank()
                && !store.distance().isBlank()
                && !store.hours().isBlank()
                && store.hoursArrowClickable()
                && store.callButtonClickable()
                && store.directionsButtonClickable();
    }

    public void printFirstStore(StoreInfo store) {
        System.out.println("==================================================");
        System.out.println("[STORE MAP] DEFAULT FIRST STORE");
        System.out.println("==================================================");
        System.out.println("Name            : " + store.name());
        System.out.println("Address         : " + store.address());
        System.out.println("Postcode        : " + store.postcode());
        System.out.println("Distance        : " + store.distance());
        System.out.println("Hours           : " + store.hours());
        System.out.println("Hours Arrow     : " + status(store.hoursArrowClickable()));
        System.out.println("Call Button     : " + status(store.callButtonClickable()));
        System.out.println("Directions      : " + status(store.directionsButtonClickable()));
        System.out.println("==================================================");
    }

    /** Clicks the visible map Back control, falls back to Android Back, then closes APK. */
    public boolean clickBackAndCloseApp() {
        System.out.println("[ACTION] Clicking Map Back button.");
        if (!clickIfVisible(MAP_BACK_BUTTON, Duration.ofSeconds(5))) {
            System.out.println("[INFO] Back locator unavailable; pressing Android Back.");
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
        }

        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(SEARCH_FIELD));
        } catch (RuntimeException ignored) {
            System.out.println("[INFO] Closing APK even though map transition was not detectable.");
        }

        boolean terminated = driver.terminateApp(APP_PACKAGE);
        System.out.println("[RESULT] APK closed: " + (terminated ? "PASS" : "FAIL"));
        return terminated;
    }

    private List<TextSnapshot> visibleTexts() {
        return driver.findElements(By.className("android.widget.TextView")).stream()
                .filter(WebElement::isDisplayed)
                .map(element -> new TextSnapshot(
                        element.getText() == null ? "" : element.getText().trim(),
                        element.getRect().getX(), element.getRect().getY()))
                .filter(item -> !item.text().isBlank())
                .sorted(Comparator.comparingInt(TextSnapshot::y)
                        .thenComparingInt(TextSnapshot::x))
                .toList();
    }

    private String firstTextAfter(
            List<TextSnapshot> texts,
            int startY,
            java.util.function.Predicate<String> condition) {
        return texts.stream()
                .filter(item -> item.y() >= startY)
                .map(TextSnapshot::text)
                .filter(condition)
                .findFirst()
                .orElse("");
    }

    private boolean isPossibleStoreName(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        return !normalized.equals("location")
                && !normalized.startsWith("address:")
                && !normalized.equals("hours:")
                && !normalized.equals("open")
                && !normalized.equals("closed")
                && !normalized.startsWith("closes")
                && !normalized.endsWith("miles")
                && !UK_POSTCODE.matcher(value).matches();
    }

    private boolean isHoursText(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        return normalized.equals("hours:")
                || normalized.equals("open")
                || normalized.equals("closed")
                || normalized.startsWith("closes");
    }

    private int countFirstStoreControls(int addressY) {
        int minimumX = (int) (driver.manage().window().getSize().getWidth() * 0.55);
        return (int) driver.findElements(By.xpath("//*[@clickable='true']")).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(element -> element.getRect().getX() >= minimumX)
                .filter(element -> element.getRect().getY() > addressY)
                .count();
    }

    private String status(boolean available) {
        return available ? "CLICKABLE" : "NOT CLICKABLE";
    }

    private boolean clickFirstDisplayed(By... locators) {
        for (By locator : locators) {
            if (clickIfVisible(locator, Duration.ofSeconds(2))) return true;
        }
        return false;
    }

    private boolean clickIfVisible(By locator, Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.elementToBeClickable(locator)).click();
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
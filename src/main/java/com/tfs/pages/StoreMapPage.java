package com.tfs.pages;

import com.tfs.config.ConfigManager;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Page object for Home -> Store Map/Location. */
public class StoreMapPage extends BasePage {

    public record StoreInfo(
            String name,
            String address,
            String postcode,
            String distance,
            String hours,
            boolean callButtonAvailable,
            boolean directionsButtonAvailable) {

        public String key() {
            return (name + "|" + postcode).toLowerCase(Locale.ROOT);
        }
    }

    private static final Duration WAIT = Duration.ofSeconds(30);
    private static final String APP_PACKAGE = "com.thefragranceshop";

    private static final By HOME_MAP_ACCESSIBILITY = AppiumBy.accessibilityId("Store Map");
    private static final By HOME_MAP_TEXT_FALLBACK = By.xpath(
            "//*[@content-desc='Map' or @content-desc='Location' " +
                    "or @content-desc='Store Locator' or @text='Map']");
    private static final By LOCATION_PERMISSION_ALLOW = By.id(
            "com.android.permissioncontroller:id/permission_allow_foreground_only_button");
    private static final By LOCATION_PERMISSION_DENY = By.id(
            "com.android.permissioncontroller:id/permission_deny_button");
    private static final By LOCATION_PERMISSION_DENY_ANYWAY = By.id(
            "com.android.permissioncontroller:id/permission_deny_and_dont_ask_again_button");
    private static final By SEARCH_FIELD = By.xpath(
            "//android.widget.EditText[@hint='Search' or @text='Search']");
    private static final By GOOGLE_MAP = AppiumBy.accessibilityId("Google Map");
    private static final By LOCATION_TITLE = By.xpath(
            "//android.widget.TextView[@text='Location']");
    private static final By ADDRESS_ELEMENTS = By.xpath(
            "//android.widget.TextView[starts-with(@text,'Address:')]");
    private static final By SCROLL_VIEW = By.className("android.widget.ScrollView");

    private static final Pattern UK_POSTCODE = Pattern.compile(
            "(?i)^[A-Z]{1,2}\\d[A-Z\\d]?\\s*\\d[A-Z]{2}$");
    private static final Pattern TIME = Pattern.compile(
            "^\\d{1,2}:\\d{2}\\s*-\\s*\\d{1,2}:\\d{2}$");
    private static final Set<String> DAYS = Set.of(
            "sunday", "monday", "tuesday", "wednesday",
            "thursday", "friday", "saturday");

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

    /** Uses stable identifiers first and a resolution-independent fallback only when required. */
    public void openMapFromHome() {
        if (clickFirstDisplayed(HOME_MAP_ACCESSIBILITY, HOME_MAP_TEXT_FALLBACK)) {
            return;
        }

        Dimension size = driver.manage().window().getSize();
        driver.executeScript("mobile: clickGesture", Map.of(
                "x", (int) (size.getWidth() * 0.835),
                "y", (int) (size.getHeight() * 0.070)));
    }

    public boolean denyLocationPermissionIfDisplayed() {
        if (clickIfVisible(LOCATION_PERMISSION_DENY, Duration.ofSeconds(12))) {
            clickIfVisible(LOCATION_PERMISSION_DENY_ANYWAY, Duration.ofSeconds(2));
            return true;
        }
        return false;
    }

    public boolean allowLocationPermissionIfDisplayed() {
        return clickIfVisible(LOCATION_PERMISSION_ALLOW, Duration.ofSeconds(8));
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
        WebElement search = wait.until(
                ExpectedConditions.elementToBeClickable(SEARCH_FIELD));
        search.click();
        search.clear();
        search.sendKeys(postcode);
        driver.pressKey(new KeyEvent(AndroidKey.ENTER));
        try {
            driver.hideKeyboard();
        } catch (RuntimeException ignored) {
            // Keyboard already closed.
        }
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(LOCATION_TITLE),
                ExpectedConditions.presenceOfElementLocated(ADDRESS_ELEMENTS)));
    }

    public boolean isStoreDetailsSheetDisplayed() {
        return !driver.findElements(LOCATION_TITLE).isEmpty()
                && !driver.findElements(ADDRESS_ELEMENTS).isEmpty();
    }

    /** Collects every dynamically loaded store, deduplicating between scroll positions. */
    public List<StoreInfo> collectAllStores() {
        Map<String, StoreInfo> stores = new LinkedHashMap<>();
        int unchangedScrolls = 0;

        for (int scroll = 0; scroll < 15 && unchangedScrolls < 2; scroll++) {
            int before = stores.size();
            collectVisibleStores(stores);
            unchangedScrolls = stores.size() == before ? unchangedScrolls + 1 : 0;
            if (unchangedScrolls < 2) {
                swipeStoreListUp();
            }
        }

        List<StoreInfo> result = new ArrayList<>(stores.values());
        printStores(result);
        return result;
    }

    public boolean everyStoreHasRequiredData(List<StoreInfo> stores) {
        return !stores.isEmpty() && stores.stream().allMatch(store ->
                !store.name().isBlank()
                        && !store.address().isBlank()
                        && !store.postcode().isBlank()
                        && !store.distance().isBlank()
                        && !store.hours().isBlank()
                        && store.callButtonAvailable()
                        && store.directionsButtonAvailable());
    }

    /** Expands the hours control of the first visible store and validates weekly hours. */
    public boolean expandAndValidateFirstStoreHours() {
        scrollListToTop();
        WebElement card = firstVisibleStoreCard();
        List<WebElement> buttons = card.findElements(By.xpath(".//*[@clickable='true']"));
        if (buttons.size() < 3) return false;
        buttons.get(0).click();
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10)).until(current -> {
                String source = current.getPageSource().toLowerCase(Locale.ROOT);
                return DAYS.stream().filter(source::contains).count() >= 5
                        && TIME.matcher(current.getPageSource()).find();
            });
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** Opens Call and Directions for the first store and safely returns to TFS. */
    public boolean validateFirstStoreCallAndDirections() {
        scrollListToTop();
        boolean callWorked = activateExternalAction(1, "Call Store");
        boolean directionsWorked = activateExternalAction(2, "Directions");
        return callWorked && directionsWorked;
    }

    private void collectVisibleStores(Map<String, StoreInfo> target) {
        for (WebElement addressElement : driver.findElements(ADDRESS_ELEMENTS)) {
            try {
                WebElement card = addressElement.findElement(By.xpath(".."));
                List<String> values = card.findElements(By.className("android.widget.TextView"))
                        .stream().map(WebElement::getText).map(String::trim)
                        .filter(value -> !value.isBlank()).toList();
                StoreInfo store = parseStore(values, card);
                if (!store.name().isBlank()) target.putIfAbsent(store.key(), store);
            } catch (RuntimeException ignored) {
                // Card may be re-rendered while scrolling; next pass will collect it.
            }
        }
    }

    private StoreInfo parseStore(List<String> values, WebElement card) {
        String name = values.isEmpty() ? "" : values.get(0);
        String address = firstMatching(values, value -> value.startsWith("Address:"));
        String postcode = firstMatching(values, value -> UK_POSTCODE.matcher(value).matches());
        String distance = firstMatching(values,
                value -> value.toLowerCase(Locale.ROOT).endsWith("miles"));
        String hours = values.stream()
                .filter(this::isHoursValue)
                .reduce((left, right) -> left + " " + right).orElse("");
        List<WebElement> buttons = card.findElements(By.xpath(".//*[@clickable='true']"));
        return new StoreInfo(name, address, postcode, distance, hours,
                buttons.size() >= 2, buttons.size() >= 3);
    }

    private boolean isHoursValue(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.equals("hours:") || normalized.equals("open")
                || normalized.startsWith("closes") || DAYS.contains(normalized)
                || TIME.matcher(value).matches();
    }

    private String firstMatching(List<String> values,
                                 java.util.function.Predicate<String> condition) {
        return values.stream().filter(condition).findFirst().orElse("");
    }

    private void printStores(List<StoreInfo> stores) {
        System.out.println("==================================================");
        System.out.println("[STORE MAP] TOTAL STORES FOUND: " + stores.size());
        System.out.println("==================================================");
        for (int index = 0; index < stores.size(); index++) {
            StoreInfo store = stores.get(index);
            System.out.printf(Locale.ROOT,
                    "[STORE %02d]%nName       : %s%nAddress    : %s%nPostcode   : %s%n" +
                            "Distance   : %s%nHours      : %s%nCall       : %s%nDirections : %s%n%n",
                    index + 1, store.name(), store.address(), store.postcode(),
                    store.distance(), store.hours(), store.callButtonAvailable() ? "AVAILABLE" : "MISSING",
                    store.directionsButtonAvailable() ? "AVAILABLE" : "MISSING");
        }
    }

    private boolean activateExternalAction(int actionIndex, String actionName) {
        WebElement card = firstVisibleStoreCard();
        List<WebElement> buttons = card.findElements(By.xpath(".//*[@clickable='true']"));
        if (buttons.size() < 3) return false;
        // Source order supplied by Inspector: hours toggle, Call, Directions.
        int index = buttons.size() - 3 + actionIndex;
        if (index < 0 || index >= buttons.size()) return false;
        buttons.get(index).click();
        try {
            boolean opened = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(current -> !APP_PACKAGE.equals(driver.getCurrentPackage()));
            System.out.println("[PASS] " + actionName + " opened an external handler.");
            driver.navigate().back();
            driver.activateApp(APP_PACKAGE);
            wait.until(ExpectedConditions.presenceOfElementLocated(LOCATION_TITLE));
            return opened;
        } catch (RuntimeException exception) {
            System.out.println("[FAIL] " + actionName + " did not open an external handler.");
            driver.activateApp(APP_PACKAGE);
            return false;
        }
    }

    private WebElement firstVisibleStoreCard() {
        return wait.until(current -> {
            List<WebElement> addresses = current.findElements(ADDRESS_ELEMENTS);
            return addresses.isEmpty() ? null : addresses.get(0).findElement(By.xpath(".."));
        });
    }

    private void swipeStoreListUp() {
        WebElement scroll = driver.findElements(SCROLL_VIEW).stream().findFirst().orElse(null);
        Dimension size = driver.manage().window().getSize();
        int left = scroll == null ? 0 : scroll.getRect().getX();
        int top = scroll == null ? (int) (size.getHeight() * 0.35) : scroll.getRect().getY();
        int width = scroll == null ? size.getWidth() : scroll.getRect().getWidth();
        int height = scroll == null ? (int) (size.getHeight() * 0.58) : scroll.getRect().getHeight();
        driver.executeScript("mobile: swipeGesture", Map.of(
                "left", left, "top", top, "width", width, "height", height,
                "direction", "up", "percent", 0.75));
    }

    private void scrollListToTop() {
        Dimension size = driver.manage().window().getSize();
        for (int i = 0; i < 8; i++) {
            driver.executeScript("mobile: swipeGesture", Map.of(
                    "left", 0, "top", (int) (size.getHeight() * 0.25),
                    "width", size.getWidth(), "height", (int) (size.getHeight() * 0.65),
                    "direction", "down", "percent", 0.85));
        }
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
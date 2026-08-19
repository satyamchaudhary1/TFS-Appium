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
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final By LOCATION_ACCURACY_PRECISE = By.id(
            "com.android.permissioncontroller:id/permission_location_accuracy_radio_fine");
    private static final By LOCATION_PERMISSION_ALLOW_TEXT =
            AppiumBy.androidUIAutomator(
                    "new UiSelector().className(\"android.widget.Button\")"
                            + ".text(\"While using the app\")");
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

    public boolean allowLocationPermissionIfDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(current ->
                    !current.findElements(LOCATION_PERMISSION_ALLOW).isEmpty()
                            || !current.findElements(LOCATION_PERMISSION_ALLOW_TEXT).isEmpty()
                            || !current.findElements(SEARCH_FIELD).isEmpty());

            List<WebElement> idButtons = driver.findElements(LOCATION_PERMISSION_ALLOW);
            List<WebElement> textButtons = driver.findElements(LOCATION_PERMISSION_ALLOW_TEXT);

            if (idButtons.isEmpty() && textButtons.isEmpty()
                    && !driver.findElements(SEARCH_FIELD).isEmpty()) {
                System.out.println(
                        "[INFO] Location permission was already granted. Map screen displayed.");
                return false;
            }

            WebElement allowButton = !idButtons.isEmpty()
                    ? idButtons.get(0)
                    : textButtons.get(0);

            List<WebElement> preciseOptions =
                    driver.findElements(LOCATION_ACCURACY_PRECISE);
            if (!preciseOptions.isEmpty()
                    && preciseOptions.get(0).isDisplayed()
                    && !preciseOptions.get(0).isSelected()) {
                preciseOptions.get(0).click();
            }

            System.out.println("[ACTION] Clicking 'While using the app'.");
            allowButton.click();

            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.invisibilityOfElementLocated(
                            LOCATION_PERMISSION_ALLOW));

            System.out.println("[PASS] 'While using the app' clicked successfully.");
            return true;

        } catch (RuntimeException exception) {
            throw new AssertionError(
                    "Unable to click the 'While using the app' location permission button.",
                    exception);
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
        WebElement search = wait.until(
                ExpectedConditions.elementToBeClickable(SEARCH_FIELD));
        search.click();
        search.clear();
        search.sendKeys(postcode);

        System.out.println("[ACTION] Postcode entered: " + postcode);
        System.out.println("[ACTION] Submitting postcode using keyboard Search action.");

        // React Native TextInput normally triggers onSubmitEditing from the IME
        // Search action. A raw ENTER key alone may only add/consume a key event.
        try {
            driver.executeScript("mobile: performEditorAction", Map.of("action", "search"));
        } catch (RuntimeException editorActionException) {
            System.out.println("[INFO] IME Search action unavailable; using ENTER fallback.");
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
        }

        try {
            driver.hideKeyboard();
        } catch (RuntimeException ignored) {
            // Keyboard already closed.
        }

        try {
            waitForStoreResultPopup(Duration.ofSeconds(25));
        } catch (RuntimeException firstAttemptException) {
            // Some Android keyboards expose only Enter/Go even when the field
            // requests Search. Retry once without retyping the postcode.
            System.out.println("[INFO] Store popup not displayed after IME Search. "
                    + "Retrying with ENTER.");
            search = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_FIELD));
            search.click();
            driver.pressKey(new KeyEvent(AndroidKey.ENTER));
            try {
                driver.hideKeyboard();
            } catch (RuntimeException ignored) {
                // Keyboard already closed.
            }
            waitForStoreResultPopup(Duration.ofSeconds(25));
        }

        System.out.println("[PASS] Store-list popup displayed for postcode: " + postcode);
    }

    private void waitForStoreResultPopup(Duration timeout) {
        new WebDriverWait(driver, timeout).until(current -> {
            boolean locationTitleDisplayed = current.findElements(LOCATION_TITLE)
                    .stream().anyMatch(WebElement::isDisplayed);
            boolean storeAddressDisplayed = current.findElements(ADDRESS_ELEMENTS)
                    .stream().anyMatch(WebElement::isDisplayed);
            return locationTitleDisplayed || storeAddressDisplayed;
        });
    }

    public boolean isStoreDetailsSheetDisplayed() {
        return !driver.findElements(LOCATION_TITLE).isEmpty()
                && !driver.findElements(ADDRESS_ELEMENTS).isEmpty();
    }

    /** Expands the initially collapsed/half-open store results bottom sheet. */
    public void expandStoreDetailsSheet() {
        WebElement locationHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(LOCATION_TITLE));
        Dimension size = driver.manage().window().getSize();

        System.out.println("[STEP] Expanding collapsed store-list popup.");

        for (int attempt = 1; attempt <= 4; attempt++) {
            List<WebElement> titles = driver.findElements(LOCATION_TITLE);
            boolean expanded = !titles.isEmpty()
                    && titles.get(0).getRect().getY() < size.getHeight() * 0.30
                    && driver.findElements(ADDRESS_ELEMENTS).size() >= 2;

            if (expanded) {
                System.out.println("[PASS] Store results sheet expanded.");
                return;
            }

            locationHeader = titles.isEmpty() ? locationHeader : titles.get(0);
            int headerCenterX = locationHeader.getRect().getX()
                    + (locationHeader.getRect().getWidth() / 2);
            int headerCenterY = locationHeader.getRect().getY()
                    + (locationHeader.getRect().getHeight() / 2);

            System.out.println("[ACTION] Dragging Location popup upward. Attempt " + attempt
                    + " | Start=" + headerCenterX + "," + headerCenterY);
            driver.executeScript("mobile: swipeGesture", Map.of(
                    "left", (int) (size.getWidth() * 0.05),
                    "top", Math.max((int) (size.getHeight() * 0.12), headerCenterY - 20),
                    "width", (int) (size.getWidth() * 0.90),
                    "height", Math.max(300, size.getHeight() - headerCenterY - 80),
                    "direction", "up",
                    "percent", 0.95));
        }

        wait.until(current -> current.findElements(ADDRESS_ELEMENTS).size() >= 2);
        System.out.println("[PASS] Store popup expanded and multiple stores are visible.");
    }

    /** Collects every dynamically loaded store, deduplicating between scroll positions. */
    public List<StoreInfo> collectAllStores() {
        Map<String, StoreInfo> stores = new LinkedHashMap<>();
        int unchangedScrolls = 0;

        for (int scroll = 0; scroll < 20 && unchangedScrolls < 3; scroll++) {
            int before = stores.size();
            collectVisibleStores(stores);
            unchangedScrolls = stores.size() == before ? unchangedScrolls + 1 : 0;
            System.out.println("[STORE MAP] Scroll " + scroll
                    + " | Unique stores collected: " + stores.size());
            if (unchangedScrolls < 3) {
                scrollStorePopupUp();
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

    /** Expands every store arrow and validates Sunday-Saturday opening hours. */
    public boolean validateAllStoreHours(List<StoreInfo> stores) {
        if (stores.isEmpty()) return false;
        scrollListToTop();
        boolean allPassed = true;

        for (StoreInfo store : stores) {
            if (!bringStoreIntoView(store.name())) {
                System.out.println("[FAIL] Hours arrow not reachable for: " + store.name());
                allPassed = false;
                continue;
            }

            StoreControls controls = findStoreControls(store.name());
            boolean buttonsReady = isClickable(controls.hoursArrow())
                    && isClickable(controls.callButton())
                    && isClickable(controls.directionsButton());

            System.out.println("[STORE CONTROLS] " + store.name()
                    + " | Hours Arrow=" + status(isClickable(controls.hoursArrow()))
                    + " | Call=" + status(isClickable(controls.callButton()))
                    + " | Directions=" + status(isClickable(controls.directionsButton())));

            if (!buttonsReady) {
                allPassed = false;
                continue;
            }

            controls.hoursArrow().click();
            boolean hoursDisplayed = waitForWeeklyHours();
            System.out.println("[STORE HOURS] " + store.name() + " | "
                    + (hoursDisplayed ? "PASS - Sunday to Saturday displayed"
                    : "FAIL - Weekly hours not displayed"));
            allPassed &= hoursDisplayed;

            // Collapse again so the next store can be brought into view reliably.
            if (hoursDisplayed) {
                findStoreControls(store.name()).hoursArrow().click();
            }
        }

        return allPassed;
    }

    /** Backward-compatible helper retained for any older test class. */
    public boolean expandAndValidateFirstStoreHours(List<StoreInfo> stores) {
        if (stores.isEmpty()) return false;
        return validateAllStoreHours(List.of(stores.get(0)));
    }

    /** Opens Call and Directions for every discovered store and returns to TFS. */
    public boolean validateAllStoreCallAndDirections(List<StoreInfo> stores) {
        scrollListToTop();
        boolean allPassed = true;

        for (StoreInfo store : stores) {
            if (!bringStoreIntoView(store.name())) {
                System.out.println("[FAIL] Could not bring store into view: " + store.name());
                allPassed = false;
                continue;
            }

            boolean callWorked = activateExternalAction(store.name(), 0, "Call Store");
            boolean directionsWorked = activateExternalAction(store.name(), 1, "Directions");
            System.out.println("[STORE ACTION] " + store.name()
                    + " | Call=" + (callWorked ? "PASS" : "FAIL")
                    + " | Directions=" + (directionsWorked ? "PASS" : "FAIL"));
            allPassed &= callWorked && directionsWorked;
        }

        return allPassed;
    }

    private void collectVisibleStores(Map<String, StoreInfo> target) {
        List<TextSnapshot> texts = driver.findElements(By.className("android.widget.TextView"))
                .stream()
                .filter(WebElement::isDisplayed)
                .map(element -> new TextSnapshot(
                        element.getText() == null ? "" : element.getText().trim(),
                        element.getRect().getX(), element.getRect().getY()))
                .filter(item -> !item.text().isBlank())
                .sorted(Comparator.comparingInt(TextSnapshot::y)
                        .thenComparingInt(TextSnapshot::x))
                .toList();

        List<TextSnapshot> addresses = texts.stream()
                .filter(item -> item.text().startsWith("Address:"))
                .toList();

        for (int addressIndex = 0; addressIndex < addresses.size(); addressIndex++) {
            TextSnapshot address = addresses.get(addressIndex);
            int nextAddressY = addressIndex + 1 < addresses.size()
                    ? addresses.get(addressIndex + 1).y()
                    : driver.manage().window().getSize().getHeight();

            String name = nearestStoreNameAbove(texts, address);
            String postcode = firstTextBetween(texts, address.y(), nextAddressY,
                    value -> UK_POSTCODE.matcher(value).matches());
            String distance = firstTextBetween(texts, address.y(), nextAddressY,
                    value -> value.toLowerCase(Locale.ROOT).endsWith("miles"));
            String hours = texts.stream()
                    .filter(item -> item.y() > address.y() && item.y() < nextAddressY)
                    .map(TextSnapshot::text)
                    .filter(this::isHoursValue)
                    .reduce((left, right) -> left + " " + right)
                    .orElse("");

            int actionCount = countStoreActionButtons(address.y(), nextAddressY);
            StoreInfo store = new StoreInfo(name, address.text(), postcode, distance, hours,
                    actionCount >= 3, actionCount >= 3);

            if (!store.name().isBlank()) {
                StoreInfo existing = target.get(store.key());
                if (existing == null || completeness(store) > completeness(existing)) {
                    target.put(store.key(), store);
                }
            }
        }
    }

    private String nearestStoreNameAbove(List<TextSnapshot> texts, TextSnapshot address) {
        return texts.stream()
                .filter(item -> item.y() < address.y())
                .filter(item -> address.y() - item.y() < 350)
                .sorted(Comparator.comparingInt(TextSnapshot::y).reversed())
                .map(TextSnapshot::text)
                .filter(this::isPossibleStoreName)
                .findFirst().orElse("");
    }

    private boolean isPossibleStoreName(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        return !normalized.equals("location")
                && !normalized.equals("hours:")
                && !normalized.equals("open")
                && !normalized.equals("closed")
                && !normalized.startsWith("closes")
                && !normalized.startsWith("address:")
                && !normalized.endsWith("miles")
                && !DAYS.contains(normalized)
                && !UK_POSTCODE.matcher(value).matches()
                && !TIME.matcher(value).matches();
    }

    private String firstTextBetween(List<TextSnapshot> texts, int startY, int endY,
                                    java.util.function.Predicate<String> condition) {
        return texts.stream()
                .filter(item -> item.y() >= startY && item.y() < endY)
                .map(TextSnapshot::text)
                .filter(condition)
                .findFirst().orElse("");
    }

    private int countStoreActionButtons(int startY, int endY) {
        int minimumX = (int) (driver.manage().window().getSize().getWidth() * 0.58);
        return (int) driver.findElements(By.xpath("//*[@clickable='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() >= minimumX)
                .filter(element -> element.getRect().getY() >= startY)
                .filter(element -> element.getRect().getY() < endY)
                .count();
    }

    private int completeness(StoreInfo store) {
        int score = 0;
        if (!store.name().isBlank()) score++;
        if (!store.address().isBlank()) score++;
        if (!store.postcode().isBlank()) score++;
        if (!store.distance().isBlank()) score++;
        if (!store.hours().isBlank()) score++;
        if (store.callButtonAvailable()) score++;
        if (store.directionsButtonAvailable()) score++;
        return score;
    }

    private boolean isHoursValue(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.equals("hours:") || normalized.equals("open")
                || normalized.startsWith("closes") || DAYS.contains(normalized)
                || TIME.matcher(value).matches();
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

    private boolean activateExternalAction(String storeName, int actionIndex, String actionName) {
        List<WebElement> buttons = actionButtonsForStore(storeName);
        if (buttons.size() < 2 || actionIndex >= buttons.size()) return false;
        buttons.get(actionIndex).click();
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

    private List<WebElement> actionButtonsForStore(String storeName) {
        StoreControls controls = findStoreControls(storeName);
        return List.of(controls.callButton(), controls.directionsButton());
    }

    private StoreControls findStoreControls(String storeName) {
        WebElement nameElement = driver.findElements(
                        By.xpath("//android.widget.TextView[@text=" + xpathLiteral(storeName) + "]"))
                .stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new AssertionError("Store not visible: " + storeName));
        int nameY = nameElement.getRect().getY();

        WebElement hoursLabel = driver.findElements(
                        By.xpath("//android.widget.TextView[@text='Hours:']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getY() > nameY)
                .min(Comparator.comparingInt(element -> element.getRect().getY()))
                .orElseThrow(() -> new AssertionError(
                        "Hours row not visible for store: " + storeName));

        int hoursCenterY = hoursLabel.getRect().getY()
                + (hoursLabel.getRect().getHeight() / 2);
        int minimumX = (int) (driver.manage().window().getSize().getWidth() * 0.55);

        List<WebElement> candidates = driver.findElements(By.xpath("//*[@clickable='true']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(element -> element.getRect().getX() >= minimumX)
                .filter(element -> Math.abs(elementCenterY(element) - hoursCenterY) <= 100)
                .sorted(Comparator.comparingInt(element -> element.getRect().getX()))
                .toList();

        if (candidates.size() < 3) {
            throw new AssertionError("Expected Hours, Call and Directions controls for "
                    + storeName + " but found " + candidates.size());
        }

        return new StoreControls(candidates.get(0), candidates.get(1), candidates.get(2));
    }

    private int elementCenterY(WebElement element) {
        return element.getRect().getY() + (element.getRect().getHeight() / 2);
    }

    private boolean isClickable(WebElement element) {
        return element != null && element.isDisplayed() && element.isEnabled()
                && Boolean.parseBoolean(element.getAttribute("clickable"));
    }

    private String status(boolean passed) {
        return passed ? "CLICKABLE" : "NOT CLICKABLE";
    }

    private boolean waitForWeeklyHours() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10)).until(current -> {
                List<String> visibleTexts = current
                        .findElements(By.className("android.widget.TextView"))
                        .stream()
                        .filter(WebElement::isDisplayed)
                        .map(WebElement::getText)
                        .filter(text -> text != null && !text.isBlank())
                        .toList();

                long visibleDays = visibleTexts.stream()
                        .map(text -> text.toLowerCase(Locale.ROOT).trim())
                        .filter(DAYS::contains)
                        .distinct()
                        .count();
                long visibleTimes = visibleTexts.stream()
                        .filter(text -> TIME.matcher(text.trim()).matches())
                        .count();
                return visibleDays == 7 && visibleTimes >= 5;
            });
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private boolean bringStoreIntoView(String storeName) {
        Dimension size = driver.manage().window().getSize();
        for (int attempt = 0; attempt < 20; attempt++) {
            List<WebElement> matches = driver.findElements(
                    By.xpath("//android.widget.TextView[@text=" + xpathLiteral(storeName) + "]"));
            if (matches.stream().anyMatch(element -> element.isDisplayed()
                    && element.getRect().getY() > size.getHeight() * 0.12
                    && element.getRect().getY() < size.getHeight() * 0.70)) {
                return true;
            }
            scrollStorePopupUp();
        }
        return false;
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        return "concat('" + value.replace("'", "',\"'\",'") + "')";
    }

    /** Scrolls only inside the expanded store popup to reveal additional stores. */
    public void scrollStorePopupUp() {
        WebElement scroll = driver.findElements(SCROLL_VIEW).stream().findFirst().orElse(null);
        Dimension size = driver.manage().window().getSize();
        int left = scroll == null ? 0 : scroll.getRect().getX();
        int top = scroll == null ? (int) (size.getHeight() * 0.30) : scroll.getRect().getY();
        int width = scroll == null ? size.getWidth() : scroll.getRect().getWidth();
        int height = scroll == null ? (int) (size.getHeight() * 0.62)
                : Math.min(scroll.getRect().getHeight(), size.getHeight() - top - 80);

        System.out.println("[ACTION] Scrolling store popup upward to load more stores.");
        driver.executeScript("mobile: swipeGesture", Map.of(
                "left", Math.max(0, left),
                "top", Math.max(0, top),
                "width", Math.max(1, width),
                "height", Math.max(300, height),
                "direction", "up",
                "percent", 0.80));
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

    private record TextSnapshot(String text, int x, int y) {
    }

    private record StoreControls(
            WebElement hoursArrow,
            WebElement callButton,
            WebElement directionsButton) {
    }
}
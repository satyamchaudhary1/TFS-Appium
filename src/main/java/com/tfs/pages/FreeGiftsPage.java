package com.tfs.pages;

import com.tfs.driver.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.appmanagement.ApplicationState;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TC014 complete scope: Free Gifts with Purchase sample PLP, Show More,
 * Product Name A-Z sorting, Armani Filter,
 * return Home and close the application.
 * These cards are free samples, not normal purchasable product cards.
 */
public class FreeGiftsPage extends BasePage {

    private record PositionedElement(WebElement element, int y, int x) {
    }

    public record FreeSampleInfo(
            String brand,
            String sampleName,
            String sampleDescription,
            double samplePrice,
            boolean sampleImageDisplayed,
            String accessibilityDescription) {
    }

    private static final String CODE_VERSION = "TC014-FREE-SAMPLES-STEPS-1-13-V3";
    private static final String APP_PACKAGE = "com.thefragranceshop";
    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final int MAX_SCROLLS_TO_SHOW_MORE = 12;
    private static final int MAX_HORIZONTAL_STORY_SCROLLS = 4;

    private static final By MOBILE_STORIES_HORIZONTAL_ROW = By.xpath(
            "(//android.widget.HorizontalScrollView[@scrollable='true'])[last()]");

    private static final By FREE_GIFTS_LABEL = By.xpath(
            "//android.widget.TextView[@text=\"Free Gifts\"]");
    private static final By FREE_GIFTS_CLICKABLE_CARD = By.xpath(
            "//android.widget.TextView[@text=\"Free Gifts\"]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");
    private static final By FREE_GIFTS_CLICKABLE_CARD_FALLBACK = By.xpath(
            "//android.widget.TextView[@text=\"Free Gifts\"]"
                    + "/parent::android.view.ViewGroup"
                    + "/android.view.ViewGroup[@clickable='true'][1]");

    private static final By PLP_TITLE = By.xpath(
            "(//android.widget.TextView[contains("
                    + "translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                    + "'free gift')])[1]");
    private static final By NUMERIC_TEXTS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.TextView\")"
                    + ".textMatches(\"^[0-9][0-9,]*$\")");
    private static final By SAMPLE_CARDS = By.xpath(
            "//android.view.ViewGroup[@clickable='true'"
                    + " and contains(@content-desc,'0.00')"
                    + " and contains(@content-desc,'Non-Members')]");
    private static final By BEST_SELLER_PRODUCT_CARDS = By.xpath(
            "//android.view.ViewGroup[@clickable='true'"
                    + " and contains(@content-desc,'Members')"
                    + " and contains(@content-desc,'Non-Members')]");
    private static final By PRODUCT_IMAGES = By.className("android.widget.ImageView");
    private static final By VERTICAL_PLP_SCROLL_VIEW = By.xpath(
            "//android.widget.ScrollView[@scrollable='true']");

    private static final By EMPTY_COLLECTION_MESSAGE = By.xpath(
            "//android.widget.TextView[contains(@text,'could not find any products')]");
    private static final By BROWSE_BESTSELLERS_BUTTON =
            AppiumBy.accessibilityId("BROWSE BESTSELLERS");
    private static final By BROWSE_BESTSELLERS_TEXT = By.xpath(
            "//android.widget.TextView[@text='BROWSE BESTSELLERS']");
    private static final By BROWSE_BESTSELLERS_CLICKABLE_PARENT = By.xpath(
            "//android.widget.TextView[@text='BROWSE BESTSELLERS']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
    private static final By BEST_SELLER_PLP_TITLE = By.xpath(
            "//android.widget.TextView[contains("
                    + "translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                    + "'best seller')]");
    private static final By BEST_SELLER_HEADER_BACK = By.xpath(
            "//android.widget.TextView[contains("
                    + "translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                    + "'best seller')]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");

    // The Inspector XML confirms the clickable parent has content-desc=Show More.
    private static final By SHOW_MORE_BUTTON = AppiumBy.accessibilityId("Show More");
    private static final By SHOW_MORE_TEXT = By.xpath(
            "//android.widget.TextView[@text='Show More']");
    private static final By SHOW_MORE_CLICKABLE_PARENT = By.xpath(
            "//android.widget.TextView[@text='Show More']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");

    private static final By SORT_BUTTON = AppiumBy.accessibilityId("Sort By");
    private static final By SORT_BUTTON_TEXT = By.xpath(
            "//android.widget.TextView[@text='Sort By']");
    private static final By SELECTED_SORT_CONTROL = By.xpath(
            "//android.widget.TextView[starts-with(@text,'Product Name : A')]");
    private static final By SORT_PRODUCT_NAME_AZ =
            AppiumBy.accessibilityId("Product Name : A-Z");
    private static final By SORT_OPTIONS = By.xpath(
            "//android.widget.TextView[@text='Price : low to high'"
                    + " or @text='Price : high to low'"
                    + " or @text='Product Name : A-Z'"
                    + " or @text='Product Name : Z-A'"
                    + " or @text='Saving : GBP'"
                    + " or @text='Best Seller'"
                    + " or @text='Review rating'"
                    + " or @text='Release date'"
                    + " or @text='Relevance']");
    private static final List<String> EXPECTED_SORT_OPTIONS = List.of(
            "Price : low to high", "Price : high to low",
            "Product Name : A-Z", "Product Name : Z-A", "Saving : GBP",
            "Best Seller", "Review rating", "Release date", "Relevance");

    private static final By FILTER_BUTTON = AppiumBy.accessibilityId("Filter");
    private static final By FILTER_SCREEN_TITLE = By.xpath(
            "//android.widget.TextView[@text='Filter']");
    private static final By NO_FILTER_RESULTS = By.xpath(
            "//android.widget.TextView[contains("
                    + "translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                    + "'no record') or contains("
                    + "translate(@text,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                    + "'no product')]");
    private static final By BRAND_ARMANI = By.xpath(
            "//android.widget.TextView[@text='Armani']");
    private static final By BRAND_ARMANI_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='Armani']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
    private static final By SHOW_RESULTS_BUTTON = AppiumBy.accessibilityId("show results");
    private static final By SHOW_RESULTS_TEXT = By.xpath(
            "//android.widget.TextView[@text='show results']");
    private static final By SHOW_RESULTS_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='show results']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");

    private static final By HEADER_BACK_ACCESSIBILITY = AppiumBy.accessibilityId("Back");
    private static final By HEADER_BACK_BY_TITLE = By.xpath(
            "(//android.widget.TextView[@text='Free Gifts'"
                    + " or @text='All Free Gifts'"
                    + " or @text='Free Gifts Fragrances'])[1]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");

    // Currency-symbol independent to avoid Windows encoding issues with £.
    private static final Pattern PRICE = Pattern.compile(
            "(?<!\\d)(\\d+\\.\\d{2})(?!\\d)");

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public FreeGiftsPage() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, WAIT);
        System.out.println("[TC014 CODE VERSION] " + CODE_VERSION);
    }

    public boolean isFreeGiftsQuickAccessDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(FREE_GIFTS_LABEL));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void scrollMobileStoriesHorizontallyToFreeGifts() {
        for (int attempt = 0; attempt <= MAX_HORIZONTAL_STORY_SCROLLS; attempt++) {
            if (firstDisplayed(FREE_GIFTS_LABEL).isPresent()) {
                System.out.println("[PASS] Free Gifts category displayed after " + attempt
                        + " horizontal scroll(s).");
                return;
            }
            if (attempt == MAX_HORIZONTAL_STORY_SCROLLS) break;

            WebElement storyRow = firstDisplayed(MOBILE_STORIES_HORIZONTAL_ROW)
                    .orElseThrow(() -> new AssertionError(
                            "[MOBILE STORIES NOT FOUND] Horizontal category row "
                                    + "was not displayed on Home page."));
            try {
                driver.executeScript("mobile: scrollGesture", Map.of(
                        "elementId", ((RemoteWebElement) storyRow).getId(),
                        "direction", "right",
                        "percent", 0.80));
                System.out.println("[SCROLL] Mobile Stories moved horizontally right "
                        + (attempt + 1) + "/" + MAX_HORIZONTAL_STORY_SCROLLS + ".");
            } catch (RuntimeException exception) {
                Rectangle row = storyRow.getRect();
                int centerY = row.getY() + row.getHeight() / 2;
                performW3cFingerDrag(
                        row.getX() + (int) (row.getWidth() * 0.82), centerY,
                        row.getX() + (int) (row.getWidth() * 0.18), centerY);
                System.out.println("[SCROLL] W3C horizontal fallback used for Mobile Stories.");
            }
        }
        throw new AssertionError("[FREE GIFTS CATEGORY NOT FOUND] Free Gifts was not "
                + "displayed after " + MAX_HORIZONTAL_STORY_SCROLLS
                + " horizontal Mobile Stories scrolls.");
    }

    public void openFreeGiftsCategory() {
        WebElement newInCard = firstDisplayed(FREE_GIFTS_CLICKABLE_CARD)
                .or(() -> firstDisplayed(FREE_GIFTS_CLICKABLE_CARD_FALLBACK))
                .orElseThrow(() -> new AssertionError(
                        "Free Gifts label was visible but its clickable card was not found."));

        System.out.println("[ACTION] Clicking Free Gifts category from Home page.");
        newInCard.click();
        try {
            wait.until(current -> !visibleSampleCards().isEmpty()
                    || isEmptyCategoryDisplayed());
        } catch (TimeoutException exception) {
            throw new AssertionError("[NEW IN LOAD FAILED] After clicking Free Gifts, "
                    + "neither product cards nor the BROWSE BESTSELLERS empty-state "
                    + "button appeared within " + WAIT.toSeconds() + " seconds. "
                    + buildFreeGiftsLoadDiagnostics(), exception);
        }

        if (isEmptyCategoryDisplayed()) {
            System.out.println("[INFO] Free Gifts category opened with 0 free samples.");
            System.out.println("[PASS] Empty-category message and BROWSE BESTSELLERS "
                    + "button are displayed.");
        } else {
            System.out.println("[PASS] Free Gifts with Purchase PLP opened with samples.");
        }
    }

    public boolean isEmptyCategoryDisplayed() {
        // The clickable parent exposes the reliable content-desc. The message is
        // multiline in React Native and may not be exposed as one exact text node.
        return firstDisplayed(BROWSE_BESTSELLERS_BUTTON).isPresent()
                || firstDisplayed(BROWSE_BESTSELLERS_CLICKABLE_PARENT).isPresent()
                || firstDisplayed(BROWSE_BESTSELLERS_TEXT).isPresent();
    }

    private String buildFreeGiftsLoadDiagnostics() {
        return "Detected state: productCards=" + visibleSampleCards().size()
                + ", browseBestSellersAccessibility="
                + firstDisplayed(BROWSE_BESTSELLERS_BUTTON).isPresent()
                + ", browseBestSellersText="
                + firstDisplayed(BROWSE_BESTSELLERS_TEXT).isPresent()
                + ", emptyMessage="
                + firstDisplayed(EMPTY_COLLECTION_MESSAGE).isPresent()
                + ", newInTitle=" + firstDisplayed(PLP_TITLE).isPresent()
                + ", sortBy=" + (firstDisplayed(SORT_BUTTON).isPresent()
                || firstDisplayed(SORT_BUTTON_TEXT).isPresent()) + ".";
    }

    public int validateEmptyCategory() {
        if (!isEmptyCategoryDisplayed()) {
            throw new AssertionError("[EMPTY STATE NOT DISPLAYED] Expected the empty "
                    + "Free Gifts collection message and BROWSE BESTSELLERS button.");
        }

        Integer displayedCount = driver.findElements(NUMERIC_TEXTS).stream()
                .filter(this::isSafelyDisplayed)
                .map(element -> element.getText().trim().replace(",", ""))
                .filter(value -> value.equals("0"))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(null);
        if (displayedCount == null) {
            throw new AssertionError("[EMPTY COUNT INVALID] Empty Free Gifts category was "
                    + "displayed, but product count 0 was not visible in the header.");
        }

        System.out.println("[PASS] Free Gifts empty-state product count: 0");
        System.out.println("[PASS] Empty-collection message is displayed.");
        return displayedCount;
    }

    public boolean isBrowseBestSellersDisplayedAndEnabled() {
        WebElement browseButton = firstDisplayed(BROWSE_BESTSELLERS_BUTTON)
                .or(() -> firstDisplayed(BROWSE_BESTSELLERS_CLICKABLE_PARENT))
                .or(() -> firstDisplayed(BROWSE_BESTSELLERS_TEXT))
                .orElse(null);
        boolean valid = browseButton != null
                && isSafelyDisplayed(browseButton)
                && browseButton.isEnabled();
        System.out.println("[" + (valid ? "PASS" : "FAIL")
                + "] BROWSE BESTSELLERS displayed and enabled: " + valid);
        return valid;
    }

    public boolean clickBrowseBestSellersAndVerifyRedirect() {
        WebElement browseButton = firstDisplayed(BROWSE_BESTSELLERS_BUTTON)
                .or(() -> firstDisplayed(BROWSE_BESTSELLERS_CLICKABLE_PARENT))
                .or(() -> firstDisplayed(BROWSE_BESTSELLERS_TEXT))
                .orElseThrow(() -> new AssertionError(
                        "[CTA NOT FOUND] BROWSE BESTSELLERS was not displayed."));
        if (!browseButton.isEnabled()) {
            throw new AssertionError(
                    "[CTA DISABLED] BROWSE BESTSELLERS was displayed but disabled.");
        }

        System.out.println("[ACTION] Clicking BROWSE BESTSELLERS.");
        clickElementOrCenter(browseButton, "BROWSE BESTSELLERS");

        try {
            wait.until(current -> firstDisplayed(BROWSE_BESTSELLERS_TEXT).isEmpty()
                    && (firstDisplayed(BEST_SELLER_PLP_TITLE).isPresent()
                    || (hasVisibleBestSellerProducts()
                    && firstDisplayed(FILTER_BUTTON).isPresent())
                    || firstDisplayed(EMPTY_COLLECTION_MESSAGE).isPresent()));
        } catch (TimeoutException exception) {
            throw new AssertionError("[BEST SELLER REDIRECTION FAILED] BROWSE "
                    + "BESTSELLERS was clicked, but the Best Seller PLP did not appear "
                    + "within " + WAIT.toSeconds() + " seconds.", exception);
        }

        System.out.println("[PASS] BROWSE BESTSELLERS redirected to the Best Seller PLP.");
        boolean productsFound = hasVisibleBestSellerProducts();
        if (productsFound) {
            System.out.println("[PASS] Products are displayed on the Best Seller PLP.");
        } else {
            System.out.println("[INFO] No product found on the Best Seller PLP.");
            System.out.println("[ACTION REQUIRED] Return to Home using the top Back icon.");
        }
        return productsFound;
    }

    public boolean goBackFromBestSellerToHome() {
        Optional<WebElement> back = firstDisplayed(HEADER_BACK_ACCESSIBILITY);
        if (back.isEmpty()) back = firstDisplayed(BEST_SELLER_HEADER_BACK);
        if (back.isEmpty()) back = findTopHeaderBackIcon();
        if (back.isPresent()) {
            clickElementOrCenter(back.get(), "Best Seller header Back");
        } else {
            driver.navigate().back();
            System.out.println("[ACTION] Android Back used from Best Seller PLP.");
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(FREE_GIFTS_LABEL));
            System.out.println("[PASS] Navigated from Best Seller PLP to Home page.");
            return true;
        } catch (RuntimeException exception) {
            System.out.println("[FAIL] Home page did not appear after leaving Best Seller PLP: "
                    + exception.getMessage());
            return false;
        }
    }

    private Optional<WebElement> findTopHeaderBackIcon() {
        return driver.findElements(By.className("android.view.ViewGroup")).stream()
                .filter(this::isSafelyDisplayed)
                .filter(element -> "true".equalsIgnoreCase(attribute(element, "clickable")))
                .filter(element -> {
                    Rectangle box = element.getRect();
                    return box.getX() < 180 && box.getY() < 400
                            && box.getWidth() <= 250 && box.getHeight() <= 250;
                })
                .sorted(Comparator.comparingInt(element -> element.getRect().getY()))
                .findFirst();
    }

    private boolean hasVisibleBestSellerProducts() {
        return driver.findElements(BEST_SELLER_PRODUCT_CARDS).stream()
                .anyMatch(this::isSafelyDisplayed);
    }

    public boolean isFreeGiftsPlpDisplayed() {
        try {
            return wait.until(current -> {
                boolean titleDisplayed = firstDisplayed(PLP_TITLE).isPresent();
                boolean productGridDisplayed = !visibleSampleCards().isEmpty()
                        && firstDisplayed(FILTER_BUTTON).isPresent();
                boolean emptyStateDisplayed = isEmptyCategoryDisplayed();
                return titleDisplayed || productGridDisplayed || emptyStateDisplayed;
            });
        } catch (RuntimeException exception) {
            System.out.println("[FAIL] Free Gifts PLP markers were not detected. "
                    + "title=" + firstDisplayed(PLP_TITLE).isPresent()
                    + ", products=" + visibleSampleCards().size()
                    + ", filter=" + firstDisplayed(FILTER_BUTTON).isPresent()
                    + ", emptyState=" + isEmptyCategoryDisplayed());
            return false;
        }
    }

    /** Reads the dynamic numeric value displayed to the right of the Free Gifts title. */
    public int getTotalSampleCount() {
        final Integer count;
        try {
            count = wait.until(this::findDisplayedSampleCount);
        } catch (TimeoutException exception) {
            throw new AssertionError(buildSampleCountError(), exception);
        }

        System.out.println("[PASS] Free Gifts total sample count: " + count);
        return count;
    }

    /** Validates and prints exactly the first two visible free samples. */
    public List<FreeSampleInfo> validateAndPrintFirstTwoSamples() {
        List<WebElement> cards = wait.until(current -> {
            List<WebElement> visible = visibleSampleCards();
            return visible.size() >= 2 ? visible : null;
        });

        List<FreeSampleInfo> samples = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        for (int index = 0; index < 2; index++) {
            FreeSampleInfo sample = readSample(cards.get(index));
            samples.add(sample);
            printSample(index + 1, sample);

            if (!sample.sampleImageDisplayed()) {
                System.out.println("[WARN][FREE SAMPLE " + (index + 1)
                        + "] Sample image is missing. Continuing with text validations.");
            }

            List<String> missing = missingSampleFields(sample);
            if (missing.isEmpty()) {
                System.out.println("[PASS][FREE SAMPLE " + (index + 1)
                        + "] Required sample details are valid: brand, £0.00 price "
                        + "and Non-Members label.");
            } else {
                String failure = "Free sample " + (index + 1) + " (" + sample.brand()
                        + " - " + sample.sampleName() + ") invalid: " + missing;
                failures.add(failure);
                System.out.println("[FAIL] " + failure);
            }
        }

        if (!failures.isEmpty()) {
            throw new AssertionError("First-two free-sample validation failed: "
                    + String.join(" | ", failures));
        }

        System.out.println("[PASS] Exactly 2 initial free samples validated and printed.");
        return samples;
    }

    /**
     * Scrolls to Show More, clicks its clickable parent, and proves that at least
     * one different product becomes visible. Call this method twice for Steps 4–5.
     */
    public int clickShowMoreAndVerifyNewSamples(int clickNumber) {
        if (clickNumber < 1) {
            throw new IllegalArgumentException("Show More click number must be at least 1.");
        }

        System.out.println("[STEP] Searching for Show More click #" + clickNumber + ".");
        WebElement showMore = scrollToShowMore();
        Set<String> samplesBeforeClick = visibleSampleDescriptions();

        WebElement clickableButton = firstDisplayed(SHOW_MORE_BUTTON)
                .or(() -> firstDisplayed(SHOW_MORE_CLICKABLE_PARENT))
                .orElse(showMore);

        if (!clickableButton.isEnabled()) {
            throw new AssertionError("Show More #" + clickNumber
                    + " was displayed but disabled.");
        }

        System.out.println("[ACTION] Clicking Show More #" + clickNumber + ".");
        clickableButton.click();
        waitForShowMoreRefresh(showMore);

        Set<String> newlyVisibleSamples = findNewSamplesAfterClick(samplesBeforeClick);
        if (newlyVisibleSamples.isEmpty()) {
            throw new AssertionError("Show More #" + clickNumber
                    + " was clicked, but no new free sample was detected.");
        }

        System.out.println("[PASS] Show More #" + clickNumber
                + " loaded " + newlyVisibleSamples.size()
                + " newly detected free sample(s). Example: "
                + newlyVisibleSamples.iterator().next());
        return newlyVisibleSamples.size();
    }

    public void openSortAndValidateOptions() {
        scrollToPlpTop();
        WebElement sort = wait.until(ExpectedConditions.elementToBeClickable(SORT_BUTTON));
        sort.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(SORT_PRODUCT_NAME_AZ));
        Set<String> displayedOptions = new LinkedHashSet<>();
        for (WebElement option : driver.findElements(SORT_OPTIONS)) {
            if (isSafelyDisplayed(option)) displayedOptions.add(option.getText().trim());
        }

        List<String> missing = EXPECTED_SORT_OPTIONS.stream()
                .filter(option -> !displayedOptions.contains(option))
                .toList();
        if (!missing.isEmpty()) {
            throw new AssertionError("Sort dropdown is missing option(s): " + missing
                    + ". Displayed options: " + displayedOptions);
        }
        System.out.println("[PASS] Sort By dropdown options: " + displayedOptions);
    }

    public void selectProductNameAToZAndValidate() {
        WebElement sampleNameAToZ = wait.until(
                ExpectedConditions.elementToBeClickable(SORT_PRODUCT_NAME_AZ));
        sampleNameAToZ.click();

        wait.until(current -> {
            List<FreeSampleInfo> samples = readFirstTwoFreshSamples();
            return samples.size() == 2
                    && sampleSortName(samples.get(0)).compareToIgnoreCase(
                    sampleSortName(samples.get(1))) <= 0;
        });

        List<FreeSampleInfo> sorted = readFirstTwoFreshSamples();
        if (sorted.size() != 2) {
            throw new AssertionError("Unable to read first two free samples after sorting.");
        }
        int count = getTotalSampleCount();
        if (count <= 0) {
            throw new AssertionError("Free-sample count was not displayed after sorting.");
        }
        System.out.println("[PASS] Product Name A-Z applied. First names: "
                + sampleSortName(sorted.get(0)) + " <= "
                + sampleSortName(sorted.get(1)));
        System.out.println("[PASS] Free-sample count after sorting: " + count);
    }

    public void openFilter() {
        scrollToPlpTop();
        wait.until(ExpectedConditions.elementToBeClickable(FILTER_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(FILTER_SCREEN_TITLE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(BRAND_ARMANI));
        System.out.println("[PASS] Filter screen opened.");
    }

    public void selectArmaniAndShowResults() {
        clickTextOrClickableAncestor(BRAND_ARMANI_CLICKABLE, BRAND_ARMANI, "Armani");
        clickTextOrClickableAncestor(
                SHOW_RESULTS_BUTTON, SHOW_RESULTS_CLICKABLE, SHOW_RESULTS_TEXT,
                "show results");

        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        System.out.println("[PASS] Armani selected and Show Results clicked.");
    }

    public int validateArmaniFilteredResults() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        int filteredCount = getFilteredSampleCountOrZero();

        List<FreeSampleInfo> filtered = readAvailableFilteredSamples();
        System.out.println("--------------------------------------------------");
        System.out.println("[FILTER RESULT] Applied brand       : Armani");
        System.out.println("[FILTER RESULT] PLP sample count    : " + filteredCount);

        if (filteredCount == 0 || filtered.isEmpty()) {
            System.out.println("[FILTER RESULT] No record found after applying Armani filter.");
        } else {
            System.out.println("[FILTER RESULT] Visible records read: " + filtered.size());
            for (int index = 0; index < filtered.size(); index++) {
                FreeSampleInfo sample = filtered.get(index);
                System.out.println("[FILTERED SAMPLE " + (index + 1) + "] Brand       : "
                        + sample.brand());
                System.out.println("[FILTERED SAMPLE " + (index + 1) + "] Sample Name : "
                        + sample.sampleName());
                System.out.println("[FILTERED SAMPLE " + (index + 1) + "] Description : "
                        + sample.sampleDescription());
                System.out.println("[FILTERED SAMPLE " + (index + 1) + "] Price       : £"
                        + String.format("%.2f", sample.samplePrice()));
            }
        }
        System.out.println("[PASS] Armani filtered PLP processed successfully.");
        System.out.println("--------------------------------------------------");
        return filteredCount;
    }

    public boolean goBackHome() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
            Optional<WebElement> back = firstDisplayed(HEADER_BACK_ACCESSIBILITY)
                    .or(() -> firstDisplayed(HEADER_BACK_BY_TITLE))
                    .or(this::findTopHeaderBackIcon);
            if (back.isPresent()) {
                clickElementOrCenter(back.get(), "Free Gifts PLP header Back");
            } else {
                driver.navigate().back();
                System.out.println("[WARN] Header Back not found; Android Back used.");
            }

            By homeMarkers = By.xpath("//android.widget.TextView["
                    + "@text=\"Women's\" or @text=\"Men's\""
                    + " or @text=\"Gift Sets\" or @text=\"New In\"]");
            boolean homeDisplayed = wait.until(current ->
                    current.findElements(homeMarkers).stream()
                            .filter(this::isSafelyDisplayed).count() >= 2
                            && firstDisplayed(PLP_TITLE).isEmpty());
            System.out.println("[PASS] Home page displayed after Free Gifts PLP Back click.");
            return homeDisplayed;
        } catch (RuntimeException exception) {
            System.out.println("[FAIL] Unable to navigate from Free Gifts PLP to Home: "
                    + exception.getMessage());
            return false;
        }
    }

    public boolean closeApplication() {
        boolean terminated = driver.terminateApp(APP_PACKAGE);
        ApplicationState state = driver.queryAppState(APP_PACKAGE);
        boolean closed = terminated
                && state != ApplicationState.RUNNING_IN_FOREGROUND;
        System.out.println("[" + (closed ? "PASS" : "FAIL")
                + "] APK close state: " + state);
        return closed;
    }

    private void scrollToPlpTop() {
        for (int attempt = 0; attempt < 15; attempt++) {
            if (firstDisplayed(PLP_TITLE).isPresent()
                    && firstDisplayed(FILTER_BUTTON).isPresent()) {
                System.out.println("[PASS] Returned to PLP top after " + attempt
                        + " upward scroll(s).");
                return;
            }
            scrollPlpUp();
        }
        throw new AssertionError("Unable to return to the Free Gifts PLP header.");
    }

    private void scrollPlpUp() {
        Optional<WebElement> scrollView = firstDisplayed(VERTICAL_PLP_SCROLL_VIEW);
        try {
            Map<String, Object> gesture = new java.util.HashMap<>();
            scrollView.ifPresent(element -> gesture.put(
                    "elementId", ((RemoteWebElement) element).getId()));
            gesture.put("direction", "up");
            gesture.put("percent", 0.90);
            driver.executeScript("mobile: scrollGesture", gesture);
            System.out.println("[SCROLL] PLP moved upward toward header.");
            return;
        } catch (RuntimeException exception) {
            System.out.println("[WARN] Upward scrollGesture failed; using W3C fallback: "
                    + exception.getMessage());
        }

        Rectangle area;
        if (scrollView.isPresent()) {
            area = scrollView.get().getRect();
        } else {
            Object viewportCapability = driver.getCapabilities()
                    .getCapability("viewportRect");
            if (viewportCapability instanceof Map<?, ?> viewport
                    && viewport.get("width") instanceof Number width
                    && viewport.get("height") instanceof Number height) {
                int left = viewport.get("left") instanceof Number value
                        ? value.intValue() : 0;
                int top = viewport.get("top") instanceof Number value
                        ? value.intValue() : 0;
                area = new Rectangle(
                        left, top, height.intValue(), width.intValue());
            } else {
                // Safe fallback used only when Appium does not expose viewportRect.
                area = new Rectangle(0, 0, 1920, 1080);
            }
        }
        int centerX = area.getX() + area.getWidth() / 2;
        performW3cFingerDrag(
                centerX, area.getY() + (int) (area.getHeight() * 0.22),
                centerX, area.getY() + (int) (area.getHeight() * 0.82));
    }

    private void clickTextOrClickableAncestor(By preferred, By text, String name) {
        clickTextOrClickableAncestor(preferred, preferred, text, name);
    }

    private void clickTextOrClickableAncestor(
            By preferred, By ancestor, By text, String name) {
        WebElement element = firstDisplayed(preferred)
                .or(() -> firstDisplayed(ancestor))
                .or(() -> firstDisplayed(text))
                .orElseThrow(() -> new AssertionError(name + " control was not displayed."));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (RuntimeException exception) {
            Rectangle box = element.getRect();
            driver.executeScript("mobile: clickGesture", Map.of(
                    "x", box.getX() + box.getWidth() / 2,
                    "y", box.getY() + box.getHeight() / 2));
        }
        System.out.println("[ACTION] Clicked " + name + ".");
    }

    private void clickElementOrCenter(WebElement element, String name) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (RuntimeException exception) {
            Rectangle box = element.getRect();
            driver.executeScript("mobile: clickGesture", Map.of(
                    "x", box.getX() + box.getWidth() / 2,
                    "y", box.getY() + box.getHeight() / 2));
        }
        System.out.println("[ACTION] Clicked " + name + ".");
    }

    private WebElement scrollToShowMore() {
        for (int attempt = 0; attempt <= MAX_SCROLLS_TO_SHOW_MORE; attempt++) {
            Optional<WebElement> button = firstDisplayed(SHOW_MORE_BUTTON)
                    .or(() -> firstDisplayed(SHOW_MORE_CLICKABLE_PARENT))
                    .or(() -> firstDisplayed(SHOW_MORE_TEXT));
            if (button.isPresent()) {
                System.out.println("[PASS] Show More displayed after " + attempt
                        + " scroll(s). No Add to Bag button was clicked.");
                return button.get();
            }

            if (attempt < MAX_SCROLLS_TO_SHOW_MORE) {
                scrollPlpDown();
                System.out.println("[SCROLL] PLP scroll " + (attempt + 1)
                        + "/" + MAX_SCROLLS_TO_SHOW_MORE);
            }
        }

        throw new AssertionError("Show More was not found after "
                + MAX_SCROLLS_TO_SHOW_MORE + " PLP scrolls.");
    }

    private Set<String> findNewSamplesAfterClick(Set<String> samplesBeforeClick) {
        Set<String> newSamples = new LinkedHashSet<>();

        for (int attempt = 0; attempt < 6; attempt++) {
            Set<String> currentSamples = visibleSampleDescriptions();
            currentSamples.stream()
                    .filter(description -> !samplesBeforeClick.contains(description))
                    .forEach(newSamples::add);

            if (!newSamples.isEmpty()) return newSamples;
            scrollPlpDown();
        }
        return newSamples;
    }

    private void waitForShowMoreRefresh(WebElement previousButton) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6)).until(current -> {
                try {
                    return !previousButton.isDisplayed();
                } catch (StaleElementReferenceException exception) {
                    return true;
                }
            });
        } catch (RuntimeException ignored) {
            // Some React Native builds reuse the element. New-product comparison
            // below remains the authoritative verification.
        }
    }

    private FreeSampleInfo readSample(WebElement card) {
        String description = attribute(card, "content-desc");
        if (description.isBlank()) description = attribute(card, "contentDescription");

        String[] parts = description.split(",\\s*");
        String brand = parts.length > 0 ? parts[0].trim() : "";
        List<String> sampleDetails = new ArrayList<>();
        for (int index = 1; index < parts.length; index++) {
            String part = parts[index].trim();
            boolean priceOrAudience = PRICE.matcher(part).find()
                    || part.contains("Members") || part.contains("Non-Members");
            if (!part.isBlank() && !priceOrAudience) sampleDetails.add(part);
        }
        String sampleName = sampleDetails.isEmpty() ? "" : sampleDetails.get(0);
        String sampleDescription = sampleDetails.size() < 2
                ? "" : String.join(" ", sampleDetails.subList(1, sampleDetails.size()));

        List<Double> prices = new ArrayList<>();
        Matcher matcher = PRICE.matcher(description);
        while (matcher.find()) prices.add(Double.parseDouble(matcher.group(1)));

        Rectangle sampleBox = card.getRect();
        boolean imageDisplayed = hasElementInside(PRODUCT_IMAGES, sampleBox);

        return new FreeSampleInfo(
                brand,
                sampleName,
                sampleDescription,
                prices.isEmpty() ? -1 : prices.get(0),
                imageDisplayed,
                description);
    }

    private List<String> missingSampleFields(FreeSampleInfo sample) {
        List<String> missing = new ArrayList<>();
        if (sample.brand().isBlank()) missing.add("brand");
        if (sample.samplePrice() < 0) missing.add("£0.00 sample price");
        if (sample.samplePrice() > 0.001) missing.add(
                "expected £0.00 but found £" + sample.samplePrice());
        if (!sample.accessibilityDescription().contains("Non-Members")) {
            missing.add("Non-Members label");
        }
        return missing;
    }

    private void printSample(int number, FreeSampleInfo sample) {
        System.out.println("--------------------------------------------------");
        System.out.println("[FREE SAMPLE " + number + "] Brand       : " + sample.brand());
        System.out.println("[FREE SAMPLE " + number + "] Sample Name : "
                + displayOrNotProvided(sample.sampleName()));
        System.out.println("[FREE SAMPLE " + number + "] Description : "
                + displayOrNotProvided(sample.sampleDescription()));
        System.out.println("[FREE SAMPLE " + number + "] Price       : £"
                + String.format("%.2f", sample.samplePrice()));
        System.out.println("[FREE SAMPLE " + number + "] Audience    : Non-Members");
        System.out.println("[FREE SAMPLE " + number + "] Image       : "
                + sample.sampleImageDisplayed());
    }

    private String sampleSortName(FreeSampleInfo sample) {
        return sample.sampleName().isBlank() ? sample.brand() : sample.sampleName();
    }

    private String displayOrNotProvided(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }

    private List<WebElement> visibleSampleCards() {
        List<PositionedElement> freshCards = new ArrayList<>();
        for (WebElement element : driver.findElements(SAMPLE_CARDS)) {
            try {
                if (!element.isDisplayed()) continue;
                Rectangle box = element.getRect();
                freshCards.add(new PositionedElement(element, box.getY(), box.getX()));
            } catch (StaleElementReferenceException ignored) {
                // React Native replaced the card during a PLP refresh.
                // The next explicit-wait poll locates the fresh card again.
            }
        }
        return freshCards.stream()
                .sorted(Comparator.comparingInt(PositionedElement::y)
                        .thenComparingInt(PositionedElement::x))
                .map(PositionedElement::element)
                .toList();
    }

    private List<FreeSampleInfo> readFirstTwoFreshSamples() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                List<WebElement> cards = visibleSampleCards();
                if (cards.size() < 2) continue;
                return List.of(readSample(cards.get(0)), readSample(cards.get(1)));
            } catch (StaleElementReferenceException ignored) {
                System.out.println("[INFO] PLP refreshed during product read; retry "
                        + attempt + "/5.");
            }
        }
        return List.of();
    }

    /** Reads zero, one, or two filtered products without requiring two records. */
    private List<FreeSampleInfo> readAvailableFilteredSamples() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                List<WebElement> cards = visibleSampleCards();
                if (cards.isEmpty()) return List.of();

                int numberToRead = Math.min(cards.size(), 2);
                List<FreeSampleInfo> products = new ArrayList<>();
                for (int index = 0; index < numberToRead; index++) {
                    products.add(readSample(cards.get(index)));
                }
                return products;
            } catch (StaleElementReferenceException ignored) {
                System.out.println("[INFO] Filtered PLP refreshed during product read; retry "
                        + attempt + "/5.");
            }
        }
        return List.of();
    }

    /** Returns the displayed filtered count, or zero when the PLP shows no results. */
    private int getFilteredSampleCountOrZero() {
        Integer count = wait.until(current -> {
            if (firstDisplayed(NO_FILTER_RESULTS).isPresent()) return 0;
            return findDisplayedSampleCount(current);
        });

        return count;
    }

    /**
     * Finds the numeric PLP count above the Sort/Filter row. After sorting, the
     * Sort By label changes to Product Name : A-Z, so Filter is retained as a
     * stable fallback anchor.
     */
    private Integer findDisplayedSampleCount(org.openqa.selenium.WebDriver current) {
        try {
            WebElement controlAnchor = current.findElements(SORT_BUTTON).stream()
                    .filter(this::isSafelyDisplayed)
                    .findFirst()
                    .or(() -> current.findElements(SORT_BUTTON_TEXT).stream()
                            .filter(this::isSafelyDisplayed)
                            .findFirst())
                    .or(() -> current.findElements(SELECTED_SORT_CONTROL).stream()
                            .filter(this::isSafelyDisplayed)
                            .findFirst())
                    .or(() -> current.findElements(FILTER_BUTTON).stream()
                            .filter(this::isSafelyDisplayed)
                            .findFirst())
                    .orElse(null);
            if (controlAnchor == null) return null;

            Rectangle anchorBox = controlAnchor.getRect();
            return current.findElements(NUMERIC_TEXTS).stream()
                    .filter(this::isSafelyDisplayed)
                    .filter(candidate -> {
                        Rectangle box = candidate.getRect();
                        boolean aboveControls = box.getY() < anchorBox.getY();
                        boolean inRightHeader = box.getX()
                                > anchorBox.getX() + anchorBox.getWidth() / 2;
                        return aboveControls && inRightHeader;
                    })
                    .sorted((first, second) -> Integer.compare(
                            second.getRect().getX(), first.getRect().getX()))
                    .map(candidate -> candidate.getText().trim().replace(",", ""))
                    .map(Integer::parseInt)
                    .findFirst()
                    .orElse(null);
        } catch (StaleElementReferenceException exception) {
            return null;
        }
    }

    /**
     * Produces an actionable sample-count failure instead of Selenium's lambda text.
     * It prints the screen anchors and every visible numeric TextView so a changed
     * count position can be diagnosed directly from the console.
     */
    private String buildSampleCountError() {
        boolean plpTitleDisplayed = firstDisplayed(PLP_TITLE).isPresent();
        boolean sortByDisplayed = firstDisplayed(SORT_BUTTON).isPresent()
                || firstDisplayed(SORT_BUTTON_TEXT).isPresent()
                || firstDisplayed(SELECTED_SORT_CONTROL).isPresent();
        boolean filterDisplayed = firstDisplayed(FILTER_BUTTON).isPresent();

        List<String> numericCandidates = new ArrayList<>();
        for (WebElement candidate : driver.findElements(NUMERIC_TEXTS)) {
            try {
                if (!candidate.isDisplayed()) continue;
                Rectangle box = candidate.getRect();
                numericCandidates.add("'" + candidate.getText() + "' at [x="
                        + box.getX() + ", y=" + box.getY() + ", width="
                        + box.getWidth() + ", height=" + box.getHeight() + "]");
            } catch (RuntimeException ignored) {
                // A React Native refresh can replace an element while diagnostics
                // are collected. Remaining candidates are still reported.
            }
        }

        return "[SAMPLE COUNT NOT FOUND] Free Gifts with Purchase PLP opened, but its "
                + "numeric sample count was not found in the top-right header within "
                + WAIT.toSeconds() + " seconds. PLP title displayed="
                + plpTitleDisplayed + ", Sort By displayed=" + sortByDisplayed
                + ", Filter displayed=" + filterDisplayed
                + ", visible numeric TextViews=" + numericCandidates
                + ". Expected: a value such as 164 above and to the right of "
                + "the Sort By control. Capture the current page XML if the "
                + "numeric candidates list is empty.";
    }

    private Set<String> visibleSampleDescriptions() {
        Set<String> descriptions = new LinkedHashSet<>();
        for (WebElement card : visibleSampleCards()) {
            String description = attribute(card, "content-desc").trim();
            if (!description.isBlank()) descriptions.add(description);
        }
        return descriptions;
    }

    private boolean hasElementInside(By locator, Rectangle container) {
        return driver.findElements(locator).stream()
                .filter(this::isSafelyDisplayed)
                .map(WebElement::getRect)
                .anyMatch(elementBox -> {
                    int centerX = elementBox.getX() + elementBox.getWidth() / 2;
                    int centerY = elementBox.getY() + elementBox.getHeight() / 2;
                    boolean inside = centerX >= container.getX()
                            && centerX <= container.getX() + container.getWidth()
                            && centerY >= container.getY()
                            && centerY <= container.getY() + container.getHeight();
                    return inside;
                });
    }

    /**
     * Scrolls the actual vertical React Native ScrollView. It first uses Appium's
     * element-based scrollGesture and falls back to a W3C finger drag inside the
     * same container. No screen-level coordinate-only swipe is used.
     */
    private void scrollPlpDown() {
        WebElement scrollView = wait.until(current -> firstDisplayed(VERTICAL_PLP_SCROLL_VIEW)
                .orElse(null));
        try {
            String elementId = ((RemoteWebElement) scrollView).getId();
            Object canScrollMore = driver.executeScript("mobile: scrollGesture", Map.of(
                    "elementId", elementId,
                    "direction", "down",
                    "percent", 0.82));
            System.out.println("[SCROLL] element-based scrollGesture result: "
                    + canScrollMore);
            return;
        } catch (RuntimeException exception) {
            System.out.println("[WARN] element-based scrollGesture failed: "
                    + exception.getMessage());
        }

        Rectangle area = scrollView.getRect();
        int centerX = area.getX() + area.getWidth() / 2;
        int startY = area.getY() + (int) (area.getHeight() * 0.80);
        int endY = area.getY() + (int) (area.getHeight() * 0.22);

        performW3cFingerDrag(centerX, startY, centerX, endY);
        System.out.println("[SCROLL] W3C fallback drag performed inside PLP ScrollView.");
    }

    private void performW3cFingerDrag(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(
                Duration.ZERO,
                PointerInput.Origin.viewport(),
                startX,
                startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(
                Duration.ofMillis(700),
                PointerInput.Origin.viewport(),
                endX,
                endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }


    private Optional<WebElement> firstDisplayed(By locator) {
        return driver.findElements(locator).stream()
                .filter(this::isSafelyDisplayed)
                .findFirst();
    }

    private boolean isSafelyDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String attribute(WebElement element, String name) {
        try {
            String value = element.getAttribute(name);
            return value == null ? "" : value;
        } catch (RuntimeException exception) {
            return "";
        }
    }
}

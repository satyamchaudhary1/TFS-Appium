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
 * TC013 complete scope: horizontal Mobile Stories navigation, Sale PLP,
 * Show More, Sort, Aery Filter,
 * return Home and close the application.
 * Add to Bag is validated for visibility only and is never clicked.
 */
public class SalePage extends BasePage {

    private record PositionedElement(WebElement element, int y, int x) {
    }

    public record ProductInfo(
            String brand,
            String productName,
            String subBrand,
            double memberPrice,
            double nonMemberPrice,
            boolean productImageDisplayed,
            boolean wishlistDisplayed,
            boolean addToBagDisplayed,
            String accessibilityDescription) {
    }

    private static final String CODE_VERSION = "TC013-SALE-STEPS-1-13-V1";
    private static final String APP_PACKAGE = "com.thefragranceshop";
    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final int MAX_SCROLLS_TO_SHOW_MORE = 12;
    private static final int MAX_HORIZONTAL_STORY_SCROLLS = 4;

    private static final By MOBILE_STORIES_HORIZONTAL_ROW = By.xpath(
            "(//android.widget.HorizontalScrollView[@scrollable='true'])[last()]");

    private static final By SALE_LABEL = By.xpath(
            "//android.widget.TextView[@text=\"Sale\"]");
    private static final By SALE_CLICKABLE_CARD = By.xpath(
            "//android.widget.TextView[@text=\"Sale\"]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");
    private static final By SALE_CLICKABLE_CARD_FALLBACK = By.xpath(
            "//android.widget.TextView[@text=\"Sale\"]"
                    + "/parent::android.view.ViewGroup"
                    + "/android.view.ViewGroup[@clickable='true'][1]");

    private static final By PLP_TITLE = By.xpath(
            "(//android.widget.TextView[@text='Sale'"
                    + " or @text='All Sale'"
                    + " or @text='Sale Fragrances'])[1]");
    private static final By NUMERIC_TEXTS = AppiumBy.androidUIAutomator(
            "new UiSelector().className(\"android.widget.TextView\")"
                    + ".textMatches(\"^[0-9][0-9,]*$\")");
    private static final By PRODUCT_CARDS = By.xpath(
            "//android.view.ViewGroup[@clickable='true'"
                    + " and contains(@content-desc,'Members')"
                    + " and contains(@content-desc,'Non-Members')]");
    private static final By PRODUCT_IMAGES = By.className("android.widget.ImageView");
    private static final By VERTICAL_PLP_SCROLL_VIEW = By.xpath(
            "//android.widget.ScrollView[@scrollable='true']");
    private static final By SVG_ELEMENTS = By.xpath(
            "//*[starts-with(@class,'com.horcrux.svg.')]");
    private static final By ADD_TO_BAG_BUTTONS = AppiumBy.accessibilityId("Add to Bag");

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
            "//android.widget.TextView[starts-with(@text,'Price : low to')]");
    private static final By SORT_LOW_TO_HIGH = AppiumBy.accessibilityId("Price : low to high");
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
    private static final By BRAND_AERY = By.xpath(
            "//android.widget.TextView[@text='Aery']");
    private static final By BRAND_AERY_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='Aery']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
    private static final By SHOW_RESULTS_BUTTON = AppiumBy.accessibilityId("show results");
    private static final By SHOW_RESULTS_TEXT = By.xpath(
            "//android.widget.TextView[@text='show results']");
    private static final By SHOW_RESULTS_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='show results']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");

    private static final By HEADER_BACK_ACCESSIBILITY = AppiumBy.accessibilityId("Back");
    private static final By HEADER_BACK_BY_TITLE = By.xpath(
            "(//android.widget.TextView[@text='Sale'"
                    + " or @text='All Sale'"
                    + " or @text='Sale Fragrances'])[1]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");

    // Currency-symbol independent to avoid Windows encoding issues with £.
    private static final Pattern PRICE = Pattern.compile(
            "(?<!\\d)(\\d+\\.\\d{2})(?!\\d)");

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public SalePage() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, WAIT);
        System.out.println("[TC013 CODE VERSION] " + CODE_VERSION);
    }

    public boolean isSaleQuickAccessDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(SALE_LABEL));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void scrollMobileStoriesHorizontallyToSale() {
        for (int attempt = 0; attempt <= MAX_HORIZONTAL_STORY_SCROLLS; attempt++) {
            if (firstDisplayed(SALE_LABEL).isPresent()) {
                System.out.println("[PASS] Sale category displayed after " + attempt
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
        throw new AssertionError("[SALE CATEGORY NOT FOUND] Sale was not displayed after "
                + MAX_HORIZONTAL_STORY_SCROLLS + " horizontal Mobile Stories scrolls.");
    }

    public void openSaleCategory() {
        WebElement saleCard = firstDisplayed(SALE_CLICKABLE_CARD)
                .or(() -> firstDisplayed(SALE_CLICKABLE_CARD_FALLBACK))
                .orElseThrow(() -> new AssertionError(
                        "Sale label was visible but its clickable card was not found."));

        System.out.println("[ACTION] Clicking Sale category from Home page.");
        saleCard.click();
        try {
            wait.until(current -> !visibleProductCards().isEmpty()
                    || isEmptyCategoryDisplayed());
        } catch (TimeoutException exception) {
            throw new AssertionError("[NEW IN LOAD FAILED] After clicking Sale, "
                    + "neither product cards nor the BROWSE BESTSELLERS empty-state "
                    + "button appeared within " + WAIT.toSeconds() + " seconds. "
                    + buildSaleLoadDiagnostics(), exception);
        }

        if (isEmptyCategoryDisplayed()) {
            System.out.println("[INFO] Sale category opened with 0 products.");
            System.out.println("[PASS] Empty-category message and BROWSE BESTSELLERS "
                    + "button are displayed.");
        } else {
            System.out.println("[PASS] Sale PLP opened with products.");
        }
    }

    public boolean isEmptyCategoryDisplayed() {
        // The clickable parent exposes the reliable content-desc. The message is
        // multiline in React Native and may not be exposed as one exact text node.
        return firstDisplayed(BROWSE_BESTSELLERS_BUTTON).isPresent()
                || firstDisplayed(BROWSE_BESTSELLERS_CLICKABLE_PARENT).isPresent()
                || firstDisplayed(BROWSE_BESTSELLERS_TEXT).isPresent();
    }

    private String buildSaleLoadDiagnostics() {
        return "Detected state: productCards=" + visibleProductCards().size()
                + ", browseBestSellersAccessibility="
                + firstDisplayed(BROWSE_BESTSELLERS_BUTTON).isPresent()
                + ", browseBestSellersText="
                + firstDisplayed(BROWSE_BESTSELLERS_TEXT).isPresent()
                + ", emptyMessage="
                + firstDisplayed(EMPTY_COLLECTION_MESSAGE).isPresent()
                + ", saleTitle=" + firstDisplayed(PLP_TITLE).isPresent()
                + ", sortBy=" + (firstDisplayed(SORT_BUTTON).isPresent()
                || firstDisplayed(SORT_BUTTON_TEXT).isPresent()) + ".";
    }

    public int validateEmptyCategory() {
        if (!isEmptyCategoryDisplayed()) {
            throw new AssertionError("[EMPTY STATE NOT DISPLAYED] Expected the empty "
                    + "Sale collection message and BROWSE BESTSELLERS button.");
        }

        Integer displayedCount = driver.findElements(NUMERIC_TEXTS).stream()
                .filter(this::isSafelyDisplayed)
                .map(element -> element.getText().trim().replace(",", ""))
                .filter(value -> value.equals("0"))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(null);
        if (displayedCount == null) {
            throw new AssertionError("[EMPTY COUNT INVALID] Empty Sale category was "
                    + "displayed, but product count 0 was not visible in the header.");
        }

        System.out.println("[PASS] Sale empty-state product count: 0");
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
                    || (!visibleProductCards().isEmpty()
                    && firstDisplayed(FILTER_BUTTON).isPresent())
                    || firstDisplayed(EMPTY_COLLECTION_MESSAGE).isPresent()));
        } catch (TimeoutException exception) {
            throw new AssertionError("[BEST SELLER REDIRECTION FAILED] BROWSE "
                    + "BESTSELLERS was clicked, but the Best Seller PLP did not appear "
                    + "within " + WAIT.toSeconds() + " seconds.", exception);
        }

        System.out.println("[PASS] BROWSE BESTSELLERS redirected to the Best Seller PLP.");
        boolean productsFound = !visibleProductCards().isEmpty();
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
            wait.until(ExpectedConditions.visibilityOfElementLocated(SALE_LABEL));
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

    public boolean isSalePlpDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE))
                    .isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** Reads the dynamic numeric value displayed to the right of the Sale title. */
    public int getTotalProductCount() {
        final Integer count;
        try {
            count = wait.until(this::findDisplayedProductCount);
        } catch (TimeoutException exception) {
            throw new AssertionError(buildProductCountError(), exception);
        }

        System.out.println("[PASS] Sale total product count: " + count);
        return count;
    }

    /** Validates and prints exactly the first two visible PLP products. */
    public List<ProductInfo> validateAndPrintFirstTwoProducts() {
        List<WebElement> cards = wait.until(current -> {
            List<WebElement> visible = visibleProductCards();
            return visible.size() >= 2 ? visible : null;
        });

        List<ProductInfo> products = new ArrayList<>();
        List<String> failures = new ArrayList<>();

        for (int index = 0; index < 2; index++) {
            ProductInfo product = readProduct(cards.get(index));
            products.add(product);
            printProduct(index + 1, product);

            if (!product.productImageDisplayed()) {
                System.out.println("[WARN][PRODUCT " + (index + 1)
                        + "] Product image is missing. Continuing with remaining validations.");
            }

            List<String> missing = missingProductFields(product);
            if (missing.isEmpty()) {
                System.out.println("[PASS][PRODUCT " + (index + 1)
                        + "] All required non-image PLP details are displayed."
                        + " Add to Bag was NOT clicked.");
            } else {
                String failure = "Product " + (index + 1) + " (" + product.brand()
                        + " - " + product.productName() + ") missing: " + missing;
                failures.add(failure);
                System.out.println("[FAIL] " + failure);
            }
        }

        if (!failures.isEmpty()) {
            throw new AssertionError("First-two product validation failed: "
                    + String.join(" | ", failures));
        }

        System.out.println("[PASS] Exactly 2 initial products validated and printed.");
        return products;
    }

    /**
     * Scrolls to Show More, clicks its clickable parent, and proves that at least
     * one different product becomes visible. Call this method twice for Steps 4–5.
     */
    public int clickShowMoreAndVerifyNewProducts(int clickNumber) {
        if (clickNumber < 1) {
            throw new IllegalArgumentException("Show More click number must be at least 1.");
        }

        System.out.println("[STEP] Searching for Show More click #" + clickNumber + ".");
        WebElement showMore = scrollToShowMore();
        Set<String> productsBeforeClick = visibleProductDescriptions();

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

        Set<String> newlyVisibleProducts = findNewProductsAfterClick(productsBeforeClick);
        if (newlyVisibleProducts.isEmpty()) {
            throw new AssertionError("Show More #" + clickNumber
                    + " was clicked, but no new product was detected.");
        }

        System.out.println("[PASS] Show More #" + clickNumber
                + " loaded " + newlyVisibleProducts.size()
                + " newly detected product(s). Example: "
                + newlyVisibleProducts.iterator().next());
        return newlyVisibleProducts.size();
    }

    public void openSortAndValidateOptions() {
        scrollToPlpTop();
        WebElement sort = wait.until(ExpectedConditions.elementToBeClickable(SORT_BUTTON));
        sort.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(SORT_LOW_TO_HIGH));
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

    public void selectLowToHighAndValidate() {
        WebElement lowToHigh = wait.until(
                ExpectedConditions.elementToBeClickable(SORT_LOW_TO_HIGH));
        lowToHigh.click();

        wait.until(current -> {
            List<ProductInfo> products = readFirstTwoFreshProducts();
            return products.size() == 2
                    && products.get(0).memberPrice() >= 0
                    && products.get(1).memberPrice() >= 0
                    && products.get(0).memberPrice() <= products.get(1).memberPrice();
        });

        List<ProductInfo> sorted = readFirstTwoFreshProducts();
        if (sorted.size() != 2) {
            throw new AssertionError("Unable to read first two products after sorting.");
        }
        int count = getTotalProductCount();
        if (count <= 0) {
            throw new AssertionError("Product count was not displayed after sorting.");
        }
        System.out.println("[PASS] Price low-to-high applied. First prices: "
                + sorted.get(0).memberPrice() + " <= " + sorted.get(1).memberPrice());
        System.out.println("[PASS] Product count after sorting: " + count);
    }

    public void openFilter() {
        scrollToPlpTop();
        wait.until(ExpectedConditions.elementToBeClickable(FILTER_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(FILTER_SCREEN_TITLE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(BRAND_AERY));
        System.out.println("[PASS] Filter screen opened.");
    }

    public void selectAeryAndShowResults() {
        clickTextOrClickableAncestor(BRAND_AERY_CLICKABLE, BRAND_AERY, "Aery");
        clickTextOrClickableAncestor(
                SHOW_RESULTS_BUTTON, SHOW_RESULTS_CLICKABLE, SHOW_RESULTS_TEXT,
                "show results");

        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        System.out.println("[PASS] Aery selected and Show Results clicked.");
    }

    public int validateAeryFilteredResults() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        int filteredCount = getFilteredProductCountOrZero();

        List<ProductInfo> filtered = readAvailableFilteredProducts();
        System.out.println("--------------------------------------------------");
        System.out.println("[FILTER RESULT] Applied brand       : Aery");
        System.out.println("[FILTER RESULT] PLP product count   : " + filteredCount);

        if (filteredCount == 0 || filtered.isEmpty()) {
            System.out.println("[FILTER RESULT] No record found after applying Aery filter.");
        } else {
            System.out.println("[FILTER RESULT] Visible records read: " + filtered.size());
            for (int index = 0; index < filtered.size(); index++) {
                ProductInfo product = filtered.get(index);
                System.out.println("[FILTERED PRODUCT " + (index + 1) + "] Brand        : "
                        + product.brand());
                System.out.println("[FILTERED PRODUCT " + (index + 1) + "] Product Name : "
                        + product.productName());
                System.out.println("[FILTERED PRODUCT " + (index + 1) + "] Sub Brand    : "
                        + product.subBrand());
                System.out.println("[FILTERED PRODUCT " + (index + 1) + "] Member Price : "
                        + product.memberPrice());
                System.out.println("[FILTERED PRODUCT " + (index + 1) + "] Non-Member   : "
                        + product.nonMemberPrice());
            }
        }
        System.out.println("[PASS] Aery filtered PLP processed successfully.");
        System.out.println("--------------------------------------------------");
        return filteredCount;
    }

    public boolean goBackHome() {
        System.out.println("[ACTION] Clicking PLP top Back icon.");

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));

            Optional<WebElement> backButton =
                    firstDisplayed(HEADER_BACK_ACCESSIBILITY)
                            .or(() -> firstDisplayed(HEADER_BACK_BY_TITLE))
                            .or(this::findTopHeaderBackIcon);

            if (backButton.isPresent()) {
                clickElementOrCenter(backButton.get(), "Sale PLP header Back");
            } else {
                System.out.println(
                        "[WARN] Header Back icon was not located. Using Android Back."
                );
                driver.navigate().back();
            }

            By homeCategoryMarkers = By.xpath(
                    "//android.widget.TextView["
                            + "@text=\"Women's\""
                            + " or @text=\"Men's\""
                            + " or @text=\"Gift Sets\""
                            + " or @text=\"New In\"]"
            );

            boolean homeDisplayed = wait.until(current -> {
                long visibleCategories = current.findElements(homeCategoryMarkers)
                        .stream()
                        .filter(this::isSafelyDisplayed)
                        .count();

                boolean salePlpClosed =
                        firstDisplayed(PLP_TITLE).isEmpty();

                return visibleCategories >= 2 && salePlpClosed;
            });

            if (homeDisplayed) {
                System.out.println(
                        "[PASS] Home page displayed after Sale PLP Back click."
                );
                System.out.println(
                        "[INFO] Sale label is not required because the Mobile Stories row "
                                + "returns to its initial horizontal position."
                );
                return true;
            }

            System.out.println(
                    "[FAIL] Back was clicked, but Home page markers were not displayed."
            );
            return false;

        } catch (RuntimeException exception) {
            System.out.println("[FAIL] Unable to navigate from Sale PLP to Home page.");
            System.out.println(
                    "[ERROR DETAILS] " + exception.getClass().getSimpleName()
                            + ": " + exception.getMessage()
            );
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
        throw new AssertionError("Unable to return to the Sale PLP header.");
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

    private Set<String> findNewProductsAfterClick(Set<String> productsBeforeClick) {
        Set<String> newProducts = new LinkedHashSet<>();

        for (int attempt = 0; attempt < 6; attempt++) {
            Set<String> currentProducts = visibleProductDescriptions();
            currentProducts.stream()
                    .filter(description -> !productsBeforeClick.contains(description))
                    .forEach(newProducts::add);

            if (!newProducts.isEmpty()) return newProducts;
            scrollPlpDown();
        }
        return newProducts;
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

    private ProductInfo readProduct(WebElement card) {
        String description = attribute(card, "content-desc");
        if (description.isBlank()) description = attribute(card, "contentDescription");

        String[] parts = description.split(",\\s*", 4);
        String brand = parts.length > 0 ? parts[0].trim() : "";
        String productName = parts.length > 1 ? parts[1].trim() : "";
        String subBrand = parts.length > 2 ? parts[2].trim() : "";

        List<Double> prices = new ArrayList<>();
        Matcher matcher = PRICE.matcher(description);
        while (matcher.find()) prices.add(Double.parseDouble(matcher.group(1)));

        Rectangle productBox = card.getRect();
        boolean imageDisplayed = hasElementInside(PRODUCT_IMAGES, productBox, false);
        boolean wishlistDisplayed = hasElementInside(SVG_ELEMENTS, productBox, true);
        boolean addToBagDisplayed = hasAssociatedAddToBag(productBox);

        return new ProductInfo(
                brand,
                productName,
                subBrand,
                prices.isEmpty() ? -1 : prices.get(0),
                prices.size() < 2 ? -1 : prices.get(1),
                imageDisplayed,
                wishlistDisplayed,
                addToBagDisplayed,
                description);
    }

    private List<String> missingProductFields(ProductInfo product) {
        List<String> missing = new ArrayList<>();
        if (product.brand().isBlank()) missing.add("brand");
        if (product.productName().isBlank()) missing.add("product name");
        if (product.subBrand().isBlank()) missing.add("sub-brand");
        if (product.memberPrice() < 0) missing.add("member price");
        if (product.nonMemberPrice() < 0) missing.add("non-member price");
        if (!product.wishlistDisplayed()) missing.add("wishlist icon");
        if (!product.addToBagDisplayed()) missing.add("Add to Bag CTA visibility");
        return missing;
    }

    private void printProduct(int number, ProductInfo product) {
        System.out.println("--------------------------------------------------");
        System.out.println("[PRODUCT " + number + "] Brand          : " + product.brand());
        System.out.println("[PRODUCT " + number + "] Product Name   : " + product.productName());
        System.out.println("[PRODUCT " + number + "] Sub Brand      : " + product.subBrand());
        System.out.println("[PRODUCT " + number + "] Member Price   : " + product.memberPrice());
        System.out.println("[PRODUCT " + number + "] Non-Member     : " + product.nonMemberPrice());
        System.out.println("[PRODUCT " + number + "] Image          : "
                + product.productImageDisplayed());
        System.out.println("[PRODUCT " + number + "] Wishlist       : "
                + product.wishlistDisplayed());
        System.out.println("[PRODUCT " + number + "] Add to Bag CTA : "
                + product.addToBagDisplayed() + " (visibility only; NOT clicked)");
    }

    private List<WebElement> visibleProductCards() {
        List<PositionedElement> freshCards = new ArrayList<>();
        for (WebElement element : driver.findElements(PRODUCT_CARDS)) {
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

    private List<ProductInfo> readFirstTwoFreshProducts() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                List<WebElement> cards = visibleProductCards();
                if (cards.size() < 2) continue;
                return List.of(readProduct(cards.get(0)), readProduct(cards.get(1)));
            } catch (StaleElementReferenceException ignored) {
                System.out.println("[INFO] PLP refreshed during product read; retry "
                        + attempt + "/5.");
            }
        }
        return List.of();
    }

    /** Reads zero, one, or two filtered products without requiring two records. */
    private List<ProductInfo> readAvailableFilteredProducts() {
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                List<WebElement> cards = visibleProductCards();
                if (cards.isEmpty()) return List.of();

                int numberToRead = Math.min(cards.size(), 2);
                List<ProductInfo> products = new ArrayList<>();
                for (int index = 0; index < numberToRead; index++) {
                    products.add(readProduct(cards.get(index)));
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
    private int getFilteredProductCountOrZero() {
        Integer count = wait.until(current -> {
            if (firstDisplayed(NO_FILTER_RESULTS).isPresent()) return 0;
            return findDisplayedProductCount(current);
        });

        return count;
    }

    /**
     * Finds the numeric PLP count above the Sort/Filter row. After sorting, the
     * Sort By label changes to Price : low to..., so Filter is retained as a
     * stable fallback anchor.
     */
    private Integer findDisplayedProductCount(org.openqa.selenium.WebDriver current) {
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
     * Produces an actionable failure instead of Selenium's generated lambda text.
     * It prints the screen anchors and every visible numeric TextView so a changed
     * count position can be diagnosed directly from the console.
     */
    private String buildProductCountError() {
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

        return "[PRODUCT COUNT NOT FOUND] Sale PLP opened, but its numeric "
                + "product count was not found in the top-right header within "
                + WAIT.toSeconds() + " seconds. PLP title displayed="
                + plpTitleDisplayed + ", Sort By displayed=" + sortByDisplayed
                + ", Filter displayed=" + filterDisplayed
                + ", visible numeric TextViews=" + numericCandidates
                + ". Expected: a value such as 164 above and to the right of "
                + "the Sort By control. Capture the current page XML if the "
                + "numeric candidates list is empty.";
    }

    private Set<String> visibleProductDescriptions() {
        Set<String> descriptions = new LinkedHashSet<>();
        for (WebElement card : visibleProductCards()) {
            String description = attribute(card, "content-desc").trim();
            if (!description.isBlank()) descriptions.add(description);
        }
        return descriptions;
    }

    private boolean hasAssociatedAddToBag(Rectangle productBox) {
        return driver.findElements(ADD_TO_BAG_BUTTONS).stream()
                .filter(this::isSafelyDisplayed)
                .map(WebElement::getRect)
                .anyMatch(buttonBox -> {
                    int centerX = buttonBox.getX() + buttonBox.getWidth() / 2;
                    int centerY = buttonBox.getY() + buttonBox.getHeight() / 2;
                    return centerX >= productBox.getX()
                            && centerX <= productBox.getX() + productBox.getWidth()
                            && centerY >= productBox.getY()
                            && centerY <= productBox.getY() + productBox.getHeight() + 150;
                });
    }

    private boolean hasElementInside(By locator, Rectangle container, boolean topRightOnly) {
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
                    return inside && (!topRightOnly
                            || (centerX > container.getX() + container.getWidth() / 2
                            && centerY < container.getY() + container.getHeight() / 2));
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

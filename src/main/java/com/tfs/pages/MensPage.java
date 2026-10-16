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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TC010 complete scope: Men's PLP, Show More twice, Sort, Filter, Back and close.
 * Add to Bag is validated for visibility only and is never clicked.
 */
public class MensPage extends BasePage {

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

    private static final String CODE_VERSION = "TC010-MENS-STEPS-1-13-V4-FRESH-LOGIN";
    private static final String APP_PACKAGE = "com.thefragranceshop";
    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final int MAX_SCROLLS_TO_SHOW_MORE = 12;

    private static final By MENS_LABEL = By.xpath(
            "//android.widget.TextView[@text=\"Men's\"]");
    private static final By MENS_CLICKABLE_CARD = By.xpath(
            "//android.widget.TextView[@text=\"Men's\"]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");
    private static final By MENS_CLICKABLE_CARD_FALLBACK = By.xpath(
            "//android.widget.TextView[@text=\"Men's\"]"
                    + "/parent::android.view.ViewGroup"
                    + "/android.view.ViewGroup[@clickable='true'][1]");

    private static final By PLP_TITLE = By.xpath(
            "//android.widget.TextView[@text=\"Men's Fragrances\"]");
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

    // The Inspector XML confirms the clickable parent has content-desc=Show More.
    private static final By SHOW_MORE_BUTTON = AppiumBy.accessibilityId("Show More");
    private static final By SHOW_MORE_TEXT = By.xpath(
            "//android.widget.TextView[@text='Show More']");
    private static final By SHOW_MORE_CLICKABLE_PARENT = By.xpath(
            "//android.widget.TextView[@text='Show More']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");

    private static final By SORT_BUTTON = AppiumBy.accessibilityId("Sort By");
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
    private static final By BRAND_1667 = By.xpath(
            "//android.widget.TextView[@text='1667']");
    private static final By BRAND_1667_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='1667']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
    private static final By SHOW_RESULTS_BUTTON = AppiumBy.accessibilityId("show results");
    private static final By SHOW_RESULTS_TEXT = By.xpath(
            "//android.widget.TextView[@text='show results']");
    private static final By SHOW_RESULTS_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='show results']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");

    private static final By HEADER_BACK_ACCESSIBILITY = AppiumBy.accessibilityId("Back");
    private static final By HEADER_BACK_BY_TITLE = By.xpath(
            "//android.widget.TextView[@text=\"Men's Fragrances\"]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");

    // Currency-symbol independent to avoid Windows encoding issues with £.
    private static final Pattern PRICE = Pattern.compile(
            "(?<!\\d)(\\d+\\.\\d{2})(?!\\d)");

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public MensPage() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, WAIT);
        System.out.println("[TC010 CODE VERSION] " + CODE_VERSION);
    }

    public boolean isHomePageDisplayedQuickly() {
        try {
            return driver.findElements(MENS_LABEL).stream().anyMatch(WebElement::isDisplayed);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public boolean isMensQuickAccessDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(MENS_LABEL));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void openMensCategory() {
        WebElement womensCard = firstDisplayed(MENS_CLICKABLE_CARD)
                .or(() -> firstDisplayed(MENS_CLICKABLE_CARD_FALLBACK))
                .orElseThrow(() -> new AssertionError(
                        "Men's label was visible but its clickable card was not found."));

        System.out.println("[ACTION] Clicking Men's category from Home page.");
        womensCard.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        wait.until(current -> visibleProductCards().size() >= 2);
        System.out.println("[PASS] Men's PLP opened with products.");
    }

    public boolean isMensPlpDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE))
                    .isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** Reads the dynamic numeric value displayed to the right of Men's Fragrances. */
    public int getTotalProductCount() {
        Integer count = wait.until(current -> {
            try {
                WebElement title = current.findElement(PLP_TITLE);
                Rectangle titleBox = title.getRect();
                for (WebElement candidate : current.findElements(NUMERIC_TEXTS)) {
                    try {
                        if (!candidate.isDisplayed()) continue;
                        String value = candidate.getText().trim();
                        Rectangle box = candidate.getRect();
                        boolean besideTitle = Math.abs(box.getY() - titleBox.getY()) < 110
                                && box.getX() > titleBox.getX();
                        if (besideTitle) return Integer.parseInt(value.replace(",", ""));
                    } catch (StaleElementReferenceException ignored) {
                        // Filter/sort refresh replaced the candidate; retry with fresh DOM.
                    }
                }
                return null;
            } catch (StaleElementReferenceException exception) {
                return null;
            }
        });

        System.out.println("[PASS] Men's total product count: " + count);
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

            List<String> missing = missingProductFields(product);
            if (missing.isEmpty()) {
                System.out.println("[PASS][PRODUCT " + (index + 1)
                        + "] All required PLP details are displayed."
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
        wait.until(ExpectedConditions.visibilityOfElementLocated(BRAND_1667));
        System.out.println("[PASS] Filter screen opened.");
    }

    public int apply1667FilterAndValidate() {
        clickTextOrClickableAncestor(BRAND_1667_CLICKABLE, BRAND_1667, "1667");
        clickTextOrClickableAncestor(
                SHOW_RESULTS_BUTTON, SHOW_RESULTS_CLICKABLE, SHOW_RESULTS_TEXT,
                "show results");

        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        wait.until(current -> {
            List<ProductInfo> products = readFirstTwoFreshProducts();
            return !products.isEmpty() && products.stream().allMatch(product ->
                    product.accessibilityDescription().toLowerCase(Locale.ROOT)
                            .contains("1667"));
        });

        int filteredCount = getTotalProductCount();
        if (filteredCount <= 0) {
            throw new AssertionError("Filtered product count must be greater than zero.");
        }

        List<ProductInfo> filtered = readFirstTwoFreshProducts();
        for (int index = 0; index < filtered.size(); index++) {
            System.out.println("[FILTERED PRODUCT " + (index + 1) + "] "
                    + filtered.get(index).brand() + " - "
                    + filtered.get(index).productName());
        }
        System.out.println("[PASS] 1667 filter applied. Product count: " + filteredCount);
        return filteredCount;
    }

    public boolean goBackHome() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        Optional<WebElement> back = firstDisplayed(HEADER_BACK_ACCESSIBILITY)
                .or(() -> firstDisplayed(HEADER_BACK_BY_TITLE));

        if (back.isPresent()) {
            back.get().click();
        } else {
            WebElement title = driver.findElement(PLP_TITLE);
            Rectangle box = title.getRect();
            driver.executeScript("mobile: clickGesture", Map.of(
                    "x", Math.max(25, box.getX() - 55),
                    "y", box.getY() + box.getHeight() / 2));
        }

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(MENS_LABEL));
            System.out.println("[PASS] Navigated back to Home page.");
            return true;
        } catch (RuntimeException exception) {
            System.out.println("[FAIL] Home page did not appear after PLP Back click: "
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
        throw new AssertionError("Unable to return to the Men's PLP header.");
    }

    private void scrollPlpUp() {
        WebElement scrollView = wait.until(current -> firstDisplayed(VERTICAL_PLP_SCROLL_VIEW)
                .orElse(null));
        try {
            driver.executeScript("mobile: scrollGesture", Map.of(
                    "elementId", ((RemoteWebElement) scrollView).getId(),
                    "direction", "up",
                    "percent", 0.90));
            return;
        } catch (RuntimeException exception) {
            System.out.println("[WARN] Upward scrollGesture failed; using W3C fallback: "
                    + exception.getMessage());
        }

        Rectangle area = scrollView.getRect();
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
        if (!product.productImageDisplayed()) missing.add("product image");
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
    private boolean scrollPlpDown() {
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
            return true;
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
        return true;
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

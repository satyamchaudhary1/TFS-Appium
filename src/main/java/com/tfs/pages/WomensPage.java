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
 * TC009 complete scope: Women's PLP, Show More twice, Sort, Filter, Back and close.
 * Add to Bag is validated for visibility only and is never clicked.
 */
public class WomensPage extends BasePage {

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

    private static final String CODE_VERSION = "TC009-V4-FAST-STALE-SAFE";
    private static final String APP_PACKAGE = "com.thefragranceshop";
    private static final Duration WAIT = Duration.ofSeconds(30);
    private static final int MAX_SCROLLS_TO_SHOW_MORE = 15;

    private static final By WOMENS_LABEL = By.xpath(
            "//android.widget.TextView[@text=\"Women's\"]");
    private static final By WOMENS_CLICKABLE_CARD = By.xpath(
            "//android.widget.TextView[@text=\"Women's\"]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");
    private static final By WOMENS_CLICKABLE_CARD_FALLBACK = By.xpath(
            "//android.widget.TextView[@text=\"Women's\"]"
                    + "/parent::android.view.ViewGroup"
                    + "/android.view.ViewGroup[@clickable='true'][1]");

    private static final By PLP_TITLE = By.xpath(
            "//android.widget.TextView[@text=\"Women's Fragrances\"]");
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
    private static final By SORT_LOW_TO_HIGH = By.xpath(
            "//android.widget.TextView[@text='Price : low to high']");
    private static final By SORT_LOW_TO_HIGH_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='Price : low to high']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
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
            "Price : low to high",
            "Price : high to low",
            "Product Name : A-Z",
            "Product Name : Z-A",
            "Saving : GBP",
            "Best Seller",
            "Review rating",
            "Release date",
            "Relevance");

    private static final By FILTER_BUTTON = AppiumBy.accessibilityId("Filter");
    private static final By FILTER_SCREEN_TITLE = By.xpath(
            "//android.widget.TextView[@text='Filter']");
    private static final By BRAND_100BON = By.xpath(
            "//android.widget.TextView[@text='100bon']");
    private static final By BRAND_100BON_CLICKABLE = By.xpath(
            "//android.widget.TextView[@text='100bon']"
                    + "/ancestor::android.view.ViewGroup[@clickable='true'][1]");
    private static final By SHOW_RESULTS_BUTTON = AppiumBy.accessibilityId("show results");

    private static final By HEADER_BACK_ACCESSIBILITY = AppiumBy.accessibilityId("Back");
    private static final By HEADER_BACK_BY_TITLE = By.xpath(
            "//android.widget.TextView[@text=\"Women's Fragrances\"]"
                    + "/preceding-sibling::android.view.ViewGroup[@clickable='true'][1]");

    // Currency-symbol independent to avoid Windows encoding issues with £.
    private static final Pattern PRICE = Pattern.compile(
            "(?<!\\d)(\\d+\\.\\d{2})(?!\\d)");

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public WomensPage() {
        driver = DriverManager.getDriver();
        wait = new WebDriverWait(driver, WAIT);
        System.out.println("[TC009 CODE VERSION] " + CODE_VERSION);
    }

    public boolean isHomePageDisplayedQuickly() {
        try {
            return driver.findElements(WOMENS_LABEL).stream().anyMatch(WebElement::isDisplayed);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public boolean isWomensQuickAccessDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(WOMENS_LABEL));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void openWomensCategory() {
        WebElement womensCard = firstDisplayed(WOMENS_CLICKABLE_CARD)
                .or(() -> firstDisplayed(WOMENS_CLICKABLE_CARD_FALLBACK))
                .orElseThrow(() -> new AssertionError(
                        "Women's label was visible but its clickable card was not found."));

        System.out.println("[ACTION] Clicking Women's category from Home page.");
        womensCard.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        wait.until(current -> visibleProductCards().size() >= 2);
        System.out.println("[PASS] Women's PLP opened with products.");
    }

    public boolean isWomensPlpDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE))
                    .isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /** Reads the dynamic numeric value displayed to the right of Women's Fragrances. */
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

        System.out.println("[PASS] Women's total product count: " + count);
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

    public void scrollToPlpTop() {
        System.out.println("[STEP] Returning to the top of Women's PLP.");
        for (int attempt = 0; attempt < 15; attempt++) {
            // After a sort is applied, the Sort button's accessibility label changes
            // from "Sort By" to the selected option. Filter remains stable.
            if (firstDisplayed(PLP_TITLE).isPresent() && firstDisplayed(FILTER_BUTTON).isPresent()) {
                System.out.println("[PASS] Women's PLP top reached.");
                return;
            }
            scrollPlpUp();
        }
        throw new AssertionError("Unable to return to the top of Women's PLP after 15 scrolls.");
    }

    public void openSortAndValidateOptions() {
        scrollToPlpTop();
        wait.until(ExpectedConditions.elementToBeClickable(SORT_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SORT_LOW_TO_HIGH));
        Set<String> actual = new LinkedHashSet<>();
        for (WebElement option : driver.findElements(SORT_OPTIONS)) {
            if (isSafelyDisplayed(option)) actual.add(option.getText().trim());
        }
        List<String> missing = EXPECTED_SORT_OPTIONS.stream()
                .filter(option -> !actual.contains(option)).toList();
        System.out.println("[SORT OPTIONS] " + actual);
        if (!missing.isEmpty()) {
            throw new AssertionError("Sort dropdown is missing option(s): " + missing
                    + ". Displayed options: " + actual);
        }
        System.out.println("[PASS] All expected Sort By options are displayed.");
    }

    public void selectLowToHighAndValidate() {
        clickTextOrClickableAncestor(
                SORT_LOW_TO_HIGH_CLICKABLE, SORT_LOW_TO_HIGH, "Price : low to high");
        wait.until(current -> driver.findElements(SORT_OPTIONS).stream()
                .filter(this::isSafelyDisplayed).count() <= 1);
        wait.until(current -> {
            try {
                List<ProductInfo> currentProducts = readFirstTwoFreshProducts();
                return currentProducts.size() == 2
                        && currentProducts.get(0).memberPrice() >= 0
                        && currentProducts.get(1).memberPrice() >= 0
                        && currentProducts.get(0).memberPrice()
                        <= currentProducts.get(1).memberPrice();
            } catch (StaleElementReferenceException exception) {
                return false;
            }
        });
        int count = getTotalProductCount();
        List<ProductInfo> products = readFirstTwoFreshProducts();
        if (count <= 0 || products.size() < 2
                || products.stream().anyMatch(product -> product.memberPrice() < 0)) {
            throw new AssertionError("Product count or first two prices were unavailable after sorting.");
        }
        if (products.get(0).memberPrice() > products.get(1).memberPrice()) {
            throw new AssertionError("Low-to-high sort failed. First prices: "
                    + products.get(0).memberPrice() + " then " + products.get(1).memberPrice());
        }
        System.out.println("[PASS] Low-to-high sorting verified: "
                + products.get(0).memberPrice() + " <= " + products.get(1).memberPrice()
                + "; count=" + count);
    }

    public void openFilter() {
        scrollToPlpTop();
        wait.until(ExpectedConditions.elementToBeClickable(FILTER_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(FILTER_SCREEN_TITLE));
        wait.until(ExpectedConditions.visibilityOfElementLocated(BRAND_100BON));
        System.out.println("[PASS] Filter screen opened.");
    }

    public int apply100bonFilterAndValidate() {
        clickTextOrClickableAncestor(BRAND_100BON_CLICKABLE, BRAND_100BON, "100bon");
        wait.until(ExpectedConditions.elementToBeClickable(SHOW_RESULTS_BUTTON)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        List<String> filteredProducts = wait.until(current -> {
            List<String> descriptions = visibleProductCards().stream()
                    .limit(2)
                    .map(card -> attribute(card, "content-desc"))
                    .filter(description -> !description.isBlank())
                    .toList();
            boolean filtered = !descriptions.isEmpty() && descriptions.stream()
                    .allMatch(description -> description.toLowerCase(Locale.ROOT)
                            .contains("100bon"));
            return filtered ? descriptions : null;
        });
        int count = getTotalProductCount();
        if (count <= 0) {
            throw new AssertionError("100bon filtered product count must be greater than zero.");
        }
        System.out.println("[PASS] 100bon filter verified. count=" + count
                + ", checked cards=" + filteredProducts.size());
        return count;
    }

    public boolean goBackHome() {
        // Filter results already open at the PLP header. Do not scroll again.
        wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
        System.out.println("[ACTION] Clicking Women's PLP header back icon.");
        Optional<WebElement> back = firstDisplayed(HEADER_BACK_ACCESSIBILITY)
                .or(() -> firstDisplayed(HEADER_BACK_BY_TITLE));
        if (back.isPresent()) {
            back.get().click();
        } else {
            WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(PLP_TITLE));
            Rectangle box = title.getRect();
            driver.executeScript("mobile: clickGesture", Map.of(
                    "x", Math.max(25, box.getX() - 55),
                    "y", box.getY() + box.getHeight() / 2));
        }
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(WOMENS_LABEL));
            System.out.println("[PASS] Returned to Home page.");
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public boolean closeApplication() {
        boolean terminated = driver.terminateApp(APP_PACKAGE);
        ApplicationState state = driver.queryAppState(APP_PACKAGE);
        System.out.println("[APP CLOSE] terminated=" + terminated + ", state=" + state);
        return state != ApplicationState.RUNNING_IN_FOREGROUND;
    }

    private void clickTextOrClickableAncestor(By clickable, By text, String name) {
        WebElement element = firstDisplayed(clickable).or(() -> firstDisplayed(text))
                .orElseThrow(() -> new AssertionError(name + " was not displayed."));
        try {
            element.click();
        } catch (RuntimeException exception) {
            Rectangle box = element.getRect();
            driver.executeScript("mobile: clickGesture", Map.of(
                    "x", box.getX() + box.getWidth() / 2,
                    "y", box.getY() + box.getHeight() / 2));
        }
        System.out.println("[ACTION] Selected " + name + ".");
    }

    private void scrollPlpUp() {
        WebElement scrollView = wait.until(current -> firstDisplayed(VERTICAL_PLP_SCROLL_VIEW)
                .orElse(null));
        try {
            driver.executeScript("mobile: scrollGesture", Map.of(
                    "elementId", ((RemoteWebElement) scrollView).getId(),
                    "direction", "up", "percent", 0.90));
        } catch (RuntimeException exception) {
            Rectangle area = scrollView.getRect();
            int x = area.getX() + area.getWidth() / 2;
            performW3cFingerDrag(x,
                    area.getY() + (int) (area.getHeight() * 0.22), x,
                    area.getY() + (int) (area.getHeight() * 0.82));
        }
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
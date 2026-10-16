package com.tfs.pages;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page object for TC031 - Home "Men's" tile -> Men's Fragrances PLP -> 5th
 * product -> PDP (MyTFS membership, first variant, gift options) -> basket
 * (read, quantity 2) -> Secure Checkout -> Express Delivery -> Pay by Card ->
 * Credit Card details -> Pay (only when payment.submit.enabled=true) -> order
 * confirmation -> close app.
 * <p>
 * Built on the helpers of SideMenuBrandsPage.java (tap-and-verify, scroll into
 * view, tap-without-retry for selections).
 * <p>
 * FLAGGED ASSUMPTIONS:
 *  - REAL MONEY GUARD: Pay is tapped ONLY when framework.properties has
 *    payment.submit.enabled=true. Otherwise the card details are entered, the
 *    Pay button text (with its amount) is printed, and the test carries on.
 *  - Product cards expose no ids. The Nth product is found by counting cards in
 *    the order they appear, each identified by its first three text lines
 *    (brand, name, description), read from one page-source snapshot per scroll.
 *  - Radio circles (gift options, Express Delivery, payment) are bare icons
 *    (class com.horcrux.svg.d). The one nearest to its label is tapped; the
 *    payment radio is the first circle below the "Payment Method" heading.
 *  - The membership info icon is the small icon on the same row as the
 *    "Member Price" text.
 *  - Variant / Peach / Medium / Delivery taps may legitimately show no visible
 *    change (already selected), so they do not fail the test; the radio taps
 *    are made ONCE and never retried (a retry would deselect them).
 *  - The Express Delivery label is matched by "Express"; its exact text is not
 *    in the captured data.
 *  - The Order Confirmation screen may be a web page the app tree cannot read;
 *    the test prints whatever is visible and warns instead of failing.
 *  - The card fields use their real resource ids (editText_cardNumber,
 *    editText_expiryDate, editText_securityCode, payButton).
 */
public class LoginUserBasketPage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    private static final int SEARCH_NAV_ICON_X1 = 276;
    private static final int SEARCH_NAV_ICON_Y1 = 2341;
    private static final int SEARCH_NAV_ICON_X2 = 499;
    private static final int SEARCH_NAV_ICON_Y2 = 2577;

    private static final By SEARCH_FIELD =
            AppiumBy.xpath("//android.widget.EditText[@hint='Search']");

    // The PLP product to open: 3rd row, 5th product.
    private static final int PRODUCT_POSITION = 5;

    // Step 1 - Home top-stories tile label (the image sits above it).
    private static final By HOME_TILE_MENS = AppiumBy.xpath("//android.widget.TextView[@text=\"Men's\"]");
    private static final By MENS_PLP_TITLE = AppiumBy.xpath("//android.widget.TextView[@text=\"Men's Fragrances\"]");

    // PDP.
    private static final By PDP_MEMBER_PRICE_ROW = AppiumBy.xpath(
            "//android.widget.TextView[contains(@text,'Member Price') and not(contains(@text,'Non'))]");
    private static final By PDP_MEMBERS_PRICE_BOX = AppiumBy.xpath("//android.widget.TextView[contains(@text,'Members Price')]");
    private static final By ADD_MEMBERSHIP = AppiumBy.accessibilityId("Add Membership");
    private static final By FIRST_VARIANT = AppiumBy.xpath(
            "(//android.view.ViewGroup[@clickable='true' and contains(@content-desc,'per ml')])[1]");
    private static final By GIFT_OPTIONS = AppiumBy.accessibilityId("Gift Options");
    private static final By GIFT_WINDOW_TITLE = AppiumBy.xpath("//android.widget.TextView[contains(@text,'Why not pair')]");
    private static final By PEACH_BAG = AppiumBy.accessibilityId("Premium Peach Gift Bag");
    private static final By GIFT_BAGS_LABEL = AppiumBy.xpath("//android.widget.TextView[@text='Gift Bags']");
    private static final By MEDIUM_SIZE = AppiumBy.accessibilityId("Medium");
    private static final By CONFIRM = AppiumBy.accessibilityId("Confirm");
    private static final By ADD_AND_CONTINUE = AppiumBy.accessibilityId("Add and Continue");

    // Basket.
    private static final By BASKET_TITLE = AppiumBy.xpath("//android.widget.TextView[@text='Your Bag']");
    private static final By QTY_DROPDOWN = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='1' and @clickable='true']");
    private static final By QTY_TWO = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='2' and @clickable='true']");
    private static final By SECURE_CHECKOUT = AppiumBy.accessibilityId("Secure Checkout");

    // Checkout.
    private static final By CHECKOUT_DETAILS = AppiumBy.xpath("//android.widget.TextView[contains(@text,'Your Details')]");
    private static final By DELIVERY_OPTION = AppiumBy.accessibilityId("Delivery, To an address of my choice");
    private static final By EXPRESS_LABEL = AppiumBy.xpath("//android.widget.TextView[contains(@text,'Express')]");
    private static final By PAYMENT_HEADING = AppiumBy.xpath("//android.widget.TextView[contains(@text,'Payment Method')]");
    private static final By CONTINUE_BUTTON = AppiumBy.accessibilityId("Continue");

    // Payment sheet and card form (native). Resource ids are from the captured JSON.
    private static final By PAYMENT_SHEET_TITLE = AppiumBy.xpath("//android.widget.TextView[@text='PAYMENT METHODS']");
    private static final By CREDIT_CARD_ROW = textAncestorLocator("Credit Card");
    private static final String CARD_NUMBER_ID = "com.thefragranceshop:id/editText_cardNumber";
    private static final String EXPIRY_ID = "com.thefragranceshop:id/editText_expiryDate";
    private static final String CVC_ID = "com.thefragranceshop:id/editText_securityCode";
    private static final String PAY_BUTTON_ID = "com.thefragranceshop:id/payButton";

    private static final String APP_PACKAGE = "com.thefragranceshop";

    private static By textAncestorLocator(String exactText) {
        if (exactText.contains("'")) {
            return AppiumBy.xpath("//android.widget.TextView[@text=\"" + exactText
                    + "\"]/ancestor::*[@clickable='true'][1]");
        }
        return AppiumBy.xpath("//android.widget.TextView[@text='" + exactText
                + "']/ancestor::*[@clickable='true'][1]");
    }

    private static By textContainsAncestorLocator(String partialText) {
        return AppiumBy.xpath("//android.widget.TextView[contains(@text,'" + partialText
                + "')]/ancestor::*[@clickable='true'][1]");
    }

    public LoginUserBasketPage() {
        this(com.tfs.driver.DriverManager.getDriver());
    }

    public LoginUserBasketPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new FluentWait<>(driver)
                .withTimeout(TIMEOUT)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    // ---------------------------------------------------------------
    // Step 1 - Tap the Side Menu / Search icon
    // ---------------------------------------------------------------
    public void openSideMenu() {
        System.out.println("[STEP 01] Tap the Side Menu icon on the bottom navigation bar");
        tapAtScaledCoordinates(SEARCH_NAV_ICON_X1, SEARCH_NAV_ICON_Y1, SEARCH_NAV_ICON_X2, SEARCH_NAV_ICON_Y2,
                REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
        System.out.println("[ACTION] Tapped the Side Menu icon.");
        waitForElement(SEARCH_FIELD, 8);
    }

    // ===============================================================
    // Page-source helpers: one call returns every visible node with its bounds, which is far
    // faster and steadier than many getRect() calls (essential for card and radio detection).
    // ===============================================================
    private static final class TextNode {
        final String cls;
        final String text;
        final String desc;
        final int x1;
        final int y1;
        final int x2;
        final int y2;

        TextNode(String cls, String text, String desc, int x1, int y1, int x2, int y2) {
            this.cls = cls;
            this.text = text == null ? "" : text;
            this.desc = desc == null ? "" : desc;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        int cx() {
            return (x1 + x2) / 2;
        }

        int cy() {
            return (y1 + y2) / 2;
        }

        int width() {
            return x2 - x1;
        }

        int height() {
            return y2 - y1;
        }

        boolean isText() {
            return "android.widget.TextView".equals(cls) && !text.trim().isEmpty();
        }
    }

    private List<TextNode> readNodes() {
        List<TextNode> out = new ArrayList<>();
        try {
            String xml = driver.getPageSource();
            javax.xml.parsers.DocumentBuilder builder =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(
                    new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            org.w3c.dom.NodeList all = doc.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) all.item(i);
                String bounds = el.getAttribute("bounds");
                if (bounds == null || bounds.isEmpty()) {
                    continue;
                }
                String[] p = bounds.replace("][", ",").replace("[", "").replace("]", "").split(",");
                if (p.length != 4) {
                    continue;
                }
                out.add(new TextNode(el.getAttribute("class"), el.getAttribute("text"),
                        el.getAttribute("content-desc"),
                        Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()),
                        Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim())));
            }
        } catch (Exception e) {
            System.out.println("[WARN] Could not read the page source: " + e.getMessage());
        }
        return out;
    }

    private TextNode findTextNode(List<TextNode> nodes, String contains) {
        for (TextNode n : nodes) {
            if (n.isText() && n.text.contains(contains)) {
                return n;
            }
        }
        return null;
    }

    /** Taps the circle (radio) icon that sits to the left of the given label, once, with no retry. */
    private void tapRadioNextToLabel(String labelContains, String label) {
        List<TextNode> nodes = readNodes();
        TextNode lab = findTextNode(nodes, labelContains);
        if (lab == null) {
            printAllVisibleTextAndDescs("Screen state - label '" + labelContains + "' not found");
            throw new org.openqa.selenium.TimeoutException("Label '" + labelContains + "' was not found for '" + label + "'.");
        }
        TextNode radio = null;
        int best = Integer.MAX_VALUE;
        for (TextNode n : nodes) {
            if ("com.horcrux.svg.d".equals(n.cls) && n.width() >= 30 && n.width() <= 80 && n.cx() < lab.cx()) {
                int dy = Math.abs(n.cy() - lab.cy());
                if (dy < 110 && dy < best) {
                    best = dy;
                    radio = n;
                }
            }
        }
        int x;
        int y;
        if (radio != null) {
            x = radio.cx();
            y = radio.cy();
            System.out.println("[ACTION] Tapping the '" + label + "' radio circle at (" + x + "," + y + ") - once, no retry.");
        } else {
            x = (int) (driver.manage().window().getSize().width * 0.09);
            y = lab.cy();
            System.out.println("[WARN] No radio icon found next to '" + label + "' - tapping where it normally sits ("
                    + x + "," + y + ").");
        }
        tapAtRealCoordinates(x, y);
        pause(700);
    }

    /** Taps the first radio circle on the left edge that sits below the given heading. */
    private void tapFirstRadioBelow(String headingContains, String label) {
        List<TextNode> nodes = readNodes();
        TextNode heading = findTextNode(nodes, headingContains);
        if (heading == null) {
            printAllVisibleTextAndDescs("Screen state - heading '" + headingContains + "' not found");
            throw new org.openqa.selenium.TimeoutException("Heading '" + headingContains + "' was not found.");
        }
        int width = driver.manage().window().getSize().width;
        TextNode radio = null;
        for (TextNode n : nodes) {
            if ("com.horcrux.svg.d".equals(n.cls) && n.width() >= 30 && n.width() <= 80
                    && n.cx() < width * 0.15 && n.cy() > heading.cy()) {
                if (radio == null || n.cy() < radio.cy()) {
                    radio = n;
                }
            }
        }
        if (radio == null) {
            printAllVisibleTextAndDescs("Screen state - no radio circle found below '" + headingContains + "'");
            throw new org.openqa.selenium.TimeoutException("No radio circle found below '" + headingContains + "'.");
        }
        System.out.println("[ACTION] Tapping the '" + label + "' radio circle at (" + radio.cx() + "," + radio.cy()
                + ") - once, no retry.");
        tapAtRealCoordinates(radio.cx(), radio.cy());
        pause(700);
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isCardNoise(String t) {
        String s = t.trim();
        if (s.isEmpty() || s.equals("Add to Bag") || s.equals("i") || s.startsWith("From") || s.startsWith("Save")
                || s.startsWith("Was") || s.contains("\u00a3") || s.contains("Member Price")
                || s.contains("Non-Member")) {
            return true;
        }
        if (s.startsWith("(") && s.endsWith(")")) {
            return true;
        }
        String upper = s.toUpperCase();
        return upper.equals("SPONSORED") || upper.equals("NEW IN") || upper.equals("FREE GIFT")
                || upper.equals("TRY IT FIRST") || upper.equals("SALE") || upper.equals("IMAGE UNAVAILABLE");
    }

    private static final class CardRef {
        final String id;
        final int col;
        final int addCy;
        final int brandX;
        final int brandY;

        CardRef(String id, int col, int addCy, int brandX, int brandY) {
            this.id = id;
            this.col = col;
            this.addCy = addCy;
            this.brandX = brandX;
            this.brandY = brandY;
        }
    }

    /** One snapshot of the product cards on screen, in reading order (row by row, left to right). */
    private List<CardRef> snapshotCards() {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int window = (int) (1150.0 * size.height / REF_SCREEN_HEIGHT);
        List<TextNode> nodes = readNodes();
        List<CardRef> cards = new ArrayList<>();
        for (TextNode add : nodes) {
            if (!add.isText() || !add.text.trim().equals("Add to Bag")) {
                continue;
            }
            int col = add.cx() < size.width / 2 ? 0 : 1;
            List<TextNode> lines = new ArrayList<>();
            for (TextNode n : nodes) {
                if (!n.isText() || isCardNoise(n.text)) {
                    continue;
                }
                boolean sameColumn = (n.cx() < size.width / 2 ? 0 : 1) == col;
                if (sameColumn && n.cy() < add.cy() && n.cy() > add.cy() - window) {
                    lines.add(n);
                }
            }
            if (lines.isEmpty()) {
                continue;
            }
            lines.sort(java.util.Comparator.comparingInt(TextNode::cy));
            StringBuilder id = new StringBuilder();
            for (int i = 0; i < Math.min(3, lines.size()); i++) {
                if (i > 0) {
                    id.append(" | ");
                }
                id.append(lines.get(i).text.trim());
            }
            cards.add(new CardRef(id.toString(), col, add.cy(), lines.get(0).cx(), lines.get(0).cy()));
        }
        // reading order: group cards whose Add to Bag sit within 150px into one row, then left to right
        cards.sort(java.util.Comparator.comparingInt((CardRef c) -> c.addCy));
        List<CardRef> ordered = new ArrayList<>();
        int i = 0;
        while (i < cards.size()) {
            int rowY = cards.get(i).addCy;
            List<CardRef> row = new ArrayList<>();
            while (i < cards.size() && Math.abs(cards.get(i).addCy - rowY) < 150) {
                row.add(cards.get(i));
                i++;
            }
            row.sort(java.util.Comparator.comparingInt((CardRef c) -> c.col));
            ordered.addAll(row);
        }
        return ordered;
    }

    // ---------------------------------------------------------------
    // Step 1 - Tap the "Men's" tile in the Home top stories
    // ---------------------------------------------------------------
    public void selectMenCategoryFromHome() {
        System.out.println("[STEP 01] Select the Men's category from the Home top stories");
        waitForElement(HOME_TILE_MENS, 10);
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        for (int attempt = 1; attempt <= 3; attempt++) {
            org.openqa.selenium.Rectangle label = driver.findElement(HOME_TILE_MENS).getRect();
            // The tile image sits directly above its label; the label itself is not the touch target.
            int tapX = label.getX() + label.getWidth() / 2;
            int tapY = label.getY() - (int) (size.height * 0.045);
            System.out.println("[ACTION] Tapping the Men's tile image at (" + tapX + "," + tapY + ") (attempt "
                    + attempt + "/3).");
            tapAtRealCoordinates(tapX, tapY);
            if (waitForElement(MENS_PLP_TITLE, 10)) {
                System.out.println("[PASS] The Men's Fragrances PLP is displayed.");
                return;
            }
            System.out.println("[WARN] The Men's Fragrances PLP did not open (attempt " + attempt + "/3).");
        }
        printAllVisibleTextAndDescs("Screen state after failing to open Men's Fragrances");
        throw new org.openqa.selenium.TimeoutException("The Men's Fragrances PLP did not open after 3 taps on the tile.");
    }

    // ---------------------------------------------------------------
    // Step 2 - Read the Men's Fragrances PLP (top of the page)
    // ---------------------------------------------------------------
    public void readMensPlp() {
        System.out.println("[STEP 02] Check the Men's Fragrances PLP with Sort by and Filter");
        waitForElement(AppiumBy.accessibilityId("Sort by"), 10);
        System.out.println((isDisplayedSafely(AppiumBy.accessibilityId("Sort by")) ? "[PASS]" : "[WARN]") + " Sort by option shown.");
        System.out.println((isDisplayedSafely(AppiumBy.accessibilityId("Filter")) ? "[PASS]" : "[WARN]") + " Filter option shown.");
        printCollected("Men's Fragrances PLP (top of the page)", new java.util.LinkedHashSet<>(collectVisibleText()));
    }

    // ---------------------------------------------------------------
    // Step 3 - Scroll to the Nth product (row 3, 5th product) and open it
    // ---------------------------------------------------------------
    public void selectProductByPosition(int position) {
        System.out.println("[STEP 03] Scroll down to row " + ((position + 1) / 2) + " and select product #" + position);
        java.util.LinkedHashSet<String> order = new java.util.LinkedHashSet<>();
        String targetId = null;
        for (int i = 0; i < 25 && targetId == null; i++) {
            for (CardRef card : snapshotCards()) {
                order.add(card.id);
                if (order.size() >= position) {
                    targetId = new ArrayList<>(order).get(position - 1);
                    break;
                }
            }
            if (targetId == null) {
                scrollDown();
            }
        }
        if (targetId == null) {
            printAllVisibleTextAndDescs("Screen state - product #" + position + " was never reached");
            throw new org.openqa.selenium.TimeoutException("Product #" + position + " was not found on the PLP.");
        }
        System.out.println("[INFO] Products seen in order: " + order);
        System.out.println("[RESULT] Product #" + position + " is: " + targetId);
        if (!targetId.toUpperCase().contains("CHANEL")) {
            System.out.println("[WARN] The captured JSON for this step was the CHANEL product - the list may have changed.");
        }

        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        for (int attempt = 1; attempt <= 3; attempt++) {
            CardRef target = null;
            for (int s = 0; s < 4 && target == null; s++) {
                for (CardRef c : snapshotCards()) {
                    if (c.id.equals(targetId)) {
                        target = c;
                        break;
                    }
                }
                if (target == null) {
                    scrollDownGently();
                }
            }
            if (target == null) {
                break;
            }
            if (target.brandY > size.height * 0.70) {
                scrollDownGently();
                continue;
            }
            System.out.println("[ACTION] Tapping product #" + position + " at (" + target.brandX + "," + target.brandY
                    + ") (attempt " + attempt + "/3).");
            tapAtRealCoordinates(target.brandX, target.brandY);
            if (!waitForElementGone(MENS_PLP_TITLE, 10)) {
                System.out.println("[WARN] Still on the PLP after tapping the product (attempt " + attempt + "/3).");
                continue;
            }
            System.out.println("[PASS] The product page opened.");
            return;
        }
        printAllVisibleTextAndDescs("Screen state after failing to open product #" + position);
        throw new org.openqa.selenium.TimeoutException("Product #" + position + " did not open after 3 attempts.");
    }

    private boolean waitForElementGone(By locator, int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> !isDisplayedSafely(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private java.util.LinkedHashSet<String> readPageFully(int maxScrolls) {
        java.util.LinkedHashSet<String> seen = new java.util.LinkedHashSet<>(collectVisibleText());
        int quiet = 0;
        for (int i = 0; i < maxScrolls; i++) {
            int before = seen.size();
            scrollDown();
            seen.addAll(collectVisibleText());
            if (seen.size() == before) {
                quiet++;
                if (quiet >= 2) {
                    break;
                }
            } else {
                quiet = 0;
            }
        }
        return seen;
    }

    // ---------------------------------------------------------------
    // Step 5 - Read the PDP, then tap the info icon next to the member price
    // ---------------------------------------------------------------
    public void readPdpAndOpenMembershipInfo() {
        System.out.println("[STEP 05] Read the product page, scroll down a little, tap the member-price info icon");
        waitForElement(PDP_MEMBERS_PRICE_BOX, 20);
        printCollected("PDP (full)", readPageFully(8));
        scrollToTop();

        scrollIntoUpperPart(PDP_MEMBER_PRICE_ROW, 0.55, 6);
        for (int attempt = 1; attempt <= 3; attempt++) {
            int[] icon = findMemberPriceInfoIcon();
            if (icon == null) {
                printAllVisibleTextAndDescs("Screen state - member price row not found");
                throw new org.openqa.selenium.TimeoutException("The 'Member Price' row was not found on the PDP.");
            }
            System.out.println("[ACTION] Tapping the member-price info icon at (" + icon[0] + "," + icon[1]
                    + ") (attempt " + attempt + "/3).");
            tapAtRealCoordinates(icon[0], icon[1]);
            if (waitForElement(ADD_MEMBERSHIP, 6)) {
                System.out.println("[PASS] The MyTFS Membership window is open.");
                return;
            }
            System.out.println("[WARN] The Membership window did not open (attempt " + attempt + "/3).");
        }
        printAllVisibleTextAndDescs("Screen state after failing to open the Membership window");
        throw new org.openqa.selenium.TimeoutException("The Membership window did not open after 3 attempts.");
    }

    /** The info icon is the small icon (about 45px) on the same row as the "Member Price" text, right side. */
    private int[] findMemberPriceInfoIcon() {
        List<TextNode> nodes = readNodes();
        TextNode row = null;
        for (TextNode n : nodes) {
            if (n.isText() && n.text.contains("Member Price") && !n.text.contains("Non")) {
                row = n;
                break;
            }
        }
        if (row == null) {
            return null;
        }
        int width = driver.manage().window().getSize().width;
        TextNode best = null;
        for (TextNode n : nodes) {
            if (n.cls.startsWith("com.horcrux.svg") && n.width() >= 30 && n.width() <= 70
                    && n.height() >= 30 && n.height() <= 70
                    && n.cx() > width * 0.7 && Math.abs(n.cy() - row.cy()) < 70) {
                if (best == null || Math.abs(n.cy() - row.cy()) < Math.abs(best.cy() - row.cy())) {
                    best = n;
                }
            }
        }
        if (best != null) {
            return new int[]{best.cx(), best.cy()};
        }
        System.out.println("[WARN] No icon element found on the 'Member Price' row - tapping where it normally sits.");
        return new int[]{(int) (width * 0.83), row.cy()};
    }

    // ---------------------------------------------------------------
    // Step 6 - Add Membership
    // ---------------------------------------------------------------
    public void addMembership() {
        System.out.println("[STEP 06] Select 'Add Membership'");
        printCollected("Membership window", new java.util.LinkedHashSet<>(collectVisibleText()));
        tapConfirmedButtonWithVerification(ADD_MEMBERSHIP, "Add Membership");
        if (waitForElementGone(ADD_MEMBERSHIP, 8)) {
            System.out.println("[PASS] The Membership window closed.");
        } else {
            System.out.println("[WARN] The Membership window is still showing.");
        }
    }

    // ---------------------------------------------------------------
    // Step 7 - Select the first variant
    // ---------------------------------------------------------------
    public void selectFirstVariant() {
        System.out.println("[STEP 07] Select the first variant");
        scrollIntoUpperPart(FIRST_VARIANT, 0.6, 6);
        tapSoft(FIRST_VARIANT, "First variant", 2);
        printCollected("PDP after selecting the first variant", new java.util.LinkedHashSet<>(collectVisibleText()));
    }

    // ---------------------------------------------------------------
    // Step 7b - Scroll down a little and open Gift Options
    // ---------------------------------------------------------------
    public void openGiftOptions() {
        System.out.println("[STEP 07b] Scroll down a little, check Gift Options and open it");
        scrollIntoUpperPart(GIFT_OPTIONS, 0.6, 6);
        tapConfirmedButtonWithVerification(GIFT_OPTIONS, "Gift Options");
        if (waitForElement(GIFT_WINDOW_TITLE, 8)) {
            System.out.println("[PASS] The Gift Options window is open.");
            printCollected("Gift Options window", new java.util.LinkedHashSet<>(collectVisibleText()));
        } else {
            printAllVisibleTextAndDescs("Screen state - Gift Options window not detected");
            throw new org.openqa.selenium.TimeoutException("The Gift Options window did not open.");
        }
    }

    // ---------------------------------------------------------------
    // Steps 8-10 - Occasion Bag -> Premium Peach Gift Bag -> Confirm
    // ---------------------------------------------------------------
    public void selectOccasionBagPeach() {
        System.out.println("[STEP 08] Select 'Occasion Bag'");
        tapRadioNextToLabel("Occasion Bag", "Occasion Bag");
        if (!waitForElement(PEACH_BAG, 8)) {
            printAllVisibleTextAndDescs("Screen state - gift bag choices did not appear");
            throw new org.openqa.selenium.TimeoutException("The Occasion Bag choices did not appear.");
        }

        System.out.println("[STEP 09] Select 'Premium Peach Gift Bag'");
        tapSoft(PEACH_BAG, "Premium Peach Gift Bag", 2);

        System.out.println("[STEP 10] Select 'Confirm'");
        tapConfirmedButtonWithVerification(CONFIRM, "Confirm (Occasion Bag)");
        System.out.println("[RESULT] Order Total after the Occasion Bag: " + readOrderTotalText());
    }

    // ---------------------------------------------------------------
    // Steps 11-13 - Gift Bags -> Medium -> Confirm
    // ---------------------------------------------------------------
    public void selectGiftBagMedium() {
        System.out.println("[STEP 11] Scroll the gift list and select 'Gift Bags'");
        scrollIntoUpperPart(GIFT_BAGS_LABEL, 0.6, 8);
        tapRadioNextToLabel("Gift Bags", "Gift Bags");
        if (!waitForElement(MEDIUM_SIZE, 8)) {
            printAllVisibleTextAndDescs("Screen state - gift bag sizes did not appear");
            throw new org.openqa.selenium.TimeoutException("The Gift Bags size choices did not appear.");
        }

        System.out.println("[STEP 12] Select the 'Medium' size");
        tapSoft(MEDIUM_SIZE, "Medium", 2);

        System.out.println("[STEP 13] Select 'Confirm'");
        tapConfirmedButtonWithVerification(CONFIRM, "Confirm (Gift Bags)");
    }

    // ---------------------------------------------------------------
    // Step 15 - Print the Order Total, tap "Add and Continue"
    // ---------------------------------------------------------------
    public void printOrderTotalAndAddAndContinue() {
        System.out.println("[STEP 15] Check the Order Total and select 'Add and Continue'");
        System.out.println("[RESULT] Order Total: " + readOrderTotalText());
        tapConfirmedButtonWithVerification(ADD_AND_CONTINUE, "Add and Continue");
        if (waitForElement(BASKET_TITLE, 20)) {
            System.out.println("[PASS] The basket is displayed.");
        } else {
            printAllVisibleTextAndDescs("Screen state - basket not detected");
            throw new org.openqa.selenium.TimeoutException("The basket did not open after 'Add and Continue'.");
        }
    }

    /** The amount on the same row as the "Order Total" label. */
    private String readOrderTotalText() {
        List<TextNode> nodes = readNodes();
        TextNode label = null;
        for (TextNode n : nodes) {
            if (n.isText() && n.text.trim().equalsIgnoreCase("Order Total")) {
                label = n;
                break;
            }
        }
        if (label == null) {
            return "n/a (no 'Order Total' label on screen)";
        }
        TextNode best = null;
        for (TextNode n : nodes) {
            if (n.isText() && n.text.contains("\u00a3") && Math.abs(n.cy() - label.cy()) < 70 && n.cx() > label.cx()) {
                if (best == null || Math.abs(n.cx() - label.cx()) < Math.abs(best.cx() - label.cx())) {
                    best = n;
                }
            }
        }
        return best == null ? "n/a (no amount next to the label)" : best.text.trim();
    }

    // ---------------------------------------------------------------
    // Steps 16 / 19 - Read the whole basket, print it and its Order Summary
    // ---------------------------------------------------------------
    public void readBasketFully(String label) {
        System.out.println("[STEP 16/19] Read the whole basket (" + label + ")");
        waitForElement(BASKET_TITLE, 10);
        java.util.LinkedHashSet<String> content = readPageFully(25);
        printCollected(label + " - full content", content);
        printSection(new ArrayList<>(content), "Order Summary", "Secure Checkout", label + " - Order Summary");
        scrollToTop();
    }

    private void printSection(List<String> lines, String startMarker, String endMarker, String title) {
        int start = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(startMarker)) {
                start = i;
                break;
            }
        }
        if (start < 0) {
            System.out.println("[WARN] '" + startMarker + "' was not found in the captured content.");
            return;
        }
        int end = lines.size();
        for (int i = start + 1; i < lines.size(); i++) {
            if (lines.get(i).contains(endMarker)) {
                end = i;
                break;
            }
        }
        printCollected(title, new ArrayList<>(lines.subList(start, end)));
    }

    // ---------------------------------------------------------------
    // Steps 17-18 - Quantity drop-down -> 2
    // ---------------------------------------------------------------
    public void changeQuantityToTwo() {
        System.out.println("[STEP 17] Scroll up and open the Quantity drop-down");
        scrollToTop();
        int dropdownY = driver.findElement(QTY_DROPDOWN).getRect().getY();
        tapConfirmedButtonWithVerification(QTY_DROPDOWN, "Quantity drop-down");

        System.out.println("[STEP 18] Select quantity 2");
        int width = driver.manage().window().getSize().width;
        TextNode two = null;
        for (TextNode n : readNodes()) {
            if (n.isText() && n.text.trim().equals("2") && n.cx() < width * 0.35 && n.cy() > dropdownY) {
                if (two == null || n.cy() < two.cy()) {
                    two = n;
                }
            }
        }
        if (two == null) {
            printAllVisibleTextAndDescs("Screen state - quantity option 2 not found");
            throw new org.openqa.selenium.TimeoutException("The quantity option '2' was not found in the drop-down.");
        }
        System.out.println("[ACTION] Tapping quantity '2' at (" + two.cx() + "," + two.cy() + ").");
        tapAtRealCoordinates(two.cx(), two.cy());
        if (waitForElement(QTY_TWO, 8)) {
            System.out.println("[PASS] The quantity now shows 2.");
        } else {
            System.out.println("[WARN] The quantity drop-down does not show 2 yet.");
        }
        pause(2500);
    }

    // ---------------------------------------------------------------
    // Step 20 - Secure Checkout
    // ---------------------------------------------------------------
    public void secureCheckout() {
        System.out.println("[STEP 20] Select 'Secure Checkout'");
        scrollIntoUpperPart(SECURE_CHECKOUT, 0.7, 12);
        tapConfirmedButtonWithVerification(SECURE_CHECKOUT, "Secure Checkout");
        if (waitForElement(CHECKOUT_DETAILS, 40)) {
            System.out.println("[PASS] The checkout page is displayed.");
        } else {
            printAllVisibleTextAndDescs("Screen state - checkout page not detected");
            throw new org.openqa.selenium.TimeoutException("The checkout page did not open.");
        }
    }

    // ---------------------------------------------------------------
    // Step 21 - Read the whole checkout page
    // ---------------------------------------------------------------
    public void readCheckoutFully() {
        System.out.println("[STEP 21] Read the whole checkout page (payment methods, order summary, order total)");
        java.util.LinkedHashSet<String> content = readPageFully(20);
        printCollected("Checkout page - full content", content);
        List<String> lines = new ArrayList<>(content);
        printSection(lines, "Payment Method", "Order Summary", "Checkout - Payment Method");
        printSection(lines, "Order Summary", "Promotion Code", "Checkout - Order Summary");
        scrollToTop();
    }

    // ---------------------------------------------------------------
    // Steps 22-24 - Delivery -> Express Delivery -> Confirm
    // ---------------------------------------------------------------
    public void selectExpressDelivery() {
        System.out.println("[STEP 22] Scroll up and select 'Delivery'");
        scrollToTop();
        tapSoft(DELIVERY_OPTION, "Delivery", 2);

        System.out.println("[STEP 23] Select 'Express Delivery'");
        if (!waitForElement(EXPRESS_LABEL, 8)) {
            printAllVisibleTextAndDescs("Screen state - Express Delivery option not found");
            throw new org.openqa.selenium.TimeoutException("The Express Delivery option did not appear.");
        }
        scrollIntoUpperPart(EXPRESS_LABEL, 0.6, 4);
        tapRadioNextToLabel("Express", "Express Delivery");

        System.out.println("[STEP 24] Select 'Confirm' for Express Delivery");
        tapConfirmedButtonWithVerification(CONFIRM, "Confirm (Express Delivery)");
    }

    // ---------------------------------------------------------------
    // Steps 25-26 - Pay by Card -> Continue (opens the payment sheet)
    // ---------------------------------------------------------------
    public void selectPayByCardAndContinue() {
        System.out.println("[STEP 25] Select the Pay by Card payment method");
        scrollIntoUpperPart(PAYMENT_HEADING, 0.4, 10);
        tapFirstRadioBelow("Payment Method", "Pay by Card");

        System.out.println("[STEP 26] Select 'Continue' to open the payment sheet");
        for (int attempt = 1; attempt <= 3; attempt++) {
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_BUTTON));
            org.openqa.selenium.Rectangle r = el.getRect();
            System.out.println("[ACTION] Tapping 'Continue' at its live bounds (attempt " + attempt + "/3).");
            tapAtRealCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
            if (waitForElement(PAYMENT_SHEET_TITLE, 20)) {
                System.out.println("[PASS] The payment methods sheet is open.");
                return;
            }
            System.out.println("[WARN] The payment sheet did not open (attempt " + attempt + "/3).");
        }
        printAllVisibleTextAndDescs("Screen state after failing to open the payment sheet");
        throw new org.openqa.selenium.TimeoutException("The payment sheet did not open after 3 attempts.");
    }

    // ---------------------------------------------------------------
    // Steps 27-30 - Credit Card -> card number, expiry, CVC
    // ---------------------------------------------------------------
    public void selectCreditCardAndEnterDetails(String cardNumber, String expiry, String cvc) {
        System.out.println("[STEP 27] Select 'Credit Card'");
        tapConfirmedButtonWithVerification(CREDIT_CARD_ROW, "Credit Card");
        if (!waitForElement(AppiumBy.id(CARD_NUMBER_ID), 25)) {
            printAllVisibleTextAndDescs("Screen state - card form not detected");
            throw new org.openqa.selenium.TimeoutException("The card details form did not appear.");
        }

        String last4 = cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        System.out.println("[STEP 28] Enter the card number (ending " + last4 + ")");
        typeInto(CARD_NUMBER_ID, cardNumber);
        System.out.println("[STEP 29] Enter the expiry date");
        typeInto(EXPIRY_ID, expiry);
        System.out.println("[STEP 30] Enter the CVC / CVV");
        typeInto(CVC_ID, cvc);

        try {
            driver.hideKeyboard();
        } catch (Exception ignored) {
            // keyboard already hidden
        }
    }

    private void typeInto(String resourceId, String value) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(AppiumBy.id(resourceId)));
        el.click();
        el.sendKeys(value);
        pause(500);
    }

    // ---------------------------------------------------------------
    // Step 31 - Pay (only when payment.submit.enabled=true) and the order confirmation
    // ---------------------------------------------------------------
    public void payAndVerifyOrder(boolean submitEnabled) {
        System.out.println("[STEP 31] Pay and check the order confirmation");
        WebElement pay = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.id(PAY_BUTTON_ID)));
        System.out.println("[RESULT] Pay button text: '" + pay.getText() + "'");

        if (!submitEnabled) {
            System.out.println("[INFO] payment.submit.enabled is not 'true' - the Pay button was NOT tapped, so no order "
                    + "was placed. Set payment.submit.enabled=true in framework.properties to place the order.");
            return;
        }

        System.out.println("[ACTION] payment.submit.enabled=true - tapping Pay (this places a real order).");
        pay.click();
        if (!waitForElementGone(AppiumBy.id(PAY_BUTTON_ID), 90)) {
            printAllVisibleTextAndDescs("Screen state - the Pay button is still showing after 90 seconds");
            throw new org.openqa.selenium.TimeoutException("The payment did not complete within 90 seconds.");
        }
        pause(3000);
        List<String> visible = collectVisibleText();
        printCollected("Screen after paying", new java.util.LinkedHashSet<>(visible));
        boolean found = false;
        for (String line : visible) {
            if (line.toLowerCase().contains("order")) {
                System.out.println("[RESULT] Order confirmation text: " + line);
                found = true;
            }
        }
        if (!found) {
            System.out.println("[WARN] No 'order' text is visible - the confirmation may be a web page the app tree "
                    + "cannot read. Review the printed screen above.");
        }
    }

    private void avoidBottomNav(By locator) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int limit = (int) (size.height * 0.84);
        for (int i = 0; i < 2; i++) {
            List<WebElement> els = driver.findElements(locator);
            if (els.isEmpty()) {
                return;
            }
            org.openqa.selenium.Rectangle r = els.get(0).getRect();
            if (r.getY() + r.getHeight() / 2 <= limit) {
                return;
            }
            System.out.println("[INFO] Target is low on screen (near the bottom nav bar) - scrolling it clear first.");
            scrollDown();
        }
    }

    private void tapOnceWithoutVerification(By locator, String label) {
        ensureVisible(locator, label);
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        org.openqa.selenium.Rectangle rect = el.getRect();
        System.out.println("[ACTION] Tapping '" + label + "' once at its live bounds (selection - "
                + "no visible-text change to verify, no retry).");
        tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);
        try {
            Thread.sleep(700);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean tapSoft(By locator, String label, int attempts) {
        for (int a = 1; a <= attempts; a++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle r = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds (attempt " + a + "/" + attempts + ").");
            tapAtRealCoordinates(r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
            if (waitForContentChangeFromSnapshot(before, 4)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return true;
            }
            System.out.println("[WARN] No visible change after tapping '" + label + "' (attempt " + a + "/" + attempts + ").");
        }
        System.out.println("[INFO] '" + label + "' produced no visible change - it may already be selected; continuing.");
        return false;
    }

    private void scrollIntoUpperPart(By locator, double maxCenterRatio, int maxScrolls) {
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int limit = (int) (size.height * maxCenterRatio);
        for (int i = 0; i < maxScrolls; i++) {
            java.util.List<WebElement> els = driver.findElements(locator);
            if (!els.isEmpty()) {
                org.openqa.selenium.Rectangle r = els.get(0).getRect();
                int centerY = r.getY() + r.getHeight() / 2;
                if (centerY <= limit) {
                    System.out.println("[INFO] Target is now at y=" + centerY + " - section is on screen.");
                    return;
                }
            }
            scrollDownGently();
        }
        System.out.println("[WARN] Target did not reach the upper part of the screen after " + maxScrolls
                + " gentle scrolls (the page may have reached its end).");
    }

    private void scrollDownGently() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "down");
        params.put("percent", 0.35);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] gentle scroll result: " + result);
    }

    private By firstDisplayed(By preferred, By fallback) {
        return isDisplayedSafely(preferred) ? preferred : fallback;
    }

    // ---------------------------------------------------------------
    // Step 32 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 32] Close the app");
        driver.terminateApp(APP_PACKAGE);
        System.out.println("[PASS] App terminated: " + APP_PACKAGE);
    }

    // =================================================================
    // Helpers
    // =================================================================

    /**
     * Makes sure the target is actually on screen before trying to tap it.
     * React Native lists only expose currently-visible rows in the
     * accessibility tree, and a real run showed that returning from the
     * Shop All PLP restores the Menu at a different scroll offset than
     * before - the target header was scrolled out of view above, so it
     * simply wasn't in the tree and the tap helper timed out waiting for
     * it. Scrolls up first (headers are above their sub-lists), then down
     * as a fallback, stopping as soon as the target is visible.
     */
    private void ensureVisible(By locator, String label) {
        if (isDisplayedSafely(locator)) {
            return;
        }
        System.out.println("[INFO] '" + label + "' not currently visible - scrolling to find it.");
        for (int i = 0; i < 8 && !isDisplayedSafely(locator); i++) {
            scrollUpOnce();
        }
        for (int i = 0; i < 8 && !isDisplayedSafely(locator); i++) {
            scrollDown();
        }
        if (isDisplayedSafely(locator)) {
            System.out.println("[PASS] '" + label + "' is now visible.");
        } else {
            System.out.println("[WARN] '" + label + "' still not visible after scrolling up and down.");
            // Dump what is actually on screen so the next failure shows which
            // screen the app is on (Menu, Home, PLP, ...) instead of a bare timeout.
            printCollected("Screen state - '" + label + "' not found after scrolling", collectVisibleText());
        }
    }

    private void scrollUpOnce() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "up");
        params.put("percent", 0.6);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] scroll-up result: " + result);
    }

    /**
     * Tap helper for expand/collapse headers (works with either locator
     * type). Same tap-and-verify as the helpers that passed steps 1-12 in the
     * earlier TC025 run, with ONE exception: a header in the bottom fifth of
     * the screen (that run: "Discover Your New Fragrance" at y~2260). When
     * such a header expands or collapses, the rows that change are below the
     * visible area, so the visible text does not change and the normal check
     * wrongly reports failure - then the retries flip the list back and
     * forth. For those headers the helper taps ONCE and does not treat
     * "no visible change" as a failure.
     *
     * An earlier attempt scrolled every header into the upper half first;
     * that broke Step 8 (the header scrolled out of the tree), so it was
     * removed.
     */
    private void tapToggleHeaderWithVerification(By locator, String label) {
        ensureVisible(locator, label);
        avoidBottomNav(locator);
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        int bottomZone = (int) (size.height * 0.8);

        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            int centerY = rect.getY() + rect.getHeight() / 2;
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, centerY);

            if (centerY > bottomZone) {
                System.out.println("[INFO] '" + label + "' is at the bottom edge (y=" + centerY
                        + ") - its sub-list opens or closes below the visible area, so a visible-text "
                        + "change cannot be used to verify this tap. Tapped once; not retrying.");
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return;
            }

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not change the screen after 3 attempts.");
    }

    private void tapTextAncestorWithVerification(By locator, String label) {
        ensureVisible(locator, label);
        avoidBottomNav(locator);
        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not change the screen after 3 attempts.");
    }

    private void tapConfirmedButtonWithVerification(By locator, String label) {
        ensureVisible(locator, label);
        for (int attempt = 1; attempt <= 3; attempt++) {
            List<String> before = collectVisibleText();
            WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            org.openqa.selenium.Rectangle rect = el.getRect();
            System.out.println("[ACTION] Tapping '" + label + "' at its live bounds "
                    + "(attempt " + attempt + "/3).");
            tapAtRealCoordinates(rect.getX() + rect.getWidth() / 2, rect.getY() + rect.getHeight() / 2);

            if (waitForContentChangeFromSnapshot(before, 5)) {
                System.out.println("[PASS] Screen changed after tapping '" + label + "'.");
                return;
            }
            System.out.println("[WARN] Screen unchanged after tapping '" + label + "' (attempt "
                    + attempt + "/3) - retrying.");
        }
        throw new org.openqa.selenium.TimeoutException(
                "'" + label + "' tap did not change the screen after 3 attempts.");
    }

    private boolean waitForContentChangeFromSnapshot(List<String> before, int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(400))
                    .until(d -> !collectVisibleText().equals(before));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private void tapAtRealCoordinates(int x, int y) {
        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        tap.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(java.util.Collections.singletonList(tap));
        System.out.println("[ACTION] Tapped real coordinate (" + x + "," + y + ").");
    }

    private void tapAtScaledCoordinates(int refX1, int refY1, int refX2, int refY2,
                                        int refWidth, int refHeight) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        double xRatio = (double) screenSize.width / refWidth;
        double yRatio = (double) screenSize.height / refHeight;

        int centerX = (int) Math.round(((refX1 + refX2) / 2.0) * xRatio);
        int centerY = (int) Math.round(((refY1 + refY2) / 2.0) * yRatio);
        tapAtRealCoordinates(centerX, centerY);
        System.out.println("[INFO] [reference was (" + ((refX1 + refX2) / 2) + "," + ((refY1 + refY2) / 2)
                + ") on a " + refWidth + "x" + refHeight + " screen].");
    }

    private boolean waitForElement(By locator, int timeoutSeconds) {
        try {
            new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(timeoutSeconds))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    private boolean isDisplayedSafely(By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            return !els.isEmpty() && els.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void scrollDown() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "down");
        params.put("percent", 0.6);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] element-based scrollGesture result: " + result);
    }

    private void scrollToTop() {
        for (int i = 0; i < 8; i++) {
            Map<String, Object> params = new HashMap<>();
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            params.put("left", (int) (size.width * 0.1));
            params.put("top", (int) (size.height * 0.2));
            params.put("width", (int) (size.width * 0.8));
            params.put("height", (int) (size.height * 0.6));
            params.put("direction", "up");
            params.put("percent", 0.75);
            Object result = driver.executeScript("mobile: scrollGesture", params);
            if (Boolean.FALSE.equals(result)) {
                break;
            }
        }
    }

    /**
     * Scrolls up repeatedly until a given top-of-page landmark becomes
     * visible, rather than trusting mobile:scrollGesture's own boolean
     * result as the stopping signal - this app has repeatedly shown that
     * result isn't fully reliable (returning false even while further
     * scrolling genuinely still works). Keeps scrolling the full attempt
     * budget regardless of individual gesture results.
     */
    private void scrollToTopUntilVisible(By landmark, int maxScrolls) {
        for (int i = 0; i < maxScrolls && !isDisplayedSafely(landmark); i++) {
            Map<String, Object> params = new HashMap<>();
            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
            params.put("left", (int) (size.width * 0.1));
            params.put("top", (int) (size.height * 0.2));
            params.put("width", (int) (size.width * 0.8));
            params.put("height", (int) (size.height * 0.6));
            params.put("direction", "up");
            params.put("percent", 0.75);
            Object result = driver.executeScript("mobile: scrollGesture", params);
            System.out.println("[SCROLL] scroll-to-top-until-visible attempt " + (i + 1) + "/" + maxScrolls
                    + " result: " + result);
        }
        if (!isDisplayedSafely(landmark)) {
            System.out.println("[WARN] Top-of-page landmark still not visible after " + maxScrolls
                    + " scroll-up attempts.");
        }
    }

    private List<String> collectVisibleText() {
        List<String> lines = new ArrayList<>();
        for (WebElement tv : driver.findElements(AppiumBy.className("android.widget.TextView"))) {
            try {
                String text = tv.getText();
                if (text != null && !text.trim().isEmpty()) {
                    lines.add(text.trim());
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        return lines;
    }

    private void printCollected(String label, java.util.Collection<String> lines) {
        System.out.println("----- [" + label + "] visible text (" + lines.size() + " line(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + label + "] -----");
    }

    private void printAllVisibleTextAndDescs(String label) {
        List<String> lines = new ArrayList<>();
        for (WebElement el : driver.findElements(AppiumBy.xpath("//*"))) {
            try {
                String text = el.getText();
                if (text != null && !text.trim().isEmpty()) {
                    lines.add("text: " + text.trim());
                }
                String desc = el.getAttribute("content-desc");
                if (desc != null && !desc.trim().isEmpty()) {
                    lines.add("content-desc: " + desc.trim());
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        System.out.println("----- [" + label + "] (" + lines.size() + " entrie(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + label + "] -----");
    }
}
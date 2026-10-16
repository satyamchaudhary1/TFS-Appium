package com.tfs.pages;

import com.tfs.config.ConfigManager;
import com.tfs.driver.DriverManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
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
 * Page object for TC017 - MyTFS Membership purchase flow
 * (Home card -> Learn More -> Get Started -> Membership page -> Add Membership
 *  -> Bag -> Secure Checkout -> Billing -> Payment method -> Card entry -> Pay
 *  -> Order confirmation -> Continue shopping -> Close app)
 *
 * ASSUMPTIONS (adjust to match your real BaseTest/BasePage if they differ):
 *  - CONFIRMED from LoginTest.java: page objects use a no-arg constructor
 *    and pull the driver internally via DriverManager.getDriver() (e.g.
 *    "new LoginPage()"). This class now follows the same convention -
 *    "new MyTfsMembershipPage()" - instead of taking AndroidDriver as a
 *    constructor argument.
 *  - Card details are read via com.tfs.config.ConfigManager.require(key),
 *    confirmed from your DriverFactory.java (which uses ConfigManager for
 *    appium.url / app.package / etc). This gives fail-fast behaviour for
 *    free: ConfigManager.require() throws if a key is missing, which is
 *    exactly what your framework.properties comment describes:
 *      # Used by MyTfsMembershipPage.fillCardDetailsFromProperties(props)
 *      # All three keys are required; if any is missing, Step 13 will fail fast.
 *    (Method kept the same name/step for continuity, but now takes no
 *    parameter since it reads directly from ConfigManager rather than a
 *    Properties object passed in from the test.)
 *  - Console logging style mirrors what TC015's log showed:
 *      [STEP NN] ..., [ACTION] ..., [PASS] ..., [SCROLL] ..., [FAILED STEP] ...
 *  - Wait timeout 20s / poll 500ms, matching the TC015 WebDriverWait behaviour.
 *
 * LOCATORS FLAGGED "TODO": the Appium Inspector data you supplied did not
 * include resource-id / content-desc for a few elements (billing card-network
 * radio, card number/expiry/CVV fields). I used the most robust fallback
 * available from what you shared. If these fail on your device, re-capture
 * with Appium Inspector and drop the exact locator in.
 */
public class MyTfsMembershipPage {

    private final AndroidDriver driver;
    private final Wait<AndroidDriver> wait;

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    // ---------------------------------------------------------------
    // Locators
    // ---------------------------------------------------------------

    // Step 2 - "Learn More" expander on the home page MyTFS card
    private static final By LEARN_MORE = AppiumBy.accessibilityId("Learn More");

    // Step 3/4 - "Get Started" button revealed after expansion
    private static final By GET_STARTED = AppiumBy.accessibilityId("Get Started");

    // Step 5 - "What MyTFS gives you" section container (heading text)
    private static final By GIVES_YOU_HEADING =
            AppiumBy.xpath("//android.widget.TextView[@text='What MyTFS gives you']");

    // Step 5 - FAQ heading, and FAQ question rows (rows end with '?')
    private static final By FAQ_HEADING =
            AppiumBy.xpath("//android.widget.TextView[@text='Frequently asked questions']");
    private static final By FAQ_QUESTION_TEXTVIEWS =
            AppiumBy.xpath("//android.widget.TextView[contains(@text,'?')]");

    // Step 5 - Best Sellers heading on the membership page
    private static final By BEST_SELLERS_HEADING =
            AppiumBy.xpath("//android.widget.TextView[@text='Bestsellers' or @text='Best Sellers']");

    // Step 6 - "Add Membership" button
    private static final By ADD_MEMBERSHIP = AppiumBy.accessibilityId("Add Membership");

    // Same button once this account already holds an active membership -
    // confirmed from your screenshot/log: the button label switches from
    // "Add Membership" to "Added Membership" (greyed out) server-side, based
    // on account state, not local app storage - so FULL_APP_RESET isolation
    // does not reset it.
    private static final By ADDED_MEMBERSHIP = AppiumBy.accessibilityId("Added Membership");

    // Step 8 - "Secure Checkout" button on the bag screen
    private static final By SECURE_CHECKOUT = AppiumBy.accessibilityId("Secure Checkout");

    // Step 8a (conditional) - billing address handling, confirmed via Inspector.
    // If billing address is not auto-populated the screen shows "No Address"
    // next to a (non-clickable) "Edit" TextView - its clickable ancestor
    // opens the saved-address list, where "Save Details" confirms the
    // selected/default address and returns to the payment method screen.
    private static final By BILLING_NO_ADDRESS =
            AppiumBy.xpath("//android.widget.TextView[@text='No Address']");
    private static final By BILLING_EDIT_CLICKABLE =
            AppiumBy.xpath("//android.widget.TextView[@text='Edit']/ancestor::*[@clickable='true'][1]");
    private static final By SAVE_DETAILS = AppiumBy.accessibilityId("Save Details");

    // Confirmed via Inspector: the app's own validation error when Continue
    // is tapped with no billing address selected ("Error! / Please select
    // Billing Address / OKAY"). Recovered from by dismissing OKAY and
    // forcing the address-selection flow.
    private static final By BILLING_ERROR_TEXT =
            AppiumBy.xpath("//android.widget.TextView[@text='Please select Billing Address']");
    private static final By BILLING_ERROR_OKAY = AppiumBy.accessibilityId("OKAY");

    // BILLING_ADDRESS_TEXT is used as the "have we left the billing screen"
    // verification anchor in selectCardNetworkOptionAndContinue() below -
    // see that method for why the VISA row itself is now tapped by scaled
    // coordinate instead of located via DOM order (which proved unreliable).
    private static final By BILLING_ADDRESS_TEXT =
            AppiumBy.xpath("//android.widget.TextView[@text='Billing Address']");

    // Step 10 - "Continue" button on billing/payment-method screen
    private static final By CONTINUE_BUTTON = AppiumBy.accessibilityId("Continue");

    // Step 11 - CONFIRMED via Inspector: the "Credit Card" row in the payment
    // methods bottom sheet has NO text/content-desc on the captured element
    // (android.widget.LinearLayout, index 0) - the visible "Credit Card"
    // label is not exposed as accessible text on this element. It's simply
    // the first (topmost) clickable row in the list, so locate it as the
    // first clickable element below the "PAYMENT METHODS" sheet header.
    //
    // NOTE: an earlier version restricted this to android.widget.LinearLayout
    // only (matching one specific Inspector capture), which returned zero
    // matches on a later run and burned the full 20s timeout - most of this
    // app's clickable surfaces render as generic android.view.ViewGroup, so
    // restricting to one class was too narrow. Broadened to any clickable
    // element (@clickable='true', any class) below the header.
    private static final By PAYMENT_METHODS_HEADER =
            AppiumBy.xpath("//android.widget.TextView[@text='PAYMENT METHODS']");
    private static final By ANY_CLICKABLE_ELEMENT =
            AppiumBy.xpath("//*[@clickable='true']");

    // Step 12 - CONFIRMED real resource-ids from Appium Inspector.
    private static final By CARD_NUMBER_FIELD =
            By.id("com.thefragranceshop:id/editText_cardNumber");
    private static final By EXPIRY_FIELD =
            By.id("com.thefragranceshop:id/editText_expiryDate");
    private static final By CVV_FIELD =
            By.id("com.thefragranceshop:id/editText_securityCode");
    private static final By PAY_BUTTON = By.id("com.thefragranceshop:id/payButton");

    // Step 14 - CONFIRMED exact accessibility id: "Continue shopping"
    // (lowercase "shopping").
    private static final By CONTINUE_SHOPPING = AppiumBy.accessibilityId("Continue shopping");

    private static final String APP_PACKAGE = "com.thefragranceshop";

    public MyTfsMembershipPage() {
        this.driver = DriverManager.getDriver();
        this.wait = new FluentWait<>(driver)
                .withTimeout(TIMEOUT)
                .pollingEvery(POLL_INTERVAL)
                .ignoring(org.openqa.selenium.NoSuchElementException.class);
    }

    // ---------------------------------------------------------------
    // Step 1 - Scroll from top of Home page down to the MyTFS card
    // ---------------------------------------------------------------
    public void scrollToMembershipCard() {
        System.out.println("[STEP 01] Scroll down Home page to MyTFS Membership card");
        for (int i = 1; i <= 6; i++) {
            if (isDisplayedSafely(LEARN_MORE)) {
                System.out.println("[PASS] MyTFS Membership card visible after " + (i - 1) + " scroll(s).");
                return;
            }
            scrollDown();
            System.out.println("[SCROLL] Home page scroll " + i + "/6");
        }
        if (!isDisplayedSafely(LEARN_MORE)) {
            throw new org.openqa.selenium.TimeoutException(
                    "MyTFS Membership card ('Learn More') not found after scrolling.");
        }
    }

    // ---------------------------------------------------------------
    // Step 2 - Tap "Learn More"
    // ---------------------------------------------------------------
    public void clickLearnMore() {
        System.out.println("[STEP 02] Click 'Learn More' on MyTFS Membership card");
        WebElement learnMore = wait.until(ExpectedConditions.elementToBeClickable(LEARN_MORE));
        System.out.println("[ACTION] Tapping 'Learn More'.");
        learnMore.click();
    }

    // ---------------------------------------------------------------
    // Step 3 - Card expands, read description text, confirm "Get Started" shown
    // ---------------------------------------------------------------
    public void verifyCardExpandedAndReadDetails() {
        System.out.println("[STEP 03] Verify MyTFS card expanded and read membership details");
        wait.until(ExpectedConditions.presenceOfElementLocated(GET_STARTED));
        System.out.println("[PASS] Card expanded - 'Get Started' button is present.");
        printAllVisibleText("MyTFS card details");
    }

    // ---------------------------------------------------------------
    // Step 4 - Tap "Get Started", navigate to full Membership page
    // ---------------------------------------------------------------
    public void clickGetStarted() {
        System.out.println("[STEP 04] Click 'Get Started' and navigate to Membership page");
        WebElement getStarted = wait.until(ExpectedConditions.elementToBeClickable(GET_STARTED));
        System.out.println("[ACTION] Tapping 'Get Started'.");
        getStarted.click();
        printAllVisibleText("Membership page content");
    }

    // ---------------------------------------------------------------
    // Step 5 - Read "What MyTFS gives you" (horizontal scroll), FAQs
    //          (expand each), then Best Sellers (horizontal scroll)
    // ---------------------------------------------------------------
    public void readMembershipPageSections() {
        System.out.println("[STEP 05] Read Membership page sections: gives-you, FAQs, Best Sellers");

        // -- "What MyTFS gives you" horizontal carousel --
        if (scrollUntilVisible(GIVES_YOU_HEADING, 6)) {
            System.out.println("[PASS] 'What MyTFS gives you' section found.");
            swipeHorizontal(GIVES_YOU_HEADING, 3);
        } else {
            System.out.println("[WARN] 'What MyTFS gives you' heading not found - skipping carousel read.");
        }

        // -- FAQs: expand each question row and print revealed text --
        if (scrollUntilVisible(FAQ_HEADING, 6)) {
            System.out.println("[PASS] 'Frequently asked questions' section found.");
            expandAllFaqs();
        } else {
            System.out.println("[WARN] FAQ heading not found - skipping FAQ read.");
        }

        // -- Best Sellers horizontal product carousel --
        if (scrollUntilVisible(BEST_SELLERS_HEADING, 6)) {
            System.out.println("[PASS] 'Best Sellers' section found.");
            swipeHorizontal(BEST_SELLERS_HEADING, 3);
            printAllVisibleText("Best Sellers section");
        } else {
            System.out.println("[WARN] 'Best Sellers' heading not found - skipping carousel read.");
        }
    }

    private void expandAllFaqs() {
        List<WebElement> questions = driver.findElements(FAQ_QUESTION_TEXTVIEWS);
        System.out.println("[ACTION] Found " + questions.size() + " FAQ question(s). Expanding each.");
        for (int i = 0; i < questions.size(); i++) {
            try {
                List<WebElement> current = driver.findElements(FAQ_QUESTION_TEXTVIEWS);
                if (i >= current.size()) break;
                WebElement question = current.get(i);
                String qText = question.getText();
                System.out.println("[ACTION] Expanding FAQ: \"" + qText + "\"");
                question.click();
                Thread.sleep(500); // allow accordion animation
                printAllVisibleText("FAQ answer for: " + qText);
                question.click(); // collapse again before moving to next
            } catch (Exception e) {
                System.out.println("[WARN] Could not expand FAQ at index " + i + ": " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------
    // Step 6 - Scroll to top, tap "Add Membership"
    // ---------------------------------------------------------------
    /**
     * Scrolls back to the top of the Membership page (after content has
     * already been read in step 5) and taps "Add Membership".
     * <p>
     * Returns {@code true} if the membership was added (button said
     * "Add Membership" and was tapped), or {@code false} if this account
     * already holds an active membership (button said "Added Membership"),
     * in which case the remaining purchase/checkout/payment steps cannot be
     * exercised and the caller should stop here rather than fail on a
     * timeout.
     */
    public boolean scrollToTopAndClickAddMembership() {
        System.out.println("[STEP 06] Return to top of Membership page and click 'Add Membership'");
        for (int i = 0; i < 8
                && !isDisplayedSafely(ADD_MEMBERSHIP)
                && !isDisplayedSafely(ADDED_MEMBERSHIP); i++) {
            scrollUp();
            System.out.println("[SCROLL] Membership page moved upward toward header.");
        }

        if (isDisplayedSafely(ADDED_MEMBERSHIP) && !isDisplayedSafely(ADD_MEMBERSHIP)) {
            System.out.println("[INFO] Button reads 'Added Membership' - this account already has an "
                    + "active MyTFS membership.");
            System.out.println("[INFO] Skipping bag/checkout/payment steps (7-15); nothing left to purchase.");
            return false;
        }

        WebElement addMembership = wait.until(ExpectedConditions.elementToBeClickable(ADD_MEMBERSHIP));
        System.out.println("[ACTION] Tapping 'Add Membership'.");
        addMembership.click();
        return true;
    }

    // ---------------------------------------------------------------
    // Step 7 - Bag screen: scroll to bottom, print content, scroll back to top
    // ---------------------------------------------------------------
    public void verifyBagContentsAndScroll() {
        System.out.println("[STEP 07] Membership added to bag - scroll and read bag contents");
        for (int i = 1; i <= 6; i++) {
            scrollDown();
            System.out.println("[SCROLL] Bag page scroll " + i + "/6");
        }
        printAllVisibleText("Bag page (bottom)");
        for (int i = 1; i <= 6; i++) {
            scrollUp();
            System.out.println("[SCROLL] Bag page moved upward " + i + "/6");
        }
        printAllVisibleText("Bag page (top)");
    }

    // ---------------------------------------------------------------
    // Step 8 - Tap "Secure Checkout"
    // ---------------------------------------------------------------
    public void clickSecureCheckout() {
        System.out.println("[STEP 08] Click 'Secure Checkout'");
        WebElement checkout = wait.until(ExpectedConditions.elementToBeClickable(SECURE_CHECKOUT));
        System.out.println("[ACTION] Tapping 'Secure Checkout'.");
        checkout.click();
    }

    // ---------------------------------------------------------------
    // Step 8a (conditional) - if the billing address is not auto-populated
    // ("No Address"), open Edit, select the saved/default address, and
    // tap "Save Details" to return to the payment method screen with a
    // billing address set.
    //
    // FIXED RACE CONDITION: the original check called isDisplayedSafely()
    // instantly with no wait right after "Secure Checkout" was tapped -
    // on a real run the billing screen hadn't finished rendering "No
    // Address" yet, so the check saw neither state and defaulted to
    // "already populated", skipping this entirely. That let the flow
    // proceed to tap Continue with no address selected, which triggered
    // the app's own "Please select Billing Address" error popup. Now
    // waits for the "Billing Address" heading to be present first, then
    // polls briefly for "No Address" before deciding.
    // ---------------------------------------------------------------
    public void ensureBillingAddressSelected() {
        System.out.println("[STEP 08a] Check billing address state");
        wait.until(ExpectedConditions.presenceOfElementLocated(BILLING_ADDRESS_TEXT));

        boolean noAddress;
        try {
            noAddress = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(3))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> isDisplayedSafely(BILLING_NO_ADDRESS));
        } catch (org.openqa.selenium.TimeoutException e) {
            noAddress = false;
        }

        if (!noAddress) {
            System.out.println("[PASS] Billing address already populated - nothing to do.");
            return;
        }

        selectSavedBillingAddress();
    }

    /**
     * Unconditionally runs Edit -> resolve a billing address, regardless of
     * what the "No Address" check reports. Used by
     * selectCardNetworkOptionAndContinue() to recover after the "Please
     * select Billing Address" error popup - at that point we know for
     * certain no address is set, so there's no need to re-check.
     * <p>
     * CORRECTED: tapping "Edit" can resolve in two different ways depending
     * on how many saved addresses the account has -
     * <ul>
     *   <li>Multiple saved addresses: a list appears with a "Save Details"
     *       button to confirm the selected/default one.</li>
     *   <li>A single saved address: Edit auto-selects it and returns
     *       straight to the main billing screen with it already populated -
     *       no separate "Save Details" confirmation screen appears at all.
     *       (Confirmed via a diagnostic dump showing the populated address
     *       already present immediately after tapping Edit - an earlier
     *       version of this method assumed "Save Details" always appears
     *       and failed here even though the address was actually already
     *       fine.)</li>
     * </ul>
     * Both outcomes are treated as success. Only genuinely zero saved
     * addresses (still showing "No Address" after waiting) is a real
     * failure.
     */
    private void selectSavedBillingAddress() {
        System.out.println("[INFO] Billing address not set - opening Edit to select one.");
        WebElement editClickable = wait.until(ExpectedConditions.elementToBeClickable(BILLING_EDIT_CLICKABLE));
        System.out.println("[ACTION] Tapping 'Edit'.");
        editClickable.click();

        boolean resolved;
        try {
            resolved = new FluentWait<AndroidDriver>(driver)
                    .withTimeout(Duration.ofSeconds(5))
                    .pollingEvery(Duration.ofMillis(300))
                    .until(d -> isDisplayedSafely(SAVE_DETAILS)
                            || (isDisplayedSafely(BILLING_ADDRESS_TEXT) && !isDisplayedSafely(BILLING_NO_ADDRESS)));
        } catch (org.openqa.selenium.TimeoutException e) {
            resolved = false;
        }

        if (!resolved) {
            printAllVisibleText("Address selection screen - still unresolved after tapping Edit");
            throw new IllegalStateException(
                    "This account has no saved billing address to select (still showing 'No Address' "
                            + "after tapping Edit - only 'Add new address' is likely available). Creating "
                            + "a new address from scratch is not yet implemented (no Appium Inspector "
                            + "locators captured for that form). Use a test account that already has a "
                            + "saved address (set login.email/login.password in framework.properties), "
                            + "or capture that form's locators to add support.");
        }

        if (isDisplayedSafely(SAVE_DETAILS)) {
            WebElement saveDetails = wait.until(ExpectedConditions.elementToBeClickable(SAVE_DETAILS));
            System.out.println("[ACTION] Tapping 'Save Details' (using the default/selected saved address).");
            saveDetails.click();
        } else {
            System.out.println("[PASS] Address auto-selected - billing screen already shows a populated "
                    + "address, no separate confirmation step needed.");
        }
    }

    // ---------------------------------------------------------------
    // Steps 9-10 - Select the Visa/Mastercard/Visa Debit card network row,
    // then tap "Continue".
    //
    // CHANGED FROM DOM-ORDER HEURISTIC TO COORDINATE TAP: the previous
    // "first clickable element below Billing Address heading" heuristic was
    // confirmed unreliable across runs - the diagnostic dump from a failed
    // run showed the flow never left the billing screen at all after
    // tapping "Continue", meaning the card network row was never actually
    // selected (likely tapped "Edit" instead, which sits at the same
    // relative position). Since Continue stays functionally disabled when
    // no card network is selected, tapping it silently does nothing (no
    // exception - RN ignores taps on disabled elements), which is why this
    // failed inconsistently rather than with a clear error.
    //
    // Now uses the EXACT bounds captured via Appium Inspector for the VISA
    // row ([8,1149]-[1212,1300] on a 1220x2712 reference screen), scaled to
    // the actual device's screen size and tapped by coordinate - no
    // dependency on element ordering at all. After tapping Continue, this
    // verifies the billing screen was actually left (retries once if not)
    // instead of silently proceeding on a no-op tap.
    // ---------------------------------------------------------------
    private static final int REF_SCREEN_WIDTH = 1220;
    private static final int REF_SCREEN_HEIGHT = 2712;
    private static final int VISA_ROW_X1 = 8;
    private static final int VISA_ROW_Y1 = 1149;
    private static final int VISA_ROW_X2 = 1212;
    private static final int VISA_ROW_Y2 = 1300;

    public void selectCardNetworkOptionAndContinue() {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("[STEP 09] Select card payment method on billing screen (attempt "
                    + attempt + "/" + maxAttempts + ")");
            tapAtScaledCoordinates(VISA_ROW_X1, VISA_ROW_Y1, VISA_ROW_X2, VISA_ROW_Y2,
                    REF_SCREEN_WIDTH, REF_SCREEN_HEIGHT);
            System.out.println("[ACTION] Tapped card network row (Visa/Mastercard/Visa Debit) "
                    + "via scaled coordinates.");

            System.out.println("[STEP 10] Click 'Continue' to proceed for checkout (attempt "
                    + attempt + "/" + maxAttempts + ")");
            WebElement cont = wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_BUTTON));
            System.out.println("[ACTION] Tapping 'Continue'.");
            cont.click();

            // Check specifically for the "Please select Billing Address" error
            // popup first - this is a distinct, known outcome (address was
            // never set) and needs a different recovery than a generic
            // "nothing happened" retry.
            boolean errorShown;
            try {
                new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(3))
                        .pollingEvery(Duration.ofMillis(300))
                        .until(d -> isDisplayedSafely(BILLING_ERROR_TEXT));
                errorShown = true;
            } catch (org.openqa.selenium.TimeoutException e) {
                errorShown = false;
            }

            if (errorShown) {
                System.out.println("[WARN] 'Please select Billing Address' error appeared - "
                        + "no address was set (attempt " + attempt + "/" + maxAttempts + ").");
                WebElement okay = wait.until(ExpectedConditions.elementToBeClickable(BILLING_ERROR_OKAY));
                System.out.println("[ACTION] Dismissing error popup (OKAY).");
                okay.click();

                if (attempt == maxAttempts) {
                    printAllVisibleText("Still failing to set billing address after "
                            + maxAttempts + " attempts");
                    throw new org.openqa.selenium.TimeoutException(
                            "Billing address could not be set after " + maxAttempts + " attempts "
                                    + "(app kept showing 'Please select Billing Address').");
                }
                System.out.println("[ACTION] Selecting a saved billing address before retrying.");
                selectSavedBillingAddress();
                continue;
            }

            boolean leftBillingScreen;
            try {
                new FluentWait<>(driver)
                        .withTimeout(Duration.ofSeconds(6))
                        .pollingEvery(Duration.ofMillis(500))
                        .until(d -> !isDisplayedSafely(BILLING_ADDRESS_TEXT));
                leftBillingScreen = true;
            } catch (org.openqa.selenium.TimeoutException e) {
                leftBillingScreen = false;
            }

            if (leftBillingScreen) {
                System.out.println("[PASS] Left the billing screen - Continue navigation succeeded.");
                return;
            }

            System.out.println("[WARN] Still on billing screen after tapping Continue - the card "
                    + "network selection likely didn't register (attempt " + attempt + "/" + maxAttempts + ").");
            if (attempt == maxAttempts) {
                printAllVisibleText("Still stuck on billing screen after " + maxAttempts + " attempts");
                throw new org.openqa.selenium.TimeoutException(
                        "Could not proceed past the billing screen after selecting the card network "
                                + "and tapping Continue, even after " + maxAttempts + " attempts.");
            }
            System.out.println("[ACTION] Retrying card network selection...");
        }
    }

    // ---------------------------------------------------------------
    // Step 11 - Payment methods list appears, select "Credit Card".
    // CONFIRMED via Inspector: the captured element has no text/content-desc
    // of its own - it's simply the first clickable row (index 0) in the
    // bottom sheet, located relative to the "PAYMENT METHODS" header.
    // ---------------------------------------------------------------
    public void selectCreditCardPaymentMethod() {
        System.out.println("[STEP 11] Select 'Credit Card' from Payment Methods list");
        WebElement creditCard;
        try {
            creditCard = wait.until(d -> locateFirstPaymentMethodRow());
        } catch (org.openqa.selenium.TimeoutException e) {
            System.out.println("[FAIL] 'PAYMENT METHODS' sheet/row not found within 20s.");
            printAllVisibleText("Screen state when Payment Methods row not found");
            throw e;
        }
        System.out.println("[ACTION] Tapping 'Credit Card' payment method (first row in the list).");
        creditCard.click();
    }

    // ---------------------------------------------------------------
    // Step 12 - Fill in card number / expiry / CVV from framework.properties
    // and tap PAY. Card number + CVV masked in console output. CONFIRMED
    // via Inspector: real resource-ids exist for all three fields, so no
    // more positional guessing.
    // ---------------------------------------------------------------
    public void fillCardDetailsFromProperties() {
        System.out.println("[STEP 12] Enter card details from framework.properties");

        // ConfigManager.require() throws IllegalStateException (or similar)
        // if the key is missing, giving the "fail fast" behaviour your
        // framework.properties comment calls for - no manual blank-check needed.
        String cardNumber = ConfigManager.require("payment.card.number");
        String expiry = ConfigManager.require("payment.card.expiry");
        String cvv = ConfigManager.require("payment.card.cvv");

        WebElement cardNumberField = wait.until(ExpectedConditions.elementToBeClickable(CARD_NUMBER_FIELD));
        System.out.println("[ACTION] Entering card number: " + mask(cardNumber));
        cardNumberField.click();
        cardNumberField.sendKeys(cardNumber);
        hideKeyboardSafely();

        WebElement expiryField = wait.until(ExpectedConditions.elementToBeClickable(EXPIRY_FIELD));
        System.out.println("[ACTION] Entering expiry date: " + expiry);
        expiryField.click();
        expiryField.sendKeys(expiry);

        WebElement cvvField = wait.until(ExpectedConditions.elementToBeClickable(CVV_FIELD));
        System.out.println("[ACTION] Entering CVV: " + mask(cvv));
        cvvField.click();
        cvvField.sendKeys(cvv);
        hideKeyboardSafely();

        System.out.println("[PASS] Card details entered (masked in log).");
    }

    public void clickPayButton() {
        WebElement payButton = wait.until(ExpectedConditions.elementToBeClickable(PAY_BUTTON));
        System.out.println("[ACTION] Tapping PAY button.");
        payButton.click();
    }

    // ---------------------------------------------------------------
    // Step 14 - Order confirmation: print content, click "Continue Shopping"
    // ---------------------------------------------------------------
    public void verifyOrderConfirmationAndContinueShopping() {
        System.out.println("[STEP 14] Verify Order Confirmation screen and click 'Continue Shopping'");
        WebElement continueShopping = wait.until(ExpectedConditions.presenceOfElementLocated(CONTINUE_SHOPPING));
        printAllVisibleText("Order Confirmation screen");
        System.out.println("[ACTION] Tapping 'Continue Shopping'.");
        continueShopping.click();
    }

    // ---------------------------------------------------------------
    // Step 14b - Confirm navigation back to Home Page after order.
    // No single stable anchor element for "Home Page" was captured, so this
    // logs the visible content for manual/log-based confirmation rather
    // than asserting on a specific locator.
    // ---------------------------------------------------------------
    public void verifyHomeScreenAfterOrder() {
        System.out.println("[STEP 14b] Confirm navigation to Home Page after order");
        printAllVisibleText("Home Page (after Continue Shopping)");
    }

    // ---------------------------------------------------------------
    // Step 15 - Close the app
    // ---------------------------------------------------------------
    public void closeApp() {
        System.out.println("[STEP 15] Close the app");
        driver.terminateApp(APP_PACKAGE);
        System.out.println("[PASS] App terminated: " + APP_PACKAGE);
    }

    // =================================================================
    // Helpers
    // =================================================================

    /**
     * Taps the center of a bounds rectangle captured at a known reference
     * screen resolution, scaled proportionally to the actual device's
     * current screen size. Used for the VISA/Mastercard/Visa Debit row,
     * which has no text/content-desc of its own and whose DOM position
     * relative to other clickable elements proved unreliable to rely on.
     */
    private void tapAtScaledCoordinates(int refX1, int refY1, int refX2, int refY2,
                                        int refWidth, int refHeight) {
        org.openqa.selenium.Dimension screenSize = driver.manage().window().getSize();
        double xRatio = (double) screenSize.width / refWidth;
        double yRatio = (double) screenSize.height / refHeight;

        int centerX = (int) Math.round(((refX1 + refX2) / 2.0) * xRatio);
        int centerY = (int) Math.round(((refY1 + refY2) / 2.0) * yRatio);

        org.openqa.selenium.interactions.PointerInput finger =
                new org.openqa.selenium.interactions.PointerInput(
                        org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap =
                new org.openqa.selenium.interactions.Sequence(finger, 0);
        tap.addAction(finger.createPointerMove(Duration.ZERO,
                org.openqa.selenium.interactions.PointerInput.Origin.viewport(), centerX, centerY));
        tap.addAction(finger.createPointerDown(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(
                org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(java.util.Collections.singletonList(tap));
        System.out.println("[ACTION] Tapped scaled coordinate (" + centerX + "," + centerY + ") "
                + "[reference was (" + ((refX1 + refX2) / 2) + "," + ((refY1 + refY2) / 2)
                + ") on a " + refWidth + "x" + refHeight + " screen].");
    }

    /**
     * Locator for the "Credit Card" row in the Payment Methods bottom
     * sheet. CONFIRMED via Appium Inspector: the captured element has no
     * text/content-desc of its own - it's simply the first (topmost)
     * clickable row in the list.
     * <p>
     * Rule: first clickable element (any class) positioned at or below the
     * "PAYMENT METHODS" sheet header. The header must actually be present
     * before searching for a row - if it isn't found yet, this throws
     * (rather than falling back to "first clickable element on screen"),
     * so an early poll during the sheet's open animation can't repeat the
     * earlier back-arrow mis-click bug by grabbing something from the
     * screen underneath. The outer wait.until() keeps retrying every
     * 500ms until the header genuinely appears.
     */
    private WebElement locateFirstPaymentMethodRow() {
        List<WebElement> header = driver.findElements(PAYMENT_METHODS_HEADER);
        if (header.isEmpty()) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "'PAYMENT METHODS' header not yet present - sheet may still be opening.");
        }
        org.openqa.selenium.Rectangle headerRect = header.get(0).getRect();
        int headerBottomY = headerRect.getY() + headerRect.getHeight();

        for (WebElement el : driver.findElements(ANY_CLICKABLE_ELEMENT)) {
            try {
                org.openqa.selenium.Rectangle r = el.getRect();
                if (r.getY() >= headerBottomY && r.getWidth() > 0) {
                    return el;
                }
            } catch (Exception ignored) {
                // stale element - skip
            }
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Could not locate the 'Credit Card' row below the 'PAYMENT METHODS' header.");
    }

    private boolean isDisplayedSafely(By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            return !els.isEmpty() && els.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean scrollUntilVisible(By locator, int maxScrolls) {
        for (int i = 0; i <= maxScrolls; i++) {
            if (isDisplayedSafely(locator)) {
                return true;
            }
            scrollDown();
            System.out.println("[SCROLL] Searching for element, scroll " + i + "/" + maxScrolls);
        }
        return isDisplayedSafely(locator);
    }

    private void swipeHorizontal(By anchorLocator, int times) {
        try {
            WebElement anchor = driver.findElement(anchorLocator);
            org.openqa.selenium.Rectangle rect = anchor.getRect();
            int startX = (int) (rect.getWidth() * 0.85) + rect.getX();
            int endX = (int) (rect.getWidth() * 0.15) + rect.getX();
            int y = rect.getY() + Math.max(rect.getHeight() * 3, 200);
            for (int i = 0; i < times; i++) {
                Map<String, Object> params = new HashMap<>();
                params.put("left", Math.min(startX, endX));
                params.put("top", y - 50);
                params.put("width", Math.abs(startX - endX));
                params.put("height", 100);
                params.put("direction", "left");
                params.put("percent", 0.75);
                driver.executeScript("mobile: scrollGesture", params);
                System.out.println("[SCROLL] Horizontal carousel swipe " + (i + 1) + "/" + times);
            }
        } catch (Exception e) {
            System.out.println("[WARN] Horizontal swipe skipped: " + e.getMessage());
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
        params.put("percent", 0.75);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] element-based scrollGesture result: " + result);
    }

    private void scrollUp() {
        Map<String, Object> params = new HashMap<>();
        org.openqa.selenium.Dimension size = driver.manage().window().getSize();
        params.put("left", (int) (size.width * 0.1));
        params.put("top", (int) (size.height * 0.2));
        params.put("width", (int) (size.width * 0.8));
        params.put("height", (int) (size.height * 0.6));
        params.put("direction", "up");
        params.put("percent", 0.75);
        Object result = driver.executeScript("mobile: scrollGesture", params);
        System.out.println("[SCROLL] element-based scrollGesture result: " + result);
    }

    private void printAllVisibleText(String sectionLabel) {
        List<WebElement> textViews = driver.findElements(AppiumBy.className("android.widget.TextView"));
        List<String> lines = new ArrayList<>();
        for (WebElement tv : textViews) {
            try {
                String text = tv.getText();
                if (text != null && !text.trim().isEmpty()) {
                    lines.add(text.trim());
                }
            } catch (Exception ignored) {
                // stale element references from RN re-render - skip
            }
        }
        System.out.println("----- [" + sectionLabel + "] visible text (" + lines.size() + " line(s)) -----");
        for (String line : lines) {
            System.out.println("    " + line);
        }
        System.out.println("----- end of [" + sectionLabel + "] -----");
    }

    private void hideKeyboardSafely() {
        try {
            driver.pressKey(new KeyEvent(AndroidKey.BACK));
        } catch (Exception ignored) {
            // keyboard may already be hidden
        }
    }

    private String mask(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        int visible = 4;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length() - visible; i++) {
            sb.append('*');
        }
        sb.append(value.substring(value.length() - visible));
        return sb.toString();
    }
}
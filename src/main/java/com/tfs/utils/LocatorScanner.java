package com.tfs.utils;

import com.tfs.driver.DriverManager;
import io.appium.java_client.android.AndroidDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Inspects the current Android page source and prints stable locator candidates.
 * Temporary Appium elementId values and screen coordinates are not recommended.
 */
public final class LocatorScanner {

    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Path OUTPUT_DIRECTORY =
            Paths.get("target", "locator-scans");

    private LocatorScanner() {
        // Utility class.
    }

    public static Path scanCurrentScreen(String screenName) {
        AndroidDriver driver = DriverManager.getDriver();
        String pageSource = driver.getPageSource();
        Document document = parse(pageSource);
        String safeScreenName = sanitizeFileName(screenName);
        String report = buildReport(driver, document, screenName);

        try {
            Files.createDirectories(OUTPUT_DIRECTORY);
            Path reportPath = OUTPUT_DIRECTORY.resolve(
                    safeScreenName + "-" + LocalDateTime.now().format(FILE_TIME) + ".txt");
            Files.writeString(reportPath, report, StandardCharsets.UTF_8);
            System.out.print(report);
            System.out.println("[LOCATOR SCANNER] Report saved: "
                    + reportPath.toAbsolutePath());
            return reportPath.toAbsolutePath();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to save locator report.", exception);
        }
    }

    private static Document parse(String pageSource) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(
                    new InputSource(new StringReader(pageSource)));
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse Appium page source.", exception);
        }
    }

    private static String buildReport(
            AndroidDriver driver, Document document, String screenName) {
        StringBuilder report = new StringBuilder();
        report.append("==================================================\n")
                .append("APPIUM LOCATOR SCANNER\n")
                .append("==================================================\n")
                .append("Screen       : ").append(screenName).append('\n')
                .append("Package      : ").append(driver.getCurrentPackage()).append('\n')
                .append("Activity     : ").append(driver.currentActivity()).append('\n')
                .append("Captured At  : ").append(LocalDateTime.now()).append('\n')
                .append("Priority     : accessibilityId > id > UiSelector > XPath\n")
                .append("Note         : elementId and bounds are informational only.\n")
                .append("==================================================\n\n");

        NodeList allNodes = document.getElementsByTagName("*");
        int printedElements = 0;

        for (int index = 0; index < allNodes.getLength(); index++) {
            Node node = allNodes.item(index);
            if (!(node instanceof Element element) || !isUseful(element)) continue;

            printedElements++;
            appendElement(report, document, element, printedElements);
        }

        report.append("==================================================\n")
                .append("TOTAL USEFUL ELEMENTS: ").append(printedElements).append('\n')
                .append("==================================================\n");
        return report.toString();
    }

    private static boolean isUseful(Element element) {
        if ("false".equalsIgnoreCase(attribute(element, "displayed"))) return false;

        String text = attribute(element, "text");
        String contentDescription = attribute(element, "content-desc");
        String resourceId = attribute(element, "resource-id");
        boolean interactive = isTrue(element, "clickable")
                || isTrue(element, "checkable")
                || isTrue(element, "scrollable")
                || isTrue(element, "focusable");

        return !text.isBlank()
                || !contentDescription.isBlank()
                || !resourceId.isBlank()
                || interactive;
    }

    private static void appendElement(
            StringBuilder report, Document document, Element element, int number) {
        String className = element.getTagName();
        String text = attribute(element, "text");
        String contentDescription = attribute(element, "content-desc");
        String resourceId = attribute(element, "resource-id");

        report.append("--------------------------------------------------\n")
                .append("ELEMENT ").append(number).append('\n')
                .append("--------------------------------------------------\n")
                .append("Class        : ").append(className).append('\n')
                .append("Text         : ").append(valueOrDash(text)).append('\n')
                .append("Content-desc : ").append(valueOrDash(contentDescription)).append('\n')
                .append("Resource-id  : ").append(valueOrDash(resourceId)).append('\n')
                .append("Clickable    : ").append(attribute(element, "clickable")).append('\n')
                .append("Enabled      : ").append(attribute(element, "enabled")).append('\n')
                .append("Displayed    : ").append(attribute(element, "displayed")).append('\n')
                .append("Bounds       : ").append(valueOrDash(attribute(element, "bounds")))
                .append(" (informational only)\n")
                .append("Unique values: content-desc=")
                .append(isUnique(document, "content-desc", contentDescription))
                .append(", resource-id=")
                .append(isUnique(document, "resource-id", resourceId))
                .append(", text=").append(isUnique(document, "text", text)).append('\n')
                .append("SUGGESTED LOCATORS\n");

        List<String> suggestions = locatorSuggestions(
                document, element, className, text, contentDescription, resourceId);
        if (suggestions.isEmpty()) {
            report.append("  [WARN] No stable locator available. Inspect its parent manually.\n");
        } else {
            for (String suggestion : suggestions) {
                report.append("  ").append(suggestion).append('\n');
            }
        }
        report.append('\n');
    }

    private static List<String> locatorSuggestions(
            Document document,
            Element element,
            String className,
            String text,
            String contentDescription,
            String resourceId) {
        List<String> suggestions = new ArrayList<>();

        if (!contentDescription.isBlank()) {
            suggestions.add(priority(document, "content-desc", contentDescription)
                    + " AppiumBy.accessibilityId(\"" + javaEscape(contentDescription) + "\")");
        }

        if (!resourceId.isBlank()) {
            suggestions.add(priority(document, "resource-id", resourceId)
                    + " AppiumBy.id(\"" + javaEscape(resourceId) + "\")");
        }

        if (!text.isBlank()) {
            suggestions.add(priority(document, "text", text)
                    + " AppiumBy.androidUIAutomator("
                    + "\"new UiSelector().text(\\\"" + javaEscape(text) + "\\\")\")");
            suggestions.add("[XPATH] By.xpath(\"//" + className
                    + "[@text=" + javaEscape(xpathLiteral(text)) + "]\")");

            if (!isTrue(element, "clickable")) {
                suggestions.add("[CLICKABLE PARENT] By.xpath(\"//" + className
                        + "[@text=" + javaEscape(xpathLiteral(text)) + "]"
                        + "/ancestor::android.view.ViewGroup[@clickable='true'][1]\")");
            }
        }

        return suggestions;
    }

    private static String priority(Document document, String attribute, String value) {
        return isUnique(document, attribute, value) ? "[RECOMMENDED/UNIQUE]" : "[DUPLICATE]";
    }

    private static boolean isUnique(Document document, String attribute, String value) {
        if (value == null || value.isBlank()) return false;
        NodeList allNodes = document.getElementsByTagName("*");
        int matches = 0;
        for (int index = 0; index < allNodes.getLength(); index++) {
            Node node = allNodes.item(index);
            if (node instanceof Element element
                    && value.equals(element.getAttribute(attribute))) {
                matches++;
                if (matches > 1) return false;
            }
        }
        return matches == 1;
    }

    private static String xpathLiteral(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        if (!value.contains("\"")) return "\"" + value + "\"";

        String[] parts = value.split("'", -1);
        StringBuilder literal = new StringBuilder("concat(");
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) literal.append(", \"'\", ");
            literal.append("'").append(parts[index]).append("'");
        }
        return literal.append(')').toString();
    }

    private static String javaEscape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String attribute(Element element, String name) {
        return element.hasAttribute(name) ? element.getAttribute(name).trim() : "";
    }

    private static boolean isTrue(Element element, String attribute) {
        return "true".equalsIgnoreCase(attribute(element, attribute));
    }

    private static String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String sanitizeFileName(String value) {
        String safe = value == null ? "screen" : value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return safe.isBlank() ? "screen" : safe;
    }
}

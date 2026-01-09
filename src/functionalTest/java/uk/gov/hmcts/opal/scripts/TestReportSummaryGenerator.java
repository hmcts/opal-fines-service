package uk.gov.hmcts.opal.scripts;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestReportSummaryGenerator {

    public static void main(String[] args) throws Exception {
        List<Path> xmlIntegrationPaths = new ArrayList<>();
        List<Path> xmlFunctionalPaths = new ArrayList<>();

        final String reportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));


        Path integrationDir = Paths.get("build/test-results/integration");
        Path functionalDir = Paths.get("target/site/serenity");

        if (Files.exists(integrationDir)) {
            xmlIntegrationPaths.addAll(Files.list(integrationDir)
                                           .filter(p -> p.toString().endsWith(".xml"))
                                           .toList());
        }

        if (Files.exists(functionalDir)) {
            xmlFunctionalPaths.addAll(Files.list(functionalDir)
                                          .filter(p -> p.getFileName().toString().startsWith("SERENITY-JUNIT"))
                                          .filter(p -> p.toString().endsWith(".xml"))
                                          .toList());
        }

        List<String> integrationRows = new ArrayList<>();
        List<String> functionalRows = new ArrayList<>();

        // Integration tests
        for (Path xmlPath : xmlIntegrationPaths) {
            try (InputStream input = Files.newInputStream(xmlPath)) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
                Element root = doc.getDocumentElement();

                String suiteName = root.getAttribute("name");
                int total = Integer.parseInt(root.getAttribute("tests"));
                int failures = Integer.parseInt(root.getAttribute("failures"));
                int errors = Integer.parseInt(root.getAttribute("errors"));
                int skipped = Integer.parseInt(root.getAttribute("skipped"));
                int passed = total - failures - errors - skipped;

                String rowClass = (failures > 0 || errors > 0) ? " class=\"has-failure\"" : "";
                integrationRows.add(String.format(
                    """
                        <tr%s>
                            <td>%s</td>
                            <td>%d</td>
                            <td>%d</td>
                            <td class="failures">%d</td>
                            <td class="errors">%d</td>
                            <td>%d</td>
                        </tr>""", rowClass, suiteName, total, passed, failures, errors, skipped
                ));
            } catch (Exception e) {
                log.error("Error reading {}: {}", xmlPath, e.getMessage());
            }
        }

        // Functional tests
        for (Path xmlPath : xmlFunctionalPaths) {
            try (InputStream input = Files.newInputStream(xmlPath)) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
                Element root = doc.getDocumentElement();

                String suiteName = root.getAttribute("name");
                int total = Integer.parseInt(root.getAttribute("tests"));
                int failures = Integer.parseInt(root.getAttribute("failures"));
                int errors = Integer.parseInt(root.getAttribute("errors"));
                int skipped = Integer.parseInt(root.getAttribute("skipped"));
                int passed = total - failures - errors - skipped;

                String rowClass = (failures > 0 || errors > 0) ? " class=\"has-failure\"" : "";
                functionalRows.add(String.format(
                    """
                        <tr%s>
                            <td>%s</td>
                            <td>%d</td>
                            <td>%d</td>
                            <td class="failures">%d</td>
                            <td class="errors">%d</td>
                            <td>%d</td>
                        </tr>""", rowClass, suiteName, total, passed, failures, errors, skipped
                ));
            } catch (Exception e) {
                log.error("Error reading {}: {}", xmlPath, e.getMessage());
            }
        }


        int totalIntegration = 0;
        int passedIntegration = 0;
        int failedIntegration = 0;
        int errorIntegration = 0;
        int skippedIntegration = 0;
        for (Path xmlPath : xmlIntegrationPaths) {
            try (InputStream input = Files.newInputStream(xmlPath)) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
                Element root = doc.getDocumentElement();
                int total = Integer.parseInt(root.getAttribute("tests"));
                int failures = Integer.parseInt(root.getAttribute("failures"));
                int errors = Integer.parseInt(root.getAttribute("errors"));
                int skipped = Integer.parseInt(root.getAttribute("skipped"));
                int passed = total - failures - errors - skipped;

                totalIntegration += total;
                passedIntegration += passed;
                failedIntegration += failures;
                errorIntegration += errors;
                skippedIntegration += skipped;
            } catch (Exception e) {
                log.error("Exception: {}", String.valueOf(e));
            }
        }

        int totalFunctional = 0;
        int passedFunctional = 0;
        int failedFunctional = 0;
        int errorFunctional = 0;
        int skippedFunctional = 0;
        for (Path xmlPath : xmlFunctionalPaths) {
            try (InputStream input = Files.newInputStream(xmlPath)) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
                Element root = doc.getDocumentElement();
                int total = Integer.parseInt(root.getAttribute("tests"));
                int failures = Integer.parseInt(root.getAttribute("failures"));
                int errors = Integer.parseInt(root.getAttribute("errors"));
                int skipped = Integer.parseInt(root.getAttribute("skipped"));
                int passed = total - failures - errors - skipped;

                totalFunctional += total;
                passedFunctional += passed;
                failedFunctional += failures;
                errorFunctional += errors;
                skippedFunctional += skipped;
            } catch (Exception e) {
                log.error("Exception: {}", String.valueOf(e));
            }
        }

        int totalAll = totalIntegration + totalFunctional;
        int passedAll = passedIntegration + passedFunctional;
        int failedAll = failedIntegration + failedFunctional;
        int errorAll = errorIntegration + errorFunctional;
        int skippedAll = skippedIntegration + skippedFunctional;

        Path sourceCss = Path.of("src/functionalTest/java/uk/gov/hmcts/opal/scripts/styles.css");
        String cssContent = Files.readString(sourceCss, StandardCharsets.UTF_8);

        Path sourceScript = Path.of("src/functionalTest/java/uk/gov/hmcts/opal/scripts/script.js");
        String scriptContent = Files.readString(sourceScript, StandardCharsets.UTF_8);

        Path output = Paths.get("functional-test-report/htmlReport/test-summary.html");
        Files.createDirectories(output.getParent());
        Files.writeString(
            output, String.format(
                """
                        <html>
                        <head>
                            <style>
                            %s
                            </style>
                            <script>
                            %s
                            </script>
                        </head>
                        <body>
                        <h1>Test Summary Report</h1>
                        <p> Report generated data: %s</p>
                        <div class="summaries">
                        <div class="overall">
                        <h2>Overall Summary</h2>
                        <table>
                            <tr><th>Total</th><td>%d</td></tr>
                            <tr><th>Passed</th><td>%d</td></tr>
                            <tr><th>Failures</th><td>%d</td></tr>
                            <tr><th>Errors</th><td>%d</td></tr>
                            <tr><tr><th>Skipped</th><td>%d</td></tr>
                        </table>
                        </div>
                        <div class="integration">
                        <h2>Integration Summary</h2>
                        <table>
                            <tr><th>Total</th><td>%d</td></tr>
                            <tr><th>Passed</th><td>%d</td></tr>
                            <tr><th>Failures</th><td>%d</td></tr>
                            <tr><th>Errors</th><td>%d</td></tr>
                            <tr><tr><th>Skipped</th><td>%d</td></tr>
                        </table>
                        </div>
                        <div class="functional">
                        <h2>Functional Summary</h2>
                        <table>
                            <tr><th>Total</th><td>%d</td></tr>
                            <tr><th>Passed</th><td>%d</td></tr>
                            <tr><th>Failures</th><td>%d</td></tr>
                            <tr><th>Errors</th><td>%d</td></tr>
                            <tr><tr><th>Skipped</th><td>%d</td></tr>
                        </table>
                        </div>
                        </div>
                        <h2>Integration Tests</h2>
                        <button onclick="copyTestSummary('integrationTable')">Copy Test Suite + Total</button>
                        <button onclick="copyTotalsOnly('integrationTable')">Copy Total Only</button>
                        <table id="integrationTable">
                            <tr>
                                <th>Test Suite</th>
                                <th>Total</th>
                                <th>Passed</th>
                                <th>Failures</th>
                                <th>Errors</th>
                                <th>Skipped</th>
                            </tr>
                            %s
                        </table>

                        <h2>Functional Tests</h2>
                        <button onclick="copyTestSummary('functionalTable')">Copy Test Suite + Total</button>
                        <button onclick="copyTotalsOnly('functionalTable')">Copy Total Only</button>
                        <table id="functionalTable">
                            <tr>
                                <th>Test Suite</th>
                                <th>Total</th>
                                <th>Passed</th>
                                <th>Failures</th>
                                <th>Errors</th>
                                <th>Skipped</th>
                            </tr>
                            %s
                        </table>

                        </body></html>
                    """,
                cssContent,
                scriptContent,
                reportDate,
                totalAll,
                passedAll,
                failedAll,
                errorAll,
                skippedAll,
                totalIntegration,
                passedIntegration,
                failedIntegration,
                errorIntegration,
                skippedIntegration,
                totalFunctional,
                passedFunctional,
                failedFunctional,
                errorFunctional,
                skippedFunctional,
                String.join("\n", integrationRows),
                String.join("\n", functionalRows)
            ), StandardCharsets.UTF_8
        );

        log.info("HTML test summary written to: {}", output.toAbsolutePath());
    }
}

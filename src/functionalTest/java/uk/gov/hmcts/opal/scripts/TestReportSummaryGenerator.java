package uk.gov.hmcts.opal.scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Stream;

public class TestReportSummaryGenerator {
    private static final Logger log = LoggerFactory.getLogger(TestReportSummaryGenerator.class);

    public static void main(String[] args) throws Exception {
        ReportOptions options = ReportOptions.fromArgs(args);
        List<Path> xmlIntegrationPaths = listXmlFiles(options.integrationDir());
        List<Path> xmlFunctionalPaths = listXmlFiles(options.functionalDir());

        final String reportDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

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
                log.warn("Error reading {}: {}", xmlPath, e.getMessage());
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
                log.warn("Error reading {}: {}", xmlPath, e.getMessage());
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
                log.warn("Exception while processing integration test summary", e);
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
                log.warn("Exception while processing functional test summary", e);
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

        Path output = options.outputDir().resolve("htmlReport/test-summary.html");
        Files.createDirectories(output.getParent());
        Files.writeString(
            output, String.format(
                """
                        <html>
                        <head>
                            <title>%s</title>
                            <style>
                            %s
                            </style>
                            <script>
                            %s
                            </script>
                        </head>
                        <body>
                        <h1>%s</h1>
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
                options.title(),
                cssContent,
                scriptContent,
                options.title(),
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

    private static List<Path> listXmlFiles(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(directory)) {
            return files
                .filter(path -> path.toString().endsWith(".xml"))
                .sorted()
                .toList();
        }
    }

    private record ReportOptions(Path integrationDir, Path functionalDir, Path outputDir, String title) {
        private static final String DEFAULT_TITLE = "Test Summary Report";

        private static ReportOptions fromArgs(String[] args) {
            Path integrationDir = Paths.get("build/test-results/integration");
            Path functionalDir = Paths.get("build/test-results/functional");
            Path outputDir = Paths.get("functional-test-report");
            String title = DEFAULT_TITLE;

            for (String arg : args) {
                String[] parts = arg.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                switch (parts[0]) {
                    case "--integration-dir" -> integrationDir = Paths.get(parts[1]);
                    case "--functional-dir" -> functionalDir = Paths.get(parts[1]);
                    case "--output-dir" -> outputDir = Paths.get(parts[1]);
                    case "--title" -> title = parts[1];
                    default -> {
                        // Ignore unknown options to keep the generator backward compatible.
                    }
                }
            }

            return new ReportOptions(integrationDir, functionalDir, outputDir, title);
        }
    }
}

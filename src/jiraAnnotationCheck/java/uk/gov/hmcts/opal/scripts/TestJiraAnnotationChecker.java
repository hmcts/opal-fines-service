package uk.gov.hmcts.opal.scripts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Validates Jira metadata on runnable integration and functional tests.
 *
 * <p>Integration tests are parsed from Java source annotations, while functional tests are parsed from Cucumber
 * feature-file tags. Functional scenarios inherit tags from feature and scenario-outline scopes in the same way
 * Cucumber does, so failures are reported against the runnable scenario or examples block that needs fixing.
 */
public final class TestJiraAnnotationChecker {

    private static final Path DEFAULT_INTEGRATION_SOURCE_ROOT = Path.of("src/integrationTest/java");
    private static final Path DEFAULT_FUNCTIONAL_FEATURE_ROOT = Path.of("src/functionalTest/resources/features");
    private static final List<String> TEST_ANNOTATION_NAMES = List.of(
        "@Test",
        "@ParameterizedTest",
        "@RepeatedTest",
        "@TestFactory",
        "@TestTemplate"
    );
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^\\s*(?:public|protected|private|static|final|synchronized|abstract|default|native|\\s)*"
            + "[\\w<>\\[\\], ?.@]+?\\s+"
            + "(?<name>[A-Za-z_]\\w*)\\s*\\("
    );
    private static final Pattern JIRA_STORY_ANNOTATION_PATTERN = Pattern.compile("@JiraStory\\s*\\(");
    private static final Pattern JIRA_EPIC_ANNOTATION_PATTERN = Pattern.compile("@JiraEpic\\s*\\(");
    private static final Pattern JIRA_STORY_TAG_PATTERN = Pattern.compile("(?i)^@JIRA-STORY:.+");
    private static final Pattern JIRA_EPIC_TAG_PATTERN = Pattern.compile("(?i)^@JIRA-EPIC:.+");

    private TestJiraAnnotationChecker() {
        // Utility class.
    }

    /**
     * Runs the Jira metadata check and exits non-zero when any runnable test is missing required metadata.
     *
     * @param args optional integration source root and functional feature root
     * @throws IOException if a source file cannot be read
     */
    public static void main(String[] args) throws IOException {
        Path integrationSourceRoot = args.length > 0 ? Path.of(args[0]) : DEFAULT_INTEGRATION_SOURCE_ROOT;
        Path functionalFeatureRoot = args.length > 1 ? Path.of(args[1]) : DEFAULT_FUNCTIONAL_FEATURE_ROOT;

        CheckResult integrationResult = checkIntegrationTests(integrationSourceRoot);
        CheckResult functionalResult = checkFunctionalTests(functionalFeatureRoot);
        List<Violation> violations = new ArrayList<>();
        violations.addAll(integrationResult.violations());
        violations.addAll(functionalResult.violations());

        if (violations.isEmpty()) {
            System.out.printf(
                "Checked %d runnable integration test method(s) and %d runnable functional test case(s). "
                    + "All have at least one Jira story and exactly one Jira epic.%n",
                integrationResult.checkedTests(),
                functionalResult.checkedTests()
            );
            return;
        }

        System.err.println("Test Jira annotation check failed.");
        System.err.println("Each runnable integration test method must have:");
        System.err.println("- at least one @JiraStory annotation");
        System.err.println("- exactly one @JiraEpic annotation");
        System.err.println("Each runnable functional scenario or scenario-outline examples block must have:");
        System.err.println("- at least one @JIRA-STORY tag");
        System.err.println("- exactly one @JIRA-EPIC tag");
        System.err.println();
        System.err.printf("Found %d failing test case(s):%n", violations.size());

        violations.forEach(violation -> {
            System.err.printf(
                "- %s %s:%d %s%n",
                violation.testType(),
                violation.sourcePath(),
                violation.lineNumber(),
                violation.testIdentifier()
            );
            violation.reasons().forEach(reason -> System.err.printf("  - %s%n", reason));
        });

        System.exit(1);
    }

    /**
     * Checks Java integration-test methods for Jira annotations.
     *
     * @param sourceRoot integration test source root
     * @return count and violations for runnable JUnit methods
     * @throws IOException if a Java source file cannot be read
     */
    private static CheckResult checkIntegrationTests(Path sourceRoot) throws IOException {
        if (!Files.isDirectory(sourceRoot)) {
            throw new IllegalArgumentException("Integration test source root does not exist: " + sourceRoot);
        }

        List<Violation> violations = new ArrayList<>();
        int checkedTests = 0;

        for (Path javaFile : files(sourceRoot, ".java")) {
            FileCheckResult fileResult = checkJavaFile(sourceRoot, javaFile);
            checkedTests += fileResult.checkedTests();
            violations.addAll(fileResult.violations());
        }

        return new CheckResult(checkedTests, violations);
    }

    /**
     * Checks Cucumber functional scenarios and scenario-outline examples for Jira tags.
     *
     * @param featureRoot functional feature root
     * @return count and violations for runnable Cucumber cases
     * @throws IOException if a feature file cannot be read
     */
    private static CheckResult checkFunctionalTests(Path featureRoot) throws IOException {
        if (!Files.isDirectory(featureRoot)) {
            throw new IllegalArgumentException("Functional test feature root does not exist: " + featureRoot);
        }

        List<Violation> violations = new ArrayList<>();
        int checkedTests = 0;

        for (Path featureFile : files(featureRoot, ".feature")) {
            FileCheckResult fileResult = checkFeatureFile(featureRoot, featureFile);
            checkedTests += fileResult.checkedTests();
            violations.addAll(fileResult.violations());
        }

        return new CheckResult(checkedTests, violations);
    }

    /**
     * Finds source files below a root in stable order.
     *
     * @param sourceRoot directory to scan
     * @param suffix file suffix to include
     * @return sorted matching files
     * @throws IOException if the directory cannot be walked
     */
    private static List<Path> files(Path sourceRoot, String suffix) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(suffix))
                .sorted(Comparator.comparing(Path::toString))
                .toList();
        }
    }

    /**
     * Parses a Java source file and checks each runnable test method.
     *
     * @param sourceRoot root used for relative violation paths
     * @param javaFile Java source file to parse
     * @return count and violations for the file
     * @throws IOException if the Java source file cannot be read
     */
    private static FileCheckResult checkJavaFile(Path sourceRoot, Path javaFile) throws IOException {
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
        List<String> annotations = new ArrayList<>();
        List<Violation> violations = new ArrayList<>();
        String className = javaFile.getFileName().toString().replaceFirst("\\.java$", "");
        int checkedTests = 0;
        int index = 0;

        while (index < lines.size()) {
            String rawLine = lines.get(index);
            String strippedLine = stripLineComment(rawLine).strip();

            if (strippedLine.isBlank() || strippedLine.startsWith("//")) {
                index++;
                continue;
            }

            if (strippedLine.startsWith("@")) {
                AnnotationBlock annotation = readAnnotation(lines, index);
                annotations.add(annotation.text());
                index = annotation.endIndex() + 1;
                continue;
            }

            SignatureBlock signature = readSignature(lines, index);
            String methodName = extractMethodName(signature.text());

            if (methodName != null && hasRunnableTestAnnotation(annotations)) {
                checkedTests++;
                List<String> reasons = javaViolationReasons(annotations);
                if (!reasons.isEmpty()) {
                    violations.add(new Violation(
                        "integration",
                        sourceRoot.relativize(javaFile),
                        signature.startIndex() + 1,
                        className + "#" + methodName,
                        reasons
                    ));
                }
            }

            annotations.clear();
            index = signature.endIndex() + 1;
        }

        return new FileCheckResult(checkedTests, violations);
    }

    /**
     * Parses a Cucumber feature file and checks each runnable scenario or examples block.
     *
     * @param featureRoot root used for relative violation paths
     * @param featureFile feature file to parse
     * @return count and violations for the file
     * @throws IOException if the feature file cannot be read
     */
    private static FileCheckResult checkFeatureFile(Path featureRoot, Path featureFile) throws IOException {
        List<String> lines = Files.readAllLines(featureFile, StandardCharsets.UTF_8);
        List<Violation> violations = new ArrayList<>();
        List<String> pendingTags = new ArrayList<>();
        List<String> featureTags = new ArrayList<>();
        List<String> currentScenarioTags = new ArrayList<>();
        String featureName = featureFile.getFileName().toString();
        String currentScenarioName = "";
        boolean currentScenarioOutline = false;
        int checkedTests = 0;

        for (int index = 0; index < lines.size(); index++) {
            String strippedLine = stripFeatureComment(lines.get(index)).strip();

            if (strippedLine.isBlank()) {
                continue;
            }

            if (strippedLine.startsWith("@")) {
                pendingTags.addAll(parseTags(strippedLine));
                continue;
            }

            if (strippedLine.startsWith("Feature:")) {
                featureName = strippedLine.substring("Feature:".length()).strip();
                featureTags = List.copyOf(pendingTags);
                pendingTags.clear();
                continue;
            }

            if (strippedLine.startsWith("Scenario Outline:")) {
                currentScenarioOutline = true;
                currentScenarioName = strippedLine.substring("Scenario Outline:".length()).strip();
                currentScenarioTags = combine(featureTags, pendingTags);
                pendingTags.clear();
                continue;
            }

            if (strippedLine.startsWith("Scenario:")) {
                currentScenarioOutline = false;
                currentScenarioName = strippedLine.substring("Scenario:".length()).strip();
                List<String> scenarioTags = combine(featureTags, pendingTags);
                pendingTags.clear();
                if (!hasIgnoreTag(scenarioTags)) {
                    checkedTests++;
                    violations.addAll(featureViolations(
                        featureRoot,
                        featureFile,
                        index + 1,
                        featureName + " > Scenario: " + currentScenarioName,
                        scenarioTags
                    ));
                }
                continue;
            }

            if (currentScenarioOutline && strippedLine.startsWith("Examples:")) {
                List<String> examplesTags = combine(currentScenarioTags, pendingTags);
                String examplesName = strippedLine.substring("Examples:".length()).strip();
                String identifier = featureName + " > Scenario Outline: " + currentScenarioName + " > Examples";
                if (!examplesName.isBlank()) {
                    identifier += ": " + examplesName;
                }

                pendingTags.clear();
                if (!hasIgnoreTag(examplesTags)) {
                    checkedTests++;
                    violations.addAll(featureViolations(
                        featureRoot,
                        featureFile,
                        index + 1,
                        identifier,
                        examplesTags
                    ));
                }
            }
        }

        return new FileCheckResult(checkedTests, violations);
    }

    /**
     * Reads a Java annotation, including multi-line annotation arguments.
     *
     * @param lines source lines
     * @param startIndex zero-based start line for the annotation
     * @return annotation text and final source line index
     */
    private static AnnotationBlock readAnnotation(List<String> lines, int startIndex) {
        int depth = 0;
        boolean seenOpen = false;
        boolean inString = false;
        boolean escaped = false;
        StringBuilder annotationText = new StringBuilder();

        for (int index = startIndex; index < lines.size(); index++) {
            String line = stripLineComment(lines.get(index));
            annotationText.append(line.strip()).append(' ');

            for (int charIndex = 0; charIndex < line.length(); charIndex++) {
                char character = line.charAt(charIndex);
                if (inString) {
                    if (escaped) {
                        escaped = false;
                    } else if (character == '\\') {
                        escaped = true;
                    } else if (character == '"') {
                        inString = false;
                    }
                    continue;
                }

                if (character == '"') {
                    inString = true;
                } else if (character == '(') {
                    depth++;
                    seenOpen = true;
                } else if (character == ')') {
                    depth--;
                }
            }

            if (!seenOpen || depth == 0) {
                return new AnnotationBlock(annotationText.toString(), index);
            }
        }

        throw new IllegalArgumentException("Unterminated annotation starting on line " + (startIndex + 1));
    }

    /**
     * Reads a Java method signature, including wrapped method declarations.
     *
     * @param lines source lines
     * @param startIndex zero-based start line for the signature
     * @return signature text and source line bounds
     */
    private static SignatureBlock readSignature(List<String> lines, int startIndex) {
        StringBuilder signatureText = new StringBuilder(stripLineComment(lines.get(startIndex)).strip());
        int endIndex = startIndex;

        while (endIndex + 1 < lines.size()
            && !signatureText.toString().contains("{")
            && !signatureText.toString().contains(";")) {
            String nextLine = stripLineComment(lines.get(endIndex + 1)).strip();
            if (nextLine.isBlank() || nextLine.startsWith("@")) {
                break;
            }
            signatureText.append(' ').append(nextLine);
            endIndex++;
        }

        return new SignatureBlock(signatureText.toString(), startIndex, endIndex);
    }

    /**
     * Removes a Java line comment while preserving comment markers inside string literals.
     *
     * @param line source line
     * @return line content before a real line comment
     */
    private static String stripLineComment(String line) {
        boolean inString = false;
        boolean escaped = false;

        for (int index = 0; index < line.length() - 1; index++) {
            char character = line.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    inString = false;
                }
                continue;
            }

            if (character == '"') {
                inString = true;
            } else if (character == '/' && line.charAt(index + 1) == '/') {
                return line.substring(0, index);
            }
        }

        return line;
    }

    /**
     * Removes a Cucumber feature-file comment.
     *
     * @param line feature-file line
     * @return line content before a comment marker
     */
    private static String stripFeatureComment(String line) {
        int commentIndex = line.indexOf('#');
        return commentIndex == -1 ? line : line.substring(0, commentIndex);
    }

    /**
     * Extracts a Java method name from a signature.
     *
     * @param signature method signature text
     * @return method name, or {@code null} when the signature is not a method
     */
    private static String extractMethodName(String signature) {
        Matcher matcher = METHOD_PATTERN.matcher(signature);
        return matcher.find() ? matcher.group("name") : null;
    }

    /**
     * Determines whether collected annotations include a runnable JUnit test annotation.
     *
     * @param annotations annotation blocks immediately before a method
     * @return {@code true} when the method is runnable as a JUnit test
     */
    private static boolean hasRunnableTestAnnotation(List<String> annotations) {
        return annotations.stream().anyMatch(TestJiraAnnotationChecker::isRunnableTestAnnotation);
    }

    /**
     * Determines whether one annotation block starts with a runnable JUnit annotation.
     *
     * @param annotation annotation block text
     * @return {@code true} when the annotation is a runnable JUnit test annotation
     */
    private static boolean isRunnableTestAnnotation(String annotation) {
        String strippedAnnotation = annotation.stripLeading();
        return TEST_ANNOTATION_NAMES.stream()
            .anyMatch(testAnnotation -> isAnnotationNameMatch(strippedAnnotation, testAnnotation));
    }

    /**
     * Matches a Java annotation name without allowing prefix-only matches.
     *
     * @param annotation annotation text
     * @param annotationName annotation name to match, including the leading {@code @}
     * @return {@code true} when the annotation name matches exactly
     */
    private static boolean isAnnotationNameMatch(String annotation, String annotationName) {
        if (!annotation.startsWith(annotationName)) {
            return false;
        }

        if (annotation.length() == annotationName.length()) {
            return true;
        }

        char nextCharacter = annotation.charAt(annotationName.length());
        return Character.isWhitespace(nextCharacter) || nextCharacter == '(';
    }

    /**
     * Builds failure reasons for a Java integration test method.
     *
     * @param annotations annotations immediately before a runnable test method
     * @return rule failures for the method
     */
    private static List<String> javaViolationReasons(List<String> annotations) {
        int jiraStoryCount = countAnnotations(annotations, JIRA_STORY_ANNOTATION_PATTERN);
        int jiraEpicCount = countAnnotations(annotations, JIRA_EPIC_ANNOTATION_PATTERN);
        List<String> reasons = new ArrayList<>();

        if (jiraStoryCount == 0) {
            reasons.add("missing @JiraStory annotation");
        }

        if (jiraEpicCount == 0) {
            reasons.add("missing @JiraEpic annotation");
        } else if (jiraEpicCount > 1) {
            reasons.add("has " + jiraEpicCount + " @JiraEpic annotations; expected exactly one");
        }

        return reasons;
    }

    /**
     * Builds a functional-test violation when the provided tags break the Jira metadata rules.
     *
     * @param featureRoot root used for relative violation paths
     * @param featureFile feature file containing the scenario
     * @param lineNumber one-based scenario or examples line number
     * @param identifier human-readable scenario or examples identifier
     * @param tags effective Cucumber tags after inheritance
     * @return a single violation, or an empty list when the tags are valid
     */
    private static List<Violation> featureViolations(
        Path featureRoot,
        Path featureFile,
        int lineNumber,
        String identifier,
        List<String> tags
    ) {
        List<String> reasons = featureViolationReasons(tags);
        if (reasons.isEmpty()) {
            return List.of();
        }

        return List.of(new Violation(
            "functional",
            featureRoot.relativize(featureFile),
            lineNumber,
            identifier,
            reasons
        ));
    }

    /**
     * Builds failure reasons for a functional scenario or examples block.
     *
     * @param tags effective Cucumber tags after inheritance
     * @return rule failures for the scenario or examples block
     */
    private static List<String> featureViolationReasons(List<String> tags) {
        int jiraStoryCount = countTags(tags, JIRA_STORY_TAG_PATTERN);
        int jiraEpicCount = countTags(tags, JIRA_EPIC_TAG_PATTERN);
        List<String> reasons = new ArrayList<>();

        if (jiraStoryCount == 0) {
            reasons.add("missing @JIRA-STORY tag");
        }

        if (jiraEpicCount == 0) {
            reasons.add("missing @JIRA-EPIC tag");
        } else if (jiraEpicCount > 1) {
            reasons.add("has " + jiraEpicCount + " @JIRA-EPIC tags; expected exactly one");
        }

        return reasons;
    }

    /**
     * Counts matching Java annotations in collected annotation blocks.
     *
     * @param annotations annotation block text
     * @param pattern annotation pattern to count
     * @return number of matching annotations
     */
    private static int countAnnotations(List<String> annotations, Pattern pattern) {
        int count = 0;
        for (String annotation : annotations) {
            Matcher matcher = pattern.matcher(annotation);
            while (matcher.find()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts matching Cucumber tags.
     *
     * @param tags effective Cucumber tags
     * @param pattern tag pattern to count
     * @return number of matching tags
     */
    private static int countTags(List<String> tags, Pattern pattern) {
        return (int)tags.stream().filter(tag -> pattern.matcher(tag).matches()).count();
    }

    /**
     * Checks whether a functional test case should be ignored by the metadata gate.
     *
     * @param tags effective Cucumber tags
     * @return {@code true} when the case has {@code @Ignore}
     */
    private static boolean hasIgnoreTag(List<String> tags) {
        return tags.stream().anyMatch(tag -> "@Ignore".equalsIgnoreCase(tag));
    }

    /**
     * Splits a Cucumber tag line into individual tags.
     *
     * @param tagLine feature-file tag line
     * @return tags from the line
     */
    private static List<String> parseTags(String tagLine) {
        return Stream.of(tagLine.split("\\s+"))
            .filter(tag -> !tag.isBlank())
            .toList();
    }

    /**
     * Combines inherited and local Cucumber tags.
     *
     * @param inheritedTags tags inherited from feature or scenario-outline scope
     * @param ownTags tags applied directly to the current scope
     * @return combined immutable tag list
     */
    private static List<String> combine(List<String> inheritedTags, List<String> ownTags) {
        List<String> combinedTags = new ArrayList<>(inheritedTags);
        combinedTags.addAll(ownTags);
        return List.copyOf(combinedTags);
    }

    /**
     * Aggregated check result for one test source set.
     *
     * @param checkedTests number of runnable tests checked
     * @param violations metadata rule violations found
     */
    private record CheckResult(int checkedTests, List<Violation> violations) {
    }

    /**
     * Aggregated check result for one source file.
     *
     * @param checkedTests number of runnable tests checked in the file
     * @param violations metadata rule violations found in the file
     */
    private record FileCheckResult(int checkedTests, List<Violation> violations) {
    }

    /**
     * Java annotation block extracted from source.
     *
     * @param text full annotation text
     * @param endIndex zero-based final line index of the annotation
     */
    private record AnnotationBlock(String text, int endIndex) {
    }

    /**
     * Java method signature block extracted from source.
     *
     * @param text full signature text
     * @param startIndex zero-based first line index of the signature
     * @param endIndex zero-based final line index of the signature
     */
    private record SignatureBlock(String text, int startIndex, int endIndex) {
    }

    /**
     * One metadata rule violation reported by the checker.
     *
     * @param testType integration or functional
     * @param sourcePath path to the source file relative to its source root
     * @param lineNumber one-based line number for the runnable test
     * @param testIdentifier human-readable test identifier
     * @param reasons rule failures for the test
     */
    private record Violation(
        String testType,
        Path sourcePath,
        int lineNumber,
        String testIdentifier,
        List<String> reasons
    ) {
    }
}

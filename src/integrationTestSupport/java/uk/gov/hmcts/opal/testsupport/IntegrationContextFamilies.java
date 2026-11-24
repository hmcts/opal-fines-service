package uk.gov.hmcts.opal.testsupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.springframework.test.context.BootstrapUtils;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.support.TestPropertySourceUtils;
import tools.jackson.databind.json.JsonMapper;

/** Discovers executable integration classes without refreshing their Spring contexts. */
public final class IntegrationContextFamilies {

    private static final String MODE_PROPERTY = "launchdarkly.default-flag-values.is-legacy-mode";
    private static final String SECURITY_BASE = "uk.gov.hmcts.opal.AbstractIntegrationWithSecurityTest";
    private static final String MANUAL_CONNECTIVITY_TEST =
        "uk.gov.hmcts.opal.config.ReportQueueConnectivityIntegrationTest";

    private IntegrationContextFamilies() {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        final Path output = Path.of(args[0]);
        Set<Path> roots = new LinkedHashSet<>();
        for (int index = 1; index < args.length; index++) {
            Path root = Path.of(args[index]);
            if (Files.isDirectory(root)) {
                roots.add(root);
            }
        }
        Launcher launcher = LauncherFactory.create();
        TestPlan plan = launcher.discover(LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClasspathRoots(roots))
            .filters(ClassNameFilter.includeClassNamePatterns(".*")).build());
        Map<Class<?>, Set<Class<?>>> owners = new LinkedHashMap<>();
        for (var root : plan.getRoots()) {
            for (var identifier : plan.getDescendants(root)) {
                if (identifier.getSource().orElse(null) instanceof ClassSource source) {
                    Class<?> type = Class.forName(source.getClassName(), false,
                        IntegrationContextFamilies.class.getClassLoader());
                    owners.computeIfAbsent(owner(type), ignored -> new LinkedHashSet<>()).add(type);
                }
            }
        }
        List<Membership> memberships = new ArrayList<>();
        List<String> excluded = new ArrayList<>();
        for (var entry : owners.entrySet()) {
            if (entry.getKey().getName().equals(MANUAL_CONNECTIVITY_TEST)) {
                excluded.add(entry.getKey().getName());
            } else {
                memberships.add(classify(entry.getKey(), entry.getValue()));
            }
        }
        memberships.sort(Comparator.comparing(Membership::className));
        Files.createDirectories(output.getParent());
        JsonMapper.builder().build().writerWithDefaultPrettyPrinter()
            .writeValue(output.toFile(), Map.of("classes", memberships, "excluded", excluded));
        System.out.printf("Discovered %d integration classes; %d intentional exclusion(s). Inventory: %s%n",
            memberships.size(), excluded.size(), output);
    }

    static Membership classify(Class<?> owner, Collection<Class<?>> contexts) {
        Set<String> families = new LinkedHashSet<>();
        Set<String> profiles = new LinkedHashSet<>();
        for (Class<?> type : contexts) {
            if (hasSecurityFoundation(type)) {
                families.add("integrationSecurity");
            }
            if (TestContextAnnotationUtils.hasAnnotation(type, BootstrapWith.class)
                || TestContextAnnotationUtils.hasAnnotation(type, ContextConfiguration.class)) {
                MergedContextConfiguration configuration = BootstrapUtils.resolveTestContextBootstrapper(type)
                    .buildMergedContextConfiguration();
                profiles.addAll(List.of(configuration.getActiveProfiles()));
                Map<String, Object> properties = TestPropertySourceUtils.convertInlinedPropertiesToMap(
                    configuration.getPropertySourceProperties());
                families.add(family(List.of(configuration.getActiveProfiles()), properties));
            } else {
                families.add("integrationBase");
            }
        }
        String selected;
        String reason;
        if (families.contains("integrationSecurity")) {
            selected = "integrationSecurity";
            reason = "Spring Security profile or security test foundation; takes precedence over mode";
        } else if (families.size() == 1) {
            selected = families.iterator().next();
            reason = selected.equals("integrationBase") ? "Shared/default context or non-Spring test"
                : "Explicit mode profile or inline test property";
        } else {
            selected = "integrationBase";
            reason = "Owning class spans context families " + families + "; keep nested tests together";
        }
        return new Membership(owner.getName(), selected, reason, List.copyOf(profiles));
    }

    static String family(List<String> profiles, Map<String, ?> properties) {
        if (profiles.contains("integration-with-spring-security")) {
            return "integrationSecurity";
        }
        Object legacy = properties.get(MODE_PROPERTY);
        if (legacy != null) {
            if (!legacy.equals("true") && !legacy.equals("false")) {
                throw new IllegalArgumentException("Unresolved integration mode: " + legacy);
            }
            return legacy.equals("true") ? "integrationLegacy" : "integrationOpal";
        }
        if (profiles.contains("opal") && profiles.contains("legacy")) {
            throw new IllegalArgumentException("Conflicting Opal/legacy profiles require an explicit mode property");
        }
        if (profiles.contains("legacy")) {
            return "integrationLegacy";
        }
        return profiles.contains("opal") ? "integrationOpal" : "integrationBase";
    }

    private static boolean hasSecurityFoundation(Class<?> type) {
        for (Class<?> parent = type; parent != null; parent = parent.getSuperclass()) {
            if (parent.getName().equals(SECURITY_BASE)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> owner(Class<?> type) {
        Class<?> owner = type;
        while (owner.getEnclosingClass() != null) {
            owner = owner.getEnclosingClass();
        }
        return owner;
    }

    public record Membership(String className, String task, String reason, List<String> profiles) {
    }
}

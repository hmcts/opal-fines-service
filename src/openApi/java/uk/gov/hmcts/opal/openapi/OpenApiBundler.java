package uk.gov.hmcts.opal.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenApiBundler {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    // All OpenAPI component sections that can contain $ref targets
    private static final List<String> COMPONENT_SECTIONS = List.of(
        "schemas", "responses", "parameters", "headers",
        "requestBodies", "examples", "links", "callbacks", "securitySchemes", "pathItems"
    );

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: OpenApiBundler <inputDir> <outputFile>");
            System.exit(1);
        }

        final Path inputDir = Paths.get(args[0]);
        final Path outputFile = Paths.get(args[1]);

        Map<String, Object> bundled = new LinkedHashMap<>();
        bundled.put("openapi", "3.1.1");
        bundled.put("info", Map.of("title", "Bundled API", "version", "1.0.0"));
        bundled.put("paths", new LinkedHashMap<>());
        Map<String, Object> bundledComponents = new LinkedHashMap<>();
        bundled.put("components", bundledComponents);

        // Pre-create empty maps for known component sections
        for (String section : COMPONENT_SECTIONS) {
            bundledComponents.put(section, new LinkedHashMap<String, Object>());
        }

        Files.list(inputDir)
            .filter(f -> f.toString().endsWith(".yaml"))
            .sorted() // deterministic
            .forEach(file -> {
                try {
                    Map<String, Object> yaml = mapper.readValue(file.toFile(), Map.class);
                    String suffix = capitalize(stripYaml(file.getFileName().toString()));

                    // Merge paths (rewrite local & cross-file refs)
                    Map<String, Object> paths = (Map<String, Object>) yaml.get("paths");
                    if (paths != null) {
                        paths.replaceAll((k, v) -> rewriteRefs(v, suffix));
                        ((Map<String, Object>) bundled.get("paths")).putAll(paths);
                    }

                    // Merge ALL component sections, adding the file suffix to each component key
                    Map<String, Object> components = (Map<String, Object>) yaml.get("components");
                    if (components != null) {
                        for (String section : COMPONENT_SECTIONS) {
                            Map<String, Object> src = (Map<String, Object>) components.get(section);
                            if (src == null) {
                                continue;
                            }

                            Map<String, Object> dst = getOrCreateSection(bundledComponents, section);
                            for (Map.Entry<String, Object> e : src.entrySet()) {
                                String oldName = e.getKey();
                                String newName = maybeSuffix(oldName, suffix);
                                Object valueWithRewrites = rewriteRefs(e.getValue(), suffix);

                                if (dst.containsKey(newName)) {
                                    // You can choose to overwrite, skip, or fail. Failing is safest.
                                    throw new IllegalStateException(
                                        "Name collision in components/" + section + ": " + newName + " (from "
                                            + file.getFileName() + ")"
                                    );
                                }
                                dst.put(newName, valueWithRewrites);
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException("While processing " + file.getFileName() + ": " + e.getMessage(), e);
                }
            });

        mapper.writeValue(outputFile.toFile(), bundled);
    }

    @SuppressWarnings("unchecked")
    private static Object rewriteRefs(Object node, String currentSuffix) {
        if (node instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<>((Map<String, Object>) node);

            Object refVal = map.get("$ref");
            if (refVal instanceof String ref) {
                // Case 1: external file reference: ./file.yaml#/components/<section>/<name>[...]
                if (ref.startsWith("./")) {
                    String[] fileAndPath = ref.split("#", 2);
                    String fileName = stripYaml(new File(fileAndPath[0]).getName());
                    String suffix = capitalize(fileName);

                    if (fileAndPath.length == 2 && fileAndPath[1].startsWith("/components/")) {
                        String componentPath = fileAndPath[1].replaceFirst("^/components/", "");
                        map.put("$ref", "#/components/" + addSuffixToLastSegment(componentPath, suffix));
                    }
                // Case 2: local reference: #/components/<section>/<name>[...]
                } else if (ref.startsWith("#/components/")) {
                    String componentPath = ref.replaceFirst("^#/components/", "");
                    String suffix = currentSuffix; // already capitalized outside
                    map.put("$ref", "#/components/" + addSuffixToLastSegment(componentPath, suffix));
                }
            }

            // Recurse
            map.replaceAll((k, v) -> rewriteRefs(v, currentSuffix));
            return map;
        } else if (node instanceof List) {
            List<Object> list = new ArrayList<>();
            for (Object item : (List<Object>) node) {
                list.add(rewriteRefs(item, currentSuffix));
            }
            return list;
        }
        return node;
    }

    // Append suffix only to the final name segment, keeping the section and any trailing JSON Pointers intact.
    // e.g. "headers/ETagCommon" -> "headers/ETagCommonX"
    //      "schemas/MyThing/allOf/0" -> "schemas/MyThingX/allOf/0"
    private static String addSuffixToLastSegment(String componentPath, String suffix) {
        int slash = componentPath.indexOf('/'); // first slash after section
        if (slash < 0) {
            return componentPath; // malformed; don't touch
        }

        String section = componentPath.substring(0, slash);
        String rest = componentPath.substring(slash + 1);

        // Split rest at the next '/' to isolate the component name
        int next = rest.indexOf('/');
        String name = (next == -1) ? rest : rest.substring(0, next);
        String tail = (next == -1) ? "" : rest.substring(next); // includes the '/'

        String suffixedName = maybeSuffix(name, suffix);
        return section + "/" + suffixedName + tail;
    }

    // Only add the suffix if it's not already there
    private static String maybeSuffix(String name, String suffix) {
        return name.endsWith(suffix) ? name : name + suffix;
    }

    private static String stripYaml(String fileName) {
        return fileName.endsWith(".yaml") ? fileName.substring(0, fileName.length() - 5) : fileName;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getOrCreateSection(Map<String, Object> components, String section) {
        return (Map<String, Object>) components.computeIfAbsent(section, k -> new LinkedHashMap<>());
    }
}

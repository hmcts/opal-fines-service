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

/**
 * This is a support class used to combine multiple OpenAPI YAML files into a single bundled file. It rewrites $ref
 * references to ensure they point correctly within the bundled file. This is not part of the main application code and
 * is intended to be run as a standalone utility.
 */
public class OpenApiBundler {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

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
        bundled.put("components", Map.of("schemas", new LinkedHashMap<>()));

        Files.list(inputDir).filter(f -> f.toString().endsWith(".yaml")).forEach(file -> {
            try {
                Map<String, Object> yaml = mapper.readValue(file.toFile(), Map.class);
                String suffix = file.getFileName().toString().replace(".yaml", "");

                // Merge paths
                Map<String, Object> paths = (Map<String, Object>) yaml.get("paths");
                if (paths != null) {
                    paths.replaceAll((k, v) -> rewriteRefs(v, suffix));
                    ((Map<String, Object>) bundled.get("paths")).putAll(paths);
                }

                // Merge schemas
                Map<String, Object> comps = (Map<String, Object>) yaml.get("components");
                if (comps != null && comps.get("schemas") != null) {
                    Map<String, Object> schemas = (Map<String, Object>) comps.get("schemas");
                    Map<String, Object> bundledSchemas =
                        (Map<String, Object>) ((Map<String, Object>) bundled.get("components")).get("schemas");

                    for (Map.Entry<String, Object> e : schemas.entrySet()) {
                        String newName = e.getKey() + capitalize(suffix);
                        bundledSchemas.put(newName, rewriteRefs(e.getValue(), suffix));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mapper.writeValue(outputFile.toFile(), bundled);
    }

    @SuppressWarnings("unchecked")
    private static Object rewriteRefs(Object node, String currentSuffix) {
        if (node instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<>((Map<String, Object>) node);
            if (map.containsKey("$ref")) {
                String ref = map.get("$ref").toString();

                // Case 1: external file reference
                if (ref.startsWith("./")) {
                    // Example: ./types.yaml#/components/schemas/BigInt
                    String[] fileAndPath = ref.split("#", 2);
                    String fileName = new File(fileAndPath[0]).getName().replace(".yaml", "");
                    String suffix = capitalize(fileName);

                    if (fileAndPath.length == 2 && fileAndPath[1].startsWith("/components/")) {
                        String componentPath = fileAndPath[1].replace("/components/", "");
                        map.put("$ref", "#/components/" + addSuffix(componentPath, suffix));
                    }
                } else if (ref.startsWith("#/components/")) {
                    // Case 2: local reference
                    String componentPath = ref.replace("#/components/", "");
                    String suffix = capitalize(currentSuffix);
                    map.put("$ref", "#/components/" + addSuffix(componentPath, suffix));
                }
            }

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

    // Append suffix only to the last segment of the component path
    private static String addSuffix(String componentPath, String suffix) {
        // Example: "schemas/BigInt" → "schemas/BigIntTypes"
        int idx = componentPath.lastIndexOf('/');
        if (idx == -1) {
            return componentPath + suffix;
        }
        String prefix = componentPath.substring(0, idx + 1);
        String name = componentPath.substring(idx + 1);
        return prefix + name + suffix;
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}

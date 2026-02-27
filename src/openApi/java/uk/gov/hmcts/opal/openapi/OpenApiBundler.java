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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenApiBundler {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    // All OpenAPI component sections that can contain $ref targets
    private static final List<String> COMPONENT_SECTIONS = List.of(
        "schemas", "responses", "parameters", "headers",
        "requestBodies", "examples", "links", "callbacks", "securitySchemes", "pathItems"
    );

    private static final List<String> OPAL_VERSION_TYPE_KEYS = List.of("returnType", "requestType");
    private static final String OPAL_API_VERSIONS_KEY = "x-opal-api-versions";
    private static final String OPAL_DEFAULT_VERSION = "1.0";
    private static final List<String> HTTP_METHODS = List.of(
        "get", "put", "post", "delete", "options", "head", "patch", "trace"
    );
    private static final Pattern VERSIONED_MEDIA_TYPE = Pattern.compile("^(.*)\\+v(\\d+)$", Pattern.CASE_INSENSITIVE);

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

        Map<String, Object> codeGenBundled = mapper.convertValue(bundled, Map.class);
        ensureDefaultApiVersions(codeGenBundled);
        stripOneOfSchemasForCodegen(codeGenBundled);
        markSuccessResponsesForVersionedReturnTypes(codeGenBundled);
        normalizeVersionedMediaTypes(codeGenBundled);
        Path codeGenOutputFile = outputFile.resolveSibling("openapi-bundled-code-gen.yaml");
        mapper.writeValue(codeGenOutputFile.toFile(), codeGenBundled);
    }

    @SuppressWarnings("unchecked")
    private static void ensureDefaultApiVersions(Map<String, Object> root) {
        Object pathsObj = root.get("paths");
        if (!(pathsObj instanceof Map)) {
            return;
        }

        Map<String, Object> paths = (Map<String, Object>) pathsObj;
        for (Object pathItemObj : paths.values()) {
            if (!(pathItemObj instanceof Map)) {
                continue;
            }

            Map<String, Object> pathItem = (Map<String, Object>) pathItemObj;
            for (Map.Entry<String, Object> entry : pathItem.entrySet()) {
                String method = entry.getKey();
                if (!HTTP_METHODS.contains(method)) {
                    continue;
                }

                Object operationObj = entry.getValue();
                if (!(operationObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> operation = (Map<String, Object>) operationObj;
                Object existing = operation.get(OPAL_API_VERSIONS_KEY);
                if (existing instanceof List && !((List<?>) existing).isEmpty()) {
                    continue;
                }

                operation.remove(OPAL_API_VERSIONS_KEY);
                if (populateApiVersionsFromOperation(operation)) {
                    continue;
                }

                List<Map<String, Object>> versions = List.of(Map.of("version", OPAL_DEFAULT_VERSION));
                operation.put(OPAL_API_VERSIONS_KEY, versions);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean populateApiVersionsFromOperation(Map<String, Object> operation) {
        Map<String, Map<String, Object>> versionsByVersion = new LinkedHashMap<>();

        Object responsesObj = operation.get("responses");
        if (responsesObj instanceof Map) {
            Map<String, Object> responses = (Map<String, Object>) responsesObj;
            for (Object responseObj : responses.values()) {
                if (!(responseObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> response = (Map<String, Object>) responseObj;
                Object contentObj = response.get("content");
                if (contentObj instanceof Map) {
                    Map<String, Object> content = (Map<String, Object>) contentObj;
                    boolean foundVersionedMedia = collectVersionedSchemas(content, versionsByVersion,
                        "returnType", operation);
                    if (!foundVersionedMedia) {
                        collectOneOfVersions(content, versionsByVersion, "returnType", operation);
                    }
                }
            }
        }

        Object requestBodyObj = operation.get("requestBody");
        if (requestBodyObj instanceof Map) {
            Map<String, Object> requestBody = (Map<String, Object>) requestBodyObj;
            Object contentObj = requestBody.get("content");
            if (contentObj instanceof Map) {
                Map<String, Object> content = (Map<String, Object>) contentObj;
                boolean foundVersionedMedia = collectVersionedSchemas(content, versionsByVersion,
                    "requestType", operation);
                if (!foundVersionedMedia) {
                    collectOneOfVersions(content, versionsByVersion, "requestType", operation);
                }
            }
        }

        if (versionsByVersion.isEmpty()) {
            return false;
        }

        operation.put(OPAL_API_VERSIONS_KEY, new ArrayList<>(versionsByVersion.values()));
        return true;
    }

    @SuppressWarnings("unchecked")
    private static boolean collectVersionedSchemas(
        Map<String, Object> content,
        Map<String, Map<String, Object>> versionsByVersion,
        String typeKey,
        Map<String, Object> operation
    ) {
        boolean found = false;
        for (Map.Entry<String, Object> contentEntry : content.entrySet()) {
            String mediaType = contentEntry.getKey();
            Matcher matcher = VERSIONED_MEDIA_TYPE.matcher(mediaType);
            if (!matcher.matches()) {
                continue;
            }

            found = true;

            Object mediaTypeObj = contentEntry.getValue();
            if (!(mediaTypeObj instanceof Map)) {
                continue;
            }

            Map<String, Object> mediaTypeMap = (Map<String, Object>) mediaTypeObj;
            Object schemaObj = mediaTypeMap.get("schema");
            if (!(schemaObj instanceof Map)) {
                continue;
            }

            Map<String, Object> schema = (Map<String, Object>) schemaObj;
            Object refObj = schema.get("$ref");
            if (!(refObj instanceof String)) {
                continue;
            }

            String baseMediaType = matcher.group(1);
            String version = "V" + matcher.group(2);

            Map<String, Object> versionEntry = versionsByVersion.computeIfAbsent(version, key -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("version", key);
                return entry;
            });

            versionEntry.put(typeKey, stripSchemaPrefix((String) refObj));
            versionEntry.putIfAbsent("mediaType", baseMediaType);
            String operationId = buildVersionedOperationId(operation, version);
            if (operationId != null) {
                versionEntry.putIfAbsent("operationId", operationId);
            }
        }

        return found;
    }

    @SuppressWarnings("unchecked")
    private static void collectOneOfVersions(
        Map<String, Object> content,
        Map<String, Map<String, Object>> versionsByVersion,
        String typeKey,
        Map<String, Object> operation
    ) {
        for (Map.Entry<String, Object> contentEntry : content.entrySet()) {
            Object mediaTypeObj = contentEntry.getValue();
            if (!(mediaTypeObj instanceof Map)) {
                continue;
            }

            Map<String, Object> mediaTypeMap = (Map<String, Object>) mediaTypeObj;
            Object schemaObj = mediaTypeMap.get("schema");
            if (!(schemaObj instanceof Map)) {
                continue;
            }

            Map<String, Object> schema = (Map<String, Object>) schemaObj;
            Object oneOfObj = schema.get("oneOf");
            if (!(oneOfObj instanceof List)) {
                continue;
            }

            for (Object itemObj : (List<Object>) oneOfObj) {
                if (!(itemObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> oneOfItem = (Map<String, Object>) itemObj;
                Object versionObj = oneOfItem.get("x-opal-version");
                Object refObj = oneOfItem.get("$ref");
                if (!(versionObj instanceof String) || !(refObj instanceof String)) {
                    continue;
                }

                String version = (String) versionObj;
                String baseMediaType = contentEntry.getKey();
                Map<String, Object> versionEntry = versionsByVersion.computeIfAbsent(version, key -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("version", key);
                    return entry;
                });

                versionEntry.put(typeKey, stripSchemaPrefix((String) refObj));
                versionEntry.putIfAbsent("mediaType", baseMediaType);
                String operationId = buildVersionedOperationId(operation, version);
                if (operationId != null) {
                    versionEntry.putIfAbsent("operationId", operationId);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void markSuccessResponsesForVersionedReturnTypes(Map<String, Object> root) {
        Object pathsObj = root.get("paths");
        if (!(pathsObj instanceof Map)) {
            return;
        }

        Map<String, Object> paths = (Map<String, Object>) pathsObj;
        for (Object pathItemObj : paths.values()) {
            if (!(pathItemObj instanceof Map)) {
                continue;
            }

            Map<String, Object> pathItem = (Map<String, Object>) pathItemObj;
            for (Map.Entry<String, Object> entry : pathItem.entrySet()) {
                String method = entry.getKey();
                if (!HTTP_METHODS.contains(method)) {
                    continue;
                }

                Object operationObj = entry.getValue();
                if (!(operationObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> operation = (Map<String, Object>) operationObj;
                Object responsesObj = operation.get("responses");
                if (!(responsesObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> responses = (Map<String, Object>) responsesObj;
                for (Map.Entry<String, Object> responseEntry : responses.entrySet()) {
                    String code = responseEntry.getKey();
                    if (!code.startsWith("2")) {
                        continue;
                    }

                    Object responseObj = responseEntry.getValue();
                    if (!(responseObj instanceof Map)) {
                        continue;
                    }

                    Map<String, Object> response = (Map<String, Object>) responseObj;
                    response.put("x-opal-use-version-return-type", true);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void normalizeVersionedMediaTypes(Map<String, Object> root) {
        Object pathsObj = root.get("paths");
        if (!(pathsObj instanceof Map)) {
            return;
        }

        Map<String, Object> paths = (Map<String, Object>) pathsObj;
        for (Object pathItemObj : paths.values()) {
            if (!(pathItemObj instanceof Map)) {
                continue;
            }

            Map<String, Object> pathItem = (Map<String, Object>) pathItemObj;
            for (Map.Entry<String, Object> entry : pathItem.entrySet()) {
                String method = entry.getKey();
                if (!HTTP_METHODS.contains(method)) {
                    continue;
                }

                Object operationObj = entry.getValue();
                if (!(operationObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> operation = (Map<String, Object>) operationObj;
                Object requestBodyObj = operation.get("requestBody");
                if (requestBodyObj instanceof Map) {
                    Map<String, Object> requestBody = (Map<String, Object>) requestBodyObj;
                    normalizeContentMap(requestBody, "content");
                }

                Object responsesObj = operation.get("responses");
                if (!(responsesObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> responses = (Map<String, Object>) responsesObj;
                for (Object responseObj : responses.values()) {
                    if (!(responseObj instanceof Map)) {
                        continue;
                    }

                    Map<String, Object> response = (Map<String, Object>) responseObj;
                    normalizeContentMap(response, "content");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void normalizeContentMap(Map<String, Object> container, String contentKey) {
        Object contentObj = container.get(contentKey);
        if (!(contentObj instanceof Map)) {
            return;
        }

        Map<String, Object> content = (Map<String, Object>) contentObj;
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> contentEntry : content.entrySet()) {
            String mediaType = contentEntry.getKey();
            Matcher matcher = VERSIONED_MEDIA_TYPE.matcher(mediaType);
            String baseMediaType = matcher.matches() ? matcher.group(1) : mediaType;
            normalized.putIfAbsent(baseMediaType, contentEntry.getValue());
        }

        container.put(contentKey, normalized);
    }

    private static String buildVersionedOperationId(Map<String, Object> operation, String version) {
        Object opIdObj = operation.get("operationId");
        if (!(opIdObj instanceof String)) {
            return null;
        }

        String opId = (String) opIdObj;
        String base = opId.replaceFirst("V\\d+$", "");
        return base + "V" + version.replace(".", "_");
    }

    @SuppressWarnings("unchecked")
    private static Object rewriteRefs(Object node, String currentSuffix) {
        if (node instanceof Map) {
            Map<String, Object> map = new LinkedHashMap<>((Map<String, Object>) node);

            Object refVal = map.get("$ref");
            if (refVal instanceof String ref) {
                map.put("$ref", rewriteComponentRefString(ref, currentSuffix));
            }

            for (String key : OPAL_VERSION_TYPE_KEYS) {
                Object typeRef = map.get(key);
                if (typeRef instanceof String ref) {
                    map.put(key, rewriteOpalVersionTypeString(ref, currentSuffix));
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

    private static String rewriteOpalVersionTypeString(String ref, String currentSuffix) {
        String rewritten = rewriteComponentRefString(ref, currentSuffix);
        return stripSchemaPrefix(rewritten);
    }

    private static String stripSchemaPrefix(String ref) {
        String schemaPrefix = "#/components/schemas/";
        return ref.startsWith(schemaPrefix) ? ref.substring(schemaPrefix.length()) : ref;
    }

    private static String rewriteComponentRefString(String ref, String currentSuffix) {
        if (ref.startsWith("./")) {
            String[] fileAndPath = ref.split("#", 2);
            String fileName = stripYaml(new File(fileAndPath[0]).getName());
            String suffix = capitalize(fileName);

            if (fileAndPath.length == 2 && fileAndPath[1].startsWith("/components/")) {
                String componentPath = fileAndPath[1].replaceFirst("^/components/", "");
                return "#/components/" + addSuffixToLastSegment(componentPath, suffix);
            }
            return ref;
        }

        if (ref.startsWith("#/components/")) {
            String componentPath = ref.replaceFirst("^#/components/", "");
            return "#/components/" + addSuffixToLastSegment(componentPath, currentSuffix);
        }

        return ref;
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

    @SuppressWarnings("unchecked")
    private static void stripOneOfSchemasForCodegen(Map<String, Object> root) {
        Object pathsObj = root.get("paths");
        if (!(pathsObj instanceof Map)) {
            return;
        }

        Map<String, Object> paths = (Map<String, Object>) pathsObj;
        for (Object pathItemObj : paths.values()) {
            if (!(pathItemObj instanceof Map)) {
                continue;
            }

            Map<String, Object> pathItem = (Map<String, Object>) pathItemObj;
            for (Map.Entry<String, Object> entry : pathItem.entrySet()) {
                String method = entry.getKey();
                if (!HTTP_METHODS.contains(method)) {
                    continue;
                }

                Object operationObj = entry.getValue();
                if (!(operationObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> operation = (Map<String, Object>) operationObj;
                Object requestBodyObj = operation.get("requestBody");
                if (requestBodyObj instanceof Map) {
                    Map<String, Object> requestBody = (Map<String, Object>) requestBodyObj;
                    stripOneOfFromContent(requestBody, "content");
                }

                Object responsesObj = operation.get("responses");
                if (!(responsesObj instanceof Map)) {
                    continue;
                }

                Map<String, Object> responses = (Map<String, Object>) responsesObj;
                for (Object responseObj : responses.values()) {
                    if (!(responseObj instanceof Map)) {
                        continue;
                    }

                    Map<String, Object> response = (Map<String, Object>) responseObj;
                    stripOneOfFromContent(response, "content");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void stripOneOfFromContent(Map<String, Object> container, String contentKey) {
        Object contentObj = container.get(contentKey);
        if (!(contentObj instanceof Map)) {
            return;
        }

        Map<String, Object> content = (Map<String, Object>) contentObj;
        for (Object mediaTypeObj : content.values()) {
            if (!(mediaTypeObj instanceof Map)) {
                continue;
            }

            Map<String, Object> mediaTypeMap = (Map<String, Object>) mediaTypeObj;
            Object schemaObj = mediaTypeMap.get("schema");
            if (!(schemaObj instanceof Map)) {
                continue;
            }

            Map<String, Object> schema = (Map<String, Object>) schemaObj;
            Object oneOfObj = schema.get("oneOf");
            if (!(oneOfObj instanceof List)) {
                continue;
            }

            String ref = firstRefFromOneOf((List<Object>) oneOfObj);
            if (ref != null) {
                mediaTypeMap.put("schema", Map.of("$ref", ref));
            } else {
                schema.remove("oneOf");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String firstRefFromOneOf(List<Object> oneOf) {
        for (Object itemObj : oneOf) {
            if (!(itemObj instanceof Map)) {
                continue;
            }

            Map<String, Object> item = (Map<String, Object>) itemObj;
            Object refObj = item.get("$ref");
            if (refObj instanceof String) {
                return (String) refObj;
            }
        }

        return null;
    }
}

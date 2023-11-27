package uk.gov.hmcts.opal.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.integrations.FileData;
import com.launchdarkly.sdk.server.subsystems.ComponentConfigurer;
import com.launchdarkly.sdk.server.subsystems.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class LaunchDarklyConfiguration {

    /**
     * Builds the client for Launch Darkly.
     *
     * @param sdkKey      sdk key to connect to launchdarkly.
     * @param offlineMode true to use launchdarkly offline mode.
     * @param flagFiles   (optional) a list of paths to json or yaml files containing flags for launchdarkly.
     *                    If there are duplicate keys, the first files have precedence.
     * @return Launch Darkly Client.
     */
    @Bean
    public LDClient ldClient(@Value("${launchdarkly.sdk-key}") String sdkKey,
                             @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode,
                             @Value("${launchdarkly.file:''}") String[] flagFiles) {
        LDConfig.Builder builder = new LDConfig.Builder().offline(offlineMode);
        getExistingFiles(flagFiles)
            .map(this::getDataSource)
            .ifPresent(builder::dataSource);
        return new LDClient(sdkKey, builder.build());
    }

    /**
     * Converts an array of paths in a datasource suitable for Launch Darkly.
     *
     * @param flagFilePaths an array of files that exist and that have to be used to create the launch darkly flags
     * @return a datasource able to combine the contents of all the files. If there are duplicated keys, the values
     *     on the first file have precedence
     */
    private ComponentConfigurer<DataSource> getDataSource(Path[] flagFilePaths) {
        return FileData.dataSource()
            .filePaths(flagFilePaths)
            .duplicateKeysHandling(FileData.DuplicateKeysHandling.IGNORE);
    }

    /**
     * Filters the array of files looking for the ones that can actually be found, and converts those to Path.
     *
     * @param files an array of strings describing files. Each starts with / if absolute.
     *              Otherwise, it's considered relative to the working directory.
     * @return an Optional containing those files describes which actually exist. Empty if files is null,
     *     empty or if none can be found.
     */
    private Optional<Path[]> getExistingFiles(String[] files) {
        if (files == null || files.length < 1) {
            return Optional.empty();
        } else {
            Path[] existing = Stream.of(files).map(this::getPathIfExists)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(Path[]::new);
            if (existing.length > 0) {
                return Optional.of(existing);
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * Tries to convert file into the Path of a file that can be found in the filesystem.
     *
     * @param file a string describing a file. Start with / if absolute path.
     * @return if it exists the described file, an Optional containing the path.
     *     An empty Optional otherwise.
     */
    private Optional<Path> getPathIfExists(String file) {
        if (StringUtils.isNotBlank(file)) {
            Path flagFile;
            if (file.startsWith("/")) {
                flagFile = Paths.get(file);
            } else {
                flagFile = Paths.get("").resolve(file);
            }
            if (Files.exists(flagFile)) {
                return Optional.of(flagFile);
            } else {
                log.debug("Could not find files defined by " + file);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}

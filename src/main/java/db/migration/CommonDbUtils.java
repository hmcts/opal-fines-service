package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommonDbUtils {

    public static void insertFile(Context context, String name, String file) throws IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getConfiguration().getDataSource());
        jdbcTemplate.update("INSERT INTO file_poc (name, value) VALUES ((?), (?))", name, readFileFromResources(file));
    }

    private static byte[] readFileFromResources(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        Path path = resource.getFile().toPath();
        return Files.readAllBytes(path);
    }
}

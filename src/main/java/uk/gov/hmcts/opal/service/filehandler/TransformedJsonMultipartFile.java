package uk.gov.hmcts.opal.service.filehandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public class TransformedJsonMultipartFile implements MultipartFile {

    private static final String PART_NAME = "file";

    private final String fileName;
    private final byte[] content;

    public TransformedJsonMultipartFile(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content.clone();
    }

    @Override
    public String getName() {
        return PART_NAME;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content.clone();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File destination) throws IOException {
        Files.write(destination.toPath(), content);
    }
}

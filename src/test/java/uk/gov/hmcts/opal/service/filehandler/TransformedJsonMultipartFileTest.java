package uk.gov.hmcts.opal.service.filehandler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TransformedJsonMultipartFileTest {

    @Test
    void shouldReturnPartName() {

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                "{}".getBytes());

        assertEquals("file", file.getName());
    }

    @Test
    void shouldReturnOriginalFilename() {

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                "{}".getBytes());

        assertEquals("test.json", file.getOriginalFilename());
    }

    @Test
    void shouldReturnJsonContentType() {

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                "{}".getBytes());

        assertEquals(
            MediaType.APPLICATION_JSON_VALUE,
            file.getContentType()
        );
    }

    @Test
    void shouldBeEmptyWhenContentIsEmpty() {

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                new byte[0]);

        assertTrue(file.isEmpty());
    }

    @Test
    void shouldNotBeEmptyWhenContentExists() {

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                "{}".getBytes());

        assertFalse(file.isEmpty());
    }

    @Test
    void shouldReturnSize() {

        byte[] content = "{}".getBytes();

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                content);

        assertEquals(content.length, file.getSize());
    }

    @Test
    void shouldReturnBytes() {

        byte[] content = "{\"test\":1}".getBytes();

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                content);

        assertArrayEquals(content, file.getBytes());
    }

    @Test
    void shouldReturnInputStream() throws Exception {

        byte[] content = "{\"test\":1}".getBytes();

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                content);

        assertArrayEquals(
            content,
            file.getInputStream().readAllBytes()
        );
    }

    @Test
    void shouldTransferContentToDestinationFile() throws Exception {

        byte[] content = "{\"test\":1}".getBytes();

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                content);

        File destination =
            File.createTempFile("multipart", ".json");

        try {
            file.transferTo(destination);

            assertArrayEquals(
                content,
                Files.readAllBytes(destination.toPath())
            );
        } finally {
            destination.delete();
        }
    }

    @Test
    void shouldDefensivelyCopyContentInConstructor() {

        byte[] content = "{\"test\":1}".getBytes();

        TransformedJsonMultipartFile file =
            new TransformedJsonMultipartFile(
                "test.json",
                content);

        content[0] = 'X';

        assertFalse(
            new String(file.getBytes()).startsWith("X")
        );
    }
}
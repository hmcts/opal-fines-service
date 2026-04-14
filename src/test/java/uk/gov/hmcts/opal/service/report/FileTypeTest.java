package uk.gov.hmcts.opal.service.report;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileTypeTest {

    @Test
    void shouldExposeSupportedFileTypesInStableOrder() {
        assertArrayEquals(new FileType[]{FileType.CSV, FileType.PDF, FileType.JSON, FileType.XML}, FileType.values());
    }

    @Test
    void shouldResolveFileTypeByName() {
        assertEquals(FileType.CSV, FileType.valueOf("CSV"));
        assertEquals(FileType.PDF, FileType.valueOf("PDF"));
        assertEquals(FileType.JSON, FileType.valueOf("JSON"));
        assertEquals(FileType.XML, FileType.valueOf("XML"));
    }
}

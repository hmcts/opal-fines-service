package uk.gov.hmcts.opal.entity.report;

/**
 * Enum representing supported file types for reports.
 * Mirrors the {@code r_supported_file_type_enum} PostgreSQL type.
 */
public enum SupportedFileType {
    CSV,
    PDF,
    XML
}


package uk.gov.hmcts.opal.entity.print;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.Column;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

class PrintJobEntityTest {

    @Test
    void shouldDefaultStatusToPending_whenUsingNoArgsConstructor() {
        PrintJobEntity printJobEntity = new PrintJobEntity();

        assertEquals(PrintStatus.PENDING, printJobEntity.getStatus());
    }

    @Test
    void shouldDefaultStatusToPending_whenUsingBuilder() {
        PrintJobEntity printJobEntity = PrintJobEntity.builder().build();

        assertEquals(PrintStatus.PENDING, printJobEntity.getStatus());
    }

    @Test
    void shouldMapStatusAsNamedPostgresEnum() throws NoSuchFieldException {
        Field field = PrintJobEntity.class.getDeclaredField("status");

        JdbcTypeCode jdbcTypeCode = field.getAnnotation(JdbcTypeCode.class);
        Column column = field.getAnnotation(Column.class);

        assertNotNull(jdbcTypeCode);
        assertEquals(SqlTypes.NAMED_ENUM, jdbcTypeCode.value());
        assertNotNull(column);
        assertEquals("t_print_job_status_enum", column.columnDefinition());
    }
}

package uk.gov.hmcts.opal.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.util.JsonPathUtil.DocContext;

class JsonPathUtilTest {

    static Stream<Arguments> returnsDefaultWhenPathUnknown() {
        DocContext context = JsonPathUtil.createDocContext("{}", "");
        final String unknownPath = "$.unknown";

        return Stream.of(
            Arguments.of(
                (Supplier<Object>) () -> JsonPathUtil.safeReadString(context, unknownPath, "def"),
                "def"
            ),
            Arguments.of(
                (Supplier<Object>) () -> JsonPathUtil.safeReadLocalDate(context, unknownPath),
                null
            ),
            Arguments.of(
                (Supplier<Object>) () -> JsonPathUtil.safeReadBoolean(context, unknownPath, false),
                false
            ),
            Arguments.of(
                (Supplier<Object>) () -> JsonPathUtil.safeReadBigDecimal(context, unknownPath),
                null
            )
        );
    }

    @ParameterizedTest
    @MethodSource("returnsDefaultWhenPathUnknown")
    void safeRead_unknownPath_DefaultsToNullOrGivenDefaultValue(Supplier<Object> call, Object defaultValue) {
        assertThat(call.get()).isEqualTo(defaultValue);
    }

    @Test
    void safeReadString_readsString() {
        String json = "{\"stringField\":\"stringValue\"}";
        DocContext context = JsonPathUtil.createDocContext(json, "");

        String actual = JsonPathUtil.safeReadString(context, "$.stringField", "");

        assertThat(actual).isEqualTo("stringValue");
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "TRUE", "TrUe", "Y", "y", "YES", "yes", "yEs"})
    void safeReadBoolean_readsTrueValues(String value) {
        String json = String.format("{\"booleanField\": %s}", value);
        DocContext context = JsonPathUtil.createDocContext(json, "");

        Boolean actual = JsonPathUtil.safeReadBoolean(context, "$.booleanField", false);

        assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "FALSE", "falSe", "N", "n", "No"})
    void safeReadBoolean_readsFalseValues(String value) {
        String json = String.format("{\"booleanField\": %s}", value);
        DocContext context = JsonPathUtil.createDocContext(json, "");

        Boolean actual = JsonPathUtil.safeReadBoolean(context, "$.booleanField", true);

        assertFalse(actual);
    }

    @Test
    void safeReadBoolean_unknownValue_returnsDefault() {
        String json = "{\"booleanField\": \"notBoolean\"}";
        DocContext context = JsonPathUtil.createDocContext(json, "");

        Boolean actual = JsonPathUtil.safeReadBoolean(context, "$.booleanField", true);

        assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"bigDecimalField\": 10.5}", "{\"bigDecimalField\": \"10.5\"}"})
    void safeReadBigDecimal_returnsBigDecimal(String json) {
        DocContext context = JsonPathUtil.createDocContext(json, "");

        BigDecimal actual = JsonPathUtil.safeReadBigDecimal(context, "$.bigDecimalField");

        assertEquals(BigDecimal.valueOf(10.5), actual);
    }

    @Test
    void safeReadLocalDate_returnsLocalDate() {
        String json = "{\"localDateField\": \"2024-03-01\"}";
        DocContext context = JsonPathUtil.createDocContext(json, "");

        LocalDate actual = JsonPathUtil.safeReadLocalDate(context, "$.localDateField");

        assertEquals(LocalDate.of(2024, 3, 1), actual);
    }
}
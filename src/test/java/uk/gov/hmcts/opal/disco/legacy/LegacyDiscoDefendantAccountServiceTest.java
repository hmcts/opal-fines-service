package uk.gov.hmcts.opal.disco.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchResult;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.search.DefendantDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class LegacyDiscoDefendantAccountServiceTest extends LegacyTestsBase {

    @Test
    void searchForDefendantAccounts_ValidateRequest() throws IOException, ProcessingException {

        LegacyDefendantAccountSearchCriteria legacyAccountSearchCriteria = constructDefendantAccountSearchCriteria();

        // Serialize the DTO to JSON using Jackson
        String json = ToJsonString.getObjectMapper().writeValueAsString(legacyAccountSearchCriteria);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountSearch/of_f_search_defendant_accounts_in.json"),
            StandardCharsets.UTF_8
        );

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Validate the serialized JSON against the schema
        assertTrue(schema.validInstance(JsonLoader.fromString(json)));
    }

    @Test
    void searchForDefendantAccounts_ValidateResponse() throws IOException, ProcessingException {

        LegacyDefendantAccountsSearchResults legacyAccountsSearchResults =
            LegacyDefendantAccountsSearchResults.builder()
            .totalCount(1L)
            .defendantAccountsSearchResult(List.of(constructDefendantAccountSearchResult()))
            .build();

        // Serialize the DTO to JSON using Jackson
        ObjectMapper objectMapper = ToJsonString.getObjectMapper();
        String json = objectMapper.writeValueAsString(legacyAccountsSearchResults);

        String content = Files.readString(
            Paths.get("src/test/resources/schemas/AccountSearch/of_f_search_defendant_accounts_out.json"),
            StandardCharsets.UTF_8
        );

        // Parse the JSON schema
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.byDefault();
        JsonSchema schema = schemaFactory.getJsonSchema(JsonLoader.fromString(content));

        // Generate validation report
        ProcessingReport report = schema.validate(JsonLoader.fromString(json));

        // Validate the serialized JSON against the schema
        assertTrue(report.isSuccess());

    }

    private LegacyDefendantAccountSearchCriteria constructDefendantAccountSearchCriteria() {
        return LegacyDefendantAccountSearchCriteria.builder()
            .businessUnitIds(List.of((short)10))
            .activeAccountsOnly(true)
            .defendant(new DefendantDto())
            .build();
    }

    public static LegacyDefendantAccountSearchResult constructDefendantAccountSearchResult() {
        return LegacyDefendantAccountSearchResult.builder()
            .accountNumber("accountNo")
            .defendantAccountId(12345L)
            .surname("Smith")
            .forenames("John")
            .title("Mr")
            .birthDate(LocalDate.parse("1977-06-26"))
            .addressLine1("Scotland")
            .accountBalance(BigDecimal.valueOf(1000))
            .businessUnitId("9")
            .businessUnitName("Cardiff")
            .build();
    }
}

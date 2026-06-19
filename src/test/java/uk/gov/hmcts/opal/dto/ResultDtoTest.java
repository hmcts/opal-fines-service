package uk.gov.hmcts.opal.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultDtoTest {

    @Test
    void toJson_shouldIncludeAllowAdditionalActionWhenNull() throws Exception {
        ResultDto dto = ResultDto.builder()
            .resultId("AAAAAA")
            .allowAdditionalAction(null)
            .build();

        String json = ToJsonString.getObjectMapper().writeValueAsString(dto);

        assertTrue(json.contains("\"allow_additional_action\":null"));
    }
}

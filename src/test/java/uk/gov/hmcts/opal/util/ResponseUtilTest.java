package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseUtilTest {

    @Test
    void buildResponse_withNonNullList_returnsOkResponse() {
        // Arrange
        List<String> responseList = Arrays.asList("item1", "item2", "item3");

        // Act
        ResponseEntity<List<String>> responseEntity = HttpUtil.buildResponse(responseList);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(responseList, responseEntity.getBody());
    }

    @Test
    void buildResponse_withEmptyList_returnsNoContentResponse() {
        // Arrange
        List<String> responseList = Collections.emptyList();

        // Act
        ResponseEntity<List<String>> responseEntity = HttpUtil.buildResponse(responseList);

        // Assert
        assertEquals(204, responseEntity.getStatusCode().value());
        assertNull(responseEntity.getBody());
    }

    @Test
    void buildResponse_withNullList_returnsNoContentResponse() {
        // Arrange
        List<String> responseList = null;

        // Act
        ResponseEntity<List<String>> responseEntity = HttpUtil.buildResponse(responseList);

        // Assert
        assertEquals(204, responseEntity.getStatusCode().value());
        assertNull(responseEntity.getBody());
    }

    @Test
    void buildResponse_withNonNullString_returnsOkResponse() {
        // Arrange
        String response = "item1";

        // Act
        ResponseEntity<String> responseEntity = HttpUtil.buildResponse(response);

        // Assert
        assertEquals(200, responseEntity.getStatusCode().value());
        assertEquals(response, responseEntity.getBody());
    }


    @Test
    void buildResponse_withNullString_returnsNoContentResponse() {
        // Arrange
        String response = null;

        // Act
        ResponseEntity<String> responseEntity = HttpUtil.buildResponse(response);

        // Assert
        assertEquals(204, responseEntity.getStatusCode().value());
        assertNull(responseEntity.getBody());
    }
}

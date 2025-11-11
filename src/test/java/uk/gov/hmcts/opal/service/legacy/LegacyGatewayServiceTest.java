package uk.gov.hmcts.opal.service.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.xml.sax.SAXParseException;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

@ExtendWith(MockitoExtension.class)
@Slf4j(topic = "opal.LegacyGatewayServiceTest")
class LegacyGatewayServiceTest {

    @Mock
    RestClient restClient;

    @Mock
    LegacyGatewayProperties properties;

    @Mock
    RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    RestClient.RequestBodySpec requestBodySpec;

    @Mock
    RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private LegacyGatewayService legacy;

    // Dummy payload type for unmarshalled responses
    public static class Dummy {

    }

    @Test
    @SuppressWarnings("unchecked")
    void testPostParamsToGateway_JsonException() {

        BrokenMapImplementation broken = new BrokenMapImplementation();

        // Act
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            legacy.postParamsToGateway("", TestDto.class, broken));

        // Assert
        assertNotNull(thrown);
        log.info(":testPostParamsToGateway_JsonException: ", thrown);
        assertTrue(thrown.getMessage().contains("JsonMappingException"));

    }

    @Test
    void postToGateway_success() {
        mockRestClientPost();
        String actionType = "testAction";
        String request = "{}";
        String responseBody = "{\"status\":\"success\"}";

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(responseSpec.toEntity(String.class)).thenReturn(successfulResponseEntity);

        Response<String> response = legacy.postToGateway(actionType, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.code);
        assertEquals(responseBody, response.body);
    }

    @Test
    void postToGateway_emptyResponse() {
        mockRestClientPost();
        String actionType = "testAction";
        String  request = "{}";

        ResponseEntity<String> emptyResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(responseSpec.toEntity(String.class)).thenReturn(emptyResponseEntity);

        Response<String> response = legacy.postToGateway(actionType, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.code);
        assertNull(response.body);
    }

    @Test
    void postToGateway_non2xxResponse() {
        mockRestClientPost();
        String actionType = "testAction";
        String  request = "{}";

        ResponseEntity<String> errorResponseEntity = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
        when(responseSpec.toEntity(String.class)).thenReturn(errorResponseEntity);

        Response<String> response = legacy.postToGateway(actionType, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.code);
        assertEquals("Error", response.body);
    }

    @Test
    void postToGatewayDtoResponse_success() {
        mockRestClientPost();
        String actionType = "testAction";
        String request = "{}";

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(getTestEntityXml(), HttpStatus.OK);
        when(responseSpec.toEntity(String.class)).thenReturn(successfulResponseEntity);

        Response<TestDto> response = legacy.postToGateway(actionType, TestDto.class, request, null);

        log.info(":postToGatewayDtoResponse_success:", response.exception);

        assertFalse(response.isError());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.code);

        TestDto dto = TestDto.builder()
            .testId(1L)
            .testType("NT")
            .testDate(LocalDateTime.of(2022, 12, 01, 12, 00))
            .build();
        assertEquals(dto, response.responseEntity);
    }

    @Test
    void postToGatewayDtoResponse_invalidXml() {
        mockRestClientPost();
        String actionType = "testAction";
        String request = "{}";
        String responseBody = "{\"test_id\":\"test 1\", \"test_type\":\"incorrect\"}";

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(responseSpec.toEntity(String.class)).thenReturn(successfulResponseEntity);

        Response<TestDto> response = legacy.postToGateway(actionType, TestDto.class, request, null);

        assertTrue(response.isError());
        assertInstanceOf(UnmarshalException.class, response.exception);
        assertInstanceOf(SAXParseException.class, response.exception.getCause());
        assertEquals(HttpStatus.OK, response.code);
        assertEquals(responseBody, response.body);
    }

    @Test
    void postToGatewayDtoResponse_invalidEntity() {
        mockRestClientPost();
        String actionType = "testAction";
        String request = "{}";

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(getBrokenEntityXml(), HttpStatus.OK);
        when(responseSpec.toEntity(String.class)).thenReturn(successfulResponseEntity);

        Response<TestDto> response = legacy.postToGateway(actionType, TestDto.class, request, null);

        assertTrue(response.isError());
        assertInstanceOf(UnmarshalException.class, response.exception);
        assertTrue(response.exception.getMessage().contains("Expected elements are <{}testEntity>"));
        assertEquals(HttpStatus.OK, response.code);
    }

    void mockRestClientPost() {
        when(properties.getUrl()).thenReturn("http://test.com");
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    // =========================
    // extractResponse() tests
    // =========================

    @Test
    void testExtractResponse_withStringClass_returnsRawBody() {

        var svc = new LegacyGatewayService(properties, restClient);
        var re = ResponseEntity.ok("plain string body");
        var out = svc.extractResponse(re, String.class, null);
        assertThat(out.body).isEqualTo("plain string body");
    }

    @Test
    void testPatchToGateway_tunnelsToPostAndReturnsSameResponse() {
        // Arrange: real service instance but spied, so we can intercept postToGateway(...)
        LegacyGatewayProperties props = mock(LegacyGatewayProperties.class);
        LegacyGatewayService svc = Mockito.spy(new LegacyGatewayService(props, restClient));

        var expected = new LegacyGatewayService.Response<>(HttpStatus.OK, new Dummy());
        var requestBody = Map.of("k", "v");

        // Stub the delegated call
        doReturn(expected).when(svc)
            .postToGateway(eq("ACTION_PATCH"), eq(Dummy.class), same(requestBody), isNull());

        // Act
        LegacyGatewayService.Response<Dummy> out =
            svc.patchToGateway("ACTION_PATCH", Dummy.class, requestBody, null);

        // Assert: same instance returned, and delegation happened once with the same args
        assertThat(out).isSameAs(expected);
        verify(svc, times(1))
            .postToGateway(eq("ACTION_PATCH"), eq(Dummy.class), same(requestBody), isNull());

        // No other interactions with HTTP chain needed
        verifyNoMoreInteractions(restClient);
    }

    @Test
    void patchToGatewayAsync_delegatesToSync_andCompletesWithSameResponse() throws Exception {
        // Arrange
        LegacyGatewayProperties props = mock(LegacyGatewayProperties.class);
        LegacyGatewayService svc = Mockito.spy(new LegacyGatewayService(props, restClient));

        var request = Map.of("k", "v");
        var expected = new LegacyGatewayService.Response<>(HttpStatus.OK, new Dummy());

        // stub the sync method that async delegates to
        doReturn(expected).when(svc)
            .patchToGateway(eq("ACTION"), eq(Dummy.class), same(request), isNull());

        // Act
        CompletableFuture<Response<Dummy>> fut =
            svc.patchToGatewayAsync("ACTION", Dummy.class, request, null);

        // Assert
        assertThat(fut).isNotNull();
        assertThat(fut).isCompleted();
        assertThat(fut.get()).isSameAs(expected);

        verify(svc, times(1))
            .patchToGateway(eq("ACTION"), eq(Dummy.class), same(request), isNull());
        verifyNoMoreInteractions(restClient);
    }

    class BrokenMapImplementation<K, V> implements Map<K, V> {

        @Override
        public int size() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @Nullable
        @Override
        public V put(K key, V value) {
            return null;
        }

        @Override
        public V remove(Object key) {
            return null;
        }

        @Override
        public void putAll(@NotNull Map<? extends K, ? extends V> m) {

        }

        @Override
        public void clear() {

        }

        @NotNull
        @Override
        public Set<K> keySet() {
            return null;
        }

        @NotNull
        @Override
        public Collection<V> values() {
            return null;
        }

        @NotNull
        @Override
        public Set<Entry<K, V>> entrySet() {
            return null;
        }
    }

    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    @XmlRootElement(name = "testEntity")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestDto implements ToJsonString {

        @JsonProperty("test_id")
        private Long testId;

        @JsonProperty("test_type")
        private String testType;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        @JsonProperty("test_date")
        private LocalDateTime testDate;
    }

    private String getTestEntityXml() {
        return """
            <testEntity>
              <testId>1</testId>
              <testType>NT</testType>
              <testDate>2022-12-01T12:00:00</testDate>
            </testEntity>
            """;
    }

    private String getBrokenEntityXml() {
        return """
            <testThing>
              <testId>One</testId>
              <testInfo>NT</testInfo>
              <testDate>2022-12-01T12:00:00</testDate>
            </testThing>
            """;
    }
}

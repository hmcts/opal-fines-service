package uk.gov.hmcts.opal.service.legacy;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {


    @Mock
    protected LegacyGatewayProperties gatewayProperties;

    @Mock
    protected RestClient restClient;

    @Mock
    RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    RestClient.RequestBodySpec requestBodySpec;

    @Mock
    RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private LegacyGatewayService legacy;

    @SuppressWarnings("unchecked")
    void mockRestClientPost() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.accept(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @SuppressWarnings("unchecked")
    void postToGateway_getNoteSuccess() {

        mockRestClientPost();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(getNoteXml(), HttpStatus.OK);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        Response<NoteDto> response = legacy.postToGateway("", NoteDto.class, "");

        // Assert
        assertNotNull(response);
        NoteDto dto = response.responseEntity;

        assertNotNull(dto);
        assertEquals(1L, dto.getNoteId());
        assertEquals("AC", dto.getNoteType());
        assertEquals("This is a sample note text.", dto.getNoteText());
        assertEquals("user123", dto.getPostedBy());
        assertEquals(1001L, dto.getPostedByUserId());
        assertEquals(LocalDateTime.of(2022, 12, 01, 12, 00), dto.getPostedDate());
        assertEquals((short)10, dto.getBusinessUnitId());
        assertEquals("defendants_accounts", dto.getAssociatedRecordType());
        assertEquals("123456", dto.getAssociatedRecordId());
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void postToGatewayAsyc_getNoteSuccess() {

        mockRestClientPost();

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(getNoteXml(), HttpStatus.OK);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(anyString())).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        CompletableFuture<Response<NoteDto>> future = legacy.postToGatewayAsync("", NoteDto.class, "");

        // Assert
        assertNotNull(future);
        Response<NoteDto> response = future.get();
        NoteDto dto = response.responseEntity;

        assertNotNull(dto);
        assertEquals(1L, dto.getNoteId());
        assertEquals("AC", dto.getNoteType());
        assertEquals("This is a sample note text.", dto.getNoteText());
        assertEquals("user123", dto.getPostedBy());
        assertEquals(1001L, dto.getPostedByUserId());
        assertEquals(LocalDateTime.of(2022, 12, 01, 12, 00), dto.getPostedDate());
        assertEquals((short)10, dto.getBusinessUnitId());
        assertEquals("defendants_accounts", dto.getAssociatedRecordType());
        assertEquals("123456", dto.getAssociatedRecordId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void postParamsToGateway_JsonException() {

        BrokenMapImplementation broken = new BrokenMapImplementation();

        // Act
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            legacy.postParamsToGateway("", NoteDto.class, broken));

        // Assert
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().contains("JsonMappingException"));

    }

    @Test
    void postToGateway_stringResponse_success() {
        mockRestClientPost();
        String actionType = "testAction";
        String  request = "{}";
        String responseBody = "{\"status\":\"success\"}";

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(String.class)).thenReturn(successfulResponseEntity);

        Response<String> response = legacy.postToGateway(actionType, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.code);
        assertEquals(responseBody, response.body);
    }

    @Test
    void postToGateway_stringResponse_emptyResponse() {
        mockRestClientPost();
        String actionType = "testAction";
        String  request = "{}";

        ResponseEntity<String> emptyResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(String.class)).thenReturn(emptyResponseEntity);

        Response<String> response = legacy.postToGateway(actionType, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.code);
        assertNull(response.body);
    }

    @Test
    void postToGateway_stringResponse_non2xxResponse() {
        mockRestClientPost();
        String actionType = "testAction";
        String  request = "{}";

        ResponseEntity<String> errorResponseEntity = new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
        when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(String.class))).thenReturn(requestBodySpec);
        when(responseSpec.toEntity(String.class)).thenReturn(errorResponseEntity);

        Response<String> response = legacy.postToGateway(actionType, request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.code);
        assertEquals("Error", response.body);
    }

    private String getNoteXml() {
        return """
            <note>
                 <noteId>1</noteId>
                 <noteType>AC</noteType>
                 <associatedRecordType>defendants_accounts</associatedRecordType>
                 <associatedRecordId>123456</associatedRecordId>
                 <businessUnitId>10</businessUnitId>
                 <noteText>This is a sample note text.</noteText>
                 <postedDate>2022-12-01T12:00:00</postedDate>
                 <postedBy>user123</postedBy>
                 <postedByUserId>1001</postedByUserId>
             </note>
            """;
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

}

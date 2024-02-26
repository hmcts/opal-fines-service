package uk.gov.hmcts.opal.service.legacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.NoteDto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyServiceTest extends LegacyTestsBase {

    private LegacyService legacy;

    @Mock
    private Logger log;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        legacy = spy(new LegacyService(properties, restClient) {
            @Override
            protected Logger getLog() {
                return log;
            }
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetFromGateway_success() {

        // Arrange
        mockRestClientGet();

        String jsonBody = """
            {
            "noteId": 1,
            "noteType": "AC",
            "associatedRecordType": "defendants_accounts",
            "associatedRecordId": "123456",
            "noteText": "This is a sample note text.",
            "postedBy": "user123"
            }
            """;

        ResponseEntity<String> successfulResponseEntity = new ResponseEntity<>(jsonBody, HttpStatus.OK);
        when(requestHeaderSpec.header(anyString(), anyString())).thenReturn(requestHeaderSpec);
        when(responseSpec.toEntity(any(Class.class))).thenReturn(successfulResponseEntity);

        // Act
        NoteDto response = legacy.getFromGateway("", NoteDto.class);

        // Assert
        assertNotNull(response);

    }

    @Test
    @SuppressWarnings("unchecked")
    void testPostParamsToGateway_JsonException() {

        // Act
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
            legacy.postParamsToGateway("", NoteDto.class, new BrokenMapImplementation()));

        // Assert
        assertNotNull(thrown);
        assertTrue(thrown.getMessage().contains("JsonMappingException"));

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

package uk.gov.hmcts.opal.spring.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.common.exceptions.standard.InvalidContentDigestException;
import uk.gov.hmcts.opal.spring.http.CachedBodyHttpServletRequest;
import uk.gov.hmcts.opal.spring.interceptor.ContentDigestValidatorInterceptor.ContentDigest;
import uk.gov.hmcts.opal.spring.properties.ContentDigestProperties;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentDigestValidatorInterceptor")
class ContentDigestValidatorInterceptorTest {

    private static final Map<String, String> VALID_SUPPORTED_ALGORITHMS = Map.of("sha-256", "SHA-256");

    private static final String VALID_CONTENT_DIGEST_ALGORITHM = "sha-256";
    private static final String VALID_CONTENT_DIGEST_BASE64 = "cA88WX2aDbX8LcxByNm2UNZLoO2XncAPHj3qF/ygeh8=";
    private static final String VALID_CONTENT_DIGEST_HEADER =
        VALID_CONTENT_DIGEST_ALGORITHM + "=:" + VALID_CONTENT_DIGEST_BASE64 + ":";
    private static final String VALID_CONTENT_FOR_DIGEST = "some-value";

    private ContentDigestProperties getContentDigestProperties(boolean enforce,
        Map<String, String> supportedAlgorithms) {
        ContentDigestProperties.Request request = new ContentDigestProperties.Request(enforce, supportedAlgorithms);
        return new ContentDigestProperties(request, null);
    }

    private ContentDigestValidatorInterceptor getValidContentDigestValidatorInterceptor() {
        return new ContentDigestValidatorInterceptor(
            getContentDigestProperties(true, VALID_SUPPORTED_ALGORITHMS)
        );
    }

    @DisplayName("Constructor initialization")
    @Test
    void constructorInitialization() {
        ContentDigestValidatorInterceptor interceptor = new ContentDigestValidatorInterceptor(
            getContentDigestProperties(true, Map.of("sha-256", "SHA-256", "sha-512", "SHA-512"))
        );

        assertThat(interceptor.isEnforce()).isTrue();
        assertThat(interceptor.getRfcToJca()).containsExactlyInAnyOrderEntriesOf(
            Map.of("sha-256", "SHA-256", "sha-512", "SHA-512")
        );
        assertThat(interceptor.getSupportedAlgosLower()).containsExactlyInAnyOrder("sha-256", "sha-512");

        interceptor = new ContentDigestValidatorInterceptor(
            getContentDigestProperties(false, Map.of("sha-256", "SHA-256"))
        );
        assertThat(interceptor.isEnforce()).isFalse();
        assertThat(interceptor.getRfcToJca()).containsExactlyInAnyOrderEntriesOf(Map.of("sha-256", "SHA-256"));
        assertThat(interceptor.getSupportedAlgosLower()).containsExactlyInAnyOrder("sha-256");
    }

    @Nested
    @DisplayName("String getAndValidateContentDigestHeader(HttpServletRequest request)")
    class GetAndValidateContentDigestHeader {

        @DisplayName("Should throw exception when Content-Digest header is missing")
        @Test
        void shouldThrowExceptionWhenHeaderMissing() {
            CachedBodyHttpServletRequest request = mock(CachedBodyHttpServletRequest.class);

            ContentDigestValidatorInterceptor interceptor = getValidContentDigestValidatorInterceptor();
            assertThatThrownBy(() -> interceptor.getAndValidateContentDigestHeader(request))
                .isInstanceOf(InvalidContentDigestException.class)
                .hasFieldOrPropertyWithValue("title", "Missing/Blank Content-Digest header")
                .hasFieldOrPropertyWithValue("detail",
                    "The Content-Digest header must be provided with a non blank value.");
        }

        @DisplayName("Should throw exception when Content-Digest header is blank")
        @Test
        void shouldThrowExceptionWhenHeaderIsBlank() {
            CachedBodyHttpServletRequest request = mock(CachedBodyHttpServletRequest.class);
            lenient().doReturn("")
                .when(request).getHeader("Content-Digest");

            ContentDigestValidatorInterceptor interceptor = getValidContentDigestValidatorInterceptor();
            assertThatThrownBy(() -> interceptor.getAndValidateContentDigestHeader(request))
                .isInstanceOf(InvalidContentDigestException.class)
                .hasFieldOrPropertyWithValue("title", "Missing/Blank Content-Digest header")
                .hasFieldOrPropertyWithValue("detail",
                    "The Content-Digest header must be provided with a non blank value.");
        }
    }

    @Nested
    @DisplayName("public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)")
    class PreHandle {

        private CachedBodyHttpServletRequest getValidRequest() {
            CachedBodyHttpServletRequest request = mock(CachedBodyHttpServletRequest.class);
            lenient().doReturn(VALID_CONTENT_DIGEST_HEADER)
                .when(request).getHeader("Content-Digest");
            lenient().doReturn(VALID_CONTENT_FOR_DIGEST.getBytes()).when(request).getCachedBody();
            return request;
        }

        @DisplayName("Should skip validation when enforcement is disabled")
        @Test
        void shouldSkipValidationWhenEnforcementIsDisabled() throws Exception {
            ContentDigestValidatorInterceptor interceptor = spy(new ContentDigestValidatorInterceptor(
                getContentDigestProperties(false, VALID_SUPPORTED_ALGORITHMS)
            ));
            CachedBodyHttpServletRequest request = getValidRequest();
            //Simulate bad request to ensure validation is skipped
            lenient().doReturn("some-value".getBytes()).when(request).getCachedBody();
            boolean result = interceptor.preHandle(request, null, null);
            assertThat(result).isTrue();
        }

        @DisplayName("Should skip validation when body is empty")
        @Test
        void shouldSkipValidationWhenBodyIsEmpty() throws Exception {
            ContentDigestValidatorInterceptor interceptor = spy(new ContentDigestValidatorInterceptor(
                getContentDigestProperties(true, VALID_SUPPORTED_ALGORITHMS)
            ));
            CachedBodyHttpServletRequest request = getValidRequest();
            //Simulate bad request to ensure validation is skipped
            lenient().doReturn("some-value".getBytes()).when(request).getCachedBody();
            //Simulate empty body
            doReturn(new byte[0]).when(request).getCachedBody();
            boolean result = interceptor.preHandle(request, null, null);
            assertThat(result).isTrue();
        }

        @DisplayName("Should pass validation when Content-Digest is valid")
        @Test
        void shouldPassValidationWhenContentDigestIsValid() throws Exception {
            ContentDigestValidatorInterceptor interceptor = spy(new ContentDigestValidatorInterceptor(
                getContentDigestProperties(true, VALID_SUPPORTED_ALGORITHMS)
            ));
            CachedBodyHttpServletRequest request = getValidRequest();
            boolean result = interceptor.preHandle(request, null, null);
            assertThat(result).isTrue();
        }

        @DisplayName("Should fail validation when Content-Digest is invalid")
        @Test
        void shouldFailValidationWhenContentDigestIsInvalid() {
            ContentDigestValidatorInterceptor interceptor = spy(new ContentDigestValidatorInterceptor(
                getContentDigestProperties(true, VALID_SUPPORTED_ALGORITHMS)
            ));
            CachedBodyHttpServletRequest request = getValidRequest();
            //Modify header to make it invalid
            lenient().doReturn("some-value-invalid".getBytes()).when(request).getCachedBody();
            assertThatThrownBy(() -> interceptor.preHandle(request, null, null))
                .isInstanceOf(InvalidContentDigestException.class)
                .hasFieldOrPropertyWithValue("title", "Digest validation failed")
                .hasFieldOrPropertyWithValue("detail", "Body hash did not match for algorithm: sha-256");
        }
    }

    @Nested
    @DisplayName("ContentDigest")
    class ContentDigestTest {

        private ContentDigest createContentDigest(String contentDigest) {
            ContentDigestValidatorInterceptor interceptor = getValidContentDigestValidatorInterceptor();
            return interceptor.new ContentDigest(contentDigest);
        }

        private ContentDigest createContentDigest(String algorithm, String base64) {
            ContentDigestValidatorInterceptor interceptor = getValidContentDigestValidatorInterceptor();
            ContentDigest contentDigest = interceptor.new ContentDigest(VALID_CONTENT_DIGEST_HEADER);
            ReflectionTestUtils.setField(contentDigest, "algorithm", algorithm);
            ReflectionTestUtils.setField(contentDigest, "base64", base64);
            return contentDigest;
        }

        @Nested
        @DisplayName("public ContentDigest(String contentDigest)")
        class Constructor {

            @DisplayName("Should throw exception when multiple digest entries are provided")
            @Test
            void shouldThrowExceptionWhenMultipleDigestEntriesProvided() {
                assertThatThrownBy(() -> createContentDigest("someValue,someOtherValue"))
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Invalid Content-Digest header")
                    .hasFieldOrPropertyWithValue("detail", "Multiple digest entries are not supported");
            }

            @DisplayName("Should throw exception when no valid digest entries are found")
            @Test
            void shouldThrowExceptionWhenNoValidDigestEntriesFound() {
                assertThatThrownBy(() -> createContentDigest("someInvalidPattern"))
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Invalid Content-Digest header")
                    .hasFieldOrPropertyWithValue("detail", "No valid digest entries found in header");
            }

            @DisplayName("Should create ContentDigest when valid entry is provided")
            @Test
            void shouldCreateContentDigestWhenValidEntryProvided() {
                ContentDigestValidatorInterceptor.ContentDigest contentDigest =
                    createContentDigest(VALID_CONTENT_DIGEST_HEADER);

                assertThat(contentDigest.getAlgorithm()).isEqualTo(VALID_CONTENT_DIGEST_ALGORITHM);
                assertThat(contentDigest.getBase64()).isEqualTo(VALID_CONTENT_DIGEST_BASE64);
            }
        }

        @Nested
        @DisplayName("void validate()")
        class Validate {

            @DisplayName("Should throw exception algorithm is missing")
            @Test
            void shouldThrowExceptionWhenAlgorithmIsMissing() {
                ContentDigest contentDigest = createContentDigest(null, VALID_CONTENT_DIGEST_BASE64);
                assertThatThrownBy(contentDigest::validate)
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Digest validation failed")
                    .hasFieldOrPropertyWithValue("detail", "Digest algorithm is missing or blank.");
            }

            @DisplayName("Should throw exception when algorithm is blank")
            @Test
            void shouldThrowExceptionWhenAlgorithmIsBlank() {
                ContentDigest contentDigest = createContentDigest("", VALID_CONTENT_DIGEST_BASE64);
                assertThatThrownBy(contentDigest::validate)
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Digest validation failed")
                    .hasFieldOrPropertyWithValue("detail", "Digest algorithm is missing or blank.");
            }

            @DisplayName("Should throw exception when algorithm is not supported")
            @Test
            void shouldThrowExceptionWhenAlgorithmNotSupported() {
                ContentDigest contentDigest = createContentDigest("unsupported-algorithm", VALID_CONTENT_DIGEST_BASE64);
                assertThatThrownBy(contentDigest::validate)
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Digest validation failed")
                    .hasFieldOrPropertyWithValue("detail",
                        "Unsupported digest algorithm: unsupported-algorithm. Supported algorithms (sha-256).");

            }

            @DisplayName("Should pass validation when algorithm is supported")
            @Test
            void shouldPassValidationWhenAlgorithmSupported() {
                assertThatNoException().isThrownBy(() -> createContentDigest(VALID_CONTENT_DIGEST_HEADER).validate());
            }
        }

        @Nested
        @DisplayName("public MessageDigest getMessageDigest()")
        class GetMessageDigest {

            @DisplayName("Should throw exception when algorithm is not supported by JCA")
            @Test
            void shouldThrowExceptionWhenAlgorithmNotSupportedByJca() {
                ContentDigest contentDigest = createContentDigest("sha-512", VALID_CONTENT_DIGEST_BASE64);

                assertThatThrownBy(contentDigest::getMessageDigest)
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Digest validation failed")
                    .hasFieldOrPropertyWithValue("detail",
                        "Unsupported digest algorithm: sha-512. Supported algorithms (sha-256).");
            }

            @DisplayName("Should return MessageDigest when algorithm is supported by JCA")
            @Test
            void shouldReturnMessageDigestWhenAlgorithmSupportedByJca() {
                ContentDigest contentDigest = createContentDigest(VALID_CONTENT_DIGEST_HEADER);
                assertThatNoException().isThrownBy(contentDigest::getMessageDigest);
                assertThat(contentDigest.getMessageDigest().getAlgorithm()).isEqualTo("SHA-256");
            }

        }

        @Nested
        @DisplayName("byte[] decodeSfBinary(String base64NoColons)")
        class DecodeSfBinary {

            @DisplayName("Should throw exception when base64 is invalid")
            @Test
            void shouldThrowExceptionWhenBase64IsInvalid() {
                ContentDigest contentDigest = createContentDigest(VALID_CONTENT_DIGEST_ALGORITHM, "invalid-base64");
                assertThatThrownBy(contentDigest::decodeSfBinary)
                    .isInstanceOf(InvalidContentDigestException.class)
                    .hasFieldOrPropertyWithValue("title", "Digest validation failed")
                    .hasFieldOrPropertyWithValue("detail", "Bad base64 encoding for algorithm: sha-256");
            }

            @DisplayName("Should return decoded byte array when base64 is valid")
            @Test
            void shouldReturnDecodedByteArrayWhenBase64IsValid() {
                ContentDigest contentDigest = createContentDigest(VALID_CONTENT_DIGEST_HEADER);
                byte[] decoded = contentDigest.decodeSfBinary();
                assertThat(decoded)
                    .isNotNull()
                    .isEqualTo(Base64.getDecoder().decode(VALID_CONTENT_DIGEST_BASE64));
            }

            @DisplayName("Should pad and return decoded byte array when base64 is valid but missing padding")
            @Test
            void shouldPadAndReturnDecodedByteArrayWhenBase64IsValidButMissingPadding() {
                String base64MissingPadding = "cA88WX2aDbX8LcxByNm2UNZLoO2XncAPHj3qF/ygeh8"; // removed '=' at end
                ContentDigest contentDigest = createContentDigest(VALID_CONTENT_DIGEST_ALGORITHM, base64MissingPadding);
                byte[] decoded = contentDigest.decodeSfBinary();
                assertThat(decoded)
                    .isNotNull()
                    .isEqualTo(Base64.getDecoder().decode(VALID_CONTENT_DIGEST_BASE64));
            }
        }
    }

}

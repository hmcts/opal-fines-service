package uk.gov.hmcts.opal.authorisation.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.opal.authentication.aspect.AccessTokenParam;
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.Role;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteDto;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = AuthorizationAspectService.class)
@ExtendWith(MockitoExtension.class)
class AuthorizationAspectServiceTest {

    static final Role ROLE = Role.builder()
        .businessUnitId((short) 12)
        .businessUserId("BU123")
        .permissions(Set.of(
            Permission.builder()
                .permissionId(1L)
                .permissionName("Notes")
                .build()))
        .build();

    static final UserState USER_STATE = UserState.builder()
        .userId(123L).userName("John Smith")
        .roles(Set.of(ROLE))
        .build();

    @MockBean
    private HttpServletRequest servletRequest;

    @Autowired
    private AuthorizationAspectService authorizationAspectService;


    @Nested
    class GetAccessTokenParam {

        @Mock
        private ProceedingJoinPoint joinPoint;

        @Mock
        private MethodSignature methodSignature;

        @Test
        public void testGetAccessTokenParam_found() throws NoSuchMethodException {
            String expectedToken = "testToken";

            when(joinPoint.getSignature()).thenReturn(methodSignature);

            Method method = AuthorizationAspectServiceTest.GetAccessTokenParam.class.getMethod(
                "methodWithAccessToken",
                String.class
            );
            when(methodSignature.getMethod()).thenReturn(method);

            when(joinPoint.getArgs()).thenReturn(new Object[]{expectedToken});

            Optional<String> result = authorizationAspectService.getAccessTokenParam(joinPoint);

            assertTrue(result.isPresent());
            assertEquals(expectedToken, result.get());
        }

        @Test
        public void testGetAccessTokenParam_notFound() throws NoSuchMethodException {
            when(joinPoint.getSignature()).thenReturn(methodSignature);

            Method method = AuthorizationAspectServiceTest.GetAccessTokenParam.class.getMethod(
                "methodWithoutAccessToken",
                String.class
            );
            when(methodSignature.getMethod()).thenReturn(method);

            Parameter[] parameters = method.getParameters();
            when(joinPoint.getArgs()).thenReturn(new Object[]{"someOtherArg"});

            Optional<String> result = authorizationAspectService.getAccessTokenParam(joinPoint);

            assertTrue(result.isEmpty());
        }

        @Test
        public void testGetAccessTokenParam_exceptionHandling() throws NoSuchMethodException {
            when(joinPoint.getSignature()).thenThrow(new RuntimeException("Exception"));

            Optional<String> result = authorizationAspectService.getAccessTokenParam(joinPoint);

            assertTrue(result.isEmpty());
        }

        public void methodWithAccessToken(@AccessTokenParam String token) {
        }

        public void methodWithoutAccessToken(String arg) {
        }
    }

    @Nested
    class GetAuthorization {
        @Test
        void getAuthorization_WhenAuthHeaderValueNotNull_ReturnsOptionalWithValue() {
            String authHeaderValue = "Bearer token";

            Optional<String> result = authorizationAspectService.getAuthorization(authHeaderValue);

            assertEquals(Optional.of(authHeaderValue), result);
        }

        @Test
        void getAuthorization_WhenRequestAttributesNotNull_ReturnsOptionalWithValue() {
            String authHeaderValue = "Bearer token";
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
            when(servletRequest.getHeader(AuthorizationAspectService.AUTHORIZATION)).thenReturn(authHeaderValue);

            Optional<String> result = authorizationAspectService.getAuthorization(null);

            assertEquals(Optional.of(authHeaderValue), result);
        }

        @Test
        void getAuthorization_WhenRequestAttributesNull_ReturnsOptionalEmpty() {
            RequestContextHolder.setRequestAttributes(null);

            Optional<String> result = authorizationAspectService.getAuthorization(null);

            assertEquals(Optional.empty(), result);
        }
    }

    @Nested
    class GetRole {
        @Test
        void getRole_WhenInvalidArguments() {
            Object[] args = {"invalid"};
            String expectedMessage = "Can't infer the role for user John Smith."
                + " Annotated method needs to have arguments of types (Role, AddNoteDto, NoteDto).";

            RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> authorizationAspectService.getRole(args, USER_STATE)
            );

            assertEquals(expectedMessage, exception.getMessage());
        }

        @Test
        void getRole_WhenAddNoteDtoArgument() {
            AddNoteDto addNoteDto = AddNoteDto.builder().businessUnitId((short) 12).build();
            Object[] args = {addNoteDto};

            Role actualRole = authorizationAspectService.getRole(args, USER_STATE);

            assertEquals(ROLE, actualRole);
        }

        @Test
        void getRole_WhenRoleArgument() {
            Role expectedRole = ROLE;
            Object[] args = {expectedRole};

            Role actualRole = authorizationAspectService.getRole(args, USER_STATE);

            assertEquals(expectedRole, actualRole);
        }
    }

    @Nested
    class GetArgument {
        @Test
        void testGetArgumentWithInvalidArgument() {
            Object[] args = {"someString", 42};

            Optional<UserState> result = authorizationAspectService.getArgument(args, UserState.class);

            assertTrue(result.isEmpty());
        }

        @Test
        void testGetArgumentWithEmptyArgs() {
            Object[] args = {};

            Optional<UserState> result = authorizationAspectService.getArgument(args, UserState.class);

            assertTrue(result.isEmpty());
        }

        @Test
        void testGetStringWithValidArgument() {
            String str = "testString";
            Object[] args = {USER_STATE, str, 42};

            Optional<String> result = authorizationAspectService.getArgument(args, String.class);

            assertTrue(result.isPresent());
            assertEquals(str, result.get());
        }

        @Test
        void testGetUserStateWithValidArgument() {
            String str = "testString";
            Object[] args = {USER_STATE, str, 42};

            Optional<UserState> result = authorizationAspectService.getArgument(args, UserState.class);

            assertTrue(result.isPresent());
            assertEquals(USER_STATE, result.get());
        }
    }
}

package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.FluentQuery;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessUnitUserService businessUnitUserService;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetUser() {
        // Arrange

        UserEntity userEntity = UserEntity.builder().build();
        when(userRepository.getReferenceById(any())).thenReturn(userEntity);

        // Act
        UserEntity result = userService.getUser("1");

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchUsers() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        UserEntity userEntity = UserEntity.builder().build();
        Page<UserEntity> mockPage = new PageImpl<>(List.of(userEntity), Pageable.unpaged(), 999L);
        when(userRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<UserEntity> result = userService.searchUsers(UserSearchDto.builder().build());

        // Assert
        assertEquals(List.of(userEntity), result);

    }

    @Test
    void testGetUserStateByUsername() {
        // Arrange
        UserEntity userEntity = UserEntity.builder().userId(123L).username("John Smith").build();
        when(userRepository.findByUsername(any())).thenReturn(userEntity);
        when(businessUnitUserService.getAuthorisationRolesByUserId(any())).thenReturn(Collections.emptySet());

        // Act
        UserState result = userService.getUserStateByUsername("");

        // Assert
        assertNotNull(result);
        assertEquals(123L, result.getUserId());
        assertEquals("John Smith", result.getUserName());

    }

    @Test
    void testGetLimitedUserStateByUsername() {
        // Arrange
        UserEntity userEntity = UserEntity.builder().userId(123L).username("John Smith").build();
        when(userRepository.findByUsername(any())).thenReturn(userEntity);

        // Act
        Optional<UserState> result = userService.getLimitedUserStateByUsername("");
        // UserState result = userService.getLimitedUserStateByUsername("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(123L, result.get().getUserId());
        assertEquals("John Smith", result.get().getUserName());

    }
}

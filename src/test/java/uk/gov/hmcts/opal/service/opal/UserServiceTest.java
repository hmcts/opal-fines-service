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
import uk.gov.hmcts.opal.dto.search.UserSearchDto;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.repository.UserRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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


}
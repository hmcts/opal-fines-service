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
import uk.gov.hmcts.opal.authorisation.model.Permission;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.search.UserEntitlementSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.entity.UserEntitlementEntity;
import uk.gov.hmcts.opal.entity.UserEntity;
import uk.gov.hmcts.opal.repository.UserEntitlementRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEntitlementServiceTest {

    @Mock
    private UserEntitlementRepository userEntitlementRepository;

    @InjectMocks
    private UserEntitlementService userEntitlementService;

    @Test
    void testGetUserEntitlement() {
        // Arrange

        UserEntitlementEntity userEntitlementEntity = UserEntitlementEntity.builder().build();
        when(userEntitlementRepository.getReferenceById(any())).thenReturn(userEntitlementEntity);

        // Act
        UserEntitlementEntity result = userEntitlementService.getUserEntitlement(1);

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchUserEntitlements() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        UserEntitlementEntity userEntitlementEntity = UserEntitlementEntity.builder().build();
        Page<UserEntitlementEntity> mockPage = new PageImpl<>(List.of(userEntitlementEntity),
                                                              Pageable.unpaged(), 999L);
        when(userEntitlementRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<UserEntitlementEntity> result = userEntitlementService
            .searchUserEntitlements(UserEntitlementSearchDto.builder().build());

        // Assert
        assertEquals(List.of(userEntitlementEntity), result);

    }

    @Test
    void testGetPermissionsByBusinessUnitUserId() {
        // Arrange
        ApplicationFunctionEntity afe = ApplicationFunctionEntity.builder()
            .applicationFunctionId(100L).functionName("Add_Note").build();
        UserEntitlementEntity userEntitlementEntity = UserEntitlementEntity.builder()
            .userEntitlementId(1L).applicationFunction(afe).build();
        List<UserEntitlementEntity> list = List.of(userEntitlementEntity);
        when(userEntitlementRepository.findAllByBusinessUnitUser_BusinessUnitUserId(any())).thenReturn(list);

        // Act
        Set<Permission> result = userEntitlementService.getPermissionsByBusinessUnitUserId("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetUserStateByUsername() {
        // Arrange
        UserEntitlementEntity entitlement = UserEntitlementEntity.builder()
            .businessUnitUser(BusinessUnitUserEntity.builder()
                                  .businessUnitUserId("BUU_001")
                                  .user(UserEntity.builder()
                                            .userId(123L).username("John Smith").build())
                                  .businessUnit(BusinessUnitEntity.builder()
                                            .businessUnitId((short)101).build())
                                  .build())
            .applicationFunction(ApplicationFunctionEntity.builder()
                                  .applicationFunctionId(23L)
                                  .functionName("Read_Notes").build())
            .build();
        List<UserEntitlementEntity> list = List.of(entitlement);
        when(userEntitlementRepository.findAll(any(Specification.class))).thenReturn(list);

        // Act
        Optional<UserState> result = userEntitlementService.getUserStateByUsername("");

        // Assert
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(123L, result.get().getUserId());
        assertEquals("John Smith", result.get().getUserName());

    }

}

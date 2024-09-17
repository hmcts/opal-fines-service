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
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUserPermissions;
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitUserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitUserServiceTest {

    @Mock
    private BusinessUnitUserRepository businessUnitUserRepository;

    @Mock
    private UserEntitlementService userEntitlementService;

    @InjectMocks
    private BusinessUnitUserService businessUnitUserService;

    @Test
    void testGetBusinessUnitUser() {
        // Arrange

        BusinessUnitUserEntity businessUnitUserEntity = BusinessUnitUserEntity.builder().build();
        when(businessUnitUserRepository.getReferenceById(any())).thenReturn(businessUnitUserEntity);

        // Act
        BusinessUnitUserEntity result = businessUnitUserService.getBusinessUnitUser("1");

        // Assert
        assertNotNull(result);

    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchBusinessUnitUsers() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        BusinessUnitUserEntity businessUnitUserEntity = BusinessUnitUserEntity.builder().build();
        Page<BusinessUnitUserEntity> mockPage = new PageImpl<>(List.of(businessUnitUserEntity),
                                                               Pageable.unpaged(), 999L);
        when(businessUnitUserRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<BusinessUnitUserEntity> result = businessUnitUserService
            .searchBusinessUnitUsers(BusinessUnitUserSearchDto.builder().build());

        // Assert
        assertEquals(List.of(businessUnitUserEntity), result);

    }

    @Test
    void testGetAuthorisationRolesByUserId() {
        // Arrange
        BusinessUnitEntity bue = BusinessUnitEntity.builder().businessUnitId((short)100).build();
        BusinessUnitUserEntity businessUnitUserEntity = BusinessUnitUserEntity.builder()
            .businessUnitUserId("BUUserABCD").businessUnit(bue).build();
        List<BusinessUnitUserEntity> list = List.of(businessUnitUserEntity);
        when(businessUnitUserRepository.findAllByUser_UserId(any())).thenReturn(list);
        when(userEntitlementService.getPermissionsByBusinessUnitUserId(any())).thenReturn(Collections.emptySet());

        // Act
        Set<BusinessUnitUserPermissions> result = businessUnitUserService.getAuthorisationRolesByUserId(0L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

    }

    @Test
    void testGetLimitedRolesByUserId() {
        // Arrange
        BusinessUnitEntity bue = BusinessUnitEntity.builder().businessUnitId((short)100).build();
        BusinessUnitUserEntity businessUnitUserEntity = BusinessUnitUserEntity.builder()
            .businessUnitUserId("BUUserABCD").businessUnit(bue).build();
        List<BusinessUnitUserEntity> list = List.of(businessUnitUserEntity);
        when(businessUnitUserRepository.findAllByUser_UserId(any())).thenReturn(list);

        // Act
        Set<BusinessUnitUserPermissions> result = businessUnitUserService.getLimitedRolesByUserId(0L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

    }

}

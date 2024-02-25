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
import uk.gov.hmcts.opal.dto.search.BusinessUnitUserSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitUserEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitUserRepository;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessUnitUserServiceTest {

    @Mock
    private BusinessUnitUserRepository businessUnitUserRepository;

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


}

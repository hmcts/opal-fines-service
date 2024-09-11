package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
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
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;
import uk.gov.hmcts.opal.repository.DraftAccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftAccountServiceTest {

    @Mock
    private DraftAccountRepository draftAccountRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @InjectMocks
    private DraftAccountService draftAccountService;

    @Test
    void testGetDraftAccount() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        DraftAccountEntity result = draftAccountService.getDraftAccount(1);

        // Assert
        assertNotNull(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchDraftAccounts() {
        // Arrange
        FluentQuery.FetchableFluentQuery ffq = Mockito.mock(FluentQuery.FetchableFluentQuery.class);

        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        Page<DraftAccountEntity> mockPage = new PageImpl<>(List.of(draftAccountEntity), Pageable.unpaged(), 999L);
        when(draftAccountRepository.findBy(any(Specification.class), any())).thenAnswer(iom -> {
            iom.getArgument(1, Function.class).apply(ffq);
            return mockPage;
        });

        // Act
        List<DraftAccountEntity> result = draftAccountService.searchDraftAccounts(
            DraftAccountSearchDto.builder().build());

        // Assert
        assertEquals(List.of(draftAccountEntity), result);

    }

    @Test
    void testSubmitDraftAccounts_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .account(createAccountString())
            .build();
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnit));
        when(draftAccountRepository.save(any(DraftAccountEntity.class))).thenReturn(draftAccountEntity);

        // Act
        DraftAccountEntity result = draftAccountService.submitDraftAccount(addDraftAccountDto, "Charles");

        // Assert
        assertEquals(draftAccountEntity, result);
    }

    @Test
    void testSubmitDraftAccounts_fail() {
        // Arrange
        AddDraftAccountRequestDto addDraftAccountDto = AddDraftAccountRequestDto.builder()
            .account("{}")
            .build();
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder()
            .businessUnitName("Old Bailey")
            .build();

        when(businessUnitRepository.findById(any())).thenReturn(Optional.of(businessUnit));

        // Act
        RuntimeException re = assertThrows(RuntimeException.class, () ->
            draftAccountService.submitDraftAccount(addDraftAccountDto, "Charles"));

        // Assert
        assertEquals("Missing property in path $['accountCreateRequest']", re.getMessage());
    }

    @Test
    void testDeleteDraftAccount_success() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().createdDate(LocalDateTime.now()).build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        draftAccountService.deleteDraftAccount(1, Optional.empty());
    }

    @Test
    void testDeleteDraftAccount_fail1() {
        // Arrange
        DraftAccountEntity draftAccountEntity = mock(DraftAccountEntity.class);
        when(draftAccountEntity.getCreatedDate()).thenThrow(new EntityNotFoundException("No Entity in DB"));
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        EntityNotFoundException enfe = assertThrows(
            EntityNotFoundException.class, () -> draftAccountService.deleteDraftAccount(1, Optional.empty())
        );

        // Assert
        assertEquals("No Entity in DB", enfe.getMessage());
    }

    @Test
    void testDeleteDraftAccount_fail2() {
        // Arrange
        DraftAccountEntity draftAccountEntity = DraftAccountEntity.builder().build();
        when(draftAccountRepository.getReferenceById(any())).thenReturn(draftAccountEntity);

        // Act
        RuntimeException re = assertThrows(
            RuntimeException.class, () -> draftAccountService.deleteDraftAccount(8, Optional.empty())
        );

        // Assert
        assertEquals("Draft Account entity '8' does not exist in the DB.", re.getMessage());
    }

    private String createAccountString() {
        return """
            {
                "accountCreateRequest": {
                    "Defendant": {
                        "Surname": "Windsor",
                        "Forenames": "Charles",
                        "DOB": "August 1958"
                    },
                    "Account": {
                        "AccountType": "Fine"
                    }
                }
            }
            """;
    }
}

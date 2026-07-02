package uk.gov.hmcts.opal.service.report.operation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.ImpositionTotalsDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedOperationReportAccountRowDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.OriginatorType;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountHeaderViewRepositoryService;
import uk.gov.hmcts.opal.service.ImpositionService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentTermsRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@ExtendWith(MockitoExtension.class)
class DetailedRowDtoMapperDecoratorTest {

    @Mock
    private DetailedRowDtoCoreMapper delegate;

    @Mock
    private CommonRowMappingHelper helper;

    @Mock
    private DebtorDetailRepositoryService debtorService;

    @Mock
    private ImpositionService impositionService;

    @Mock
    private PaymentTermsRepositoryService paymentTermsService;

    @Mock
    private DefendantAccountHeaderViewRepositoryService headerViewService;

    private DetailedRowDtoMapperDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new DetailedRowDtoMapperDecorator();
        decorator.setDelegate(delegate);
        decorator.setHelper(helper);
        decorator.setDebtorService(debtorService);
        decorator.setImpositionService(impositionService);
        decorator.setPaymentTermsService(paymentTermsService);
        decorator.setHeaderViewService(headerViewService);
    }

    @Test
    void map_shouldApplyAllDecorationsForMacNewAccount() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(100L)
            .originatorType(OriginatorType.MAC_NEW_ACCOUNT)
            .imposedHearingDate(LocalDate.of(2024, 2, 1))
            .build();

        ReportMetadataContext context = new ReportMetadataContext();
        DetailedOperationReportAccountRowDto dto =
            new DetailedOperationReportAccountRowDto();

        ImpositionTotalsDto impositions = ImpositionTotalsDto.builder().build();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn("N");
        when(headerViewService.getArrearsTotalForDefendantAccount(100L))
            .thenReturn(new BigDecimal("500.58"));
        when(paymentTermsService.getPaymentTermsAsFormattedString(100L))
            .thenReturn("02/01/2024");
        when(impositionService.getAccountImpositionTotals(100L))
            .thenReturn(impositions);

        DetailedOperationReportAccountRowDto result = decorator.map(entity, context);

        assertThat(result).isSameAs(dto);
        assertThat(dto.getDateOfHearing()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(dto.getPaymentTerms()).isEqualTo("02/01/2024");
        assertThat(dto.getArrearsTotal()).isEqualByComparingTo("500.58");
        assertThat(dto.getParentOrGuardian()).isEqualTo("N");
        assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .contains("100");

        verify(delegate).map(entity, context);
        verify(delegate).mapImpositions(impositions, dto);
        verify(helper).parentGuardianValue(entity);
        verify(helper).applyParty(
            eq(entity),
            eq(dto),
            eq(context),
            anyBiConsumer(),
            anyBiConsumer()
        );
        verifyNoInteractions(debtorService);
    }

    @Test
    void map_shouldUseEarliestImpositionDateForFixedPenalty() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .originatorType(OriginatorType.FIXED_PENALTY)
            .build();

        ReportMetadataContext context = new ReportMetadataContext();
        DetailedOperationReportAccountRowDto dto =
            new DetailedOperationReportAccountRowDto();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn("N");
        when(headerViewService.getArrearsTotalForDefendantAccount(1L))
            .thenReturn(BigDecimal.ZERO);
        when(paymentTermsService.getPaymentTermsAsFormattedString(1L))
            .thenReturn("01/01/2024");
        when(impositionService.getEarliestImpositionDate(1L))
            .thenReturn(LocalDate.of(2024, 3, 1));
        when(impositionService.getAccountImpositionTotals(1L))
            .thenReturn(ImpositionTotalsDto.builder().build());

        decorator.map(entity, context);

        assertThat(dto.getDateOfHearing()).isEqualTo(LocalDate.of(2024, 3, 1));
        verify(impositionService).getEarliestImpositionDate(1L);
    }

    @Test
    void map_shouldUseImposedHearingDateForTransferInAccountWhenPresent() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(2L)
            .originatorType(OriginatorType.TRANSFER_IN_ACCOUNT)
            .imposedHearingDate(LocalDate.of(2024, 4, 5))
            .build();

        ReportMetadataContext context = new ReportMetadataContext();
        DetailedOperationReportAccountRowDto dto =
            new DetailedOperationReportAccountRowDto();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn("N");
        when(headerViewService.getArrearsTotalForDefendantAccount(2L))
            .thenReturn(BigDecimal.ZERO);
        when(paymentTermsService.getPaymentTermsAsFormattedString(2L))
            .thenReturn("01/01/2024");
        when(impositionService.getAccountImpositionTotals(2L))
            .thenReturn(ImpositionTotalsDto.builder().build());

        decorator.map(entity, context);

        assertThat(dto.getDateOfHearing()).isEqualTo(LocalDate.of(2024, 4, 5));
        verify(impositionService).getAccountImpositionTotals(2L);
    }

    @Test
    void map_shouldUseEarliestImpositionDateForTransferInAccountWhenHearingDateMissing() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(3L)
            .originatorType(OriginatorType.TRANSFER_IN_ACCOUNT)
            .imposedHearingDate(null)
            .build();

        ReportMetadataContext context = new ReportMetadataContext();
        DetailedOperationReportAccountRowDto dto =
            new DetailedOperationReportAccountRowDto();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn("N");
        when(headerViewService.getArrearsTotalForDefendantAccount(3L))
            .thenReturn(BigDecimal.ZERO);
        when(paymentTermsService.getPaymentTermsAsFormattedString(3L))
            .thenReturn("01/01/2024");
        when(impositionService.getEarliestImpositionDate(3L))
            .thenReturn(LocalDate.of(2024, 5, 6));
        when(impositionService.getAccountImpositionTotals(3L))
            .thenReturn(ImpositionTotalsDto.builder().build());

        decorator.map(entity, context);

        assertThat(dto.getDateOfHearing()).isEqualTo(LocalDate.of(2024, 5, 6));
        verify(impositionService).getEarliestImpositionDate(3L);
    }

    private static <T> BiConsumer<T, DetailedOperationReportAccountRowDto> anyBiConsumer() {
        return ArgumentMatchers.any();
    }
}
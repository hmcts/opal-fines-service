package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.generated.model.CourtFeeCommon;
import uk.gov.hmcts.opal.generated.model.CourtFeesResponse;

@Mapper(componentModel = "spring")
public interface CourtFeeMapper {

    default CourtFeesResponse toCourtFeesResponse(List<CourtFeeEntity> courtFees) {
        return CourtFeesResponse.builder()
            .courtFees(courtFees == null ? List.of() : toCourtFeeCommons(courtFees))
            .build();
    }

    List<CourtFeeCommon> toCourtFeeCommons(List<CourtFeeEntity> courtFee);

    CourtFeeCommon toCourtFeeCommon(CourtFeeEntity courtFee);
}

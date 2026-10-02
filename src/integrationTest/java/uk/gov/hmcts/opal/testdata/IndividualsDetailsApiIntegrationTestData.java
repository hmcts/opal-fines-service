package uk.gov.hmcts.opal.testdata;

import static uk.gov.hmcts.opal.common.dto.ToJsonString.toJsonString;

import java.util.List;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Address;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.IndividualsDetailsAddressesResponse;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.IndividualsDetailsResponse400;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.IndividualsDetailsResponse404;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Links1;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Residence;
import uk.gov.hmcts.opal.hmrc.generated.IndividualsDetails.model.Self;

public final class IndividualsDetailsApiIntegrationTestData {
    public static final String minResponse = toJsonString(
        new IndividualsDetailsAddressesResponse.Builder()
            .links(
                Links1.builder()
                    .self(
                        Self.builder()
                            .href("")
                            .build()
                    )
                    .build()
            )
            .residences(List.of(
                new Residence.Builder()
                    .residenceType("")
                    .inUse(Boolean.TRUE)
                    .address(
                        new Address.Builder()
                            .line1("")
                            .line2("")
                            .line3("")
                            .line4("")
                            .line5("")
                            .postcode("")
                            .build()
                    )
                    .build()
            ))
            .build()
    );

    public static final String maxResponse = toJsonString(
        new IndividualsDetailsAddressesResponse.Builder()
            .links(
                Links1.builder()
                    .self(
                        Self.builder()
                            .href("/individuals/details?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430")
                            .build()
                    )
                    .build()
            )
            .residences(List.of(
                new Residence.Builder()
                    .residenceType("NOMINATED")
                    .inUse(Boolean.TRUE)
                    .address(
                        new Address.Builder()
                            .line1("24 Trinity Street")
                            .line2("Dawley Bank")
                            .line3("Telford")
                            .line4("Shropshire")
                            .line5("UK")
                            .postcode("TF3 4ER")
                            .build()
                    )
                    .build()
            ))
            .build()
    );

    public static final String requiredOnlyResponse = toJsonString(
        new IndividualsDetailsAddressesResponse.Builder()
            .links(
                Links1.builder()
                    .self(
                        Self.builder()
                            .href("/individuals/details?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430")
                            .build()
                    )
                    .build()
            )
            .build()
    );

    public static final String multipleItemsResponse = toJsonString(
        new IndividualsDetailsAddressesResponse.Builder()
            .links(
                Links1.builder()
                    .self(
                        Self.builder()
                            .href("/individuals/details?matchId=57072660-1df9-4aeb-b4ea-cd2d7f96e430")
                            .build()
                    )
                    .build()
            )
            .residences(List.of(
                new Residence.Builder()
                    .residenceType("NOMINATED")
                    .inUse(Boolean.TRUE)
                    .address(
                        new Address.Builder()
                            .line1("24 Trinity Street")
                            .line2("Dawley Bank")
                            .line3("Telford")
                            .line4("Shropshire")
                            .line5("UK")
                            .postcode("TF3 4ER")
                            .build()
                    )
                    .build(),
                new Residence.Builder()
                    .residenceType("BASE")
                    .inUse(Boolean.FALSE)
                    .address(
                        new Address.Builder()
                            .line1("La Petite Maison")
                            .line2("Rue de Bastille")
                            .line3("Vieux Ville")
                            .line4("Dordogne")
                            .line5("Grange")
                            .build()
                    )
                    .build()
            ))
            .build()
    );

    public static final String badRequestResponse = toJsonString(
        new IndividualsDetailsResponse400().toBuilder()
            .code("INVALID_REQUEST")
            .build()
    );

    public static final String notFoundResponse = toJsonString(
        new IndividualsDetailsResponse404().toBuilder()
            .code("NOT_FOUND")
            .build()
    );

}

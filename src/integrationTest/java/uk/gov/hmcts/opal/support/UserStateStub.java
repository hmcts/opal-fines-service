package uk.gov.hmcts.opal.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.DomainBusinessUnitUsers;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.PermissionDescriptor;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2.UserStateV2Builder;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStatus;

@Getter
@Setter
public class UserStateStub {


    private UserStateV2 userState;
    private String jwtStr;
    private boolean isStubbed;

    private static final String TEST_JWT_SECRET = "SomeVeryGoodAndVeryLongSecretThatNoOneWillEverGuess";
    public static final ObjectMapper USER_STATE_MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .addMixIn(Permission.class, PermissionJsonMixin.class);

    @JsonIgnoreProperties({"id", "description"})
    private abstract static class PermissionJsonMixin {
    }

    public UserStateStub() {
        this(generateJwt());
    }

    public UserStateStub(String jwtStr) {
        this.jwtStr = jwtStr;
        this.userState = getDefaultUserState();
    }

    private static String generateJwt() {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .subject("k9LpT2xVqR8m")
            .claim("name", "Pablo")
            .claim("preferred_username", "opal-test@HMCTS.NET")
            .issueTime(java.util.Date.from(now))
            .expirationTime(java.util.Date.from(now.plusSeconds(3600)))
            .build();

        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
            claims
        );

        try {
            signedJwt.sign(new MACSigner(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8)));
            return signedJwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to generate test JWT", e);
        }
    }

    public String getUserStateAsJson() {
        try {
            return USER_STATE_MAPPER.writeValueAsString(getUserState());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise user state", e);
        }
    }

    public void createAuthentication() {
        stubFor(get("/v2/users/0/state")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .withBody(getUserStateAsJson())
            )
        );
        this.isStubbed = true;
    }

    public OpalJwtAuthenticationToken getOpalJwtAuthenticationToken() {
        return new OpalJwtAuthenticationToken(getUserState(), Domain.FINES,
            parseJwt(jwtStr),
            new ArrayList<>(),
            getUserState());
    }

    public RequestPostProcessor getAuthenticaitonRequestPostProcessor() {
        return request -> {
            OpalJwtAuthenticationToken token = getOpalJwtAuthenticationToken();
            token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            return SecurityMockMvcRequestPostProcessors.authentication(token).postProcessRequest(request);
        };
    }

    public RequestPostProcessor getInvalidAuthenticaitonRequestPostProcessor() {
        return SecurityMockMvcRequestPostProcessors.authentication(
            new Authentication() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return List.of();
                }

                @Override
                public @Nullable Object getCredentials() {
                    return null;
                }

                @Override
                public @Nullable Object getDetails() {
                    return null;
                }

                @Override
                public @Nullable Object getPrincipal() {
                    return null;
                }

                @Override
                public boolean isAuthenticated() {
                    return false;
                }

                @Override
                public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

                }

                @Override
                public String getName() {
                    return "";
                }
            }
        );
    }


    //Convert token string into Jwt for auth
    private Jwt parseJwt(String token) {
        try {
            SignedJWT parsed = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = parsed.getJWTClaimsSet();

            Map<String, Object> headers = new LinkedHashMap<>(parsed.getHeader().toJSONObject());
            Map<String, Object> claims = new LinkedHashMap<>(claimsSet.getClaims());

            Jwt.Builder builder = Jwt.withTokenValue(token)
                .headers(h -> h.putAll(headers))
                .claims(c -> c.putAll(claims));

            if (claimsSet.getIssueTime() != null) {
                builder.issuedAt(claimsSet.getIssueTime().toInstant());
            }
            if (claimsSet.getExpirationTime() != null) {
                builder.expiresAt(claimsSet.getExpirationTime().toInstant());
            }

            return builder.build();
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to parse JWT", e);
        }
    }

    public String getAuthorizationToken() {
        if (!isStubbed) {
            createAuthentication();
        }
        return jwtStr;
    }

    public String getBearerToken() {
        return "Bearer " + getAuthorizationToken();
    }

    public UserStateV2Builder getDefaultUserStateBuilder() {
        List<Short> businessUnits = List.of(
            (short) 65,
            (short) 68,
            (short) 70,
            (short) 73,
            (short) 77,
            (short) 78
        );

        List<BusinessUnitUser> businessUnitUsers = new ArrayList<>();
        businessUnits
            .forEach(aShort -> businessUnitUsers.add(
                createBusinessUnitUser("L0" + aShort + "JG", aShort, createAllPermissions())));

        Map<Domain, DomainBusinessUnitUsers> domains = new EnumMap<>(Domain.class);
        domains.put(
            Domain.FINES,
            DomainBusinessUnitUsers.builder()
                .businessUnitUsers(businessUnitUsers)
                .build()
        );

        return UserStateV2.builder()
            .userId(500000000L)
            .username("opal-test@HMCTS.NET")
            .name("Pablo")
            .status(UserStatus.ACTIVE)
            .version(0L)
            .domains(domains);
    }

    private Set<Permission> createAllPermissions() {
        return Arrays.stream(FinesPermission.values())
            .map(this::createPermission)
            .collect(Collectors.toSet());
    }

    private Permission createPermission(PermissionDescriptor permissionDescriptor) {
        return Permission.builder()
            .permissionId(permissionDescriptor.getId())
            .permissionName(permissionDescriptor.getDescription())
            .build();
    }

    private BusinessUnitUser createBusinessUnitUser(String businessUnitUserId, short businessUnitId,
        Set<Permission> permissions) {
        return BusinessUnitUser.builder()
            .businessUnitUserId(businessUnitUserId)
            .businessUnitId(businessUnitId)
            .permissions(new HashSet<>(permissions))
            .build();
    }

    private UserStateV2 getDefaultUserState() {
        return getDefaultUserStateBuilder().build();
    }

    public void setupWithNoPermissions() {
        this.userState = getDefaultUserState();
        getDomainBusinessUnitUsers().getBusinessUnitUsers().clear();
    }

    private DomainBusinessUnitUsers getDomainBusinessUnitUsers() {
        return userState.getDomains().get(Domain.FINES);
    }


    public void addPermissions(short businessUnitId, FinesPermission... values) {
        Set<Permission> permissions = Arrays.stream(values)
            .map(this::createPermission)
            .collect(Collectors.toSet());

        DomainBusinessUnitUsers businessUnitUsers = getDomainBusinessUnitUsers();

        Optional<BusinessUnitUser> businessUnitUserOpt =
            businessUnitUsers.getBusinessUnitUserForBusinessUnit(businessUnitId);

        if (businessUnitUserOpt.isEmpty()) {
            BusinessUnitUser businessUnitUser =
                createBusinessUnitUser("L0" + businessUnitId + "JG", businessUnitId, permissions);
            businessUnitUsers.getBusinessUnitUsers().add(businessUnitUser);
            return;
        }
        businessUnitUserOpt.get().getPermissions().addAll(permissions);
    }
}

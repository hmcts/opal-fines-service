package uk.gov.hmcts.opal.authentication.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "azureTokenClient", url = "https://login.microsoftonline.com")
public interface AzureTokenClient {

    @PostMapping(value = "/oauth2/v2.0/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String getAccessToken(
        @RequestParam("grant_type") String grantType,
        @RequestParam("client_id") String clientId,
        @RequestParam("client_secret") String clientSecret,
        @RequestParam("scope") String scope,
        @RequestParam("username") String username,
        @RequestParam("password") String password
    );
}


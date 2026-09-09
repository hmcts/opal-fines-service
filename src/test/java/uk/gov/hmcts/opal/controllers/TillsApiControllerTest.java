package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.service.opal.GetTillService;

@ExtendWith(MockitoExtension.class)
class TillsApiControllerTest {

    private static final Long TILL_ID = 123L;

    @Mock
    private GetTillService getTillService;

    @InjectMocks
    private TillsApiController controller;

    @Test
    void whenCalled_returnsTill_happyPath() {
        TillsGetResponse expected = new TillsGetResponse();
        when(getTillService.getTill(TILL_ID)).thenReturn(expected);

        ResponseEntity<TillsGetResponse> response = controller.getTill(TILL_ID);

        assertAll(
            () -> assertEquals(200, response.getStatusCode().value()),
            () -> assertSame(expected, response.getBody()),
            () -> verify(getTillService).getTill(TILL_ID)
        );
    }
}

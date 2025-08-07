package uk.gov.hmcts.opal.disco.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.disco.NoteServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyNoteService;
import uk.gov.hmcts.opal.disco.opal.NoteService;
import uk.gov.hmcts.opal.service.proxy.ProxyTestsBase;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NoteServiceProxyTest extends ProxyTestsBase {

    private AutoCloseable closeable;

    @Mock
    private NoteService opalService;

    @Mock
    private LegacyNoteService legacyService;

    @InjectMocks
    private NoteServiceProxy noteServiceProxy;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    void testMode(NoteServiceInterface targetService, NoteServiceInterface otherService) {
        testSaveNote(targetService, otherService);
        testSearchNotes(targetService, otherService);
    }

    void testSaveNote(NoteServiceInterface targetService, NoteServiceInterface otherService) {
        // Given: a NoteDto is returned from the target service
        NoteDto noteDto = new NoteDto();
        when(targetService.saveNote(any(NoteDto.class))).thenReturn(noteDto);

        // When: saveNote is called on the proxy
        NoteDto noteResult = noteServiceProxy.saveNote(noteDto);

        // Then: target service should be used, and the returned note should be as expected
        verify(targetService).saveNote(noteDto);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(noteDto, noteResult);
    }

    void testSearchNotes(NoteServiceInterface targetService, NoteServiceInterface otherService) {
        // Given: a notes list result is returned from the target service
        NoteDto noteDto = NoteDto.builder().build();
        List<NoteDto> notesList = List.of(noteDto);
        when(targetService.searchNotes(any())).thenReturn(notesList);

        // When: searchNotes is called on the proxy
        NoteSearchDto criteria = NoteSearchDto.builder().build();
        List<NoteDto> listResult = noteServiceProxy.searchNotes(criteria);

        // Then: target service should be used, and the returned list should be as expected
        verify(targetService).searchNotes(criteria);
        verifyNoInteractions(otherService);
        Assertions.assertEquals(notesList, listResult);
    }

    @Test
    void shouldUseOpalNoteServiceWhenModeIsNotLegacy() {
        // Given: app mode is set
        setMode(OPAL);
        // Then: the target service is called, but the other service is not
        testMode(opalService, legacyService);
    }

    @Test
    void shouldUseLegacyNoteServiceWhenModeIsLegacy() {
        // Given: app mode is set
        setMode(LEGACY);
        // Then: the target service is called, but the other service is not
        testMode(legacyService, opalService);
    }
}

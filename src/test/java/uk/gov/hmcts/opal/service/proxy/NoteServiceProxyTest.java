package uk.gov.hmcts.opal.service.proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyNoteService;
import uk.gov.hmcts.opal.service.opal.NoteService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NoteServiceProxyTest {

    private AutoCloseable closeable;

    @Mock
    private NoteService opalNoteService;

    @Mock
    private LegacyNoteService legacyNoteService;

    @Mock
    private DynamicConfigService dynamicConfigService;

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

    @Test
    void shouldUseOpalNoteServiceWhenModeIsNotLegacy() {
        // Given: a NoteDto and the app mode is set to "opal"
        NoteDto noteDto = new NoteDto();
        AppMode appMode = AppMode.builder().mode("opal").build();
        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(opalNoteService.saveNote(noteDto)).thenReturn(noteDto);

        // When: saveNote is called on the proxy
        NoteDto noteResult = noteServiceProxy.saveNote(noteDto);

        // Then: opalNoteService should be used, and the returned note should be as expected
        verify(opalNoteService).saveNote(noteDto);
        verifyNoInteractions(legacyNoteService);
        Assertions.assertEquals(noteDto, noteResult);

        // Given: a notes list result and the app mode is set to "opal"
        List<NoteDto> notesList = List.of(noteDto);
        when(opalNoteService.searchNotes(any())).thenReturn(notesList);

        // When: searchNotes is called on the proxy
        NoteSearchDto criteria = NoteSearchDto.builder().build();
        List<NoteDto> listResult = noteServiceProxy.searchNotes(criteria);

        // Then: opalNoteService should be used, and the returned list should be as expected
        verify(opalNoteService).searchNotes(criteria);
        verifyNoInteractions(legacyNoteService);
        Assertions.assertEquals(notesList, listResult);
    }

    @Test
    void shouldUseLegacyNoteServiceWhenModeIsLegacy() {
        // Given: a NoteDto and the app mode is set to "legacy"
        NoteDto noteDto = new NoteDto();
        AppMode appMode = AppMode.builder().mode("legacy").build();

        when(dynamicConfigService.getAppMode()).thenReturn(appMode);
        when(legacyNoteService.saveNote(noteDto)).thenReturn(noteDto);

        // When: saveNote is called on the proxy
        NoteDto result = noteServiceProxy.saveNote(noteDto);

        // Then: legacyNoteService should be used, and the returned note should be as expected
        verify(legacyNoteService).saveNote(noteDto);
        verifyNoInteractions(opalNoteService);
        Assertions.assertEquals(noteDto, result);

        // Given: a notes list result and the app mode is set to "legacy"
        List<NoteDto> notesList = List.of(noteDto);
        when(opalNoteService.searchNotes(any())).thenReturn(notesList);

        // When: searchNotes is called on the proxy
        NoteSearchDto criteria = NoteSearchDto.builder().build();
        List<NoteDto> listResult = noteServiceProxy.searchNotes(criteria);

        // Then: opalNoteService should be used, and the returned list should be as expected
        verify(legacyNoteService).searchNotes(criteria);
        verifyNoInteractions(opalNoteService);
        Assertions.assertEquals(Collections.emptyList(), listResult); // Not yet implemented in Legacy mode
    }
}

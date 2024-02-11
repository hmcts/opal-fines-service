package uk.gov.hmcts.opal.service.opal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void testSaveNoteWithPostedDate() {
        // Arrange
        LocalDateTime postedDate = LocalDateTime.now();

        NoteDto noteDto = NoteDto.builder()
            .noteType("AC")
            .associatedRecordType("defendants_accounts")
            .associatedRecordId("123")
            .noteText("Sample note")
            .postedDate(postedDate)
            .postedBy("user123")
            .build();

        NoteEntity noteEntity = NoteEntity.builder()
            .noteId(1L)
            .noteType("AC")
            .associatedRecordType("defendants_accounts")
            .associatedRecordId("123")
            .noteText("Sample note")
            .postedDate(postedDate)
            .postedBy("user123")
            .build();

        when(noteRepository.save(any(NoteEntity.class))).thenReturn(noteEntity);

        // Act
        NoteDto savedNoteDto = noteService.saveNote(noteDto);

        // Assert
        assertEquals(noteEntity.getNoteId(), savedNoteDto.getNoteId());
        verify(noteRepository, times(1)).save(any(NoteEntity.class));
    }

    @Test
    void testSaveNoteWithNullPostedDate() {
        // Arrange
        NoteDto noteDto = NoteDto.builder()
            .noteType("AC")
            .associatedRecordType("defendants_accounts")
            .associatedRecordId("123")
            .noteText("Sample note")
            .postedDate(null)
            .postedBy("user123")
            .build();

        NoteEntity noteEntity =  NoteEntity.builder().noteId(1L).build();

        when(noteRepository.save(any(NoteEntity.class))).thenAnswer(invocation -> {
            NoteEntity entity = invocation.getArgument(0);
            entity.setNoteId(1L); // Set ID to simulate repository behavior
            return entity;
        });

        // Act
        NoteDto savedNoteDto = noteService.saveNote(noteDto);

        // Assert
        Assertions.assertNotNull(savedNoteDto.getPostedDate());
        assertTrue(savedNoteDto.getPostedDate().isBefore(LocalDateTime.now().plusMinutes(1))
                       && savedNoteDto.getPostedDate().isAfter(LocalDateTime.now().minusMinutes(1)),
                   "Posted date should be around the current time");
        verify(noteRepository, times(1)).save(any(NoteEntity.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSearchNotes() {
        // Arrange

        NoteEntity noteEntity = NoteEntity.builder().build();
        Page<NoteEntity> mockPage = new PageImpl<>(List.of(noteEntity), Pageable.unpaged(), 999L);
        when(noteRepository.findBy(any(Specification.class), any())).thenReturn(mockPage);

        // Act
        List<NoteDto> result = noteService.searchNotes(NoteSearchDto.builder().build());

        // Assert
        assertEquals(1, result.size());

    }


}

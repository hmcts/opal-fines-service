package uk.gov.hmcts.opal.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "NoteService")
public class NoteService implements NoteServiceInterface {

    private final NoteRepository noteRepository;

    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        return toNoteDto(noteRepository.save(toNoteEntity(noteDto)));
    }

    public NoteEntity toNoteEntity(NoteDto noteDto) {
        return NoteEntity.builder()
            .noteType(noteDto.getNoteType())
            .associatedRecordType(noteDto.getAssociatedRecordType())
            .associatedRecordId(noteDto.getAssociatedRecordId())
            .noteText(noteDto.getNoteText())
            .postedDate(noteDto.getPostedDate() == null ? LocalDateTime.now() : noteDto.getPostedDate())
            .postedBy(noteDto.getPostedBy())
            .build();
    }

    public NoteDto toNoteDto(NoteEntity entity) {
        return NoteDto.builder()
            .noteId(entity.getNoteId()) // This will be the generated ID
            .noteType(entity.getNoteType())
            .associatedRecordType(entity.getAssociatedRecordType())
            .associatedRecordId(entity.getAssociatedRecordId())
            .noteText(entity.getNoteText())
            .postedDate(entity.getPostedDate())
            .postedBy(entity.getPostedBy())
            .build();
    }
}

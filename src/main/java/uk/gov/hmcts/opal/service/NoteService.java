package uk.gov.hmcts.opal.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoteService {


    private final NoteRepository noteRepository;

    public NoteDto saveNote(NoteDto noteDto) {
        NoteEntity noteEntity = NoteEntity.builder()
            .noteType(noteDto.getNoteType())
            .associatedRecordType(noteDto.getAssociatedRecordType())
            .associatedRecordId(noteDto.getAssociatedRecordId())
            .noteText(noteDto.getNoteText())
            .postedDate(noteDto.getPostedDate() == null ? LocalDateTime.now() : noteDto.getPostedDate())
            .postedBy(noteDto.getPostedBy())
            .build();

        NoteEntity savedNoteEntity = noteRepository.save(noteEntity);

        return NoteDto.builder()
            .noteId(savedNoteEntity.getNoteId()) // This will be the generated ID
            .noteType(savedNoteEntity.getNoteType())
            .associatedRecordType(savedNoteEntity.getAssociatedRecordType())
            .associatedRecordId(savedNoteEntity.getAssociatedRecordId())
            .noteText(savedNoteEntity.getNoteText())
            .postedDate(savedNoteEntity.getPostedDate())
            .postedBy(savedNoteEntity.getPostedBy())
            .build();
    }
}

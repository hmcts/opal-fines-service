package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteEntity_;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.jpa.NoteSpecs;
import uk.gov.hmcts.opal.service.NoteServiceInterface;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "NoteService")
public class NoteService implements NoteServiceInterface {

    private final NoteRepository noteRepository;

    private final NoteSpecs specs = new NoteSpecs();

    @Override
    @FeatureToggle(feature = "add-note", value = true)
    public NoteDto saveNote(NoteDto noteDto) {
        // Restrict the 'postedBy' to 20 characters length
        String postedBy = Optional.ofNullable(noteDto.getPostedBy())
            .map(s -> StringUtils.substring(s, 0, 20)).orElse(null);
        noteDto.setPostedBy(postedBy);

        return toNoteDto(noteRepository.save(toNoteEntity(noteDto)));
    }

    @Override
    public List<NoteDto> searchNotes(NoteSearchDto criteria) {

        Sort dateSort = Sort.by(Sort.Direction.DESC, NoteEntity_.POSTED_DATE);

        Page<NoteEntity> notesPage = noteRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.sortBy(dateSort).page(Pageable.unpaged()));

        List<NoteDto> noteDtos = notesPage.getContent().stream()
            .map(this::toNoteDto)
            .collect(Collectors.toList());

        return noteDtos;
    }

    public NoteEntity toNoteEntity(NoteDto noteDto) {
        return NoteEntity.builder()
            .noteType(noteDto.getNoteType())
            .associatedRecordType(noteDto.getAssociatedRecordType())
            .associatedRecordId(noteDto.getAssociatedRecordId())
            .noteText(noteDto.getNoteText())
            .postedDate(noteDto.getPostedDate() == null ? LocalDateTime.now() : noteDto.getPostedDate())
            .postedBy(noteDto.getPostedBy())
            .postedByAad(noteDto.getPostedByAAD())
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
            .postedByAAD(entity.getPostedByAad())
            .build();
    }
}

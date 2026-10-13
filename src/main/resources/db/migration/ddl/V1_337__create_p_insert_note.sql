DROP PROCEDURE IF EXISTS p_insert_note;

CREATE PROCEDURE p_insert_note(
    IN pi_associated_record_type    notes.associated_record_type%TYPE,
    IN pi_associated_record_id      notes.associated_record_id%TYPE,
    IN pi_note_text                 notes.note_text%TYPE,
    IN pi_posted_by                 notes.posted_by%TYPE,
    IN pi_posted_by_name            notes.posted_by_name%TYPE
)
LANGUAGE plpgsql
AS $$
/**
* OPAL Program
*
* MODULE      : create_p_insert_note.sql
*
* DESCRIPTION : Create suspense note records during till allocation.
*
* PARAMETERS  : pi_associated_record_type - Associated record type for the note
*               pi_associated_record_id   - Associated record identifier for the note
*               pi_note_text              - Note text entered for the suspense item
*               pi_posted_by              - User identifier posting the note
*               pi_posted_by_name         - User name posting the note
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 02/10/2026    C Cho       1.0         PO-3472 - Create p_insert_note
*
**/
BEGIN
    IF pi_associated_record_type IS NULL THEN
        RAISE EXCEPTION 'pi_associated_record_type must be provided'
            USING ERRCODE = 'P3306'
                , DETAIL = 'p_insert_note: pi_associated_record_type is required';
    END IF;

    IF pi_associated_record_id IS NULL THEN
        RAISE EXCEPTION 'pi_associated_record_id must be provided'
            USING ERRCODE = 'P3307'
                , DETAIL = 'p_insert_note: pi_associated_record_id is required';
    END IF;

    IF NULLIF(pi_note_text, '') IS NULL THEN
        RAISE EXCEPTION 'pi_note_text must be provided'
            USING ERRCODE = 'P3308'
                , DETAIL = 'p_insert_note: pi_note_text is required';
    END IF;

    INSERT INTO notes (
        note_id,
        note_type,
        associated_record_type,
        associated_record_id,
        note_text,
        posted_date,
        posted_by,
        posted_by_name
    )
    VALUES (
        nextval('note_id_seq'),
        'AA'::t_note_type_enum,
        pi_associated_record_type,
        pi_associated_record_id,
        pi_note_text,
        CURRENT_TIMESTAMP,
        pi_posted_by,
        pi_posted_by_name
    );
END;
$$;

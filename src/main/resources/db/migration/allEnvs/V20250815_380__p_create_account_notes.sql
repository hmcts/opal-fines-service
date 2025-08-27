CREATE OR REPLACE PROCEDURE p_create_account_notes (
    IN pi_defendant_account_id  defendant_accounts.defendant_account_id%TYPE,
    IN pi_posted_by             enforcements.posted_by%TYPE,            
    IN pi_posted_by_name        enforcements.posted_by_name%TYPE,
    IN pi_account_notes_json    JSON
)
LANGUAGE 'plpgsql'
AS
$BODY$
/**
* CGI OPAL Program
*
* MODULE      : p_create_account_notes.sql
*
* DESCRIPTION : Process the account notes Json object for the related defendant.
                Updates the account comments column on the DEFENDANT_ACCOUNTS table.
*               Note type AC: defendant_accounts.account_comments will be updated. If more than 1 AC entry is passed an exception is raised.
*                         AN: These will be ignored, as per design.
*                         AA: A maximum of 2 AA records will be inserted into the Notes table, in serial order, all others will be ignored.
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_account_notes_json   - The account notes Json array object related to the defendant
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1035 - Process the account notes Json object for the related defendant and 
*                                                 update the account comments column on the DEFENDANT_ACCOUNTS table
*
**/
DECLARE
    v_pg_exception_detail               TEXT;
    v_note_json                         JSON;
    v_note_type                         notes.note_type%TYPE;
    v_note_serial                       INTEGER;
    v_note_AC_Text                      defendant_accounts.account_comments%TYPE := NULL;
    v_account_notes_AA_count            INTEGER := 0;
    v_account_notes_AA_inserted_count   INTEGER := 0;
    v_account_notes_AC_count            INTEGER := 0;
BEGIN

    --Check if the passed Json is NULL
    IF pi_account_notes_json IS NULL OR JSON_TYPEOF(pi_account_notes_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_account_notes: There were no account notes to process for defendant_account_id = %', pi_defendant_account_id;

    ELSE 
        
        --Loop through each account note, in account_note_serial order
        FOR v_note_json IN
            SELECT value
              FROM JSON_ARRAY_ELEMENTS(pi_account_notes_json) AS t(value)
            ORDER BY (value ->> 'account_note_serial')::INTEGER
        LOOP
            
            v_note_type   := v_note_json ->> 'note_type';
            v_note_serial := (v_note_json ->> 'account_note_serial')::INTEGER;
            
            CASE v_note_type
                WHEN 'AC' THEN
                    v_account_notes_AC_count := v_account_notes_AC_count + 1;
                    v_note_AC_Text           := v_note_json ->> 'account_note_text';

                WHEN 'AN' THEN

                    --Ignore AN note types

                WHEN 'AA' THEN
                    v_account_notes_AA_count := v_account_notes_AA_count + 1;

                    --Only insert a maximum of two AA records into Notes, ignore the rest
                    IF v_account_notes_AA_count < 3 THEN

                        v_account_notes_AA_inserted_count := v_account_notes_AA_inserted_count + 1;

                        --Insert NOTES record
                        INSERT INTO notes (
                              note_id
                            , note_type
                            , associated_record_type
                            , associated_record_id
                            , note_text
                            , posted_date
                            , posted_by
                            , posted_by_name
                        )
                        VALUES ( 
                              NEXTVAL('note_id_seq')
                            , v_note_type
                            , 'defendant_accounts'
                            , pi_defendant_account_id
                            , v_note_json ->> 'account_note_text'
                            , CLOCK_TIMESTAMP()
                            , pi_posted_by
                            , pi_posted_by_name
                        );

                    END IF;

                ELSE
                    --Raise custom exception
                    RAISE EXCEPTION 'Note_type % is not valid', v_note_type
                        USING ERRCODE = 'P2012'
                            , DETAIL = 'p_create_account_notes: defendant_account_id = ' || pi_defendant_account_id;
            END CASE;
            
        END LOOP;

        RAISE INFO 'p_create_account_notes: Inserted % AA notes records, out of %, for defendant_account_id = %', v_account_notes_AA_inserted_count, v_account_notes_AA_count, pi_defendant_account_id;

        --Perform checks before updating the DEFENDANT_ACCOUNTS table
        IF v_account_notes_AC_count > 1 THEN

            --Raise custom exception     
            RAISE EXCEPTION 'Only one AC note type is expected. Number of AC entries = %', v_account_notes_AC_count
                USING ERRCODE = 'P2013'
                    , DETAIL = 'p_create_account_notes: defendant_account_id = ' || pi_defendant_account_id;

        END IF;

        --Update defendant_accounts table with account comments
        UPDATE defendant_accounts
           SET account_comments = v_note_AC_Text
         WHERE defendant_account_id = pi_defendant_account_id;
        
        RAISE INFO 'p_create_account_notes: defendant_accounts has been updated with account comments. defendant_account_id = %', pi_defendant_account_id;

    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2012' OR SQLSTATE 'P2013' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_account_notes: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_account_notes: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_account_notes: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$BODY$;

COMMENT ON PROCEDURE p_create_account_notes
    IS 'Procedure to process the account notes Json object for the related defendant';

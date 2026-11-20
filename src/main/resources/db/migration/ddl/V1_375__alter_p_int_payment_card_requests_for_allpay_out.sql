DROP PROCEDURE IF EXISTS p_int_payment_card_requests;

CREATE OR REPLACE PROCEDURE p_int_payment_card_requests(
    OUT po_payment_card_requests_json JSON,
    OUT po_interface_job_id           interface_jobs.interface_job_id%TYPE
)
LANGUAGE plpgsql
AS $$
/**
* OPAL Program
*
* MODULE      : p_int_payment_card_requests
*
* DESCRIPTION : Retrieves, and passes back, data related to payment card requests so they can be processed by the back-end for Allpay Out
*
* PARAMETERS  : po_payment_card_requests_json - JSON array containing an entry of information, required for Allpay Out, for each payment card request
*               po_interface_job_id           - Interface job created for this invocation
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
* 11/11/2026    TMc         2.0         PO-11165 - Alter PAYMENT_CARD_REQUESTS table and related Stored Procedure
*
**/
DECLARE
    c_interface_name            CONSTANT interface_jobs.interface_name%TYPE := 'payment_card_requests';
    c_configuration_name        CONSTANT configuration_items.item_name%TYPE := 'BUSINESS_UNIT_ALLPAY_CLIENT';
    c_non_printable_ascii       CONSTANT TEXT := '[^ -~]';
    c_excluded_chars            CONSTANT TEXT := '[%()*+<=>?\[\]{}]';
    c_max_length_debtor_name    CONSTANT INTEGER := 19;

    r_request                   RECORD;

    v_address_lines             TEXT[];
    v_allpay_client_code        configuration_items.item_value%TYPE;
    v_pcr_defendant_account_ids       BIGINT[] := ARRAY[]::BIGINT[];
    v_name_on_card              TEXT;
    v_title_initials            TEXT;
    v_pg_exception_detail       TEXT;
BEGIN
    po_payment_card_requests_json := '[]'::JSON;

    FOR r_request IN
        SELECT pcr.defendant_account_id
          FROM payment_card_requests pcr
         WHERE pcr.interface_job_id IS NULL
        ORDER BY pcr.defendant_account_id
        FOR UPDATE OF pcr SKIP LOCKED
    LOOP
        --Retrieve debtor details and clean data
        SELECT da.defendant_account_id,
               da.business_unit_id,
               da.account_number,
               dap.party_id,
               dap.association_type,
               p.organisation,
               CASE WHEN p.organisation THEN NULL
                    ELSE NULLIF(TRIM(regexp_replace(regexp_replace(COALESCE(p.title, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')),'')
               END AS title,
               CASE WHEN p.organisation THEN NULL
                    ELSE (
                          SELECT string_agg(substr(word, 1, 1), ' ' ORDER BY word_position)
                            FROM regexp_split_to_table(
                                    TRIM(regexp_replace(regexp_replace(COALESCE(p.forenames, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')), ' +')
                            WITH ORDINALITY AS words(word, word_position)
                           WHERE word <> ''
                         ) 
               END AS initials,
               CASE WHEN p.organisation THEN 
                         TRIM(SUBSTRING(TRIM(regexp_replace(regexp_replace(COALESCE(p.organisation_name, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) FROM 1 FOR c_max_length_debtor_name))
                    ELSE 
                         TRIM(SUBSTRING(TRIM(regexp_replace(regexp_replace(COALESCE(p.surname, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) FROM 1 FOR c_max_length_debtor_name))
               END AS debtor_name,
               TRIM(regexp_replace(regexp_replace(COALESCE(p.address_line_1, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) AS address_line_1,
               TRIM(regexp_replace(regexp_replace(COALESCE(p.address_line_2, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) AS address_line_2,
               TRIM(regexp_replace(regexp_replace(COALESCE(p.address_line_3, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) AS address_line_3,
               TRIM(regexp_replace(regexp_replace(COALESCE(p.address_line_4, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) AS address_line_4,
               TRIM(regexp_replace(regexp_replace(COALESCE(p.postcode, ''), c_non_printable_ascii, '', 'g'), c_excluded_chars, '', 'g')) AS postcode
        INTO STRICT r_request
        FROM defendant_accounts da
        JOIN defendant_account_parties dap
          ON dap.defendant_account_id = da.defendant_account_id
         AND dap.debtor = TRUE
        JOIN parties p
          ON p.party_id = dap.party_id
        WHERE da.defendant_account_id = r_request.defendant_account_id
        LIMIT 1;

        --Lookup Allpay client code. Raise an exception if one is not found
        SELECT ci.item_value
        INTO v_allpay_client_code
          FROM configuration_items ci
         WHERE ci.item_name = c_configuration_name
           AND ci.business_unit_id = r_request.business_unit_id;

        IF v_allpay_client_code IS NULL THEN
            RAISE EXCEPTION 'Allpay client code not found for business unit id %', r_request.business_unit_id
                USING ERRCODE = 'P4001'
                    , DETAIL = 'p_int_payment_card_requests: Allpay client code for business unit is missing from configuration_items';
        END IF;

        --Derive Name on Card, limited to 27 characters and following rules to reduce it to 27 or less as per the functional spec
        v_title_initials := NULLIF(CONCAT_WS(' ', r_request.title, r_request.initials), '');
        v_name_on_card := CONCAT_WS(' ', v_title_initials, r_request.debtor_name);

        IF LENGTH(v_name_on_card) > 27 THEN
            v_name_on_card := CONCAT_WS(
                ' ',
                r_request.title,
                NULLIF(LEFT(r_request.initials, 1), ''),
                r_request.debtor_name
            );
        END IF;

        IF LENGTH(v_name_on_card) > 27 THEN
            v_name_on_card := CONCAT_WS(
                ' ',
                NULLIF(LEFT(r_request.initials, 1), ''),
                r_request.debtor_name
            );
        END IF;

        --Clean the four address lines and remove empty gaps so later lines move upward, as per rules in the functional spec
        v_address_lines := array_remove(
            ARRAY[
                NULLIF(TRIM(r_request.address_line_1), ''),
                NULLIF(TRIM(r_request.address_line_2), ''),
                NULLIF(TRIM(r_request.address_line_3), ''),
                NULLIF(TRIM(r_request.address_line_4), '')
            ],
            NULL
        );

        po_payment_card_requests_json := (
            po_payment_card_requests_json::JSONB
            || jsonb_build_array(
                jsonb_build_object(
                    'defendant_account_id', r_request.defendant_account_id,
                    'business_unit_id', r_request.business_unit_id,
                    'allpay_client_code', v_allpay_client_code,
                    'account_number', r_request.account_number,
                    'debtor_party_id', r_request.party_id,
                    'debtor_association_type', r_request.association_type,
                    'debtor_title_initials', v_title_initials,
                    'debtor_name', r_request.debtor_name,
                    'name_on_card', v_name_on_card,
                    'address_line_1', v_address_lines[1],
                    'address_line_2', v_address_lines[2],
                    'address_line_3', v_address_lines[3],
                    'address_line_4', v_address_lines[4],
                    'postcode', r_request.postcode
                )
            )
        )::JSON;

        v_pcr_defendant_account_ids := array_append(
            v_pcr_defendant_account_ids,
            r_request.defendant_account_id
        );
    END LOOP;

    IF cardinality(v_pcr_defendant_account_ids) = 0 THEN
        po_payment_card_requests_json := '{}'::JSON;
    END IF;

    --Create an interface_jobs record, reguardless of whether there are payment_card_requests to process
    po_interface_job_id := nextval('interface_job_id_seq');

    INSERT INTO interface_jobs (
        interface_job_id,
        business_unit_id,
        interface_name,
        status,
        created_datetime,
        started_datetime,
        completed_datetime
    )
    VALUES (
        po_interface_job_id,
        NULL,
        c_interface_name,
        'PROCESSING',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    );

    UPDATE payment_card_requests
       SET interface_job_id = po_interface_job_id
     WHERE defendant_account_id = ANY(v_pcr_defendant_account_ids)
       AND interface_job_id IS NULL;

EXCEPTION
    WHEN SQLSTATE 'P4001' THEN
        RAISE NOTICE 'Error in p_int_payment_card_requests: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_int_payment_card_requests: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_int_payment_card_requests: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$$;

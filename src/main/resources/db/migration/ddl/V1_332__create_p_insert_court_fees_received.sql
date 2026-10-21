/**
* OPAL Program
*
* MODULE      : create_p_insert_court_fees_received.sql
*
* DESCRIPTION : Create p_insert_court_fees_received for Till allocation court fee received records.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 15/09/2026    P Brumby    1.0         PO-3473 - Create p_insert_court_fees_received for Till allocation.
*
**/

--DROP PROCEDURE IF EXISTS p_insert_court_fees_received(in int2, in int8, in bool, in int2, out int8);

CREATE OR REPLACE PROCEDURE p_insert_court_fees_received(
    IN  pi_business_unit_id          court_fees_received.business_unit_id%TYPE,
    IN  pi_court_fee_id              court_fees_received.court_fee_id%TYPE,
    IN  pi_overpayment               court_fees_received.overpayment%TYPE,
    IN  pi_number_of_items           court_fees_received.number_of_items%TYPE,
    OUT po_court_fees_received_id    court_fees_received.court_fee_received_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_insert_court_fees_received.sql
*
* DESCRIPTION : Insert a court_fees_received record for a Court Fee payment processed during Till allocation.
*
* PARAMETERS  : pi_business_unit_id       - The business unit associated with the Court Fee Received record
*               pi_court_fee_id           - The court fee associated with the Court Fee Received record
*               pi_overpayment            - Indicates whether the Court Fee Received record is for an overpayment
*               pi_number_of_items        - Number of court fee items; set to 0 for an overpayment
*               po_court_fees_received_id - Returns the non-overpayment court_fees_received_id
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ----------------------------------------------------------------------------
* 15/09/2026    P Brumby    1.0         PO-3473 - Create p_insert_court_fees_received for Till allocation.
*
**/
DECLARE
    v_pg_exception_detail    TEXT;
BEGIN
    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Business unit ID cannot be null'
            USING ERRCODE = 'P3205',
                  DETAIL = 'p_insert_court_fees_received: pi_business_unit_id is required';
    END IF;

    IF pi_overpayment IS NULL THEN
        RAISE EXCEPTION 'Overpayment cannot be null'
            USING ERRCODE = 'P3206',
                  DETAIL = 'p_insert_court_fees_received: pi_overpayment is required';
    END IF;

    IF pi_number_of_items IS NULL AND NOT pi_overpayment THEN
        RAISE EXCEPTION 'Number of items cannot be null'
            USING ERRCODE = 'P3207',
                  DETAIL = 'p_insert_court_fees_received: pi_number_of_items is required';
    END IF;

    INSERT INTO court_fees_received (
          court_fee_received_id
        , business_unit_id
        , court_fee_id
        , overpayment
        , suspense_transaction_id
        , transferred_date
        , number_of_items
        --, received_date -- Not specified in PO-3473 or the Functional Description.
    )
    VALUES (
          nextval('court_fee_received_id_seq')
        , pi_business_unit_id
        , CASE
              WHEN pi_overpayment THEN NULL
              ELSE pi_court_fee_id
          END
        , pi_overpayment
        , NULL
        , NULL
        , CASE
              WHEN pi_overpayment THEN 0
              ELSE pi_number_of_items
          END
        --, CURRENT_TIMESTAMP -- Not specified in PO-3473 or the Functional Description.
    )
    RETURNING CASE
                  WHEN overpayment THEN NULL
                  ELSE court_fee_received_id
              END
    INTO po_court_fees_received_id;

EXCEPTION
    WHEN SQLSTATE 'P3205' OR SQLSTATE 'P3206' OR SQLSTATE 'P3207' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_insert_court_fees_received: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_insert_court_fees_received: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_insert_court_fees_received: % - %', SQLSTATE, SQLERRM
            USING DETAIL = v_pg_exception_detail;
END;
$procedure$;


/**
 * OPAL Program
 *
 * MODULE      : V1_1__create_p_reverse_suspense_function.sql
 *
 * DESCRIPTION : Reverse the balance of a suspense item by inserting
 *               an RV or DQ suspense transaction, creating an entry
 *               in the Reversed Payments report, and incrementing
 *               the suspense item version number using optimistic
 *               concurrency control.
 *
 * PARAMETERS  : pi_suspense_item_id       - Suspense item whose balance
 *                                          is being reversed
 *               pi_suspense_item_version  - Current version number of the
 *                                          suspense item for concurrency control
 *               pi_posted_by              - User identifier performing
 *                                          the reversal
 *               pi_posted_by_name         - Name of the user performing
 *                                          the reversal
 *               pi_is_dishonoured_cheque  - TRUE for a dishonoured cheque
 *                                          reversal (DQ), FALSE for a
 *                                          standard reversal (RV)
 *
 * VERSION HISTORY:
 *
 * Date        Author      Version  Nature of Change
 * 18/11/2026  I Chandio   1.0      PO-10350- Initial implementation -  Create p_reverse_suspense_function stored procedure
 **/

CREATE OR REPLACE PROCEDURE p_reverse_suspense_function(
    IN pi_suspense_item_id suspense_items.suspense_item_id%TYPE,
    IN pi_suspense_item_version suspense_items.version_number%TYPE,
    IN pi_posted_by suspense_transactions.posted_by%TYPE,
    IN pi_posted_by_name suspense_transactions.posted_by_name%TYPE,
    IN pi_is_dishonoured_cheque BOOLEAN
)
LANGUAGE plpgsql
AS $procedure$

DECLARE
    v_amount_balance suspense_transactions.amount%TYPE;
    v_business_unit_id suspense_accounts.business_unit_id%TYPE;
    v_suspense_transaction_id suspense_transactions.suspense_transaction_id%TYPE;
    v_report_entry_id report_entries.report_entry_id%TYPE;
    v_transaction_type suspense_transactions.transaction_type%TYPE;
    v_rows_updated INTEGER;
    v_pg_exception_detail TEXT;

BEGIN

    --------------------------------------------------------------------
    -- Step 1: Validate input parameters.
    --------------------------------------------------------------------

    IF pi_suspense_item_id IS NULL THEN
        RAISE EXCEPTION 'Suspense item ID cannot be null'
            USING ERRCODE = 'P4001',
                  DETAIL = 'p_reverse_suspense_function: pi_suspense_item_id is required';
    END IF;

    IF pi_suspense_item_version IS NULL THEN
        RAISE EXCEPTION 'Suspense item version cannot be null'
            USING ERRCODE = 'P4002',
                  DETAIL = 'p_reverse_suspense_function: pi_suspense_item_version is required';
    END IF;

    IF pi_posted_by IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null'
            USING ERRCODE = 'P4003',
                  DETAIL = 'p_reverse_suspense_function: pi_posted_by is required';
    END IF;

    IF pi_posted_by_name IS NULL THEN
        RAISE EXCEPTION 'Posted by name cannot be null'
            USING ERRCODE = 'P4004',
                  DETAIL = 'p_reverse_suspense_function: pi_posted_by_name is required';
    END IF;

    IF pi_is_dishonoured_cheque IS NULL THEN
        RAISE EXCEPTION 'Dishonoured cheque indicator cannot be null'
            USING ERRCODE = 'P4005',
                  DETAIL = 'p_reverse_suspense_function: pi_is_dishonoured_cheque is required';
    END IF;

    --------------------------------------------------------------------
    -- Step 2: Calculate and validate the suspense item balance.
    --------------------------------------------------------------------

    SELECT COALESCE(SUM(st.amount), 0)
      INTO v_amount_balance
      FROM suspense_transactions st
     WHERE st.suspense_item_id = pi_suspense_item_id;

    -- Only positive balances can be reversed.
    IF v_amount_balance <= 0 THEN
        RAISE EXCEPTION
            'Suspense item % must have a positive balance. Current balance: %',
            pi_suspense_item_id,
            v_amount_balance
            USING ERRCODE = 'P4006',
                  DETAIL = 'p_reverse_suspense_function: suspense item balance must be greater than zero';
    END IF;

    --------------------------------------------------------------------
    -- Step 3: Identify the business unit.
    --------------------------------------------------------------------

    SELECT sa.business_unit_id
      INTO STRICT v_business_unit_id
      FROM suspense_items si
      JOIN suspense_accounts sa
        ON sa.suspense_account_id = si.suspense_account_id
     WHERE si.suspense_item_id = pi_suspense_item_id;

    --------------------------------------------------------------------
    -- Step 4: Determine the reversal transaction type.
    --
    -- DQ = Dishonoured Cheque
    -- RV = Standard Reversal
    --------------------------------------------------------------------

    v_transaction_type :=
        CASE
            WHEN pi_is_dishonoured_cheque IS TRUE
                THEN 'DQ'::t_suspense_transaction_type_enum
            ELSE
                'RV'::t_suspense_transaction_type_enum
        END;

    --------------------------------------------------------------------
    -- Step 5: Insert the suspense reversal transaction.
    --------------------------------------------------------------------

    CALL p_insert_suspense_transactions_common(
        pi_suspense_item_id,
        clock_timestamp()::TIMESTAMP,
        pi_posted_by,
        pi_posted_by_name,
        v_transaction_type,
        -v_amount_balance,
        NULL::t_associated_record_type_enum,
        NULL::VARCHAR,
        NULL::VARCHAR,
        NULL::t_reversed_enum,
        v_suspense_transaction_id
    );

    --------------------------------------------------------------------
    -- Step 6: Create the Reversed Payments report entry.
    --------------------------------------------------------------------

    CALL p_insert_report_entries_common(
        v_business_unit_id,
        'reversed_payments_report'::VARCHAR,
        clock_timestamp()::TIMESTAMP,
        NULL::TIMESTAMP,
        'suspense_transactions'::t_associated_record_type_enum,
        v_suspense_transaction_id::VARCHAR,
        NULL::BIGINT,
        v_report_entry_id
    );

    --------------------------------------------------------------------
    -- Step 7: Optimistic concurrency control.
    --------------------------------------------------------------------

    UPDATE suspense_items
       SET version_number = version_number + 1
     WHERE suspense_item_id = pi_suspense_item_id
       AND version_number = pi_suspense_item_version;

    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;

    IF v_rows_updated = 0 THEN
        RAISE EXCEPTION
            'Some information on this page may be out of date.'
            USING ERRCODE = 'P0001',
                  DETAIL = 'p_reverse_suspense_function: suspense item version mismatch';
    END IF;

    --------------------------------------------------------------------
    -- Exception handling.
    --------------------------------------------------------------------

EXCEPTION
    WHEN SQLSTATE 'P4001'
      OR SQLSTATE 'P4002'
      OR SQLSTATE 'P4003'
      OR SQLSTATE 'P4004'
      OR SQLSTATE 'P4005'
      OR SQLSTATE 'P4006'
    THEN
        RAISE NOTICE
            'Error in p_reverse_suspense_function: % - %',
            SQLSTATE,
            SQLERRM;

        RAISE;

    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS
            v_pg_exception_detail = PG_EXCEPTION_DETAIL;

        RAISE NOTICE
            'Error in p_reverse_suspense_function: % - %',
            SQLSTATE,
            SQLERRM;

        RAISE NOTICE
            'Error details: %',
            v_pg_exception_detail;

        RAISE;

END;
$procedure$;

COMMENT ON PROCEDURE p_reverse_suspense_function(
    BIGINT,
    BIGINT,
    VARCHAR,
    VARCHAR,
    BOOLEAN
)
IS 'PO-10350: Reverse a positive suspense balance using RV/DQ, create a Reversed Payments report entry and enforce optimistic concurrency control.';

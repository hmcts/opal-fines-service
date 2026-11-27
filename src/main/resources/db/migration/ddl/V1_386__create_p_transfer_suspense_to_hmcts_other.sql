CREATE OR REPLACE PROCEDURE p_transfer_suspense_to_hmcts_other(
    IN pi_suspense_item_id      suspense_items.suspense_item_id%TYPE,
    IN pi_suspense_item_version suspense_items.version_number%TYPE,
    IN pi_posted_by             suspense_transactions.posted_by%TYPE,
    IN pi_posted_by_name        suspense_transactions.posted_by_name%TYPE
)
LANGUAGE plpgsql
AS $BODY$
/**
* OPAL Program
*
* MODULE      : create_p_transfer_suspense_to_hmcts_other.sql
*
* DESCRIPTION : Transfer a positive suspense balance to the HMCTS/Other
*               Central Fund account using common insert procedures.
*               Validate inputs and balance, create XP and XFER transactions,
*               then apply optimistic concurrency control to the suspense item.
*
* PARAMETERS  : pi_suspense_item_id      - Suspense item from which the balance
*                                         is being transferred
*               pi_suspense_item_version - Expected suspense item version for
*                                         optimistic concurrency control
*               pi_posted_by             - User identifier performing the transfer
*               pi_posted_by_name        - Name of the user performing the transfer
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    -------      --------    --------------------------------------------------------
* 18/11/2026    I Chandio    1.0         PO-10349 Create transfer orchestration procedure
*
**/

DECLARE
    v_amount_balance          suspense_transactions.amount%TYPE;
    v_creditor_account_id     creditor_accounts.creditor_account_id%TYPE;
    v_suspense_transaction_id suspense_transactions.suspense_transaction_id%TYPE;
    v_creditor_transaction_id creditor_transactions.creditor_transaction_id%TYPE;
    v_rows_updated            INTEGER;
    v_pg_exception_detail    TEXT;
BEGIN
    -- Pre-processing: Validate mandatory parameters supplied by the mid-tier.
    -- Centralise validation before performing any transfer operations.
    -- P4101-P4104 identify missing or blank input parameters.
    IF pi_suspense_item_id IS NULL THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P4101', MESSAGE = 'Suspense item ID is required.',
            DETAIL = 'p_transfer_suspense_to_hmcts_other: pi_suspense_item_id must not be NULL.';
    END IF;

    IF pi_suspense_item_version IS NULL THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P4102', MESSAGE = 'Suspense item version is required.',
            DETAIL = 'p_transfer_suspense_to_hmcts_other: pi_suspense_item_version must not be NULL.';
    END IF;

    IF NULLIF(BTRIM(pi_posted_by), '') IS NULL THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P4103', MESSAGE = 'Posted-by user is required.',
            DETAIL = 'p_transfer_suspense_to_hmcts_other: pi_posted_by must not be NULL or blank.';
    END IF;

    IF NULLIF(BTRIM(pi_posted_by_name), '') IS NULL THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P4104', MESSAGE = 'Posted-by name is required.',
            DETAIL = 'p_transfer_suspense_to_hmcts_other: pi_posted_by_name must not be NULL or blank.';
    END IF;

    -- Pre-processing: Verify that the suspense item exists.
    -- Reject an unknown suspense item before calculating the transfer amount.
    IF NOT EXISTS (
        SELECT 1 FROM suspense_items
        WHERE suspense_item_id = pi_suspense_item_id
    ) THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P4106', MESSAGE = 'Suspense item does not exist.',
            DETAIL = FORMAT(
                'p_transfer_suspense_to_hmcts_other: suspense_item_id=%s was not found.',
                pi_suspense_item_id
            );
    END IF;

    -- =====================================================================
    -- Action 1: Calculate the amount of money being transferred.
    -- Sum suspense_transactions.amount for the supplied suspense item ID.
    -- Treat a suspense item with no transactions as a zero balance.
    -- The resulting v_amount_balance is the amount to transfer.
    -- Reject balances less than or equal to zero and abort the transfer.
    -- =====================================================================
    SELECT COALESCE(SUM(st.amount), 0)
      INTO v_amount_balance
      FROM suspense_transactions st
     WHERE st.suspense_item_id = pi_suspense_item_id;

    IF v_amount_balance <= 0 THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P4105', MESSAGE = 'Suspense balance must be greater than zero.',
            DETAIL = FORMAT(
                'p_transfer_suspense_to_hmcts_other: suspense_item_id=%s, balance=%s - zero and negative balances cannot be transferred.',
                pi_suspense_item_id, v_amount_balance
            );
    END IF;

    -- =====================================================================
    -- Action 2: Create a Suspense Transaction.
    -- Call p_insert_suspense_transactions_common with transaction type XP.
    -- Record the negative of v_amount_balance to remove that amount from
    -- suspense, using the supplied posting user details and current time.
    -- Associated record fields, text and reversed are NULL per the ticket.
    -- Capture the generated suspense transaction ID for Action 3.
    -- =====================================================================
    CALL p_insert_suspense_transactions_common(
        pi_suspense_item_id => pi_suspense_item_id,
        pi_posted_date => clock_timestamp()::TIMESTAMP,
        posted_by => pi_posted_by,
        posted_by_name => pi_posted_by_name,
        pi_transaction_type => 'XP'::t_suspense_transaction_type_enum,
        pi_amount => -v_amount_balance,
        pi_associated_record_type => NULL,
        pi_associated_record_id => NULL,
        pi_text => NULL,
        pi_reversed => NULL,
        po_suspense_transaction_id => v_suspense_transaction_id
    );

    -- =====================================================================
    -- Action 3a: Identify the HMCTS/Other Central Fund creditor account.
    -- suspense_items.suspense_account_id -> suspense_accounts.business_unit_id
    -- -> creditor_accounts.creditor_account_id, where account type is CF.
    -- The account ID is required for the XFER creditor transaction.
    -- =====================================================================
    SELECT ca.creditor_account_id
      INTO STRICT v_creditor_account_id
      FROM suspense_items si
      JOIN suspense_accounts sa
        ON sa.suspense_account_id = si.suspense_account_id
      JOIN creditor_accounts ca
        ON ca.business_unit_id = sa.business_unit_id
     WHERE si.suspense_item_id = pi_suspense_item_id
       AND ca.creditor_account_type = 'CF';

    -- =====================================================================
    -- Action 3b: Create a Creditor Transaction record.
    -- Call p_insert_creditor_transactions_common with transaction type XFER.
    -- Credit the full v_amount_balance to the CF creditor account; pass the
    -- posting details, payment_processed = FALSE and specified NULL fields.
    -- The corrected Jira ticket requires associated_record_type =
    -- suspense_transactions and associated_record_id = the generated XP
    -- suspense_transaction_id returned by the Action 2 common procedure.
    -- =====================================================================
    CALL p_insert_creditor_transactions_common(
        pi_creditor_account_id => v_creditor_account_id,
        pi_posted_date => clock_timestamp()::TIMESTAMP,
        pi_posted_by => pi_posted_by,
        pi_posted_by_name => pi_posted_by_name,
        pi_transaction_type => 'XFER'::t_creditor_transaction_type_enum,
        pi_transaction_amount => v_amount_balance,
        pi_imposition_result_id => NULL,
        pi_payment_processed => FALSE,
        pi_payment_reference => NULL,
        pi_status => NULL,
        pi_status_date => NULL,
        pi_associated_record_type => 'suspense_transactions'::t_associated_record_type_enum,
        pi_associated_record_id => v_suspense_transaction_id::VARCHAR,
        po_creditor_transaction_id => v_creditor_transaction_id
    );

    -- =====================================================================
    -- Action 4: Perform optimistic concurrency control on the suspense item.
    -- After creating both transactions, increment version_number only if it
    -- matches pi_suspense_item_version supplied by the mid-tier.
    -- If no row is updated, raise the prescribed out-of-date message.
    -- Raising an exception aborts the entire transfer, including XP and XFER.
    -- Note: the ticket says "defendant account" but the update is to
    -- suspense_items, so the check is on the suspense item.
    -- =====================================================================
    UPDATE suspense_items
       SET version_number = version_number + 1
     WHERE suspense_item_id = pi_suspense_item_id
       AND version_number = pi_suspense_item_version;

    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;

    IF v_rows_updated = 0 THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P0001',
            MESSAGE = 'Some information on this page may be out of date.',
            DETAIL = FORMAT(
                'p_transfer_suspense_to_hmcts_other: suspense_item_id=%s, expected_version=%s - version update affected no rows.',
                pi_suspense_item_id, pi_suspense_item_version
            );
    END IF;

EXCEPTION
    -- Pass known validation failures back to the mid-tier unchanged.
    WHEN SQLSTATE 'P4101'
      OR SQLSTATE 'P4102'
      OR SQLSTATE 'P4103'
      OR SQLSTATE 'P4104'
      OR SQLSTATE 'P4105'
      OR SQLSTATE 'P4106'
    THEN
        RAISE NOTICE
            'Error in p_transfer_suspense_to_hmcts_other: % - %',
            SQLSTATE,
            SQLERRM;
        RAISE;

    -- Log diagnostic information for all other failures, then re-raise.
    -- This includes concurrency errors and missing creditor accounts.
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS
            v_pg_exception_detail = PG_EXCEPTION_DETAIL;

        RAISE NOTICE
            'Error in p_transfer_suspense_to_hmcts_other: % - %',
            SQLSTATE,
            SQLERRM;

        RAISE NOTICE
            'Error details: %',
            v_pg_exception_detail;

        RAISE;
END;
$BODY$;

COMMENT ON PROCEDURE p_transfer_suspense_to_hmcts_other(
    BIGINT, BIGINT, VARCHAR, VARCHAR
) IS 'PO-10349: Orchestrates a positive suspense balance transfer to HMCTS/Other with XP and XFER transactions and optimistic concurrency control.';

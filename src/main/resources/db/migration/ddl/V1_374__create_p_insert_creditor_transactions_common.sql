
/**
 * OPAL Program
 *
 * MODULE      : V1_1__create_p_insert_creditor_transactions_common.sql
 *
 * DESCRIPTION : Creates the common procedure for inserting
 *               records into CREDITOR_TRANSACTIONS.
 *
 * VERSION HISTORY:
 *
 * Date         Author       Version   Nature of Change
 * -----------  -----------  --------  --------------------------------
 * 03/11/2026   I Chandio    0.1       Initial creation Create p_insert_creditor_transactions_common to insert data into the creditor_transactions table
 * 
 */

CREATE OR REPLACE PROCEDURE public.p_insert_creditor_transactions_common(
    IN pi_creditor_account_id public.creditor_transactions.creditor_account_id%TYPE,
    IN pi_posted_date public.creditor_transactions.posted_date%TYPE,
    IN pi_posted_by public.creditor_transactions.posted_by%TYPE,
    IN pi_posted_by_name public.creditor_transactions.posted_by_name%TYPE,
    IN pi_transaction_type public.creditor_transactions.transaction_type%TYPE,
    IN pi_transaction_amount public.creditor_transactions.transaction_amount%TYPE,
    IN pi_imposition_result_id public.creditor_transactions.imposition_result_id%TYPE,
    IN pi_payment_processed public.creditor_transactions.payment_processed%TYPE,
    IN pi_payment_reference public.creditor_transactions.payment_reference%TYPE,
    IN pi_status public.creditor_transactions.status%TYPE,
    IN pi_status_date public.creditor_transactions.status_date%TYPE,
    IN pi_associated_record_type public.creditor_transactions.associated_record_type%TYPE,
    IN pi_associated_record_id public.creditor_transactions.associated_record_id%TYPE,
    OUT po_creditor_transaction_id public.creditor_transactions.creditor_transaction_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
DECLARE
    v_pg_exception_detail TEXT;

BEGIN

    INSERT INTO public.creditor_transactions (
        creditor_transaction_id,
        creditor_account_id,
        posted_date,
        posted_by,
        posted_by_name,
        transaction_type,
        transaction_amount,
        imposition_result_id,
        payment_processed,
        payment_reference,
        status,
        status_date,
        associated_record_type,
        associated_record_id
    )
    VALUES (
        nextval('public.creditor_transaction_id_seq'),
        pi_creditor_account_id,
        pi_posted_date,
        pi_posted_by,
        pi_posted_by_name,
        pi_transaction_type,
        pi_transaction_amount,
        pi_imposition_result_id,
        pi_payment_processed,
        pi_payment_reference,
        pi_status,
        pi_status_date,
        pi_associated_record_type,
        pi_associated_record_id
    )
    RETURNING creditor_transaction_id
    INTO po_creditor_transaction_id;

EXCEPTION

    WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS
            v_pg_exception_detail = PG_EXCEPTION_DETAIL;

        RAISE NOTICE
            'Error in p_insert_creditor_transactions_common: % - %',
            SQLSTATE, SQLERRM;

        RAISE NOTICE
            'Error details: %',
            v_pg_exception_detail;

        RAISE;

END;
$procedure$;


COMMENT ON PROCEDURE public.p_insert_creditor_transactions_common
    IS 'Insert a creditor transaction into CREDITOR_TRANSACTIONS and return the generated creditor_transaction_id.';

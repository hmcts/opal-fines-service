
/**
 * OPAL Program
 *
 * MODULE      : V1_1__create_p_insert_suspense_transactions_common.sql
 *
 * DESCRIPTION : Create the common stored procedure to insert
 *               records into the SUSPENSE_TRANSACTIONS table.
 *
 * VERSION HISTORY:
 *
 * Date         Author       Version   Nature of Change
 * -----------  -----------  --------  --------------------------
 * 21/10/2026   I Chandio     0.1       PO-10346 DB - Create p_insert_suspense_transactions_common to insert data into the suspense_transactions table
 */

CREATE OR REPLACE PROCEDURE public.p_insert_suspense_transactions_common(
    IN pi_suspense_item_id public.suspense_transactions.suspense_item_id%TYPE,
    IN pi_posted_date public.suspense_transactions.posted_date%TYPE,
    IN posted_by public.suspense_transactions.posted_by%TYPE,
    IN posted_by_name public.suspense_transactions.posted_by_name%TYPE,
    IN pi_transaction_type public.suspense_transactions.transaction_type%TYPE,
    IN pi_amount public.suspense_transactions.amount%TYPE,
    IN pi_associated_record_type public.suspense_transactions.associated_record_type%TYPE,
    IN pi_associated_record_id public.suspense_transactions.associated_record_id%TYPE,
    IN pi_text public.suspense_transactions.text%TYPE,
    IN pi_reversed public.suspense_transactions.reversed%TYPE,
    OUT po_suspense_transaction_id public.suspense_transactions.suspense_transaction_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
DECLARE
    v_pg_exception_detail TEXT;

BEGIN

    INSERT INTO public.suspense_transactions (
        suspense_transaction_id,
        suspense_item_id,
        posted_date,
        posted_by,
        posted_by_name,
        transaction_type,
        amount,
        associated_record_type,
        associated_record_id,
        text,
        reversed
    )
    VALUES (
        nextval('public.suspense_transaction_id_seq'),
        pi_suspense_item_id,
        pi_posted_date,
        posted_by,
        posted_by_name,
        pi_transaction_type,
        pi_amount,
        pi_associated_record_type,
        pi_associated_record_id,
        pi_text,
        pi_reversed
    )
    RETURNING suspense_transaction_id
    INTO po_suspense_transaction_id;

EXCEPTION

    WHEN SQLSTATE 'P3901'
      OR SQLSTATE 'P3902'
      OR SQLSTATE 'P3903'
      OR SQLSTATE 'P3904'
      OR SQLSTATE 'P3905'
      OR SQLSTATE 'P3906'
      OR SQLSTATE 'P3907'
    THEN
        RAISE NOTICE
            'Error in p_insert_suspense_transactions_common: % - %',
            SQLSTATE, SQLERRM;
        RAISE;

    WHEN OTHERS THEN

        GET STACKED DIAGNOSTICS
            v_pg_exception_detail = PG_EXCEPTION_DETAIL;

        RAISE NOTICE
            'Error in p_insert_suspense_transactions_common: % - %',
            SQLSTATE, SQLERRM;

        RAISE NOTICE
            'Error details: %',
            v_pg_exception_detail;

        RAISE;

END;
$procedure$;


COMMENT ON PROCEDURE public.p_insert_suspense_transactions_common
    IS 'Insert a suspense transaction into SUSPENSE_TRANSACTIONS and return the generated suspense_transaction_id.';

DROP PROCEDURE IF EXISTS p_insert_suspense_item;

CREATE PROCEDURE p_insert_suspense_item(
    IN  pi_suspense_item_type        suspense_items.suspense_item_type%TYPE,
    IN  pi_payment_method            suspense_items.payment_method%TYPE,
    IN  pi_business_unit_id          business_units.business_unit_id%TYPE,
    IN  pi_court_fee_received_id     court_fees_received.court_fee_received_id%TYPE,
    OUT po_suspense_item_id          suspense_items.suspense_item_id%TYPE
)
LANGUAGE plpgsql
AS $$
/**
* OPAL Program
*
* MODULE      : create_p_insert_suspense_item.sql
*
* DESCRIPTION : Create suspense_items records during till allocation.
*
* PARAMETERS  : pi_suspense_item_type    - Suspense item type code
*               pi_payment_method        - Payment method from the related payment in record
*               pi_business_unit_id      - Business unit used to find the suspense account and allocate the item number
*               pi_court_fee_received_id - Court fee received record linked to the suspense item, when applicable
*               po_suspense_item_id      - Created suspense item identifier
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/09/2026    C Cho         1.0         PO-3477 - Create p_insert_suspense_item
*
**/
DECLARE
    c_suspense_item_number_start CONSTANT suspense_item_number_index.suspense_item_number%TYPE := 100000;
    c_allowed_retries            CONSTANT INTEGER := 5;
    v_retries                             INTEGER := 0;
    v_suspense_account_id                 suspense_accounts.suspense_account_id%TYPE;
    v_suspense_item_number                suspense_item_number_index.suspense_item_number%TYPE;
    v_court_fee_id                        court_fees_received.court_fee_id%TYPE;
    v_error_msg                           VARCHAR;
BEGIN
    IF pi_suspense_item_type IS NULL THEN
        RAISE EXCEPTION 'pi_suspense_item_type must be provided'
            USING ERRCODE = 'P3301'
                , DETAIL = 'p_insert_suspense_item: pi_suspense_item_type is required';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'pi_business_unit_id must be provided'
            USING ERRCODE = 'P3302'
                , DETAIL = 'p_insert_suspense_item: pi_business_unit_id is required';
    END IF;

    SELECT suspense_account_id
      INTO v_suspense_account_id
      FROM suspense_accounts
     WHERE business_unit_id = pi_business_unit_id
     ORDER BY suspense_account_id
     LIMIT 1;

    IF v_suspense_account_id IS NULL THEN
        RAISE EXCEPTION 'No suspense account found for business_unit_id %', pi_business_unit_id
            USING ERRCODE = 'P3303'
                , DETAIL = 'p_insert_suspense_item: No suspense account exists for business_unit_id '
                    || pi_business_unit_id;
    END IF;

    IF pi_court_fee_received_id IS NOT NULL THEN
        SELECT court_fee_id
          INTO v_court_fee_id
          FROM court_fees_received
         WHERE court_fee_received_id = pi_court_fee_received_id;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'court_fees_received record % was not found', pi_court_fee_received_id
                USING ERRCODE = 'P3304'
                    , DETAIL = 'p_insert_suspense_item: No court_fees_received record exists for court_fee_received_id '
                        || pi_court_fee_received_id;
        END IF;
    END IF;

    LOOP
        SELECT COALESCE(MAX(suspense_item_number), c_suspense_item_number_start - 1) + 1
          INTO v_suspense_item_number
          FROM suspense_item_number_index
         WHERE business_unit_id = pi_business_unit_id;

        BEGIN
            INSERT INTO suspense_item_number_index (
                business_unit_id,
                suspense_item_number
            )
            VALUES (
                pi_business_unit_id,
                v_suspense_item_number
            );

            EXIT;
        EXCEPTION
            WHEN UNIQUE_VIOLATION THEN
                v_error_msg := format(
                    'Unique violation inserting suspense_item_number = %s, business_unit_id = %s. Error: %s - %s',
                    v_suspense_item_number,
                    pi_business_unit_id,
                    SQLSTATE,
                    SQLERRM
                );
                RAISE WARNING '%', v_error_msg;

                v_retries := v_retries + 1;

                IF v_retries >= c_allowed_retries THEN
                    RAISE EXCEPTION '%', v_error_msg
                        USING ERRCODE = 'P3305'
                            , DETAIL = 'p_insert_suspense_item: Failed to allocate a unique suspense item number after '
                                || c_allowed_retries || ' attempts';
                END IF;
        END;
    END LOOP;

    INSERT INTO suspense_items (
        suspense_item_id,
        suspense_account_id,
        suspense_item_number,
        suspense_item_type,
        created_date,
        payment_method,
        court_fee_id
    )
    VALUES (
        nextval('suspense_item_id_seq'),
        v_suspense_account_id,
        v_suspense_item_number,
        pi_suspense_item_type,
        CURRENT_TIMESTAMP,
        pi_payment_method,
        v_court_fee_id
    )
    RETURNING suspense_item_id
         INTO po_suspense_item_id;
END;
$$;

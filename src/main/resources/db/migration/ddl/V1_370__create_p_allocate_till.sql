CREATE OR REPLACE PROCEDURE p_allocate_till(
    IN pi_till_id                   tills.till_id%TYPE,
    IN pi_posted_by                 defendant_transactions.posted_by%TYPE,
    IN pi_posted_by_name            defendant_transactions.posted_by_name%TYPE,
    IN pi_business_unit_id          business_units.business_unit_id%TYPE
)
LANGUAGE plpgsql
AS $procedure$
/**
* OPAL Program
*
* MODULE      : create_p_allocate_till.sql
*
* DESCRIPTION : Allocate the unallocated payments in a till to defendant accounts,
*               court fees and suspense.
*
* PARAMETERS  : pi_till_id          - Till to allocate
*               pi_posted_by        - User identifier posting the allocation
*               pi_posted_by_name   - Name of the user posting the allocation
*               pi_business_unit_id - Business unit that must own the till
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 30/10/2026    C Cho       1.0         PO-3417 - Create p_allocate_till
*
**/
DECLARE
    r_till                       RECORD;
    r_payment                    payments_in%ROWTYPE;
    r_account                    defendant_accounts%ROWTYPE;
    r_court_fee                  court_fees%ROWTYPE;
    v_allocation_function        allocations.allocation_function%TYPE;
    v_additional_information     JSONB;
    v_payment_information        JSONB;
    v_payment_reference          defendant_transactions.payment_reference%TYPE;
    v_source                     defendant_transactions.source%TYPE;
    v_defendant_account_id       defendant_accounts.defendant_account_id%TYPE;
    v_defendant_transaction_id   defendant_transactions.defendant_transaction_id%TYPE;
    v_inhibited                  BOOLEAN;
    v_payment_amount             payments_in.payment_amount%TYPE;
    v_imposition_balance         payments_in.payment_amount%TYPE;
    v_suspense_amount            suspense_transactions.amount%TYPE;
    v_suspense_destination       payments_in.destination_type%TYPE;
    v_suspense_item_type         suspense_items.suspense_item_type%TYPE;
    v_court_fee_id               court_fees.court_fee_id%TYPE;
    v_court_fee_received_id      court_fees_received.court_fee_received_id%TYPE;
    v_number_of_items            court_fees_received.number_of_items%TYPE;
    v_fee_amount                 payments_in.payment_amount%TYPE;
    v_allocated_amount           allocations.allocated_amount%TYPE;
BEGIN
    IF pi_till_id IS NULL THEN
        RAISE EXCEPTION 'Till ID cannot be null'
            USING ERRCODE = 'P4101', DETAIL = 'p_allocate_till: pi_till_id is required';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Business unit ID cannot be null'
            USING ERRCODE = 'P4102', DETAIL = 'p_allocate_till: pi_business_unit_id is required';
    END IF;

    IF NULLIF(BTRIM(pi_posted_by), '') IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null or blank'
            USING ERRCODE = 'P4103', DETAIL = 'p_allocate_till: pi_posted_by is required';
    END IF;

    IF NULLIF(BTRIM(pi_posted_by_name), '') IS NULL THEN
        RAISE EXCEPTION 'Posted by name cannot be null or blank'
            USING ERRCODE = 'P4104', DETAIL = 'p_allocate_till: pi_posted_by_name is required';
    END IF;

    SELECT t.business_unit_id, t.auto_payment, t.source, t.status,
           COALESCE(f.override_inhibits, FALSE) AS override_inhibits
      INTO r_till
      FROM tills t
      LEFT JOIN interface_files f ON f.interface_file_id = t.interface_file_id AND t.auto_payment
     WHERE t.till_id = pi_till_id
       FOR UPDATE OF t;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Till % was not found', pi_till_id
            USING ERRCODE = 'P4105', DETAIL = 'p_allocate_till: no till exists for pi_till_id';
    END IF;

    IF r_till.business_unit_id IS DISTINCT FROM pi_business_unit_id THEN
        RAISE EXCEPTION 'Till % belongs to a different business unit', pi_till_id
            USING ERRCODE = 'P4106',
                  DETAIL = FORMAT('p_allocate_till: till business unit %s; supplied business unit %s',
                                  r_till.business_unit_id, pi_business_unit_id);
    END IF;

    IF r_till.status = 'Allocated' THEN
        IF EXISTS (SELECT 1 FROM payments_in WHERE till_id = pi_till_id AND NOT allocated) THEN
            RAISE EXCEPTION 'Allocated till % contains unallocated payments', pi_till_id
                USING ERRCODE = 'P4107', DETAIL = 'p_allocate_till: inconsistent allocated till state';
        END IF;
        RETURN;
    END IF;

    v_allocation_function := CASE WHEN r_till.auto_payment THEN 'Automatic Cash Input' ELSE 'Manual Cash Input' END;

    FOR r_payment IN
        SELECT pi.*
          FROM payments_in pi
         WHERE pi.till_id = pi_till_id AND NOT pi.allocated
         ORDER BY pi.payment_in_id
           FOR UPDATE
    LOOP
        IF r_payment.payment_amount <= 0 THEN
            RAISE EXCEPTION 'Payment % must have a positive amount', r_payment.payment_in_id
                USING ERRCODE = 'P4108', DETAIL = 'p_allocate_till: payment_amount must be greater than zero';
        END IF;

        IF COALESCE(r_payment.auto_payment, FALSE) IS DISTINCT FROM r_till.auto_payment THEN
            RAISE EXCEPTION 'Payment % has a different allocation mode from its till', r_payment.payment_in_id
                USING ERRCODE = 'P4109', DETAIL = 'p_allocate_till: payment and till auto_payment flags must agree';
        END IF;

        BEGIN
            v_additional_information := NULLIF(r_payment.additional_information::TEXT, '')::JSONB;
        EXCEPTION
            WHEN invalid_text_representation THEN
                v_additional_information := NULL;
        END;
        v_payment_information := COALESCE(
            v_additional_information -> 'additional_information', v_additional_information
        );
        v_payment_reference := COALESCE(
            NULLIF(v_payment_information ->> 'payment_reference', ''),
            CASE WHEN r_till.auto_payment THEN COALESCE(
                NULLIF(v_payment_information ->> 'originator_reference', ''),
                NULLIF(v_additional_information ->> 'originator_reference', ''),
                CASE
                    WHEN JSONB_TYPEOF(v_additional_information) = 'string' THEN v_additional_information #>> '{}'
                    WHEN v_additional_information IS NULL THEN NULLIF(r_payment.additional_information::TEXT, '')
                END
            ) END
        );
        v_source := CASE WHEN r_till.auto_payment THEN r_till.source::TEXT ELSE COALESCE(
            NULLIF(v_payment_information ->> 'transfer_source', ''),
            NULLIF(v_payment_information ->> 'source', '')
        ) END;
        v_payment_amount := 0;
        v_suspense_amount := 0;
        v_suspense_destination := r_payment.destination_type;
        v_suspense_item_type := NULL;
        v_court_fee_received_id := NULL;

        CASE r_payment.destination_type
            WHEN 'C' THEN
                v_court_fee_id := NULLIF(v_payment_information ->> 'court_fee_id', '')::BIGINT;
                v_number_of_items := COALESCE(NULLIF(v_payment_information ->> 'number_of_items', '')::SMALLINT, 1);

                IF v_number_of_items <= 0 THEN
                    RAISE EXCEPTION 'Payment % must specify a positive number of court fee items', r_payment.payment_in_id
                        USING ERRCODE = 'P4110', DETAIL = 'p_allocate_till: number_of_items must be greater than zero';
                END IF;

                SELECT cf.*
                  INTO STRICT r_court_fee
                  FROM court_fees cf
                 WHERE cf.business_unit_id = pi_business_unit_id
                   AND ((v_court_fee_id IS NOT NULL AND cf.court_fee_id = v_court_fee_id)
                        OR (v_court_fee_id IS NULL AND cf.court_fee_code = v_payment_information ->> 'court_fee_code'))
                   FOR SHARE;

                IF r_court_fee.amount <= 0 THEN
                    RAISE EXCEPTION 'Court fee % must have a positive amount', r_court_fee.court_fee_id
                        USING ERRCODE = 'P4111', DETAIL = 'p_allocate_till: court_fees.amount must be greater than zero';
                END IF;

                v_fee_amount := LEAST(r_payment.payment_amount, r_court_fee.amount * v_number_of_items);
                CALL p_insert_court_fees_received(
                    pi_business_unit_id := pi_business_unit_id,
                    pi_court_fee_id := r_court_fee.court_fee_id,
                    pi_overpayment := FALSE,
                    pi_number_of_items := v_number_of_items,
                    po_court_fees_received_id := v_court_fee_received_id
                );
                CALL p_allocate_suspense_payment(
                    pi_posted_by := pi_posted_by,
                    pi_posted_by_name := pi_posted_by_name,
                    pi_business_unit_id := pi_business_unit_id,
                    pi_suspense_amount := v_fee_amount,
                    pi_destination_type := r_payment.destination_type,
                    pi_suspense_item_type := NULL,
                    pi_payment_method := r_payment.payment_method,
                    pi_payment_in_id := r_payment.payment_in_id,
                    pi_additional_information := r_payment.additional_information,
                    pi_court_fee_received_id := v_court_fee_received_id
                );

                v_suspense_amount := r_payment.payment_amount - v_fee_amount;
                IF v_suspense_amount > 0 THEN
                    CALL p_insert_court_fees_received(
                        pi_business_unit_id := pi_business_unit_id,
                        pi_court_fee_id := NULL,
                        pi_overpayment := TRUE,
                        pi_number_of_items := 0::SMALLINT,
                        po_court_fees_received_id := v_court_fee_received_id
                    );
                    -- The existing child returns NULL for excess rows. CURRVAL is session-local
                    -- and identifies the row inserted by that call, without a MAX(id) race.
                    IF v_court_fee_received_id IS NULL THEN
                        v_court_fee_received_id := CURRVAL('court_fee_received_id_seq');
                    END IF;
                END IF;

            WHEN 'S' THEN
                v_suspense_amount := r_payment.payment_amount;
                v_suspense_item_type := COALESCE(NULLIF(r_payment.allocation_type, '')::t_suspense_item_type_enum, 'UN');

            WHEN 'F' THEN
                v_defendant_account_id := NULL;
                IF r_payment.associated_record_type::TEXT = 'defendant_accounts' THEN
                    v_defendant_account_id := f_get_master_account_id(NULLIF(r_payment.associated_record_id, '')::BIGINT);
                END IF;

                SELECT da.* INTO r_account
                  FROM defendant_accounts da
                 WHERE da.defendant_account_id = v_defendant_account_id
                   FOR UPDATE;

                IF FOUND AND r_account.business_unit_id <> pi_business_unit_id THEN
                    RAISE EXCEPTION 'Defendant account % belongs to a different business unit', v_defendant_account_id
                        USING ERRCODE = 'P4112', DETAIL = 'p_allocate_till: the master account must belong to the till business unit';
                END IF;

                -- Check the defendant account against the BU's configured inhibit results.
                SELECT EXISTS (
                    SELECT 1
                      FROM LATERAL (
                          SELECT ci.item_values FROM configuration_items ci
                           WHERE ci.business_unit_id = r_account.business_unit_id
                             AND ci.item_name = 'Inhibit payment results'
                           ORDER BY ci.configuration_item_id LIMIT 1
                      ) inhibit_ci
                      CROSS JOIN LATERAL json_array_elements_text(COALESCE(inhibit_ci.item_values, '[]'::JSON)) res(value)
                     WHERE res.value = r_account.last_enforcement
                ) AND NOT r_till.override_inhibits INTO v_inhibited;

                IF r_account.defendant_account_id IS NULL
                   OR r_account.account_status IN ('WO', 'TO', 'TA', 'TS', 'CS')
                   OR r_account.account_balance = 0 OR v_inhibited THEN
                    v_suspense_amount := r_payment.payment_amount;
                    -- Rejected payments are unidentified suspense.
                    v_suspense_destination := 'S';
                    v_suspense_item_type := 'UN';
                ELSE
                    IF r_account.account_balance > 0 THEN
                        RAISE EXCEPTION 'Defendant account % has an invalid positive account balance', v_defendant_account_id
                            USING ERRCODE = 'P4113', DETAIL = 'p_allocate_till: outstanding account balances must be negative';
                    END IF;

                    PERFORM i.imposition_id FROM impositions i
                     WHERE i.defendant_account_id = v_defendant_account_id
                     ORDER BY i.imposition_id FOR UPDATE;
                    SELECT COALESCE(SUM(-(i.imposed_amount + i.paid_amount)), 0)
                      INTO v_imposition_balance
                      FROM impositions i JOIN results r ON r.result_id = i.result_id
                     WHERE i.defendant_account_id = v_defendant_account_id
                       AND r.imposition_allocation_priority BETWEEN 1 AND 9
                       AND i.imposed_amount + i.paid_amount < 0;

                    v_payment_amount := LEAST(r_payment.payment_amount, -r_account.account_balance, v_imposition_balance);
                    v_suspense_amount := r_payment.payment_amount - v_payment_amount;
                    v_suspense_item_type := 'OF';

                    IF v_payment_amount > 0 THEN
                        CALL p_insert_defendant_transactions(
                            pi_defendant_account_id := v_defendant_account_id,
                            pi_posted_by := pi_posted_by,
                            pi_posted_by_name := pi_posted_by_name,
                            pi_transaction_type := 'PAYMNT'::t_defendant_transaction_type_enum,
                            pi_payment_amount := v_payment_amount,
                            pi_payment_method := r_payment.payment_method,
                            pi_payment_reference := v_payment_reference,
                            pi_third_party_payer_name := r_payment.third_party_payer_name,
                            pi_associated_record_id := r_payment.payment_in_id::TEXT,
                            pi_source := v_source,
                            po_defendant_transaction_id := v_defendant_transaction_id
                        );
                        
                        UPDATE defendant_transactions SET status = 'C'
                         WHERE defendant_transaction_id = v_defendant_transaction_id;
                        CALL p_update_defendant_accounts(v_defendant_account_id, v_payment_amount);
                        CALL p_allocate_payments(
                            pi_defendant_account_id := v_defendant_account_id,
                            pi_posted_by := pi_posted_by,
                            pi_posted_by_name := pi_posted_by_name,
                            pi_payment_amount := v_payment_amount,
                            pi_transaction_type := 'PAYMNT'::t_allocation_transaction_type_enum,
                            pi_allocation_function := v_allocation_function,
                            pi_payment_reference := v_payment_reference,
                            pi_defendant_transaction_id := v_defendant_transaction_id
                        );

                        SELECT COALESCE(SUM(allocated_amount), 0) INTO v_allocated_amount
                          FROM allocations WHERE defendant_transaction_id = v_defendant_transaction_id;
                        IF v_allocated_amount <> v_payment_amount THEN
                            RAISE EXCEPTION 'Payment % was not fully allocated to impositions', r_payment.payment_in_id
                                USING ERRCODE = 'P4114', DETAIL = 'p_allocate_till: imposition allocations must equal the defendant transaction amount';
                        END IF;
                    END IF;
                END IF;

            ELSE
                RAISE EXCEPTION 'Unsupported payment destination for payment %', r_payment.payment_in_id
                    USING ERRCODE = 'P4115', DETAIL = 'p_allocate_till: destination_type must be C, S or F';
        END CASE;

        IF v_suspense_amount > 0 THEN
            CALL p_allocate_suspense_payment(
                pi_posted_by := pi_posted_by,
                pi_posted_by_name := pi_posted_by_name,
                pi_business_unit_id := pi_business_unit_id,
                pi_suspense_amount := v_suspense_amount,
                pi_destination_type := v_suspense_destination,
                pi_suspense_item_type := v_suspense_item_type,
                pi_payment_method := r_payment.payment_method,
                pi_payment_in_id := r_payment.payment_in_id,
                pi_additional_information := r_payment.additional_information,
                pi_court_fee_received_id := v_court_fee_received_id
            );
        END IF;

        UPDATE payments_in SET allocated = TRUE WHERE payment_in_id = r_payment.payment_in_id;
    END LOOP;

    UPDATE tills SET status = 'Allocated' WHERE till_id = pi_till_id;
END;
$procedure$;

COMMENT ON PROCEDURE p_allocate_till
    IS 'PO-3417: Allocate a till to defendant accounts, court fees and suspense.';

package uk.gov.hmcts.opal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.entity.projection.SuspenseTransactionHistoryProjection;

@Repository
public interface SuspenseTransactionRepository extends JpaRepository<SuspenseTransactionEntity, Long> {

    @Query(value = """
        SELECT st.suspense_transaction_id AS transaction_id,
               st.transaction_type::text AS transaction_type,
               st.amount,
               st.posted_date,
               st.posted_by,
               st.posted_by_name,
               st.associated_record_type::text AS associated_record_type,
               st.associated_record_id,
               st.text,
               st.reversed::text AS reversed,
               si.suspense_item_type::text AS suspense_item_type,
               p.payment_in_id,
               p.auto_payment,
               p.payment_method::text AS payment_method,
               COALESCE(p.additional_information::jsonb ->> 'payment_received_from',
                        info.value ->> 'payment_received_from') AS payment_received_from,
               info.value ->> 'payment_reference' AS payment_reference,
               info.value ->> 'originator_reference' AS originator_reference,
               CASE WHEN p.auto_payment THEN t.source::text
                   ELSE COALESCE(info.value ->> 'transfer_source', t.source::text) END AS payment_source,
               info.value ->> 'suspense_reason_detail' AS relevant_details,
               fee.court_fee_code,
               CASE WHEN p.associated_record_type = 'miscellaneous_accounts' THEN
                   CASE WHEN payer.organisation THEN 'Organisation'
                        WHEN payer.organisation = FALSE THEN 'Individual' END
                   ELSE info.value ->> 'payer_type' END AS payer_type,
               CASE WHEN p.associated_record_type = 'miscellaneous_accounts' THEN
                   CASE WHEN payer.organisation THEN payer.organisation_name
                        ELSE NULLIF(concat_ws(' ', payer.title, payer.forenames, payer.surname), '') END
                   WHEN info.value ->> 'payer_type' = 'Organisation' THEN
                       info.value #>> '{payer_details,organisation,organisation_name}'
                   WHEN info.value ->> 'payer_type' = 'Individual' THEN
                       NULLIF(concat_ws(' ', info.value #>> '{payer_details,individual,title}',
                           info.value #>> '{payer_details,individual,forenames}',
                           info.value #>> '{payer_details,individual,surname}'), '') END AS payer_name,
               CASE WHEN p.associated_record_type = 'miscellaneous_accounts' THEN
                   NULLIF(concat_ws(', ', payer.address_line_1, payer.address_line_2, payer.address_line_3,
                       payer.address_line_4, payer.address_line_5, payer.postcode), '')
                   ELSE NULLIF(concat_ws(', ', info.value #>> '{payer_details,address_line_1}',
                       info.value #>> '{payer_details,address_line_2}',
                       info.value #>> '{payer_details,address_line_3}',
                       info.value #>> '{payer_details,postcode}'), '') END AS payer_address,
               da.account_number AS defendant_account_number,
               dp.party_id AS defendant_party_id,
               dp.organisation AS defendant_organisation,
               dp.organisation_name AS defendant_organisation_name,
               dp.title AS defendant_title,
               dp.forenames AS defendant_forenames,
               dp.surname AS defendant_surname,
               i.imposition_id,
               ca.creditor_account_id,
               ca.account_number AS creditor_account_number,
               ca.creditor_account_type::text AS creditor_account_type,
               ca.repayment AS repayment_creditor,
               ca.pay_by_bacs,
               mc.name AS major_creditor_name,
               cp.party_id AS creditor_party_id,
               cp.organisation AS creditor_organisation,
               cp.organisation_name AS creditor_organisation_name,
               cp.forenames AS creditor_forenames,
               cp.surname AS creditor_surname,
               CASE ca.creditor_account_type WHEN 'MN' THEN cp.address_line_1
                   WHEN 'MJ' THEN mc.address_line_1 ELSE config.item_values ->> 'address_line_1' END
                   AS "creditorAddressLine1",
               CASE ca.creditor_account_type WHEN 'MN' THEN cp.address_line_2
                   WHEN 'MJ' THEN mc.address_line_2 ELSE config.item_values ->> 'address_line_2' END
                   AS "creditorAddressLine2",
               CASE ca.creditor_account_type WHEN 'MN' THEN cp.address_line_3
                   WHEN 'MJ' THEN mc.address_line_3 ELSE config.item_values ->> 'address_line_3' END
                   AS "creditorAddressLine3",
               cp.address_line_4 AS "creditorAddressLine4",
               cp.address_line_5 AS "creditorAddressLine5",
               CASE ca.creditor_account_type WHEN 'MN' THEN cp.postcode
                   WHEN 'MJ' THEN mc.postcode ELSE config.item_values ->> 'postcode' END AS creditor_postcode
        FROM public.suspense_transactions st
        JOIN public.suspense_items si
          ON si.suspense_item_id = st.suspense_item_id
        JOIN public.suspense_accounts sa
          ON sa.suspense_account_id = si.suspense_account_id
        -- Parse canonical references on the value side, preserving indexed bigint lookups.
        -- Nested CASE prevents malformed or overflowing values from reaching the bigint cast.
        LEFT JOIN LATERAL (
            SELECT CASE WHEN st.associated_record_id ~ '^(0|-?[1-9][0-9]{0,18})$' THEN
                CASE WHEN st.associated_record_id::numeric BETWEEN -9223372036854775808 AND 9223372036854775807
                    THEN st.associated_record_id::bigint END END AS record_id
        ) reference ON TRUE
        -- Till allocation stores a payment ID with a null associated record type.
        LEFT JOIN (public.payments_in p JOIN public.tills t ON t.till_id = p.till_id)
          ON p.payment_in_id = reference.record_id
         AND t.business_unit_id = sa.business_unit_id
         AND st.associated_record_type IS NULL
         AND st.transaction_type::text IN ('UN', 'CF', 'OC', 'OF', 'FA')
        LEFT JOIN LATERAL (
            SELECT CASE WHEN p.associated_record_id ~ '^(0|-?[1-9][0-9]{0,18})$' THEN
                CASE WHEN p.associated_record_id::numeric BETWEEN -9223372036854775808 AND 9223372036854775807
                    THEN p.associated_record_id::bigint END END AS record_id
        ) payment_reference ON TRUE
        LEFT JOIN LATERAL (
            SELECT COALESCE(p.additional_information::jsonb -> 'additional_information',
                            p.additional_information::jsonb) AS value
        ) info ON TRUE
        LEFT JOIN public.miscellaneous_accounts ma
          ON p.associated_record_type = 'miscellaneous_accounts'
         AND ma.miscellaneous_account_id = payment_reference.record_id
         AND ma.business_unit_id = sa.business_unit_id
        LEFT JOIN public.parties payer ON payer.party_id = ma.party_id
        LEFT JOIN LATERAL (
            SELECT CASE WHEN count(*) = 1 THEN min(cf.court_fee_code) END AS court_fee_code
            FROM public.court_fees_received cfr
            JOIN public.court_fees cf ON cf.court_fee_id = cfr.court_fee_id
             AND cf.business_unit_id = sa.business_unit_id
            WHERE cfr.suspense_transaction_id = st.suspense_transaction_id
              AND cfr.business_unit_id = sa.business_unit_id
        ) fee ON TRUE
        LEFT JOIN (public.defendant_transactions dt
                   JOIN public.defendant_accounts dta ON dta.defendant_account_id = dt.defendant_account_id)
          ON st.associated_record_type = 'defendant_transactions'
         AND dt.defendant_transaction_id = reference.record_id
         AND dta.business_unit_id = sa.business_unit_id
        LEFT JOIN (public.creditor_transactions ct
                   JOIN public.creditor_accounts cta ON cta.creditor_account_id = ct.creditor_account_id)
          ON st.associated_record_type = 'creditor_transactions'
         AND ct.creditor_transaction_id = reference.record_id
         AND cta.business_unit_id = sa.business_unit_id
        LEFT JOIN (public.impositions i
                   JOIN public.defendant_accounts ida ON ida.defendant_account_id = i.defendant_account_id
                   JOIN public.creditor_accounts ica ON ica.creditor_account_id = i.creditor_account_id)
          ON st.associated_record_type = 'impositions' AND i.imposition_id = reference.record_id
         AND ida.business_unit_id = sa.business_unit_id AND ica.business_unit_id = sa.business_unit_id
        LEFT JOIN public.defendant_accounts da
          ON da.defendant_account_id = CASE
              WHEN st.associated_record_type = 'defendant_accounts' THEN reference.record_id
              WHEN dt.defendant_transaction_id IS NOT NULL THEN dt.defendant_account_id
              WHEN i.imposition_id IS NOT NULL THEN i.defendant_account_id
              WHEN p.associated_record_type = 'defendant_accounts' THEN payment_reference.record_id END
         AND da.business_unit_id = sa.business_unit_id
        LEFT JOIN LATERAL (
            SELECT min(dap.party_id) AS party_id
            FROM public.defendant_account_parties dap
            WHERE dap.defendant_account_id = da.defendant_account_id AND dap.association_type = 'Defendant'
            HAVING count(*) = 1
        ) defendant ON TRUE
        LEFT JOIN public.parties dp ON dp.party_id = defendant.party_id
        LEFT JOIN public.creditor_accounts ca
          ON ca.creditor_account_id = CASE
              WHEN st.associated_record_type = 'creditor_accounts' THEN reference.record_id
              WHEN ct.creditor_transaction_id IS NOT NULL THEN ct.creditor_account_id
              WHEN i.imposition_id IS NOT NULL THEN i.creditor_account_id END
         AND ca.business_unit_id = sa.business_unit_id
        LEFT JOIN public.parties cp ON cp.party_id = ca.minor_creditor_party_id AND ca.creditor_account_type = 'MN'
        LEFT JOIN public.major_creditors mc ON mc.major_creditor_id = ca.major_creditor_id
         AND mc.business_unit_id = sa.business_unit_id AND ca.creditor_account_type = 'MJ'
        LEFT JOIN LATERAL (
            SELECT jsonb_agg(ci.item_values) -> 0 AS item_values
            FROM public.configuration_items ci
            WHERE ci.business_unit_id = sa.business_unit_id AND ci.item_name = 'CENTRAL_FUND_ACCOUNT'
              AND ca.creditor_account_type = 'CF'
            HAVING count(*) = 1
        ) config ON TRUE
        WHERE si.suspense_item_id = :suspenseItemId
          AND sa.business_unit_id = :businessUnitId
        ORDER BY st.posted_date DESC, st.suspense_transaction_id DESC;
        """, nativeQuery = true)
    List<SuspenseTransactionHistoryProjection> findHistory(
        @Param("suspenseItemId") Long suspenseItemId,
        @Param("businessUnitId") Long businessUnitId);
}

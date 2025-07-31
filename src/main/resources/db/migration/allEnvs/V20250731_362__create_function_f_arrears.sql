/**
* CGI OPAL Program
*
* MODULE      : create_function_f_arrears.sql
*
* DESCRIPTION : Capita's function to calculate the arrears for a defendant account
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 25/07/2025    C Cho       1.0         PO-1641 - Capita's function to calculate arrears amount
*
**/
CREATE OR REPLACE FUNCTION f_arrears(
    IN pi_defendant_account_id bigint)
RETURNS decimal(18,2)
AS $$
    SELECT  CASE
                WHEN effective_date > CURRENT_DATE THEN 0
                WHEN terms_type_code = 'B' THEN GREATEST(amount_outstanding,0)
                WHEN terms_type_code = 'I' THEN
                    GREATEST(
                        LEAST((payment_periods*instalment_amount)+lump_sum-paid,amount_outstanding),
                        0)
                ELSE 0
            END AS arrears
    FROM    (
                SELECT      da.defendant_account_id,
                            pt.terms_type_code,
                            pt.effective_date,
                            pt.instalment_period,
                            pt.instalment_amount,
                            COALESCE(pt.instalment_lump_sum,0) AS lump_sum,
                            -da.account_balance AS amount_outstanding,
                            COALESCE((
                                SELECT  SUM(COALESCE(transaction_amount,0))
                                FROM    defendant_transactions
                                WHERE   defendant_account_id = da.defendant_account_id
                                AND     posted_date > pt.posted_date
                                AND     transaction_type IN ('PAYMNT','MADJ','CANCHQ','RICHEQ','CHEQUE','REVPAY','XFER','FR-SUS','DISHCQ','REPSUS')
                            ),0) AS paid,
                            CASE pt.instalment_period
                                WHEN 'M' THEN f_floor_months_between(CURRENT_DATE,pt.effective_date::date)
                                WHEN 'F' THEN (CURRENT_DATE-pt.effective_date::date)/14
                                WHEN 'W' THEN (CURRENT_DATE-pt.effective_date::date)/7
                                ELSE 0
                            END AS payment_periods
                FROM        payment_terms pt
                INNER JOIN  defendant_accounts da ON da.defendant_account_id = pt.defendant_account_id
                WHERE       pt.defendant_account_id = pi_defendant_account_id
                AND         pt.active
                ORDER BY    posted_date DESC
                LIMIT       1
            ) AS v
$$ LANGUAGE SQL;

COMMENT ON FUNCTION f_arrears(bigint) IS 'Calculates the arrears amount for a given defendant account';
INSERT INTO public.business_units (
    business_unit_id,
    business_unit_name,
    business_unit_code,
    business_unit_type
)
VALUES (
    10347,
    'PO-10347 Integration Test',
    'Q347',
    'Accounting Division'
);

INSERT INTO public.creditor_accounts (
    creditor_account_id,
    business_unit_id,
    account_number,
    creditor_account_type,
    prosecution_service,
    repayment,
    hold_payout,
    pay_by_bacs
)
VALUES (
    9999010347001,
    10347,
    'QA10347',
    'CF',
    FALSE,
    FALSE,
    FALSE,
    FALSE
);

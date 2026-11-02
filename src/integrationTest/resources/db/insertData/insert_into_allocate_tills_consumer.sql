INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type)
VALUES (9432, 'BU Name', 'U432', 'Accounting Division');

INSERT INTO tills (till_id, business_unit_id, till_number, owned_by, status, auto_payment)
VALUES (9999003432001, 9432, 1, 'USER01', 'Processing', FALSE);

INSERT INTO suspense_accounts (suspense_account_id, business_unit_id, account_number)
VALUES (9999003432001, 9432, 'PO3432-SUSPENSE');

INSERT INTO payments_in (
    payment_in_id, till_id, payment_amount, payment_date, payment_method,
    destination_type, allocation_type, additional_information, receipt, allocated, auto_payment
)
VALUES (
    9999003432001, 9999003432001, 10.00, TIMESTAMP '2026-01-01 10:00:00', 'NC',
    'S', 'UN', '{}', FALSE, FALSE, FALSE
);

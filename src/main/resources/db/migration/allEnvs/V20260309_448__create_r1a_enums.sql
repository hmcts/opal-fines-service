/**
* CGI OPAL Program
*
* MODULE      : create_r1a_enums.sql
*
* DESCRIPTION : Create new ENUM data types
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ------------------------------------------------------------------------------------------
* 04/03/2026    T McCallion    1.0         PO-2854 - Create PostgreSQL Enumerated data types to be used instead of VARCHAR data types
*
**/
CREATE TYPE t_associated_record_type_enum AS ENUM ('defendant_accounts', 'creditor_transactions', 'miscellaneous_accounts', 'creditor_accounts', 
                                                   'suspense_transactions', 'suspense_items', 'enforcements', 'cheques', 'impositions', 
                                                   'defendant_transactions', 'report_instances');
CREATE TYPE t_association_type_enum AS ENUM ('Defendant', 'Parent/Guardian');
CREATE TYPE t_business_unit_type_enum AS ENUM ('Accounting Division', 'Area');
CREATE TYPE t_consolidated_account_type_enum AS ENUM ('M', 'C');
CREATE TYPE t_creditor_account_type_enum AS ENUM ('CF', 'MJ', 'MN');
CREATE TYPE t_creditor_transaction_status_enum AS ENUM ('C', 'D', 'P', 'R', 'X');
CREATE TYPE t_creditor_transaction_type_enum AS ENUM ('PAYMNT', 'XFER', 'MADJ', 'REPSUS', 'CHEQUE', 'CANCHQ', 'RICHEQ', 'BACS', 'RTBACS', 'RIBACS', 
                                                      'REPAYC', 'REPAYF', 'REPAYM', 'REPAYP', 'REPAYV', 'REPAYW', 'WO611B', 'CFEES', 'LIFEES', 'REPLIC', 
                                                      'PAID', 'FINES', 'FIXPEN', 'FDCOST', 'FO', 'FCOST', 'FCPC', 'FCOMP', 'FVS', 'FCC', 'FEES', 'FCUEX', 
                                                      'FNIA', 'FOPR1', 'FLAID', 'FVEA', 'FVEBD', 'FWEC', 'FDCON', 'FFR', 'FCMP', 'FCST', 'FINE');
CREATE TYPE t_da_account_status_enum AS ENUM ('CS', 'L', 'TA', 'TO', 'TS', 'WO');
CREATE TYPE t_da_account_type_enum AS ENUM ('Fine', 'Fixed Penalty', 'Conditional Caution', 'Confiscation');
CREATE TYPE t_defendant_transaction_status_enum AS ENUM ('C', 'D', 'P', 'R', 'X');
CREATE TYPE t_defendant_transaction_type_enum AS ENUM ('CANCHQ', 'CHEQUE', 'CONSOL', 'DISHCQ', 'FR-SUS', 'MADJ', 'PAYMNT', 'REPSUS', 'REVPAY', 'RICHEQ', 
                                                       'RVWOFF', 'TFO', 'TFO IN', 'WRTOFF', 'XFER');
CREATE TYPE t_di_status_enum AS ENUM ('New');  --Other values will be added at a later date
CREATE TYPE t_dra_account_status_enum AS ENUM ('DELETED', 'PUBLISHING_FAILED', 'PUBLISHING_PENDING', 'REJECTED', 'APPROVED', 'RESUBMITTED', 'LEGACY_PENDING', 'PUBLISHED', 'SUBMITTED');
CREATE TYPE t_header_type_enum AS ENUM ('A', 'AP', 'EO', 'MC', 'ME', 'MA', 'MF');
CREATE TYPE t_instalment_period_enum AS ENUM ('F', 'M', 'W');
CREATE TYPE t_language_enum AS ENUM ('EN', 'CY');
CREATE TYPE t_lja_type_enum AS ENUM ('LJA', 'CRWCRT', 'SJCRT', 'NICRT', 'SCSCRT');
CREATE TYPE t_note_type_enum AS ENUM ('AA', 'AC', 'AN');
CREATE TYPE t_originator_type_enum AS ENUM ('NEW', 'FP', 'TFO');
CREATE TYPE t_party_account_type_enum AS ENUM ('Creditor', 'Defendant');
CREATE TYPE t_payment_method_enum AS ENUM ('NC', 'CQ', 'CT', 'PO');
CREATE TYPE t_priority_enum AS ENUM ('0', '1', '2');
CREATE TYPE t_recipient_enum AS ENUM ('BENA', 'CLAC', 'CRED', 'DEF', 'EMP', 'FRA', 'OTHC', 'PRIS');
CREATE TYPE t_signature_source_enum AS ENUM ('Area', 'LJA');
CREATE TYPE t_terms_type_code_enum AS ENUM ('B', 'I', 'P');
CREATE TYPE t_write_off_code_enum AS ENUM ('JCAM-A', 'JCAM-B', 'JCAM-C', 'JCAM-D', 'JCAM-E', 'JCAM-F', 'JCAM-G', 'JCAM-H', 'JCAM-I', 'JCAM-K', 'REMITT', 
                                           'IMPRIS', 'APPEAL', 'CTPROC', 'FIXPEN', 'REVIEW', 'INPERR', 'OTHERS', 'AMTCON', 'TRNOUT');
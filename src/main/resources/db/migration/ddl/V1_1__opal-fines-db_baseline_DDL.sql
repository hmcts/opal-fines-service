--
-- PostgreSQL database dump
--

-- Dumped from database version 17.9 (Debian 17.9-1.pgdg13+1)
-- Dumped by pg_dump version 17.9 (Homebrew)

-- Started on 2026-05-01 14:25:58 BST

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';

--
-- Name: r_supported_file_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.r_supported_file_type_enum AS ENUM (
    'CSV',
    'PDF',
    'XML'
);

--
-- Name: ri_generation_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.ri_generation_status_enum AS ENUM (
    'REQUESTED',
    'IN_PROGRESS',
    'READY',
    'ERROR'
);

--
-- Name: t_account_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_account_type_enum AS ENUM (
    'COL',
    'A',
    'CO',
    'Y'
);

--
-- Name: t_associated_record_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_associated_record_type_enum AS ENUM (
    'defendant_accounts',
    'creditor_transactions',
    'miscellaneous_accounts',
    'creditor_accounts',
    'suspense_transactions',
    'suspense_items',
    'enforcements',
    'cheques',
    'impositions',
    'defendant_transactions',
    'report_instances'
);

--
-- Name: t_association_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_association_type_enum AS ENUM (
    'Defendant',
    'Parent/Guardian'
);

--
-- Name: t_business_unit_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_business_unit_type_enum AS ENUM (
    'Accounting Division',
    'Area'
);

--
-- Name: t_consolidated_account_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_consolidated_account_type_enum AS ENUM (
    'M',
    'C'
);

--
-- Name: t_creditor_account_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_creditor_account_type_enum AS ENUM (
    'CF',
    'MJ',
    'MN'
);

--
-- Name: t_creditor_transaction_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_creditor_transaction_status_enum AS ENUM (
    'C',
    'D',
    'P',
    'R',
    'X'
);

--
-- Name: t_creditor_transaction_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_creditor_transaction_type_enum AS ENUM (
    'PAYMNT',
    'XFER',
    'MADJ',
    'REPSUS',
    'CHEQUE',
    'CANCHQ',
    'RICHEQ',
    'BACS',
    'RTBACS',
    'RIBACS',
    'REPAYC',
    'REPAYF',
    'REPAYM',
    'REPAYP',
    'REPAYV',
    'REPAYW',
    'WO611B',
    'CFEES',
    'LIFEES',
    'REPLIC',
    'PAID',
    'FINES',
    'FIXPEN',
    'FDCOST',
    'FO',
    'FCOST',
    'FCPC',
    'FCOMP',
    'FVS',
    'FCC',
    'FEES',
    'FCUEX',
    'FNIA',
    'FOPR1',
    'FLAID',
    'FVEA',
    'FVEBD',
    'FWEC',
    'FDCON',
    'FFR',
    'FCMP',
    'FCST',
    'FINE'
);

--
-- Name: t_da_account_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_da_account_status_enum AS ENUM (
    'CS',
    'L',
    'TA',
    'TO',
    'TS',
    'WO'
);

--
-- Name: t_da_account_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_da_account_type_enum AS ENUM (
    'Fine',
    'Fixed Penalty',
    'Conditional Caution',
    'Confiscation'
);

--
-- Name: t_defendant_transaction_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_defendant_transaction_status_enum AS ENUM (
    'C',
    'D',
    'P',
    'R',
    'X'
);

--
-- Name: t_defendant_transaction_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_defendant_transaction_type_enum AS ENUM (
    'CANCHQ',
    'CHEQUE',
    'CONSOL',
    'DISHCQ',
    'FR-SUS',
    'MADJ',
    'PAYMNT',
    'REPSUS',
    'REVPAY',
    'RICHEQ',
    'RVWOFF',
    'TFO',
    'TFO IN',
    'WRTOFF',
    'XFER'
);

--
-- Name: t_di_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_di_status_enum AS ENUM (
    'New'
);

--
-- Name: t_dra_account_status_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_dra_account_status_enum AS ENUM (
    'DELETED',
    'PUBLISHING_FAILED',
    'PUBLISHING_PENDING',
    'REJECTED',
    'APPROVED',
    'RESUBMITTED',
    'LEGACY_PENDING',
    'PUBLISHED',
    'SUBMITTED'
);

--
-- Name: t_enforcement_account_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_enforcement_account_type_enum AS ENUM (
    'COLL',
    'COLH',
    'AL',
    'AH',
    'COL',
    'COH',
    'YL',
    'YH'
);

--
-- Name: t_header_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_header_type_enum AS ENUM (
    'A',
    'AP',
    'EO',
    'MC',
    'ME',
    'MA',
    'MF'
);

--
-- Name: t_instalment_period_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_instalment_period_enum AS ENUM (
    'F',
    'M',
    'W'
);

--
-- Name: t_language_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_language_enum AS ENUM (
    'EN',
    'CY'
);

--
-- Name: t_lja_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_lja_type_enum AS ENUM (
    'LJA',
    'CRWCRT',
    'SJCRT',
    'NICRT',
    'SCSCRT'
);

--
-- Name: t_low_high_value_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_low_high_value_enum AS ENUM (
    'L',
    'H'
);

--
-- Name: t_note_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_note_type_enum AS ENUM (
    'AA',
    'AC',
    'AN'
);

--
-- Name: t_originator_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_originator_type_enum AS ENUM (
    'NEW',
    'FP',
    'TFO'
);

--
-- Name: t_party_account_type_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_party_account_type_enum AS ENUM (
    'Creditor',
    'Defendant'
);

--
-- Name: t_payment_method_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_payment_method_enum AS ENUM (
    'NC',
    'CQ',
    'CT',
    'PO'
);

--
-- Name: t_priority_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_priority_enum AS ENUM (
    '0',
    '1',
    '2'
);

--
-- Name: t_recipient_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_recipient_enum AS ENUM (
    'BENA',
    'CLAC',
    'CRED',
    'DEF',
    'EMP',
    'FRA',
    'OTHC',
    'PRIS'
);

--
-- Name: t_signature_source_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_signature_source_enum AS ENUM (
    'Area',
    'LJA'
);

--
-- Name: t_terms_type_code_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_terms_type_code_enum AS ENUM (
    'B',
    'I',
    'P'
);

--
-- Name: t_write_off_code_enum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.t_write_off_code_enum AS ENUM (
    'JCAM-A',
    'JCAM-B',
    'JCAM-C',
    'JCAM-D',
    'JCAM-E',
    'JCAM-F',
    'JCAM-G',
    'JCAM-H',
    'JCAM-I',
    'JCAM-K',
    'REMITT',
    'IMPRIS',
    'APPEAL',
    'CTPROC',
    'FIXPEN',
    'REVIEW',
    'INPERR',
    'OTHERS',
    'AMTCON',
    'TRNOUT'
);

--
-- Name: account_number_index; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_number_index (
    account_number_index_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    account_number character varying(20) NOT NULL,
    associated_record_type public.t_associated_record_type_enum
);

--
-- Name: COLUMN account_number_index.account_number_index_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_number_index.account_number_index_id IS 'Unique ID of this record';

--
-- Name: COLUMN account_number_index.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_number_index.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN account_number_index.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_number_index.account_number IS 'Account number unique within the business unit';

--
-- Name: COLUMN account_number_index.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_number_index.associated_record_type IS 'The target table the account is intended for. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: account_number_index_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.account_number_index_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: account_number_index_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.account_number_index_seq OWNED BY public.account_number_index.account_number_index_id;

--
-- Name: account_transfers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.account_transfers (
    account_transfer_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    defendant_account_id bigint NOT NULL,
    initiated_date timestamp without time zone,
    initiated_by character varying(20),
    printed_date timestamp without time zone,
    printed_by character varying(20),
    document_instance_id bigint,
    destination_lja_id smallint NOT NULL,
    reason character varying(100),
    reminder_date timestamp without time zone
);

--
-- Name: COLUMN account_transfers.account_transfer_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.account_transfer_id IS 'Unique ID of this record';

--
-- Name: COLUMN account_transfers.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN account_transfers.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.defendant_account_id IS 'Account Number being transferred out';

--
-- Name: COLUMN account_transfers.initiated_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.initiated_date IS 'Date this transfer was initiated';

--
-- Name: COLUMN account_transfers.initiated_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.initiated_by IS 'User generating the transfer Out';

--
-- Name: COLUMN account_transfers.printed_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.printed_date IS 'Date the TFO Enforcement Order was printed';

--
-- Name: COLUMN account_transfers.printed_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.printed_by IS 'ID of the user that printed the TFO Enforcement Order';

--
-- Name: COLUMN account_transfers.document_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.document_instance_id IS 'ID of the TFFOUT Enforcement Order document';

--
-- Name: COLUMN account_transfers.destination_lja_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.destination_lja_id IS 'Destination LJA code';

--
-- Name: COLUMN account_transfers.reason; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.reason IS 'Reason for the transfer out';

--
-- Name: COLUMN account_transfers.reminder_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.account_transfers.reminder_date IS 'Date that a reminder is sent to the destination court to acknowledge receipt';

--
-- Name: account_transfer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.account_transfer_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: account_transfer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.account_transfer_id_seq OWNED BY public.account_transfers.account_transfer_id;

--
-- Name: aliases; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.aliases (
    alias_id bigint NOT NULL,
    party_id bigint NOT NULL,
    surname character varying(50),
    forenames character varying(50),
    sequence_number integer NOT NULL,
    organisation_name character varying(50)
);

--
-- Name: COLUMN aliases.alias_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.aliases.alias_id IS 'Unique ID of this record';

--
-- Name: COLUMN aliases.party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.aliases.party_id IS 'ID of the party the alias belongs to';

--
-- Name: COLUMN aliases.surname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.aliases.surname IS 'Alias surname';

--
-- Name: COLUMN aliases.forenames; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.aliases.forenames IS 'Alias forenames';

--
-- Name: COLUMN aliases.sequence_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.aliases.sequence_number IS 'Account/party level lias sequence';

--
-- Name: COLUMN aliases.organisation_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.aliases.organisation_name IS 'Alias organisation name';

--
-- Name: alias_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alias_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: alias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.alias_id_seq OWNED BY public.aliases.alias_id;

--
-- Name: allocations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.allocations (
    allocation_id bigint NOT NULL,
    imposition_id bigint NOT NULL,
    allocated_date timestamp without time zone NOT NULL,
    allocated_amount numeric(18,2) NOT NULL,
    transaction_type character varying(10) NOT NULL,
    allocation_function character varying(30) NOT NULL,
    defendant_transaction_id bigint
);

--
-- Name: COLUMN allocations.allocation_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.allocation_id IS 'Unique ID of this record';

--
-- Name: COLUMN allocations.imposition_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.imposition_id IS 'Imposition account Number';

--
-- Name: COLUMN allocations.allocated_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.allocated_date IS 'Allocation timestamp';

--
-- Name: COLUMN allocations.allocated_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.allocated_amount IS 'Allocation amount';

--
-- Name: COLUMN allocations.transaction_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.transaction_type IS 'The type of transaction this allocation is associated with';

--
-- Name: COLUMN allocations.allocation_function; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.allocation_function IS 'The function used to create the allocation';

--
-- Name: COLUMN allocations.defendant_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.allocations.defendant_transaction_id IS 'The transaction being allocated to the imposition';

--
-- Name: allocation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.allocation_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: allocation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.allocation_id_seq OWNED BY public.allocations.allocation_id;

--
-- Name: amendments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.amendments (
    amendment_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    associated_record_type character varying(30) NOT NULL,
    associated_record_id character varying(30) NOT NULL,
    amended_date timestamp without time zone NOT NULL,
    amended_by character varying(20) NOT NULL,
    field_code smallint NOT NULL,
    old_value text,
    new_value text,
    case_reference character varying(40),
    function_code character varying(30)
);

--
-- Name: COLUMN amendments.amendment_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.amendment_id IS 'Unique ID of this record';

--
-- Name: COLUMN amendments.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN amendments.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.associated_record_type IS 'ID of the account amended';

--
-- Name: COLUMN amendments.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.associated_record_id IS 'ID of the creditor account amended';

--
-- Name: COLUMN amendments.amended_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.amended_date IS 'Date the amendment was made';

--
-- Name: COLUMN amendments.amended_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.amended_by IS 'ID of the user that made the amendment';

--
-- Name: COLUMN amendments.field_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.field_code IS 'The fied modified';

--
-- Name: COLUMN amendments.old_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.old_value IS 'Field value before amendment';

--
-- Name: COLUMN amendments.new_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.new_value IS 'Field value after amendment';

--
-- Name: COLUMN amendments.case_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.case_reference IS 'Case number if modified by Case Management';

--
-- Name: COLUMN amendments.function_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.amendments.function_code IS 'Function from which the amendment was made';

--
-- Name: amendment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.amendment_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: amendment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.amendment_id_seq OWNED BY public.amendments.amendment_id;

--
-- Name: audit_amendment_fields; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_amendment_fields (
    field_code smallint NOT NULL,
    data_item character varying(50) NOT NULL
);

--
-- Name: COLUMN audit_amendment_fields.field_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.audit_amendment_fields.field_code IS 'The numeric value assigned to the field';

--
-- Name: COLUMN audit_amendment_fields.data_item; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.audit_amendment_fields.data_item IS 'The name of the data held against the field code';

--
-- Name: bacs_payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bacs_payments (
    bacs_payment_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    bacs_number bigint NOT NULL,
    issue_date timestamp without time zone NOT NULL,
    creditor_transaction_id bigint,
    defendant_transaction_id bigint,
    amount numeric(18,2) NOT NULL,
    status character varying(10) NOT NULL
);

--
-- Name: COLUMN bacs_payments.bacs_payment_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.bacs_payment_id IS 'Unique ID of this record';

--
-- Name: COLUMN bacs_payments.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN bacs_payments.bacs_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.bacs_number IS 'BACS schema level sequence number';

--
-- Name: COLUMN bacs_payments.issue_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.issue_date IS 'Issue date';

--
-- Name: COLUMN bacs_payments.creditor_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.creditor_transaction_id IS 'ID of the relating creditor transaction';

--
-- Name: COLUMN bacs_payments.defendant_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.defendant_transaction_id IS 'ID of the relating defendant transaction';

--
-- Name: COLUMN bacs_payments.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.amount IS 'Payment amount';

--
-- Name: COLUMN bacs_payments.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.bacs_payments.status IS 'BACS status';

--
-- Name: bacs_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.bacs_payment_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: bacs_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.bacs_payment_id_seq OWNED BY public.bacs_payments.bacs_payment_id;

--
-- Name: business_units; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.business_units (
    business_unit_id smallint NOT NULL,
    business_unit_name character varying(200) NOT NULL,
    business_unit_code character varying(4),
    business_unit_type public.t_business_unit_type_enum NOT NULL,
    account_number_prefix character varying(2),
    parent_business_unit_id smallint,
    opal_domain character varying(30),
    welsh_language boolean,
    account_number_suffix character varying(2)
);

--
-- Name: COLUMN business_units.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.business_unit_id IS 'Unique ID of this record';

--
-- Name: COLUMN business_units.business_unit_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.business_unit_name IS 'Business Unit name';

--
-- Name: COLUMN business_units.business_unit_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.business_unit_code IS 'Business unit code';

--
-- Name: COLUMN business_units.business_unit_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.business_unit_type IS 'Area or Accounting Division';

--
-- Name: COLUMN business_units.account_number_prefix; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.account_number_prefix IS 'Accounting division code that appears before account numbers';

--
-- Name: COLUMN business_units.parent_business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.parent_business_unit_id IS 'ID of the business unit that is the parent for this one';

--
-- Name: COLUMN business_units.opal_domain; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.opal_domain IS 'When business unit type is Accounting Division, then this value is the opal domain that the business uint is owned by';

--
-- Name: COLUMN business_units.welsh_language; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.welsh_language IS 'To identify if this is a welsh language business unit in Opal. It does not exist in Legacy GoB';

--
-- Name: COLUMN business_units.account_number_suffix; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.business_units.account_number_suffix IS 'The Accounting Division suffix used with Account Numbers';

--
-- Name: cheques; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cheques (
    cheque_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    cheque_number bigint NOT NULL,
    issue_date timestamp without time zone NOT NULL,
    creditor_transaction_id bigint,
    defendant_transaction_id bigint,
    amount numeric(18,2) NOT NULL,
    allocation_type character varying(10),
    reminder_date timestamp without time zone,
    status character varying(1) NOT NULL
);

--
-- Name: COLUMN cheques.cheque_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.cheque_id IS 'Unique ID of this record';

--
-- Name: COLUMN cheques.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN cheques.cheque_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.cheque_number IS 'Business unit level cheque number';

--
-- Name: COLUMN cheques.issue_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.issue_date IS 'Issue date';

--
-- Name: COLUMN cheques.creditor_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.creditor_transaction_id IS 'ID of the relating creditor transaction';

--
-- Name: COLUMN cheques.defendant_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.defendant_transaction_id IS 'ID of the relating defendant transaction';

--
-- Name: COLUMN cheques.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.amount IS 'Payment amount';

--
-- Name: COLUMN cheques.allocation_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.allocation_type IS 'Indicates what this cheque payment is in respect of, for example, COMP or REPAYW.';

--
-- Name: COLUMN cheques.reminder_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.reminder_date IS 'The date a reminder letter to present the cheque was sent to the creditor';

--
-- Name: COLUMN cheques.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cheques.status IS 'The cheque status. Values: N (new), D (destroyed), P (presented), Q (query - presented different amount), W (Withdrawn), X (Awaiting deletion)';

--
-- Name: cheque_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cheque_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: cheque_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cheque_id_seq OWNED BY public.cheques.cheque_id;

--
-- Name: committal_warrant_progress; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.committal_warrant_progress (
    defendant_account_id bigint NOT NULL,
    enforcement_id bigint NOT NULL,
    amount numeric(18,2),
    body_receipt_date timestamp without time zone,
    certificate_part_a_date timestamp without time zone,
    certificate_part_b_date timestamp without time zone,
    prison_id bigint
);

--
-- Name: COLUMN committal_warrant_progress.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.defendant_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN committal_warrant_progress.enforcement_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.enforcement_id IS 'Associated CW enforcement ID containing CW date and warrant reference';

--
-- Name: COLUMN committal_warrant_progress.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.amount IS 'Committal Warrant amount';

--
-- Name: COLUMN committal_warrant_progress.body_receipt_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.body_receipt_date IS 'Committal Warrant date of body receipt';

--
-- Name: COLUMN committal_warrant_progress.certificate_part_a_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.certificate_part_a_date IS 'Committal Warrant date of Certificate of Imprisonment Part A';

--
-- Name: COLUMN committal_warrant_progress.certificate_part_b_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.certificate_part_b_date IS 'Committal Warrant date of Certificate of Imprisonment Part B';

--
-- Name: COLUMN committal_warrant_progress.prison_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.committal_warrant_progress.prison_id IS 'Unique identifier of prison committed to';

--
-- Name: configuration_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.configuration_items (
    configuration_item_id bigint NOT NULL,
    item_name character varying(50) NOT NULL,
    business_unit_id smallint,
    item_value text,
    item_values json
);

--
-- Name: COLUMN configuration_items.configuration_item_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.configuration_items.configuration_item_id IS 'Configuration item ID';

--
-- Name: COLUMN configuration_items.item_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.configuration_items.item_name IS 'Configuration item name';

--
-- Name: COLUMN configuration_items.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.configuration_items.business_unit_id IS 'ID of the business unit or NULL for all';

--
-- Name: COLUMN configuration_items.item_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.configuration_items.item_value IS 'Single text value';

--
-- Name: configuration_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.configuration_item_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: configuration_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.configuration_item_id_seq OWNED BY public.configuration_items.configuration_item_id;

--
-- Name: control_totals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.control_totals (
    control_total_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    item_number smallint NOT NULL,
    amount numeric(18,2) NOT NULL,
    associated_record_type character varying(30),
    associated_record_id character varying(30),
    ct_report_instance_id bigint,
    qe_report_instance_id bigint
);

--
-- Name: COLUMN control_totals.control_total_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.control_total_id IS 'Unique ID of this record';

--
-- Name: COLUMN control_totals.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN control_totals.item_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.item_number IS 'Control total item number';

--
-- Name: COLUMN control_totals.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.amount IS 'Amount of this item';

--
-- Name: COLUMN control_totals.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.associated_record_type IS 'Type of record identified by associated_record_id';

--
-- Name: COLUMN control_totals.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.associated_record_id IS 'ID or other reference/number of an associated record';

--
-- Name: COLUMN control_totals.ct_report_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.ct_report_instance_id IS 'Report instance where this amount was reported on control totals';

--
-- Name: COLUMN control_totals.qe_report_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.control_totals.qe_report_instance_id IS 'Report instance where this amount was reported on quarter end';

--
-- Name: control_total_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.control_total_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: control_total_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.control_total_id_seq OWNED BY public.control_totals.control_total_id;

--
-- Name: court_fees; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.court_fees (
    court_fee_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    court_fee_code character varying(10) NOT NULL,
    description character varying(50) NOT NULL,
    amount numeric(18,2) NOT NULL,
    stats_code character varying(10) NOT NULL
);

--
-- Name: COLUMN court_fees.court_fee_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees.court_fee_id IS 'Unique ID of this record';

--
-- Name: COLUMN court_fees.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN court_fees.court_fee_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees.court_fee_code IS 'Court fee code unique to the business unit';

--
-- Name: COLUMN court_fees.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees.description IS 'Description of the service for which this court fee is payable';

--
-- Name: COLUMN court_fees.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees.amount IS 'The amount payable for this service';

--
-- Name: COLUMN court_fees.stats_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees.stats_code IS 'The statistic code that this fee is counted under';

--
-- Name: court_fee_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.court_fee_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: court_fee_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.court_fee_id_seq OWNED BY public.court_fees.court_fee_id;

--
-- Name: court_fees_received; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.court_fees_received (
    court_fee_received_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    court_fee_id bigint,
    overpayment boolean NOT NULL,
    suspense_transaction_id bigint,
    transferred_date timestamp without time zone,
    number_of_items smallint NOT NULL,
    received_date timestamp without time zone
);

--
-- Name: COLUMN court_fees_received.court_fee_received_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.court_fee_received_id IS 'Unique ID of this record';

--
-- Name: COLUMN court_fees_received.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.business_unit_id IS 'ID of the business unit that charged this court fee';

--
-- Name: COLUMN court_fees_received.court_fee_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.court_fee_id IS 'ID of the court fee the payment is in regard of. Null for any amounts overpaid';

--
-- Name: COLUMN court_fees_received.overpayment; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.overpayment IS 'If this amount is a fee overpayment';

--
-- Name: COLUMN court_fees_received.suspense_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.suspense_transaction_id IS 'ID of ths suspense item created for this payment';

--
-- Name: COLUMN court_fees_received.transferred_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.transferred_date IS 'Date transferred to HMCTS';

--
-- Name: COLUMN court_fees_received.number_of_items; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.number_of_items IS 'Number of instances of the fee covered by this payment. 0 of this is an overpayment';

--
-- Name: COLUMN court_fees_received.received_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.court_fees_received.received_date IS 'Date the court fee was received';

--
-- Name: court_fee_received_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.court_fee_received_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: court_fee_received_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.court_fee_received_id_seq OWNED BY public.court_fees_received.court_fee_received_id;

--
-- Name: courts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.courts (
    court_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    court_code smallint NOT NULL,
    parent_court_id bigint,
    name character varying(35) NOT NULL,
    name_cy character varying(35),
    address_line_1 character varying(35),
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    address_line_1_cy character varying(35),
    address_line_2_cy character varying(35),
    address_line_3_cy character varying(35),
    postcode character varying(8),
    local_justice_area_id smallint,
    national_court_code character varying(7),
    gob_enforcing_court_code smallint,
    lja smallint,
    court_type character varying(2),
    division character varying(2),
    session character varying(2),
    start_time character varying(8),
    max_load bigint,
    record_session_times character varying(1),
    max_court_duration bigint,
    group_code character varying(24)
);

--
-- Name: COLUMN courts.court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.court_id IS 'Unique ID of this record';

--
-- Name: COLUMN courts.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.business_unit_id IS 'ID of the relating till to which this till belongs';

--
-- Name: COLUMN courts.court_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.court_code IS 'Court code unique within the business unit';

--
-- Name: COLUMN courts.parent_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.parent_court_id IS 'ID of parent court for enforcement/admin purposes';

--
-- Name: COLUMN courts.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.name IS 'Court name';

--
-- Name: COLUMN courts.name_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.name_cy IS 'Court name in welsh';

--
-- Name: COLUMN courts.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.address_line_1 IS 'Court address line 1';

--
-- Name: COLUMN courts.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.address_line_2 IS 'Court address line 2';

--
-- Name: COLUMN courts.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.address_line_3 IS 'Court address line 3, not stored in legacy GoB';

--
-- Name: COLUMN courts.address_line_1_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.address_line_1_cy IS 'Court address line 1 in welsh';

--
-- Name: COLUMN courts.address_line_2_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.address_line_2_cy IS 'Court address line 2 in welsh';

--
-- Name: COLUMN courts.address_line_3_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.address_line_3_cy IS 'Court address line 3 in welsh, not stored in legacy GoB';

--
-- Name: COLUMN courts.postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.postcode IS 'Court postcode, not stored in legacy GoB';

--
-- Name: COLUMN courts.local_justice_area_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.local_justice_area_id IS 'Local justice area ID';

--
-- Name: COLUMN courts.national_court_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.national_court_code IS 'National court location code (OU code). New field for future development with Common Platform';

--
-- Name: COLUMN courts.gob_enforcing_court_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.gob_enforcing_court_code IS 'The GoB enforcing court code';

--
-- Name: COLUMN courts.lja; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.lja IS 'GoB local justice area code';

--
-- Name: COLUMN courts.court_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.court_type IS 'GoB Court Type';

--
-- Name: COLUMN courts.division; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.division IS 'GoB Division';

--
-- Name: COLUMN courts.session; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.session IS 'AM or PM session';

--
-- Name: COLUMN courts.start_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.start_time IS 'The start time';

--
-- Name: COLUMN courts.max_load; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.max_load IS 'Maximum load';

--
-- Name: COLUMN courts.record_session_times; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.record_session_times IS 'Y or N whether session time is recorded';

--
-- Name: COLUMN courts.max_court_duration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.max_court_duration IS 'The maximum court duration';

--
-- Name: COLUMN courts.group_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.courts.group_code IS 'The group this court belongs to';

--
-- Name: court_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.court_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: court_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.court_id_seq OWNED BY public.courts.court_id;

--
-- Name: creditor_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.creditor_accounts (
    creditor_account_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    account_number character varying(20) NOT NULL,
    creditor_account_type public.t_creditor_account_type_enum NOT NULL,
    prosecution_service boolean NOT NULL,
    major_creditor_id bigint,
    minor_creditor_party_id bigint,
    from_suspense boolean NOT NULL,
    hold_payout boolean NOT NULL,
    pay_by_bacs boolean NOT NULL,
    bank_sort_code character varying(6),
    bank_account_number character varying(10),
    bank_account_name character varying(18),
    bank_account_reference character varying(18),
    bank_account_type character varying(1),
    last_changed_date timestamp without time zone,
    version_number bigint
);

--
-- Name: COLUMN creditor_accounts.creditor_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.creditor_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN creditor_accounts.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN creditor_accounts.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.account_number IS 'Account number unique within the business unit';

--
-- Name: COLUMN creditor_accounts.creditor_account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.creditor_account_type IS 'The type of creditor account. Values: MN (Minor Creditor), MJ (Major Creditor), CF (Central Fund).';

--
-- Name: COLUMN creditor_accounts.prosecution_service; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.prosecution_service IS 'Indicates a major creditor is the crown prosecution service';

--
-- Name: COLUMN creditor_accounts.major_creditor_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.major_creditor_id IS 'The major creditor that this account belongs to';

--
-- Name: COLUMN creditor_accounts.minor_creditor_party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.minor_creditor_party_id IS 'The person or organisation this account belongs to';

--
-- Name: COLUMN creditor_accounts.from_suspense; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.from_suspense IS 'If the creditor was created from a suspense transaction. If so, there will be no relating impositions for this creditor account.';

--
-- Name: COLUMN creditor_accounts.hold_payout; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.hold_payout IS 'If set, prevents paying out monies received to this account';

--
-- Name: COLUMN creditor_accounts.pay_by_bacs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.pay_by_bacs IS 'If the creditor is paid by BACS as opposed to cheque';

--
-- Name: COLUMN creditor_accounts.bank_sort_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.bank_sort_code IS 'Bank sort code for payments out';

--
-- Name: COLUMN creditor_accounts.bank_account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.bank_account_number IS 'Bank account number for payments out';

--
-- Name: COLUMN creditor_accounts.bank_account_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.bank_account_name IS 'Bank account name for payments out';

--
-- Name: COLUMN creditor_accounts.bank_account_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.bank_account_reference IS 'Bank account reference for payments out';

--
-- Name: COLUMN creditor_accounts.bank_account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.bank_account_type IS 'Bank account type number (0-5) for payments out';

--
-- Name: COLUMN creditor_accounts.last_changed_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.last_changed_date IS 'The date that the account or party was last changed in Account Maintenance.';

--
-- Name: COLUMN creditor_accounts.version_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_accounts.version_number IS 'Used to check that related items have not changed since retrieval and prior to being amended';

--
-- Name: creditor_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.creditor_account_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: creditor_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.creditor_account_id_seq OWNED BY public.creditor_accounts.creditor_account_id;

--
-- Name: creditor_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.creditor_transactions (
    creditor_transaction_id bigint NOT NULL,
    creditor_account_id bigint NOT NULL,
    posted_date timestamp without time zone NOT NULL,
    posted_by character varying(20),
    posted_by_name character varying(100),
    transaction_type public.t_creditor_transaction_type_enum NOT NULL,
    transaction_amount numeric(18,2) NOT NULL,
    imposition_result_id character varying(10),
    payment_processed boolean,
    payment_reference character varying(10),
    status public.t_creditor_transaction_status_enum,
    status_date timestamp without time zone,
    associated_record_type public.t_associated_record_type_enum,
    associated_record_id character varying(30)
);

--
-- Name: COLUMN creditor_transactions.creditor_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.creditor_transaction_id IS 'Unique ID of this record';

--
-- Name: COLUMN creditor_transactions.creditor_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.creditor_account_id IS 'ID of the creditor account this record belongs to';

--
-- Name: COLUMN creditor_transactions.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.posted_date IS 'The date the record was posted to the account';

--
-- Name: COLUMN creditor_transactions.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.posted_by IS 'The ID of the user that posted this transaction';

--
-- Name: COLUMN creditor_transactions.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.posted_by_name IS 'The name of the user that posted the transaction';

--
-- Name: COLUMN creditor_transactions.transaction_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.transaction_type IS 'The code that determines the type of transaction. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN creditor_transactions.transaction_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.transaction_amount IS 'Transaction amount';

--
-- Name: COLUMN creditor_transactions.imposition_result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.imposition_result_id IS 'The imposition result this transaction is in respect of';

--
-- Name: COLUMN creditor_transactions.payment_processed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.payment_processed IS 'Indicates if a transaction has been paid by payout or marked as processed by some internal process so payout will ignore it.';

--
-- Name: COLUMN creditor_transactions.payment_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.payment_reference IS 'The reference number of a cheque or bacs payment';

--
-- Name: COLUMN creditor_transactions.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.status IS 'Indicates if a transaction has been Reversed, partially-reversed, dishonoured, cancelled or cleared/presented. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN creditor_transactions.status_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.status_date IS 'Indicates the date the status was set, if known.';

--
-- Name: COLUMN creditor_transactions.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.associated_record_type IS 'Table where relating record that caused this amount is stored. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN creditor_transactions.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.creditor_transactions.associated_record_id IS 'ID or other reference/number of an associated record';

--
-- Name: creditor_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.creditor_transaction_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: creditor_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.creditor_transaction_id_seq OWNED BY public.creditor_transactions.creditor_transaction_id;

--
-- Name: debtor_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.debtor_detail (
    party_id bigint NOT NULL,
    vehicle_make character varying(30),
    vehicle_registration character varying(20),
    employer_name character varying(50),
    employer_address_line_1 character varying(35),
    employer_address_line_2 character varying(35),
    employer_address_line_3 character varying(35),
    employer_address_line_4 character varying(35),
    employer_address_line_5 character varying(35),
    employer_postcode character varying(10),
    employee_reference character varying(35),
    employer_telephone character varying(35),
    employer_email character varying(80),
    document_language public.t_language_enum,
    document_language_date timestamp without time zone,
    hearing_language public.t_language_enum,
    hearing_language_date timestamp without time zone
);

--
-- Name: COLUMN debtor_detail.party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.party_id IS 'Unique ID of this record';

--
-- Name: COLUMN debtor_detail.vehicle_make; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.vehicle_make IS 'Debtor asset vehicle make';

--
-- Name: COLUMN debtor_detail.vehicle_registration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.vehicle_registration IS 'Debtor asset vehicle registration';

--
-- Name: COLUMN debtor_detail.employer_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_name IS 'Employer name';

--
-- Name: COLUMN debtor_detail.employer_address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_address_line_1 IS 'Employer address line 1';

--
-- Name: COLUMN debtor_detail.employer_address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_address_line_2 IS 'Employer address line 2';

--
-- Name: COLUMN debtor_detail.employer_address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_address_line_3 IS 'Employer address line 3';

--
-- Name: COLUMN debtor_detail.employer_address_line_4; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_address_line_4 IS 'Employer address line 4';

--
-- Name: COLUMN debtor_detail.employer_address_line_5; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_address_line_5 IS 'Employer address line 5';

--
-- Name: COLUMN debtor_detail.employer_postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_postcode IS 'Employer postcode';

--
-- Name: COLUMN debtor_detail.employee_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employee_reference IS 'Employee reference number';

--
-- Name: COLUMN debtor_detail.employer_telephone; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_telephone IS 'Employer telephone number';

--
-- Name: COLUMN debtor_detail.employer_email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.employer_email IS 'Employer email address';

--
-- Name: COLUMN debtor_detail.document_language; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.document_language IS 'Document language preference (CY or EN). Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN debtor_detail.document_language_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.document_language_date IS 'Document language preference effective date';

--
-- Name: COLUMN debtor_detail.hearing_language; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.hearing_language IS 'Hearing language preference (CY or EN). Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN debtor_detail.hearing_language_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.debtor_detail.hearing_language_date IS 'Hearing language preference effective date';

--
-- Name: defendant_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.defendant_accounts (
    defendant_account_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    account_number character varying(20) NOT NULL,
    imposed_hearing_date timestamp without time zone,
    imposing_court_id bigint,
    amount_imposed numeric(18,2) NOT NULL,
    amount_paid numeric(18,2) NOT NULL,
    account_balance numeric(18,2) NOT NULL,
    account_status public.t_da_account_status_enum NOT NULL,
    completed_date timestamp without time zone,
    enforcing_court_id bigint,
    last_hearing_court_id bigint,
    last_hearing_date timestamp without time zone,
    last_movement_date timestamp without time zone,
    last_changed_date timestamp without time zone,
    last_enforcement character varying(6),
    originator_name character varying(50),
    originator_type public.t_originator_type_enum,
    allow_writeoffs boolean,
    allow_cheques boolean,
    cheque_clearance_period smallint,
    credit_trans_clearance_period smallint,
    enf_override_result_id character varying(10),
    enf_override_enforcer_id bigint,
    enf_override_tfo_lja_id smallint,
    unit_fine_detail character varying(100),
    unit_fine_value numeric(18,2),
    collection_order boolean,
    collection_order_date timestamp without time zone,
    further_steps_notice_date timestamp without time zone,
    confiscation_order_date timestamp without time zone,
    fine_registration_date timestamp without time zone,
    suspended_committal_date timestamp without time zone,
    consolidated_account_type public.t_consolidated_account_type_enum,
    payment_card_requested boolean,
    payment_card_requested_date timestamp without time zone,
    payment_card_requested_by character varying(20),
    prosecutor_case_reference character varying(40),
    enforcement_case_status character varying(10),
    originator_id character varying(40),
    account_type public.t_da_account_type_enum NOT NULL,
    account_comments text,
    account_note_1 text,
    account_note_2 text,
    account_note_3 text,
    jail_days integer,
    version_number bigint,
    payment_card_requested_by_name character varying(100),
    imposed_by_name character varying(100)
);

--
-- Name: COLUMN defendant_accounts.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.defendant_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN defendant_accounts.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN defendant_accounts.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_number IS 'Account number unique within the business unit';

--
-- Name: COLUMN defendant_accounts.imposed_hearing_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.imposed_hearing_date IS 'Date the financial penalties were imposed by court';

--
-- Name: COLUMN defendant_accounts.imposing_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.imposing_court_id IS 'ID of the court that imposed the financial penalties';

--
-- Name: COLUMN defendant_accounts.amount_imposed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.amount_imposed IS 'The total amount of impositions against this account';

--
-- Name: COLUMN defendant_accounts.amount_paid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.amount_paid IS 'The total amount of payments received for this account';

--
-- Name: COLUMN defendant_accounts.account_balance; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_balance IS 'The balance outstanding on this account. Was not stored in Legacy GoB';

--
-- Name: COLUMN defendant_accounts.account_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_status IS 'The status of the account. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_accounts.completed_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.completed_date IS 'Date the account balance was cleared. Not set by GoB Consolidation as it duplicates the account elsewhere. Going forward new consolidation does not need to do this as it can link multiple accounts';

--
-- Name: COLUMN defendant_accounts.enforcing_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.enforcing_court_id IS 'ID of the court responsible for enforcing this account';

--
-- Name: COLUMN defendant_accounts.last_hearing_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.last_hearing_court_id IS 'ID of the court where the last hearing relating to this account was heard';

--
-- Name: COLUMN defendant_accounts.last_hearing_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.last_hearing_date IS 'The last date a case relating to this account was heard to court';

--
-- Name: COLUMN defendant_accounts.last_movement_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.last_movement_date IS 'The last date there was movement against this account. A movement is considered to be account creation, hearing validation, enforcement, or any change to the account status, paid amount of payment terms.';

--
-- Name: COLUMN defendant_accounts.last_changed_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.last_changed_date IS 'The date this account was last modified by Account Maintenance';

--
-- Name: COLUMN defendant_accounts.last_enforcement; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.last_enforcement IS 'The last (or currently in force) enforcement action on this account. Not necessarily the most recent enforcement as some do not update this.';

--
-- Name: COLUMN defendant_accounts.originator_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.originator_name IS 'The name of the court or system where the account came from';

--
-- Name: COLUMN defendant_accounts.originator_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.originator_type IS 'How the account originated. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_accounts.allow_writeoffs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.allow_writeoffs IS 'Whether cheque payments are accepted for this account';

--
-- Name: COLUMN defendant_accounts.allow_cheques; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.allow_cheques IS 'Whether cheque payments are accepted for this account';

--
-- Name: COLUMN defendant_accounts.cheque_clearance_period; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.cheque_clearance_period IS 'The number of days before cheque payments are considered cleared';

--
-- Name: COLUMN defendant_accounts.credit_trans_clearance_period; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.credit_trans_clearance_period IS 'The number of days before creditor transfer payments are considered cleared';

--
-- Name: COLUMN defendant_accounts.enf_override_result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.enf_override_result_id IS 'The enforcement result that will be applied if this account is enforcement by auto-enforcement';

--
-- Name: COLUMN defendant_accounts.enf_override_enforcer_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.enf_override_enforcer_id IS 'The enforcer that will be allocated to enforcement override result';

--
-- Name: COLUMN defendant_accounts.enf_override_tfo_lja_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.enf_override_tfo_lja_id IS 'The ID of the LJA the account will be transferred to if the override result is TFFOUT';

--
-- Name: COLUMN defendant_accounts.unit_fine_detail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.unit_fine_detail IS 'Unit fine calculation information used to calculate the fine amount';

--
-- Name: COLUMN defendant_accounts.unit_fine_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.unit_fine_value IS 'The unit value used in the fine amount calculation';

--
-- Name: COLUMN defendant_accounts.collection_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.collection_order IS 'Whether the account is a collection order';

--
-- Name: COLUMN defendant_accounts.collection_order_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.collection_order_date IS 'The date the collection order status last changed';

--
-- Name: COLUMN defendant_accounts.further_steps_notice_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.further_steps_notice_date IS 'The date a Further Steps Notice was first issued for this account';

--
-- Name: COLUMN defendant_accounts.confiscation_order_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.confiscation_order_date IS 'The date a Confiscation Order was first issued for this account';

--
-- Name: COLUMN defendant_accounts.fine_registration_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.fine_registration_date IS 'The date a Registration of Fine was first made for this account';

--
-- Name: COLUMN defendant_accounts.suspended_committal_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.suspended_committal_date IS 'Hearing date when a suspended committal (SC) enforcement result was applied to this account';

--
-- Name: COLUMN defendant_accounts.consolidated_account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.consolidated_account_type IS 'If the account has been subject to a consolidation. M for Master or C for Child. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_accounts.payment_card_requested; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.payment_card_requested IS 'Whether a payment card has been requested for this account';

--
-- Name: COLUMN defendant_accounts.payment_card_requested_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.payment_card_requested_date IS 'The date a payment card was last request for this account';

--
-- Name: COLUMN defendant_accounts.payment_card_requested_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.payment_card_requested_by IS 'The ID of the user that requested a payment card for this account';

--
-- Name: COLUMN defendant_accounts.prosecutor_case_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.prosecutor_case_reference IS 'The reference from the prosecuting authority';

--
-- Name: COLUMN defendant_accounts.enforcement_case_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.enforcement_case_status IS 'Status of an enforcement case creation request to Common Platform';

--
-- Name: COLUMN defendant_accounts.originator_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.originator_id IS 'ID of the Originator';

--
-- Name: COLUMN defendant_accounts.account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_type IS 'The type of the account. Specific values can be found in the DB LLD on Confluence. One of Fixed Penalty, Fine, Conditional Caution, Confiscation.';

--
-- Name: COLUMN defendant_accounts.account_comments; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_comments IS 'Holds comments for this account.';

--
-- Name: COLUMN defendant_accounts.account_note_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_note_1 IS 'First free text note for this account.';

--
-- Name: COLUMN defendant_accounts.account_note_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_note_2 IS 'Second free text note for this account.';

--
-- Name: COLUMN defendant_accounts.account_note_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.account_note_3 IS 'Third free text note for this account.';

--
-- Name: COLUMN defendant_accounts.jail_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.jail_days IS 'The number of days in jail the defendant will spend in default of payment.';

--
-- Name: COLUMN defendant_accounts.version_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.version_number IS 'Used to check that related items have not changed since retrieval and prior to being amended';

--
-- Name: COLUMN defendant_accounts.payment_card_requested_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.payment_card_requested_by_name IS 'Name value of the user that requested card from the AAD Access Token';

--
-- Name: COLUMN defendant_accounts.imposed_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_accounts.imposed_by_name IS 'Court or associated LJA that imposed the penalty against the Defendant account during account creation.';

--
-- Name: defendant_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.defendant_account_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: defendant_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.defendant_account_id_seq OWNED BY public.defendant_accounts.defendant_account_id;

--
-- Name: defendant_account_parties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.defendant_account_parties (
    defendant_account_party_id bigint NOT NULL,
    defendant_account_id bigint NOT NULL,
    party_id bigint NOT NULL,
    association_type public.t_association_type_enum NOT NULL,
    debtor boolean NOT NULL
);

--
-- Name: COLUMN defendant_account_parties.defendant_account_party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_account_parties.defendant_account_party_id IS 'Unique ID of this record';

--
-- Name: COLUMN defendant_account_parties.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_account_parties.defendant_account_id IS 'ID of the defendant account';

--
-- Name: COLUMN defendant_account_parties.party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_account_parties.party_id IS 'ID of the party associated ';

--
-- Name: COLUMN defendant_account_parties.association_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_account_parties.association_type IS 'The party''s association type to the defendant account (Defendant or Parent/Guardian)';

--
-- Name: COLUMN defendant_account_parties.debtor; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_account_parties.debtor IS 'If this party is responsible for paying the account';

--
-- Name: defendant_account_party_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.defendant_account_party_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: defendant_account_party_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.defendant_account_party_id_seq OWNED BY public.defendant_account_parties.defendant_account_party_id;

--
-- Name: defendant_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.defendant_transactions (
    defendant_transaction_id bigint NOT NULL,
    defendant_account_id bigint NOT NULL,
    posted_date timestamp without time zone NOT NULL,
    posted_by character varying(20),
    transaction_type public.t_defendant_transaction_type_enum,
    transaction_amount numeric(18,2),
    payment_method public.t_payment_method_enum,
    payment_reference character varying(10),
    text character varying(50),
    status public.t_defendant_transaction_status_enum,
    status_date timestamp without time zone,
    status_amount numeric(18,2),
    write_off_code public.t_write_off_code_enum,
    associated_record_type public.t_associated_record_type_enum,
    associated_record_id character varying(30),
    imposed_amount numeric(18,2),
    posted_by_name character varying(100)
);

--
-- Name: COLUMN defendant_transactions.defendant_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.defendant_transaction_id IS 'Unique ID of this record';

--
-- Name: COLUMN defendant_transactions.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.defendant_account_id IS 'ID of the account this record belongs to';

--
-- Name: COLUMN defendant_transactions.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.posted_date IS 'The date the record was posted to the account';

--
-- Name: COLUMN defendant_transactions.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.posted_by IS 'ID of user responsible for posting this record';

--
-- Name: COLUMN defendant_transactions.transaction_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.transaction_type IS 'The code that determines the type of transaction. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_transactions.transaction_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.transaction_amount IS 'Transaction amount';

--
-- Name: COLUMN defendant_transactions.payment_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.payment_method IS 'The method of paying. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_transactions.payment_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.payment_reference IS 'Cheque of bacs payment reference';

--
-- Name: COLUMN defendant_transactions.text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.text IS 'Other information associated with this transaction such as a reason for creating it or a third-party name.';

--
-- Name: COLUMN defendant_transactions.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.status IS 'Indicates the status of the transaction. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_transactions.status_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.status_date IS 'Indicates the date the status was set, if known.';

--
-- Name: COLUMN defendant_transactions.status_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.status_amount IS 'The applicable amount, if the status does not apply to the full transaction amount. For example, where this transaction has been partially reversed the amount, the amount reversed so far. Reversed should be TRUE if reversed_amount = amount.';

--
-- Name: COLUMN defendant_transactions.write_off_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.write_off_code IS 'Code of write-off category applicable. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_transactions.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.associated_record_type IS 'Type of record that is identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN defendant_transactions.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.associated_record_id IS 'ID or other reference/number of an associated record. This could be the ID of a suspense transaction if this transaction is a transfer to or from suspense (FR-SUS, REPSUS, XFER, MADJ), or the ID of the affected imposition if this is a Reversal (REVPAY) or (DISHCQ). a reference number of a cheque, or BACS payment which may be deleted before this transaction, or any other third party reference.';

--
-- Name: COLUMN defendant_transactions.imposed_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.imposed_amount IS 'Additional amount imposed by this transaction. Currently only applies to CONSOL.';

--
-- Name: COLUMN defendant_transactions.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.defendant_transactions.posted_by_name IS 'The name of the user that posted the transaction';

--
-- Name: defendant_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.defendant_transaction_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: defendant_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.defendant_transaction_id_seq OWNED BY public.defendant_transactions.defendant_transaction_id;

--
-- Name: document_instances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.document_instances (
    document_instance_id bigint NOT NULL,
    document_id character varying(10) NOT NULL,
    business_unit_id smallint NOT NULL,
    generated_date timestamp without time zone NOT NULL,
    generated_by character varying(20) NOT NULL,
    associated_record_type public.t_associated_record_type_enum NOT NULL,
    associated_record_id character varying(30) NOT NULL,
    status public.t_di_status_enum NOT NULL,
    printed_date timestamp without time zone,
    document_content xml
);

--
-- Name: COLUMN document_instances.document_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.document_instance_id IS 'Unique ID for this record';

--
-- Name: COLUMN document_instances.document_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.document_id IS 'ID of the report being generated';

--
-- Name: COLUMN document_instances.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.business_unit_id IS 'ID of the business unit this report instance was generated for';

--
-- Name: COLUMN document_instances.generated_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.generated_date IS 'The date the document was generated';

--
-- Name: COLUMN document_instances.generated_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.generated_by IS 'ID of the user that generated this instance of the document';

--
-- Name: COLUMN document_instances.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.associated_record_type IS 'Type of record identified by associated_record_id. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN document_instances.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.status IS 'The status of the document instance. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN document_instances.document_content; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.document_instances.document_content IS 'The structured document content';

--
-- Name: document_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.document_instance_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: document_instance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.document_instance_id_seq OWNED BY public.document_instances.document_instance_id;

--
-- Name: documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.documents (
    document_id character varying(12) NOT NULL,
    recipient public.t_recipient_enum NOT NULL,
    document_language public.t_language_enum NOT NULL,
    priority public.t_priority_enum NOT NULL,
    header_type public.t_header_type_enum,
    signature_source public.t_signature_source_enum,
    document_template character varying(30),
    document_elements json,
    print_parameters json
);

--
-- Name: COLUMN documents.document_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.document_id IS 'Unique ID of this record';

--
-- Name: COLUMN documents.recipient; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.recipient IS 'The type of party that this document will be addressed to. Specific values can be found in the DB LLD on Confluence';

--
-- Name: COLUMN documents.document_language; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.document_language IS 'The language the document is written in. Specific values can be found in the DB LLD on Confluence';

--
-- Name: COLUMN documents.priority; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.priority IS 'Determines the order of printing with respect to other documents in the same batch. Specific values can be found in the DB LLD on Confluence';

--
-- Name: COLUMN documents.header_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.header_type IS 'The type of header output on the document. Specific values can be found in the DB LLD on Confluence';

--
-- Name: COLUMN documents.signature_source; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.signature_source IS 'Source of the document signature. Specific values can be found in the DB LLD on Confluence';

--
-- Name: COLUMN documents.document_elements; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.documents.document_elements IS 'Details of the structured data items to be included in the document content';

--
-- Name: draft_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.draft_accounts (
    draft_account_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    created_date timestamp without time zone NOT NULL,
    submitted_by character varying(20) NOT NULL,
    validated_date timestamp without time zone,
    validated_by character varying(20),
    account json NOT NULL,
    account_type public.t_da_account_type_enum NOT NULL,
    account_id bigint,
    account_snapshot json,
    account_status public.t_dra_account_status_enum,
    timeline_data json,
    account_number character varying(25),
    submitted_by_name character varying(100) NOT NULL,
    account_status_date timestamp without time zone NOT NULL,
    status_message text,
    validated_by_name character varying(100),
    version_number bigint,
    CONSTRAINT dra_submitted_validated_different CHECK (((submitted_by IS NULL) OR (validated_by IS NULL) OR ((submitted_by)::text <> (validated_by)::text)))
);

--
-- Name: COLUMN draft_accounts.draft_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.draft_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN draft_accounts.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.business_unit_id IS 'ID of the business unit this record belongs to';

--
-- Name: COLUMN draft_accounts.created_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.created_date IS 'Date this record was first created (the created date is not updated by successive submits, only the submitted by)';

--
-- Name: COLUMN draft_accounts.submitted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.submitted_by IS 'ID of the user that last submitted this record for checking';

--
-- Name: COLUMN draft_accounts.validated_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.validated_date IS 'Date the draft account was validated';

--
-- Name: COLUMN draft_accounts.validated_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.validated_by IS 'ID of the user that validated the draft account';

--
-- Name: COLUMN draft_accounts.account; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account IS 'The structured account data';

--
-- Name: COLUMN draft_accounts.account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account_type IS 'Type of account. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN draft_accounts.account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account_id IS 'Account ID created on validation. In Opal mode it will hold the defendant_accounts.defendant_account_id value';

--
-- Name: COLUMN draft_accounts.account_snapshot; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account_snapshot IS 'Business data to identify the account';

--
-- Name: COLUMN draft_accounts.account_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account_status IS 'The status of the draft account. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN draft_accounts.timeline_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.timeline_data IS 'A timeline of when the account has undergone validation';

--
-- Name: COLUMN draft_accounts.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account_number IS 'The Opal Account Number (2char letter code+account number)';

--
-- Name: COLUMN draft_accounts.submitted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.submitted_by_name IS 'Name value of the submitting user from the AAD Access Token';

--
-- Name: COLUMN draft_accounts.account_status_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.account_status_date IS 'The date of update of account status';

--
-- Name: COLUMN draft_accounts.status_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.status_message IS 'Any system messages, warnings, related to the status';

--
-- Name: COLUMN draft_accounts.validated_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.validated_by_name IS 'Name value of the validating user from the AAD Access Token';

--
-- Name: COLUMN draft_accounts.version_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.draft_accounts.version_number IS 'To be used to hold versions of row data as they change to help in the locking mechanism if adopted.';

--
-- Name: draft_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.draft_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: draft_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.draft_account_id_seq OWNED BY public.draft_accounts.draft_account_id;

--
-- Name: enforcement_account_types; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcement_account_types (
    enforcement_account_type_id bigint NOT NULL,
    account_type public.t_account_type_enum NOT NULL,
    minimum_balance bigint,
    enforcement_account_type public.t_enforcement_account_type_enum NOT NULL,
    account_type_path public.t_low_high_value_enum,
    version_number bigint
);

--
-- Name: COLUMN enforcement_account_types.enforcement_account_type_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_account_types.enforcement_account_type_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcement_account_types.account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_account_types.account_type IS 'The column is to hold only the values enumerated for the enforcement_account_types.account_type column, e.g. ''COL'', ''A'', ''CO'', ''Y'', and therefore a separate account_types table is not required.';

--
-- Name: COLUMN enforcement_account_types.minimum_balance; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_account_types.minimum_balance IS 'The minimum balance an account must have to be in this type';

--
-- Name: COLUMN enforcement_account_types.enforcement_account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_account_types.enforcement_account_type IS 'The column is to hold only the values uniquely enumerated for the enforcement_account_types.enforcement_account_type column, e.g. ''COLL'', ''COLH'', ''AL'', ''AH'', ''COL'', ''COH'', ''YL'', ''YH''.';

--
-- Name: COLUMN enforcement_account_types.account_type_path; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_account_types.account_type_path IS 'The column is to hold only the values enumerated for enforcement_account_types.account_type_path column, i.e. either ''L'' or ''H'' representing either a ''low'' or ''high'' account type value respectively.';

--
-- Name: COLUMN enforcement_account_types.version_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_account_types.version_number IS 'The column is to be used to check that related items have not changed since retrieval and prior to being amended.';

--
-- Name: enforcement_account_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcement_account_type_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcement_account_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcement_account_type_id_seq OWNED BY public.enforcement_account_types.enforcement_account_type_id;

--
-- Name: enforcements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcements (
    enforcement_id bigint NOT NULL,
    defendant_account_id bigint NOT NULL,
    posted_date timestamp without time zone NOT NULL,
    posted_by character varying(20),
    result_id character varying(6),
    reason character varying(50),
    enforcer_id bigint,
    jail_days integer,
    result_responses json,
    warrant_reference character varying(20),
    case_reference character varying(40),
    hearing_date timestamp without time zone,
    hearing_court_id bigint,
    posted_by_name character varying(100),
    earliest_release_date timestamp without time zone,
    enforcement_account_type public.t_enforcement_account_type_enum
);

--
-- Name: COLUMN enforcements.enforcement_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.enforcement_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcements.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.defendant_account_id IS 'ID of the account this record belongs to';

--
-- Name: COLUMN enforcements.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.posted_date IS 'The date the record was posted to the account';

--
-- Name: COLUMN enforcements.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.posted_by IS 'ID of user responsible for posting this record';

--
-- Name: COLUMN enforcements.result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.result_id IS 'The ID of the result imposed by the court that determines the type of imposition';

--
-- Name: COLUMN enforcements.reason; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.reason IS 'The reason for this enforcement action';

--
-- Name: COLUMN enforcements.enforcer_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.enforcer_id IS 'The enforcer/process server for this enforcement action';

--
-- Name: COLUMN enforcements.jail_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.jail_days IS 'only applies to SC/CW';

--
-- Name: COLUMN enforcements.result_responses; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.result_responses IS 'Name value pairs for enforcement parameters';

--
-- Name: COLUMN enforcements.warrant_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.warrant_reference IS 'The reference number of the warrant generated from this action';

--
-- Name: COLUMN enforcements.case_reference; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.case_reference IS 'The reference number of the case generated from this action';

--
-- Name: COLUMN enforcements.hearing_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.hearing_date IS 'The hearing date of the case generated from this action';

--
-- Name: COLUMN enforcements.hearing_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.hearing_court_id IS 'The hearing court of the case generated from this action';

--
-- Name: COLUMN enforcements.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.posted_by_name IS 'The name of the user that posted the enforcement';

--
-- Name: COLUMN enforcements.earliest_release_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.earliest_release_date IS 'The earliest release date for a PRIS enforcement action';

--
-- Name: COLUMN enforcements.enforcement_account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcements.enforcement_account_type IS 'The column is to hold only the values uniquely enumerated for the enforcement_account_types.enforcement_account_type column, e.g. ''COLL'', ''COLH'', ''AL'', ''AH'', ''COL'', ''COH'', ''YL'', ''YH''.';

--
-- Name: enforcement_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcement_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcement_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcement_id_seq OWNED BY public.enforcements.enforcement_id;

--
-- Name: enforcement_paths; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcement_paths (
    enforcement_path_id bigint NOT NULL,
    enforcement_path_set_id bigint NOT NULL,
    enforcement_account_type_id bigint NOT NULL,
    missed_weeks smallint,
    missed_fortnights smallint,
    missed_months smallint,
    action_1_result_id character varying(6),
    action_2_result_id character varying(6),
    action_2_days smallint,
    action_3_result_id character varying(6),
    action_3_days smallint,
    action_4_result_id character varying(6),
    action_4_days smallint,
    hearing_days_from smallint,
    hearing_days_to smallint
);

--
-- Name: COLUMN enforcement_paths.enforcement_path_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.enforcement_path_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcement_paths.enforcement_path_set_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.enforcement_path_set_id IS 'ID of the path set this is part of';

--
-- Name: COLUMN enforcement_paths.enforcement_account_type_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.enforcement_account_type_id IS 'ID of the account type this path is for';

--
-- Name: COLUMN enforcement_paths.missed_weeks; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.missed_weeks IS 'Number of weeks of missed payments before enforcement';

--
-- Name: COLUMN enforcement_paths.missed_fortnights; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.missed_fortnights IS 'Number of fortnights of missed payments before enforcement';

--
-- Name: COLUMN enforcement_paths.missed_months; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.missed_months IS 'Number of months of missed payments before enforcement';

--
-- Name: COLUMN enforcement_paths.action_1_result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_1_result_id IS 'The first enforcement action to take when enforcing this account';

--
-- Name: COLUMN enforcement_paths.action_2_result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_2_result_id IS 'The enforcement action to apply if action 1 has been applied';

--
-- Name: COLUMN enforcement_paths.action_2_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_2_days IS 'Number of days after action 1 before action 2 can be applied';

--
-- Name: COLUMN enforcement_paths.action_3_result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_3_result_id IS 'The enforcement action to apply if action 2 has been applied';

--
-- Name: COLUMN enforcement_paths.action_3_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_3_days IS 'Number of days after action 2 before action 3 can be applied';

--
-- Name: COLUMN enforcement_paths.action_4_result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_4_result_id IS 'The enforcement action to apply if action 3 has been applied';

--
-- Name: COLUMN enforcement_paths.action_4_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.action_4_days IS 'Number of days after action 3 before action 4 can be applied';

--
-- Name: COLUMN enforcement_paths.hearing_days_from; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.hearing_days_from IS 'Minimum number of days before a new hearing can be scheduled';

--
-- Name: COLUMN enforcement_paths.hearing_days_to; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_paths.hearing_days_to IS 'Maximum number of days before a new hearing can be scheduled';

--
-- Name: enforcement_path_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcement_path_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcement_path_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcement_path_id_seq OWNED BY public.enforcement_paths.enforcement_path_id;

--
-- Name: enforcement_path_sets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcement_path_sets (
    enforcement_path_set_id bigint NOT NULL,
    description character varying(240),
    business_unit_id smallint NOT NULL
);

--
-- Name: COLUMN enforcement_path_sets.enforcement_path_set_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_path_sets.enforcement_path_set_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcement_path_sets.description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_path_sets.description IS 'A name for this set of account enforcement paths';

--
-- Name: COLUMN enforcement_path_sets.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_path_sets.business_unit_id IS 'ID of the business unit this account type belongs to';

--
-- Name: enforcement_path_set_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcement_path_set_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcement_path_set_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcement_path_set_id_seq OWNED BY public.enforcement_path_sets.enforcement_path_set_id;

--
-- Name: enforcement_run_courts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcement_run_courts (
    enforcement_run_court_id bigint NOT NULL,
    enforcement_run_id bigint NOT NULL,
    court_id bigint NOT NULL
);

--
-- Name: COLUMN enforcement_run_courts.enforcement_run_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_run_courts.enforcement_run_court_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcement_run_courts.enforcement_run_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_run_courts.enforcement_run_id IS 'ID of the enforcement run that includes the court';

--
-- Name: COLUMN enforcement_run_courts.court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_run_courts.court_id IS 'ID of the court for which accounts will be enforced';

--
-- Name: enforcement_run_court_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcement_run_court_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcement_run_court_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcement_run_court_id_seq OWNED BY public.enforcement_run_courts.enforcement_run_court_id;

--
-- Name: enforcement_runs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcement_runs (
    enforcement_run_id bigint NOT NULL,
    business_unit_id smallint,
    run_name character varying(30),
    frequency_period character varying(1),
    next_run_date timestamp without time zone,
    name_range_start character varying(20),
    name_range_end character varying(20)
);

--
-- Name: COLUMN enforcement_runs.enforcement_run_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.enforcement_run_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcement_runs.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.business_unit_id IS 'ID of the business unit this account type belongs to';

--
-- Name: COLUMN enforcement_runs.run_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.run_name IS 'The name for this run';

--
-- Name: COLUMN enforcement_runs.frequency_period; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.frequency_period IS 'How often the run will be initiated';

--
-- Name: COLUMN enforcement_runs.next_run_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.next_run_date IS 'The date the next run will initiated';

--
-- Name: COLUMN enforcement_runs.name_range_start; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.name_range_start IS 'The start of the range of debtor names to be enforced in this run';

--
-- Name: COLUMN enforcement_runs.name_range_end; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcement_runs.name_range_end IS 'The end of the range of debtor names to be enforced in this run';

--
-- Name: enforcement_run_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcement_run_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcement_run_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcement_run_id_seq OWNED BY public.enforcement_runs.enforcement_run_id;

--
-- Name: enforcer_allocations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcer_allocations (
    enforcer_allocation_id bigint NOT NULL,
    enforcer_id bigint NOT NULL,
    result_id character varying(6) NOT NULL,
    priority bigint,
    name_range_start character varying(1),
    name_range_end character varying(1),
    maximum_enforcements bigint
);

--
-- Name: COLUMN enforcer_allocations.enforcer_allocation_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.enforcer_allocation_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcer_allocations.enforcer_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.enforcer_id IS 'ID of the enforcer this allocation is for';

--
-- Name: COLUMN enforcer_allocations.result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.result_id IS 'ID of the result this allocation is for';

--
-- Name: COLUMN enforcer_allocations.priority; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.priority IS 'The order to allocate an action for this result with respect to other enforcers';

--
-- Name: COLUMN enforcer_allocations.name_range_start; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.name_range_start IS 'The start of the range of debtor names this allocation is for';

--
-- Name: COLUMN enforcer_allocations.name_range_end; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.name_range_end IS 'The end of the range of debtor names this allocation is for';

--
-- Name: COLUMN enforcer_allocations.maximum_enforcements; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcer_allocations.maximum_enforcements IS 'The maximum number of enforcements for this enforcer and result';

--
-- Name: enforcers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enforcers (
    enforcer_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    enforcer_code smallint NOT NULL,
    name character varying(35) NOT NULL,
    name_cy character varying(35),
    address_line_1 character varying(35),
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    address_line_1_cy character varying(35),
    address_line_2_cy character varying(35),
    address_line_3_cy character varying(35),
    postcode character varying(8),
    warrant_reference_sequence character varying(20),
    warrant_register_sequence integer
);

--
-- Name: COLUMN enforcers.enforcer_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.enforcer_id IS 'Unique ID of this record';

--
-- Name: COLUMN enforcers.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.business_unit_id IS 'ID of the relating till to which this till belongs';

--
-- Name: COLUMN enforcers.enforcer_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.enforcer_code IS 'Enforcer code unique within the business unit';

--
-- Name: COLUMN enforcers.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.name IS 'Enforcer name';

--
-- Name: COLUMN enforcers.name_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.name_cy IS 'Enforcer name in welsh';

--
-- Name: COLUMN enforcers.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.address_line_1 IS 'Enforcer address line 1';

--
-- Name: COLUMN enforcers.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.address_line_2 IS 'Enforcer address line 2';

--
-- Name: COLUMN enforcers.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.address_line_3 IS 'Enforcer address line 3';

--
-- Name: COLUMN enforcers.address_line_1_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.address_line_1_cy IS 'Enforcer address line 1 in welsh';

--
-- Name: COLUMN enforcers.address_line_2_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.address_line_2_cy IS 'Enforcer address line 2 in welsh';

--
-- Name: COLUMN enforcers.address_line_3_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.address_line_3_cy IS 'Enforcer address line 3 in welsh';

--
-- Name: COLUMN enforcers.postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.postcode IS 'Enforcer postcode';

--
-- Name: COLUMN enforcers.warrant_reference_sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.warrant_reference_sequence IS 'Last generated warrant reference for this enforcer';

--
-- Name: COLUMN enforcers.warrant_register_sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.enforcers.warrant_register_sequence IS 'Last generated warrant register serial number for this enforcer';

--
-- Name: enforcer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enforcer_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: enforcer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enforcer_id_seq OWNED BY public.enforcers.enforcer_id;

--
-- Name: error_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.error_messages (
    error_code character varying(25) NOT NULL,
    error_message character varying(1000) NOT NULL
);

--
-- Name: COLUMN error_messages.error_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.error_messages.error_code IS 'Unique ID of the user defined error message';

--
-- Name: COLUMN error_messages.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.error_messages.error_message IS 'Descriptive wording of the error message';

--
-- Name: fixed_penalty_offences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fixed_penalty_offences (
    defendant_account_id bigint NOT NULL,
    ticket_number character varying(120) NOT NULL,
    vehicle_registration character varying(10),
    offence_location character varying(30),
    notice_number character varying(10),
    issued_date date,
    licence_number character varying(20),
    vehicle_fixed_penalty boolean,
    offence_date date,
    offence_time character varying(5)
);

--
-- Name: COLUMN fixed_penalty_offences.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.defendant_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN fixed_penalty_offences.ticket_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.ticket_number IS 'Fixed penalty ticket number';

--
-- Name: COLUMN fixed_penalty_offences.vehicle_registration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.vehicle_registration IS 'Vehicle registration or NV for non-vehicle fixed penalties';

--
-- Name: COLUMN fixed_penalty_offences.offence_location; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.offence_location IS 'Place of offence';

--
-- Name: COLUMN fixed_penalty_offences.notice_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.notice_number IS 'Notice to owner/hirer number';

--
-- Name: COLUMN fixed_penalty_offences.issued_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.issued_date IS 'Date the ticket was issued';

--
-- Name: COLUMN fixed_penalty_offences.licence_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.licence_number IS 'The driver''s licence number';

--
-- Name: COLUMN fixed_penalty_offences.vehicle_fixed_penalty; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.vehicle_fixed_penalty IS 'A flag to indicate the type of fixed penalty offence, either: Vehicle or non-vehicle. If set then the FP is a vehicle FP. If null, then the FP is a non-vehicle FP';

--
-- Name: COLUMN fixed_penalty_offences.offence_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.offence_date IS 'Date of offence';

--
-- Name: COLUMN fixed_penalty_offences.offence_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.fixed_penalty_offences.offence_time IS 'Time of offence. Stored as a string, as entered (not converted to UTC): "HH:SS"';

--
-- Name: hmrc_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hmrc_requests (
    hmrc_request_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    uuid character varying(36) NOT NULL,
    requested_date timestamp without time zone NOT NULL,
    requested_by bigint NOT NULL,
    status character varying(20) NOT NULL,
    account_id bigint NOT NULL,
    forename character varying(50) NOT NULL,
    surname character varying(50) NOT NULL,
    ni_number character varying(12) NOT NULL,
    dob timestamp without time zone NOT NULL,
    last_enforcement character varying(24),
    response_date timestamp without time zone,
    response_data text,
    qa_report_data text
);

--
-- Name: COLUMN hmrc_requests.hmrc_request_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.hmrc_request_id IS 'Unique ID of this record';

--
-- Name: COLUMN hmrc_requests.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.business_unit_id IS 'ID of the business unit this account type belongs to';

--
-- Name: COLUMN hmrc_requests.uuid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.uuid IS 'Unique ID of the request message';

--
-- Name: COLUMN hmrc_requests.requested_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.requested_date IS 'Date the request was sent';

--
-- Name: COLUMN hmrc_requests.requested_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.requested_by IS 'Requested By';

--
-- Name: COLUMN hmrc_requests.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.status IS 'Request status';

--
-- Name: COLUMN hmrc_requests.account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.account_id IS 'ID of the account the request is for';

--
-- Name: COLUMN hmrc_requests.forename; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.forename IS 'Debtor''s forename';

--
-- Name: COLUMN hmrc_requests.surname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.surname IS 'Debtor''s surname';

--
-- Name: COLUMN hmrc_requests.ni_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.ni_number IS 'Debtor''s national insurance number';

--
-- Name: COLUMN hmrc_requests.dob; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.dob IS 'Debtor''s date of birth';

--
-- Name: COLUMN hmrc_requests.last_enforcement; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.last_enforcement IS 'Account last enforcement action at the time of the request';

--
-- Name: COLUMN hmrc_requests.response_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.response_date IS 'Date the response was received';

--
-- Name: COLUMN hmrc_requests.response_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.response_data IS 'Response data';

--
-- Name: COLUMN hmrc_requests.qa_report_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.hmrc_requests.qa_report_data IS 'Additional data in the response for output on the QA report';

--
-- Name: hmrc_request_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.hmrc_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: hmrc_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.hmrc_request_id_seq OWNED BY public.hmrc_requests.hmrc_request_id;

--
-- Name: imposition_category_item_number; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.imposition_category_item_number (
    imposition_category character varying(40) NOT NULL,
    item_number smallint NOT NULL
);

--
-- Name: COLUMN imposition_category_item_number.imposition_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.imposition_category_item_number.imposition_category IS 'Financial category that monies for imposition are reported under. The imposition Category currently found in the results table.';

--
-- Name: COLUMN imposition_category_item_number.item_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.imposition_category_item_number.item_number IS 'Control total item number.';

--
-- Name: impositions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.impositions (
    imposition_id bigint NOT NULL,
    defendant_account_id bigint NOT NULL,
    posted_date timestamp without time zone NOT NULL,
    posted_by character varying(20),
    posted_by_name character varying(100),
    original_posted_date timestamp without time zone,
    result_id character varying(6) NOT NULL,
    imposing_court_id bigint,
    imposed_date timestamp without time zone,
    imposed_amount numeric(18,2) NOT NULL,
    paid_amount numeric(18,2) NOT NULL,
    offence_id bigint,
    offence_title character varying(120),
    offence_code character varying(10),
    creditor_account_id bigint NOT NULL,
    unit_fine_adjusted boolean,
    unit_fine_units smallint,
    completed boolean,
    original_imposition_id bigint
);

--
-- Name: COLUMN impositions.imposition_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.imposition_id IS 'Unique ID of this record';

--
-- Name: COLUMN impositions.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.defendant_account_id IS 'ID of the defendant account this record belongs to';

--
-- Name: COLUMN impositions.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.posted_date IS 'The date the record was posted to the account';

--
-- Name: COLUMN impositions.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.posted_by IS 'ID of user responsible for posting this record';

--
-- Name: COLUMN impositions.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.posted_by_name IS 'The name of the user that posted the imposition';

--
-- Name: COLUMN impositions.original_posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.original_posted_date IS 'Posted date of the original imposition if this imposition is a duplicate of the original which was written off by legacy account consolidation';

--
-- Name: COLUMN impositions.result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.result_id IS 'The ID of the result imposed by the court that determines the type of imposition';

--
-- Name: COLUMN impositions.imposing_court_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.imposing_court_id IS 'The ID of the court that imposed this penalty';

--
-- Name: COLUMN impositions.imposed_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.imposed_date IS 'The date this financial penalty was imposed in a court hearing, or the Date of Offence for FP Tickets';

--
-- Name: COLUMN impositions.imposed_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.imposed_amount IS 'The amount imposed by court';

--
-- Name: COLUMN impositions.paid_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.paid_amount IS 'The amount paid so far';

--
-- Name: COLUMN impositions.offence_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.offence_id IS 'The offence for which this penalty was imposed';

--
-- Name: COLUMN impositions.offence_title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.offence_title IS 'Offence title where id unavailable (local offences TFO''d)';

--
-- Name: COLUMN impositions.offence_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.offence_code IS 'Offence code where id unavailable (local offences TFO''d)';

--
-- Name: COLUMN impositions.creditor_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.creditor_account_id IS 'ID of the creditor account to be allocated payments received against this imposition';

--
-- Name: COLUMN impositions.unit_fine_adjusted; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.unit_fine_adjusted IS 'Whether a "Unit Fine Adjustment under s.18(7) CJA 1991" was made';

--
-- Name: COLUMN impositions.unit_fine_units; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.unit_fine_units IS 'Number of units';

--
-- Name: COLUMN impositions.completed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.completed IS 'If the imposition has been paid in full';

--
-- Name: COLUMN impositions.original_imposition_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.impositions.original_imposition_id IS 'Populated when an account becomes a Master account (after consolidation), stores the original imposition ID from the child account(s) when it is recreated on the master account (FK to the original imposition).';

--
-- Name: imposition_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.imposition_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: imposition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.imposition_id_seq OWNED BY public.impositions.imposition_id;

--
-- Name: interface_files; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.interface_files (
    interface_file_id bigint NOT NULL,
    interface_job_id bigint NOT NULL,
    file_name character varying(200) NOT NULL,
    created_datetime timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    records json
);

--
-- Name: COLUMN interface_files.interface_file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_files.interface_file_id IS 'Primary key';

--
-- Name: COLUMN interface_files.interface_job_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_files.interface_job_id IS 'The job responsible for either creating this file (exports) or processing the file (imports)';

--
-- Name: COLUMN interface_files.file_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_files.file_name IS 'The name and location of the stored file';

--
-- Name: COLUMN interface_files.created_datetime; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_files.created_datetime IS 'The timestamp when this record was created';

--
-- Name: COLUMN interface_files.records; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_files.records IS 'An array of JSON objects each representing a record in the file';

--
-- Name: interface_file_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.interface_file_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: interface_file_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.interface_file_id_seq OWNED BY public.interface_files.interface_file_id;

--
-- Name: interface_jobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.interface_jobs (
    interface_job_id bigint NOT NULL,
    business_unit_id smallint,
    interface_name character varying(50) NOT NULL,
    status character varying(10) DEFAULT 'Created'::character varying NOT NULL,
    created_datetime timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    started_datetime timestamp without time zone,
    completed_datetime timestamp without time zone,
    CONSTRAINT interface_jobs_status_cc CHECK (((status)::text = ANY ((ARRAY['Created'::character varying, 'Written'::character varying, 'No data'::character varying, 'Completed'::character varying, 'Failed'::character varying])::text[])))
);

--
-- Name: COLUMN interface_jobs.interface_job_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_jobs.interface_job_id IS 'Primary key';

--
-- Name: COLUMN interface_jobs.interface_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_jobs.interface_name IS 'The name of the procedure that will process records for this job';

--
-- Name: COLUMN interface_jobs.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_jobs.status IS 'The status of this interface job (Created, Completed, Failed)';

--
-- Name: COLUMN interface_jobs.created_datetime; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_jobs.created_datetime IS 'The timestamp when this record was created';

--
-- Name: COLUMN interface_jobs.started_datetime; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_jobs.started_datetime IS 'The timestamp when the procedure started';

--
-- Name: COLUMN interface_jobs.completed_datetime; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_jobs.completed_datetime IS 'The timestamp when the procedure completed of failed';

--
-- Name: interface_job_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.interface_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: interface_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.interface_job_id_seq OWNED BY public.interface_jobs.interface_job_id;

--
-- Name: interface_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.interface_messages (
    interface_message_id bigint NOT NULL,
    interface_job_id bigint NOT NULL,
    interface_file_id bigint,
    message_type character varying(10) NOT NULL,
    message_text character varying(500) NOT NULL,
    record_index bigint,
    record_detail text,
    CONSTRAINT im_message_type_cc CHECK (((message_type)::text = ANY ((ARRAY['Error'::character varying, 'Exception'::character varying, 'Warning'::character varying, 'Info'::character varying])::text[])))
);

--
-- Name: COLUMN interface_messages.interface_message_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.interface_message_id IS 'Primary key';

--
-- Name: COLUMN interface_messages.interface_job_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.interface_job_id IS 'The job that created this message';

--
-- Name: COLUMN interface_messages.interface_file_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.interface_file_id IS 'The file (if any) being processed when this message was created';

--
-- Name: COLUMN interface_messages.message_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.message_type IS 'The type of message (Exception, Error, Warning, Info)';

--
-- Name: COLUMN interface_messages.message_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.message_text IS 'The message text to be displayed';

--
-- Name: COLUMN interface_messages.record_index; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.record_index IS 'The index of the record from the array being processed (if applicable) when this message was created';

--
-- Name: COLUMN interface_messages.record_detail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.interface_messages.record_detail IS 'The detail from the record used for display purposes';

--
-- Name: interface_message_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.interface_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: interface_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.interface_message_id_seq OWNED BY public.interface_messages.interface_message_id;

--
-- Name: local_justice_areas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.local_justice_areas (
    local_justice_area_id smallint NOT NULL,
    name character varying(100) NOT NULL,
    address_line_1 character varying(35) NOT NULL,
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    postcode character varying(8),
    lja_code character varying(4),
    address_line_4 character varying(35),
    address_line_5 character varying(35),
    end_date timestamp without time zone,
    lja_type public.t_lja_type_enum
);

--
-- Name: COLUMN local_justice_areas.local_justice_area_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.local_justice_area_id IS 'Unique ID of this record';

--
-- Name: COLUMN local_justice_areas.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.name IS 'LJA name';

--
-- Name: COLUMN local_justice_areas.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.address_line_1 IS 'LJA address line 1';

--
-- Name: COLUMN local_justice_areas.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.address_line_2 IS 'LJA address line 2';

--
-- Name: COLUMN local_justice_areas.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.address_line_3 IS 'LJA address line 3';

--
-- Name: COLUMN local_justice_areas.postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.postcode IS 'LJA postcode';

--
-- Name: COLUMN local_justice_areas.lja_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.lja_code IS 'LJA Code';

--
-- Name: COLUMN local_justice_areas.address_line_4; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.address_line_4 IS 'LJA Address line 4';

--
-- Name: COLUMN local_justice_areas.address_line_5; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.address_line_5 IS 'LJA Address line 5';

--
-- Name: COLUMN local_justice_areas.end_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.end_date IS 'The end date of the record';

--
-- Name: COLUMN local_justice_areas.lja_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.local_justice_areas.lja_type IS 'The LJA type. Specific values can be found in the DB LLD on Confluence';

--
-- Name: log_actions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.log_actions (
    log_action_id smallint NOT NULL,
    log_action_name character varying(200) NOT NULL
);

--
-- Name: COLUMN log_actions.log_action_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_actions.log_action_id IS 'Unique ID of this record';

--
-- Name: COLUMN log_actions.log_action_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_actions.log_action_name IS 'The description of actions that could give rise to log creation';

--
-- Name: log_action_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.log_action_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: log_action_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.log_action_id_seq OWNED BY public.log_actions.log_action_id;

--
-- Name: log_audit_details; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.log_audit_details (
    log_audit_detail_id bigint NOT NULL,
    user_id bigint NOT NULL,
    log_timestamp timestamp without time zone NOT NULL,
    log_action_id smallint NOT NULL,
    business_unit_id smallint,
    associated_record_type character varying(30),
    associated_record_id character varying(30),
    json_request text
);

--
-- Name: COLUMN log_audit_details.log_audit_detail_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.log_audit_detail_id IS 'Unique ID of this record';

--
-- Name: COLUMN log_audit_details.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.user_id IS 'The user whose actions led to the creation of this entry';

--
-- Name: COLUMN log_audit_details.log_timestamp; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.log_timestamp IS 'System timestamp at the time of this entry';

--
-- Name: COLUMN log_audit_details.log_action_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.log_action_id IS 'The action that led to the creation of this entry';

--
-- Name: COLUMN log_audit_details.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.business_unit_id IS 'The business unit if there is one';

--
-- Name: COLUMN log_audit_details.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.associated_record_type IS 'Type of record identified by associated_record_id. Could be transaction, account, suspense line etc';

--
-- Name: COLUMN log_audit_details.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.associated_record_id IS 'ID of the associated record. So ID of the record or transaction etc being logged';

--
-- Name: COLUMN log_audit_details.json_request; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.log_audit_details.json_request IS 'The REST request information received that initiated this action and written in a json format but stored as TEXT';

--
-- Name: log_audit_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.log_audit_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: log_audit_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.log_audit_detail_id_seq OWNED BY public.log_audit_details.log_audit_detail_id;

--
-- Name: major_creditors; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.major_creditors (
    major_creditor_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    major_creditor_code character varying(4),
    name character varying(35) NOT NULL,
    address_line_1 character varying(80),
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    postcode character varying(8),
    contact_name character varying(35),
    contact_telephone character varying(35),
    contact_email character varying(80)
);

--
-- Name: COLUMN major_creditors.major_creditor_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.major_creditor_id IS 'Unique ID of this record';

--
-- Name: COLUMN major_creditors.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.business_unit_id IS 'ID of the relating business unit to which this major creditor belongs';

--
-- Name: COLUMN major_creditors.major_creditor_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.major_creditor_code IS 'Major creditor code unique within the business unit';

--
-- Name: COLUMN major_creditors.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.name IS 'Major creditor name';

--
-- Name: COLUMN major_creditors.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.address_line_1 IS 'Major creditor address line 1';

--
-- Name: COLUMN major_creditors.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.address_line_2 IS 'Major creditor address line 2';

--
-- Name: COLUMN major_creditors.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.address_line_3 IS 'Major creditor address line 3';

--
-- Name: COLUMN major_creditors.postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.postcode IS 'Major creditor postcode';

--
-- Name: COLUMN major_creditors.contact_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.contact_name IS 'Holds a named individual';

--
-- Name: COLUMN major_creditors.contact_telephone; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.contact_telephone IS 'Holds the telephone number for the major_creditor';

--
-- Name: COLUMN major_creditors.contact_email; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.major_creditors.contact_email IS 'Holds the email address for the major_creditor';

--
-- Name: major_creditor_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.major_creditor_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: major_creditor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.major_creditor_id_seq OWNED BY public.major_creditors.major_creditor_id;

--
-- Name: message_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.message_log (
    message_log_id bigint NOT NULL,
    message_uuid character varying(50),
    created_date timestamp without time zone NOT NULL,
    procedure_name character varying(40),
    error_message text,
    additional_information text
);

--
-- Name: COLUMN message_log.message_log_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.message_log.message_log_id IS 'ID of the error message being logged';

--
-- Name: COLUMN message_log.message_uuid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.message_log.message_uuid IS 'The universally unique identifier that will tie the request sent from the frontend or backend to the error message being stored in the database. This can be displayed on the frontend for the user to pass on to Admin for investigation';

--
-- Name: COLUMN message_log.created_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.message_log.created_date IS 'Date the message was created in the table';

--
-- Name: COLUMN message_log.procedure_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.message_log.procedure_name IS 'If a database procedure or function was used then record the name';

--
-- Name: COLUMN message_log.error_message; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.message_log.error_message IS 'The error message that is being stored';

--
-- Name: COLUMN message_log.additional_information; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.message_log.additional_information IS 'Any information that could be helpful to investigate the cause of failure';

--
-- Name: message_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.message_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: message_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.message_log_id_seq OWNED BY public.message_log.message_log_id;

--
-- Name: mis_debtors; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mis_debtors (
    mis_debtor_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    debtor_name character varying(100) NOT NULL,
    account_category character varying(1),
    arrears_category character varying(1),
    account_number character varying(36) NOT NULL,
    account_start_date timestamp without time zone NOT NULL,
    terms_type character varying(1) NOT NULL,
    instalment_amount numeric(18,2),
    lump_sum numeric(18,2),
    terms_date timestamp without time zone,
    days_in_jail smallint,
    date_last_movement timestamp without time zone,
    last_enforcement character varying(6),
    arrears numeric(18,2),
    amount_imposed numeric(18,2) NOT NULL,
    amount_paid numeric(18,2) NOT NULL,
    amount_outstanding numeric(18,2) NOT NULL
);

--
-- Name: COLUMN mis_debtors.mis_debtor_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.mis_debtor_id IS 'Unique ID of this record';

--
-- Name: COLUMN mis_debtors.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.business_unit_id IS 'ID of the relating till to which this till belongs';

--
-- Name: COLUMN mis_debtors.debtor_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.debtor_name IS 'Debtor full name';

--
-- Name: COLUMN mis_debtors.account_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.account_category IS 'Account category';

--
-- Name: COLUMN mis_debtors.arrears_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.arrears_category IS 'Arrears category';

--
-- Name: COLUMN mis_debtors.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.account_number IS 'Account number';

--
-- Name: COLUMN mis_debtors.account_start_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.account_start_date IS 'Account start date';

--
-- Name: COLUMN mis_debtors.terms_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.terms_type IS 'Terms type indicating if paying by a date or by instalments';

--
-- Name: COLUMN mis_debtors.instalment_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.instalment_amount IS 'Instalment amount if applicable';

--
-- Name: COLUMN mis_debtors.lump_sum; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.lump_sum IS 'Initial lump sum';

--
-- Name: COLUMN mis_debtors.terms_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.terms_date IS 'Pay-by date or instalments start date';

--
-- Name: COLUMN mis_debtors.days_in_jail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.days_in_jail IS 'Days in jail if in default';

--
-- Name: COLUMN mis_debtors.date_last_movement; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.date_last_movement IS 'Date of last movement on the account';

--
-- Name: COLUMN mis_debtors.last_enforcement; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.last_enforcement IS 'Last enforcement action';

--
-- Name: COLUMN mis_debtors.arrears; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.arrears IS 'arrears';

--
-- Name: COLUMN mis_debtors.amount_imposed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.amount_imposed IS 'Amount imposed';

--
-- Name: COLUMN mis_debtors.amount_paid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.amount_paid IS 'Amount paid so far';

--
-- Name: COLUMN mis_debtors.amount_outstanding; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.mis_debtors.amount_outstanding IS 'Amount still to pay';

--
-- Name: mis_debtor_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mis_debtor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: mis_debtor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.mis_debtor_id_seq OWNED BY public.mis_debtors.mis_debtor_id;

--
-- Name: miscellaneous_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.miscellaneous_accounts (
    miscellaneous_account_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    account_number character varying(20) NOT NULL,
    party_id bigint NOT NULL
);

--
-- Name: COLUMN miscellaneous_accounts.miscellaneous_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.miscellaneous_accounts.miscellaneous_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN miscellaneous_accounts.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.miscellaneous_accounts.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN miscellaneous_accounts.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.miscellaneous_accounts.account_number IS 'Account number unique within the business unit';

--
-- Name: COLUMN miscellaneous_accounts.party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.miscellaneous_accounts.party_id IS 'The person or organisation this account belongs to';

--
-- Name: miscellaneous_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.miscellaneous_account_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: miscellaneous_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.miscellaneous_account_id_seq OWNED BY public.miscellaneous_accounts.miscellaneous_account_id;

--
-- Name: notes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notes (
    note_id bigint NOT NULL,
    note_type public.t_note_type_enum NOT NULL,
    associated_record_type public.t_associated_record_type_enum NOT NULL,
    associated_record_id character varying(30) NOT NULL,
    note_text text NOT NULL,
    posted_date timestamp without time zone,
    posted_by character varying(20),
    posted_by_name character varying(100)
);

--
-- Name: COLUMN notes.note_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.note_id IS 'Unique ID of this record';

--
-- Name: COLUMN notes.note_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.note_type IS 'The type of note. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN notes.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.associated_record_type IS 'The type of record this note relates to. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN notes.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.associated_record_id IS 'ID of the record the notes apply to';

--
-- Name: COLUMN notes.note_text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.note_text IS 'Note text';

--
-- Name: COLUMN notes.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.posted_date IS 'The date the note was posted to the relating item. Not recorded for account comments oraccount free text notes';

--
-- Name: COLUMN notes.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.posted_by IS 'ID of the user that posted the note. Not recorded for account notes or suspense transaction notes';

--
-- Name: COLUMN notes.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.notes.posted_by_name IS 'The name of the user that posted the note';

--
-- Name: note_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.note_id_seq OWNED BY public.notes.note_id;

--
-- Name: offences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.offences (
    offence_id bigint NOT NULL,
    cjs_code character varying(10) NOT NULL,
    business_unit_id smallint,
    offence_title character varying(120),
    offence_title_cy character varying(120),
    date_used_to timestamp without time zone,
    offence_oas text,
    offence_oas_cy text,
    date_used_from timestamp without time zone
);

--
-- Name: COLUMN offences.offence_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.offence_id IS 'Unique ID of this record';

--
-- Name: COLUMN offences.cjs_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.cjs_code IS 'Offence cjs code';

--
-- Name: COLUMN offences.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.business_unit_id IS 'Indicates the area in which this is a local offence. NULL for national offences.';

--
-- Name: COLUMN offences.offence_title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.offence_title IS 'Offence title';

--
-- Name: COLUMN offences.offence_title_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.offence_title_cy IS 'Offence title in Welsh';

--
-- Name: COLUMN offences.date_used_to; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.date_used_to IS 'The date the offence was in use till. Null of a date in the future means still in use';

--
-- Name: COLUMN offences.offence_oas; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.offence_oas IS 'The English Offence Act and Section/Legislation';

--
-- Name: COLUMN offences.offence_oas_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.offence_oas_cy IS 'The Welsh Offence Act and Section/Legislation';

--
-- Name: COLUMN offences.date_used_from; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.offences.date_used_from IS 'The date the offence was in use from.';

--
-- Name: parties; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parties (
    party_id bigint NOT NULL,
    organisation boolean,
    organisation_name character varying(80),
    surname character varying(50),
    forenames character varying(50),
    title character varying(20),
    address_line_1 character varying(35),
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    address_line_4 character varying(35),
    address_line_5 character varying(35),
    postcode character varying(10),
    account_type public.t_party_account_type_enum,
    birth_date timestamp without time zone,
    age smallint,
    national_insurance_number character varying(10),
    telephone_home character varying(35),
    telephone_business character varying(35),
    telephone_mobile character varying(35),
    email_1 character varying(80),
    email_2 character varying(80),
    last_changed_date timestamp without time zone
);

--
-- Name: COLUMN parties.party_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.party_id IS 'Unique ID of this record';

--
-- Name: COLUMN parties.organisation; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.organisation IS 'Indicates if this party is an organisation or person';

--
-- Name: COLUMN parties.organisation_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.organisation_name IS 'Organisation name. Null for persons.';

--
-- Name: COLUMN parties.surname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.surname IS 'Person surname. Null for organisations. This will be the full name for parent/guardians but can be modified once GoB is decommissioned';

--
-- Name: COLUMN parties.forenames; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.forenames IS 'Person forenames. Null for organisations.';

--
-- Name: COLUMN parties.title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.title IS 'Person title. Null for organisations.';

--
-- Name: COLUMN parties.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.address_line_1 IS 'Address line 1';

--
-- Name: COLUMN parties.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.address_line_2 IS 'Address line 2';

--
-- Name: COLUMN parties.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.address_line_3 IS 'Address line 3';

--
-- Name: COLUMN parties.address_line_4; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.address_line_4 IS 'Address line 4. New field to handle larger addresses to be used once GoB has been decommissioned';

--
-- Name: COLUMN parties.address_line_5; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.address_line_5 IS 'Address line 5. New field to handle larger addresses to be used once GoB has been decommissioned';

--
-- Name: COLUMN parties.postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.postcode IS 'Postcode';

--
-- Name: COLUMN parties.account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.account_type IS 'The account type that the party is associated. We shouldn''t merge parties of different account types. We don''t want someone to amend a creditor and it affect defendant accounts if they are also a debtor. A party should not exist if no accounts exist.';

--
-- Name: COLUMN parties.birth_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.birth_date IS 'Person date of birth (only applies to an account party)';

--
-- Name: COLUMN parties.age; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.age IS 'Person estimated if birth date not known (only applies to an account party)';

--
-- Name: COLUMN parties.national_insurance_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.national_insurance_number IS 'Person national insurance number (only applies to an account party)';

--
-- Name: COLUMN parties.telephone_home; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.telephone_home IS 'Home telephone number';

--
-- Name: COLUMN parties.telephone_business; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.telephone_business IS 'Business telephone number';

--
-- Name: COLUMN parties.telephone_mobile; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.telephone_mobile IS 'Mobile telephone number';

--
-- Name: COLUMN parties.email_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.email_1 IS 'Primary e-mail address';

--
-- Name: COLUMN parties.email_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.email_2 IS 'Secondary e-mail address';

--
-- Name: COLUMN parties.last_changed_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.parties.last_changed_date IS 'Date this party was last changed in Account Maintenance.';

--
-- Name: party_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.party_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: party_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.party_id_seq OWNED BY public.parties.party_id;

--
-- Name: payment_card_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_card_requests (
    defendant_account_id bigint NOT NULL
);

--
-- Name: COLUMN payment_card_requests.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_card_requests.defendant_account_id IS 'Primary key. Indicates a request has been made for this defendant account. Deleted once processed';

--
-- Name: payments_in; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments_in (
    payment_in_id bigint NOT NULL,
    till_id bigint NOT NULL,
    payment_amount numeric(18,2) NOT NULL,
    payment_date timestamp without time zone NOT NULL,
    payment_method character varying(2) NOT NULL,
    destination_type character varying(1) NOT NULL,
    allocation_type character varying(20),
    associated_record_type character varying(30),
    associated_record_id character varying(30),
    third_party_payer_name character varying(50),
    additional_information character varying(500),
    receipt boolean,
    allocated boolean,
    auto_payment boolean
);

--
-- Name: COLUMN payments_in.payment_in_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.payment_in_id IS 'Unique ID of this record';

--
-- Name: COLUMN payments_in.till_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.till_id IS 'ID of the relating till to which this till belongs';

--
-- Name: COLUMN payments_in.payment_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.payment_amount IS 'Amount paid';

--
-- Name: COLUMN payments_in.payment_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.payment_date IS 'Date payment received';

--
-- Name: COLUMN payments_in.payment_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.payment_method IS 'Payment method';

--
-- Name: COLUMN payments_in.destination_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.destination_type IS 'Allocation destination: F (fines), S (Suspense), C (Court Fee)';

--
-- Name: COLUMN payments_in.allocation_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.allocation_type IS 'Specific types for each allocation where an initial payment amount is split, for example, if an amount is overpaid.';

--
-- Name: COLUMN payments_in.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.associated_record_type IS 'Type of record identified by associated_record_id. This could be a suspense item, court fee, miscellaneous account, defendant account or party';

--
-- Name: COLUMN payments_in.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.associated_record_id IS 'ID or other reference/number of an associated record';

--
-- Name: COLUMN payments_in.third_party_payer_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.third_party_payer_name IS 'Name of payer if a third party';

--
-- Name: COLUMN payments_in.additional_information; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.additional_information IS 'Additional information stored against the payment';

--
-- Name: COLUMN payments_in.receipt; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.receipt IS 'If a receipt was requested';

--
-- Name: COLUMN payments_in.allocated; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payments_in.allocated IS 'If this payment has been allocation';

--
-- Name: payment_in_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_in_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: payment_in_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_in_id_seq OWNED BY public.payments_in.payment_in_id;

--
-- Name: payment_terms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_terms (
    payment_terms_id bigint NOT NULL,
    defendant_account_id bigint NOT NULL,
    posted_date timestamp without time zone NOT NULL,
    posted_by character varying(20),
    terms_type_code public.t_terms_type_code_enum NOT NULL,
    effective_date timestamp without time zone,
    instalment_period public.t_instalment_period_enum,
    instalment_amount numeric(18,2),
    instalment_lump_sum numeric(18,2),
    jail_days integer,
    extension boolean,
    account_balance numeric(18,2),
    posted_by_name character varying(100),
    active boolean DEFAULT false NOT NULL,
    reason_for_extension text
);

--
-- Name: COLUMN payment_terms.payment_terms_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.payment_terms_id IS 'Unique ID of this record';

--
-- Name: COLUMN payment_terms.defendant_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.defendant_account_id IS 'ID of the account this record belongs to';

--
-- Name: COLUMN payment_terms.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.posted_date IS 'The date the record was posted to the account';

--
-- Name: COLUMN payment_terms.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.posted_by IS 'ID of user responsible for posting this record';

--
-- Name: COLUMN payment_terms.terms_type_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.terms_type_code IS 'The terms type. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN payment_terms.effective_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.effective_date IS 'the date when the full amount is due or when instalments start';

--
-- Name: COLUMN payment_terms.instalment_period; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.instalment_period IS 'The instalment period or NULL if not instalments. Specific values can be found in the DB LLD on Confluence.';

--
-- Name: COLUMN payment_terms.instalment_amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.instalment_amount IS 'Amount due each period if paying by instalments';

--
-- Name: COLUMN payment_terms.instalment_lump_sum; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.instalment_lump_sum IS 'An Initial lumpsum that is due before instalments start';

--
-- Name: COLUMN payment_terms.jail_days; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.jail_days IS 'Number of days in jail the defendant will spend in default of payment';

--
-- Name: COLUMN payment_terms.extension; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.extension IS 'If this is an extension to existing payment terms';

--
-- Name: COLUMN payment_terms.account_balance; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.account_balance IS 'Account balance at the time of posting thes terms';

--
-- Name: COLUMN payment_terms.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.posted_by_name IS 'The name of the user that posted the payment term';

--
-- Name: COLUMN payment_terms.active; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.active IS 'Indicates the active payment term for the defendant account';

--
-- Name: COLUMN payment_terms.reason_for_extension; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_terms.reason_for_extension IS 'User entered value when extending payment terms';

--
-- Name: payment_terms_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_terms_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: payment_terms_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_terms_id_seq OWNED BY public.payment_terms.payment_terms_id;

--
-- Name: print_definition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.print_definition (
    print_definition_id bigint NOT NULL,
    doc_type character varying(50) NOT NULL,
    doc_description character varying(255) NOT NULL,
    dest_main character varying(20) NOT NULL,
    dest_sec1 character varying(20),
    dest_sec2 character varying(20),
    format character varying(10) NOT NULL,
    auto_mode character varying(10),
    expiry_duration bigint NOT NULL,
    system character varying(10) NOT NULL,
    created_date timestamp without time zone,
    template_id character varying(20),
    address_val_element character varying(50),
    doc_doc_id bigint,
    xslt text NOT NULL,
    linked_areas character varying(150),
    template_file character varying(150)
);

--
-- Name: COLUMN print_definition.print_definition_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.print_definition_id IS 'Primary key created from a sequence';

--
-- Name: COLUMN print_definition.doc_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.doc_type IS 'The document type (e.g. courtreg_unv)';

--
-- Name: COLUMN print_definition.doc_description; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.doc_description IS 'A descriptive name of the document';

--
-- Name: COLUMN print_definition.dest_main; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.dest_main IS 'Is the main destination output';

--
-- Name: COLUMN print_definition.dest_sec1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.dest_sec1 IS 'Is a secondary output destination';

--
-- Name: COLUMN print_definition.dest_sec2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.dest_sec2 IS 'Is a secondary output destination';

--
-- Name: COLUMN print_definition.format; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.format IS 'Is the Apache FOP renderer to use (e.g. PDF, PS, XML)';

--
-- Name: COLUMN print_definition.auto_mode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.auto_mode IS 'Used by the Portal for printing';

--
-- Name: COLUMN print_definition.expiry_duration; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.expiry_duration IS 'The duration before the template expires';

--
-- Name: COLUMN print_definition.system; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.system IS 'Document relates to (e.g. Libra = L)';

--
-- Name: COLUMN print_definition.created_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.created_date IS 'The date / time the record is created';

--
-- Name: COLUMN print_definition.template_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.template_id IS 'The Libra Template Identifier';

--
-- Name: COLUMN print_definition.address_val_element; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.address_val_element IS 'The Libra Template Identifier';

--
-- Name: COLUMN print_definition.doc_doc_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.doc_doc_id IS 'FK to Libra Document Definition';

--
-- Name: COLUMN print_definition.xslt; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.xslt IS 'The XSLT template file(contents)';

--
-- Name: COLUMN print_definition.linked_areas; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.linked_areas IS 'Areas as a list of separated values linked to this definition';

--
-- Name: COLUMN print_definition.template_file; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_definition.template_file IS 'The XSLT template file name';

--
-- Name: print_definition_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.print_definition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: print_definition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.print_definition_id_seq OWNED BY public.print_definition.print_definition_id;

--
-- Name: print_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.print_job (
    print_job_id bigint NOT NULL,
    batch_uuid uuid NOT NULL,
    job_uuid uuid NOT NULL,
    xml_data text NOT NULL,
    doc_type character varying(50) NOT NULL,
    doc_version character varying(20) NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT print_job_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'FAILED'::character varying])::text[])))
);

--
-- Name: print_job_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.print_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: print_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.print_job_id_seq OWNED BY public.print_job.print_job_id;

--
-- Name: print_performance_monitor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.print_performance_monitor (
    print_performance_monitor_id bigint NOT NULL,
    uuid character varying(360) NOT NULL,
    document_type character varying(100) NOT NULL,
    date_rendered timestamp without time zone,
    time_rendered bigint NOT NULL,
    render_size bigint NOT NULL,
    render_server character varying(60) NOT NULL,
    used_memory bigint NOT NULL,
    free_memory bigint NOT NULL,
    total_memory bigint NOT NULL,
    max_memory bigint NOT NULL
);

--
-- Name: COLUMN print_performance_monitor.print_performance_monitor_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.print_performance_monitor_id IS 'Sequence generated primary key';

--
-- Name: COLUMN print_performance_monitor.uuid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.uuid IS 'The UUID';

--
-- Name: COLUMN print_performance_monitor.document_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.document_type IS 'The Document Type';

--
-- Name: COLUMN print_performance_monitor.date_rendered; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.date_rendered IS 'Date PDF was rendered';

--
-- Name: COLUMN print_performance_monitor.time_rendered; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.time_rendered IS 'Time taken to render the PDF';

--
-- Name: COLUMN print_performance_monitor.render_size; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.render_size IS 'Size of the render';

--
-- Name: COLUMN print_performance_monitor.render_server; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.render_server IS 'The server used to render the request';

--
-- Name: COLUMN print_performance_monitor.used_memory; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.used_memory IS 'The amount of memory used';

--
-- Name: COLUMN print_performance_monitor.free_memory; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.free_memory IS 'Amount of free memory';

--
-- Name: COLUMN print_performance_monitor.total_memory; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.total_memory IS 'Total memory used';

--
-- Name: COLUMN print_performance_monitor.max_memory; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.print_performance_monitor.max_memory IS 'The maximum amount of memory available';

--
-- Name: print_performance_monitor_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.print_performance_monitor_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: print_performance_monitor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.print_performance_monitor_id_seq OWNED BY public.print_performance_monitor.print_performance_monitor_id;

--
-- Name: prisons; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.prisons (
    prison_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    prison_code character varying(4) NOT NULL,
    name character varying(35) NOT NULL,
    address_line_1 character varying(35) NOT NULL,
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    postcode character varying(8)
);

--
-- Name: COLUMN prisons.prison_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.prison_id IS 'Unique ID of this record';

--
-- Name: COLUMN prisons.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.business_unit_id IS 'ID of the relating till to which this till belongs';

--
-- Name: COLUMN prisons.prison_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.prison_code IS 'Prison code unique within the business unit';

--
-- Name: COLUMN prisons.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.name IS 'Prison name';

--
-- Name: COLUMN prisons.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.address_line_1 IS 'Prison address line 1';

--
-- Name: COLUMN prisons.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.address_line_2 IS 'Prison address line 2';

--
-- Name: COLUMN prisons.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.address_line_3 IS 'Prison address line 3';

--
-- Name: COLUMN prisons.postcode; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prisons.postcode IS 'Prison postcode';

--
-- Name: prison_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.prison_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: prison_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.prison_id_seq OWNED BY public.prisons.prison_id;

--
-- Name: prosecutors; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.prosecutors (
    prosecutor_id smallint NOT NULL,
    name character varying(200) NOT NULL,
    prosecutor_code character varying(4),
    address_line_1 character varying(60),
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    address_line_4 character varying(35),
    address_line_5 character varying(35),
    postcode character varying(8),
    end_date timestamp without time zone
);

--
-- Name: COLUMN prosecutors.prosecutor_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.prosecutor_id IS 'Unique ID of this record';

--
-- Name: COLUMN prosecutors.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.name IS 'Name of the designated prosecutor';

--
-- Name: COLUMN prosecutors.prosecutor_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.prosecutor_code IS 'Code of the designated prosecutor';

--
-- Name: COLUMN prosecutors.address_line_1; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.address_line_1 IS 'Address line 1';

--
-- Name: COLUMN prosecutors.address_line_2; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.address_line_2 IS 'Address line 2';

--
-- Name: COLUMN prosecutors.address_line_3; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.address_line_3 IS 'Address line 3';

--
-- Name: COLUMN prosecutors.address_line_4; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.address_line_4 IS 'Address line 4';

--
-- Name: COLUMN prosecutors.address_line_5; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.address_line_5 IS 'Address line 5';

--
-- Name: COLUMN prosecutors.end_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.prosecutors.end_date IS 'Last date that this designated prosecutor should be used within Opal';

--
-- Name: report_entries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report_entries (
    report_entry_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    report_id character varying(30) NOT NULL,
    entry_timestamp timestamp without time zone NOT NULL,
    reported_timestamp timestamp without time zone,
    associated_record_type character varying(30) NOT NULL,
    associated_record_id character varying(30) NOT NULL,
    report_instance_id bigint
);

--
-- Name: COLUMN report_entries.report_entry_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.report_entry_id IS 'Unique ID for this record';

--
-- Name: COLUMN report_entries.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.business_unit_id IS 'ID of the business unit';

--
-- Name: COLUMN report_entries.report_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.report_id IS 'The report that this record should be included on';

--
-- Name: COLUMN report_entries.entry_timestamp; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.entry_timestamp IS 'Timestamp when this entry was created';

--
-- Name: COLUMN report_entries.reported_timestamp; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.reported_timestamp IS 'Timestamp when this entry was added to a report';

--
-- Name: COLUMN report_entries.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.associated_record_type IS 'Type of record identified by associated_record_id';

--
-- Name: COLUMN report_entries.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_entries.associated_record_id IS 'ID of the associated record';

--
-- Name: report_entry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.report_entry_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: report_entry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.report_entry_id_seq OWNED BY public.report_entries.report_entry_id;

--
-- Name: report_instances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report_instances (
    report_instance_id bigint NOT NULL,
    report_id character varying(30) NOT NULL,
    business_unit_id smallint[],
    audit_sequence bigint NOT NULL,
    created_timestamp timestamp without time zone,
    requested_by bigint NOT NULL,
    report_parameters json,
    location character varying(50),
    requested_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    generation_status public.ri_generation_status_enum NOT NULL,
    scheduled_deletion_timestamp timestamp without time zone,
    report_name character varying(250),
    no_of_records smallint,
    errors json,
    requested_by_name character varying(100) NOT NULL
);

--
-- Name: COLUMN report_instances.report_instance_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.report_instance_id IS 'Unique ID for this record';

--
-- Name: COLUMN report_instances.report_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.report_id IS 'ID of the report being generated';

--
-- Name: COLUMN report_instances.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.business_unit_id IS 'An array of business unit ids this report instance was generated for.';

--
-- Name: COLUMN report_instances.audit_sequence; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.audit_sequence IS 'The sequence_number of this report';

--
-- Name: COLUMN report_instances.created_timestamp; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.created_timestamp IS 'The timestamp the report instance was created.';

--
-- Name: COLUMN report_instances.requested_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.requested_by IS 'ID of the user that requested this report instance.';

--
-- Name: COLUMN report_instances.report_parameters; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.report_parameters IS 'The parameters used to generate the report';

--
-- Name: COLUMN report_instances.location; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.location IS 'The location the report data is stored at. Used if reports are stored outside of the database such as in a blob store.';

--
-- Name: COLUMN report_instances.requested_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.requested_at IS 'Indicates when the report was requested at. Default to the current timestamp.';

--
-- Name: COLUMN report_instances.generation_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.generation_status IS 'Value can be one of: REQUESTED - The report has been requested but has not yet started generation. IN_PROGRESS - The report is currently generating. READY - The report has generated successfully and is ready to be viewed. ERROR - The report has failed to generate errors can be seen in the error json field.';

--
-- Name: COLUMN report_instances.scheduled_deletion_timestamp; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.scheduled_deletion_timestamp IS 'Calculated using reports.retention_period when report is created.';

--
-- Name: COLUMN report_instances.report_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.report_name IS 'Introduced to cater for situations where a report could have subtypes (e.g. warrant register), or where a report instance has a name that has additional information (e.g. an enforcement operational report). Otherwise this will be the same as reports. report_title';

--
-- Name: COLUMN report_instances.no_of_records; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.no_of_records IS 'The number of records in the report. Used to show the No. of records value on the report summary screen.';

--
-- Name: COLUMN report_instances.errors; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.errors IS 'A list of errors that occurred when generating the report.';

--
-- Name: COLUMN report_instances.requested_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.report_instances.requested_by_name IS 'The name of the user who requested the report.';

--
-- Name: report_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.report_instance_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: report_instance_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.report_instance_id_seq OWNED BY public.report_instances.report_instance_id;

--
-- Name: reports; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reports (
    report_id character varying(30) NOT NULL,
    report_title character varying(50) NOT NULL,
    report_group character varying(50) NOT NULL,
    audited_report boolean NOT NULL,
    report_parameters json,
    supports_multi_bu boolean NOT NULL,
    is_bespoke_journey boolean NOT NULL,
    shown_as_worklist boolean NOT NULL,
    retention_period character varying(30),
    permission character varying(30),
    supported_file_types public.r_supported_file_type_enum[],
    can_manually_create boolean NOT NULL
);

--
-- Name: COLUMN reports.report_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.report_id IS 'Unique ID of this record';

--
-- Name: COLUMN reports.report_title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.report_title IS 'Report title, for e.g. List Monies Under Warrant';

--
-- Name: COLUMN reports.report_group; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.report_group IS 'The name of the group which this report is part of';

--
-- Name: COLUMN reports.audited_report; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.audited_report IS 'Whether this is an audited report or not. Each audited report for each business unit has a sequence that must not skip. Audited reports lock the last report generated for that report and business unit and generate a new one with the number in the sequence. Report instances for audited reports';

--
-- Name: COLUMN reports.report_parameters; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.report_parameters IS 'One of: A json array containing the generic parameters the report uses. Null if the report does not support generic parameters and must instead rely on a bespoke implementation. Usually when the report params are complex so they can not be represented in a generic fashion (e.g list fines)';

--
-- Name: COLUMN reports.supports_multi_bu; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.supports_multi_bu IS 'Whether the report can be run across multiple business units.';

--
-- Name: COLUMN reports.is_bespoke_journey; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.is_bespoke_journey IS 'Whether the report follows the standard journey, or needs bespoke screens (e.g. warrant register).';

--
-- Name: COLUMN reports.shown_as_worklist; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.shown_as_worklist IS 'Whether the report is shown as a worklist.';

--
-- Name: COLUMN reports.retention_period; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.retention_period IS 'An ISO 8601 duration indicating how long after creation of a report instance it should be deleted.';

--
-- Name: COLUMN reports.permission; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.permission IS 'The permission the user must have in order to use this report.';

--
-- Name: COLUMN reports.supported_file_types; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.supported_file_types IS 'An enum array supporting the following file types: CSV, PDF, XML.';

--
-- Name: COLUMN reports.can_manually_create; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.reports.can_manually_create IS 'Whether the user can manually create this report.';

--
-- Name: result_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.result_documents (
    result_document_id bigint NOT NULL,
    result_id character varying(6) NOT NULL,
    document_id character varying(12) NOT NULL,
    cy_document_id character varying(13)
);

--
-- Name: result_document_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.result_document_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: result_document_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.result_document_id_seq OWNED BY public.result_documents.result_document_id;

--
-- Name: results; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.results (
    result_id character varying(6) NOT NULL,
    result_title character varying(60) NOT NULL,
    result_title_cy character varying(60),
    result_type character varying(10) NOT NULL,
    active boolean NOT NULL,
    imposition boolean NOT NULL,
    imposition_category character varying(40),
    imposition_allocation_priority smallint,
    imposition_accruing boolean NOT NULL,
    imposition_creditor character varying(10),
    enforcement boolean NOT NULL,
    enforcement_override boolean NOT NULL,
    further_enforcement_warn boolean NOT NULL,
    further_enforcement_disallow boolean NOT NULL,
    enforcement_hold boolean NOT NULL,
    requires_enforcer boolean NOT NULL,
    generates_hearing boolean NOT NULL,
    generates_warrant boolean NOT NULL,
    collection_order boolean NOT NULL,
    extend_ttp_disallow boolean NOT NULL,
    extend_ttp_preserve_last_enf boolean NOT NULL,
    prevent_payment_card boolean NOT NULL,
    lists_monies boolean NOT NULL,
    result_parameters json,
    allow_payment_terms boolean,
    requires_employment_data boolean,
    allow_additional_action boolean,
    enf_next_permitted_actions character varying(100),
    requires_lja boolean,
    manual_enforcement boolean,
    CONSTRAINT results_imposition_category_cc CHECK (((imposition_category)::text = ANY ((ARRAY['Fines'::character varying, 'Court Charge'::character varying, 'Victim Surcharge'::character varying, ((('Witness Expenses '::text || chr(38)) || ' Central Fund'::text))::character varying, 'Crown Prosecution Costs'::character varying, 'Costs'::character varying, 'Compensation'::character varying, 'Legal Aid'::character varying])::text[]))),
    CONSTRAINT results_imposition_creditor_cc CHECK (((imposition_creditor)::text = ANY ((ARRAY['CF'::character varying, 'CPS'::character varying, '!CPS'::character varying, 'Any'::character varying])::text[]))),
    CONSTRAINT results_result_type_cc CHECK (((result_type)::text = ANY ((ARRAY['Result'::character varying, 'Action'::character varying])::text[])))
);

--
-- Name: COLUMN results.result_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.result_id IS 'Unique ID of this result';

--
-- Name: COLUMN results.result_title; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.result_title IS 'Result title';

--
-- Name: COLUMN results.result_title_cy; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.result_title_cy IS 'Result title';

--
-- Name: COLUMN results.result_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.result_type IS 'Indicates if this is an acutal result or just an action. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.active; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.active IS 'Indicates if this result can be applied to new accounts. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.imposition; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.imposition IS 'Indicates if this result creates an imposition. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.imposition_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.imposition_category IS 'Financial category that monies for this imposition are reported under. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.imposition_allocation_priority; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.imposition_allocation_priority IS 'Determines the order in which monies received are allocated to this impsition with respect to other impositions on the same account. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.imposition_accruing; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.imposition_accruing IS 'Indicates if this result is an imposition that accrues with time. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.imposition_creditor; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.imposition_creditor IS 'Indicates the creditor to be used for the imposition. Can be either Central (Central Fund Account), DPP (Crown Prosection Service), !DPP (a creditor other than DPP) or Any (any creditor). New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.enforcement; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.enforcement IS 'Indicates if this result is an enforcement result. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.enforcement_override; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.enforcement_override IS 'Indicates if this result can be used as an enforcement override. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.further_enforcement_warn; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.further_enforcement_warn IS 'Indicates if a warning should be issued when applying an enforcement action while this is the last enforcement on the account. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.further_enforcement_disallow; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.further_enforcement_disallow IS 'Indicates if the system should prevent applying an enforcement action while this is the last enforcement on the account. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.enforcement_hold; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.enforcement_hold IS 'Indicates if this action places a hold on enforcement which requires it to be explicity removed to continue further enforcement on the account. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.requires_enforcer; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.requires_enforcer IS 'Indicates if this result requires the user to also specify an enforcer. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.generates_hearing; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.generates_hearing IS 'Indicates if applying this action should attempt to schedule an enforcement hearing for the account debtor. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.collection_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.collection_order IS 'Indicates if this result is a collection order result. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.extend_ttp_disallow; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.extend_ttp_disallow IS 'Indicates if this result should prevent extension of payment terms. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.extend_ttp_preserve_last_enf; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.extend_ttp_preserve_last_enf IS 'Indicates if this should be preserved as the last enforcement on an account after extending payment instead of clearing it. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.prevent_payment_card; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.prevent_payment_card IS 'Indicates if this result should prevent requesting a payment card if it is the last enforcement on an account. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.lists_monies; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.lists_monies IS 'this result cause the account to be reported on List Monies Under Warrant if a payment is received while this is the last enforcement action on the account. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.result_parameters; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.result_parameters IS 'The parameters required to be input by the user when applyig this result. New field. Hard-coded in legacy GoB';

--
-- Name: COLUMN results.allow_payment_terms; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.allow_payment_terms IS 'Flag to state which enforcement actions allow the user to add/amend payment terms in the same journey as applying the action.';

--
-- Name: COLUMN results.requires_employment_data; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.requires_employment_data IS 'Flag to state that the enforcement action requires employment data to exist on the account in order to apply the action.';

--
-- Name: COLUMN results.allow_additional_action; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.allow_additional_action IS 'Flag to state which enforcement actions allow the user to add another enforcement action in the same journey as applying the action (WDN) or removing the action (NOENF).';

--
-- Name: COLUMN results.enf_next_permitted_actions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.enf_next_permitted_actions IS 'A comma separated list of permitted next actions of result_ids for each active manual enforcement action. If All then allow all result_ids.';

--
-- Name: COLUMN results.requires_lja; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.requires_lja IS 'Flag to state that the enforcement override requires an LJA to be selected.';

--
-- Name: COLUMN results.manual_enforcement; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.results.manual_enforcement IS 'Flag to state that the result can be used for manual enforcement';

--
-- Name: standard_letters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.standard_letters (
    standard_letter_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    standard_letter_code character varying(10) NOT NULL,
    standard_letter_name character varying(50) NOT NULL,
    associated_record_type character varying(30) NOT NULL,
    user_entries json,
    document_body text NOT NULL
);

--
-- Name: COLUMN standard_letters.standard_letter_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.standard_letter_id IS 'Unique ID of this record';

--
-- Name: COLUMN standard_letters.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.business_unit_id IS 'ID of the business unit that owns this record';

--
-- Name: COLUMN standard_letters.standard_letter_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.standard_letter_code IS 'Standard letter code unique within the business unit';

--
-- Name: COLUMN standard_letters.standard_letter_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.standard_letter_name IS 'Unique name for this Standard Letter';

--
-- Name: COLUMN standard_letters.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.associated_record_type IS 'The type of record for which this letter is generated (defendant_account or creditor_account)';

--
-- Name: COLUMN standard_letters.user_entries; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.user_entries IS 'Parameters required to be entered by the user when generating an instance of this letter';

--
-- Name: COLUMN standard_letters.document_body; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.standard_letters.document_body IS 'The document body including user parameter and formatting tags';

--
-- Name: standard_letter_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.standard_letter_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: standard_letter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.standard_letter_id_seq OWNED BY public.standard_letters.standard_letter_id;

--
-- Name: suspense_accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suspense_accounts (
    suspense_account_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    account_number character varying(20) NOT NULL
);

--
-- Name: COLUMN suspense_accounts.suspense_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_accounts.suspense_account_id IS 'Unique ID of this record';

--
-- Name: COLUMN suspense_accounts.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_accounts.business_unit_id IS 'ID of the business unit this account belongs to';

--
-- Name: COLUMN suspense_accounts.account_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_accounts.account_number IS 'Account number';

--
-- Name: suspense_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suspense_items (
    suspense_item_id bigint NOT NULL,
    suspense_account_id bigint NOT NULL,
    suspense_item_number smallint NOT NULL,
    suspense_item_type character varying(2) NOT NULL,
    created_date timestamp without time zone NOT NULL,
    payment_method character varying(2),
    court_fee_id bigint
);

--
-- Name: COLUMN suspense_items.suspense_item_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.suspense_item_id IS 'Unique ID of this record';

--
-- Name: COLUMN suspense_items.suspense_account_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.suspense_account_id IS 'Suspense account this item belongs to';

--
-- Name: COLUMN suspense_items.suspense_item_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.suspense_item_number IS 'Suspense item number unique within the business unit';

--
-- Name: COLUMN suspense_items.suspense_item_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.suspense_item_type IS 'Type of this suspense item';

--
-- Name: COLUMN suspense_items.created_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.created_date IS 'Date the suspense item was created';

--
-- Name: COLUMN suspense_items.payment_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.payment_method IS 'The method of payment';

--
-- Name: COLUMN suspense_items.court_fee_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_items.court_fee_id IS 'The associated court fee code id applicable';

--
-- Name: suspense_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.suspense_item_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: suspense_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.suspense_item_id_seq OWNED BY public.suspense_items.suspense_item_id;

--
-- Name: suspense_transactions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suspense_transactions (
    suspense_transaction_id bigint NOT NULL,
    suspense_item_id bigint NOT NULL,
    posted_date timestamp without time zone NOT NULL,
    posted_by character varying(20),
    posted_by_name character varying(100),
    transaction_type character varying(2) NOT NULL,
    amount numeric(18,2) NOT NULL,
    associated_record_type character varying(30),
    associated_record_id character varying(30),
    text character varying(50),
    reversed character varying(1)
);

--
-- Name: COLUMN suspense_transactions.suspense_transaction_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.suspense_transaction_id IS 'Unique ID of this record';

--
-- Name: COLUMN suspense_transactions.suspense_item_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.suspense_item_id IS 'The suspense item that this transaction belongs to';

--
-- Name: COLUMN suspense_transactions.posted_date; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.posted_date IS 'Date this transaction was posted';

--
-- Name: COLUMN suspense_transactions.posted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.posted_by IS 'ID of user that posted this transaction';

--
-- Name: COLUMN suspense_transactions.posted_by_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.posted_by_name IS 'The name of the user that posted the transaction';

--
-- Name: COLUMN suspense_transactions.transaction_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.transaction_type IS 'Suspense transaction type';

--
-- Name: COLUMN suspense_transactions.amount; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.amount IS 'Amount of this transaction';

--
-- Name: COLUMN suspense_transactions.associated_record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.associated_record_type IS 'Type of record identified by associated_record_id';

--
-- Name: COLUMN suspense_transactions.associated_record_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.associated_record_id IS 'ID or other reference/number of an associated record';

--
-- Name: COLUMN suspense_transactions.text; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.text IS 'Further detail associated with the transaction';

--
-- Name: COLUMN suspense_transactions.reversed; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.suspense_transactions.reversed IS 'If this transaction has subsequently been reversed (R) or dishonoured (D)';

--
-- Name: suspense_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.suspense_transaction_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: suspense_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.suspense_transaction_id_seq OWNED BY public.suspense_transactions.suspense_transaction_id;

--
-- Name: tills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tills (
    till_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    till_number smallint NOT NULL,
    owned_by character varying(20)
);

--
-- Name: COLUMN tills.till_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.tills.till_id IS 'Unique ID of this record';

--
-- Name: COLUMN tills.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.tills.business_unit_id IS 'ID of the relating business unit';

--
-- Name: COLUMN tills.till_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.tills.till_number IS 'Till number unique within the business unit';

--
-- Name: COLUMN tills.owned_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.tills.owned_by IS 'ID of the user that owns this till';

--
-- Name: till_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: till_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.till_id_seq OWNED BY public.tills.till_id;

--
-- Name: till_number_103_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_103_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_105_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_105_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_106_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_106_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_10_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_10_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_112_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_112_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_119_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_119_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_11_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_11_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_124_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_124_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_125_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_125_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_126_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_126_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_128_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_128_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_129_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_129_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_12_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_12_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_130_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_130_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_135_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_135_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_138_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_138_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_139_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_139_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_14_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_14_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_21_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_21_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_22_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_22_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_24_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_24_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_26_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_26_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_28_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_28_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_29_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_29_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_30_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_30_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_31_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_31_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_36_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_36_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_38_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_38_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_45_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_45_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_47_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_47_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_52_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_52_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_57_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_57_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_5_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_5_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_60_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_60_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_61_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_61_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_65_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_65_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_66_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_66_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_73_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_73_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_77_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_77_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_78_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_78_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_80_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_80_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_82_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_82_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_89_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_89_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_8_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_8_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_92_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_92_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_96_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_96_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_97_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_97_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_99_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_99_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: till_number_9_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.till_number_9_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    MAXVALUE 99999
    CACHE 1
    CYCLE;

--
-- Name: warrant_register; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.warrant_register (
    warrant_register_id bigint NOT NULL,
    business_unit_id smallint NOT NULL,
    enforcer_id bigint NOT NULL,
    enforcement_id bigint
);

--
-- Name: COLUMN warrant_register.warrant_register_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.warrant_register.warrant_register_id IS 'Unique ID of this record';

--
-- Name: COLUMN warrant_register.business_unit_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.warrant_register.business_unit_id IS 'ID of the relating till to which this till belongs';

--
-- Name: COLUMN warrant_register.enforcer_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.warrant_register.enforcer_id IS 'ID of the enforcer this warrant is allocated to';

--
-- Name: COLUMN warrant_register.enforcement_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.warrant_register.enforcement_id IS 'ID of the enforcement action that generated this warrant';

--
-- Name: warrant_register_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.warrant_register_id_seq
    START WITH 60000000000000
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    CACHE 1;

--
-- Name: warrant_register_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.warrant_register_id_seq OWNED BY public.warrant_register.warrant_register_id;

--
-- Name: print_job print_job_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_job ALTER COLUMN print_job_id SET DEFAULT nextval('public.print_job_id_seq'::regclass);

--
-- Name: result_documents result_document_id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.result_documents ALTER COLUMN result_document_id SET DEFAULT nextval('public.result_document_id_seq'::regclass);

--
-- Name: account_number_index account_number_index_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_number_index
    ADD CONSTRAINT account_number_index_pk PRIMARY KEY (account_number_index_id);

--
-- Name: account_transfers account_transfers_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_transfers
    ADD CONSTRAINT account_transfers_pk PRIMARY KEY (account_transfer_id);

--
-- Name: aliases aliases_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.aliases
    ADD CONSTRAINT aliases_pk PRIMARY KEY (alias_id);

--
-- Name: allocations allocations_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT allocations_pk PRIMARY KEY (allocation_id);

--
-- Name: amendments amendments_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.amendments
    ADD CONSTRAINT amendments_pk PRIMARY KEY (amendment_id);

--
-- Name: audit_amendment_fields audit_amendment_fields_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_amendment_fields
    ADD CONSTRAINT audit_amendment_fields_pk PRIMARY KEY (field_code);

--
-- Name: bacs_payments bacs_payments_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bacs_payments
    ADD CONSTRAINT bacs_payments_pk PRIMARY KEY (bacs_payment_id);

--
-- Name: business_units business_unit_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.business_units
    ADD CONSTRAINT business_unit_id_pk PRIMARY KEY (business_unit_id);

--
-- Name: cheques cheques_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cheques
    ADD CONSTRAINT cheques_pk PRIMARY KEY (cheque_id);

--
-- Name: committal_warrant_progress committal_warrant_progress_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.committal_warrant_progress
    ADD CONSTRAINT committal_warrant_progress_pk PRIMARY KEY (defendant_account_id);

--
-- Name: configuration_items configuration_items_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.configuration_items
    ADD CONSTRAINT configuration_items_pk PRIMARY KEY (configuration_item_id);

--
-- Name: control_totals control_totals_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.control_totals
    ADD CONSTRAINT control_totals_pk PRIMARY KEY (control_total_id);

--
-- Name: court_fees court_fees_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.court_fees
    ADD CONSTRAINT court_fees_pk PRIMARY KEY (court_fee_id);

--
-- Name: court_fees_received court_fees_received_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.court_fees_received
    ADD CONSTRAINT court_fees_received_pk PRIMARY KEY (court_fee_received_id);

--
-- Name: courts court_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courts
    ADD CONSTRAINT court_id_pk PRIMARY KEY (court_id);

--
-- Name: creditor_accounts creditor_accounts_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creditor_accounts
    ADD CONSTRAINT creditor_accounts_pk PRIMARY KEY (creditor_account_id);

--
-- Name: creditor_transactions creditor_transactions_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creditor_transactions
    ADD CONSTRAINT creditor_transactions_pk PRIMARY KEY (creditor_transaction_id);

--
-- Name: debtor_detail debtor_detail_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.debtor_detail
    ADD CONSTRAINT debtor_detail_pk PRIMARY KEY (party_id);

--
-- Name: defendant_accounts defendant_account_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT defendant_account_id_pk PRIMARY KEY (defendant_account_id);

--
-- Name: defendant_account_parties defendant_account_parties_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_account_parties
    ADD CONSTRAINT defendant_account_parties_pk PRIMARY KEY (defendant_account_party_id);

--
-- Name: defendant_transactions defendant_transactions_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_transactions
    ADD CONSTRAINT defendant_transactions_pk PRIMARY KEY (defendant_transaction_id);

--
-- Name: document_instances document_instances_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.document_instances
    ADD CONSTRAINT document_instances_pk PRIMARY KEY (document_instance_id);

--
-- Name: documents documents_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pk PRIMARY KEY (document_id);

--
-- Name: draft_accounts draft_accounts_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.draft_accounts
    ADD CONSTRAINT draft_accounts_pk PRIMARY KEY (draft_account_id);

--
-- Name: error_messages em_error_code_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.error_messages
    ADD CONSTRAINT em_error_code_pk PRIMARY KEY (error_code);

--
-- Name: enforcement_account_types enforcement_account_types_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_account_types
    ADD CONSTRAINT enforcement_account_types_pk PRIMARY KEY (enforcement_account_type_id);

--
-- Name: enforcement_path_sets enforcement_path_sets_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_path_sets
    ADD CONSTRAINT enforcement_path_sets_pk PRIMARY KEY (enforcement_path_set_id);

--
-- Name: enforcement_paths enforcement_paths_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_paths
    ADD CONSTRAINT enforcement_paths_pk PRIMARY KEY (enforcement_path_id);

--
-- Name: enforcement_run_courts enforcement_run_courts_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_run_courts
    ADD CONSTRAINT enforcement_run_courts_pk PRIMARY KEY (enforcement_run_court_id);

--
-- Name: enforcement_runs enforcement_runs_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_runs
    ADD CONSTRAINT enforcement_runs_pk PRIMARY KEY (enforcement_run_id);

--
-- Name: enforcements enforcements_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcements
    ADD CONSTRAINT enforcements_pk PRIMARY KEY (enforcement_id);

--
-- Name: enforcer_allocations enforcer_allocations_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcer_allocations
    ADD CONSTRAINT enforcer_allocations_pk PRIMARY KEY (enforcer_allocation_id);

--
-- Name: enforcers enforcer_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcers
    ADD CONSTRAINT enforcer_id_pk PRIMARY KEY (enforcer_id);

--
-- Name: fixed_penalty_offences fixed_penalty_offences_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_penalty_offences
    ADD CONSTRAINT fixed_penalty_offences_pk PRIMARY KEY (defendant_account_id);

--
-- Name: hmrc_requests hmrc_requests_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hmrc_requests
    ADD CONSTRAINT hmrc_requests_pk PRIMARY KEY (hmrc_request_id);

--
-- Name: imposition_category_item_number imposition_category_item_number_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.imposition_category_item_number
    ADD CONSTRAINT imposition_category_item_number_pk PRIMARY KEY (imposition_category);

--
-- Name: impositions impositions_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT impositions_pk PRIMARY KEY (imposition_id);

--
-- Name: interface_files interface_files_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_files
    ADD CONSTRAINT interface_files_pk PRIMARY KEY (interface_file_id);

--
-- Name: interface_jobs interface_jobs_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_jobs
    ADD CONSTRAINT interface_jobs_pk PRIMARY KEY (interface_job_id);

--
-- Name: interface_messages interface_messages_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_messages
    ADD CONSTRAINT interface_messages_pk PRIMARY KEY (interface_message_id);

--
-- Name: local_justice_areas local_justice_area_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.local_justice_areas
    ADD CONSTRAINT local_justice_area_id_pk PRIMARY KEY (local_justice_area_id);

--
-- Name: log_actions log_actions_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log_actions
    ADD CONSTRAINT log_actions_pk PRIMARY KEY (log_action_id);

--
-- Name: log_audit_details log_audit_details_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log_audit_details
    ADD CONSTRAINT log_audit_details_pk PRIMARY KEY (log_audit_detail_id);

--
-- Name: major_creditors major_creditors_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.major_creditors
    ADD CONSTRAINT major_creditors_pk PRIMARY KEY (major_creditor_id);

--
-- Name: mis_debtors mis_debtors_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mis_debtors
    ADD CONSTRAINT mis_debtors_pk PRIMARY KEY (mis_debtor_id);

--
-- Name: miscellaneous_accounts miscellaneous_accounts_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.miscellaneous_accounts
    ADD CONSTRAINT miscellaneous_accounts_pk PRIMARY KEY (miscellaneous_account_id);

--
-- Name: message_log ml_message_log_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.message_log
    ADD CONSTRAINT ml_message_log_id_pk PRIMARY KEY (message_log_id);

--
-- Name: notes note_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notes
    ADD CONSTRAINT note_id_pk PRIMARY KEY (note_id);

--
-- Name: offences offences_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offences
    ADD CONSTRAINT offences_pk PRIMARY KEY (offence_id);

--
-- Name: parties parties_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parties
    ADD CONSTRAINT parties_pk PRIMARY KEY (party_id);

--
-- Name: payment_card_requests payment_card_requests_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_card_requests
    ADD CONSTRAINT payment_card_requests_pk PRIMARY KEY (defendant_account_id);

--
-- Name: payment_terms payment_terms_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_terms
    ADD CONSTRAINT payment_terms_pk PRIMARY KEY (payment_terms_id);

--
-- Name: payments_in payments_in_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments_in
    ADD CONSTRAINT payments_in_pk PRIMARY KEY (payment_in_id);

--
-- Name: print_definition print_definition_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_definition
    ADD CONSTRAINT print_definition_id_pk PRIMARY KEY (print_definition_id);

--
-- Name: print_job print_job_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_job
    ADD CONSTRAINT print_job_id_pk PRIMARY KEY (print_job_id);

--
-- Name: print_performance_monitor print_performance_monitor_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.print_performance_monitor
    ADD CONSTRAINT print_performance_monitor_id_pk PRIMARY KEY (print_performance_monitor_id);

--
-- Name: prisons prisons_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.prisons
    ADD CONSTRAINT prisons_pk PRIMARY KEY (prison_id);

--
-- Name: prosecutors prosecutors_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.prosecutors
    ADD CONSTRAINT prosecutors_pk PRIMARY KEY (prosecutor_id);

--
-- Name: report_entries report_entries_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_entries
    ADD CONSTRAINT report_entries_pk PRIMARY KEY (report_entry_id);

--
-- Name: report_instances report_instances_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_instances
    ADD CONSTRAINT report_instances_pk PRIMARY KEY (report_instance_id);

--
-- Name: reports reports_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pk PRIMARY KEY (report_id);

--
-- Name: result_documents result_documents_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.result_documents
    ADD CONSTRAINT result_documents_pk PRIMARY KEY (result_document_id);

--
-- Name: results results_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.results
    ADD CONSTRAINT results_id_pk PRIMARY KEY (result_id);

--
-- Name: standard_letters standard_letters_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.standard_letters
    ADD CONSTRAINT standard_letters_pk PRIMARY KEY (standard_letter_id);

--
-- Name: suspense_accounts suspense_accounts_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_accounts
    ADD CONSTRAINT suspense_accounts_pk PRIMARY KEY (suspense_account_id);

--
-- Name: suspense_items suspense_items_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_items
    ADD CONSTRAINT suspense_items_pk PRIMARY KEY (suspense_item_id);

--
-- Name: suspense_transactions suspense_transactions_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_transactions
    ADD CONSTRAINT suspense_transactions_pk PRIMARY KEY (suspense_transaction_id);

--
-- Name: tills tills_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tills
    ADD CONSTRAINT tills_pk PRIMARY KEY (till_id);

--
-- Name: warrant_register warrant_register_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warrant_register
    ADD CONSTRAINT warrant_register_pk PRIMARY KEY (warrant_register_id);

--
-- Name: aliases_party_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX aliases_party_id_idx ON public.aliases USING btree (party_id);

--
-- Name: all_defendant_transaction_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX all_defendant_transaction_id_idx ON public.allocations USING btree (defendant_transaction_id);

--
-- Name: all_imposition_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX all_imposition_id_idx ON public.allocations USING btree (imposition_id);

--
-- Name: amdt_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX amdt_business_unit_id_idx ON public.amendments USING btree (business_unit_id);

--
-- Name: amdt_field_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX amdt_field_code_idx ON public.amendments USING btree (field_code);

--
-- Name: ani_bu_acc_num_udx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ani_bu_acc_num_udx ON public.account_number_index USING btree (business_unit_id, account_number);

--
-- Name: at_defendant_account_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX at_defendant_account_id_idx ON public.account_transfers USING btree (defendant_account_id);

--
-- Name: at_destination_lja_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX at_destination_lja_id_idx ON public.account_transfers USING btree (destination_lja_id);

--
-- Name: at_document_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX at_document_instance_id_idx ON public.account_transfers USING btree (document_instance_id);

--
-- Name: bacs_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX bacs_business_unit_id_idx ON public.bacs_payments USING btree (business_unit_id);

--
-- Name: bacs_creditor_transaction_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX bacs_creditor_transaction_id_idx ON public.bacs_payments USING btree (creditor_transaction_id);

--
-- Name: bacs_defendant_transaction_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX bacs_defendant_transaction_id_idx ON public.bacs_payments USING btree (defendant_transaction_id);

--
-- Name: ca_bu_acc_num_udx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ca_bu_acc_num_udx ON public.creditor_accounts USING btree (business_unit_id, account_number);

--
-- Name: ca_bus_unit_acc_type_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ca_bus_unit_acc_type_idx ON public.creditor_accounts USING btree (business_unit_id, creditor_account_type);

--
-- Name: ca_major_creditor_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ca_major_creditor_id_idx ON public.creditor_accounts USING btree (major_creditor_id);

--
-- Name: ca_mcpid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ca_mcpid_idx ON public.creditor_accounts USING btree (minor_creditor_party_id);

--
-- Name: cf_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cf_business_unit_id_idx ON public.court_fees USING btree (business_unit_id);

--
-- Name: cfr_court_fee_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cfr_court_fee_id_idx ON public.court_fees_received USING btree (court_fee_id);

--
-- Name: cfr_received_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cfr_received_business_unit_id_idx ON public.court_fees_received USING btree (business_unit_id);

--
-- Name: cfr_suspense_transaction_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cfr_suspense_transaction_id_idx ON public.court_fees_received USING btree (suspense_transaction_id);

--
-- Name: che_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX che_business_unit_id_idx ON public.cheques USING btree (business_unit_id);

--
-- Name: che_creditor_transaction_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX che_creditor_transaction_id_idx ON public.cheques USING btree (creditor_transaction_id);

--
-- Name: che_defendant_transaction_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX che_defendant_transaction_id_idx ON public.cheques USING btree (defendant_transaction_id);

--
-- Name: ci_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ci_business_unit_id_idx ON public.configuration_items USING btree (business_unit_id);

--
-- Name: ci_item_name_bu_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ci_item_name_bu_idx ON public.configuration_items USING btree (item_name, business_unit_id);

--
-- Name: crt_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX crt_business_unit_id_idx ON public.courts USING btree (business_unit_id);

--
-- Name: crt_local_justice_area_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX crt_local_justice_area_id_idx ON public.courts USING btree (local_justice_area_id);

--
-- Name: crt_parent_court_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX crt_parent_court_id_idx ON public.courts USING btree (parent_court_id);

--
-- Name: ct_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ct_business_unit_id_idx ON public.control_totals USING btree (business_unit_id);

--
-- Name: ct_caid_tt_pp_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ct_caid_tt_pp_idx ON public.creditor_transactions USING btree (creditor_account_id, transaction_type, payment_processed);

--
-- Name: ct_ct_report_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ct_ct_report_instance_id_idx ON public.control_totals USING btree (ct_report_instance_id);

--
-- Name: ct_qe_report_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ct_qe_report_instance_id_idx ON public.control_totals USING btree (qe_report_instance_id);

--
-- Name: cwp_defendant_account_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cwp_defendant_account_id_idx ON public.committal_warrant_progress USING btree (defendant_account_id);

--
-- Name: cwp_enforcement_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cwp_enforcement_id_idx ON public.committal_warrant_progress USING btree (enforcement_id);

--
-- Name: cwp_prison_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX cwp_prison_id_idx ON public.committal_warrant_progress USING btree (prison_id);

--
-- Name: da_account_balance_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_account_balance_idx ON public.defendant_accounts USING btree (account_balance);

--
-- Name: da_account_number_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_account_number_idx ON public.defendant_accounts USING btree (account_number);

--
-- Name: da_bu_acc_num_udx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX da_bu_acc_num_udx ON public.defendant_accounts USING btree (business_unit_id, account_number);

--
-- Name: da_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_business_unit_id_idx ON public.defendant_accounts USING btree (business_unit_id);

--
-- Name: da_enf_override_enforcer_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_enf_override_enforcer_id_idx ON public.defendant_accounts USING btree (enf_override_enforcer_id);

--
-- Name: da_enf_override_result_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_enf_override_result_id_idx ON public.defendant_accounts USING btree (enf_override_result_id);

--
-- Name: da_enf_override_tfo_lja_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_enf_override_tfo_lja_id_idx ON public.defendant_accounts USING btree (enf_override_tfo_lja_id);

--
-- Name: da_enforcing_court_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_enforcing_court_id_idx ON public.defendant_accounts USING btree (enforcing_court_id);

--
-- Name: da_imposing_court_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_imposing_court_id_idx ON public.defendant_accounts USING btree (imposing_court_id);

--
-- Name: da_last_hearing_court_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_last_hearing_court_id_idx ON public.defendant_accounts USING btree (last_hearing_court_id);

--
-- Name: da_prosecutor_case_ref_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX da_prosecutor_case_ref_idx ON public.defendant_accounts USING btree (prosecutor_case_reference);

--
-- Name: dap_daid_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dap_daid_at_idx ON public.defendant_account_parties USING btree (defendant_account_id, association_type);

--
-- Name: dap_party_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dap_party_id_idx ON public.defendant_account_parties USING btree (party_id);

--
-- Name: di_bu_document_status_date_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX di_bu_document_status_date_idx ON public.document_instances USING btree (business_unit_id, document_id, status, generated_date);

--
-- Name: di_document_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX di_document_id_idx ON public.document_instances USING btree (document_id);

--
-- Name: dra_account_status_date_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dra_account_status_date_idx ON public.draft_accounts USING btree (account_status_date);

--
-- Name: dra_account_status_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dra_account_status_idx ON public.draft_accounts USING btree (account_status);

--
-- Name: dra_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dra_business_unit_id_idx ON public.draft_accounts USING btree (business_unit_id);

--
-- Name: dra_submitted_bu_status_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dra_submitted_bu_status_idx ON public.draft_accounts USING btree (submitted_by, business_unit_id, account_status);

--
-- Name: dra_submitted_by_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dra_submitted_by_idx ON public.draft_accounts USING btree (submitted_by);

--
-- Name: dra_submitted_by_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dra_submitted_by_name_idx ON public.draft_accounts USING btree (submitted_by_name);

--
-- Name: dtr_defendant_account_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX dtr_defendant_account_id_idx ON public.defendant_transactions USING btree (defendant_account_id);

--
-- Name: ea_allocations_enforcer_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ea_allocations_enforcer_id_idx ON public.enforcer_allocations USING btree (enforcer_id);

--
-- Name: ea_result_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ea_result_id_idx ON public.enforcer_allocations USING btree (result_id);

--
-- Name: eat_enforcement_account_type_udx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX eat_enforcement_account_type_udx ON public.enforcement_account_types USING btree (enforcement_account_type);

--
-- Name: efs_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX efs_business_unit_id_idx ON public.enforcers USING btree (business_unit_id);

--
-- Name: enf_case_reference_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX enf_case_reference_idx ON public.enforcements USING btree (case_reference);

--
-- Name: enf_defendant_account_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX enf_defendant_account_id_idx ON public.enforcements USING btree (defendant_account_id);

--
-- Name: enf_enforcer_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX enf_enforcer_id_idx ON public.enforcements USING btree (enforcer_id);

--
-- Name: enf_hearing_court_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX enf_hearing_court_id ON public.enforcements USING btree (hearing_court_id);

--
-- Name: enf_result_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX enf_result_id_idx ON public.enforcements USING btree (result_id);

--
-- Name: ep_enforcement_account_type_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ep_enforcement_account_type_id_idx ON public.enforcement_paths USING btree (enforcement_account_type_id);

--
-- Name: ep_enforcement_path_set_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ep_enforcement_path_set_id_idx ON public.enforcement_paths USING btree (enforcement_path_set_id);

--
-- Name: eps_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX eps_business_unit_id_idx ON public.enforcement_path_sets USING btree (business_unit_id);

--
-- Name: erc_court_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX erc_court_id_idx ON public.enforcement_run_courts USING btree (court_id);

--
-- Name: erc_enforcement_run_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX erc_enforcement_run_id_idx ON public.enforcement_run_courts USING btree (enforcement_run_id);

--
-- Name: es_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX es_business_unit_id_idx ON public.enforcement_runs USING btree (business_unit_id);

--
-- Name: fpo_ticket_number_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX fpo_ticket_number_idx ON public.fixed_penalty_offences USING btree (ticket_number);

--
-- Name: hr_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX hr_business_unit_id_idx ON public.hmrc_requests USING btree (business_unit_id);

--
-- Name: idx_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_status ON public.print_job USING btree (status);

--
-- Name: if_interface_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX if_interface_job_id_idx ON public.interface_files USING btree (interface_job_id);

--
-- Name: ij_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ij_business_unit_id_idx ON public.interface_jobs USING btree (business_unit_id);

--
-- Name: ij_status_created_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ij_status_created_idx ON public.interface_jobs USING btree (status, interface_name, created_datetime);

--
-- Name: im_interface_file_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX im_interface_file_id_idx ON public.interface_messages USING btree (interface_file_id);

--
-- Name: im_interface_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX im_interface_job_id_idx ON public.interface_messages USING btree (interface_job_id);

--
-- Name: imp_caid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX imp_caid_idx ON public.impositions USING btree (creditor_account_id);

--
-- Name: imp_daid_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX imp_daid_idx ON public.impositions USING btree (defendant_account_id);

--
-- Name: imp_imposing_court_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX imp_imposing_court_id_idx ON public.impositions USING btree (imposing_court_id);

--
-- Name: imp_offence_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX imp_offence_id_idx ON public.impositions USING btree (offence_id);

--
-- Name: imp_original_imposition_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX imp_original_imposition_id_idx ON public.impositions USING btree (original_imposition_id);

--
-- Name: imp_result_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX imp_result_id_idx ON public.impositions USING btree (result_id);

--
-- Name: lad_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX lad_business_unit_id_idx ON public.log_audit_details USING btree (business_unit_id);

--
-- Name: lad_log_action_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX lad_log_action_id_idx ON public.log_audit_details USING btree (log_action_id);

--
-- Name: lad_log_timestamp_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX lad_log_timestamp_idx ON public.log_audit_details USING btree (log_timestamp);

--
-- Name: lad_user_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX lad_user_id_idx ON public.log_audit_details USING btree (user_id);

--
-- Name: ma_bu_acc_num_udx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ma_bu_acc_num_udx ON public.miscellaneous_accounts USING btree (business_unit_id, account_number);

--
-- Name: ma_party_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ma_party_id_idx ON public.miscellaneous_accounts USING btree (party_id);

--
-- Name: mc_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mc_business_unit_id_idx ON public.major_creditors USING btree (business_unit_id);

--
-- Name: ml_created_date_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ml_created_date_idx ON public.message_log USING btree (created_date);

--
-- Name: off_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX off_business_unit_id_idx ON public.offences USING btree (business_unit_id);

--
-- Name: off_cjs_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX off_cjs_code_idx ON public.offences USING btree (cjs_code);

--
-- Name: off_offence_oas_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX off_offence_oas_idx ON public.offences USING btree (offence_oas);

--
-- Name: off_offence_title_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX off_offence_title_idx ON public.offences USING btree (offence_title);

--
-- Name: pa_address_line_1_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_address_line_1_idx ON public.parties USING btree (address_line_1);

--
-- Name: pa_address_line_1_postcode_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_address_line_1_postcode_idx ON public.parties USING btree (address_line_1, postcode);

--
-- Name: pa_national_insurance_num_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_national_insurance_num_idx ON public.parties USING btree (national_insurance_number);

--
-- Name: pa_organisation_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_organisation_name_idx ON public.parties USING btree (organisation_name);

--
-- Name: pa_postcode_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_postcode_idx ON public.parties USING btree (postcode);

--
-- Name: pa_surname_birthdate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_surname_birthdate_idx ON public.parties USING btree (surname, birth_date);

--
-- Name: pa_surname_forenames_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_surname_forenames_idx ON public.parties USING btree (surname, forenames);

--
-- Name: pa_surname_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pa_surname_idx ON public.parties USING btree (surname);

--
-- Name: pi_till_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pi_till_id_idx ON public.payments_in USING btree (till_id);

--
-- Name: pri_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pri_business_unit_id_idx ON public.prisons USING btree (business_unit_id);

--
-- Name: pt_def_acc_id_active_udx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX pt_def_acc_id_active_udx ON public.payment_terms USING btree (defendant_account_id) WHERE (active = true);

--
-- Name: pt_defendant_account_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX pt_defendant_account_id_idx ON public.payment_terms USING btree (defendant_account_id);

--
-- Name: rd_cy_document_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX rd_cy_document_id_idx ON public.result_documents USING btree (cy_document_id);

--
-- Name: rd_document_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX rd_document_id_idx ON public.result_documents USING btree (document_id);

--
-- Name: rd_result_document_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX rd_result_document_idx ON public.result_documents USING btree (result_id, document_id);

--
-- Name: re_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX re_business_unit_id_idx ON public.report_entries USING btree (business_unit_id);

--
-- Name: re_report_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX re_report_id_idx ON public.report_entries USING btree (report_id);

--
-- Name: re_report_instance_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX re_report_instance_id_idx ON public.report_entries USING btree (report_instance_id);

--
-- Name: ri_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ri_business_unit_id_idx ON public.report_instances USING btree (business_unit_id);

--
-- Name: ri_report_id_bu_id_request_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ri_report_id_bu_id_request_at_idx ON public.report_instances USING btree (report_id, business_unit_id, requested_at);

--
-- Name: ri_report_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ri_report_id_idx ON public.report_instances USING btree (report_id);

--
-- Name: ri_requested_by_bu_id_request_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ri_requested_by_bu_id_request_at_idx ON public.report_instances USING btree (requested_by, business_unit_id, requested_at);

--
-- Name: ri_scheduled_deletion_timestamp_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ri_scheduled_deletion_timestamp_idx ON public.report_instances USING btree (scheduled_deletion_timestamp);

--
-- Name: sa_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_business_unit_id_idx ON public.suspense_accounts USING btree (business_unit_id);

--
-- Name: si_court_fee_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX si_court_fee_id_idx ON public.suspense_items USING btree (court_fee_id);

--
-- Name: si_suspense_account_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX si_suspense_account_id_idx ON public.suspense_items USING btree (suspense_account_id);

--
-- Name: sl_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sl_business_unit_id_idx ON public.standard_letters USING btree (business_unit_id);

--
-- Name: st_suspense_item_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX st_suspense_item_id_idx ON public.suspense_transactions USING btree (suspense_item_id);

--
-- Name: till_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX till_business_unit_id_idx ON public.tills USING btree (business_unit_id);

--
-- Name: wr_business_unit_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX wr_business_unit_id_idx ON public.warrant_register USING btree (business_unit_id);

--
-- Name: wr_enforcement_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX wr_enforcement_id_idx ON public.warrant_register USING btree (enforcement_id);

--
-- Name: wr_enforcer_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX wr_enforcer_id_idx ON public.warrant_register USING btree (enforcer_id);

--
-- Name: aliases alias_party_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.aliases
    ADD CONSTRAINT alias_party_id_fk FOREIGN KEY (party_id) REFERENCES public.parties(party_id);

--
-- Name: allocations all_defendant_transaction_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT all_defendant_transaction_id_fk FOREIGN KEY (defendant_transaction_id) REFERENCES public.defendant_transactions(defendant_transaction_id);

--
-- Name: allocations all_imposition_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.allocations
    ADD CONSTRAINT all_imposition_id_fk FOREIGN KEY (imposition_id) REFERENCES public.impositions(imposition_id);

--
-- Name: amendments amdt_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.amendments
    ADD CONSTRAINT amdt_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: amendments amend_field_code_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.amendments
    ADD CONSTRAINT amend_field_code_fk FOREIGN KEY (field_code) REFERENCES public.audit_amendment_fields(field_code);

--
-- Name: account_number_index ani_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_number_index
    ADD CONSTRAINT ani_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: account_transfers at_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_transfers
    ADD CONSTRAINT at_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: account_transfers at_destination_lja_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_transfers
    ADD CONSTRAINT at_destination_lja_id_fk FOREIGN KEY (destination_lja_id) REFERENCES public.local_justice_areas(local_justice_area_id);

--
-- Name: account_transfers at_document_instance_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.account_transfers
    ADD CONSTRAINT at_document_instance_id_fk FOREIGN KEY (document_instance_id) REFERENCES public.document_instances(document_instance_id);

--
-- Name: bacs_payments bacs_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bacs_payments
    ADD CONSTRAINT bacs_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: bacs_payments bacs_creditor_transaction_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bacs_payments
    ADD CONSTRAINT bacs_creditor_transaction_id_fk FOREIGN KEY (creditor_transaction_id) REFERENCES public.creditor_transactions(creditor_transaction_id);

--
-- Name: bacs_payments bacs_defendant_transaction_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bacs_payments
    ADD CONSTRAINT bacs_defendant_transaction_id_fk FOREIGN KEY (defendant_transaction_id) REFERENCES public.defendant_transactions(defendant_transaction_id);

--
-- Name: creditor_accounts ca_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creditor_accounts
    ADD CONSTRAINT ca_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: creditor_accounts ca_major_creditor_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creditor_accounts
    ADD CONSTRAINT ca_major_creditor_id_fk FOREIGN KEY (major_creditor_id) REFERENCES public.major_creditors(major_creditor_id);

--
-- Name: creditor_accounts ca_minor_creditor_party_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creditor_accounts
    ADD CONSTRAINT ca_minor_creditor_party_id_fk FOREIGN KEY (minor_creditor_party_id) REFERENCES public.parties(party_id);

--
-- Name: court_fees cf_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.court_fees
    ADD CONSTRAINT cf_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: court_fees_received cfr_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.court_fees_received
    ADD CONSTRAINT cfr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: court_fees_received cfr_court_fee_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.court_fees_received
    ADD CONSTRAINT cfr_court_fee_id_fk FOREIGN KEY (court_fee_id) REFERENCES public.court_fees(court_fee_id);

--
-- Name: court_fees_received cfr_suspense_transaction_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.court_fees_received
    ADD CONSTRAINT cfr_suspense_transaction_id_fk FOREIGN KEY (suspense_transaction_id) REFERENCES public.suspense_transactions(suspense_transaction_id);

--
-- Name: cheques che_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cheques
    ADD CONSTRAINT che_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: cheques che_creditor_transaction_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cheques
    ADD CONSTRAINT che_creditor_transaction_id_fk FOREIGN KEY (creditor_transaction_id) REFERENCES public.creditor_transactions(creditor_transaction_id);

--
-- Name: cheques che_defendant_transaction_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cheques
    ADD CONSTRAINT che_defendant_transaction_id_fk FOREIGN KEY (defendant_transaction_id) REFERENCES public.defendant_transactions(defendant_transaction_id);

--
-- Name: configuration_items ci_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.configuration_items
    ADD CONSTRAINT ci_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: courts crt_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courts
    ADD CONSTRAINT crt_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: courts crt_local_justice_area_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courts
    ADD CONSTRAINT crt_local_justice_area_id_fk FOREIGN KEY (local_justice_area_id) REFERENCES public.local_justice_areas(local_justice_area_id);

--
-- Name: courts crt_parent_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courts
    ADD CONSTRAINT crt_parent_court_id_fk FOREIGN KEY (parent_court_id) REFERENCES public.courts(court_id);

--
-- Name: control_totals ct_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.control_totals
    ADD CONSTRAINT ct_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: creditor_transactions ct_creditor_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.creditor_transactions
    ADD CONSTRAINT ct_creditor_account_id_fk FOREIGN KEY (creditor_account_id) REFERENCES public.creditor_accounts(creditor_account_id);

--
-- Name: control_totals ct_ct_report_instance_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.control_totals
    ADD CONSTRAINT ct_ct_report_instance_id_fk FOREIGN KEY (ct_report_instance_id) REFERENCES public.report_instances(report_instance_id);

--
-- Name: control_totals ct_qe_report_instance_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.control_totals
    ADD CONSTRAINT ct_qe_report_instance_id_fk FOREIGN KEY (qe_report_instance_id) REFERENCES public.report_instances(report_instance_id);

--
-- Name: committal_warrant_progress cwp_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.committal_warrant_progress
    ADD CONSTRAINT cwp_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: committal_warrant_progress cwp_enforcement_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.committal_warrant_progress
    ADD CONSTRAINT cwp_enforcement_id_fk FOREIGN KEY (enforcement_id) REFERENCES public.enforcements(enforcement_id);

--
-- Name: defendant_accounts da_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: defendant_accounts da_enf_override_enforcer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_enf_override_enforcer_id_fk FOREIGN KEY (enf_override_enforcer_id) REFERENCES public.enforcers(enforcer_id);

--
-- Name: defendant_accounts da_enf_override_result_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_enf_override_result_id_fk FOREIGN KEY (enf_override_result_id) REFERENCES public.results(result_id);

--
-- Name: defendant_accounts da_enf_override_tfo_lja_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_enf_override_tfo_lja_id_fk FOREIGN KEY (enf_override_tfo_lja_id) REFERENCES public.local_justice_areas(local_justice_area_id);

--
-- Name: defendant_accounts da_enforcing_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_enforcing_court_id_fk FOREIGN KEY (enforcing_court_id) REFERENCES public.courts(court_id);

--
-- Name: defendant_accounts da_imposing_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_imposing_court_id_fk FOREIGN KEY (imposing_court_id) REFERENCES public.courts(court_id);

--
-- Name: defendant_accounts da_last_hearing_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_accounts
    ADD CONSTRAINT da_last_hearing_court_id_fk FOREIGN KEY (last_hearing_court_id) REFERENCES public.courts(court_id);

--
-- Name: draft_accounts dac_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.draft_accounts
    ADD CONSTRAINT dac_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: defendant_account_parties dap_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_account_parties
    ADD CONSTRAINT dap_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: defendant_account_parties dap_party_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_account_parties
    ADD CONSTRAINT dap_party_id_fk FOREIGN KEY (party_id) REFERENCES public.parties(party_id);

--
-- Name: debtor_detail dd_party_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.debtor_detail
    ADD CONSTRAINT dd_party_id_fk FOREIGN KEY (party_id) REFERENCES public.parties(party_id);

--
-- Name: document_instances di_business_unit_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.document_instances
    ADD CONSTRAINT di_business_unit_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: document_instances di_document_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.document_instances
    ADD CONSTRAINT di_document_id_fk FOREIGN KEY (document_id) REFERENCES public.documents(document_id);

--
-- Name: defendant_transactions dtr_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.defendant_transactions
    ADD CONSTRAINT dtr_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: enforcer_allocations ea_enforcer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcer_allocations
    ADD CONSTRAINT ea_enforcer_id_fk FOREIGN KEY (enforcer_id) REFERENCES public.enforcers(enforcer_id);

--
-- Name: enforcer_allocations ea_result_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcer_allocations
    ADD CONSTRAINT ea_result_id_fk FOREIGN KEY (result_id) REFERENCES public.results(result_id);

--
-- Name: enforcers enf_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcers
    ADD CONSTRAINT enf_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: enforcements enf_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcements
    ADD CONSTRAINT enf_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: enforcements enf_enforcer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcements
    ADD CONSTRAINT enf_enforcer_id_fk FOREIGN KEY (enforcer_id) REFERENCES public.enforcers(enforcer_id);

--
-- Name: enforcements enf_hearing_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcements
    ADD CONSTRAINT enf_hearing_court_id_fk FOREIGN KEY (hearing_court_id) REFERENCES public.courts(court_id);

--
-- Name: committal_warrant_progress enf_prison_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.committal_warrant_progress
    ADD CONSTRAINT enf_prison_id_fk FOREIGN KEY (prison_id) REFERENCES public.prisons(prison_id);

--
-- Name: enforcements enf_result_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcements
    ADD CONSTRAINT enf_result_id_fk FOREIGN KEY (result_id) REFERENCES public.results(result_id);

--
-- Name: enforcement_paths ep_enforcement_account_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_paths
    ADD CONSTRAINT ep_enforcement_account_type_id_fk FOREIGN KEY (enforcement_account_type_id) REFERENCES public.enforcement_account_types(enforcement_account_type_id);

--
-- Name: enforcement_paths ep_enforcement_path_set_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_paths
    ADD CONSTRAINT ep_enforcement_path_set_id_fk FOREIGN KEY (enforcement_path_set_id) REFERENCES public.enforcement_path_sets(enforcement_path_set_id);

--
-- Name: enforcement_path_sets eps_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_path_sets
    ADD CONSTRAINT eps_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: enforcement_run_courts erc_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_run_courts
    ADD CONSTRAINT erc_court_id_fk FOREIGN KEY (court_id) REFERENCES public.courts(court_id);

--
-- Name: enforcement_run_courts erc_enforcement_run_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_run_courts
    ADD CONSTRAINT erc_enforcement_run_id_fk FOREIGN KEY (enforcement_run_id) REFERENCES public.enforcement_runs(enforcement_run_id);

--
-- Name: enforcement_runs es_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enforcement_runs
    ADD CONSTRAINT es_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: fixed_penalty_offences fpo_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fixed_penalty_offences
    ADD CONSTRAINT fpo_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: hmrc_requests hr_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hmrc_requests
    ADD CONSTRAINT hr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: interface_jobs if_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_jobs
    ADD CONSTRAINT if_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: interface_files if_interface_job_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_files
    ADD CONSTRAINT if_interface_job_id_fk FOREIGN KEY (interface_job_id) REFERENCES public.interface_jobs(interface_job_id);

--
-- Name: interface_messages im_interface_file_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_messages
    ADD CONSTRAINT im_interface_file_id_fk FOREIGN KEY (interface_file_id) REFERENCES public.interface_files(interface_file_id);

--
-- Name: interface_messages im_interface_job_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.interface_messages
    ADD CONSTRAINT im_interface_job_id_fk FOREIGN KEY (interface_job_id) REFERENCES public.interface_jobs(interface_job_id);

--
-- Name: impositions imp_creditor_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT imp_creditor_account_id_fk FOREIGN KEY (creditor_account_id) REFERENCES public.creditor_accounts(creditor_account_id);

--
-- Name: impositions imp_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT imp_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: impositions imp_imposing_court_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT imp_imposing_court_id_fk FOREIGN KEY (imposing_court_id) REFERENCES public.courts(court_id);

--
-- Name: impositions imp_offence_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT imp_offence_id_fk FOREIGN KEY (offence_id) REFERENCES public.offences(offence_id);

--
-- Name: impositions imp_original_imposition_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT imp_original_imposition_id_fk FOREIGN KEY (original_imposition_id) REFERENCES public.impositions(imposition_id);

--
-- Name: impositions imp_result_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.impositions
    ADD CONSTRAINT imp_result_id_fk FOREIGN KEY (result_id) REFERENCES public.results(result_id);

--
-- Name: log_audit_details lad_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log_audit_details
    ADD CONSTRAINT lad_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: log_audit_details lad_log_action_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log_audit_details
    ADD CONSTRAINT lad_log_action_id_fk FOREIGN KEY (log_action_id) REFERENCES public.log_actions(log_action_id);

--
-- Name: miscellaneous_accounts ma_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.miscellaneous_accounts
    ADD CONSTRAINT ma_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: miscellaneous_accounts ma_party_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.miscellaneous_accounts
    ADD CONSTRAINT ma_party_id_fk FOREIGN KEY (party_id) REFERENCES public.parties(party_id);

--
-- Name: major_creditors mc_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.major_creditors
    ADD CONSTRAINT mc_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: offences off_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offences
    ADD CONSTRAINT off_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: payment_card_requests pcr_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_card_requests
    ADD CONSTRAINT pcr_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: payments_in pi_till_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments_in
    ADD CONSTRAINT pi_till_id_fk FOREIGN KEY (till_id) REFERENCES public.tills(till_id);

--
-- Name: prisons pri_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.prisons
    ADD CONSTRAINT pri_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: payment_terms pt_defendant_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_terms
    ADD CONSTRAINT pt_defendant_account_id_fk FOREIGN KEY (defendant_account_id) REFERENCES public.defendant_accounts(defendant_account_id);

--
-- Name: result_documents rd_cy_document_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.result_documents
    ADD CONSTRAINT rd_cy_document_id_fk FOREIGN KEY (cy_document_id) REFERENCES public.documents(document_id);

--
-- Name: result_documents rd_document_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.result_documents
    ADD CONSTRAINT rd_document_id_fk FOREIGN KEY (document_id) REFERENCES public.documents(document_id);

--
-- Name: result_documents rd_result_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.result_documents
    ADD CONSTRAINT rd_result_id_fk FOREIGN KEY (result_id) REFERENCES public.results(result_id);

--
-- Name: report_entries re_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_entries
    ADD CONSTRAINT re_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: report_entries re_report_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_entries
    ADD CONSTRAINT re_report_id_fk FOREIGN KEY (report_id) REFERENCES public.reports(report_id);

--
-- Name: report_entries re_report_instance_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_entries
    ADD CONSTRAINT re_report_instance_id_fk FOREIGN KEY (report_instance_id) REFERENCES public.report_instances(report_instance_id) DEFERRABLE INITIALLY DEFERRED;

--
-- Name: report_instances ri_report_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_instances
    ADD CONSTRAINT ri_report_id_fk FOREIGN KEY (report_id) REFERENCES public.reports(report_id);

--
-- Name: suspense_accounts sa_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_accounts
    ADD CONSTRAINT sa_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: suspense_items si_court_fee_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_items
    ADD CONSTRAINT si_court_fee_id_fk FOREIGN KEY (court_fee_id) REFERENCES public.court_fees(court_fee_id);

--
-- Name: suspense_items si_suspense_account_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_items
    ADD CONSTRAINT si_suspense_account_id_fk FOREIGN KEY (suspense_account_id) REFERENCES public.suspense_accounts(suspense_account_id);

--
-- Name: standard_letters sl_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.standard_letters
    ADD CONSTRAINT sl_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: suspense_transactions st_suspense_item_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suspense_transactions
    ADD CONSTRAINT st_suspense_item_id_fk FOREIGN KEY (suspense_item_id) REFERENCES public.suspense_items(suspense_item_id);

--
-- Name: tills till_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tills
    ADD CONSTRAINT till_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: warrant_register wr_business_unit_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warrant_register
    ADD CONSTRAINT wr_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES public.business_units(business_unit_id);

--
-- Name: warrant_register wr_enforcement_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warrant_register
    ADD CONSTRAINT wr_enforcement_id_fk FOREIGN KEY (enforcement_id) REFERENCES public.enforcements(enforcement_id);

--
-- Name: warrant_register wr_enforcer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.warrant_register
    ADD CONSTRAINT wr_enforcer_id_fk FOREIGN KEY (enforcer_id) REFERENCES public.enforcers(enforcer_id);

--
-- Name: delete_expired_log_audit(); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.delete_expired_log_audit()
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : delete_expired_log_audit.sql
*
* DESCRIPTION : Procedure to physically delete log and audit data from the log_audit_details table after their retention time.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------------------------------------------
* 11/04/2024    A Dennis    1.0         PO-238 Procedure to physically delete log and audit data from the log_audit_details table after their retention time.
*
**/
DECLARE

    v_log_audit_deletion_time     INTEGER;
    v_deletion_time_string        VARCHAR(20);
	
BEGIN

    -- Get the retention time for Log and Audit data
    SELECT item_value
    INTO   v_log_audit_deletion_time
    FROM   configuration_items
    WHERE  item_name = 'AUDIT_LOG_RETENTION_PERIOD_DAYS';

    v_deletion_time_string := v_log_audit_deletion_time||' days';

    -- Delete log audit data that have exceeded their retention time.
    DELETE FROM log_audit_details 
    WHERE date_trunc('day', LOCALTIMESTAMP) > date_trunc('day', log_timestamp + v_deletion_time_string::INTERVAL);

    -- No exceptions error handling here so that if any failures occur the application layer that calls it will report it in the logs.
        		
END;
$$;

--
-- Name: PROCEDURE delete_expired_log_audit(); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.delete_expired_log_audit() IS 'Procedure to physically delete log and audit data from the log_audit_details table after their retention time';

--
-- Name: f_floor_months_between(date, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_floor_months_between(p1 date, p2 date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    m1 INTEGER;
    m2 INTEGER;
BEGIN
    m1 = date_part('month',p1) + (12 * date_part('year',p1));
    m2 = date_part('month',p2) + (12 * date_part('year',p2));
    RETURN
        CASE
            WHEN p1 > p2 THEN CASE WHEN date_part('day',p1) >= date_part('day',p2) THEN m1-m2 ELSE m1-m2-1 END
            ELSE CASE WHEN date_part('day',p2) >= date_part('day',p1) THEN m1-m2 ELSE m1-m2-1 END
        END;
END;
$$;

--
-- Name: f_arrears(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_arrears(pi_defendant_account_id bigint) RETURNS numeric
    LANGUAGE sql
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
$$;

--
-- Name: FUNCTION f_arrears(pi_defendant_account_id bigint); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON FUNCTION public.f_arrears(pi_defendant_account_id bigint) IS 'Calculates the arrears amount for a given defendant account';

--
-- Name: f_get_account_number(smallint, public.t_associated_record_type_enum); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_get_account_number(pi_business_unit_id smallint, pi_associated_record_type public.t_associated_record_type_enum) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : f_get_account_number.sql
*
* DESCRIPTION : Generate an account number based on a given business unit.
* 
* PARAMETERS  : pi_business_unit_id       - The business_unit_id the generated account_number is related to
*               pi_associated_record_type - The type of account (i.e. target table) the generated account is intended for 
*                                           Expected values: defendant_accounts, creditor_accounts or miscellaneous_accounts
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------------------------
* 08/07/2025    TMc         1.0         PO-899 Generate an account number based on a given business unit
* 18/07/2025    TMc         1.1         PO-1959 Rename the ACCOUNT_NUMBER_INDEX.ACCOUNT_INDEX_TYPE column to ASSOCIATED_RECORD_TYPE
*                                               Rename input parameter from pi_account_index_type to pi_associated_record_type
* 17/03/2026    TMc         2.0         PO-2933 Update columns on ACCOUNT_NUMBER_INDEX table to use postgresql enum instead of varchar
*                                               Amend the datatype of input parameter pi_associated_record_type from VARCHAR to t_associated_record_type_enum
**/
DECLARE
    c_seq_min_value    CONSTANT VARCHAR := '000001';
    c_seq_max_value    CONSTANT VARCHAR := '999999';
    c_allowed_retries  CONSTANT INTEGER := 5;
    v_retries                   INTEGER := 0;
    v_yy                        VARCHAR;
    v_min_account_number        account_number_index.account_number%TYPE;
    v_max_account_number        account_number_index.account_number%TYPE;
    v_account_number_gap        account_number_index.account_number%TYPE;
    v_account_number_partial    account_number_index.account_number%TYPE;
    v_account_number            account_number_index.account_number%TYPE;
    v_check_letter              VARCHAR;
    v_error_msg                 VARCHAR;
BEGIN

    --RAISE INFO 'pi_business_unit_id = %', pi_business_unit_id;
    --RAISE INFO 'pi_associated_record_type = %', pi_associated_record_type;
    --RAISE INFO 'c_seq_min_value = %', c_seq_min_value;
    --RAISE INFO 'c_seq_max_value = %', c_seq_max_value;
    
    --Loop until successful insert into account_number_index
    LOOP 
        v_yy := TO_CHAR(NOW(), 'YY');

        --RAISE INFO 'v_yy = %', v_yy;

        --Get minimum and maximum account_number for the passed BU and greater than the current year's minimum value, without the check character
        SELECT MIN(account_number), MAX(account_number)
          FROM account_number_index
         WHERE account_number > v_yy || c_seq_min_value
           AND business_unit_id = pi_business_unit_id
        INTO v_min_account_number, v_max_account_number;
        
        --RAISE INFO 'Min account_number = %', v_min_account_number;
        --RAISE INFO 'Max account_number = %', v_max_account_number;
        
        IF v_max_account_number IS NULL THEN
            --Account number, within current or future years, NOT found so set to min value for the current year
            --RAISE INFO 'Max value NOT found. Starting from min sequence for the current year [%]', v_yy || c_seq_min_value;
    
            v_account_number_partial := v_yy || c_seq_min_value;
        ELSE
            --Get YY from the max account number. It may have already rolled into next years range
            v_yy := LEFT(v_max_account_number, 2);
    
            --Check if max value reached, if it hasn't, then increment sequence value, otherwise check for gaps before rolling into next year
            IF SUBSTRING(v_max_account_number FROM 3 FOR 6) < c_seq_max_value THEN
                --Within range so increment value
                --RAISE INFO 'Within the range. Incrementing sequence';
                v_account_number_partial := v_yy || LPAD((SUBSTRING(v_max_account_number FROM 3 FOR 6)::INT + 1)::VARCHAR, 6, '0');
            ELSE
                --RAISE INFO 'Max value has been reached. Checking for gaps';
                
                --Check if account_number for min value is available. Gap SQL won't return records if gaps exist at the beginning of the range
                IF LEFT(v_min_account_number, 8) = v_yy || c_seq_min_value THEN
                    --Min sequence value exists, check for gaps within the range before rolling into next years range
                    --RAISE INFO 'Min sequence value exists, checking for other gaps within the range';
    
                    SELECT (MIN(account_number) + 1)::VARCHAR AS next_available_account_number  
                      FROM (
                            SELECT LEFT(account_number, 8)::INT AS account_number, lead(LEFT(account_number, 8)::INT) OVER ( PARTITION BY business_unit_id ORDER BY account_number) next_account_number
                              FROM account_number_index
                             WHERE account_number > v_yy || c_seq_min_value
                               AND business_unit_id = pi_business_unit_id
                           ) maxan
                     WHERE account_number + 1 != next_account_number
                    INTO v_account_number_gap;
    
                    IF v_account_number_gap IS NULL THEN
                        --No gaps found so roll into next years range
                        --RAISE INFO 'No gaps found so rolling into next years range';
                        v_account_number_partial := (v_yy::INT + 1) || c_seq_min_value;
                    ELSE
                        --Gap was found
                        --RAISE INFO 'Gap found: %', v_account_number_gap;
                        v_account_number_partial := v_account_number_gap;
                    END IF;
                ELSE
                    --Min sequence value doesn't exist so use it
                    --RAISE INFO 'Gap found. Minimum sequence can be used';
                    v_account_number_partial := v_yy || c_seq_min_value;
                END IF;
            END IF;
        END IF;
    
        --RAISE INFO 'v_account_number_partial = %', v_account_number_partial;
    
        v_check_letter := f_get_check_letter(v_account_number_partial);
        v_account_number := v_account_number_partial || v_check_letter;
        
        BEGIN
            RAISE INFO 'Inserting record into ACCOUNT_NUMBER_INDEX for account_number: %, BU: %', v_account_number, pi_business_unit_id;
        
            INSERT INTO account_number_index(account_number_index_id, business_unit_id, account_number, associated_record_type)
            VALUES ( NEXTVAL('account_number_index_seq')
                   , pi_business_unit_id
                   , v_account_number
                   , pi_associated_record_type
                   );
 
            --Exit loop on success
            EXIT;
        EXCEPTION 
            WHEN UNIQUE_VIOLATION THEN
                v_error_msg := format('Error in f_get_account_number. Unique violation inserting Account number = %s, BU = %s. Error: %s - %s', v_account_number, pi_business_unit_id, SQLSTATE, SQLERRM);
                RAISE WARNING '%', v_error_msg;

                v_retries := v_retries + 1;

                IF v_retries >= c_allowed_retries THEN
                    RAISE EXCEPTION '%', v_error_msg;
                END IF;
        END;
    END LOOP;

    RETURN v_account_number;
END;
$$;

--
-- Name: f_get_check_letter(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_get_check_letter(pi_account_number character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : f_get_check_letter.sql
*
* DESCRIPTION : Generate a check letter
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 02/07/2025    Garry C     1.0         PO-899 Generate a check letter
* 02/07/2025    TMc         1.1         PO-899 Prepare code for deployment
*
**/
DECLARE
    n_asc int;
BEGIN
    n_asc := substr(pi_account_number,1,1)::int * 5
        + substr(pi_account_number,2,1)::int*1
        + substr(pi_account_number,3,1)::int*4
        + substr(pi_account_number,4,1)::int*2
        + substr(pi_account_number,5,1)::int*7
        + substr(pi_account_number,6,1)::int*5
        + substr(pi_account_number,7,1)::int*1
        + substr(pi_account_number,8,1)::int*4;
    n_asc := 23-(n_asc-(trunc(n_asc/23)*23));
    n_asc := CASE n_asc WHEN 23 THEN 0 ELSE n_asc END;
    RETURN chr(n_asc+65);
END;
$$;

--
-- Name: f_get_master_account_id(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_get_master_account_id(pi_defendant_account_id bigint) RETURNS bigint
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : f_get_master_account_id.sql
*
* DESCRIPTION : This function was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This function was written by Capita required for interface files
*
**/
DECLARE
    v_master_account_id defendant_accounts.defendant_account_id%TYPE;
BEGIN
    WITH RECURSIVE master_accounts AS (
        SELECT      da.defendant_account_id, dt.associated_record_id::bigint AS master_account_id
        FROM        defendant_accounts da
        LEFT JOIN   defendant_transactions dt ON da.account_status = 'CS' AND dt.defendant_account_id = da.defendant_account_id AND dt.transaction_type = 'WRTOFF' AND dt.text LIKE 'CONSOLIDATED%'
        WHERE       da.defendant_account_id = pi_defendant_account_id
        UNION ALL
        SELECT      da1.defendant_account_id, dt1.associated_record_id::bigint AS master_account_id
        FROM        defendant_accounts da1
        LEFT JOIN   defendant_transactions dt1 ON da1.account_status = 'CS' AND dt1.defendant_account_id = da1.defendant_account_id AND dt1.transaction_type = 'WRTOFF' AND dt1.text LIKE 'CONSOLIDATED%'
        INNER JOIN  master_accounts ma ON da1.defendant_account_id = ma.master_account_id
    )
    SELECT  defendant_account_id
    INTO    v_master_account_id
    FROM    master_accounts WHERE master_account_id IS NULL;
    RETURN v_master_account_id;
END;
$$;

--
-- Name: has_uncleared_cheques(bigint); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.has_uncleared_cheques(p_defendant_account_id bigint) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_clearance_period INTEGER;
    v_has_uncleared BOOLEAN := FALSE;
BEGIN
    -- Get the cheque clearance period for the account
    SELECT 
        CASE 
            WHEN da.cheque_clearance_period IS NOT NULL AND da.cheque_clearance_period > 0 
            THEN da.cheque_clearance_period
            ELSE NULL
        END
    INTO v_clearance_period
    FROM defendant_accounts da
    WHERE da.defendant_account_id = p_defendant_account_id;
    
    -- If clearance period is not set on account, get from configuration items table
    IF v_clearance_period IS NULL THEN
        SELECT ci.item_value::int
        INTO v_clearance_period
        FROM configuration_items ci
        WHERE ci.item_name = 'DEFAULT_CHEQUE_CLEARANCE_PERIOD'
        ORDER BY ci.configuration_item_id
        LIMIT 1;
        
        -- Set to 0 if no configuration found
        v_clearance_period := COALESCE(v_clearance_period, 0);
    END IF;
    
    -- Check for uncleared cheques
    SELECT TRUE
    INTO v_has_uncleared
    FROM defendant_transactions dt
    WHERE dt.defendant_account_id = p_defendant_account_id
      AND dt.payment_method = 'CQ'
      AND dt.posted_date IS NOT NULL
      AND (CURRENT_DATE - dt.posted_date::date) <= v_clearance_period
    LIMIT 1;
    
    RETURN COALESCE(v_has_uncleared, FALSE);
END;
$$;

--
-- Name: p_add_defendant_account_enforcement(character varying, bigint, smallint, character varying, character varying, character varying, integer, character varying, character varying, character varying, bigint, json, timestamp without time zone, bigint); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_add_defendant_account_enforcement(IN pi_result_id character varying, IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_record_type character varying, IN pi_case_reference character varying, IN pi_function_code character varying, IN pi_jail_days integer, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_reason character varying, IN pi_enforcer_id bigint, IN pi_result_responses json, IN pi_earliest_release_date timestamp without time zone, IN pi_version_number bigint, OUT po_enforcement_id bigint)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_add_defendant_account_enforcement.sql
*
* DESCRIPTION : Procedure to add a new enforcement action for a defendant account.
*               Creates enforcement records and updates defendant account fields as required.
*               Calls audit procedures to track changes to auditable fields.
*               Implements concurrency check via version number checking.
*
* PARAMETERS : pi_result_id               - The incoming result_id which is the same as the enforcement action
*            : pi_defendant_account_id    - The Opal defendant account id
*            : pi_business_unit_id        - Business unit identifier
*            : pi_record_type             - For audit, this will be 'defendant_accounts'
*            : pi_case_reference          - For audit, case reference if set by Case Management
*            : pi_function_code           - For audit, the function from which the Amendment was made
*            : pi_jail_days               - Number of days in jail the defendant will spend in default of payment
*            : pi_posted_by               - User ID submitting the request
*            : pi_posted_by_name          - User name submitting the request
*            : pi_reason                  - The reason for this enforcement action
*            : pi_enforcer_id             - The enforcer/process server for this enforcement action
*            : pi_result_responses        - The result parameters
*            : pi_earliest_release_date   - The earliest release date for a PRIS enforcement action
*            : pi_version_number          - Current version number for concurrency check
*            : po_enforcement_id          - Returns the ID of the new enforcement created
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------
* 12/09/2025    C Cho       1.0         PO-1790 Initial version - Create add defendant account enforcement procedure.
* 24/09/2025    C Cho       1.1         PO-2130 Add concurrency check via version number parameter.
* 17/03/2026    TMc         2.0         PO-2930 Amended INSERT statements for DOCUMENT_INSTANCES and REPORT_ENTRIES, 
*                                               set ASSOCIATED_RECORD_TYPE to a valid ENUM value (enforcements)
*
**/
DECLARE
    v_pg_exception_detail            TEXT;
    v_enforcement_id                 BIGINT;
    v_enforcement_action             VARCHAR(6);
    v_generates_warrant              BOOLEAN := FALSE;
    v_enforcer_code                  VARCHAR(4);
    v_current_warrant_ref            VARCHAR(20);
    v_warrant_reference              VARCHAR(20);
    v_current_year                   VARCHAR(2);
    v_current_ref_year               VARCHAR(2);
    v_warrant_register_id            BIGINT;
    v_document_id                    VARCHAR(12);
    v_document_instance_id           BIGINT;
    v_fsn_date                       TIMESTAMP;
    v_fine_reg_date                  TIMESTAMP;
    v_confiscation_date              TIMESTAMP;
    v_serial_part                    VARCHAR(5);
    v_current_serial                 INTEGER;
    v_rows_updated                   INTEGER;
    
BEGIN
    RAISE INFO 'p_add_defendant_account_enforcement: Starting with pi_result_id = %, pi_defendant_account_id = %, pi_version_number = %', pi_result_id, pi_defendant_account_id, pi_version_number;

    -- Validate input parameters
    IF pi_result_id IS NULL THEN
        RAISE EXCEPTION 'Result ID cannot be null'
            USING ERRCODE = 'P4001'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_result_id is required';
    END IF;

    IF pi_defendant_account_id IS NULL THEN
        RAISE EXCEPTION 'Defendant account ID cannot be null'
            USING ERRCODE = 'P4002'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_defendant_account_id is required';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Business unit ID cannot be null'
            USING ERRCODE = 'P4003'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_business_unit_id is required';
    END IF;

    IF pi_record_type IS NULL OR pi_record_type != 'defendant_accounts' THEN
        RAISE EXCEPTION 'Invalid record type: %. Must be defendant_accounts', pi_record_type
            USING ERRCODE = 'P4004'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_record_type must be defendant_accounts';
    END IF;

    IF pi_posted_by IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null'
            USING ERRCODE = 'P4005'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_posted_by is required';
    END IF;

    IF pi_version_number IS NULL THEN
        RAISE EXCEPTION 'Version number cannot be null'
            USING ERRCODE = 'P4007'
                , DETAIL = 'p_add_defendant_account_enforcement: pi_version_number is required for concurrency control';
    END IF;

    -- The incoming result_id is also the enforcement action
    v_enforcement_action := pi_result_id;

    -- Call audit initialise procedure
    CALL p_audit_initialise(pi_defendant_account_id, pi_record_type);

    -- Get next enforcement ID
    v_enforcement_id := nextval('enforcement_id_seq');
    po_enforcement_id := v_enforcement_id;

    -- Check if this result generates a warrant
    SELECT generates_warrant INTO v_generates_warrant
    FROM results
    WHERE result_id = pi_result_id;

    -- Generate warrant reference if needed
    IF v_generates_warrant = TRUE AND pi_enforcer_id IS NOT NULL THEN
        -- Get enforcer code and current warrant reference sequence
        SELECT enforcer_code, warrant_reference_sequence
        INTO v_enforcer_code, v_current_warrant_ref
        FROM enforcers
        WHERE enforcer_id = pi_enforcer_id;

        IF v_enforcer_code IS NULL THEN
            RAISE EXCEPTION 'Enforcer not found with ID: %', pi_enforcer_id
                USING ERRCODE = 'P4006'
                    , DETAIL = 'p_add_defendant_account_enforcement: enforcer_id not found';
        END IF;

        -- Get current year suffix (last 2 digits)
        v_current_year := RIGHT(EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::TEXT, 2);

        -- Check if we have a current warrant reference and extract year and sequence
        IF v_current_warrant_ref IS NOT NULL AND LENGTH(v_current_warrant_ref) > 0 THEN
            -- Extract the year part from current warrant reference (format: 101/24/00008)
            v_current_ref_year := SPLIT_PART(v_current_warrant_ref, '/', 2);
            
            -- Check if the year has changed
            IF v_current_ref_year = v_current_year THEN
                -- Same year, increment the sequence number
                v_serial_part := SPLIT_PART(v_current_warrant_ref, '/', 3);
                v_current_serial := v_serial_part::INTEGER + 1;
            ELSE
                -- Different year, reset sequence to 1
                v_current_serial := 1;
            END IF;
        ELSE
            -- No previous warrant reference, start with 1
            v_current_serial := 1;
        END IF;

        -- Generate warrant reference (format: 101/25/00001)
        v_warrant_reference := LPAD(v_enforcer_code, 3, '0') || '/' || v_current_year || '/' || LPAD(v_current_serial::TEXT, 5, '0');

        -- Update enforcer warrant sequence
        UPDATE enforcers
        SET warrant_reference_sequence = v_warrant_reference
        WHERE enforcer_id = pi_enforcer_id;
    END IF;

    -- Insert into ENFORCEMENTS table
    INSERT INTO enforcements (
        enforcement_id,
        defendant_account_id,
        result_id,
        posted_date,
        posted_by,
        posted_by_name,
        reason,
        enforcer_id,
        result_responses,
        warrant_reference,
        earliest_release_date,
        jail_days
    ) VALUES (
        v_enforcement_id,
        pi_defendant_account_id,
        pi_result_id,
        CURRENT_TIMESTAMP,
        pi_posted_by,
        pi_posted_by_name,
        pi_reason,
        pi_enforcer_id,
        pi_result_responses,
        v_warrant_reference,
        CASE WHEN v_enforcement_action = 'PRIS' THEN pi_earliest_release_date ELSE NULL END,
        pi_jail_days
    );

    -- Update DEFENDANT_ACCOUNTS table based on enforcement action
    SELECT further_steps_notice_date, fine_registration_date, confiscation_order_date
    INTO v_fsn_date, v_fine_reg_date, v_confiscation_date
    FROM defendant_accounts
    WHERE defendant_account_id = pi_defendant_account_id;

    UPDATE defendant_accounts
    SET 
        last_enforcement = pi_result_id,
        last_movement_date = CURRENT_TIMESTAMP,
        jail_days = CASE WHEN pi_jail_days IS NOT NULL THEN pi_jail_days ELSE jail_days END,
        collection_order = CASE WHEN v_enforcement_action = 'COLLO' THEN TRUE ELSE collection_order END,
        collection_order_date = CASE WHEN v_enforcement_action = 'COLLO' THEN CURRENT_TIMESTAMP ELSE collection_order_date END,
        further_steps_notice_date = CASE 
            WHEN v_enforcement_action = 'FSN' AND v_fsn_date IS NULL THEN CURRENT_TIMESTAMP 
            ELSE further_steps_notice_date 
        END,
        suspended_committal_date = CASE WHEN v_enforcement_action = 'SC' THEN CURRENT_TIMESTAMP ELSE suspended_committal_date END,
        fine_registration_date = CASE 
            WHEN v_enforcement_action = 'REGF' AND v_fine_reg_date IS NULL THEN CURRENT_TIMESTAMP 
            ELSE fine_registration_date 
        END,
        confiscation_order_date = CASE 
            WHEN v_enforcement_action = 'CONF' AND v_confiscation_date IS NULL THEN CURRENT_TIMESTAMP 
            ELSE confiscation_order_date 
        END
    WHERE defendant_account_id = pi_defendant_account_id;

    -- Insert into WARRANT_REGISTER if warrant is generated
    IF v_generates_warrant = TRUE AND pi_enforcer_id IS NOT NULL THEN
        v_warrant_register_id := nextval('warrant_register_id_seq');
        
        INSERT INTO warrant_register (
            warrant_register_id,
            business_unit_id,
            enforcer_id,
            enforcement_id
        ) VALUES (
            v_warrant_register_id,
            pi_business_unit_id,
            pi_enforcer_id,
            v_enforcement_id
        );
    END IF;

    -- Insert into DOCUMENT_INSTANCES for any result documents
    FOR v_document_id IN (
        SELECT document_id
        FROM result_documents
        WHERE result_id = pi_result_id
    ) LOOP
        v_document_instance_id := nextval('document_instance_id_seq');
        
        INSERT INTO document_instances (
            document_instance_id,
            document_id,
            business_unit_id,
            generated_date,
            generated_by,
            associated_record_type,
            associated_record_id,
            status,
            printed_date,
            document_content
        ) VALUES (
            v_document_instance_id,
            v_document_id,
            pi_business_unit_id,
            CURRENT_TIMESTAMP,
            pi_posted_by,
            'enforcements',
            v_enforcement_id,
            'New',
            NULL,
            NULL
        );
    END LOOP;

    -- Insert into REPORT_ENTRIES if warrant is generated
    IF v_generates_warrant = TRUE THEN
        INSERT INTO report_entries (
            report_entry_id,
            business_unit_id,
            report_id,
            entry_timestamp,
            associated_record_type,
            associated_record_id
        ) VALUES (
            nextval('report_entry_id_seq'),
            pi_business_unit_id,
            'warrant_register',
            CURRENT_TIMESTAMP,
            'enforcements',
            v_enforcement_id
        );
    END IF;

    -- Call audit finalise procedure
    CALL p_audit_finalise(
        pi_defendant_account_id,
        pi_record_type,
        pi_business_unit_id,
        pi_posted_by,
        pi_case_reference,
        pi_function_code
    );

    -- Increment version number by 1
    UPDATE defendant_accounts
    SET version_number = version_number + 1
    WHERE defendant_account_id = pi_defendant_account_id
      AND version_number = pi_version_number;

    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;

    IF v_rows_updated = 0 THEN
        RAISE EXCEPTION 'Some information on this page may be out of date.'
            USING ERRCODE = 'P4008'
                , DETAIL = 'p_add_defendant_account_enforcement: Concurrency check failed - version number mismatch';
    END IF;

    RAISE INFO 'p_add_defendant_account_enforcement: Successfully completed. Created enforcement_id = %', v_enforcement_id;

EXCEPTION
    WHEN SQLSTATE 'P4001' OR SQLSTATE 'P4002' OR SQLSTATE 'P4003' OR 
         SQLSTATE 'P4004' OR SQLSTATE 'P4005' OR SQLSTATE 'P4006' OR
         SQLSTATE 'P4007' OR SQLSTATE 'P4008' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_add_defendant_account_enforcement: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_add_defendant_account_enforcement: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_add_defendant_account_enforcement: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_add_defendant_account_enforcement(IN pi_result_id character varying, IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_record_type character varying, IN pi_case_reference character varying, IN pi_function_code character varying, IN pi_jail_days integer, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_reason character varying, IN pi_enforcer_id bigint, IN pi_result_responses json, IN pi_earliest_release_date timestamp without time zone, IN pi_version_number bigint, OUT po_enforcement_id bigint); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_add_defendant_account_enforcement(IN pi_result_id character varying, IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_record_type character varying, IN pi_case_reference character varying, IN pi_function_code character varying, IN pi_jail_days integer, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_reason character varying, IN pi_enforcer_id bigint, IN pi_result_responses json, IN pi_earliest_release_date timestamp without time zone, IN pi_version_number bigint, OUT po_enforcement_id bigint) IS 'Procedure to add a new enforcement action for a defendant account with audit tracking, associated record creation, and concurrency check.';

--
-- Name: p_audit_finalise(bigint, character varying, smallint, character varying, character varying, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_audit_finalise(IN pi_associated_account_id bigint, IN pi_record_type character varying, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_case_reference character varying, IN pi_function_code character varying)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_audit_finalise.sql
*
* DESCRIPTION : Procedure to be called to fetch the current state of values of the auditable amendment data item fields,
*               depending on the record type passed in, from the corresponding database view in the same session as when 
*               p_audit_initialise was called. The values of the fields fetched will be compared with the values in the 
*               audit amendment list that have been stored in the temporary table. Those that have changed will be 
*               inserted into the AMENDMENTS table.
*
* PARAMETERS : pi_associated_account_id  - The Opal defendant account id or creditor account id
*            : pi_record_type            - Can be 'defendant_accounts' or 'creditor_accounts'
*            : pi_business_unit_id       - Business unit ID for the amendment
*            : pi_posted_by              - User ID who made the amendment
*            : pi_case_reference         - Case reference if set by Case Management
*            : pi_function_code          - Function code where amendment was made
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1677 Initial version - Create audit finalise procedure.
*
**/
DECLARE
    v_pg_exception_detail            TEXT;
    v_record_count                   INTEGER;
    v_amendment_count                INTEGER := 0;
    v_current_record                 RECORD;
    v_temp_record                    RECORD;
    v_field_code                     SMALLINT;
    v_update_defendant_last_changed  BOOLEAN := FALSE;
    v_update_creditor_last_changed   BOOLEAN := FALSE;
    v_field_name                     TEXT;
    v_old_value                      TEXT;
    v_new_value                      TEXT;
    v_fields_to_check                TEXT[];
    v_last_changed_fields            TEXT[];
    v_column_exists                  BOOLEAN;
    
BEGIN
    RAISE INFO 'p_audit_finalise: Starting with pi_associated_account_id = %, pi_record_type = %', pi_associated_account_id, pi_record_type;

    -- Validate input parameters
    IF pi_associated_account_id IS NULL THEN
        RAISE EXCEPTION 'Associated account ID cannot be null'
            USING ERRCODE = 'P3005'
                , DETAIL = 'p_audit_finalise: pi_associated_account_id is required';
    END IF;

    IF pi_record_type IS NULL OR pi_record_type NOT IN ('defendant_accounts', 'creditor_accounts') THEN
        RAISE EXCEPTION 'Invalid record type: %. Must be defendant_accounts or creditor_accounts', pi_record_type
            USING ERRCODE = 'P3006'
                , DETAIL = 'p_audit_finalise: pi_record_type must be defendant_accounts or creditor_accounts';
    END IF;

    IF pi_business_unit_id IS NULL THEN
        RAISE EXCEPTION 'Business unit ID cannot be null'
            USING ERRCODE = 'P3007'
                , DETAIL = 'p_audit_finalise: pi_business_unit_id is required';
    END IF;

    IF pi_posted_by IS NULL THEN
        RAISE EXCEPTION 'Posted by cannot be null'
            USING ERRCODE = 'P3008'
                , DETAIL = 'p_audit_finalise: pi_posted_by is required';
    END IF;

    -- Process based on record type
    IF pi_record_type = 'defendant_accounts' THEN
        
        -- Check if temporary table exists
        IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'temp_def_ac_amendment_list') THEN
            RAISE EXCEPTION 'Temporary table temp_def_ac_amendment_list does not exist. Call p_audit_initialise first.'
                USING ERRCODE = 'P3009'
                    , DETAIL = 'p_audit_finalise: temp_def_ac_amendment_list not found';
        END IF;

        -- Get current values from view
        SELECT INTO v_current_record
            defendant_account_id,
            cheque_clearance_period,
            allow_cheques,
            credit_trans_clearance_period,
            allow_writeoffs,
            enf_override_enforcer_id,
            enf_override_result_id,
            enf_override_tfo_lja_id,
            enforcing_court_id,
            collection_order,
            suspended_committal_date,
            account_comments,
            account_note_1,
            account_note_2,
            account_note_3,
            name,
            birth_date,
            age,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            national_insurance_number,
            telephone_home,
            telephone_business,
            telephone_mobile,
            email_1,
            email_2,
            pname,
            paddr1,
            paddr2,
            paddr3,
            pbdate,
            pninumber,
            alias1,
            alias2,
            alias3,
            alias4,
            alias5,
            document_language,
            hearing_language,
            vehicle_make,
            vehicle_registration,
            employee_reference,
            employer_name,
            employer_address_line_1,
            employer_address_line_2,
            employer_address_line_3,
            employer_address_line_4,
            employer_address_line_5,
            employer_postcode,
            employer_telephone,
            employer_email
        FROM v_audit_defendant_accounts
        WHERE defendant_account_id = pi_associated_account_id;

        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        
        IF v_record_count = 0 THEN
            RAISE EXCEPTION 'No defendant account found with ID: %', pi_associated_account_id
                USING ERRCODE = 'P3010'
                    , DETAIL = 'p_audit_finalise: defendant_account_id not found';
        END IF;

        -- Define all fields to check
        v_fields_to_check := ARRAY[
            'cheque_clearance_period', 'allow_cheques', 'credit_trans_clearance_period', 'allow_writeoffs',
            'enf_override_enforcer_id', 'enf_override_result_id', 'enf_override_tfo_lja_id', 'enforcing_court_id',
            'collection_order', 'suspended_committal_date', 'account_comments', 'account_note_1', 'account_note_2',
            'account_note_3', 'name', 'birth_date', 'age', 'address_line_1', 'address_line_2', 'address_line_3',
            'postcode', 'national_insurance_number', 'telephone_home', 'telephone_business', 'telephone_mobile',
            'email_1', 'email_2', 'pname', 'paddr1', 'paddr2', 'paddr3', 'pbdate', 'pninumber', 'alias1',
            'alias2', 'alias3', 'alias4', 'alias5', 'document_language', 'hearing_language', 'vehicle_make',
            'vehicle_registration', 'employee_reference', 'employer_name', 'employer_address_line_1',
            'employer_address_line_2', 'employer_address_line_3', 'employer_address_line_4', 'employer_address_line_5',
            'employer_postcode', 'employer_telephone', 'employer_email'
        ];

        -- Fields that require last_changed_date update
        v_last_changed_fields := ARRAY['name', 'birth_date', 'address_line_1', 'postcode', 'alias1',
            'alias2', 'alias3', 'alias4', 'alias5', 'credit_trans_clearance_period', 'account_comments', 
            'allow_writeoffs', 'enforcing_court_id'];

        -- Loop through all fields and compare
        FOREACH v_field_name IN ARRAY v_fields_to_check
        LOOP
            BEGIN
                -- Get old value directly from temporary table
                EXECUTE format('SELECT %I::TEXT FROM temp_def_ac_amendment_list LIMIT 1', v_field_name) INTO v_old_value;
                -- Get new value directly from view
                EXECUTE format('SELECT %I::TEXT FROM v_audit_defendant_accounts WHERE defendant_account_id = %s LIMIT 1', v_field_name, pi_associated_account_id) INTO v_new_value;
                
                -- Compare values
                IF COALESCE(v_new_value, '') != COALESCE(v_old_value, '') THEN
                    -- Map database column name to audit field name
                    DECLARE
                        v_audit_field_name TEXT;
                    BEGIN
                        CASE v_field_name
                            WHEN 'cheque_clearance_period' THEN v_audit_field_name := 'Cheque Clearance Period';
                            WHEN 'allow_cheques' THEN v_audit_field_name := 'Cheque Hold';
                            WHEN 'credit_trans_clearance_period' THEN v_audit_field_name := 'Credit Transfer Clearance Period';
                            WHEN 'allow_writeoffs' THEN v_audit_field_name := 'Inhibit Write Off';
                            WHEN 'enf_override_enforcer_id' THEN v_audit_field_name := 'Override Enforcer';
                            WHEN 'enf_override_result_id' THEN v_audit_field_name := 'Enforcement Override';
                            WHEN 'enf_override_tfo_lja_id' THEN v_audit_field_name := 'TFOOUT LJA Code';
                            WHEN 'enforcing_court_id' THEN v_audit_field_name := 'Enforcement Court';
                            WHEN 'collection_order' THEN v_audit_field_name := 'Collection Order';
                            WHEN 'suspended_committal_date' THEN v_audit_field_name := 'SC Date';
                            WHEN 'account_comments' THEN v_audit_field_name := 'Comment';
                            WHEN 'account_note_1' THEN v_audit_field_name := 'Free Text Notes 1';
                            WHEN 'account_note_2' THEN v_audit_field_name := 'Free Text Notes 2';
                            WHEN 'account_note_3' THEN v_audit_field_name := 'Free Text Notes 3';
                            WHEN 'name' THEN v_audit_field_name := 'Name';
                            WHEN 'birth_date' THEN v_audit_field_name := 'Date of Birth';
                            WHEN 'age' THEN v_audit_field_name := 'Age';
                            WHEN 'address_line_1' THEN v_audit_field_name := 'Address Line 1';
                            WHEN 'address_line_2' THEN v_audit_field_name := 'Address Line 2';
                            WHEN 'address_line_3' THEN v_audit_field_name := 'Address Line 3';
                            WHEN 'postcode' THEN v_audit_field_name := 'Postcode';
                            WHEN 'national_insurance_number' THEN v_audit_field_name := 'National Insurance Number';
                            WHEN 'telephone_home' THEN v_audit_field_name := 'Home Phone Number';
                            WHEN 'telephone_business' THEN v_audit_field_name := 'Business Phone Number';
                            WHEN 'telephone_mobile' THEN v_audit_field_name := 'Mobile Phone Number';
                            WHEN 'email_1' THEN v_audit_field_name := 'Email Address 1';
                            WHEN 'email_2' THEN v_audit_field_name := 'Email Address 2';
                            WHEN 'pname' THEN v_audit_field_name := 'Parent Name';
                            WHEN 'paddr1' THEN v_audit_field_name := 'Parent Address Line 1';
                            WHEN 'paddr2' THEN v_audit_field_name := 'Parent Address Line 2';
                            WHEN 'paddr3' THEN v_audit_field_name := 'Parent Address Line 3';
                            WHEN 'pbdate' THEN v_audit_field_name := 'Parent Date of Birth';
                            WHEN 'pninumber' THEN v_audit_field_name := 'Parent NI Number';
                            WHEN 'alias1' THEN v_audit_field_name := 'AKA Name 1';
                            WHEN 'alias2' THEN v_audit_field_name := 'AKA Name 2';
                            WHEN 'alias3' THEN v_audit_field_name := 'AKA Name 3';
                            WHEN 'alias4' THEN v_audit_field_name := 'AKA Name 4';
                            WHEN 'alias5' THEN v_audit_field_name := 'AKA Name 5';
                            WHEN 'document_language' THEN v_audit_field_name := 'Document Language';
                            WHEN 'hearing_language' THEN v_audit_field_name := 'Hearing Language';
                            WHEN 'vehicle_make' THEN v_audit_field_name := 'Vehicle Make';
                            WHEN 'vehicle_registration' THEN v_audit_field_name := 'Vehicle Registration';
                            WHEN 'employee_reference' THEN v_audit_field_name := 'Employee Reference';
                            WHEN 'employer_name' THEN v_audit_field_name := 'Employer Name';
                            WHEN 'employer_address_line_1' THEN v_audit_field_name := 'Employer Address Line 1';
                            WHEN 'employer_address_line_2' THEN v_audit_field_name := 'Employer Address Line 2';
                            WHEN 'employer_address_line_3' THEN v_audit_field_name := 'Employer Address Line 3';
                            WHEN 'employer_address_line_4' THEN v_audit_field_name := 'Employer Address Line 4';
                            WHEN 'employer_address_line_5' THEN v_audit_field_name := 'Employer Address Line 5';
                            WHEN 'employer_postcode' THEN v_audit_field_name := 'Employer Postcode';
                            WHEN 'employer_telephone' THEN v_audit_field_name := 'Employer Phone Number';
                            WHEN 'employer_email' THEN v_audit_field_name := 'Employer Email';
                            ELSE v_audit_field_name := NULL;
                        END CASE;
                        
                        -- Get field code using audit field name
                        SELECT field_code INTO v_field_code FROM audit_amendment_fields WHERE data_item = v_audit_field_name;
                    END;
                    
                    -- Only insert if field_code was found
                    IF v_field_code IS NOT NULL THEN
                        -- Insert amendment record
                        INSERT INTO amendments (amendment_id, associated_record_type, associated_record_id, field_code, old_value, new_value, business_unit_id, amended_by, amended_date, case_reference, function_code)
                        VALUES (nextval('amendment_id_seq'), pi_record_type, pi_associated_account_id, v_field_code, v_old_value, v_new_value, pi_business_unit_id, pi_posted_by, CURRENT_TIMESTAMP, pi_case_reference, pi_function_code);
                        
                        v_amendment_count := v_amendment_count + 1;
                        
                        -- Check if this field requires last_changed_date update
                        IF v_field_name = ANY(v_last_changed_fields) THEN
                            v_update_defendant_last_changed := TRUE;
                        END IF;
                    ELSE
                        RAISE NOTICE 'p_audit_finalise: Field code not found for data_item: % (column: %)', v_audit_field_name, v_field_name;
                    END IF;
                END IF;
            EXCEPTION
                WHEN OTHERS THEN
                    -- Log the error and continue with the next field
                    RAISE NOTICE 'p_audit_finalise: Error processing field %, skipping: % - %', v_field_name, SQLSTATE, SQLERRM;
                    CONTINUE;
            END;
        END LOOP;

        -- Update last_changed_date if any relevant fields have changed
        IF v_update_defendant_last_changed THEN
            UPDATE defendant_accounts
            SET last_changed_date = CURRENT_TIMESTAMP
            WHERE defendant_account_id = pi_associated_account_id;
        END IF;

        RAISE INFO 'p_audit_finalise: Processed defendant account with % amendments recorded', v_amendment_count;

    ELSIF pi_record_type = 'creditor_accounts' THEN
        
        -- Check if temporary table exists
        IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'temp_cred_ac_amendment_list') THEN
            RAISE EXCEPTION 'Temporary table temp_cred_ac_amendment_list does not exist. Call p_audit_initialise first.'
                USING ERRCODE = 'P3011'
                    , DETAIL = 'p_audit_finalise: temp_cred_ac_amendment_list not found';
        END IF;

        -- Get current values from view
        SELECT INTO v_current_record
            name,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            creditor_account_id,
            hold_payout,
            pay_by_bacs,
            bank_sort_code,
            bank_account_type,
            bank_account_number,
            bank_account_name,
            bank_account_reference
        FROM v_audit_creditor_accounts
        WHERE creditor_account_id = pi_associated_account_id;

        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        
        IF v_record_count = 0 THEN
            RAISE EXCEPTION 'No creditor account found with ID: %', pi_associated_account_id
                USING ERRCODE = 'P3012'
                    , DETAIL = 'p_audit_finalise: creditor_account_id not found';
        END IF;

        -- Define all fields to check
        v_fields_to_check := ARRAY[
            'name', 'address_line_1', 'address_line_2', 'address_line_3', 'postcode',
            'hold_payout', 'pay_by_bacs', 'bank_sort_code', 'bank_account_type',
            'bank_account_number', 'bank_account_name', 'bank_account_reference'
        ];

        -- Fields that require last_changed_date update
        v_last_changed_fields := ARRAY['name', 'address_line_1', 'postcode', 'hold_payout', 
                                    'pay_by_bacs', 'bank_sort_code', 'bank_account_type',
                                    'bank_account_number', 'bank_account_name', 'bank_account_reference'];

        -- Loop through all fields and compare
        FOREACH v_field_name IN ARRAY v_fields_to_check
        LOOP
            BEGIN
                -- Get old value directly from temporary table
                EXECUTE format('SELECT %I::TEXT FROM temp_cred_ac_amendment_list LIMIT 1', v_field_name) INTO v_old_value;
                -- Get new value directly from view
                EXECUTE format('SELECT %I::TEXT FROM v_audit_creditor_accounts WHERE creditor_account_id = %s LIMIT 1', v_field_name, pi_associated_account_id) INTO v_new_value;
                
                -- Compare values
                IF COALESCE(v_new_value, '') != COALESCE(v_old_value, '') THEN
                    -- Map database column name to audit field name
                    DECLARE
                        v_audit_field_name TEXT;
                    BEGIN
                        CASE v_field_name
                            WHEN 'name' THEN v_audit_field_name := 'Name';
                            WHEN 'address_line_1' THEN v_audit_field_name := 'Address Line 1';
                            WHEN 'address_line_2' THEN v_audit_field_name := 'Address Line 2';
                            WHEN 'address_line_3' THEN v_audit_field_name := 'Address Line 3';
                            WHEN 'postcode' THEN v_audit_field_name := 'Postcode';
                            WHEN 'hold_payout' THEN v_audit_field_name := 'Hold Pay Out';
                            WHEN 'pay_by_bacs' THEN v_audit_field_name := 'Pay by BACS';
                            WHEN 'bank_sort_code' THEN v_audit_field_name := 'BACS Sort Code';
                            WHEN 'bank_account_type' THEN v_audit_field_name := 'BACS Account Type';
                            WHEN 'bank_account_number' THEN v_audit_field_name := 'BACS Account Number';
                            WHEN 'bank_account_name' THEN v_audit_field_name := 'BACS Account Name';
                            WHEN 'bank_account_reference' THEN v_audit_field_name := 'BACS Account Reference';
                            ELSE v_audit_field_name := NULL;
                        END CASE;
                        
                        -- Get field code using audit field name
                        SELECT field_code INTO v_field_code FROM audit_amendment_fields WHERE data_item = v_audit_field_name;
                    END;
                    
                    -- Only insert if field_code was found
                    IF v_field_code IS NOT NULL THEN
                        -- Insert amendment record
                        INSERT INTO amendments (amendment_id, associated_record_type, associated_record_id, field_code, old_value, new_value, business_unit_id, amended_by, amended_date, case_reference, function_code)
                        VALUES (nextval('amendment_id_seq'), pi_record_type, pi_associated_account_id, v_field_code, v_old_value, v_new_value, pi_business_unit_id, pi_posted_by, CURRENT_TIMESTAMP, pi_case_reference, pi_function_code);
                        
                        v_amendment_count := v_amendment_count + 1;
                        
                        -- Check if this field requires last_changed_date update
                        IF v_field_name = ANY(v_last_changed_fields) THEN
                            v_update_creditor_last_changed := TRUE;
                        END IF;
                    ELSE
                        RAISE NOTICE 'p_audit_finalise: Field code not found for data_item: % (column: %)', v_audit_field_name, v_field_name;
                    END IF;
                END IF;
            EXCEPTION
                WHEN OTHERS THEN
                    -- Log the error and continue with the next field
                    RAISE NOTICE 'p_audit_finalise: Error processing field %, skipping: % - %', v_field_name, SQLSTATE, SQLERRM;
                    CONTINUE;
            END;
        END LOOP;

        -- Update last_changed_date if any relevant fields have changed
        IF v_update_creditor_last_changed THEN
            UPDATE creditor_accounts
            SET last_changed_date = CURRENT_TIMESTAMP
            WHERE creditor_account_id = pi_associated_account_id;
        END IF;

        RAISE INFO 'p_audit_finalise: Processed creditor account with % amendments recorded', v_amendment_count;

    END IF;

    -- Clean up temporary tables
    IF pi_record_type = 'defendant_accounts' THEN
        DROP TABLE IF EXISTS temp_def_ac_amendment_list;
    ELSIF pi_record_type = 'creditor_accounts' THEN
        DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
    END IF;

    RAISE INFO 'p_audit_finalise: Successfully completed with % total amendments recorded', v_amendment_count;

EXCEPTION
    WHEN SQLSTATE 'P3005' OR SQLSTATE 'P3006' OR SQLSTATE 'P3007' OR SQLSTATE 'P3008' OR 
         SQLSTATE 'P3009' OR SQLSTATE 'P3010' OR SQLSTATE 'P3011' OR SQLSTATE 'P3012' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_audit_finalise: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_audit_finalise: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_audit_finalise: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_audit_finalise(IN pi_associated_account_id bigint, IN pi_record_type character varying, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_case_reference character varying, IN pi_function_code character varying); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_audit_finalise(IN pi_associated_account_id bigint, IN pi_record_type character varying, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_case_reference character varying, IN pi_function_code character varying) IS 'Procedure to fetch current values of auditable amendment data fields, compare with stored initial values, and record changes in the amendments table.';

--
-- Name: p_audit_initialise(bigint, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_audit_initialise(IN pi_associated_account_id bigint, IN pi_record_type character varying)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_audit_initialise.sql
*
* DESCRIPTION : Procedure to be called at the start of the session to fetch the initial values of the auditable amendment data item fields.
*               Creates temporary tables to store initial data values which will later be used by p_audit_finalise to check for changes.
*
* PARAMETERS : pi_associated_account_id  - The Opal defendant account id or creditor account id
*            : pi_record_type            - Can be 'defendant_accounts' or 'creditor_accounts' to determine which view to query
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 28/08/2025    C Cho       1.0         PO-1666 Initial version - Create audit initialise procedure.
*
**/
DECLARE
    v_pg_exception_detail            TEXT;
    v_record_count                   INTEGER;
    
BEGIN
    RAISE INFO 'p_audit_initialise: Starting with pi_associated_account_id = %, pi_record_type = %', pi_associated_account_id, pi_record_type;

    -- Validate input parameters
    IF pi_associated_account_id IS NULL THEN
        RAISE EXCEPTION 'Associated account ID cannot be null'
            USING ERRCODE = 'P3001'
                , DETAIL = 'p_audit_initialise: pi_associated_account_id is required';
    END IF;

    IF pi_record_type IS NULL OR pi_record_type NOT IN ('defendant_accounts', 'creditor_accounts') THEN
        RAISE EXCEPTION 'Invalid record type: %. Must be defendant_accounts or creditor_accounts', pi_record_type
            USING ERRCODE = 'P3002'
                , DETAIL = 'p_audit_initialise: pi_record_type must be defendant_accounts or creditor_accounts';
    END IF;

    -- Process based on record type
    IF pi_record_type = 'defendant_accounts' THEN
        
        -- Drop temp table if it exists from previous session
        DROP TABLE IF EXISTS temp_def_ac_amendment_list;
        
        -- Create temporary table for defendant accounts
        CREATE TEMP TABLE temp_def_ac_amendment_list AS
        SELECT 
            -- Defendant Accounts fields
            defendant_account_id,
            cheque_clearance_period,
            allow_cheques,
            credit_trans_clearance_period,
            allow_writeoffs,
            enf_override_enforcer_id,
            enf_override_result_id,
            enf_override_tfo_lja_id,
            enforcing_court_id,
            collection_order,
            suspended_committal_date,
            account_comments,
            account_note_1,
            account_note_2,
            account_note_3,
            -- Defendant Party fields
            name,
            birth_date,
            age,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            national_insurance_number,
            telephone_home,
            telephone_business,
            telephone_mobile,
            email_1,
            email_2,
            -- Parent/Guardian Party fields
            pname,
            paddr1,
            paddr2,
            paddr3,
            pbdate,
            pninumber,
            -- Aliases fields
            alias1,
            alias2,
            alias3,
            alias4,
            alias5,
            -- Debtor Details fields
            document_language,
            hearing_language,
            vehicle_make,
            vehicle_registration,
            employee_reference,
            employer_name,
            employer_address_line_1,
            employer_address_line_2,
            employer_address_line_3,
            employer_address_line_4,
            employer_address_line_5,
            employer_postcode,
            employer_telephone,
            employer_email
        FROM v_audit_defendant_accounts
        WHERE defendant_account_id = pi_associated_account_id;

        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        
        IF v_record_count = 0 THEN
            RAISE EXCEPTION 'No defendant account found with ID: %', pi_associated_account_id
                USING ERRCODE = 'P3003'
                    , DETAIL = 'p_audit_initialise: defendant_account_id not found';
        END IF;

        RAISE INFO 'p_audit_initialise: Created temp_def_ac_amendment_list with % records', v_record_count;

    ELSIF pi_record_type = 'creditor_accounts' THEN
        
        -- Drop temp table if it exists from previous session
        DROP TABLE IF EXISTS temp_cred_ac_amendment_list;
        
        -- Create temporary table for creditor accounts
        CREATE TEMP TABLE temp_cred_ac_amendment_list AS
        SELECT 
            -- Party fields
            name,
            address_line_1,
            address_line_2,
            address_line_3,
            postcode,
            -- Creditor Accounts fields
            creditor_account_id,
            hold_payout,
            pay_by_bacs,
            bank_sort_code,
            bank_account_type,
            bank_account_number,
            bank_account_name,
            bank_account_reference
        FROM v_audit_creditor_accounts
        WHERE creditor_account_id = pi_associated_account_id;

        GET DIAGNOSTICS v_record_count = ROW_COUNT;
        
        IF v_record_count = 0 THEN
            RAISE EXCEPTION 'No creditor account found with ID: %', pi_associated_account_id
                USING ERRCODE = 'P3004'
                    , DETAIL = 'p_audit_initialise: creditor_account_id not found';
        END IF;

        RAISE INFO 'p_audit_initialise: Created temp_cred_ac_amendment_list with % records', v_record_count;

    END IF;

    RAISE INFO 'p_audit_initialise: Successfully completed for record_type = %', pi_record_type;

EXCEPTION
    WHEN SQLSTATE 'P3001' OR SQLSTATE 'P3002' OR SQLSTATE 'P3003' OR SQLSTATE 'P3004' THEN
        -- When custom exceptions just re-raise them so they're not manipulated
        RAISE NOTICE 'Error in p_audit_initialise: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        -- Output full exception details
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_audit_initialise: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_audit_initialise: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_audit_initialise(IN pi_associated_account_id bigint, IN pi_record_type character varying); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_audit_initialise(IN pi_associated_account_id bigint, IN pi_record_type character varying) IS 'Procedure to fetch initial values of auditable amendment data fields and store them in temporary tables for later comparison.';

--
-- Name: p_create_account_notes(bigint, character varying, character varying, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_account_notes(IN pi_defendant_account_id bigint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_account_notes_json json)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_account_notes.sql
*
* DESCRIPTION : Process the account notes Json object for the related defendant.
                Updates the account comments column on the DEFENDANT_ACCOUNTS table.
*               Note type AC: defendant_accounts.account_comments will be updated. If more than 1 AC entry is passed an exception is raised.
*                         AN: These will be ignored, as per design.
*                         AA: A maximum of 2 AA records will be inserted into the Notes table, in serial order, all others will be ignored.
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_account_notes_json   - The account notes Json array object related to the defendant
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1035 - Process the account notes Json object for the related defendant and 
*                                                 update the account comments column on the DEFENDANT_ACCOUNTS table
* 16/03/2026    TMc         2.0         PO-2920 - Update columns on NOTES table to use postgresql enum instead of varchar
*                                                 Amended the data type for variable v_note_type so an exception isn't raised before the validation code.
*                                                 This will still let the validation code to be executed and the custom exception will still be raised for an invalid ENUM value.
*                                                 Corrected data type for pi_posted_by and pi_posted_by_name. Now using table NOTES instead of ENFORCEMENTS
*
**/
DECLARE
    v_pg_exception_detail               TEXT;
    v_note_json                         JSON;
    v_note_type                         VARCHAR; --notes.note_type%TYPE;
    v_note_serial                       INTEGER;
    v_note_AC_Text                      defendant_accounts.account_comments%TYPE := NULL;
    v_account_notes_AA_count            INTEGER := 0;
    v_account_notes_AA_inserted_count   INTEGER := 0;
    v_account_notes_AC_count            INTEGER := 0;
BEGIN

    --Check if the passed Json is NULL
    IF pi_account_notes_json IS NULL OR JSON_TYPEOF(pi_account_notes_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_account_notes: There were no account notes to process for defendant_account_id = %', pi_defendant_account_id;

    ELSE 
        
        --Loop through each account note, in account_note_serial order
        FOR v_note_json IN
            SELECT value
              FROM JSON_ARRAY_ELEMENTS(pi_account_notes_json) AS t(value)
            ORDER BY (value ->> 'account_note_serial')::INTEGER
        LOOP
            
            v_note_type   := v_note_json ->> 'note_type';
            v_note_serial := (v_note_json ->> 'account_note_serial')::INTEGER;
            
            CASE v_note_type
                WHEN 'AC' THEN
                    v_account_notes_AC_count := v_account_notes_AC_count + 1;
                    v_note_AC_Text           := v_note_json ->> 'account_note_text';

                WHEN 'AN' THEN

                    --Ignore AN note types

                WHEN 'AA' THEN
                    v_account_notes_AA_count := v_account_notes_AA_count + 1;

                    --Only insert a maximum of two AA records into Notes, ignore the rest
                    IF v_account_notes_AA_count < 3 THEN

                        v_account_notes_AA_inserted_count := v_account_notes_AA_inserted_count + 1;

                        --Insert NOTES record
                        INSERT INTO notes (
                              note_id
                            , note_type
                            , associated_record_type
                            , associated_record_id
                            , note_text
                            , posted_date
                            , posted_by
                            , posted_by_name
                        )
                        VALUES ( 
                              NEXTVAL('note_id_seq')
                            , v_note_type::t_note_type_enum
                            , 'defendant_accounts'
                            , pi_defendant_account_id
                            , v_note_json ->> 'account_note_text'
                            , CLOCK_TIMESTAMP()
                            , pi_posted_by
                            , pi_posted_by_name
                        );

                    END IF;

                ELSE
                    --Raise custom exception
                    RAISE EXCEPTION 'Note_type % is not valid', v_note_type
                        USING ERRCODE = 'P2012'
                            , DETAIL = 'p_create_account_notes: defendant_account_id = ' || pi_defendant_account_id;
            END CASE;
            
        END LOOP;

        RAISE INFO 'p_create_account_notes: Inserted % AA notes records, out of %, for defendant_account_id = %', v_account_notes_AA_inserted_count, v_account_notes_AA_count, pi_defendant_account_id;

        --Perform checks before updating the DEFENDANT_ACCOUNTS table
        IF v_account_notes_AC_count > 1 THEN

            --Raise custom exception     
            RAISE EXCEPTION 'Only one AC note type is expected. Number of AC entries = %', v_account_notes_AC_count
                USING ERRCODE = 'P2013'
                    , DETAIL = 'p_create_account_notes: defendant_account_id = ' || pi_defendant_account_id;

        END IF;

        --Update defendant_accounts table with account comments
        UPDATE defendant_accounts
           SET account_comments = v_note_AC_Text
         WHERE defendant_account_id = pi_defendant_account_id;
        
        RAISE INFO 'p_create_account_notes: defendant_accounts has been updated with account comments. defendant_account_id = %', pi_defendant_account_id;

    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2012' OR SQLSTATE 'P2013' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_account_notes: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_account_notes: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_account_notes: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_account_notes(IN pi_defendant_account_id bigint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_account_notes_json json); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_account_notes(IN pi_defendant_account_id bigint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_account_notes_json json) IS 'Procedure to process the account notes Json object for the related defendant';

--
-- Name: p_create_aliases(bigint, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_aliases(IN pi_party_id bigint, IN pi_aliases_json json)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_aliases.sql
*
* DESCRIPTION : Insert records into ALIASES for the defendant or parent/guardian debtor_details --> aliases Json object.
*
* PARAMETERS  : pi_party_id     - The Opal party id of the associated party of the parent debtor details Json object.
*               pi_aliases_json - The dedendant or parent/guardian debtor_details --> aliases Json object
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------------
* 24/07/2025    TMc         1.0         PO-1043 - Insert records into ALIASES for the defendant or parent/guardian debtor_details --> aliases Json object. 
*
**/
DECLARE
    v_pg_exception_detail   TEXT;
    v_alias_json            JSON;
    v_alias_count           INTEGER := 0;
BEGIN

    --Insert the ALIASES record(s), if the Json passed is not NULL
    IF pi_aliases_json IS NULL OR JSON_TYPEOF(pi_aliases_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_aliases: There were no aliases to process for party_id = %', pi_party_id;

    ELSE 
    
        FOR v_alias_json IN SELECT JSON_ARRAY_ELEMENTS(pi_aliases_json)
        LOOP
            v_alias_count := v_alias_count + 1;
            
            INSERT INTO aliases (
                  alias_id
                , party_id
                , surname
                , forenames
                --, initials
                , sequence_number
                , organisation_name)
            VALUES (
                  NEXTVAL('alias_id_seq')
                , pi_party_id
                , v_alias_json ->> 'alias_surname'
                , v_alias_json ->> 'alias_forenames'
                --, NULL  --Initials not required
                , v_alias_count --sequence_number
                , v_alias_json ->> 'alias_company_name'
            );
        END LOOP;
        
        RAISE INFO 'p_create_aliases: Created aliases for party_id = % - Alias count = %', pi_party_id, v_alias_count;
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_aliases: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        --RAISE;
        RAISE EXCEPTION 'Error in p_create_aliases: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_aliases(IN pi_party_id bigint, IN pi_aliases_json json); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_aliases(IN pi_party_id bigint, IN pi_aliases_json json) IS 'Procedure to insert records into ALIASES for the defendant or parent/guardian debtor_details-aliases Json object.';

--
-- Name: p_create_debtor_details(bigint, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_debtor_details(IN pi_party_id bigint, IN pi_debtor_detail_json json)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_debtor_details.sql
*
* DESCRIPTION : Insert a DEBTOR_DETAIL record for the defendant or parent/guardian debtor_details Json object.
*
* PARAMETERS  : pi_party_id           - The Opal party id of the associated debtor details
*               pi_debtor_detail_json - The dedendant or parent/guardian debtor_details Json object
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------------------------------------
* 23/07/2025    TMc         1.0         PO-1043 - Insert a DEBTOR_DETAIL record for the defendant or parent/guardian debtor_details Json object. 
* 16/03/2026    TMc         2.0         PO-2906 - Remove unused variables: v_document_language, v_hearing_language
*                                                 Amended insert into DEBTOR_DETAIL statement to cast values for DOCUMENT_LANGUAGE and HEARING_LANGUAGE to new ENUM data types
*
**/
DECLARE
    v_pg_exception_detail   TEXT;
    v_aliases_json          JSON := NULL;
BEGIN

    --Insert the DEBTOR_DETAIL record, if the Json passed is not NULL

    IF pi_debtor_detail_json IS NULL OR JSON_TYPEOF(pi_debtor_detail_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_debtor_details: There were no debtor details to process for party_id = %', pi_party_id;

    ELSE 
        INSERT INTO debtor_detail (
              party_id
            , vehicle_make
            , vehicle_registration
            , employer_name
            , employer_address_line_1
            , employer_address_line_2
            , employer_address_line_3
            , employer_address_line_4
            , employer_address_line_5
            , employer_postcode
            , employee_reference
            , employer_telephone
            , employer_email
            , document_language
            , document_language_date
            , hearing_language
            , hearing_language_date
        )
        VALUES (
              pi_party_id
            , pi_debtor_detail_json ->> 'vehicle_make'
            , pi_debtor_detail_json ->> 'vehicle_registration_mark'
            , pi_debtor_detail_json ->> 'employer_company_name'
            , pi_debtor_detail_json ->> 'employer_address_line_1'
            , pi_debtor_detail_json ->> 'employer_address_line_2'
            , pi_debtor_detail_json ->> 'employer_address_line_3'
            , pi_debtor_detail_json ->> 'employer_address_line_4'
            , pi_debtor_detail_json ->> 'employer_address_line_5'
            , pi_debtor_detail_json ->> 'employer_post_code'
            , pi_debtor_detail_json ->> 'employee_reference'
            , pi_debtor_detail_json ->> 'employer_telephone_number'
            , pi_debtor_detail_json ->> 'employer_email_address'
            , (pi_debtor_detail_json ->> 'document_language')::t_language_enum
            , CURRENT_TIMESTAMP
            , (pi_debtor_detail_json ->> 'hearing_language')::t_language_enum
            , CURRENT_TIMESTAMP
        );

        RAISE INFO 'p_create_debtor_details: Created debtor_detail record for party_id = %', pi_party_id;

        --Call p_create_aliases to insert the related ALIASES record(s), for this debtor_details, if the Json object exists
        v_aliases_json := json_extract_path(pi_debtor_detail_json, 'aliases');
    
        CALL p_create_aliases( pi_party_id,
                               v_aliases_json
        );

    END IF;

EXCEPTION
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_debtor_details: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_debtor_details: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_debtor_details(IN pi_party_id bigint, IN pi_debtor_detail_json json); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_debtor_details(IN pi_party_id bigint, IN pi_debtor_detail_json json) IS 'Procedure to insert a DEBTOR_DETAIL record for the defendant or parent/guardian debtor_details Json object.';

--
-- Name: p_create_defendant_account(bigint, smallint, character varying, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_defendant_account(IN pi_draft_account_id bigint, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, OUT po_account_number character varying, OUT po_defendant_account_id bigint)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_defendant_account.sql
*
* DESCRIPTION : The interface procedure to create manual account. It will parse the account json to insert into the substantive tables.
*
* PARAMETERS : pi_draft_account_id       - The draft account id from the DRAFT_ACCOUNTS table to be passed in by the backend to identify the account json to be processed
*            : pi_business_unit_id       - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*            : pi_posted_by              - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*            : pi_posted_by_name         - The user that is submitting the request to be passed in by the backend
*            : po_account_number         - The Opal account number to be generated and returned to the backend
*            : po_defendant_account_id   - The Opal defendant account id to be generated and returned to the backend
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------
* 30/06/2025    A Dennis    1.0         PO-1905 Initial version - Create manual defendant account.
* 21/07/2025    C Cho       2.0         PO-1044 Create Defendant Account 
* 22/07/2025    TMc         3.0         PO-1043 Call new procedure (p_create_defendant_parties) to process the defendant Json and insert records into
*                                               PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES for the defendant and parent/guardian.
*                                       PO-1037 Call new procedure (p_create_fp_offences) to insert a record into FIXED_PENALTY_OFFENCES for the defendant.
*                                       Amended exception handling to raise a custom exception when the enforcing court ID is not valid and to re-raise all custom exceptions without manipulating them.
*                                       All changes commented with 'v3.0'
* 01/08/2024    TMc         4.0         Call to new procedure (p_create_impositions) to process:
*                                               PO-1039 offences and impositions, 
*                                               PO-1043 creditor_account, minor creditor, 
*                                               PO-1039 control_totals, 
*                                               PO-1040 allocations, 
*                                               PO-1038 defendant_transactions, 
*                                               PO-1041 document_instances (compensation notice)
*                                               Also updates defendant_accounts (amount_imposed, amount_paid, amount_balance)
*                                       PO-1036 Added code for payment_card_requests and call to new procedure (p_create_payment_terms) to process payment terms.
*                                       PO-1034 Call to new procedure (p_create_enforcements) to process enforcements.
*                                               Also updates defendant_accounts (last_enforcement)
*                                       PO-1035 Call to new procedure (p_create_account_notes) to process acocunt notes.
*                                       PO-1041 Added code to process document_instances ('TFO Order' then 'TFO Letter')
*                                       PO-1042 Added code to process report_entries
*                                       Changes commented with 'v4.0'
* 28/08/2025    TMc         4.1         PO-2096 Added originator_name parameter when calling p_create_impositions
* 28/08/2025    TMc         4.2         PO-2099 Populate DEFENDANT_ACCOUNTS.VERSION_NUMBER with 1 instead of NULL
* 03/09/2025    TMc         4.3         PO-1044 Populate DEFENDANT_ACCOUNTS.CHEQUE_CLEARANCE_PERIOD, CREDIT_TRANS_CLEARANCE_PERIOD 
*                                               Values for CHEQUE_CLEARANCE_PERIOD and CREDIT_TRANS_CLEARANCE_PERIOD are retrieved from the CONFIGURATION_ITEMS table.
*                                       PO-2118 Populate DEFENDANT_ACCOUNTS.PAYMENT_CARD_REQUESTED_BY_NAME
* 13/10/2025    CL          5.0         PO-2291 - Removed the originator_name parameter from the call to p_create_impositions, as the column has been 
*                                                 removed from the impositions table
* 03/02/2026    TMc         6.0         PO-2751 - Populate new column DEFENDANT_ACCOUNTS.IMPOSED_BY_NAME and amend how DEFENDANT_ACCOUNTS.ORIGINATOR_TYPE is populated.
* 16/03/2026    TMc         7.0         PO-2868 - Update columns on DEFENDANT_ACCOUNTS table to use postgresql enum instead of varchar
*                                                 Amended IF statements to use ENUM value and remove LOWER function around 'IF v_account_type = 'Fixed Penalty' THEN'
*                                       PO-2930 - Update columns on DOCUMENT_INSTANCES table to use postgresql enum instead of varchar
*                                                 Amended INSERT statements for DOCUMENT_INSTANCES, set ASSOCIATED_RECORD_TYPE to a valid ENUM value (defendant_accounts)
**/
DECLARE
    c_ci_cheque_clearance_period       CONSTANT    configuration_items.item_name%TYPE := 'DEFAULT_CHEQUE_CLEARANCE_PERIOD';         --v4.3
    c_ci_credit_trans_clearance_period CONSTANT    configuration_items.item_name%TYPE := 'DEFAULT_CREDIT_TRANS_CLEARANCE_PERIOD';   --v4.3
    
    v_account_type                   defendant_accounts.account_type%TYPE;
    v_account_sentence_date          defendant_accounts.imposed_hearing_date%TYPE;
    v_enforcement_court_id           defendant_accounts.enforcing_court_id%TYPE;
    v_enforcements_json_array        json := NULL;
    --v_enforcements_json_length       smallint;    -- v3.0 Commented out 
    v_offences_json_array            json := NULL;
    v_payment_terms_json             json := NULL;
    /* v3.0 - Commented out
    v_payment_term_type_code         payment_terms.terms_type_code%TYPE;
    v_payment_term_eff_date          payment_terms.effective_date%TYPE := NULL;
    v_payment_term_instal_period     payment_terms.instalment_period%TYPE;
    v_payment_term_instal_amount     payment_terms.instalment_amount%TYPE := NULL;
    v_payment_term_instal_lump_sum   payment_terms.instalment_lump_sum%TYPE;
    v_payment_term_jail_days         payment_terms.jail_days%TYPE;
    */
    v_originator_name                defendant_accounts.originator_name%TYPE;
    v_originator_id                  defendant_accounts.originator_id%TYPE;
    v_fp_ticket_detail_json          json := NULL;
    v_collection_order               defendant_accounts.collection_order%TYPE;
    v_collection_order_date          defendant_accounts.collection_order_date%TYPE;
    v_suspended_committal_date       defendant_accounts.suspended_committal_date%TYPE;
    v_payment_card_requested         defendant_accounts.payment_card_requested%TYPE;
    v_prosecutor_case_reference      defendant_accounts.prosecutor_case_reference%TYPE;
    --v_account_number                 defendant_accounts.account_number%TYPE;          -- v3.0 Commented out 

    -- v3.0 - Start
    v_pg_exception_detail            TEXT;
    v_pg_exception_constraint        TEXT;
    v_defendant_type                 VARCHAR;  --Needed for parties and defendant_account_parties
    v_defendant_json                 json := NULL;
    v_da_account_balance             defendant_accounts.account_balance%TYPE; --Returned from p_create_impositions
    v_last_enforcement               defendant_accounts.last_enforcement%TYPE;
    v_account_notes_json_array       json := NULL;
    v_debtor_document_language       debtor_detail.document_language%TYPE;
    v_document_id                    document_instances.document_id%TYPE;
    v_report_id                      reports.report_id%TYPE;
    -- v3.0 - End
    
    v_originator_type                defendant_accounts.originator_type%TYPE;  -- v4.0
    v_cheque_clearance_period        defendant_accounts.cheque_clearance_period%TYPE;        -- v4.3
    v_credit_trans_clearance_period  defendant_accounts.credit_trans_clearance_period%TYPE;  -- v4.3
    v_imposed_by_name                defendant_accounts.imposed_by_name%TYPE;  -- v6.0

BEGIN
    -- Get the account json from the DRAFT_ACCOUNTS table using the draft_account_id passed in
    SELECT account ->> 'account_type'
         , account ->> 'defendant_type'         -- v3.0
         , account ->> 'account_sentence_date'
         , account ->> 'offences'
         , account ->> 'enforcement_court_id'
         , account ->> 'payment_terms'          -- v3.0
         /* v3.0 - Commented out 
         , account ->  'payment_terms' ->> 'payment_terms_type_code'
         , account ->  'payment_terms' ->> 'effective_date'
         , account ->  'payment_terms' ->> 'instalment_period'
         , account ->  'payment_terms' ->> 'instalment_amount'
         , account ->  'payment_terms' ->> 'lump_sum_amount'
         , account ->  'payment_terms' ->> 'jail_days'
         */
         , account ->  'payment_terms' ->> 'enforcements'
         , account ->> 'originator_name'
         , account ->> 'originator_id' 
         , account ->> 'originator_type'        -- v6.0
         , account ->> 'fp_ticket_detail'
         , account ->> 'collection_order_made'
         , account ->> 'collection_order_date'
         , account ->> 'suspended_committal_date'
         , account ->> 'payment_card_request'
         , account ->> 'prosecutor_case_reference'
         , account ->> 'defendant'              -- v3.0
         , account ->> 'account_notes'          -- v3.0
    INTO STRICT v_account_type                  -- STRICT to raise an exception if the value is NULL. One row needs to be returned otherwise an exception is thrown.
       , v_defendant_type                       -- v3.0
       , v_account_sentence_date
       , v_offences_json_array
       , v_enforcement_court_id
       , v_payment_terms_json                   -- v3.0  
       /* v3.0 - Commented out 
       , v_payment_term_type_code
       , v_payment_term_eff_date
       , v_payment_term_instal_period
       , v_payment_term_instal_amount
       , v_payment_term_instal_lump_sum
       , v_payment_term_jail_days
       */ 
       , v_enforcements_json_array
       , v_originator_name
       , v_originator_id
       , v_originator_type                      -- v6.0
       , v_fp_ticket_detail_json
       , v_collection_order
       , v_collection_order_date
       , v_suspended_committal_date
       , v_payment_card_requested
       , v_prosecutor_case_reference
       , v_defendant_json                       -- v3.0
       , v_account_notes_json_array             -- v4.0
    FROM draft_accounts
    WHERE draft_account_id = pi_draft_account_id
      AND account_number IS NULL;  --v3.0 - Ensures that the draft account is only processed once

    RAISE INFO 'v_account_type                = %', v_account_type;
    RAISE INFO 'v_defendant_type              = %', v_defendant_type;           -- v3.0
    RAISE INFO 'v_account_sentence_date       = %', v_account_sentence_date;
    RAISE INFO 'v_enforcement_court_id        = %', v_enforcement_court_id;
    RAISE INFO 'v_originator_name             = %', v_originator_name;
    RAISE INFO 'v_originator_id               = %', v_originator_id;
    
    --RAISE INFO 'Generated account number: %', v_account_number;  -- v3.0 Commented out

    --Retrieve values for CHEQUE_CLEARANCE_PERIOD and CREDIT_TRANS_CLEARANCE_PERIOD from CONFIGURATION_ITEMS
    --Both SELECT statements added in v4.3
    SELECT item_value 
      INTO STRICT v_cheque_clearance_period         --STRICT to ensure 1 row is returned
      FROM configuration_items
     WHERE item_name = c_ci_cheque_clearance_period;
      
    SELECT item_value 
      INTO STRICT v_credit_trans_clearance_period   --STRICT to ensure 1 row is returned
      FROM configuration_items
     WHERE item_name = c_ci_credit_trans_clearance_period;
    
    -- v6.0 - Added IF statement for imposed_by_name
    IF v_account_type = 'Fixed Penalty' THEN
        SELECT lja.name
          INTO v_imposed_by_name
          FROM courts c 
          JOIN local_justice_areas lja 
            ON lja.local_justice_area_id = c.local_justice_area_id
         WHERE c.court_id = v_enforcement_court_id;
    ELSE
        v_imposed_by_name := v_originator_name;
    END IF;    

    -- Determine report_id - If originator_type is FP then 'fp_register' else 'tfo_in_register'  - v6.0
    IF v_originator_type = 'FP' THEN 
        v_report_id := 'fp_register';
    ELSE 
        v_report_id := 'tfo_in_register';
    END IF;

    -- Insert into DEFENDANT_ACCOUNTS table
    INSERT INTO defendant_accounts(
            defendant_account_id
          , business_unit_id
          , account_number
          , account_type
          , imposed_hearing_date
          , imposing_court_id
          , amount_imposed
          , amount_paid
          , account_balance
          , account_status 
          , completed_date
          , enforcing_court_id
          , last_hearing_court_id
          , last_hearing_date
          , last_movement_date
          , last_changed_date
          , last_enforcement
          , originator_name
          , originator_id
          , originator_type
          , allow_writeoffs
          , allow_cheques
          , cheque_clearance_period
          , credit_trans_clearance_period
          , enf_override_result_id
          , enf_override_enforcer_id
          , enf_override_tfo_lja_id
          , unit_fine_detail
          , unit_fine_value
          , collection_order
          , collection_order_date
          , further_steps_notice_date
          , confiscation_order_date
          , fine_registration_date
          , suspended_committal_date
          , consolidated_account_type
          , payment_card_requested
          , payment_card_requested_date
          , payment_card_requested_by
          , payment_card_requested_by_name  --v4.3
          , prosecutor_case_reference
          , enforcement_case_status
          , account_comments            --v3.0
          , account_note_1              --v3.0
          , account_note_2              --v3.0
          , account_note_3              --v3.0
          , jail_days                   --v3.0
          , version_number              --v4.0
          , imposed_by_name             --v6.0
          )
    VALUES( 
            NEXTVAL('defendant_account_id_seq')
          , pi_business_unit_id
          , f_get_account_number(pi_business_unit_id, 'defendant_accounts')  -- Use the generated account number from f_get_account_number
          , v_account_type
          , v_account_sentence_date
          , NULL                    -- imposing_court_id should be NULL
          , 0.00                    -- amount_imposed  - this will be updated by a later procedure - v4.0: Updated by p_create_impositions
          , 0.00                    -- amount_paid     - this will be updated by a later procedure - v4.0: Updated by p_create_impositions
          , 0.00                    -- account_balance - this will be updated by a later procedure - v4.0: Updated by p_create_impositions
          , 'L'                     -- account_status should be L for Live
          , NULL                    -- completed_date should be NULL
          , v_enforcement_court_id  -- If this doesn't exist in COURTS then a FK violation exception will be raised 
          , NULL                    -- last_hearing_court_id should be NULL
          , NULL                    -- last_hearing_date should be NULL
          --, NULL                    -- last_movement_date should be NULL      v3.0 - Commented out
          , CURRENT_TIMESTAMP       -- last_movement_date                       v3.0 - Added
          , NULL                    -- last_changed_date should be NULL
          , NULL                    -- last_enforcement will be updated when determined below when enforcements are processed - v4.0: Updated by p_create_enforcements
          , v_originator_name
          , v_originator_id
          /*  v4.0 Commented out
          , CASE                    -- originator_type is FP if fp_ticket_detail exists, TFO if no fp_ticket_detail exists
                WHEN v_fp_ticket_detail_json IS NOT NULL THEN 'FP'    
                ELSE 'TFO'
            END
          */
          , v_originator_type       -- v4.0 - Added
          , TRUE                    -- allow_writeoffs should be TRUE
          , TRUE                    -- allow_cheques should be TRUE
          , v_cheque_clearance_period        -- cheque_clearance_period           Amended in v4.3. Was being set to NULL
          , v_credit_trans_clearance_period  -- credit_trans_clearance_period     Amended in v4.3. Was being set to NULL
          , NULL                    -- enf_override_result_id should be NULL
          , NULL                    -- enf_override_enforcer_id should be NULL
          , NULL                    -- enf_override_tfo_lja_id should be NULL
          , NULL                    -- unit_fine_detail should be NULL
          , NULL                    -- unit_fine_value should be NULL
          , v_collection_order
          , v_collection_order_date
          , NULL                    -- further_steps_notice_date should be NULL
          , NULL                    -- confiscation_order_date should be NULL
          , NULL                    -- fine_registration_date should be NULL
          , v_suspended_committal_date
          , NULL                    -- consolidated_account_type should be NULL
          , v_payment_card_requested
          , CASE                    -- payment_card_requested_date should be the current timestamp if payment_card_requested is TRUE
                WHEN v_payment_card_requested = TRUE THEN CURRENT_TIMESTAMP
                ELSE NULL
            END
          , CASE                    -- payment_card_requested_by should be the user id if payment_card_requested is TRUE  
                WHEN v_payment_card_requested = TRUE THEN pi_posted_by     
                ELSE NULL
            END
          , CASE                    -- payment_card_requested_by_name should be pi_posted_by_name if payment_card_requested is TRUE   v4.3
                WHEN v_payment_card_requested = TRUE THEN pi_posted_by_name     
                ELSE NULL
            END
          , v_prosecutor_case_reference 
          , NULL                    -- enforcement_case_status should be NULL
          , NULL                    -- account_comments                 v4.0 - Updated by p_create_account_notes
          , NULL                    -- account_note_1 should be NULL    v4.0
          , NULL                    -- account_note_2 should be NULL    v4.0
          , NULL                    -- account_note_3 should be NULL    v4.0
          , NULL                    -- jail_days                        v4.0 - Updated by p_create_payment_terms
          , 1                       -- version_number                   v4.0 - v4.2 changed from NULL to 1
          , v_imposed_by_name       --v6.0
          )
    RETURNING 
            defendant_account_id
          , account_number 
    INTO 
            po_defendant_account_id
          , po_account_number;

    RAISE INFO 'p_create_defendant_account: defendant_account_id: %. Generated account number: %', po_defendant_account_id, po_account_number;  -- v3.0

    -- Process PARTIES, including DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAILS and ALIASES
    -- v3.0 - Added call to p_create_defendant_parties
    CALL p_create_defendant_parties ( po_defendant_account_id,
                                      v_defendant_type,
                                      v_defendant_json
    );

    -- FIXED_PENALTY_OFFENCES
    -- v3.0 - Added call to p_create_fp_offences
    CALL p_create_fp_offences ( po_defendant_account_id,
                                v_account_type,
                                v_fp_ticket_detail_json
    );

    

    -- PROCESS OFFENCES  (impositions, creditor_accounts, minor creditor, control_totals, allocations, defendant_transactions, document_instances (compensation notice))
    --                    Also updates defendant_accounts (amount_imposed, amount_paid, amount_balance)
    -- v4.0 - Added call to p_create_impositions
    CALL p_create_impositions ( po_defendant_account_id,
                                pi_business_unit_id,
                                pi_posted_by,
                                pi_posted_by_name,                                
                                v_offences_json_array,
                                v_da_account_balance
    );

    -- PAYMENT_CARD_REQUESTS
    -- v4.0 - Added IF statement
    IF v_payment_card_requested THEN

        INSERT INTO payment_card_requests (defendant_account_id)
        VALUES (po_defendant_account_id)
        ON CONFLICT(defendant_account_id) DO NOTHING;   --Ignore if defendant_account_id already exists in the table

    END IF;

    -- PAYMENT_TERMS  -  Must be done after p_create_impositions (i.e. after defendant_transactions POSTED_DATE)
    --                   Also updates defendant_accounts (jail_days)
    -- v4.0 - Added call to p_create_payment_terms
    CALL p_create_payment_terms ( po_defendant_account_id,
                                  v_account_type,
                                  v_da_account_balance,
                                  pi_posted_by,
                                  pi_posted_by_name,
                                  v_payment_terms_json
    );

    -- ENFORCEMENTS  -  Must be done after p_create_payment_terms (i.e. after payment_terms POSTED_DATE)
    --                  Also updates defendant_accounts (last_enforcement)
    -- v4.0 - Added call to p_create_enforcements
    CALL p_create_enforcements ( po_defendant_account_id,
                                 pi_posted_by,
                                 pi_posted_by_name,
                                 v_enforcements_json_array,
                                 v_last_enforcement
    );

    -- ACCOUNT NOTES  -  Must be done after p_create_enforcements (i.e. after enforcements POSTED_DATE)
    --                   Also updates defendant_accounts (account_comments)
    -- v4.0 - Added call to p_create_account_notes
    CALL p_create_account_notes ( po_defendant_account_id,
                                  pi_posted_by,
                                  pi_posted_by_name,
                                  v_account_notes_json_array
    );

    -- DOCUMENT_INSTANCES ('TFO Order' and 'TFO Letter'. 'compensation notice' is done by p_create_impositions)
    -- v4.0 - Added SELECT, both IF and INSERT statement

    --TFO Order - Retrieve the document_language for the debtor and work out the document_id
    SELECT document_language
      INTO STRICT v_debtor_document_language        --STRICT because there should be one debtor_detail record for the debtor
      FROM debtor_detail dt
      JOIN defendant_account_parties dap
        ON dt.party_id = dap.party_id
     WHERE dap.defendant_account_id = po_defendant_account_id
       AND dap.debtor = TRUE;

    IF v_account_type = 'Fixed Penalty' THEN
        v_document_id := 'FINOR';
    ELSE
        v_document_id := 'FINOT';
    END IF;

    IF v_debtor_document_language = 'CY' THEN
        v_document_id := 'CY_' || v_document_id;
    END IF;

    --Insert DOCUMENT_INSTANCES records. 'TFO Order' then 'TFO Letter'
    INSERT INTO document_instances (
          document_instance_id
        , document_id
        , business_unit_id
        , generated_date
        , generated_by
        , associated_record_type
        , associated_record_id
        , status
        , printed_date
        , document_content
    )
    VALUES (
          NEXTVAL('document_instance_id_seq')
        , v_document_id
        , pi_business_unit_id
        , CURRENT_TIMESTAMP        --generated_date
        , pi_posted_by             --generated_by
        , 'defendant_accounts'      --associated_record_type
        , po_defendant_account_id  --associated_record_id
        , 'New'                    --status
        , NULL                     --printed_date
        , NULL                     --document_content
    ),
    (
        NEXTVAL('document_instance_id_seq')
        , 'FINOTA'
        , pi_business_unit_id
        , CURRENT_TIMESTAMP        --generated_date
        , pi_posted_by             --generated_by
        , 'defendant_accounts'      --associated_record_type
        , po_defendant_account_id  --associated_record_id
        , 'New'                    --status
        , NULL                     --printed_date
        , NULL                     --document_content
    );

    RAISE INFO 'p_create_defendant_account: Created document_instances records for defendant_account_id = %', po_defendant_account_id;

    --Insert REPORT_ENTRIES record.
    -- v4.0 - Added IF and INSERT statements    
    INSERT INTO report_entries (
          report_entry_id
        , business_unit_id
        , report_id
        , entry_timestamp
        , reported_timestamp
        , associated_record_type
        , associated_record_id
        , report_instance_id
    )
    VALUES (
          NEXTVAL('report_entry_id_seq')
        , pi_business_unit_id
        , v_report_id
        , CLOCK_TIMESTAMP()        --entry_timestamp
        , NULL                     --reported_timestamp        --PO-1995 make REPORTED_TIMESTAMP nullable
        , 'defendant_accounts'     --associated_record_type
        , po_defendant_account_id  --associated_record_id
        , NULL                     --report_instance_id
    );

    RAISE INFO 'p_create_defendant_account: Created report_entries record for defendant_account_id = %, report_id = %', po_defendant_account_id, v_report_id;

EXCEPTION
    WHEN SQLSTATE 'P2002' OR SQLSTATE 'P2003' OR SQLSTATE 'P2004' OR SQLSTATE 'P2005' OR SQLSTATE 'P2006' OR    --v3.0
         SQLSTATE 'P2008' OR SQLSTATE 'P2009' OR SQLSTATE 'P2010' OR SQLSTATE 'P2011' OR SQLSTATE 'P2012' OR SQLSTATE 'P2013' THEN   --v4.0
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN FOREIGN_KEY_VIOLATION THEN     --v3.0
        --Check for specific FK violations (i.e. enforcing_court_id)
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL, v_pg_exception_constraint = CONSTRAINT_NAME;
        RAISE NOTICE 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;

        IF v_pg_exception_constraint = 'da_enforcing_court_id_fk' THEN
            --Raise custom exception
            RAISE EXCEPTION 'Enforcement court % not found', v_enforcement_court_id 
                USING ERRCODE = 'P2007'
                    , DETAIL = 'p_create_defendant_account: %' || v_pg_exception_detail;
        ELSE
            --Any other FK violation then construct standard exception
            RAISE EXCEPTION 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM 
                USING DETAIL = v_pg_exception_detail;
        END IF;
    WHEN OTHERS THEN
        --v3.0 Output full exception details and added DETAIL to RAISE EXCEPTION
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;           --v3.0 
        RAISE NOTICE 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM;  --v3.0
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;                        --v3.0 
        --RAISE EXCEPTION 'Error in p_create_defendant_account: %', ' SQLSTATE: ' || SQLSTATE || '   SQLERRM: ' || SQLERRM;  --v3.0 - Commented out
        RAISE EXCEPTION 'Error in p_create_defendant_account: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;  --v3.0
END;
$$;

--
-- Name: PROCEDURE p_create_defendant_account(IN pi_draft_account_id bigint, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, OUT po_account_number character varying, OUT po_defendant_account_id bigint); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_defendant_account(IN pi_draft_account_id bigint, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, OUT po_account_number character varying, OUT po_defendant_account_id bigint) IS 'The interface procedure to create manual account. It parses the account Json to insert into the substantive tables.';

--
-- Name: p_create_defendant_parties(bigint, character varying, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_defendant_parties(IN pi_defendant_account_id bigint, IN pi_defendant_type character varying, IN pi_defendant_json json)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_defendant_parties.sql
*
* DESCRIPTION : Process the defendant Json and insert PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES records for the defendant and parent/guardian.
*               Throws 'P2002 - Missing parent/guardian' exception if defendant_type = 'pgToPay' and the parent_guardian Json object is not present.
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_defendant_type       - Type of the defendant account - Adult Or Youth, Parent/Guardian to pay or company
*               pi_defendant_json       - The dedendant Json object from the DRAFT_ACCOUNTS.ACCOUNT Json
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 22/07/2025    TMc         1.0         PO-1043 - Process the defendant Json and insert PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES
*                                                 records for the defendant and parent/guardian.
*                           1.1         Corrected typo in INSERT INTO parties statement
*                           1.2         Amended c_defendant_type_pgToPay to use pgToPay instead of parentOrGuardianToPay
*
**/
DECLARE
    c_account_type_defendant        CONSTANT    parties.account_type%TYPE := 'Defendant';
    c_association_type_defendant    CONSTANT    defendant_account_parties.association_type%TYPE := 'Defendant';
    c_association_type_pg           CONSTANT    defendant_account_parties.association_type%TYPE := 'Parent/Guardian';
    c_defendant_type_pgToPay        CONSTANT    VARCHAR := 'pgToPay';

    v_pg_exception_detail           TEXT;
    v_party_id_defendant            parties.party_id%TYPE := NULL;
    v_party_id_pg                   parties.party_id%TYPE := NULL;

    v_def_pg_json                   JSON := NULL;
    v_debtor_detail_def_json        JSON := NULL;
    v_debtor_detail_pg_json         JSON := NULL;
BEGIN

    -----------------------------------------
    --Process the defendant information
    -----------------------------------------

    --Insert record for the defendant into the PARTIES table
    INSERT INTO parties (
          party_id
        , organisation
        , organisation_name
        , surname
        , forenames
        --, initials
        , title
        , address_line_1
        , address_line_2
        , address_line_3
        , address_line_4
        , address_line_5
        , postcode
        , account_type
        , birth_date
        , age
        , national_insurance_number
        , telephone_home
        , telephone_business
        , telephone_mobile
        , email_1
        , email_2
        , last_changed_date
        /*  Not required for Release 1A
         , driving_licence_number
         , pnc_id
         , nationality1
         , nationality2
         , self_defined_ethnicity
         , observed_ethnicity
         , cro_number
         , occupation
         , gender
         , custody_status
         , prison_number
         , interpreter_language_needs
        */
    )
    VALUES ( 
          NEXTVAL('party_id_seq')
        , (pi_defendant_json ->> 'company_flag')::BOOLEAN
        , pi_defendant_json ->> 'company_name'
        , pi_defendant_json ->> 'surname'
        , pi_defendant_json ->> 'forenames'
        --, NULL  --Initials not required
        , pi_defendant_json ->> 'title'
        , pi_defendant_json ->> 'address_line_1'
        , pi_defendant_json ->> 'address_line_2'
        , pi_defendant_json ->> 'address_line_3'
        , pi_defendant_json ->> 'address_line_4'
        , pi_defendant_json ->> 'address_line_5'
        , pi_defendant_json ->> 'post_code'
        , c_account_type_defendant
        , TO_TIMESTAMP(pi_defendant_json ->> 'dob', 'YYYY-MM-DD')
        , NULL --age
        , pi_defendant_json ->> 'national_insurance_number'
        , pi_defendant_json ->> 'telephone_number_home'
        , pi_defendant_json ->> 'telephone_number_business'
        , pi_defendant_json ->> 'telephone_number_mobile'
        , pi_defendant_json ->> 'email_address_1'
        , pi_defendant_json ->> 'email_address_2'
        , NULL --last_changed_date
        /*  Not required for Release 1A
         , pi_defendant_json ->> 'driving_licence_number'
         , pi_defendant_json ->> 'pnc_id'
         , pi_defendant_json ->> 'nationality_1'
         , pi_defendant_json ->> 'nationality_2'
         , pi_defendant_json ->> 'ethnicity_self_defined'
         , pi_defendant_json ->> 'ethnicity_observed'
         , pi_defendant_json ->> 'cro_number'
         , pi_defendant_json ->> 'occupation'
         , pi_defendant_json ->> 'gender'
         , pi_defendant_json ->> 'custody_status'
         , pi_defendant_json ->> 'prison_number'
         , pi_defendant_json ->> 'interpreter_lang'
        */
    )
    RETURNING party_id
    INTO      v_party_id_defendant;

    RAISE INFO 'p_create_defendant_parties: Created parties record for the defendant. defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_defendant;

    --Insert the related DEFENDANT_ACCOUNT_PARTIES record for the defendant
    INSERT INTO defendant_account_parties (
          defendant_account_party_id
        , defendant_account_id
        , party_id
        , association_type
        , debtor
    )
    VALUES (
          NEXTVAL('defendant_account_party_id_seq')
        , pi_defendant_account_id
        , v_party_id_defendant
        , c_association_type_defendant
        , (pi_defendant_type != c_defendant_type_pgToPay)
    );

    RAISE INFO 'p_create_defendant_parties: Created defendant_account_parties record for the defendant. defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_defendant;

    --Call p_create_debtor_details to insert the related DEBTOR_DETAIL record, including ALIASES, for the defendant, if the Json object exists
    v_debtor_detail_def_json := json_extract_path(pi_defendant_json, 'debtor_detail');

    CALL p_create_debtor_details( v_party_id_defendant,
                                  v_debtor_detail_def_json
    );

    -----------------------------------------
    --Process the parent/guardian information
    -----------------------------------------

    --Insert record for the parent/guardian into the PARTIES table if defendant_type = 'parentOrGuardianToPay'
    IF pi_defendant_type = c_defendant_type_pgToPay THEN

        v_def_pg_json := json_extract_path(pi_defendant_json, 'parent_guardian');
        
        --Check if parent_guardian Json object exists
        IF v_def_pg_json IS NULL OR JSON_TYPEOF(v_def_pg_json) = 'null' THEN

            --Raise custom exception
            RAISE EXCEPTION 'Missing parent/guardian' 
                USING ERRCODE = 'P2002'
                    , DETAIL = 'p_create_defendant_parties: defendant_account_id = ' || pi_defendant_account_id || ', defendant_type = ' || pi_defendant_type;

        ELSE
            --Insert the parent/guardian record into PARTIES table
            INSERT INTO parties (
                  party_id
                , organisation
                , organisation_name
                , surname
                , forenames
                --, initials
                , title
                , address_line_1
                , address_line_2
                , address_line_3
                , address_line_4
                , address_line_5
                , postcode
                , account_type
                , birth_date
                , age
                , national_insurance_number
                , telephone_home
                , telephone_business
                , telephone_mobile
                , email_1
                , email_2
                , last_changed_date
                /*  Not required for Release 1A
                 , driving_licence_number
                 , pnc_id
                 , nationality1
                 , nationality2
                 , self_defined_ethnicity
                 , observed_ethnicity
                 , cro_number
                 , occupation
                 , gender
                 , custody_status
                 , prison_number
                 , interpreter_language_needs
                */
            )
            VALUES ( 
                  NEXTVAL('party_id_seq')
                , (v_def_pg_json ->> 'company_flag')::BOOLEAN
                , v_def_pg_json ->> 'company_name'
                , v_def_pg_json ->> 'surname'
                , v_def_pg_json ->> 'forenames'
                --, NULL  --Initials not required
                , NULL  --title
                , v_def_pg_json ->> 'address_line_1'
                , v_def_pg_json ->> 'address_line_2'
                , v_def_pg_json ->> 'address_line_3'
                , v_def_pg_json ->> 'address_line_4'
                , v_def_pg_json ->> 'address_line_5'
                , v_def_pg_json ->> 'post_code'
                , c_account_type_defendant
                , TO_TIMESTAMP(v_def_pg_json ->> 'dob', 'YYYY-MM-DD')
                , NULL  --age
                , v_def_pg_json ->> 'national_insurance_number'
                , v_def_pg_json ->> 'telephone_number_home'
                , v_def_pg_json ->> 'telephone_number_business'
                , v_def_pg_json ->> 'telephone_number_mobile'
                , v_def_pg_json ->> 'email_address_1'
                , v_def_pg_json ->> 'email_address_2'
                , NULL  --last_changed_date
                /*  Not required for Release 1A
                 , NULL --driving_licence_number
                 , NULL --pnc_id
                 , NULL --nationality_1
                 , NULL --nationality_2
                 , NULL --ethnicity_self_defined
                 , NULL --ethnicity_observed
                 , NULL --cro_number
                 , NULL --occupation
                 , NULL --gender
                 , NULL --custody_status
                 , NULL --prison_number
                 , NULL --interpreter_lang
                */
            )
            RETURNING party_id
            INTO      v_party_id_pg;

            RAISE INFO 'p_create_defendant_parties: Created parties record for the parent_guardian. defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_pg;

            --Insert the related DEFENDANT_ACCOUNT_PARTIES record for the parent/guardian
            INSERT INTO defendant_account_parties (
                  defendant_account_party_id
                , defendant_account_id
                , party_id
                , association_type
                , debtor
            )
            VALUES (
                  NEXTVAL('defendant_account_party_id_seq')
                , pi_defendant_account_id
                , v_party_id_pg
                , c_association_type_pg
                , (pi_defendant_type = c_defendant_type_pgToPay)
            );

            RAISE INFO 'p_create_defendant_parties: Created defendant_account_parties record for the parent_guardian. defendant_account_id = %, party_id = %', pi_defendant_account_id, v_party_id_pg;

            --Call p_create_debtor_details to insert the related DEBTOR_DETAIL record, including ALIASES, for the parent/guardian, if the Json object exists
            v_debtor_detail_pg_json := json_extract_path(v_def_pg_json, 'debtor_detail');
        
            CALL p_create_debtor_details( v_party_id_pg,
                                          v_debtor_detail_pg_json
            );

        END IF;
    END IF;

EXCEPTION
    WHEN SQLSTATE 'P2002' THEN 
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_defendant_parties: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_defendant_parties: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_defendant_parties: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_defendant_parties(IN pi_defendant_account_id bigint, IN pi_defendant_type character varying, IN pi_defendant_json json); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_defendant_parties(IN pi_defendant_account_id bigint, IN pi_defendant_type character varying, IN pi_defendant_json json) IS 'Procedure to process the defendant Json and insert PARTIES, DEFENDANT_ACCOUNT_PARTIES, DEBTOR_DETAIL and ALIASES records for the defendant and parent/guardian.';

--
-- Name: p_create_enforcements(bigint, character varying, character varying, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_enforcements(IN pi_defendant_account_id bigint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_enforcements_json json, OUT po_last_enforcement character varying)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_enforcements.sql
*
* DESCRIPTION : Process the enforcements Json object for the related defendant
*               It also updates DEFENDANT_ACCOUNTS (last_enforcement) as well as returning it
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_enforcements_json    - The defendant enforcements Json array object
*               po_last_enforcement     - The last result_id processed
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 30/07/2025    TMc         1.0         PO-1034 - Process the enforcements Json object for the related defendant
* 03/02/2026    C Cho       1.1         PO-2454, PO-2455 - Replace account_type with enforcement_account_type.
*
**/
DECLARE
	v_pg_exception_detail	TEXT;
    v_enforcement_item_json JSON;
    v_result_id             enforcements.result_id%TYPE;
    v_enforcements_count    INTEGER := 0;
BEGIN

    --Check if the passed Json is NULL
    IF pi_enforcements_json IS NULL OR JSON_TYPEOF(pi_enforcements_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_enforcements: There were no enforcements to process for defendant_account_id = %', pi_defendant_account_id;

    ELSE 
        
        --Loop through each enforcement, in result_id order: COLLO, PRIS, NOENF
        FOR v_enforcement_item_json IN
            SELECT value
              FROM JSON_ARRAY_ELEMENTS(pi_enforcements_json) AS t(value)
            ORDER BY CASE value ->> 'result_id'
                        WHEN 'COLLO' THEN 1
                        WHEN 'PRIS'  THEN 2
                        WHEN 'NOENF' THEN 3
                        ELSE 999
                     END
        LOOP
            v_enforcements_count := v_enforcements_count + 1;
            --Keep track of the result_id for the last enforcement
            v_result_id := v_enforcement_item_json ->> 'result_id';
            
            --Insert ENFORCEMENTS record
            INSERT INTO enforcements (
                  enforcement_id
                , defendant_account_id
                , posted_date
                , posted_by
                , result_id
                , reason
                , enforcer_id
                , jail_days
                , result_responses
                , warrant_reference
                , case_reference
                , hearing_date
                , hearing_court_id
                , enforcement_account_type
                , posted_by_name
            )
            VALUES (
                  NEXTVAL('enforcement_id_seq')
                , pi_defendant_account_id
                , CLOCK_TIMESTAMP()
                , pi_posted_by
                , v_result_id
                , NULL --reason
                , NULL --enforcer_id
                , NULL --jail_days
                , v_enforcement_item_json -> 'enforcement_result_responses'   --This stores only the Json array, without the "enforcement_result_responses" wrapper
                , NULL --warrant_reference
                , NULL --case_reference
                , NULL --hearing_date
                , NULL --hearing_court_id
                , NULL --enforcement_account_type
                , pi_posted_by_name
            );
            
        END LOOP;

        RAISE INFO 'p_create_enforcements: last enforcement result ID = %', v_result_id;
        RAISE INFO 'p_create_enforcements: Inserted % enforcements records for defendant_account_id = %', v_enforcements_count, pi_defendant_account_id;

        --Update the DEFENDANT_ACCOUNTS table with last_enforcement
        UPDATE defendant_accounts
           SET last_enforcement = v_result_id
         WHERE defendant_account_id = pi_defendant_account_id
        RETURNING last_enforcement INTO STRICT po_last_enforcement;  --Returning STRICT to ensure 1 record is found and updated;

        RAISE INFO 'p_create_enforcements: defendant_accounts.last_enforcement [%] has been updated. defendant_account_id = %', po_last_enforcement, pi_defendant_account_id;

    END IF;

EXCEPTION 
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_enforcements: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_enforcements: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_enforcements(IN pi_defendant_account_id bigint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_enforcements_json json, OUT po_last_enforcement character varying); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_enforcements(IN pi_defendant_account_id bigint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_enforcements_json json, OUT po_last_enforcement character varying) IS 'Procedure to process the enforcements Json object for the related defendant';

--
-- Name: p_create_fp_offences(bigint, public.t_da_account_type_enum, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_fp_offences(IN pi_defendant_account_id bigint, IN pi_account_type public.t_da_account_type_enum, IN pi_fp_ticket_detail_json json)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_fp_offences.sql
*
* DESCRIPTION : Insert record into FIXED_PENALTY_OFFENCES for the defendant.
*               Throws 'P2011 - Missing ticket number' exception if account type is 'fixed penalty' and the ticket number is not present in the passed Json object.
*
* PARAMETERS  : pi_defendant_account_id  - The Opal defendant account id associated with the fp_ticket_detail Json object.
*               pi_account_type          - The account type of the associated defendant_accounts record.
*               pi_fp_ticket_detail_json - The fixed penalty offence Json object related to the dedendant.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1037 - Insert record into FIXED_PENALTY_OFFENCES for the defendant. 
* 27/08/2025    TMc         2.0         PO-2084 - Amended how OFFENCE_DATE and ISSUED_DATE columns are populated.
* 16/03/2026    TMc         3.0         PO-2868 - Update columns on DEFENDANT_ACCOUNTS table to use postgresql enum instead of varchar
*                                                 Amended statement to use ENUM value and remove LOWER function: 'v_vehicle_fixed_penalty := (pi_account_type = 'Fixed Penalty')'
*                                                 Added drop statement as signature has changed.
*
**/
DECLARE
    v_pg_exception_detail   TEXT;
    v_vehicle_fixed_penalty BOOLEAN;
    v_ticket_number         VARCHAR := NULL;
BEGIN
    
    v_vehicle_fixed_penalty := (pi_account_type = 'Fixed Penalty');
    v_ticket_number := pi_fp_ticket_detail_json ->> 'notice_number';

    --Check if the account type is Fixed Penalty and a ticket_number (i.e. notice_number) is present in the passed Json object
    IF v_vehicle_fixed_penalty AND v_ticket_number IS NULL THEN

        --Raise custom exception
        RAISE EXCEPTION 'Missing ticket number' 
            USING ERRCODE = 'P2011'
                , DETAIL = 'p_create_fp_offences: pi_defendant_account_id = ' || pi_defendant_account_id || ', pi_account_type = ' || pi_account_type;

    END IF;

    --Check if passed Json is NULL
    IF pi_fp_ticket_detail_json IS NULL OR JSON_TYPEOF(pi_fp_ticket_detail_json) = 'null' THEN

        --Do Nothing
        RAISE INFO 'p_create_fp_offences: There were no FP ticket details to process for defendant_account_id = %', pi_defendant_account_id;

    ELSE 
    
        --Insert the FIXED_PENALTY_OFFENCES record
        INSERT INTO fixed_penalty_offences (
              defendant_account_id
            , ticket_number
            , vehicle_registration
            , offence_location
            , notice_number
            , issued_date
            , licence_number
            , vehicle_fixed_penalty
            , offence_date
            , offence_time)
        VALUES (
              pi_defendant_account_id
            , v_ticket_number
            , pi_fp_ticket_detail_json ->> 'fp_registration_number'
            , pi_fp_ticket_detail_json ->> 'place_of_offence'
            , pi_fp_ticket_detail_json ->> 'notice_to_owner_hirer'
            , TO_DATE(pi_fp_ticket_detail_json ->> 'date_of_issue', 'YYYY-MM-DD') --issued_date
            , pi_fp_ticket_detail_json ->> 'fp_driving_licence_number'
            , v_vehicle_fixed_penalty
            , TO_DATE(pi_fp_ticket_detail_json ->> 'date_of_issue', 'YYYY-MM-DD') --offence_date
            , pi_fp_ticket_detail_json ->> 'time_of_issue' --offence_time
        );
        
        RAISE INFO 'p_create_fp_offences: Created fixed_penalty_offences record for defendant_account_id = %', pi_defendant_account_id;
    END IF;

EXCEPTION
    WHEN SQLSTATE 'P2011' THEN
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_fp_offences: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        --Log error and re-raise the exception ensuring the caller has access to the complete exception details.
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_fp_offences: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_fp_offences: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_fp_offences(IN pi_defendant_account_id bigint, IN pi_account_type public.t_da_account_type_enum, IN pi_fp_ticket_detail_json json); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_fp_offences(IN pi_defendant_account_id bigint, IN pi_account_type public.t_da_account_type_enum, IN pi_fp_ticket_detail_json json) IS 'Procedure to insert a record into FIXED_PENALTY_OFFENCES for the defendant fp_ticket_detail Json object.';

--
-- Name: p_create_impositions(bigint, smallint, character varying, character varying, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_impositions(IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_offences_json json, OUT po_da_account_balance numeric)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_impositions.sql
*
* DESCRIPTION : Process the Offences Json object for the related defendant.
*               Tables inserted into: IMPOSITIONS, CONTROL_TOTALS, DEFENDANT_TRANSACTIONS, ALLOCATIONS, DOCUMENT_INSTANCES (compensation notice)
*                                     and CREDITOR_ACCOUNTS and PARTIES (minor creditor) if necessary
*               It also updates DEFENDANT_ACCOUNTS (amount_imposed, amount_paid, amount_balance)
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_business_unit_id     - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_offences_json        - The dedendant offences Json array object
*               po_da_account_balance   - The calculated defendant account balance
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 28/07/2025    TMc         1.0         PO-1038, PO-1039, PO-1040 - Processes the Offences Json object for the related defendant.
* 28/08/2025    TMc         1.1         PO-2096 - Added pi_originator_name and populate new column IMPOSITIONS.ORIGINATOR_NAME with the passed value.
* 13/10/2025    CL          2.0         PO-2291 - Removed the originator_name parameter from signature and insert statement, as the column has been 
*                                                 removed from the impositions table
*
**/
DECLARE
	v_pg_exception_detail           TEXT;
    v_pg_exception_constraint       TEXT;
    v_custom_exception_code         TEXT;
    v_custom_exception_msg          TEXT;

    v_offence_json                  JSON;
    v_offence_count                 INTEGER := 0;
    v_impositions_count             INTEGER := 0;
    v_impositions_json              JSON;
    v_imposition_json               JSON;
    v_imposition_id                 impositions.imposition_id%TYPE;
    v_impositions_result_id         impositions.result_id%TYPE;
    v_offence_id                    offences.offence_id%TYPE;
    v_offence_cjs_code              offences.cjs_code%TYPE;
    v_offence_offence_title         offences.offence_title%TYPE;
    v_offence_imposing_court_id     impositions.imposing_court_id%TYPE;

    v_imposed_amount                impositions.imposed_amount%TYPE;
    v_imposed_amount_total          impositions.imposed_amount%TYPE := 0;
    v_paid_amount                   impositions.paid_amount%TYPE;
    v_paid_amount_total             impositions.paid_amount%TYPE    := 0;

    v_defendant_transactions_id     defendant_transactions.defendant_transaction_id%TYPE := NULL;
    v_dt_updated_transaction_amount defendant_transactions.transaction_amount%TYPE;

    v_creditor_account_id           creditor_accounts.creditor_account_id%TYPE;
    v_is_minor_creditor             BOOLEAN;
    v_results_mapped_item_number    control_totals.item_number%TYPE;
BEGIN

    --Process each Offence object within the Offences array
    IF pi_offences_json IS NULL OR JSON_TYPEOF(pi_offences_json) = 'null' THEN

        --Do nothing for now

    ELSE
        <<offences_loop>>
        FOR v_offence_json IN SELECT json_array_elements(pi_offences_json)
        LOOP
            v_offence_count             := v_offence_count + 1;
            v_offence_id                := (v_offence_json ->> 'offence_id')::BIGINT;
            v_offence_imposing_court_id := (v_offence_json ->> 'imposing_court_id')::BIGINT;

            --Retrieve the Offence details from OFFENCES. Raise an exception if not found or more than 1 record is returned.
            BEGIN
                SELECT cjs_code
                     , offence_title
                  INTO STRICT v_offence_cjs_code
                            , v_offence_offence_title
                  FROM offences
                 WHERE offence_id = v_offence_id;
            EXCEPTION
                --When NO_DATA_FOUND or TOO_MANY_ROWS raise custom exception otherwise re-raise the original
                WHEN SQLSTATE 'P0002' OR SQLSTATE 'P0003' THEN
                    RAISE EXCEPTION 'Offence % not found', v_offence_id 
                        USING ERRCODE = 'P2009'
                            , DETAIL = 'p_create_impositions: defendant_account_id = ' || pi_defendant_account_id || ', offence_id = ' || v_offence_id;
                WHEN OTHERS THEN
                    RAISE;
            END;

            --Process the impositions Json
            v_impositions_json := v_offence_json ->> 'impositions';

            <<impositions_loop>>
            FOR v_imposition_json IN SELECT json_array_elements(v_impositions_json)
            LOOP
                v_impositions_count     := v_impositions_count + 1;
                v_impositions_result_id := v_imposition_json ->> 'result_id';
                v_imposed_amount        := (v_imposition_json ->> 'amount_imposed')::NUMERIC(18,2);
                v_paid_amount           := (v_imposition_json ->> 'amount_paid')::NUMERIC(18,2);

                --Keep a running total of imposed_amount and paid_amount to update DEFENDANT_ACCOUNTS and DEFENDANT_TRANSACTIONS (transaction_amount) tables later
                v_imposed_amount_total := v_imposed_amount_total + v_imposed_amount;
                v_paid_amount_total    := v_paid_amount_total + v_paid_amount;

                -- Get CREDITOR_ACCOUNT 
                CALL p_get_creditor_account ( pi_business_unit_id,
                                              v_imposition_json,
                                              v_creditor_account_id,
                                              v_is_minor_creditor,
                                              v_results_mapped_item_number
                );

                --Insert IMPOSITIONS record
                INSERT INTO impositions (
                      imposition_id
                    , defendant_account_id
                    , posted_date
                    , posted_by
                    , posted_by_name
                    , original_posted_date
                    , result_id
                    , imposing_court_id
                    , imposed_date
                    , imposed_amount
                    , paid_amount
                    , offence_id
                    , offence_title
                    , offence_code
                    , creditor_account_id
                    , unit_fine_adjusted
                    , unit_fine_units
                    , completed                    
                    , original_imposition_id
                )
                VALUES (
                      NEXTVAL('imposition_id_seq')
                    , pi_defendant_account_id
                    , CLOCK_TIMESTAMP()
                    , pi_posted_by
                    , pi_posted_by_name
                    , CLOCK_TIMESTAMP()
                    , v_impositions_result_id               --If this doesn't exist in RESULTS then a FK violation exception will be raised
                    , v_offence_imposing_court_id           --If this is present and doesn't exist in COURTS then a FK violation exception will be raised
                    , TO_TIMESTAMP(v_offence_json ->> 'date_of_sentence', 'YYYY-MM-DD')  --imposed_date
                    , 0 - v_imposed_amount                  --Store as a negative value
                    , v_paid_amount
                    , v_offence_id                          --If this is present and doesn't exist in OFFENCES then a FK violation exception will be raised
                    , v_offence_offence_title
                    , v_offence_cjs_code
                    , v_creditor_account_id
                    , NULL  --unit_fine_adjusted
                    , NULL  --unit_fine_units
                    , FALSE --completed                    
                    , NULL  --original_imposition_id
                )
                RETURNING imposition_id
                INTO      v_imposition_id;

                --Insert CONTROL_TOTALS record for the imposition
                INSERT INTO control_totals (
                      control_total_id
                    , business_unit_id
                    , item_number
                    , amount
                    , associated_record_type
                    , associated_record_id
                    , ct_report_instance_id
                    , qe_report_instance_id
                )
                VALUES (
                      NEXTVAL('control_total_id_seq')
                    , pi_business_unit_id
                    , v_results_mapped_item_number
                    , (0 - v_imposed_amount) + v_paid_amount
                    , 'impositions'
                    , v_imposition_id
                    , NULL  --ct_report_instance_id
                    , NULL  --qe_report_instance_id
                );

                --Insert ALLOCATIONS record and DEFENDANT_TRANSACTIONS record when needed (i.e. amount_paid > 0). Only create 1 record
                IF v_paid_amount > 0 THEN

                    --Insert DEFENDANT_TRANSACTIONS record, if not already created. 
                    --TRANSACTION_AMOUNT is initially set to 0 and is updated after all Impositions has been processed
                    IF v_defendant_transactions_id IS NULL THEN
                     
                        INSERT INTO defendant_transactions (
                              defendant_transaction_id
                            , defendant_account_id
                            , posted_date
                            , posted_by
                            , transaction_type
                            , transaction_amount
                            , payment_method
                            , payment_reference
                            , "text"
                            , status
                            , status_date
                            , status_amount
                            , write_off_code
                            , associated_record_type
                            , associated_record_id
                            , imposed_amount
                            , posted_by_name
                        )
                        VALUES (
                              NEXTVAL('defendant_transaction_id_seq')
                            , pi_defendant_account_id
                            , CLOCK_TIMESTAMP()
                            , pi_posted_by
                            , 'TFO IN'
                            , 0                 --This will be updated once all Impositions have been processed
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , NULL
                            , pi_posted_by_name
                        )
                        RETURNING defendant_transaction_id
                        INTO      v_defendant_transactions_id;
                    END IF;

                    --Insert ALLOCATIONS record
                    INSERT INTO allocations (
                          allocation_id
                        , imposition_id
                        , allocated_date
                        , allocated_amount
                        , transaction_type
                        , allocation_function
                        , defendant_transaction_id
                    )
                    VALUES (
                          NEXTVAL('allocation_id_seq')
                        , v_imposition_id
                        , CURRENT_TIMESTAMP
                        , v_paid_amount
                        , 'TFO IN'
                        , 'MAC'
                        , v_defendant_transactions_id
                    );
                    
                END IF;

                --Insert DOCUMENT_INSTANCES record (compensation notice) if result_id = 'FCOMP' and the creditor is a minor creditor
                IF v_impositions_result_id = 'FCOMP' AND v_is_minor_creditor THEN

                    INSERT INTO document_instances (
                          document_instance_id
                        , document_id
                        , business_unit_id
                        , generated_date
                        , generated_by
                        , associated_record_type
                        , associated_record_id
                        , status
                        , printed_date
                        , document_content
                    )
                    VALUES (
                          NEXTVAL('document_instance_id_seq')
                        , 'COMPLETT'            --document_id
                        , pi_business_unit_id
                        , CURRENT_TIMESTAMP     --generated_date
                        , pi_posted_by          --generated_by
                        , 'impositions'         --associated_record_type
                        , v_imposition_id       --associated_record_id
                        , 'New'                 --status
                        , NULL                  --printed_date
                        , NULL                  --document_content
                    );
                END IF;
                
            END LOOP impositions_loop;

        END LOOP offences_loop;

        RAISE INFO 'p_create_impositions: Processed % offences, % impositions', v_offence_count, v_impositions_count;

        --Update DEFENDANT_TRANSACTIONS record. Set TRANSACTION_AMOUNT to the AMOUNT_PAID total if it's GT 0
        IF v_paid_amount_total > 0 THEN

            UPDATE defendant_transactions
               SET transaction_amount = v_paid_amount_total
             WHERE defendant_transaction_id = v_defendant_transactions_id
            RETURNING transaction_amount INTO STRICT v_dt_updated_transaction_amount;   --Returning STRICT to ensure 1 record is found and updated

            RAISE INFO 'p_create_impositions: defendant_transactions has been updated. defendant_transaction_id = %, transaction_amount = %', v_defendant_transactions_id, v_paid_amount_total;
        END IF;
 
        --Update DEFENDANT_ACCOUNTS (amount_imposed, amount_paid, amount_balance)
        UPDATE defendant_accounts
           SET amount_imposed  = (0 - v_imposed_amount_total)
             , amount_paid     = v_paid_amount_total
             , account_balance = (0 - v_imposed_amount_total) + v_paid_amount_total
         WHERE defendant_account_id = pi_defendant_account_id
        RETURNING account_balance INTO STRICT po_da_account_balance;   --Returning STRICT to ensure 1 record is found and updated

        RAISE INFO 'p_create_impositions: defendant_accounts has been updated. defendant_account_id = %', pi_defendant_account_id;
    END IF;

    --If no offences were found then raise custom error
    IF v_offence_count = 0 THEN

        --Raise custom exception
        RAISE EXCEPTION 'Offence not found' 
            USING ERRCODE = 'P2009'
                , DETAIL = 'p_create_impositions: There were no Offences to process. defendant_account_id = %' || pi_defendant_account_id;

    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2009' OR SQLSTATE 'P2005' OR SQLSTATE 'P2010' OR SQLSTATE 'P2004' OR SQLSTATE 'P2006' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE;
    
    WHEN FOREIGN_KEY_VIOLATION THEN
        --Check for specific FK violations (i.e. imposing_court_id, offence_id, result_id)
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL, v_pg_exception_constraint = CONSTRAINT_NAME;
        RAISE NOTICE 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;

        --IF v_pg_exception_constraint = 'imp_defendant_account_id_fk' THEN 

        --    v_custom_exception_code := NULL;
        --    v_custom_exception_msg  := format('defendant_account_id %L does not exist!', v_defendant_account_id);

        IF v_pg_exception_constraint = 'imp_result_id_fk' THEN 

            v_custom_exception_code := 'P2004';
            v_custom_exception_msg  := format('Result %L is not valid', v_impositions_result_id);

        ELSIF v_pg_exception_constraint = 'imp_imposing_court_id_fk' THEN 

            v_custom_exception_code := 'P2008';
            v_custom_exception_msg  := format('Imposing court %L not found', v_offence_imposing_court_id);

        ELSIF v_pg_exception_constraint = 'imp_offence_id_fk' THEN

            v_custom_exception_code := 'P2009';
            v_custom_exception_msg  := format('Offence %L not found', v_offence_id);

        ELSE
            --Any other FK violation then construct standard message
            v_custom_exception_code := NULL;
            v_custom_exception_msg  := format('Error in p_create_impositions: %s - %s', SQLSTATE, SQLERRM);

        END IF;
        
        IF v_custom_exception_code IS NULL THEN  
            --Raise generic exception
            RAISE EXCEPTION  
                USING MESSAGE = v_custom_exception_msg
                    , DETAIL = v_pg_exception_detail;
        ELSE
            --Raise custom exception
            RAISE EXCEPTION 
                USING MESSAGE = v_custom_exception_msg
                    , ERRCODE = v_custom_exception_code
                    , DETAIL = 'p_create_impositions: ' || v_pg_exception_detail;
        END IF;

    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_impositions: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_impositions(IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_offences_json json, OUT po_da_account_balance numeric); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_impositions(IN pi_defendant_account_id bigint, IN pi_business_unit_id smallint, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_offences_json json, OUT po_da_account_balance numeric) IS 'Processes the Offences Json object for the related defendant.';

--
-- Name: p_create_minor_creditor(smallint, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_minor_creditor(IN pi_business_unit_id smallint, IN pi_minor_creditor_json json, OUT po_creditor_account_id bigint)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_minor_creditor.sql
*
* DESCRIPTION : Create new CREDITOR_ACCOUNTS and PARTIES records for a minor creditor.
*
* PARAMETERS  : pi_business_unit_id    - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*               pi_minor_creditor_json - The minor_creditor Json object used to create the CREDITOR_ACCOUNT and PARTIES records
*               po_creditor_account_id - The creditor_account_id to be generated and returned
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------
* 29/07/2025    TMc         1.0         PO-1043 - Create new CREDITOR_ACCOUNTS and PARTIES records for a minor creditor.
* 28/08/2025    TMc         1.1         PO-2099 - Populate new column CREDITOR_ACCOUNTS.VERSION_NUMBER with 1.
*
**/
DECLARE
    c_account_type_creditor CONSTANT    parties.account_type%TYPE := 'Creditor';
	v_pg_exception_detail	TEXT;

    v_party_id              parties.party_id%TYPE := NULL;
    v_pay_by_bacs           creditor_accounts.pay_by_bacs%TYPE;
    v_bank_sort_code        creditor_accounts.bank_sort_code%TYPE;
    v_bank_account_number   creditor_accounts.bank_account_number%TYPE;
    v_bank_account_name     creditor_accounts.bank_account_name%TYPE;
    v_bank_account_ref      creditor_accounts.bank_account_reference%TYPE;
    v_bank_account_type     creditor_accounts.bank_account_type%TYPE;
BEGIN

    --Check to ensure the passed Json is not NULL
    IF pi_minor_creditor_json IS NULL OR JSON_TYPEOF(pi_minor_creditor_json) = 'null' THEN

        --Raise custom exception
        RAISE EXCEPTION 'Missing creditor' 
            USING ERRCODE = 'P2005'
                , DETAIL = 'p_create_minor_creditor: Passed minor_creditor Json was missing or empty.';

    ELSE 
        --Check bank account details are present if pay_by_bacs is TRUE
        v_pay_by_bacs := (pi_minor_creditor_json ->> 'pay_by_bacs')::BOOLEAN;

        v_bank_sort_code      := pi_minor_creditor_json ->> 'bank_sort_code';
        v_bank_account_number := pi_minor_creditor_json ->> 'bank_account_number';
        v_bank_account_name   := pi_minor_creditor_json ->> 'bank_account_name';
        v_bank_account_ref    := pi_minor_creditor_json ->> 'bank_account_ref';
        v_bank_account_type   := pi_minor_creditor_json ->> 'bank_account_type';

        IF COALESCE(v_pay_by_bacs, TRUE) THEN 
            IF v_pay_by_bacs IS NULL OR
               COALESCE(LENGTH(TRIM(v_bank_sort_code)), 0)      = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_number)), 0) = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_name)), 0)   = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_ref)), 0)    = 0 OR
               COALESCE(LENGTH(TRIM(v_bank_account_type)), 0)   = 0 THEN 

                --Raise an exception. One or more bank details are missing or not populated
                RAISE EXCEPTION 'Missing bank detail' 
                    USING ERRCODE = 'P2010'
                        , DETAIL = 'p_create_minor_creditor: Passed bank details: ' || json_build_object(
                                                                                            'pay_by_bacs', v_pay_by_bacs,
                                                                                            'bank_sort_code', v_bank_sort_code,
                                                                                            'bank_account_number', v_bank_account_number,
                                                                                            'bank_account_name', v_bank_account_name,
                                                                                            'bank_account_ref', v_bank_account_ref,
                                                                                            'bank_account_type', v_bank_account_type
                                                                                       );
            END IF;
        END IF;

        --Insert the PARTIES record for the minor creditor
        INSERT INTO parties (
              party_id
            , organisation
            , organisation_name
            , surname
            , forenames
            --, initials
            , title
            , address_line_1
            , address_line_2
            , address_line_3
            , address_line_4
            , address_line_5
            , postcode
            , account_type
            , birth_date
            , age
            , national_insurance_number
            , telephone_home
            , telephone_business
            , telephone_mobile
            , email_1
            , email_2
            , last_changed_date
            /*  Not required for Release 1A
             , driving_licence_number
             , pnc_id
             , nationality1
             , nationality2
             , self_defined_ethnicity
             , observed_ethnicity
             , cro_number
             , occupation
             , gender
             , custody_status
             , prison_number
             , interpreter_language_needs
            */
        )
        VALUES ( 
              NEXTVAL('party_id_seq')
            , (pi_minor_creditor_json ->> 'company_flag')::BOOLEAN
            , pi_minor_creditor_json ->> 'company_name'
            , pi_minor_creditor_json ->> 'surname'
            , pi_minor_creditor_json ->> 'forenames'
            --, NULL  --Initials not required
            , pi_minor_creditor_json ->> 'title'
            , pi_minor_creditor_json ->> 'address_line_1'
            , pi_minor_creditor_json ->> 'address_line_2'
            , pi_minor_creditor_json ->> 'address_line_3'
            , pi_minor_creditor_json ->> 'address_line_4'
            , pi_minor_creditor_json ->> 'address_line_5'
            , pi_minor_creditor_json ->> 'post_code'
            , c_account_type_creditor
            , TO_TIMESTAMP(pi_minor_creditor_json ->> 'dob', 'YYYY-MM-DD')
            , NULL  --age
            , NULL  --national_insurance_number
            , pi_minor_creditor_json ->> 'telephone'   --telephone_home
            , NULL  --telephone_business
            , NULL  --telephone_mobile
            , pi_minor_creditor_json ->> 'email_address'
            , NULL  --email_2
            , NULL  --last_changed_date
            /*  Not required for Release 1A
             , NULL --driving_licence_number
             , NULL --pnc_id
             , NULL --nationality_1
             , NULL --nationality_2
             , NULL --ethnicity_self_defined
             , NULL --ethnicity_observed
             , NULL --cro_number
             , NULL --occupation
             , NULL --gender
             , NULL --custody_status
             , NULL --prison_number
             , NULL --interpreter_lang
            */
        )
        RETURNING party_id
        INTO      v_party_id;

        RAISE INFO 'p_create_minor_creditor: Created parties record for the minor_creditor. party_id = %', v_party_id;

        --Insert the CREDITOR_ACCOUNTS record for the minor creditor
        INSERT INTO creditor_accounts (
              creditor_account_id
            , business_unit_id
            , account_number
            , creditor_account_type
            , prosecution_service
            , major_creditor_id
            , minor_creditor_party_id
            , from_suspense
            , hold_payout
            , pay_by_bacs
            , bank_sort_code
            , bank_account_number
            , bank_account_name
            , bank_account_reference
            , bank_account_type
            , last_changed_date
            , version_number    --v1.1
        )
        VALUES (
              NEXTVAL('creditor_account_id_seq')
            , pi_business_unit_id
            , f_get_account_number(pi_business_unit_id, 'creditor_accounts')
            , 'MN'        --creditor_account_type
            , FALSE       --prosecution_service
            , NULL        --major_creditor_id
            , v_party_id  --minor_creditor_party_id
            , FALSE       --from_suspense
            , (pi_minor_creditor_json ->> 'payout_hold')::BOOLEAN
            , v_pay_by_bacs
            , v_bank_sort_code
            , v_bank_account_number
            , v_bank_account_name
            , v_bank_account_ref
            , v_bank_account_type
            , NULL        --last_changed_date
            , 1           --version_number    v1.1
        )
        RETURNING creditor_account_id
        INTO      po_creditor_account_id;

        RAISE INFO 'p_create_minor_creditor: Created creditor_accounts record for the minor_creditor. creditor_account_id = %', po_creditor_account_id;

    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2005' OR SQLSTATE 'P2010' THEN
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_minor_creditor: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_minor_creditor: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_minor_creditor: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_minor_creditor(IN pi_business_unit_id smallint, IN pi_minor_creditor_json json, OUT po_creditor_account_id bigint); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_minor_creditor(IN pi_business_unit_id smallint, IN pi_minor_creditor_json json, OUT po_creditor_account_id bigint) IS 'Procedure to create new CREDITOR_ACCOUNTS and PARTIES records for a minor creditor.';

--
-- Name: p_create_payment_terms(bigint, public.t_da_account_type_enum, numeric, character varying, character varying, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_create_payment_terms(IN pi_defendant_account_id bigint, IN pi_account_type public.t_da_account_type_enum, IN pi_da_account_balance numeric, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_payment_terms_json json)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_create_payment_terms.sql
*
* DESCRIPTION : Insert into the PAYMENT_TERMS table and update JAIL_DAYS on DEFENDANT_ACCOUNTS table.
*
* PARAMETERS  : pi_defendant_account_id - The Opal defendant account id that has been generated and will be returned to the backend
*               pi_account_type         - The account type of the associated defendant_accounts record
*               pi_da_account_balance   - The calculated defendant account balance
*               pi_posted_by            - Identifies the user that is submitting the request to be passed in by the backend. This is the business_unit_user_id
*               pi_posted_by_name       - The user that is submitting the request to be passed in by the backend
*               pi_payment_terms_json   - The payment_terms Json object for the defendant account
*
* VERSION HISTORY:
*
* Date          Author               Version     Nature of Change
* ----------    -----------------    --------    -----------------------------------------------------------------------------------------------------------------------------------------
* 29/07/2025    TMc and A Dennis     1.0         PO-1036 - Insert into the PAYMENT_TERMS table and update JAIL_DAYS on DEFENDANT_ACCOUNTS table
* 02/09/2025    TMc                  2.0         PO-2115 - Removed time component when populating PAYMENT_TERMS.EFFECTIVE_DATE
* 09/03/2026    TMc                  3.0         PO-2910 - Amended insert into PAYMENT_TERMS statement to cast values for INSTALMENT_PERIOD and TERMS_TYPE_CODE to new ENUM data types
*                                                          Amended the data type for variable v_payment_terms_type_code so an exception isn't raised before the validation code.
*                                                          This will still let the validation code to be executed and the custom exception will still be raised for an invalid ENUM value.
*                                                PO-2868 - Update columns on DEFENDANT_ACCOUNTS table to use postgresql enum instead of varchar
*                                                          Amended statement to use ENUM value and remove LOWER function: 'v_is_fixed_penalty := (pi_account_type = 'Fixed Penalty')'
*                                                Added drop statement as signature has changed.
*
**/
DECLARE
	v_pg_exception_detail	TEXT;

    --v_payment_terms_type_code   payment_terms.terms_type_code%TYPE;  --v3.0 Commented out
	v_payment_terms_type_code   VARCHAR;  --v3.0 Added
    v_effective_date            payment_terms.effective_date%TYPE;
    v_instalment_amount         payment_terms.instalment_amount%TYPE;
    v_jail_days                 payment_terms.jail_days%TYPE;
    v_is_fixed_penalty          BOOLEAN;
    v_payment_term_condition    BOOLEAN; --Flag to check if payment terms are valid
BEGIN

    --Insert the PAYMENT_TERMS record, if the Json passed is not NULL
    IF pi_payment_terms_json IS NULL OR JSON_TYPEOF(pi_payment_terms_json) = 'null' THEN
        
        --Raise exception - payment_terms Json is required
        RAISE EXCEPTION 'Missing payment terms' 
            USING DETAIL = 'p_create_payment_terms: Passed payment_terms Json was missing or empty.';

    ELSE

        --Parse payment_terms fields
        v_payment_terms_type_code := pi_payment_terms_json ->> 'payment_terms_type_code';
        v_effective_date          := TO_TIMESTAMP(pi_payment_terms_json ->> 'effective_date', 'YYYY-MM-DD');
        v_instalment_amount       := (pi_payment_terms_json ->> 'instalment_amount')::NUMERIC(18,2);
        v_jail_days               := (pi_payment_terms_json ->> 'default_days_in_jail')::INTEGER;
        v_is_fixed_penalty        := (pi_account_type = 'Fixed Penalty'); 
        v_payment_term_condition  := FALSE; --Flag to check if payment terms are valid 

        IF v_is_fixed_penalty 
        THEN 
            IF (v_payment_terms_type_code = 'B' AND v_effective_date IS NULL)  -- effective date not required for fixed penalty accounts
            THEN
                v_payment_term_condition  := TRUE;
            END IF;
        ELSE
            IF (v_payment_terms_type_code = 'B' AND v_effective_date IS NOT NULL AND NOT v_is_fixed_penalty) OR  -- A non fixed penalty account can have payment type code of B
               (v_payment_terms_type_code = 'I' AND v_effective_date IS NOT NULL AND v_instalment_amount IS NOT NULL) OR
               (v_payment_terms_type_code = 'P') 
            THEN
                v_payment_term_condition := TRUE;
            END IF;
        END IF;

        IF v_payment_term_condition 
        THEN
            
            --Payment terms are valid

            IF v_is_fixed_penalty THEN
                --Add 28 days to the current_date
                --v_effective_date := CURRENT_TIMESTAMP + INTERVAL '28 days';  --v2.0 Commented out
                v_effective_date := (CURRENT_DATE + INTERVAL '28 days')::timestamp;    --v2.0 Added
            END IF;
            
            INSERT INTO payment_terms (
                  payment_terms_id
                , defendant_account_id
                , posted_date
                , posted_by
                , terms_type_code
                , effective_date
                , instalment_period
                , instalment_amount
                , instalment_lump_sum
                , jail_days
                , "extension"
                , account_balance
                , posted_by_name
                , active
            )
            VALUES (
                  NEXTVAL('payment_terms_id_seq')
                , pi_defendant_account_id
                , CLOCK_TIMESTAMP()
                , pi_posted_by
                , v_payment_terms_type_code::t_terms_type_code_enum
                , v_effective_date
                , (pi_payment_terms_json ->> 'instalment_period')::t_instalment_period_enum
                , v_instalment_amount
                , (pi_payment_terms_json ->> 'lump_sum_amount')::NUMERIC(18,2)
                , v_jail_days
                , FALSE --extension
                , pi_da_account_balance
                , pi_posted_by_name
                , TRUE  --active
            );

            RAISE INFO 'p_create_payment_terms: Created payment_terms record for defendant_account_id = %', pi_defendant_account_id;

            --Update DEFENDANT_ACCOUNTS.JAIL_DAYS column
            UPDATE defendant_accounts
               SET jail_days = v_jail_days
             WHERE defendant_account_id = pi_defendant_account_id;

            RAISE INFO 'p_create_payment_terms: defendant_accounts.jail_days [%] has been updated. defendant_account_id = %', v_jail_days, pi_defendant_account_id;

        ELSE
            --Payment terms are invalid. Raise exception
            RAISE EXCEPTION 'Invalid payment terms' 
                USING ERRCODE = 'P2003'
                    , DETAIL = 'p_create_payment_terms: Passed payment_terms Json = ' || pi_payment_terms_json;     

        END IF;
    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2003' THEN 
        --When custom exception just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_create_payment_terms: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_create_payment_terms: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_create_payment_terms: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_create_payment_terms(IN pi_defendant_account_id bigint, IN pi_account_type public.t_da_account_type_enum, IN pi_da_account_balance numeric, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_payment_terms_json json); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_create_payment_terms(IN pi_defendant_account_id bigint, IN pi_account_type public.t_da_account_type_enum, IN pi_da_account_balance numeric, IN pi_posted_by character varying, IN pi_posted_by_name character varying, IN pi_payment_terms_json json) IS 'Procedure to insert into the PAYMENT_TERMS table.';

--
-- Name: p_get_creditor_account(smallint, json); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_get_creditor_account(IN pi_business_unit_id smallint, IN pi_imposition_json json, OUT po_creditor_account_id bigint, OUT po_is_minor_creditor boolean, OUT po_results_mapped_item_number smallint)
    LANGUAGE plpgsql
    AS $$
/**
* CGI OPAL Program
*
* MODULE      : p_get_creditor_account.sql
*
* DESCRIPTION : Retrieves or Creates the CREDITOR_ACCOUNT details, including minor_creditor PARTIES record(s).
*
* PARAMETERS  : pi_business_unit_id           - The business unit id from the DRAFT_ACCOUNTS table to be passed in by the backend
*               pi_imposition_json            - The offence -> impositions -> imposition Json object
*               po_creditor_account_id        - The found or newly created creditor_account_id
*               po_is_minor_creditor          - Whether or not the creditor account is for a minor creditor
*               po_results_mapped_item_number - The resutls.imposition_category mapped to an item_number and returned
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------
* 29/07/2025    TMc         1.0         PO-1043 - Retrieves or Creates the CREDITOR_ACCOUNT details, including minor_creditor PARTIES record(s).
* 27/08/2025    TMc         2.0         PO-2082 - Replaced item_number logic to retrieve it from the new IMPOSITION_CATEGORY_ITEM_NUMBER table.
*
**/
DECLARE
	v_pg_exception_detail	TEXT;
    v_result_id             results.result_id%TYPE;
    v_major_creditor_id     creditor_accounts.major_creditor_id%TYPE;
    v_minor_creditor_json   JSON;

    v_results_imposition_creditor   results.imposition_creditor%TYPE;
BEGIN
    po_is_minor_creditor := FALSE;

    --Extract details from Impositions Json object
    v_result_id           := pi_imposition_json ->> 'result_id';
    v_major_creditor_id   := (pi_imposition_json ->> 'major_creditor_id')::BIGINT;
    v_minor_creditor_json := json_extract_path(pi_imposition_json, 'minor_creditor');

    --Retrieve the RESULTS record and map to an item_number
    BEGIN
        /*      Select replaced in v2.0
        SELECT imposition_creditor
             , CASE imposition_category
                    WHEN 'Crown Prosecution Costs'         THEN 301
                    WHEN 'Court Charge'                    THEN 805
                    WHEN 'Costs'                           THEN 213
                    WHEN 'Fines'                           THEN 208
                    WHEN 'Witness Expenses & Central Fund' THEN 212
                    WHEN 'Victim Surcharge'                THEN 405
                    WHEN 'Compensation'                    THEN 214
                    WHEN 'Legal Aid'                       THEN 209
                    ELSE NULL
               END CASE
        INTO STRICT v_results_imposition_creditor, po_results_mapped_item_number
          FROM results
         WHERE result_id = v_result_id
           AND imposition = TRUE;
        */
        SELECT r.imposition_creditor
             , icin.item_number
        INTO STRICT v_results_imposition_creditor, po_results_mapped_item_number
          FROM results r
          JOIN imposition_category_item_number icin
            ON r.imposition_category = icin.imposition_category
         WHERE result_id = v_result_id
           AND imposition = TRUE;
    EXCEPTION 
        --When NO_DATA_FOUND or TOO_MANY_ROWS raise custom exception otherwise re-raise the original
        WHEN SQLSTATE 'P0002' OR SQLSTATE 'P0003' THEN
            RAISE EXCEPTION 'Result % is not valid', v_result_id
                USING ERRCODE = 'P2004'
                    , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id;
        WHEN OTHERS THEN
            RAISE;
    END;

    --Retrieve the CREDITOR_ACCOUNT record, if possible
    BEGIN
        IF v_results_imposition_creditor = 'CF' THEN
    
            SELECT creditor_account_id
            INTO STRICT po_creditor_account_id
              FROM creditor_accounts  
             WHERE business_unit_id      = pi_business_unit_id 
               AND creditor_account_type = 'CF';
    
        ELSIF v_results_imposition_creditor = 'CPS' THEN
    
            SELECT creditor_account_id
            INTO STRICT po_creditor_account_id
              FROM creditor_accounts  
             WHERE business_unit_id      = pi_business_unit_id 
               AND creditor_account_type = 'MJ'
               AND prosecution_service   = TRUE;
    
        ELSIF v_results_imposition_creditor = '!CPS' THEN
    
            IF v_major_creditor_id IS NULL THEN
                
                --New minor creditor account & parties will be created
                po_creditor_account_id := NULL;

            ELSE
                
                SELECT creditor_account_id
                INTO STRICT po_creditor_account_id
                  FROM creditor_accounts  
                 WHERE business_unit_id      = pi_business_unit_id 
                   AND creditor_account_type = 'MJ'
                   AND prosecution_service   = FALSE
                   AND major_creditor_id     = v_major_creditor_id;

            END IF;
    
        ELSIF v_results_imposition_creditor = 'Any' THEN
    
            IF v_major_creditor_id IS NULL THEN
                
                --New minor creditor account & parties will be created
                po_creditor_account_id := NULL;

            ELSE
                
                SELECT creditor_account_id
                INTO STRICT po_creditor_account_id
                  FROM creditor_accounts  
                 WHERE business_unit_id      = pi_business_unit_id 
                   AND creditor_account_type = 'MJ'
                   AND major_creditor_id     = v_major_creditor_id;

            END IF;
        
        ELSE
            -- Should not happen but raise exception if it does
            RAISE EXCEPTION 'Result % is not valid', v_result_id
                USING ERRCODE = 'P2004'
                    , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id;
        END IF;
    EXCEPTION 
        --When NO_DATA_FOUND or TOO_MANY_ROWS raise custom exception otherwise re-raise the original
        WHEN SQLSTATE 'P0002' OR SQLSTATE 'P0003' THEN
            If v_major_creditor_id IS NULL THEN
                RAISE EXCEPTION 'Creditor not found' 
                    USING ERRCODE = 'P2006'
                        , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id || ', imposition_creditor = ' || v_results_imposition_creditor;
            ELSE 
                RAISE EXCEPTION 'Creditor % not found', v_major_creditor_id
                    USING ERRCODE = 'P2006'
                        , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id || ', imposition_creditor = ' || v_results_imposition_creditor;
            END IF;
        WHEN OTHERS THEN
            RAISE;
    END;

    IF po_creditor_account_id IS NULL THEN

        --Create new minor creditor account & parties, if passed minor_creditor Json is present
        IF v_minor_creditor_json IS NULL OR JSON_TYPEOF(v_minor_creditor_json) = 'null' THEN
            --Raise custom exception
            RAISE EXCEPTION 'Missing creditor' 
                USING ERRCODE = 'P2005'
                    , DETAIL = 'p_get_creditor_account: result_id = ' || v_result_id || ', imposition_creditor = ' || v_results_imposition_creditor;
        ELSE
            po_is_minor_creditor := TRUE;            

            CALL p_create_minor_creditor ( pi_business_unit_id,
                                           v_minor_creditor_json,
                                           po_creditor_account_id
            );

            RAISE INFO 'p_get_creditor_account: Created creditor_account_id = % - result_id = %, imposition_creditor = %', po_creditor_account_id, v_result_id, v_results_imposition_creditor;

        END IF;
    ELSE
        RAISE INFO 'p_get_creditor_account: Found creditor_account_id = % - result_id = %, imposition_creditor = %', po_creditor_account_id, v_result_id, v_results_imposition_creditor;
    END IF;

EXCEPTION 
    WHEN SQLSTATE 'P2004' OR SQLSTATE 'P2005' OR SQLSTATE 'P2006' OR SQLSTATE 'P2010' THEN
        --When custom exceptions just re-raise it so it's not manipulated
        RAISE NOTICE 'Error in p_get_creditor_account: % - %', SQLSTATE, SQLERRM;
        RAISE;
    WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS v_pg_exception_detail = PG_EXCEPTION_DETAIL;
        RAISE NOTICE 'Error in p_get_creditor_account: % - %', SQLSTATE, SQLERRM;
        RAISE NOTICE 'Error details: %', v_pg_exception_detail;
        RAISE EXCEPTION 'Error in p_get_creditor_account: % - %', SQLSTATE, SQLERRM 
            USING DETAIL = v_pg_exception_detail;
END;
$$;

--
-- Name: PROCEDURE p_get_creditor_account(IN pi_business_unit_id smallint, IN pi_imposition_json json, OUT po_creditor_account_id bigint, OUT po_is_minor_creditor boolean, OUT po_results_mapped_item_number smallint); Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON PROCEDURE public.p_get_creditor_account(IN pi_business_unit_id smallint, IN pi_imposition_json json, OUT po_creditor_account_id bigint, OUT po_is_minor_creditor boolean, OUT po_results_mapped_item_number smallint) IS 'Retrieves or Creates the CREDITOR_ACCOUNT details, including minor_creditor PARTIES record(s).';

--
-- Name: p_insert_interface_message(bigint, character varying, character varying, bigint, bigint, text); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_insert_interface_message(IN pi_interface_job_id bigint, IN pi_message_type character varying, IN pi_message_text character varying, IN pi_interface_file_id bigint DEFAULT NULL::bigint, IN pi_record_index bigint DEFAULT NULL::bigint, IN pi_record_detail text DEFAULT NULL::text)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_interface_message.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
BEGIN
    INSERT INTO interface_messages (
        interface_message_id,
        interface_job_id,
        interface_file_id,
        record_index,
        record_detail,
        message_type,
        message_text)
    VALUES (
        nextval('interface_message_id_seq'),
        pi_interface_job_id,
        pi_interface_file_id,
        pi_record_index,
        pi_record_detail,
        pi_message_type,
        LEFT(pi_message_text,500));
END;
$$;

--
-- Name: p_insert_payment_in(bigint, numeric, character varying, character varying, character varying, character varying, character varying, character varying, character varying, boolean, boolean); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_insert_payment_in(IN pi_till_id bigint, IN pi_payment_amount numeric, IN pi_payment_method character varying, IN pi_destination_type character varying, IN pi_allocation_type character varying DEFAULT NULL::character varying, IN pi_associated_record_type character varying DEFAULT NULL::character varying, IN pi_associated_record_id character varying DEFAULT NULL::character varying, IN pi_third_party_payer_name character varying DEFAULT NULL::character varying, IN pi_additional_information character varying DEFAULT NULL::character varying, IN pi_receipt boolean DEFAULT false, IN pi_auto_payment boolean DEFAULT false)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_payment_in.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
BEGIN
    INSERT INTO payments_in (
                payment_in_id,
                till_id,
                payment_amount,
                payment_date,
                payment_method,
                destination_type,
                allocation_type,
                associated_record_type,
                associated_record_id,
                third_party_payer_name,
                additional_information,
                allocated,
                receipt,
                auto_payment)
    VALUES (
                nextval('payment_in_id_seq'),
                pi_till_id,
                pi_payment_amount,
                CURRENT_TIMESTAMP,
                pi_payment_method,
                pi_destination_type,
                pi_allocation_type,
                pi_associated_record_type,
                pi_associated_record_id,
                pi_third_party_payer_name,
                pi_additional_information,
                FALSE,
                pi_receipt,
                pi_auto_payment);
END;
$$;

--
-- Name: p_insert_till(bigint, smallint, smallint); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_insert_till(INOUT pio_till_id bigint, INOUT pio_till_number smallint, IN pi_business_unit_id smallint)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_insert_till.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
BEGIN
    INSERT INTO tills (
                    till_id,
                    business_unit_id,
                    till_number)
    VALUES      (
                    nextval('till_id_seq'),
                    pi_business_unit_id,
                    nextval('till_number_'||pi_business_unit_id::text||'_seq'))
    RETURNING   till_id, till_number
    INTO        pio_till_id, pio_till_number;
END;
$$;

--
-- Name: p_int_payment_card_requests(bigint); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_int_payment_card_requests(IN pi_interface_job_id bigint)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_int_payment_card_requests.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
    k_interface_name text := 'PAYMENT_CARD_REQUESTS';
	k_char_exclusion_range text := '[^\u0020-\u00FF]';
    k_excluded_chars text := '[%()*+<=>?\[\]{}]';
    v_err_context text;
    v_json_data json;
    v_interface_file_id interface_files.interface_file_id%TYPE;
	v_file_name text;
	v_defendant_account_list bigint[] := '{}';
BEGIN
    SELECT  array_to_json(
			    array_agg(
				    json_build_object(
                        'business_unit_id', da.business_unit_id,
                        'account', da.account_number,
                        'debtor_title_and_initials', regexp_replace(regexp_replace(case when p.title is not null then TRIM(p.title || ' ' || p.initials) else null end,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'debtor_name', regexp_replace(regexp_replace(substr(case when p.organisation then p.organisation_name else p.surname end,1,19),k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'name_on_card', regexp_replace(regexp_replace(substr(case when p.title is not null then TRIM(p.title || ' ' || p.initials) else '' end || ' ' || case when p.organisation then p.organisation_name else p.surname end,1,27),k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_1', regexp_replace(regexp_replace(p.address_line_1,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_2', regexp_replace(regexp_replace(p.address_line_2,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_3', regexp_replace(regexp_replace(p.address_line_3,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'address_line_4', regexp_replace(regexp_replace(p.address_line_4,k_char_exclusion_range,'','g'),k_excluded_chars,'','g'),
                        'postcode', p.postcode
                    )
                )
		    ) AS json_array,
	        array_agg(pcr.defendant_account_id)
    INTO    v_json_data, v_defendant_account_list 
    FROM    payment_card_requests pcr
    JOIN    defendant_account_parties dap ON dap.defendant_account_id = pcr.defendant_account_id
    JOIN    parties p ON p.party_id = dap.party_id
	JOIN    defendant_accounts da ON da.defendant_account_id = dap.defendant_account_id
	WHERE   dap.debtor = true;
	IF v_json_data IS NOT NULL THEN
        v_interface_file_id := nextval('interface_file_id_seq');
        SELECT  'PCR_' || TO_CHAR(CURRENT_DATE, 'DDMMYYYY') || '_' || TO_CHAR(COUNT(1), 'FM000')
        INTO    v_file_name
        FROM    interface_files if
        JOIN    interface_jobs ij ON ij.interface_job_id = if.interface_job_id
        WHERE   if.created_datetime::date = CURRENT_DATE
        AND     ij.interface_name = k_interface_name;
        INSERT INTO interface_files (interface_file_id, interface_job_id, file_name, created_datetime, records)
        VALUES      (v_interface_file_id, pi_interface_job_id, v_file_name , CURRENT_TIMESTAMP, v_json_data);
        DELETE FROM payment_card_requests 
        WHERE       defendant_account_id = ANY(v_defendant_account_list);
    END IF;
END;
$$;

--
-- Name: p_int_payments_in(bigint); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_int_payments_in(IN pi_interface_job_id bigint)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_int_payments_in.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
    k_valid_transaction_codes varchar[] := ARRAY['00','68','99'];
    k_status_cs text := 'CS';
    k_status_ta text := 'TA';
    k_status_to text := 'TO';
    k_status_ts text := 'TS';
    k_status_wo text := 'WO';
    k_msg_bank interface_messages.message_text%TYPE := 'Unknown bank details';
    k_msg_tran interface_messages.message_text%TYPE := 'Invalid transaction code';
    k_msg_nf interface_messages.message_text%TYPE := 'Account not found';
    k_msg_cs interface_messages.message_text%TYPE := 'Account consolidated';
    k_msg_ta interface_messages.message_text%TYPE := 'Account transferred out (acknowledged)';
    k_msg_to interface_messages.message_text%TYPE := 'Account transferred out';
    k_msg_ts interface_messages.message_text%TYPE := 'Account transferred out to Scotland/NI';
    k_msg_wo interface_messages.message_text%TYPE := 'Account written off';
    k_msg_bal interface_messages.message_text%TYPE := 'Account has a zero balance';
    k_msg_enf interface_messages.message_text%TYPE := 'Account last enforcement inhibits payments';
    k_msg_type_info interface_messages.message_type%TYPE := 'Info';
    k_msg_type_warn interface_messages.message_type%TYPE := 'Warning';
    k_msg_type_exc interface_messages.message_type%TYPE := 'Exception';
    k_tbl_defendant_accounts text := 'defendant_accounts';
    k_dest_fines text := 'F';
    k_dest_suspense text := 'S';
    k_alloc_unidentified text := 'UN';
    k_unidentified_ref_label text := ' - Auto Cash Input';
    k_summary_till text :=  'Till allocated: ';
    k_summary_processed text := 'Payment records processed: ';
    k_summary_accepted text := 'Payment records accepted: ';
    k_summary_fines text := 'Payment records (fines): ';
    k_summary_suspense text := 'Payment records (suspense): ';
    k_summary_rejected text := 'Payment records rejected: ';
    k_summary_ignored text := 'Payment records ignored: ';
    k_summary_value text := ', value: '||chr(163);
    r_master_account record;
    r_payment record;
    v_msg_text interface_messages.message_text%TYPE;
    v_msg_type interface_messages.message_type%TYPE;
    v_record_detail interface_messages.record_detail%TYPE;
    v_till_id tills.till_id%TYPE;
    v_till_number tills.till_number%TYPE;
    v_count_ignored bigint := 0;
    v_count_processed bigint := 0;
    v_count_rejected bigint := 0;
    v_count_accepted bigint := 0;
    v_count_fines bigint := 0;
    v_count_suspense bigint := 0;
    v_total_processed bigint := 0;
    v_total_rejected bigint := 0;
    v_total_accepted bigint := 0;
    v_total_fines bigint := 0;
    v_total_suspense bigint := 0;
BEGIN
    FOR r_payment IN (
         SELECT             rec.rec_index,
                            j.business_unit_id,
                            f.interface_file_id,
                            CASE WHEN ba->>'account_number' IS NOT NULL THEN true ELSE false END AS is_bu_bank,
                            rec.obj->>'receiving_sort_code' AS receiving_sort_code,
                            rec.obj->>'receiving_bank_account_number' AS receiving_bank_account_number,
                            rec.obj->>'receiving_account_type' AS receiving_account_type,
                            rec.obj->>'transaction_code' AS transaction_code,
                            rec.obj->>'originator_sort_code' AS originator_sort_code,
                            rec.obj->>'originator_bank_account_number' AS originator_bank_account_number,
                            (rec.obj->>'amount_pence')::bigint AS amount_pence,
                            rec.obj->>'originator_name' AS originator_name,
                            rec.obj->>'originator_reference' AS originator_reference,
                            rec.obj->>'originator_beneficiary_name' AS originator_beneficiary_name,
                            da.defendant_account_id,
                            da.account_status
        FROM                interface_jobs j
        INNER JOIN          interface_files f ON f.interface_job_id = j.interface_job_id
        INNER JOIN          configuration_items ci ON ci.business_unit_id = j.business_unit_id AND ci.item_name = 'BANKLIST'
        CROSS JOIN LATERAL  json_array_elements(f.records) WITH ORDINALITY AS rec(obj, rec_index)
        LEFT JOIN LATERAL   json_array_elements(ci.item_values) AS ba
                                ON ba->>'account_number' = rec.obj->>'receiving_bank_account_number'
                                AND ba->>'sort_code' = rec.obj->>'receiving_sort_code'
        LEFT JOIN           defendant_accounts da ON da.business_unit_id = j.business_unit_id AND da.account_number = rec.obj->>'originator_reference'
        WHERE               j.interface_job_id = pi_interface_job_id
        ORDER BY            rec.rec_index)
    LOOP
        v_count_processed := v_count_processed + 1;
        v_total_processed := v_total_processed + r_payment.amount_pence;
        IF r_payment.amount_pence > 0 THEN
            v_record_detail := r_payment.receiving_sort_code
                ||','||r_payment.receiving_bank_account_number
                ||','||r_payment.receiving_account_type
                ||','||r_payment.transaction_code
                ||','||r_payment.originator_sort_code
                ||','||r_payment.originator_bank_account_number
                ||','||round((r_payment.amount_pence/100.00),2)::text
                ||','||r_payment.originator_name
                ||','||r_payment.originator_reference
                ||','||r_payment.originator_beneficiary_name;
            IF NOT r_payment.is_bu_bank THEN
                -- rejected records
                v_msg_text := k_msg_bank;
                v_msg_type := k_msg_type_exc;
                v_count_rejected := v_count_rejected + 1;
                v_total_rejected := v_total_rejected + r_payment.amount_pence;
            ELSE
                -- accepted records (might still have warnings)
                SELECT              da.defendant_account_id,
                                    da.account_number,
                                    da.last_enforcement,
                                    da.account_status,
                                    da.account_balance,
                                    (res IS NOT NULL) AS enf
                INTO                r_master_account
                FROM                defendant_accounts da
                LEFT JOIN           configuration_items ci ON ci.business_unit_id = da.business_unit_id AND ci.item_name = 'INHIBIT_PAYMENT_RESULTS'
                LEFT JOIN LATERAL   json_array_elements_text(ci.item_values) AS res ON res = da.last_enforcement
                WHERE               da.defendant_account_id = f_get_master_account_id(r_payment.defendant_account_id);
                v_msg_text := CASE
                    WHEN r_payment.transaction_code != ALL(k_valid_transaction_codes) THEN k_msg_tran
                    WHEN r_master_account.defendant_account_id IS NULL THEN k_msg_nf
                    WHEN r_master_account.account_status = k_status_cs THEN k_msg_cs
                    WHEN r_master_account.account_status = k_status_ta THEN k_msg_ta
                    WHEN r_master_account.account_status = k_status_to THEN k_msg_to
                    WHEN r_master_account.account_status = k_status_ts THEN k_msg_ts
                    WHEN r_master_account.account_status = k_status_wo THEN k_msg_wo
                    WHEN r_master_account.enf THEN k_msg_enf
                    WHEN r_master_account.account_balance = 0 THEN k_msg_bal
                    ELSE NULL END;
                IF v_msg_text IS NOT NULL THEN
                    v_msg_type = k_msg_type_warn;
                END IF;
                -- create payment in (first payment requires till to be created)
                IF v_till_id IS NULL THEN
                    CALL p_insert_till(v_till_id, v_till_number, r_payment.business_unit_id);
                END IF;
                CALL p_insert_payment_in(
                    pi_till_id := v_till_id,
                    pi_payment_amount := round((r_payment.amount_pence/100.00),2),
                    pi_payment_method := 'CT'::text,
                    pi_destination_type := CASE WHEN v_msg_text IS NULL THEN k_dest_fines ELSE k_dest_suspense END,
                    pi_allocation_type := CASE WHEN v_msg_text IS NULL THEN NULL ELSE k_alloc_unidentified END,
                    pi_associated_record_type := CASE WHEN v_msg_text IS NULL THEN k_tbl_defendant_accounts ELSE NULL::text END,
                    pi_associated_record_id := CASE WHEN v_msg_text IS NULL THEN r_master_account.defendant_account_id::text ELSE NULL::text END,
                    pi_additional_information := r_payment.originator_reference||k_unidentified_ref_label,
                    pi_auto_payment := true);
                v_count_accepted := v_count_accepted + 1;
                v_total_accepted := v_total_accepted + r_payment.amount_pence;
                IF v_msg_text IS NULL THEN
                    v_count_fines := v_count_fines + 1;
                    v_total_fines := v_total_fines + r_payment.amount_pence;
                ELSE
                    v_count_suspense := v_count_suspense + 1;
                    v_total_suspense := v_total_suspense + r_payment.amount_pence;
                END IF;
            END IF;
            IF v_msg_text IS NOT NULL THEN
                CALL p_insert_interface_message(pi_interface_job_id, v_msg_type, v_msg_text, r_payment.interface_file_id, r_payment.rec_index, v_record_detail);
            END IF;
        ELSE
            v_count_ignored := v_count_ignored + 1;
        END IF;
    END LOOP;
    -- job summary messages
    IF v_till_number IS NOT NULL THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_till||v_till_number::text);
    END IF;
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_processed||v_count_processed::text||k_summary_value||round(v_total_processed/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_accepted||v_count_accepted::text||k_summary_value||round(v_total_accepted/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_fines||v_count_fines::text||k_summary_value||round(v_total_fines/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_suspense||v_count_suspense::text||k_summary_value||round(v_total_suspense/100.00,2)::text);
    IF v_count_rejected > 0 THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_rejected||v_count_rejected::text||k_summary_value||round(v_total_rejected/100.00,2)::text);
    END IF;
    IF v_count_ignored > 0 THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_ignored||v_count_ignored::text);
    END IF;
END;
$$;

--
-- Name: p_int_presented_cheques(bigint); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_int_presented_cheques(IN pi_interface_job_id bigint)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_int_presented_cheques.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
    k_valid_transaction_code varchar := '11';
    k_msg_type_info interface_messages.message_type%TYPE := 'Info';
    k_msg_type_warn interface_messages.message_type%TYPE := 'Warning';
    k_msg_type_exc interface_messages.message_type%TYPE := 'Exception';
    k_summary_presented text := 'Cheques presented: ';
    k_summary_not_presented text := 'Cheques not presented: ';
    k_summary_ignored text := 'Cheque records ignored: ';
    k_summary_value text := ', value: '||chr(163);
	k_status_cheque_awaiting_deletion text := 'X';
	k_status_cheque_destroyed text := 'D';
	k_status_cheque_withdrawn text := 'W';
	k_status_cheque_presented text := 'P';
	k_status_cheque_query text := 'Q';
	k_status_cheque_cleared text := 'C';
	k_updated_cheque_status text := null;
	k_msg_bank interface_messages.message_text%TYPE := 'Unknown bank details';
	k_msg_cheque_not_found interface_messages.message_text%TYPE := 'Cheque not found';
	k_msg_cheque_destroyed interface_messages.message_text%TYPE := 'Cheque destroyed';
	k_msg_cheque_withdrawn interface_messages.message_text%TYPE := 'Cheque withdrawn';
	k_msg_cheque_presented interface_messages.message_text%TYPE := 'Cheque already presented';
	k_msg_cheque_amount_mismatch interface_messages.message_text%TYPE := 'Cheque amount mismatch'; 
    r_file_record record;
	r_cheque record;
    v_msg_text interface_messages.message_text%TYPE;
    v_msg_type interface_messages.message_type%TYPE;
    v_record_detail interface_messages.record_detail%TYPE;
    v_count_presented integer := 0;
    v_count_not_presented integer := 0;
	v_count_ignored integer := 0;
    v_total_presented integer := 0;
    v_total_not_presented integer := 0;
BEGIN
    FOR r_file_record IN (
         SELECT             rec.rec_index,
                            j.business_unit_id,
                            f.interface_file_id,
                            CASE WHEN ba->>'account_number' IS NOT NULL THEN true ELSE false END AS is_bu_bank,
                            rec.obj->>'receiving_sort_code' AS receiving_sort_code,
                            rec.obj->>'receiving_bank_account_number' AS receiving_bank_account_number,
                            rec.obj->>'transaction_code' AS transaction_code,
                            (rec.obj->>'amount_pence')::bigint AS amount_pence,
							(rec.obj->>'cheque_number')::bigint AS cheque_number,
                            rec.obj->>'entry_date' AS entry_date
        FROM                interface_jobs j
        INNER JOIN          interface_files f ON f.interface_job_id = j.interface_job_id
        INNER JOIN          configuration_items ci ON ci.business_unit_id = j.business_unit_id AND ci.item_name = 'BANKLIST'
        CROSS JOIN LATERAL  json_array_elements(f.records) WITH ORDINALITY AS rec(obj, rec_index)
        LEFT JOIN LATERAL   json_array_elements(ci.item_values) AS ba
                                ON ba->>'account_number' = rec.obj->>'receiving_bank_account_number'
                                AND ba->>'sort_code' = rec.obj->>'receiving_sort_code'
        WHERE               j.interface_job_id = pi_interface_job_id
        ORDER BY            rec.rec_index)
    LOOP
	    v_msg_text := NULL;
	    v_msg_type := NULL;
        IF r_file_record.transaction_code = k_valid_transaction_code AND r_file_record.amount_pence > 0 THEN
            v_record_detail := 'Cheque number: ' || r_file_record.cheque_number::text
				|| ', Value: ' ||chr(163) || round((r_file_record.amount_pence/100.00),2)::text
				|| ', Date: ' || r_file_record.entry_date;
            IF NOT r_file_record.is_bu_bank THEN
                -- rejected records
				v_msg_text := k_msg_bank;
				v_msg_type := k_msg_type_exc;
				v_count_not_presented := v_count_not_presented +1;
				v_total_not_presented := v_total_not_presented + r_file_record.amount_pence;
            ELSE				
                -- Read Cheque and check status
				SELECT  cheque_id, creditor_transaction_id, amount, status
				INTO    r_cheque
				FROM    cheques 
				WHERE   cheque_number = r_file_record.cheque_number AND
					    business_unit_id = r_file_record.business_unit_id;
				-- check the Status of the cheque
				IF r_cheque.status IS NULL OR r_cheque.status = k_status_cheque_awaiting_deletion THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_not_found;
				ELSIF r_cheque.status = k_status_cheque_destroyed THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_destroyed;
				ELSIF r_cheque.status = k_status_cheque_withdrawn THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_withdrawn;
				ELSIF r_cheque.status = k_status_cheque_presented THEN
					v_msg_type := k_msg_type_exc;
					v_msg_text := k_msg_cheque_presented;
				ELSIF round((r_file_record.amount_pence/100.00),2) != round((r_cheque.amount/100.00),2) THEN
					v_msg_type := k_msg_type_warn;
					v_msg_text := k_msg_cheque_amount_mismatch;
				END IF;
				-- update cheque/creditor_transaction (if no exception)
				IF v_msg_type IS NULL OR v_msg_type != k_msg_type_exc THEN
					-- update cheque status
					r_cheque.status = CASE WHEN v_msg_text IS NULL THEN 
							k_status_cheque_presented ELSE k_status_cheque_query END;
					CALL p_update_cheque_status(r_cheque.cheque_id, r_cheque.status);
					-- Only when cheque is cleared (status c) update the creditor transaction
					IF r_cheque.status = k_status_cheque_presented THEN
						CALL p_update_creditor_transaction_status (r_cheque.creditor_transaction_id, k_status_cheque_cleared);
					END IF;
				END IF;
				--Update counts/totals
				IF v_msg_text IS NULL THEN
					v_count_presented := v_count_presented +1;
					v_total_presented := v_total_presented + r_file_record.amount_pence;
				ELSE
					v_count_not_presented := v_count_not_presented +1;
					v_total_not_presented := v_total_not_presented + r_file_record.amount_pence;
				END IF;
            END IF;
            IF v_msg_text IS NOT NULL THEN
                CALL p_insert_interface_message(pi_interface_job_id, v_msg_type, v_msg_text, r_file_record.interface_file_id, r_file_record.rec_index, v_record_detail);
            END IF;
        ELSE
            v_count_ignored := v_count_ignored +1;
        END IF;
    END LOOP;
    -- job summary messages
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_presented||v_count_presented::text||k_summary_value||round(v_total_presented/100.00,2)::text);
    CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_not_presented||v_count_not_presented::text||k_summary_value||round(v_total_not_presented/100.00,2)::text);
    IF v_count_ignored > 0 THEN
        CALL p_insert_interface_message(pi_interface_job_id, k_msg_type_info, k_summary_ignored||v_count_ignored::text);
    END IF;
END;
$$;

--
-- Name: p_run_interface_job(character varying, smallint); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_run_interface_job(IN pi_interface_name character varying, IN pi_business_unit_id smallint DEFAULT NULL::smallint)
    LANGUAGE plpgsql
    AS $_$
/**
* OPAL Program
*
* MODULE      : p_run_interface_job.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
    k_msg_type_error text := 'Error';
    k_status_created text := 'Created';
    k_status_written text := 'Written';
    k_status_nodata text := 'No data';
    k_status_completed text := 'Completed';
    k_status_failed text := 'Failed';
    v_err_context text;
    v_files bigint := 0;
    v_days integer;
    v_outbound boolean;
    v_stored_procedure text;
    v_ij_id interface_jobs.interface_job_id%TYPE;
BEGIN
    -- get interface configuration
    SELECT      (item_values->>'direction' = 'outbound'),
                item_values->>'days_before_deletion',
                item_values->>'stored_procedure'
    INTO        v_outbound, v_days, v_stored_procedure
    FROM        configuration_items
    WHERE       item_name = 'INTERFACE_'||pi_interface_name;
    IF v_stored_procedure IS NULL THEN
        RAISE EXCEPTION 'Interface % not implemented', pi_interface_name;
    END IF;
    IF v_outbound THEN
        -- for outbound jobs, database creates the job and the file
        INSERT INTO interface_jobs (interface_job_id,business_unit_id, interface_name)
        VALUES      (nextval('interface_job_id_seq'),pi_business_unit_id, pi_interface_name)
        RETURNING   interface_job_id
        INTO        v_ij_id;
    ELSE
        -- for inbound, jobs and files are created by the fines service - find first available job to run
        SELECT      interface_job_id
        INTO        v_ij_id
        FROM        interface_jobs
        WHERE       interface_name = pi_interface_name AND
                    status = k_status_created AND
                    business_unit_id = COALESCE(pi_business_unit_id,business_unit_id)
        ORDER BY    created_datetime
        LIMIT 1
        FOR UPDATE SKIP LOCKED;
    END IF;
    IF v_ij_id IS NOT NULL THEN
        UPDATE  interface_jobs
        SET     started_datetime = CURRENT_TIMESTAMP
        WHERE   interface_job_id = v_ij_id;
        BEGIN
            -- call interface specific procedure - any exception in this block rollback and fall into the exception handler
            EXECUTE format('CALL %I($1)',v_stored_procedure)
            USING   v_ij_id;
            -- verify file created for an outbound interface
            IF v_outbound THEN
                SELECT  COUNT(*)
                INTO    v_files
                FROM    interface_files
                WHERE   interface_job_id = v_ij_id;
            END IF;
            -- set new job status
            UPDATE  interface_jobs
            SET     status = 
                        CASE
                            WHEN v_outbound AND v_files > 0 THEN k_status_written
                            WHEN v_outbound THEN k_status_nodata
                            ELSE k_status_completed
                        END,
                    completed_datetime = CURRENT_TIMESTAMP
            WHERE   interface_job_id = v_ij_id;
        EXCEPTION
            WHEN OTHERS THEN
                -- a failure here rolls back this block
                GET STACKED DIAGNOSTICS
                    v_err_context = PG_EXCEPTION_CONTEXT;
                UPDATE  interface_jobs
                SET     status = k_status_failed,
                        completed_datetime = CURRENT_TIMESTAMP
                WHERE   interface_job_id = v_ij_id;
                CALL p_insert_interface_message(v_ij_id,k_msg_type_error,REPLACE(sqlerrm||' - '||v_err_context,CHR(10),''));
        END;
    END IF;
    -- purge jobs here
    DELETE FROM interface_messages 
    WHERE       interface_job_id IN (
                    SELECT  interface_job_id
                    FROM    interface_jobs
                    WHERE   interface_name = pi_interface_name AND
                            status IN (k_status_completed,k_status_failed) AND
                            completed_datetime < CURRENT_DATE - v_days);
    DELETE FROM interface_files
    WHERE       interface_job_id IN (
                    SELECT  interface_job_id
                    FROM    interface_jobs
                    WHERE   interface_name = pi_interface_name AND
                            status IN (k_status_completed,k_status_failed) AND
                            completed_datetime < CURRENT_DATE - v_days);
    DELETE FROM interface_jobs
    WHERE       interface_name = pi_interface_name AND
                status IN (k_status_completed,k_status_failed) AND
                completed_datetime < CURRENT_DATE - v_days;
    COMMIT;
END;
$_$;

--
-- Name: p_update_cheque_status(bigint, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_update_cheque_status(IN pi_cheque_id bigint, IN pi_status character varying)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_update_cheque_status.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
BEGIN
    UPDATE  cheques
    SET     status = pi_status
    WHERE   cheque_id = pi_cheque_id;
END;
$$;

--
-- Name: p_update_creditor_transaction_status(bigint, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.p_update_creditor_transaction_status(IN pi_creditor_transaction_id bigint, IN pi_status character varying)
    LANGUAGE plpgsql
    AS $$
/**
* OPAL Program
*
* MODULE      : p_update_creditor_transaction_status.sql
*
* DESCRIPTION : This procedure was written by Capita required for interface files
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------
* 25/11/2024    Capita      1.0         PO-1010 This procedure was written by Capita required for interface files
*
**/
DECLARE
BEGIN
    UPDATE  creditor_transactions
    SET     status = pi_status,
            status_date = CURRENT_TIMESTAMP
    WHERE   creditor_transaction_id = pi_creditor_transaction_id;
END;
$$;

--
-- Name: v_audit_creditor_accounts; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_audit_creditor_accounts AS
 SELECT DISTINCT TRIM(BOTH FROM (((((COALESCE(p.title, ''::character varying))::text || ' '::text) || (COALESCE(p.forenames, ''::character varying))::text) || ' '::text) || (COALESCE(p.surname, ''::character varying))::text)) AS name,
    p.address_line_1,
    p.address_line_2,
    p.address_line_3,
    p.postcode,
    ca.creditor_account_id,
    ca.hold_payout,
    ca.pay_by_bacs,
    ca.bank_sort_code,
    ca.bank_account_type,
    ca.bank_account_number,
    ca.bank_account_name,
    ca.bank_account_reference
   FROM (public.creditor_accounts ca
     LEFT JOIN public.parties p ON (((ca.minor_creditor_party_id = p.party_id) AND (ca.creditor_account_type = 'MN'::public.t_creditor_account_type_enum))));

--
-- Name: VIEW v_audit_creditor_accounts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_audit_creditor_accounts IS 'Retrieves audit creditor account information with related party details';

--
-- Name: v_audit_defendant_accounts; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_audit_defendant_accounts AS
 SELECT DISTINCT da.defendant_account_id,
    da.cheque_clearance_period,
    da.allow_cheques,
    da.credit_trans_clearance_period,
    da.allow_writeoffs,
    da.enf_override_enforcer_id,
    da.enf_override_result_id,
    da.enf_override_tfo_lja_id,
    da.enforcing_court_id,
    da.collection_order,
    da.suspended_committal_date,
    da.account_comments,
    da.account_note_1,
    da.account_note_2,
    da.account_note_3,
        CASE
            WHEN (p_def.organisation IS TRUE) THEN (p_def.organisation_name)::text
            ELSE TRIM(BOTH FROM (((((COALESCE(p_def.title, ''::character varying))::text || ' '::text) || (COALESCE(p_def.forenames, ''::character varying))::text) || ' '::text) || (COALESCE(p_def.surname, ''::character varying))::text))
        END AS name,
    p_def.birth_date,
    p_def.age,
    p_def.address_line_1,
    p_def.address_line_2,
    p_def.address_line_3,
    p_def.postcode,
    p_def.national_insurance_number,
    p_def.telephone_home,
    p_def.telephone_business,
    p_def.telephone_mobile,
    p_def.email_1,
    p_def.email_2,
        CASE
            WHEN (p_pg.organisation IS TRUE) THEN (p_pg.organisation_name)::text
            ELSE TRIM(BOTH FROM (((((COALESCE(p_pg.title, ''::character varying))::text || ' '::text) || (COALESCE(p_pg.forenames, ''::character varying))::text) || ' '::text) || (COALESCE(p_pg.surname, ''::character varying))::text))
        END AS pname,
    p_pg.address_line_1 AS paddr1,
    p_pg.address_line_2 AS paddr2,
    p_pg.address_line_3 AS paddr3,
    p_pg.birth_date AS pbdate,
    p_pg.national_insurance_number AS pninumber,
    a1.alias_name AS alias1,
    a2.alias_name AS alias2,
    a3.alias_name AS alias3,
    a4.alias_name AS alias4,
    a5.alias_name AS alias5,
    dd.document_language,
    dd.hearing_language,
    dd.vehicle_make,
    dd.vehicle_registration,
    dd.employee_reference,
    dd.employer_name,
    dd.employer_address_line_1,
    dd.employer_address_line_2,
    dd.employer_address_line_3,
    dd.employer_address_line_4,
    dd.employer_address_line_5,
    dd.employer_postcode,
    dd.employer_telephone,
    dd.employer_email
   FROM ((((((((((public.defendant_accounts da
     JOIN public.defendant_account_parties dap_def ON (((da.defendant_account_id = dap_def.defendant_account_id) AND (dap_def.association_type = 'Defendant'::public.t_association_type_enum))))
     JOIN public.parties p_def ON ((dap_def.party_id = p_def.party_id)))
     LEFT JOIN public.defendant_account_parties dap_pg ON (((da.defendant_account_id = dap_pg.defendant_account_id) AND (dap_pg.association_type = 'Parent/Guardian'::public.t_association_type_enum))))
     LEFT JOIN public.parties p_pg ON ((dap_pg.party_id = p_pg.party_id)))
     LEFT JOIN public.debtor_detail dd ON ((p_def.party_id = dd.party_id)))
     LEFT JOIN ( SELECT a.party_id,
                CASE
                    WHEN (p.organisation IS TRUE) THEN (a.organisation_name)::text
                    ELSE TRIM(BOTH FROM (((COALESCE(a.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a.surname, ''::character varying))::text))
                END AS alias_name
           FROM (public.aliases a
             JOIN public.parties p ON (((a.party_id = p.party_id) AND (a.sequence_number = 1))))) a1 ON ((p_def.party_id = a1.party_id)))
     LEFT JOIN ( SELECT a.party_id,
                CASE
                    WHEN (p.organisation IS TRUE) THEN (a.organisation_name)::text
                    ELSE TRIM(BOTH FROM (((COALESCE(a.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a.surname, ''::character varying))::text))
                END AS alias_name
           FROM (public.aliases a
             JOIN public.parties p ON (((a.party_id = p.party_id) AND (a.sequence_number = 2))))) a2 ON ((p_def.party_id = a2.party_id)))
     LEFT JOIN ( SELECT a.party_id,
                CASE
                    WHEN (p.organisation IS TRUE) THEN (a.organisation_name)::text
                    ELSE TRIM(BOTH FROM (((COALESCE(a.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a.surname, ''::character varying))::text))
                END AS alias_name
           FROM (public.aliases a
             JOIN public.parties p ON (((a.party_id = p.party_id) AND (a.sequence_number = 3))))) a3 ON ((p_def.party_id = a3.party_id)))
     LEFT JOIN ( SELECT a.party_id,
                CASE
                    WHEN (p.organisation IS TRUE) THEN (a.organisation_name)::text
                    ELSE TRIM(BOTH FROM (((COALESCE(a.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a.surname, ''::character varying))::text))
                END AS alias_name
           FROM (public.aliases a
             JOIN public.parties p ON (((a.party_id = p.party_id) AND (a.sequence_number = 4))))) a4 ON ((p_def.party_id = a4.party_id)))
     LEFT JOIN ( SELECT a.party_id,
                CASE
                    WHEN (p.organisation IS TRUE) THEN (a.organisation_name)::text
                    ELSE TRIM(BOTH FROM (((COALESCE(a.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a.surname, ''::character varying))::text))
                END AS alias_name
           FROM (public.aliases a
             JOIN public.parties p ON (((a.party_id = p.party_id) AND (a.sequence_number = 5))))) a5 ON ((p_def.party_id = a5.party_id)));

--
-- Name: VIEW v_audit_defendant_accounts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_audit_defendant_accounts IS 'Retrieves audit defendant account information with related party, alias, and debtor details';

--
-- Name: v_consolidated_accounts; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_consolidated_accounts AS
 SELECT ca.master_account_id,
    ca.child_account_id,
    da.account_number AS child_account_number,
    da.prosecutor_case_reference AS child_reference,
    da.imposed_hearing_date AS child_date_imposed,
    da.imposed_by_name AS child_imposed_by,
    pa.forenames AS child_first_name,
    pa.surname AS child_last_name
   FROM (((( SELECT dt.defendant_account_id AS master_account_id,
            dt.associated_record_id AS child_account_id
           FROM public.defendant_transactions dt
          WHERE ((dt.transaction_type = 'CONSOL'::public.t_defendant_transaction_type_enum) AND (dt.associated_record_type = 'defendant_accounts'::public.t_associated_record_type_enum))) ca
     JOIN public.defendant_accounts da ON (((ca.child_account_id)::bigint = da.defendant_account_id)))
     LEFT JOIN public.defendant_account_parties dap ON (((da.defendant_account_id = dap.defendant_account_id) AND (dap.association_type = 'Defendant'::public.t_association_type_enum))))
     LEFT JOIN public.parties pa ON ((dap.party_id = pa.party_id)));

--
-- Name: VIEW v_consolidated_accounts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_consolidated_accounts IS 'Retrieves consolidated account information with related party details';

--
-- Name: v_defendant_accounts_header; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_defendant_accounts_header AS
 SELECT DISTINCT da.defendant_account_id,
    da.version_number,
    da.account_number,
    da.prosecutor_case_reference,
    da.account_status,
    da.account_type,
    da.amount_paid AS paid_written_off,
    da.account_balance,
    da.amount_imposed,
    public.f_arrears(da.defendant_account_id) AS arrears,
    dap_def.defendant_account_party_id,
        CASE
            WHEN (dap_def.debtor IS TRUE) THEN dap_def.association_type
            ELSE NULL::public.t_association_type_enum
        END AS debtor_type,
    p_def.party_id,
    p_def.title,
    p_def.forenames,
    p_def.surname,
    p_def.birth_date,
    p_def.organisation,
    p_def.organisation_name,
    bu.business_unit_id,
    bu.business_unit_name,
    bu.business_unit_code,
    fpo.ticket_number,
    dap_pg.defendant_account_party_id AS parent_guardian_account_party_id,
        CASE
            WHEN (dap_pg.defendant_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_parent_guardian,
        CASE
            WHEN (dap_pg.debtor IS TRUE) THEN dap_pg.association_type
            ELSE NULL::public.t_association_type_enum
        END AS parent_guardian_debtor_type,
        CASE
            WHEN (dt.defendant_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_consolidated_accounts
   FROM ((((((public.defendant_accounts da
     JOIN public.business_units bu ON ((da.business_unit_id = bu.business_unit_id)))
     JOIN public.defendant_account_parties dap_def ON (((da.defendant_account_id = dap_def.defendant_account_id) AND (dap_def.association_type = 'Defendant'::public.t_association_type_enum))))
     JOIN public.parties p_def ON ((dap_def.party_id = p_def.party_id)))
     LEFT JOIN public.fixed_penalty_offences fpo ON ((da.defendant_account_id = fpo.defendant_account_id)))
     LEFT JOIN public.defendant_account_parties dap_pg ON (((da.defendant_account_id = dap_pg.defendant_account_id) AND (dap_pg.association_type = 'Parent/Guardian'::public.t_association_type_enum))))
     LEFT JOIN ( SELECT DISTINCT defendant_transactions.defendant_account_id
           FROM public.defendant_transactions
          WHERE ((defendant_transactions.transaction_type = 'CONSOL'::public.t_defendant_transaction_type_enum) AND (defendant_transactions.associated_record_type = 'defendant_accounts'::public.t_associated_record_type_enum))) dt ON ((da.defendant_account_id = dt.defendant_account_id)));

--
-- Name: VIEW v_defendant_accounts_header; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_defendant_accounts_header IS 'Retrieves defendant account header information for the Defendant Header Summary';

--
-- Name: v_defendant_accounts_summary; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_defendant_accounts_summary AS
 SELECT DISTINCT da.defendant_account_id,
    da.version_number,
    da.account_number,
    da.last_enforcement,
    r_le.result_title AS last_enf_title,
    da.collection_order,
    da.account_comments,
    da.account_note_1,
    da.account_note_2,
    da.account_note_3,
    da.jail_days,
    da.enf_override_result_id,
    da.enf_override_enforcer_id AS enforcer_id,
    e.name AS enforcer_name,
    da.enf_override_tfo_lja_id AS lja_id,
    lja.name AS lja_name,
    r_eo.result_title AS enf_override_title,
    da.last_movement_date,
    dap.association_type AS debtor_type,
    dd.document_language,
    dd.hearing_language,
    p.party_id,
    p.title,
    p.forenames,
    p.surname,
    p.birth_date,
    p.age,
    p.organisation,
    p.organisation_name,
    p.address_line_1,
    p.address_line_2,
    p.address_line_3,
    p.address_line_4,
    p.address_line_5,
    p.postcode,
    p.national_insurance_number,
        CASE
            WHEN p.organisation THEN ((((a1.alias_id || '|'::text) || a1.sequence_number) || '|'::text) || (a1.organisation_name)::text)
            ELSE NULLIF(((((a1.alias_id || '|'::text) || a1.sequence_number) || '|'::text) || TRIM(BOTH FROM (((COALESCE(a1.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a1.surname, ''::character varying))::text))), ''::text)
        END AS alias_1,
        CASE
            WHEN p.organisation THEN ((((a2.alias_id || '|'::text) || a2.sequence_number) || '|'::text) || (a2.organisation_name)::text)
            ELSE NULLIF(((((a2.alias_id || '|'::text) || a2.sequence_number) || '|'::text) || TRIM(BOTH FROM (((COALESCE(a2.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a2.surname, ''::character varying))::text))), ''::text)
        END AS alias_2,
        CASE
            WHEN p.organisation THEN ((((a3.alias_id || '|'::text) || a3.sequence_number) || '|'::text) || (a3.organisation_name)::text)
            ELSE NULLIF(((((a3.alias_id || '|'::text) || a3.sequence_number) || '|'::text) || TRIM(BOTH FROM (((COALESCE(a3.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a3.surname, ''::character varying))::text))), ''::text)
        END AS alias_3,
        CASE
            WHEN p.organisation THEN ((((a4.alias_id || '|'::text) || a4.sequence_number) || '|'::text) || (a4.organisation_name)::text)
            ELSE NULLIF(((((a4.alias_id || '|'::text) || a4.sequence_number) || '|'::text) || TRIM(BOTH FROM (((COALESCE(a4.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a4.surname, ''::character varying))::text))), ''::text)
        END AS alias_4,
        CASE
            WHEN p.organisation THEN ((((a5.alias_id || '|'::text) || a5.sequence_number) || '|'::text) || (a5.organisation_name)::text)
            ELSE NULLIF(((((a5.alias_id || '|'::text) || a5.sequence_number) || '|'::text) || TRIM(BOTH FROM (((COALESCE(a5.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a5.surname, ''::character varying))::text))), ''::text)
        END AS alias_5,
    pt.terms_type_code,
    pt.instalment_period,
    pt.instalment_amount,
    pt.instalment_lump_sum,
    pt.effective_date
   FROM (((((((((((((public.defendant_accounts da
     JOIN public.defendant_account_parties dap ON (((da.defendant_account_id = dap.defendant_account_id) AND (dap.debtor IS TRUE))))
     LEFT JOIN public.results r_le ON (((da.last_enforcement)::text = (r_le.result_id)::text)))
     LEFT JOIN public.results r_eo ON (((da.enf_override_result_id)::text = (r_eo.result_id)::text)))
     LEFT JOIN public.debtor_detail dd ON ((dap.party_id = dd.party_id)))
     LEFT JOIN public.payment_terms pt ON ((da.defendant_account_id = pt.defendant_account_id)))
     LEFT JOIN public.enforcers e ON ((da.enf_override_enforcer_id = e.enforcer_id)))
     LEFT JOIN public.local_justice_areas lja ON ((da.enf_override_tfo_lja_id = lja.local_justice_area_id)))
     LEFT JOIN public.parties p ON ((dap.party_id = p.party_id)))
     LEFT JOIN public.aliases a1 ON (((a1.party_id = p.party_id) AND (a1.sequence_number = 1))))
     LEFT JOIN public.aliases a2 ON (((a2.party_id = p.party_id) AND (a2.sequence_number = 2))))
     LEFT JOIN public.aliases a3 ON (((a3.party_id = p.party_id) AND (a3.sequence_number = 3))))
     LEFT JOIN public.aliases a4 ON (((a4.party_id = p.party_id) AND (a4.sequence_number = 4))))
     LEFT JOIN public.aliases a5 ON (((a5.party_id = p.party_id) AND (a5.sequence_number = 5))))
  WHERE (pt.active IS TRUE);

--
-- Name: VIEW v_defendant_accounts_summary; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_defendant_accounts_summary IS 'Retrieves defendant account summary information for the At a Glance section';

--
-- Name: v_enforcement_status; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_enforcement_status AS
 SELECT da.defendant_account_id,
    da.account_status,
    da.collection_order,
    da.jail_days AS days_in_default,
    da.enforcing_court_id,
    da.last_enforcement,
    da.enf_override_result_id,
    da.enf_override_enforcer_id,
    da.enf_override_tfo_lja_id,
    e.reason,
    e.enforcer_id,
    e.posted_date,
    e.hearing_date,
    e.hearing_court_id,
    e.result_responses,
    e.warrant_reference,
    e.jail_days,
    dap.association_type,
    p.birth_date,
    p.age,
    p.organisation,
    r.result_id,
    r.result_title,
    r.enf_next_permitted_actions,
        CASE
            WHEN (dd.employer_name IS NOT NULL) THEN true
            ELSE false
        END AS employer_flag
   FROM (((((public.defendant_accounts da
     LEFT JOIN public.defendant_account_parties dap ON ((da.defendant_account_id = dap.defendant_account_id)))
     LEFT JOIN public.parties p ON ((dap.party_id = p.party_id)))
     LEFT JOIN public.enforcements e ON ((da.defendant_account_id = e.defendant_account_id)))
     LEFT JOIN public.results r ON (((da.enf_override_result_id)::text = (r.result_id)::text)))
     LEFT JOIN public.debtor_detail dd ON ((dap.party_id = dd.party_id)));

--
-- Name: VIEW v_enforcement_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_enforcement_status IS 'Retrieves enforcement status information with defendant account details and related party information for Youth/Adult/Organisation determination';

--
-- Name: v_major_creditor_account_at_a_glance; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_major_creditor_account_at_a_glance AS
 SELECT ca.creditor_account_id,
        CASE
            WHEN (ca.pay_by_bacs IS TRUE) THEN 'PROVIDED'::text
            ELSE 'NOT PROVIDED'::text
        END AS bacs_details,
    mj.name,
    mj.address_line_1,
    mj.address_line_2,
    mj.address_line_3,
    mj.postcode
   FROM (public.creditor_accounts ca
     JOIN public.major_creditors mj ON ((ca.major_creditor_id = mj.major_creditor_id)))
  WHERE (ca.creditor_account_type = 'MJ'::public.t_creditor_account_type_enum)
UNION
 SELECT ca.creditor_account_id,
        CASE
            WHEN (ca.pay_by_bacs IS TRUE) THEN 'PROVIDED'::text
            ELSE 'NOT PROVIDED'::text
        END AS bacs_details,
    (ci.item_values ->> 'name'::text) AS name,
    (ci.item_values ->> 'address_line_1'::text) AS address_line_1,
    (ci.item_values ->> 'address_line_2'::text) AS address_line_2,
    (ci.item_values ->> 'address_line_3'::text) AS address_line_3,
    NULL::character varying AS postcode
   FROM ((public.creditor_accounts ca
     JOIN public.business_units bu ON ((ca.business_unit_id = bu.business_unit_id)))
     JOIN public.configuration_items ci ON (((ci.business_unit_id = bu.business_unit_id) AND ((ci.item_name)::text = 'CENTRAL_FUND_ACCOUNT'::text))))
  WHERE (ca.creditor_account_type = 'CF'::public.t_creditor_account_type_enum);

--
-- Name: VIEW v_major_creditor_account_at_a_glance; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_major_creditor_account_at_a_glance IS 'Retrieves major creditor account at a glance information for both Major Creditors and Central Fund accounts';

--
-- Name: v_major_creditor_account_header; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_major_creditor_account_header AS
 SELECT ca.creditor_account_id,
    ca.account_number AS creditor_account_number,
    ca.creditor_account_type,
    ca.version_number,
    bu.business_unit_id,
    bu.business_unit_name,
    mj.name,
    ( SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric) AS "coalesce"
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = ca.creditor_account_id) AND (ct.transaction_type = 'PAYMNT'::public.t_creditor_transaction_type_enum) AND (ct.payment_processed = false))) AS awaiting_payout
   FROM ((public.creditor_accounts ca
     JOIN public.business_units bu ON ((bu.business_unit_id = ca.business_unit_id)))
     JOIN public.major_creditors mj ON ((mj.major_creditor_id = ca.major_creditor_id)))
  WHERE (ca.creditor_account_type = 'MJ'::public.t_creditor_account_type_enum)
UNION
 SELECT ca.creditor_account_id,
    ca.account_number AS creditor_account_number,
    ca.creditor_account_type,
    ca.version_number,
    bu.business_unit_id,
    bu.business_unit_name,
    (ci.item_values ->> 'name'::text) AS name,
    ( SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric) AS "coalesce"
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = ca.creditor_account_id) AND (ct.transaction_type = 'PAYMNT'::public.t_creditor_transaction_type_enum) AND (ct.payment_processed = false))) AS awaiting_payout
   FROM ((public.creditor_accounts ca
     JOIN public.business_units bu ON ((bu.business_unit_id = ca.business_unit_id)))
     JOIN public.configuration_items ci ON (((bu.business_unit_id = ci.business_unit_id) AND ((ci.item_name)::text = 'CENTRAL_FUND_ACCOUNT'::text))))
  WHERE (ca.creditor_account_type = 'CF'::public.t_creditor_account_type_enum);

--
-- Name: VIEW v_major_creditor_account_header; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_major_creditor_account_header IS 'Retrieves major creditor account header information';

--
-- Name: v_minor_creditor_account_header; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_minor_creditor_account_header AS
 SELECT DISTINCT t.creditor_account_id,
    t.creditor_account_number,
    t.creditor_account_type,
    t.version_number,
    p.party_id,
    p.title,
    p.forenames,
    p.surname,
    p.organisation,
    p.organisation_name,
    bu.business_unit_id,
    bu.business_unit_name,
    bu.welsh_language,
        CASE
            WHEN (imp.creditor_account_id IS NOT NULL) THEN true
            ELSE false
        END AS has_associated_defendant,
    COALESCE(( SELECT sum(i.imposed_amount) AS sum
           FROM public.impositions i
          WHERE (i.creditor_account_id = t.creditor_account_id)), (0)::numeric) AS awarded,
    COALESCE(( SELECT sum(ct.transaction_amount) AS sum
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = t.creditor_account_id) AND (ct.payment_processed IS TRUE))), (0)::numeric) AS paid_out,
    COALESCE(( SELECT sum(ct.transaction_amount) AS sum
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = t.creditor_account_id) AND (ct.payment_processed IS FALSE))), (0)::numeric) AS awaiting_payment,
    COALESCE(( SELECT (sum(i.imposed_amount) - sum(i.paid_amount))
           FROM public.impositions i
          WHERE (i.creditor_account_id = t.creditor_account_id)), (0)::numeric) AS outstanding
   FROM (((( SELECT ca.creditor_account_id,
            ca.account_number AS creditor_account_number,
            ca.creditor_account_type,
            ca.version_number,
            ca.minor_creditor_party_id,
            ca.business_unit_id
           FROM public.creditor_accounts ca
          WHERE (ca.creditor_account_type = 'MN'::public.t_creditor_account_type_enum)) t
     JOIN public.parties p ON ((t.minor_creditor_party_id = p.party_id)))
     JOIN public.business_units bu ON ((t.business_unit_id = bu.business_unit_id)))
     LEFT JOIN ( SELECT DISTINCT impositions.creditor_account_id
           FROM public.impositions) imp ON ((t.creditor_account_id = imp.creditor_account_id)));

--
-- Name: VIEW v_minor_creditor_account_header; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_minor_creditor_account_header IS 'Retrieves minor creditor accounts header information';

--
-- Name: v_minor_creditor_accounts_summary; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_minor_creditor_accounts_summary AS
 SELECT DISTINCT t.creditor_account_id,
    t.creditor_account_number,
    t.pay_by_bacs,
    t.version_number,
    t.hold_payout,
    p_mc.party_id,
    p_mc.title AS creditor_title,
    p_mc.forenames AS creditor_forenames,
    p_mc.surname AS creditor_surname,
    p_mc.organisation AS creditor_organisation,
    p_mc.organisation_name AS creditor_organisation_name,
    p_mc.address_line_1 AS creditor_address_line_1,
    p_mc.address_line_2 AS creditor_address_line_2,
    p_mc.address_line_3 AS creditor_address_line_3,
    p_mc.address_line_4 AS creditor_address_line_4,
    p_mc.address_line_5 AS creditor_address_line_5,
    p_mc.postcode AS creditor_postcode,
    da.defendant_account_id,
    da.account_number AS defendant_account_number,
    p_da.title AS defendant_title,
    p_da.forenames AS defendant_forenames,
    p_da.surname AS defendant_surname
   FROM (((((( SELECT ca.creditor_account_id,
            ca.account_number AS creditor_account_number,
            ca.pay_by_bacs,
            ca.version_number,
            ca.hold_payout,
            ca.minor_creditor_party_id
           FROM public.creditor_accounts ca
          WHERE (ca.creditor_account_type = 'MN'::public.t_creditor_account_type_enum)) t
     JOIN public.parties p_mc ON ((t.minor_creditor_party_id = p_mc.party_id)))
     LEFT JOIN public.impositions i ON ((t.creditor_account_id = i.creditor_account_id)))
     LEFT JOIN public.defendant_accounts da ON ((i.defendant_account_id = da.defendant_account_id)))
     LEFT JOIN public.defendant_account_parties dap ON (((da.defendant_account_id = dap.defendant_account_id) AND (dap.association_type = 'Defendant'::public.t_association_type_enum))))
     LEFT JOIN public.parties p_da ON ((dap.party_id = p_da.party_id)));

--
-- Name: VIEW v_minor_creditor_accounts_summary; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_minor_creditor_accounts_summary IS 'Retrieves minor creditor accounts summary information';

--
-- Name: v_search_defendant_accounts; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_search_defendant_accounts AS
 SELECT da.defendant_account_id,
    da.account_number,
    da.prosecutor_case_reference,
    da.last_enforcement,
    da.account_status,
    da.account_balance AS defendant_account_balance,
    da.completed_date,
    bu.business_unit_id,
    bu.business_unit_name,
    p_def.party_id,
    p_def.organisation,
    p_def.organisation_name,
    p_def.address_line_1,
    p_def.postcode,
    p_def.title,
    p_def.forenames,
    p_def.surname,
    p_def.birth_date,
    p_def.national_insurance_number,
    p_pg.surname AS parent_guardian_surname,
    p_pg.forenames AS parent_guardian_forenames,
        CASE
            WHEN p_def.organisation THEN (a1.organisation_name)::text
            ELSE NULLIF(TRIM(BOTH FROM (((COALESCE(a1.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a1.surname, ''::character varying))::text)), ''::text)
        END AS alias1,
        CASE
            WHEN p_def.organisation THEN (a2.organisation_name)::text
            ELSE NULLIF(TRIM(BOTH FROM (((COALESCE(a2.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a2.surname, ''::character varying))::text)), ''::text)
        END AS alias2,
        CASE
            WHEN p_def.organisation THEN (a3.organisation_name)::text
            ELSE NULLIF(TRIM(BOTH FROM (((COALESCE(a3.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a3.surname, ''::character varying))::text)), ''::text)
        END AS alias3,
        CASE
            WHEN p_def.organisation THEN (a4.organisation_name)::text
            ELSE NULLIF(TRIM(BOTH FROM (((COALESCE(a4.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a4.surname, ''::character varying))::text)), ''::text)
        END AS alias4,
        CASE
            WHEN p_def.organisation THEN (a5.organisation_name)::text
            ELSE NULLIF(TRIM(BOTH FROM (((COALESCE(a5.forenames, ''::character varying))::text || ' '::text) || (COALESCE(a5.surname, ''::character varying))::text)), ''::text)
        END AS alias5
   FROM ((((((((((public.defendant_accounts da
     JOIN public.business_units bu ON ((da.business_unit_id = bu.business_unit_id)))
     JOIN public.defendant_account_parties dap_def ON (((da.defendant_account_id = dap_def.defendant_account_id) AND (dap_def.association_type = 'Defendant'::public.t_association_type_enum))))
     JOIN public.parties p_def ON ((dap_def.party_id = p_def.party_id)))
     LEFT JOIN public.defendant_account_parties dap_pg ON (((da.defendant_account_id = dap_pg.defendant_account_id) AND (dap_pg.association_type = 'Parent/Guardian'::public.t_association_type_enum))))
     LEFT JOIN public.parties p_pg ON ((dap_pg.party_id = p_pg.party_id)))
     LEFT JOIN public.aliases a1 ON (((a1.party_id = p_def.party_id) AND (a1.sequence_number = 1))))
     LEFT JOIN public.aliases a2 ON (((a2.party_id = p_def.party_id) AND (a2.sequence_number = 2))))
     LEFT JOIN public.aliases a3 ON (((a3.party_id = p_def.party_id) AND (a3.sequence_number = 3))))
     LEFT JOIN public.aliases a4 ON (((a4.party_id = p_def.party_id) AND (a4.sequence_number = 4))))
     LEFT JOIN public.aliases a5 ON (((a5.party_id = p_def.party_id) AND (a5.sequence_number = 5))));

--
-- Name: VIEW v_search_defendant_accounts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_search_defendant_accounts IS 'Retrieves defendant account information for Search and Matches';

--
-- Name: v_search_defendant_accounts_consolidation; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_search_defendant_accounts_consolidation AS
 SELECT vsda.defendant_account_id,
    vsda.account_number,
    vsda.prosecutor_case_reference,
    vsda.last_enforcement,
    vsda.account_status,
    vsda.defendant_account_balance,
    vsda.completed_date,
    vsda.business_unit_id,
    vsda.business_unit_name,
    vsda.party_id,
    vsda.organisation,
    vsda.organisation_name,
    vsda.address_line_1,
    vsda.postcode,
    vsda.title,
    vsda.forenames,
    vsda.surname,
    vsda.birth_date,
    vsda.national_insurance_number,
    vsda.parent_guardian_surname,
    vsda.parent_guardian_forenames,
    vsda.alias1,
    vsda.alias2,
    vsda.alias3,
    vsda.alias4,
    vsda.alias5,
    da.collection_order AS has_collection_order,
    da.version_number,
        CASE
            WHEN ((((
            CASE
                WHEN (vsda.account_status = ANY (ARRAY['TA'::public.t_da_account_status_enum, 'WO'::public.t_da_account_status_enum, 'CS'::public.t_da_account_status_enum])) THEN 1
                ELSE 0
            END +
            CASE
                WHEN ((vsda.last_enforcement)::text = ANY ((ARRAY['BWTD'::character varying, 'BWTU'::character varying, 'CLAMPO'::character varying, 'CONF'::character varying, 'CW'::character varying, 'CWN'::character varying, 'DW'::character varying, 'NAP'::character varying, 'NAWT'::character varying, 'NBWT'::character varying, 'REW'::character varying, 'S136'::character varying, 'S18'::character varying, 'SC'::character varying, 'SUMM'::character varying, 'TFOOUT'::character varying])::text[])) THEN 1
                ELSE 0
            END) +
            CASE
                WHEN (COALESCE(da.jail_days, 0) > 0) THEN 1
                ELSE 0
            END) +
            CASE
                WHEN (da.enforcement_case_status IS NOT NULL) THEN 1
                ELSE 0
            END) = 0) THEN NULL::text[]
            ELSE array_remove(ARRAY[
            CASE
                WHEN (vsda.account_status = ANY (ARRAY['TA'::public.t_da_account_status_enum, 'WO'::public.t_da_account_status_enum, 'CS'::public.t_da_account_status_enum])) THEN (('CON.ER.1|Account status is `'::text || vsda.account_status) || '`'::text)
                ELSE NULL::text
            END,
            CASE
                WHEN ((vsda.last_enforcement)::text = ANY ((ARRAY['BWTD'::character varying, 'BWTU'::character varying, 'CLAMPO'::character varying, 'CONF'::character varying, 'CW'::character varying, 'CWN'::character varying, 'DW'::character varying, 'NAP'::character varying, 'NAWT'::character varying, 'NBWT'::character varying, 'REW'::character varying, 'S136'::character varying, 'S18'::character varying, 'SC'::character varying, 'SUMM'::character varying, 'TFOOUT'::character varying])::text[])) THEN (('CON.ER.3|Last enforcement action on the account is `'::text || COALESCE(((((r_last.result_title)::text || '('::text) || (vsda.last_enforcement)::text) || ')'::text), (vsda.last_enforcement)::text)) || '`'::text)
                ELSE NULL::text
            END,
            CASE
                WHEN (COALESCE(da.jail_days, 0) > 0) THEN 'CON.ER.4|Account has days in default'::text
                ELSE NULL::text
            END,
            CASE
                WHEN (da.enforcement_case_status IS NOT NULL) THEN 'CON.ER.5|Account linked to outstanding active case'::text
                ELSE NULL::text
            END], NULL::text)
        END AS errors,
        CASE
            WHEN ((
            CASE
                WHEN ((vsda.last_enforcement)::text = ANY ((ARRAY['ABDC'::character varying, 'AEO'::character varying, 'AEOC'::character varying, 'FSN'::character varying, 'MAN'::character varying, 'MPSO'::character varying, 'REGF'::character varying, 'UPWO'::character varying, 'PRIS'::character varying, 'NOENF'::character varying])::text[])) THEN 1
                ELSE 0
            END +
            CASE
                WHEN (public.has_uncleared_cheques(da.defendant_account_id) = true) THEN 1
                ELSE 0
            END) = 0) THEN NULL::text[]
            ELSE array_remove(ARRAY[
            CASE
                WHEN ((vsda.last_enforcement)::text = ANY ((ARRAY['ABDC'::character varying, 'AEO'::character varying, 'AEOC'::character varying, 'FSN'::character varying, 'MAN'::character varying, 'MPSO'::character varying, 'REGF'::character varying, 'UPWO'::character varying, 'PRIS'::character varying, 'NOENF'::character varying])::text[])) THEN (('CON.WN.1|Last enforcement action on the account is `'::text || COALESCE(((((r_last.result_title)::text || '('::text) || (vsda.last_enforcement)::text) || ')'::text), (vsda.last_enforcement)::text)) || '`'::text)
                ELSE NULL::text
            END,
            CASE
                WHEN (public.has_uncleared_cheques(da.defendant_account_id) = true) THEN 'CON.WN.2|Account has uncleared cheque payments'::text
                ELSE NULL::text
            END], NULL::text)
        END AS warnings
   FROM ((public.defendant_accounts da
     JOIN public.v_search_defendant_accounts vsda ON ((da.defendant_account_id = vsda.defendant_account_id)))
     LEFT JOIN public.results r_last ON (((r_last.result_id)::text = (vsda.last_enforcement)::text)));

--
-- Name: VIEW v_search_defendant_accounts_consolidation; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_search_defendant_accounts_consolidation IS 'Retrieves defendant account information for consolidation search';

--
-- Name: v_search_minor_creditor_accounts; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_search_minor_creditor_accounts AS
 SELECT DISTINCT ca.creditor_account_id,
    ca.account_number,
    bu.business_unit_id,
    bu.business_unit_name,
    p_ca.party_id,
    p_ca.organisation,
    p_ca.organisation_name,
    p_ca.address_line_1,
    p_ca.postcode,
    p_ca.forenames,
    p_ca.surname,
    i.defendant_account_id,
    p_da.organisation_name AS defendant_organisation_name,
    p_da.forenames AS defendant_forenames,
    p_da.surname AS defendant_surname,
    ( SELECT COALESCE(sum(ct.transaction_amount), (0)::numeric(18,2)) AS "coalesce"
           FROM public.creditor_transactions ct
          WHERE ((ct.creditor_account_id = ca.creditor_account_id) AND (ct.transaction_type = 'PAYMNT'::public.t_creditor_transaction_type_enum) AND (ct.payment_processed = false))) AS creditor_account_balance
   FROM (((((public.creditor_accounts ca
     JOIN public.business_units bu ON ((ca.business_unit_id = bu.business_unit_id)))
     JOIN public.parties p_ca ON ((ca.minor_creditor_party_id = p_ca.party_id)))
     LEFT JOIN public.impositions i ON ((ca.creditor_account_id = i.creditor_account_id)))
     LEFT JOIN public.defendant_account_parties dap ON (((i.defendant_account_id = dap.defendant_account_id) AND (dap.association_type = 'Defendant'::public.t_association_type_enum))))
     LEFT JOIN public.parties p_da ON ((dap.party_id = p_da.party_id)));

--
-- Name: VIEW v_search_minor_creditor_accounts; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON VIEW public.v_search_minor_creditor_accounts IS 'Retrieves minor creditors information for Search and Matches';

-- Completed on 2026-05-01 14:25:59 BST

--
-- PostgreSQL database dump complete
--


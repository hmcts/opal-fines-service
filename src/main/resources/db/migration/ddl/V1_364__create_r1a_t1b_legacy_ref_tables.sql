/**
* OPAL Program
*
* MODULE      : create_r1a_r1b_legacy_ref_tables.sql
*
* DESCRIPTION : Clone R1A/R1B reference tables, with a suffix of _legacy.
*               Indexes and Unique constraints will be automatically cloned, sequences and FK's will not be.
*               COURTS              - Including sequence and FK's to other ref tables
*               ENFORCERS           - Including sequence and FK to business_units
*               LOCAL_JUSTICE_AREAS 
*               MAJOR_CREDITORS     - Including sequence and FK to business_units
*               OFFENCES            - Including FK to business_units
*               PRISONS             - Including sequence and FK to business_units
*               PROSECUTORS
*               RESULTS             - Including FK to imposition_category
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------------
* 19/10/2026    T McCallion    1.0         PO-10770 - Create _legacy R1A reference data tables
*
**/

--Create _legacy tables
CREATE TABLE courts_legacy (
    LIKE courts INCLUDING ALL
);

CREATE TABLE enforcers_legacy (
    LIKE enforcers INCLUDING ALL
);

CREATE TABLE local_justice_areas_legacy (
    LIKE local_justice_areas INCLUDING ALL
);

CREATE TABLE major_creditors_legacy (
    LIKE major_creditors INCLUDING ALL
);

CREATE TABLE offences_legacy (
    LIKE offences INCLUDING ALL
);

CREATE TABLE prisons_legacy (
    LIKE prisons INCLUDING ALL
);

CREATE TABLE prosecutors_legacy (
    LIKE prosecutors INCLUDING ALL
);

CREATE TABLE results_legacy (
    LIKE results INCLUDING ALL
);

--Create sequences for _legacy tables and set their owner - (Only for: COURTS, ENFORCERS, MAJOR_CREDITORS, PRISONS)
CREATE SEQUENCE legacy_court_id_seq
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    START 60000000000000
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE legacy_enforcer_id_seq
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    START 60000000000000
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE legacy_major_creditor_id_seq
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    START 60000000000000
    CACHE 1
    NO CYCLE;

CREATE SEQUENCE legacy_prison_id_seq
    INCREMENT BY 1
    MINVALUE 60000000000000
    NO MAXVALUE
    START 60000000000000
    CACHE 1
    NO CYCLE;

ALTER SEQUENCE legacy_court_id_seq          OWNED BY courts_legacy.court_id;
ALTER SEQUENCE legacy_enforcer_id_seq       OWNED BY enforcers_legacy.enforcer_id;
ALTER SEQUENCE legacy_major_creditor_id_seq OWNED BY major_creditors_legacy.major_creditor_id;
ALTER SEQUENCE legacy_prison_id_seq         OWNED BY prisons_legacy.prison_id;


--Create FK's between _legacy tables and other 'reference' tables  (LOCAL_JUSTICE_AREAS, PROSECUTORS do not have any FK's)
--
ALTER TABLE courts_legacy ADD CONSTRAINT crt_legacy_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);
--Following was dropped as part of R1A go-live
--ALTER TABLE courts_legacy ADD CONSTRAINT crt_legacy_local_justice_area_id_fk FOREIGN KEY (local_justice_area_id) REFERENCES local_justice_areas_legacy(local_justice_area_id);
--ALTER TABLE courts_legacy ADD CONSTRAINT crt_legacy_parent_court_id_fk FOREIGN KEY (parent_court_id) REFERENCES courts_legacy(court_id);

ALTER TABLE enforcers_legacy ADD CONSTRAINT enf_legacy_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE major_creditors_legacy ADD CONSTRAINT mc_legacy_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE offences_legacy ADD CONSTRAINT off_legacy_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE prisons_legacy ADD CONSTRAINT pri_legacy_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

ALTER TABLE results_legacy ADD CONSTRAINT results_legacy_imposition_category_fk FOREIGN KEY (imposition_category) REFERENCES imposition_categories(imposition_category);

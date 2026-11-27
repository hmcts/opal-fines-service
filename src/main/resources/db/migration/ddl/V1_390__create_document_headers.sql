/**
* OPAL Program
*
* MODULE      : create_document_headers.sql
*
* DESCRIPTION : Create DOCUMENT_HEADERS table and related components.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    --------------------------------------------------------------------------
* 24/11/2026    TT          1.0         PO-11363 Create DOCUMENT_HEADERS table and related components.
*
**/

CREATE TABLE document_headers
(
    business_unit_id SMALLINT     NOT NULL,
    name_en          VARCHAR(50)  NOT NULL,
    name_cy          VARCHAR(50),
    address_en       JSON         NOT NULL,
    address_cy       JSON,
    postcode         VARCHAR(10)  NOT NULL,
    telephone        VARCHAR(35)  NOT NULL,
    email_address    VARCHAR(80)  NOT NULL,
    CONSTRAINT document_headers_pk PRIMARY KEY (business_unit_id)
);

COMMENT ON COLUMN document_headers.business_unit_id IS 'The business unit ID that this document header is associated with';
COMMENT ON COLUMN document_headers.name_en IS 'The name, in English, shown when this header is used';
COMMENT ON COLUMN document_headers.name_cy IS 'The name, in Welsh, shown when this header is used';
COMMENT ON COLUMN document_headers.address_en IS 'English address JSON object with string fields address_line_1, address_line_2, address_line_3, address_line_4, and address_line_5';
COMMENT ON COLUMN document_headers.address_cy IS 'Welsh address JSON object with the same string fields as address_en: address_line_1, address_line_2, address_line_3, address_line_4, and address_line_5';
COMMENT ON COLUMN document_headers.postcode IS 'The postcode shown when this header is used';
COMMENT ON COLUMN document_headers.telephone IS 'The telephone number shown when this header is used';
COMMENT ON COLUMN document_headers.email_address IS 'The email address shown when this header is used';

ALTER TABLE document_headers
    ADD CONSTRAINT dh_business_unit_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units (business_unit_id);

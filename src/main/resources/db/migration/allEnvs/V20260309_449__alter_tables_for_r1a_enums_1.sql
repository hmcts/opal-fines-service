/**
* CGI OPAL Program
*
* MODULE      : alter_tables_for_r1a_enums_1.sql
*
* DESCRIPTION : Alter specific columns on tables to an ENUM data type.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------------------------------------------------------------------
* 05/03/2026    T McCallion    1.0         PO-2846 - Update column local_justice_areas.lja_type to use a  postgresql enum instead of varchar
*                                          PO-2850 - Update columns on DOCUMENTS table to use postgresql enum instead of varchar/integer 
*                                          PO-2856 - Fines: Update column business_units.business_unit_type to use a PostgreSQL Enum instead of varchar 
*                                          PO-2869 - Update column parties.account_type to use a PostgreSQL Enum instead of varchar
**/

--LOCAL_JUSTICE_AREAS - PO-2846
ALTER TABLE local_justice_areas 
   ALTER COLUMN lja_type TYPE t_lja_type_enum 
   USING lja_type::t_lja_type_enum; 
   
COMMENT ON COLUMN local_justice_areas.lja_type IS 'The LJA type. Specific values can be found in the DB LLD on Confluence';

--DOCUMENTS - PO-2850
ALTER TABLE documents 
    DROP CONSTRAINT IF EXISTS d_document_language_cc,
    DROP CONSTRAINT IF EXISTS d_header_type_cc,
    DROP CONSTRAINT IF EXISTS d_priority_cc,
    DROP CONSTRAINT IF EXISTS d_recipient_cc,
    DROP CONSTRAINT IF EXISTS d_signature_source_cc;

ALTER TABLE documents 
    ALTER COLUMN document_language TYPE t_language_enum 
        USING document_language::t_language_enum,
    ALTER COLUMN header_type TYPE t_header_type_enum
        USING header_type::t_header_type_enum,
    ALTER COLUMN priority TYPE t_priority_enum
        USING priority::TEXT::t_priority_enum,
    ALTER COLUMN recipient TYPE t_recipient_enum
        USING recipient::t_recipient_enum,
    ALTER COLUMN signature_source TYPE t_signature_source_enum
        USING signature_source::t_signature_source_enum;


COMMENT ON COLUMN documents.document_language IS 'The language the document is written in. Specific values can be found in the DB LLD on Confluence';
COMMENT ON COLUMN documents.header_type IS 'The type of header output on the document. Specific values can be found in the DB LLD on Confluence';
COMMENT ON COLUMN documents.priority IS 'Determines the order of printing with respect to other documents in the same batch. Specific values can be found in the DB LLD on Confluence';
COMMENT ON COLUMN documents.recipient IS 'The type of party that this document will be addressed to. Specific values can be found in the DB LLD on Confluence';
COMMENT ON COLUMN documents.signature_source IS 'Source of the document signature. Specific values can be found in the DB LLD on Confluence'; 

--BUSINESS_UNITS - PO-2856
ALTER TABLE business_units
   ALTER COLUMN business_unit_type TYPE t_business_unit_type_enum 
   USING business_unit_type::t_business_unit_type_enum;
   
--PARTIES - PO-2869
ALTER TABLE parties
   ALTER COLUMN account_type TYPE t_party_account_type_enum 
   USING account_type::t_party_account_type_enum; 

/**
* OPAL Program
*
* MODULE      : alter_interface_file_source_enum_for_ei4.sql
*
* DESCRIPTION : Add Variant Banking to the interface file source enum
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 01/09/2026    TMc            1.0         PO-8695 - Add VARIANT_BANKING to t_interface_file_source_enum
*
**/

ALTER TYPE t_interface_file_source_enum ADD VALUE 'VARIANT_BANKING';

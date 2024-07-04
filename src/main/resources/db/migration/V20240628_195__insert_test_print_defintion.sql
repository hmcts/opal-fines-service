/**
* OPAL Program
*
* MODULE      : insert_print_definition.sql
*
* DESCRIPTION : Inserts rows of data into the PRINT_DEFINITION table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 28/06/2024    T Reed      1.0         Inserts simple case test data into the PRINT_DEFINITION table
*
**/
INSERT INTO print_definition
(
 print_definition_id
,doc_type
,doc_description
,dest_main
,dest_sec1
,dest_sec2
,format
,auto_mode
,expiry_duration
,system
,created_date
,template_id
,address_val_element
,doc_doc_id
,xslt
,linked_areas
,template_file
)
VALUES
(
 600000000
,'TEST_PDF_definition_id'
,'Test document for PDF'
,'PORTAL'
,null
,null
,'PDF'
,'ON'
,7
,'LIBRA'
,null
,'test_version_1'
,null
,2366
,'<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="simple" page-height="29.7cm" page-width="21cm" margin="2cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="simple">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="14pt" font-weight="bold" text-align="center" space-after.optimum="15pt">OPAL Fines Test PDF</fo:block>
                    <fo:block font-size="12pt" space-after.optimum="10pt">Name: <xsl:value-of select="fine/name"/></fo:block>
                    <fo:block font-size="12pt" space-after.optimum="10pt">Account Number: <xsl:value-of select="fine/accountNumber"/></fo:block>
                    <fo:block font-size="12pt" space-after.optimum="10pt">Balance: £<xsl:value-of select="fine/balance"/></fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>'
,null
,'test.xsl'
);

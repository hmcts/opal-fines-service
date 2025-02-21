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
* 01/08/2024    A Dennis    1.0         PO-208 Inserts rows of data into the PRINT_DEFINITION table
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
 700000000
,'Test_pdf_2'
,'Complett'
,'PORTAL'
,null
,null
,'PDF'
,'ON'
,7
,'LIBRA'
,null
,'test_version2'
,null
,2366
,'<?xml version="1.0" encoding="UTF-8"?>
<xslt:stylesheet xmlns:date="http://exslt.org/dates-and-times"
                 xmlns:str="http://exslt.org/strings"
                 xmlns:fo="http://www.w3.org/1999/XSL/Format"
                 xmlns:xf="http://www.ecrion.com/xf/1.0"
                 xmlns:xc="http://www.ecrion.com/2008/xc"
                 xmlns:xfd="http://www.ecrion.com/xfd/1.0"
                 xmlns:svg="http://www.w3.org/2000/svg"
                 xmlns:msxsl="urn:schemas-microsoft-com:xslt"
                 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                 xmlns:msxml="urn:schemas-microsoft-com:xslt"
                 xmlns:pss-prreq="http://www.w3.org/1999/XSL/Transform"
                 xmlns:xslt="http://www.w3.org/1999/XSL/Transform"
                 version="1.0"
                 extension-element-prefixes="date str">
   <xslt:output indent="no" encoding="utf-8"/>
   <xslt:param name="XFCrtLocalDate">2023-04-05</xslt:param>
   <xslt:param name="XFCrtLocalTime">15:54:38</xslt:param>
   <xslt:param name="XFCrtLocalDateTime">2023-04-05T15:54:38</xslt:param>
   <xslt:param name="XFCrtUTCDate">2023-04-05</xslt:param>
   <xslt:param name="XFCrtUTCTime">14:54:38</xslt:param>
   <xslt:param name="XFCrtUTCDateTime">2023-04-05T14:54:38</xslt:param>
   <xslt:param name="XFOutputFormat"/>
   <xslt:param name="XFTemplateName">COMPLETT-46_1.epr</xslt:param>
   <xslt:param name="EOSEnvironment">Undefined</xslt:param>
   <xslt:param name="EOSWorkspace">Undefined</xslt:param>
   <xslt:param name="EOSTemplateVersion">-1</xslt:param>
   <xslt:param name="EOSUserName">Undefined</xslt:param>
   <xslt:param name="EOSJobId">Undefined</xslt:param>
   <xslt:param name="XFTranslationID">en_US</xslt:param>
   <xsl:template name="zero_width_space_1">
      <xsl:param name="data"/>
      <xsl:param name="counter" select="0"/>
      <xsl:variable name="length" select="string-length($data)"/>
      <xsl:choose>
         <xsl:when test="$counter &lt; $length">
            <xsl:value-of select="concat(substring($data,$counter,1),''​'')"/>
            <xsl:call-template name="zero_width_space_2">
               <xsl:with-param name="data" select="$data"/>
               <xsl:with-param name="counter" select="$counter+1"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:if test="($length mod 2) = 0">
               <xsl:value-of select="substring($data, $length, 1)"/>
            </xsl:if>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template name="zero_width_space_2">
      <xsl:param name="data"/>
      <xsl:param name="counter"/>
      <xsl:value-of select="concat(substring($data,$counter,1),''​'')"/>
      <xsl:call-template name="zero_width_space_1">
         <xsl:with-param name="data" select="$data"/>
         <xsl:with-param name="counter" select="$counter+1"/>
      </xsl:call-template>
   </xsl:template>
   <xsl:template match="/">
      <fo:root font-family="Arial" font-size="10.0pt">
         <xf:stylesheet src="data:;base64,LypEZWZhdWx0IHN0eWxlcyovDQouTm9ybWFsDQp7DQoJZm9udC1mYW1pbHk6IENhbGlicmk7DQoJZm9udC1zaXplOiA4cHQ7DQp9DQoNCi5Ob1NwYWNpbmcNCnsNCglmb250LWZhbWlseTogQ2FsaWJyaTsNCglmb250LXNpemU6IDhwdDsNCn0NCg0KLkhlYWRpbmcxDQp7DQoJZm9udC1mYW1pbHk6IENhbWJyaWE7DQoJZm9udC1zaXplOiAxMHB0Ow0KCWZvbnQtd2VpZ2h0OiBib2xkOw0KCWNvbG9yOiAjMzY1RjkxOw0KfQ0KDQouSGVhZGluZzINCnsNCglmb250LWZhbWlseTogQ2FtYnJpYTsNCglmb250LXNpemU6IDEwcHQ7DQoJZm9udC13ZWlnaHQ6IGJvbGQ7DQoJY29sb3I6ICM0RjgxQkQ7DQp9DQoNCi5UaXRsZQ0Kew0KCWZvbnQtZmFtaWx5OiBDYW1icmlhOw0KCWZvbnQtc2l6ZTogMjBwdDsNCglmb250LXdlaWdodDogYm9sZDsNCgljb2xvcjogIzE3MzY1RDsNCn0NCg0KLlN1YnRpdGxlDQp7DQoJZm9udC1mYW1pbHk6IENhbWJyaWE7DQoJZm9udC1zaXplOiA5cHQ7DQoJdGV4dC1kZWNvcmF0aW9uOiB1bmRlcmxpbmU7DQoJZm9udC1zdHlsZTogaXRhbGljOwkNCgljb2xvcjogIzRGODFCRDsNCn0NCg0KLlN1YnRpdGxlRW1waGFzaXMNCnsNCglmb250LWZhbWlseTogQ2FtYnJpYTsNCglmb250LXNpemU6IDlwdDsNCgl0ZXh0LWRlY29yYXRpb246IHVuZGVybGluZTsNCgljb2xvcjogIzgwODA4MDsNCn0NCg0KLkVtcGhhc2lzDQp7DQoJZm9udC1zdHlsZTogaXRhbGljOwkNCglmb250LXNpemU6IDE5cHQ7DQoJZm9udC1mYW1pbHk6IENhbWJyaWE7DQp9DQoNCi5JbnRlbnNlRW1waGFzaXMNCnsNCglmb250LWZhbWlseTogQ2FtYnJpYTsNCglmb250LXNpemU6IDE5cHQ7DQoJZm9udC1zdHlsZTogaXRhbGljOw0KCWZvbnQtd2VpZ2h0OiBib2xkOw0KCWNvbG9yOiAjNEY4MUJEOw0KfQ0KDQouU3Ryb25nDQp7DQoJZm9udC1mYW1pbHk6IENhbWJyaWE7DQoJZm9udC1zaXplOiAxOXB0Ow0KCWZvbnQtd2VpZ2h0OiBib2xkOw0KfQ=="
                        default-stylesheet="true"/>
         <xfd:schema src="data:;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjx4czpzY2hlbWEgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj4NCiAgPHhzOmVsZW1lbnQgbmFtZT0iZG9jdW1lbnQiPg0KICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iaW5mbyI+DQogICAgICAgICAgPHhzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJnZW5lcmFsIj4NCiAgICAgICAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImRvY190eXBlIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im91dHB1dHR5cGUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0idmVyc2lvbiIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJzeXN0ZW0iIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJzb3VyY2VfZmlsZV9uYW1lIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InByaW50ZXJzIj4NCiAgICAgICAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InByaW50ZXIiPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0icmVuZGVyX21hY2hpbmUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0icHJpbnRfc2VydmVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InF1ZXVlX25hbWUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0icXVhbnRpdHlfY29waWVzIiB0eXBlPSJ4czp1bnNpZ25lZEJ5dGUiIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImR1cGxleF9vdmVycmlkZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgPC94czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ic3RhcnRfdGltZSIgdHlwZT0ieHM6dGltZSIgLz4NCiAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkYXRhIj4NCiAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImpvYiI+DQogICAgICAgICAgICAgICAgPHhzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICA8eHM6Y2hvaWNlIG1heE9jY3Vycz0idW5ib3VuZGVkIj4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJvcmRlcnNvdXJjZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InNwb29sX3R5cGUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJiYXRjaF9zZXJpYWwiIHR5cGU9InhzOnVuc2lnbmVkQnl0ZSIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJvcmRlcl9pZCIgdHlwZT0ieHM6dW5zaWduZWRCeXRlIiAvPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImRpdmlzaW9uIiB0eXBlPSJ4czp1bnNpZ25lZEJ5dGUiIC8+DQogICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iaGVhZGVyIj4NCiAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImNvdXJ0bmFtZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImN5X2NvdXJ0bmFtZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpbmUxIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkb2IiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJjeV9kb2IiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJlbl9kb2IiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJhY2NvdW50bnVtYmVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZGVmZW5kYW50bmFtZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImRhdGVwcm9kdWNlZCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImRhdGVvZm9yZGVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iY3lfZGF0ZW9mb3JkZXIiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJjeV9kYXRlcHJvZHVjZWQiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJvZmZlbmNlcyI+DQogICAgICAgICAgICAgICAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJvZmZlbmNlIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImFtb3VudGltcG9zZWQiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJiYWxhbmNlb3V0c3RhbmRpbmciIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJvZmZlbmNldGl0bGUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkYXRlaW1wb3NlZCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImN5X29mZmVuY2V0aXRsZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJlbmZhYXVhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im5hbWUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImFkZHIxIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iYWRkcjIiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iY3JlZGl0b3JkZXRhaWwiPg0KICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iY3JlZGl0b3JhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im5hbWUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpbmUxIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTIiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJsaW5lMyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJwb3N0Y29kZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJhY2NvdW50bnVtYmVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkZWZhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im5hbWUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpbmUxIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTIiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJsaW5lMyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJsaW5lNCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJzaWduYXR1cmUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgPC94czpjaG9pY2U+DQogICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJlbmRfdGltZSIgdHlwZT0ieHM6dGltZSIgLz4NCiAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZWxhcHNlZHNlY3MiIC8+DQogICAgICA8L3hzOnNlcXVlbmNlPg0KICAgIDwveHM6Y29tcGxleFR5cGU+DQogIDwveHM6ZWxlbWVudD4NCjwveHM6c2NoZW1hPg=="/>
         <fo:layout-master-set>
            <fo:simple-page-master master-name="first"
                                   page-width="595pt"
                                   page-height="842pt"
                                   margin-top="12.7mm"
                                   margin-bottom="0.5in">
               <fo:region-body region-name="xsl-region-body" margin="43.3mm 50.4pt 8mm 50.4pt"/>
               <fo:region-before region-name="xsl-region-before" extent="43.3mm"/>
               <fo:region-after region-name="xsl-region-after" extent="8mm"/>
               <fo:region-start region-name="xsl-region-start" extent="50.4pt"/>
               <fo:region-end region-name="xsl-region-end" extent="50.4pt"/>
            </fo:simple-page-master>
            <fo:simple-page-master master-name="other"
                                   page-height="842pt"
                                   page-width="595pt"
                                   margin-top="0.5in"
                                   margin-bottom="0.5in">
               <fo:region-body region-name="xsl-region-body" margin="50.4pt 50.4pt 8mm 50.4pt"/>
               <fo:region-before region-name="other-region-before" extent="50.4pt"/>
               <fo:region-after region-name="other-region-after" extent="8mm"/>
               <fo:region-start region-name="other-region-start" extent="50.4pt"/>
               <fo:region-end region-name="other-region-end" extent="50.4pt"/>
            </fo:simple-page-master>
            <fo:simple-page-master master-name="blank page"
                                   page-height="842pt"
                                   page-width="595pt"
                                   margin-top="0.5in"
                                   margin-bottom="0.5in">
               <fo:region-body region-name="xsl-region-body" margin="50.4pt 50.4pt 8mm 50.4pt"/>
               <fo:region-before region-name="blank page-region-before"
                                 display-align="after"
                                 extent="50.4pt"/>
               <fo:region-after region-name="blank page-region-after" extent="8mm"/>
               <fo:region-start region-name="blank page-region-start" extent="50.4pt"/>
               <fo:region-end region-name="blank page-region-end" extent="50.4pt"/>
            </fo:simple-page-master>
            <fo:page-sequence-master master-name="ComplexMaster1">
               <fo:repeatable-page-master-alternatives maximum-repeats="no-limit">
                  <fo:conditional-page-master-reference master-reference="first" page-position="first"/>
                  <fo:conditional-page-master-reference master-reference="blank page" odd-or-even="even"/>
                  <fo:conditional-page-master-reference master-reference="other"/>
               </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>
         </fo:layout-master-set>
         <fo:page-sequence master-reference="ComplexMaster1" language="en">
            <fo:static-content flow-name="xsl-region-before">
               <fo:block>
                  <xslt:call-template name="_component_C__Users_d331153_git_app_templatetransformation_ecrion-docs_Headers_Account_epb"/>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="xsl-region-after">
               <fo:block>
                  <fo:block>
                     <fo:inline> </fo:inline>
                  </fo:block>
                  <xslt:text>			</xslt:text>
                  <fo:block>
                     <xslt:text>				</xslt:text>
                     <fo:table width="auto"
                               border-collapse="collapse"
                               table-layout="fixed"
                               margin-top="0pt">
                        <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                        <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                        <fo:table-body>
                           <fo:table-row>
                              <fo:table-cell>
                                 <fo:block font-size="8pt">
                                    <fo:inline font-size="8pt">
                                       <xslt:variable name="fieldValue_id5821522">
                                          <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/name"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id5821522)">
                                          <fo:inline id="5431929">
                                             <xslt:attribute name="id">5431929</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id5821522, '' '')">
                                                <xsl:choose>
                                                   <xsl:when test="string-length(.) &gt; 16">
                                                      <xsl:choose>
                                                         <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                            <xsl:value-of select="."/>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                            <xsl:call-template name="zero_width_space_1">
                                                               <xsl:with-param name="data" select="."/>
                                                            </xsl:call-template>
                                                         </xsl:otherwise>
                                                      </xsl:choose>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="."/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                   <xsl:text> </xsl:text>
                                                </xsl:if>
                                             </xsl:for-each>
                                          </fo:inline>
                                       </xsl:if>
                                    </fo:inline>
                                 </fo:block>
                              </fo:table-cell>
                              <fo:table-cell text-align="right">
                                 <fo:block font-size="8pt">
                                    <xslt:variable name="fieldValue_id5821533">
                                       <xslt:value-of select="/document/data/job/dateproduced"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5821533)">
                                       <fo:inline id="1226E52B" font-size="8pt">
                                          <xslt:attribute name="id">1226E52B</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5821533, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                    <fo:inline font-size="8pt">
                                       <fo:inline>/COMPLETT_</fo:inline>
                                       <xslt:variable name="fieldValue_id5821542">
                                          <xslt:value-of select="/document/info/general/version"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id5821542)">
                                          <fo:inline id="125AE52B">
                                             <xslt:attribute name="id">125AE52B</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id5821542, '' '')">
                                                <xsl:choose>
                                                   <xsl:when test="string-length(.) &gt; 16">
                                                      <xsl:choose>
                                                         <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                            <xsl:value-of select="."/>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                            <xsl:call-template name="zero_width_space_1">
                                                               <xsl:with-param name="data" select="."/>
                                                            </xsl:call-template>
                                                         </xsl:otherwise>
                                                      </xsl:choose>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="."/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                   <xsl:text> </xsl:text>
                                                </xsl:if>
                                             </xsl:for-each>
                                          </fo:inline>
                                       </xsl:if>
                                       <fo:inline>/</fo:inline>
                                       <xslt:variable name="fieldValue_id5821547">
                                          <xslt:value-of select="/document/info/general/docref"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id5821547)">
                                          <fo:inline id="128BE52B">
                                             <xslt:attribute name="id">128BE52B</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id5821547, '' '')">
                                                <xsl:choose>
                                                   <xsl:when test="string-length(.) &gt; 16">
                                                      <xsl:choose>
                                                         <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                            <xsl:value-of select="."/>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                            <xsl:call-template name="zero_width_space_1">
                                                               <xsl:with-param name="data" select="."/>
                                                            </xsl:call-template>
                                                         </xsl:otherwise>
                                                      </xsl:choose>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="."/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                   <xsl:text> </xsl:text>
                                                </xsl:if>
                                             </xsl:for-each>
                                          </fo:inline>
                                       </xsl:if>
                                       <fo:inline>/</fo:inline>
                                       <fo:page-number format="1"/>
                                    </fo:inline>
                                 </fo:block>
                              </fo:table-cell>
                           </fo:table-row>
                        </fo:table-body>
                     </fo:table>
                     <xslt:text>			</xslt:text>
                  </fo:block>
                  <xslt:text>			</xslt:text>
                  <fo:block/>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="xsl-region-start">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="xsl-region-end">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="other-region-before">
               <fo:block>
                  <xslt:call-template name="_component_C__Users_d331153_git_app_templatetransformation_ecrion-docs_Headers_ContinuationAAU_epb"/>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="other-region-after">
               <fo:block>
                  <fo:block>
                     <fo:inline> </fo:inline>
                  </fo:block>
                  <xslt:text>			</xslt:text>
                  <fo:block>
                     <xslt:text>				</xslt:text>
                     <fo:table width="auto"
                               border-collapse="collapse"
                               table-layout="fixed"
                               margin-top="0pt">
                        <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                        <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                        <fo:table-body>
                           <fo:table-row>
                              <fo:table-cell>
                                 <fo:block font-size="8pt">
                                    <fo:inline font-size="8pt">
                                       <xslt:variable name="fieldValue_id5821619">
                                          <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/name"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id5821619)">
                                          <fo:inline id="5001770">
                                             <xslt:attribute name="id">5001770</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id5821619, '' '')">
                                                <xsl:choose>
                                                   <xsl:when test="string-length(.) &gt; 16">
                                                      <xsl:choose>
                                                         <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                            <xsl:value-of select="."/>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                            <xsl:call-template name="zero_width_space_1">
                                                               <xsl:with-param name="data" select="."/>
                                                            </xsl:call-template>
                                                         </xsl:otherwise>
                                                      </xsl:choose>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="."/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                   <xsl:text> </xsl:text>
                                                </xsl:if>
                                             </xsl:for-each>
                                          </fo:inline>
                                       </xsl:if>
                                    </fo:inline>
                                 </fo:block>
                              </fo:table-cell>
                              <fo:table-cell text-align="right">
                                 <fo:block font-size="8pt">
                                    <xslt:variable name="fieldValue_id5821630">
                                       <xslt:value-of select="/document/data/job/dateproduced"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5821630)">
                                       <fo:inline id="7497E52B" font-size="8pt">
                                          <xslt:attribute name="id">7497E52B</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5821630, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                    <fo:inline font-size="8pt">
                                       <fo:inline>/COMPLETT_</fo:inline>
                                       <xslt:variable name="fieldValue_id5821639">
                                          <xslt:value-of select="/document/info/general/version"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id5821639)">
                                          <fo:inline id="74CBE52B">
                                             <xslt:attribute name="id">74CBE52B</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id5821639, '' '')">
                                                <xsl:choose>
                                                   <xsl:when test="string-length(.) &gt; 16">
                                                      <xsl:choose>
                                                         <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                            <xsl:value-of select="."/>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                            <xsl:call-template name="zero_width_space_1">
                                                               <xsl:with-param name="data" select="."/>
                                                            </xsl:call-template>
                                                         </xsl:otherwise>
                                                      </xsl:choose>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="."/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                   <xsl:text> </xsl:text>
                                                </xsl:if>
                                             </xsl:for-each>
                                          </fo:inline>
                                       </xsl:if>
                                       <fo:inline>/</fo:inline>
                                       <xslt:variable name="fieldValue_id5821644">
                                          <xslt:value-of select="/document/info/general/docref"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id5821644)">
                                          <fo:inline id="74FCE52B">
                                             <xslt:attribute name="id">74FCE52B</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id5821644, '' '')">
                                                <xsl:choose>
                                                   <xsl:when test="string-length(.) &gt; 16">
                                                      <xsl:choose>
                                                         <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                            <xsl:value-of select="."/>
                                                         </xsl:when>
                                                         <xsl:otherwise>
                                                            <xsl:call-template name="zero_width_space_1">
                                                               <xsl:with-param name="data" select="."/>
                                                            </xsl:call-template>
                                                         </xsl:otherwise>
                                                      </xsl:choose>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:value-of select="."/>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                                <xsl:if test="position() != last()">
                                                   <xsl:text> </xsl:text>
                                                </xsl:if>
                                             </xsl:for-each>
                                          </fo:inline>
                                       </xsl:if>
                                       <fo:inline>/</fo:inline>
                                       <fo:page-number format="1"/>
                                    </fo:inline>
                                 </fo:block>
                              </fo:table-cell>
                           </fo:table-row>
                        </fo:table-body>
                     </fo:table>
                     <xslt:text>			</xslt:text>
                  </fo:block>
                  <xslt:text>			</xslt:text>
                  <fo:block/>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="other-region-start">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="other-region-end">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="blank page-region-before">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="blank page-region-after">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block>
                  <xslt:text>				</xslt:text>
                  <fo:table width="auto"
                            border-collapse="collapse"
                            table-layout="fixed"
                            margin-top="0pt">
                     <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell>
                              <fo:block font-size="8pt">
                                 <fo:inline font-size="8pt">
                                    <xslt:variable name="fieldValue_id5822582">
                                       <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/name"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5822582)">
                                       <fo:inline id="405F16A1">
                                          <xslt:attribute name="id">405F16A1</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5822582, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell text-align="right">
                              <fo:block font-size="8pt">
                                 <xslt:variable name="fieldValue_id5822594">
                                    <xslt:value-of select="/document/data/job/dateproduced"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5822594)">
                                    <fo:inline id="409016A1" font-size="8pt">
                                       <xslt:attribute name="id">409016A1</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5822594, '' '')">
                                          <xsl:choose>
                                             <xsl:when test="string-length(.) &gt; 16">
                                                <xsl:choose>
                                                   <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                      <xsl:value-of select="."/>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:call-template name="zero_width_space_1">
                                                         <xsl:with-param name="data" select="."/>
                                                      </xsl:call-template>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                          <xsl:if test="position() != last()">
                                             <xsl:text> </xsl:text>
                                          </xsl:if>
                                       </xsl:for-each>
                                    </fo:inline>
                                 </xsl:if>
                                 <fo:inline font-size="8pt">
                                    <fo:inline>/COMPLETT_</fo:inline>
                                    <xslt:variable name="fieldValue_id5822603">
                                       <xslt:value-of select="/document/info/general/version"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5822603)">
                                       <fo:inline id="40C516A1">
                                          <xslt:attribute name="id">40C516A1</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5822603, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                    <fo:inline>/</fo:inline>
                                    <xslt:variable name="fieldValue_id5822608">
                                       <xslt:value-of select="/document/info/general/docref"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5822608)">
                                       <fo:inline id="40F916A1">
                                          <xslt:attribute name="id">40F916A1</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5822608, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                    <fo:inline>/</fo:inline>
                                    <fo:page-number format="1"/>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
               </fo:block>
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="blank page-region-start">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="blank page-region-end">
               <fo:block>
                  <fo:inline> </fo:inline>
               </fo:block>
            </fo:static-content>
            <fo:flow flow-name="xsl-region-body">
               <fo:block>
                  <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                     <fo:table-body>
                        <fo:table-row height="auto">
                           <fo:table-cell border-left-color="rgb(0, 0, 0)"
                                          border-left-width="1pt"
                                          border-right-color="rgb(0, 0, 0)"
                                          border-right-width="1pt"
                                          border-top-color="rgb(0, 0, 0)"
                                          border-top-width="1pt"
                                          border-bottom-color="rgb(0, 0, 0)"
                                          border-bottom-width="1pt"
                                          number-columns-spanned="2">
                              <fo:block-container height="4.2mm" overflow="hidden">
                                 <fo:block>
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/creditordetail/creditoraddress/name) != ''''">
                                          <fo:inline id="5437339">
                                             <xslt:attribute name="id">5437339</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5823084">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/name"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823084)">
                                                   <fo:inline id="4317CDC5">
                                                      <xslt:attribute name="id">4317CDC5</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823084, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:inline>
                                          </fo:inline>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:inline id="5054086">
                                             <xslt:attribute name="id">5054086</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5823092">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/name"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823092)">
                                                   <fo:inline id="4870363">
                                                      <xslt:attribute name="id">4870363</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823092, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:inline>
                                          </fo:inline>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell border-left-color="rgb(0, 0, 0)"
                                          border-left-width="1pt"
                                          border-right-color="rgb(0, 0, 0)"
                                          border-right-width="1pt"
                                          border-top-color="rgb(0, 0, 0)"
                                          border-top-width="1pt"
                                          border-bottom-color="rgb(0, 0, 0)"
                                          border-bottom-width="1pt">
                              <fo:block-container height="29.8mm" overflow="hidden">
                                 <fo:block>
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/creditordetail/creditoraddress) != ''''">
                                          <fo:block id="5281191">
                                             <xslt:attribute name="id">5281191</xslt:attribute>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823125">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/address/line1"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823125)">
                                                   <fo:inline id="2EDBCDC7">
                                                      <xslt:attribute name="id">2EDBCDC7</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823125, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823131">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/address/line2"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823131)">
                                                   <fo:inline id="2F0CCDC7">
                                                      <xslt:attribute name="id">2F0CCDC7</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823131, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823137">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/address/line3"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823137)">
                                                   <fo:inline id="2F0CCDC7">
                                                      <xslt:attribute name="id">2F0CCDC7_id5823137</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823137, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823143">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/address/line4"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823143)">
                                                   <fo:inline id="2F40CDC7">
                                                      <xslt:attribute name="id">2F40CDC7</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823143, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823150">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/address/line5"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823150)">
                                                   <fo:inline id="2F71CDC7">
                                                      <xslt:attribute name="id">2F71CDC7</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823150, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823156">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/address/postcode"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823156)">
                                                   <fo:inline id="2FA5CDC7">
                                                      <xslt:attribute name="id">2FA5CDC7</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823156, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:block id="5583781">
                                             <xslt:attribute name="id">5583781</xslt:attribute>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823165">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/address/line1"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823165)">
                                                   <fo:inline id="4985346">
                                                      <xslt:attribute name="id">4985346</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823165, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823171">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/address/line2"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823171)">
                                                   <fo:inline id="5701284">
                                                      <xslt:attribute name="id">5701284</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823171, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823178">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/address/line3"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823178)">
                                                   <fo:inline id="5509307">
                                                      <xslt:attribute name="id">5509307</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823178, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823184">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/address/line4"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823184)">
                                                   <fo:inline id="4FCBCDC9">
                                                      <xslt:attribute name="id">4FCBCDC9</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823184, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823190">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/address/line5"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823190)">
                                                   <fo:inline id="5AF1CDC9">
                                                      <xslt:attribute name="id">5AF1CDC9</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823190, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5823197">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/address/postcode"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5823197)">
                                                   <fo:inline id="5087121">
                                                      <xslt:attribute name="id">5087121</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5823197, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                                 <fo:block>
                                    <fo:inline> </fo:inline>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="rgb(0, 0, 0)"
                                          border-left-width="1pt"
                                          border-right-color="rgb(0, 0, 0)"
                                          border-right-width="1pt"
                                          border-top-color="rgb(0, 0, 0)"
                                          border-top-width="1pt"
                                          border-bottom-color="rgb(0, 0, 0)"
                                          border-bottom-width="1pt">
                              <fo:block-container height="29.8mm" overflow="hidden">
                                 <fo:block text-align="right">
                                    <fo:inline>Division: </fo:inline>
                                    <xslt:variable name="fieldValue_id6055255">
                                       <xslt:value-of select="/document/data/job/division"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6055255)">
                                       <fo:inline id="5277223">
                                          <xslt:attribute name="id">5277223</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6055255, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                 </fo:block>
                                 <fo:block text-align="right">
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/accountnumber) != ''''">
                                          <fo:block id="5058036">
                                             <xslt:attribute name="id">5058036</xslt:attribute>
                                             <fo:block text-align="right">
                                                <fo:inline>Account number: </fo:inline>
                                                <xslt:variable name="fieldValue_id6055272">
                                                   <xslt:value-of select="/document/data/job/accountnumber"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id6055272)">
                                                   <fo:inline id="4913967" font-weight="bold">
                                                      <xslt:attribute name="id">4913967</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id6055272, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:when>
                                    </xsl:choose>
                                 </fo:block>
                                 <fo:block text-align="right">
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/creditordetail/accountnumber) != ''''">
                                          <fo:block id="5501542">
                                             <xslt:attribute name="id">5501542</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>Creditor nu</fo:inline>
                                                <fo:inline text-align="right">
                                                   <fo:inline>mber: </fo:inline>
                                                </fo:inline>
                                                <fo:inline font-weight="bold">
                                                   <xslt:variable name="fieldValue_id6055296">
                                                      <xslt:value-of select="/document/data/job/creditordetail/accountnumber"/>
                                                   </xslt:variable>
                                                   <xsl:if test="string($fieldValue_id6055296)">
                                                      <fo:inline id="73B1CDC1">
                                                         <xslt:attribute name="id">73B1CDC1</xslt:attribute>
                                                         <xsl:for-each select="tokenize($fieldValue_id6055296, '' '')">
                                                            <xsl:choose>
                                                               <xsl:when test="string-length(.) &gt; 16">
                                                                  <xsl:choose>
                                                                     <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                        <xsl:value-of select="."/>
                                                                     </xsl:when>
                                                                     <xsl:otherwise>
                                                                        <xsl:call-template name="zero_width_space_1">
                                                                           <xsl:with-param name="data" select="."/>
                                                                        </xsl:call-template>
                                                                     </xsl:otherwise>
                                                                  </xsl:choose>
                                                               </xsl:when>
                                                               <xsl:otherwise>
                                                                  <xsl:value-of select="."/>
                                                               </xsl:otherwise>
                                                            </xsl:choose>
                                                            <xsl:if test="position() != last()">
                                                               <xsl:text> </xsl:text>
                                                            </xsl:if>
                                                         </xsl:for-each>
                                                      </fo:inline>
                                                   </xsl:if>
                                                </fo:inline>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:block id="4965595">
                                             <xslt:attribute name="id">4965595</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>Creditor nu</fo:inline>
                                                <fo:inline text-align="right">
                                                   <fo:inline>mber: </fo:inline>
                                                </fo:inline>
                                                <fo:inline font-weight="bold">
                                                   <xslt:variable name="fieldValue_id6055311">
                                                      <xslt:value-of select="/document/data/job/creddetails/accountnumber"/>
                                                   </xslt:variable>
                                                   <xsl:if test="string($fieldValue_id6055311)">
                                                      <fo:inline id="5A2DCDC1">
                                                         <xslt:attribute name="id">5A2DCDC1</xslt:attribute>
                                                         <xsl:for-each select="tokenize($fieldValue_id6055311, '' '')">
                                                            <xsl:choose>
                                                               <xsl:when test="string-length(.) &gt; 16">
                                                                  <xsl:choose>
                                                                     <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                        <xsl:value-of select="."/>
                                                                     </xsl:when>
                                                                     <xsl:otherwise>
                                                                        <xsl:call-template name="zero_width_space_1">
                                                                           <xsl:with-param name="data" select="."/>
                                                                        </xsl:call-template>
                                                                     </xsl:otherwise>
                                                                  </xsl:choose>
                                                               </xsl:when>
                                                               <xsl:otherwise>
                                                                  <xsl:value-of select="."/>
                                                               </xsl:otherwise>
                                                            </xsl:choose>
                                                            <xsl:if test="position() != last()">
                                                               <xsl:text> </xsl:text>
                                                            </xsl:if>
                                                         </xsl:for-each>
                                                      </fo:inline>
                                                   </xsl:if>
                                                </fo:inline>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
               </fo:block>
               <fo:block-container height="15mm"
                                   border-bottom="1px solid Black"
                                   end-indent="494.35pt"
                                   start-indent="-49.65pt">
                  <fo:block>
                     <fo:inline> </fo:inline>
                  </fo:block>
               </fo:block-container>
               <fo:block/>
               <fo:block text-align="center">
                  <fo:inline font-weight="bold" font-size="14pt">
                     <fo:inline>Compensation</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>The compensation to be paid by </fo:inline>
                  <xslt:variable name="fieldValue_id6055347">
                     <xslt:value-of select="/document/data/job/defendantname"/>
                  </xslt:variable>
                  <xsl:if test="string($fieldValue_id6055347)">
                     <fo:inline id="4671322" font-weight="bold">
                        <xslt:attribute name="id">4671322</xslt:attribute>
                        <xsl:for-each select="tokenize($fieldValue_id6055347, '' '')">
                           <xsl:choose>
                              <xsl:when test="string-length(.) &gt; 16">
                                 <xsl:choose>
                                    <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                       <xsl:value-of select="."/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       <xsl:call-template name="zero_width_space_1">
                                          <xsl:with-param name="data" select="."/>
                                       </xsl:call-template>
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:value-of select="."/>
                              </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="position() != last()">
                              <xsl:text> </xsl:text>
                           </xsl:if>
                        </xsl:for-each>
                     </fo:inline>
                  </xsl:if>
                  <fo:inline> has been transferred to this court for collection. The amount is shown below.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline font-weight="bold" font-size="12pt">
                     <fo:inline>Payments</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>Compensation is only paid to you when the money has been paid into the court by the offender. It may be paid by the offender in instalments or in a lump sum. The court will send on to you any money paid at least monthly.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>The money that you have been awarded will be sent to you from HMCTS bank account into your own. DO NOT accept any payment directly from the offender.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline font-weight="bold">
                     <fo:inline>To receive your payment(s), you MUST complete the attached BACS notification form and return it to the address printed on it.</fo:inline>
                  </fo:inline>
                  <fo:inline>  This will enable HMCTS to send your money quickly and securely direct to your bank account.  Please be assured that the details you provide on the form are strictly confidential.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>Although compensation is paid before any other penalty, there may be other people who could be paid before you or at the same time.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline font-weight="bold" font-size="12pt">
                     <fo:inline>Enquiries</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>If you have any enquiries you may contact the court using the details given at the top of this letter. Please quote the account number above.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline font-weight="bold" font-size="12pt" text-decoration="underline">
                     <fo:inline>Please keep this document for future reference regarding payment.</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell>
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell text-align="right" display-align="center">
                              <fo:block>
                                 <xslt:variable name="fieldValue_id5850514">
                                    <xslt:value-of select="/document/data/job/signature"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5850514)">
                                    <fo:inline id="4644456" font-weight="bold" font-size="12pt">
                                       <xslt:attribute name="id">4644456</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5850514, '' '')">
                                          <xsl:choose>
                                             <xsl:when test="string-length(.) &gt; 16">
                                                <xsl:choose>
                                                   <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                      <xsl:value-of select="."/>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:call-template name="zero_width_space_1">
                                                         <xsl:with-param name="data" select="."/>
                                                      </xsl:call-template>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                          <xsl:if test="position() != last()">
                                             <xsl:text> </xsl:text>
                                          </xsl:if>
                                       </xsl:for-each>
                                    </fo:inline>
                                 </xsl:if>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell>
                              <fo:block>
                                 <fo:inline>Date: </fo:inline>
                                 <xslt:variable name="fieldValue_id5850530">
                                    <xslt:value-of select="/document/data/job/dateoforder"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5850530)">
                                    <fo:inline id="5385850">
                                       <xslt:attribute name="id">5385850</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5850530, '' '')">
                                          <xsl:choose>
                                             <xsl:when test="string-length(.) &gt; 16">
                                                <xsl:choose>
                                                   <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                      <xsl:value-of select="."/>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:call-template name="zero_width_space_1">
                                                         <xsl:with-param name="data" select="."/>
                                                      </xsl:call-template>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                          <xsl:if test="position() != last()">
                                             <xsl:text> </xsl:text>
                                          </xsl:if>
                                       </xsl:for-each>
                                    </fo:inline>
                                 </xsl:if>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell text-align="right" display-align="center">
                              <fo:block>
                                 <fo:inline font-weight="bold" font-style="italic">
                                    <fo:inline>Designated Officer</fo:inline>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="center">
                  <fo:inline font-weight="bold" font-size="12pt">
                     <fo:inline>Orders</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="center">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(16)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(44)" column-number="2"/>
                     <fo:table-column column-width="proportional-column-width(20)" column-number="3"/>
                     <fo:table-column column-width="proportional-column-width(19.998)" column-number="4"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell padding-left="2pt" padding-right="2pt">
                              <fo:block>
                                 <fo:inline>Date Imposed</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell padding-left="2pt" padding-right="2pt">
                              <fo:block>
                                 <fo:inline>Offence</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell padding-left="2pt" padding-right="2pt" text-align="right">
                              <fo:block>
                                 <fo:inline>Amount Imposed</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell padding-left="2pt" padding-right="2pt" text-align="right">
                              <fo:block>
                                 <fo:inline>Balance Outstanding</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell number-columns-spanned="4">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(16)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(44)" column-number="2"/>
                     <fo:table-column column-width="proportional-column-width(20)" column-number="3"/>
                     <fo:table-column column-width="proportional-column-width(20)" column-number="4"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell padding-left="2pt" padding-right="2pt">
                              <fo:block>
                                 <xslt:variable name="fieldValue_id5825911">
                                    <xslt:value-of select="/document/data/job/offences/offence/dateimposed"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5825911)">
                                    <fo:inline id="10D0169F">
                                       <xslt:attribute name="id">10D0169F</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5825911, '' '')">
                                          <xsl:choose>
                                             <xsl:when test="string-length(.) &gt; 16">
                                                <xsl:choose>
                                                   <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                      <xsl:value-of select="."/>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:call-template name="zero_width_space_1">
                                                         <xsl:with-param name="data" select="."/>
                                                      </xsl:call-template>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                          <xsl:if test="position() != last()">
                                             <xsl:text> </xsl:text>
                                          </xsl:if>
                                       </xsl:for-each>
                                    </fo:inline>
                                 </xsl:if>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell padding-left="2pt" padding-right="2pt">
                              <fo:block>
                                 <xslt:variable name="fieldValue_id5825922">
                                    <xslt:value-of select="/document/data/job/offences/offence/offencetitle"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5825922)">
                                    <fo:inline id="1104169F">
                                       <xslt:attribute name="id">1104169F</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5825922, '' '')">
                                          <xsl:choose>
                                             <xsl:when test="string-length(.) &gt; 16">
                                                <xsl:choose>
                                                   <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                      <xsl:value-of select="."/>
                                                   </xsl:when>
                                                   <xsl:otherwise>
                                                      <xsl:call-template name="zero_width_space_1">
                                                         <xsl:with-param name="data" select="."/>
                                                      </xsl:call-template>
                                                   </xsl:otherwise>
                                                </xsl:choose>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                          <xsl:if test="position() != last()">
                                             <xsl:text> </xsl:text>
                                          </xsl:if>
                                       </xsl:for-each>
                                    </fo:inline>
                                 </xsl:if>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell number-columns-spanned="2">
                              <fo:block text-align="right">
                                 <xsl:choose>
                                    <xsl:when test="string(/document/data/job/offences/offence/impositions/imposition/amountimposed) != ''''">
                                       <fo:block id="1138169F">
                                          <xslt:attribute name="id">1138169F</xslt:attribute>
                                          <fo:block>
                                             <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
                                                <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                                                <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                                                <fo:table-body>
                                                   <fo:table-row>
                                                      <fo:table-cell padding-left="2pt" padding-right="2pt">
                                                         <fo:block>
                                                            <fo:inline>£</fo:inline>
                                                            <xslt:variable name="fieldValue_id5825962">
                                                               <xslt:value-of select="/document/data/job/offences/offence/impositions/imposition/amountimposed"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id5825962)">
                                                               <fo:inline id="1169169F">
                                                                  <xslt:attribute name="id">1169169F</xslt:attribute>
                                                                  <xsl:for-each select="tokenize($fieldValue_id5825962, '' '')">
                                                                     <xsl:choose>
                                                                        <xsl:when test="string-length(.) &gt; 16">
                                                                           <xsl:choose>
                                                                              <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                                 <xsl:value-of select="."/>
                                                                              </xsl:when>
                                                                              <xsl:otherwise>
                                                                                 <xsl:call-template name="zero_width_space_1">
                                                                                    <xsl:with-param name="data" select="."/>
                                                                                 </xsl:call-template>
                                                                              </xsl:otherwise>
                                                                           </xsl:choose>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                           <xsl:value-of select="."/>
                                                                        </xsl:otherwise>
                                                                     </xsl:choose>
                                                                     <xsl:if test="position() != last()">
                                                                        <xsl:text> </xsl:text>
                                                                     </xsl:if>
                                                                  </xsl:for-each>
                                                               </fo:inline>
                                                            </xsl:if>
                                                         </fo:block>
                                                      </fo:table-cell>
                                                      <fo:table-cell padding-left="2pt" padding-right="2pt">
                                                         <fo:block>
                                                            <fo:inline>£</fo:inline>
                                                            <xslt:variable name="fieldValue_id5825974">
                                                               <xslt:value-of select="/document/data/job/offences/offence/impositions/imposition/balance"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id5825974)">
                                                               <fo:inline id="119D169F">
                                                                  <xslt:attribute name="id">119D169F</xslt:attribute>
                                                                  <xsl:for-each select="tokenize($fieldValue_id5825974, '' '')">
                                                                     <xsl:choose>
                                                                        <xsl:when test="string-length(.) &gt; 16">
                                                                           <xsl:choose>
                                                                              <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                                 <xsl:value-of select="."/>
                                                                              </xsl:when>
                                                                              <xsl:otherwise>
                                                                                 <xsl:call-template name="zero_width_space_1">
                                                                                    <xsl:with-param name="data" select="."/>
                                                                                 </xsl:call-template>
                                                                              </xsl:otherwise>
                                                                           </xsl:choose>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                           <xsl:value-of select="."/>
                                                                        </xsl:otherwise>
                                                                     </xsl:choose>
                                                                     <xsl:if test="position() != last()">
                                                                        <xsl:text> </xsl:text>
                                                                     </xsl:if>
                                                                  </xsl:for-each>
                                                               </fo:inline>
                                                            </xsl:if>
                                                         </fo:block>
                                                      </fo:table-cell>
                                                   </fo:table-row>
                                                </fo:table-body>
                                             </fo:table>
                                          </fo:block>
                                       </fo:block>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       <fo:block id="11CE169F">
                                          <xslt:attribute name="id">11CE169F</xslt:attribute>
                                          <fo:block>
                                             <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
                                                <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                                                <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                                                <fo:table-body>
                                                   <fo:table-row>
                                                      <fo:table-cell>
                                                         <fo:block>
                                                            <fo:inline>£</fo:inline>
                                                            <xslt:variable name="fieldValue_id5826002">
                                                               <xslt:value-of select="/document/data/job/offences/offence/amountimposed"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id5826002)">
                                                               <fo:inline id="1203169F">
                                                                  <xslt:attribute name="id">1203169F</xslt:attribute>
                                                                  <xsl:for-each select="tokenize($fieldValue_id5826002, '' '')">
                                                                     <xsl:choose>
                                                                        <xsl:when test="string-length(.) &gt; 16">
                                                                           <xsl:choose>
                                                                              <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                                 <xsl:value-of select="."/>
                                                                              </xsl:when>
                                                                              <xsl:otherwise>
                                                                                 <xsl:call-template name="zero_width_space_1">
                                                                                    <xsl:with-param name="data" select="."/>
                                                                                 </xsl:call-template>
                                                                              </xsl:otherwise>
                                                                           </xsl:choose>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                           <xsl:value-of select="."/>
                                                                        </xsl:otherwise>
                                                                     </xsl:choose>
                                                                     <xsl:if test="position() != last()">
                                                                        <xsl:text> </xsl:text>
                                                                     </xsl:if>
                                                                  </xsl:for-each>
                                                               </fo:inline>
                                                            </xsl:if>
                                                         </fo:block>
                                                      </fo:table-cell>
                                                      <fo:table-cell>
                                                         <fo:block>
                                                            <fo:inline>£</fo:inline>
                                                            <xslt:variable name="fieldValue_id5826012">
                                                               <xslt:value-of select="/document/data/job/offences/offence/balanceoutstanding"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id5826012)">
                                                               <fo:inline id="1237169F">
                                                                  <xslt:attribute name="id">1237169F</xslt:attribute>
                                                                  <xsl:for-each select="tokenize($fieldValue_id5826012, '' '')">
                                                                     <xsl:choose>
                                                                        <xsl:when test="string-length(.) &gt; 16">
                                                                           <xsl:choose>
                                                                              <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                                 <xsl:value-of select="."/>
                                                                              </xsl:when>
                                                                              <xsl:otherwise>
                                                                                 <xsl:call-template name="zero_width_space_1">
                                                                                    <xsl:with-param name="data" select="."/>
                                                                                 </xsl:call-template>
                                                                              </xsl:otherwise>
                                                                           </xsl:choose>
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                           <xsl:value-of select="."/>
                                                                        </xsl:otherwise>
                                                                     </xsl:choose>
                                                                     <xsl:if test="position() != last()">
                                                                        <xsl:text> </xsl:text>
                                                                     </xsl:if>
                                                                  </xsl:for-each>
                                                               </fo:inline>
                                                            </xsl:if>
                                                         </fo:block>
                                                      </fo:table-cell>
                                                   </fo:table-row>
                                                </fo:table-body>
                                             </fo:table>
                                          </fo:block>
                                       </fo:block>
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
               </fo:block>
               <fo:block text-align="left" break-before="page">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left" break-before="page">
                  <fo:block-container height="25.52mm" overflow="hidden">
                     <fo:block>
                        <fo:inline> </fo:inline>
                     </fo:block>
                  </fo:block-container>
                  <fo:table table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell>
                              <fo:block-container height="4.2mm" overflow="hidden">
                                 <fo:block>
                                    <fo:inline>To: </fo:inline>
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/enfaauaddress/name) != ''''">
                                          <fo:inline id="4723756">
                                             <xslt:attribute name="id">4723756</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5851438">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/name"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851438)">
                                                   <fo:inline id="4872953">
                                                      <xslt:attribute name="id">4872953</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851438, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:inline>
                                          </fo:inline>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:inline id="4952101">
                                             <xslt:attribute name="id">4952101</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5851446">
                                                   <xslt:value-of select="/document/data/job/aauaddress/name"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851446)">
                                                   <fo:inline id="4874524">
                                                      <xslt:attribute name="id">4874524</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851446, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:inline>
                                          </fo:inline>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
                  <fo:table table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(50)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(50)" column-number="2"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell>
                              <fo:block-container height="29.8mm" overflow="hidden">
                                 <fo:block>
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/enfaauaddress/address) != ''''">
                                          <fo:block id="4692687">
                                             <xslt:attribute name="id">4692687</xslt:attribute>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851488">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/address/addr1"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851488)">
                                                   <fo:inline id="57A5CE5D">
                                                      <xslt:attribute name="id">57A5CE5D</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851488, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851494">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/address/addr2"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851494)">
                                                   <fo:inline id="1FE2CE5D">
                                                      <xslt:attribute name="id">1FE2CE5D</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851494, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851501">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/address/addr3"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851501)">
                                                   <fo:inline id="61C1CE5D">
                                                      <xslt:attribute name="id">61C1CE5D</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851501, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851507">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/address/addr4"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851507)">
                                                   <fo:inline id="289ACE5D">
                                                      <xslt:attribute name="id">289ACE5D</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851507, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851514">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/address/addr5"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851514)">
                                                   <fo:inline id="3175CE5D">
                                                      <xslt:attribute name="id">3175CE5D</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851514, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851520">
                                                   <xslt:value-of select="/document/data/job/enfaauaddress/address/postcode"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851520)">
                                                   <fo:inline id="201BCE5E">
                                                      <xslt:attribute name="id">201BCE5E</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851520, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:block id="5041333">
                                             <xslt:attribute name="id">5041333</xslt:attribute>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851528">
                                                   <xslt:value-of select="/document/data/job/aauaddress/address/addr1"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851528)">
                                                   <fo:inline id="7CBECE5F">
                                                      <xslt:attribute name="id">7CBECE5F</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851528, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851535">
                                                   <xslt:value-of select="/document/data/job/aauaddress/address/addr2"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851535)">
                                                   <fo:inline id="F0CCE5FD">
                                                      <xslt:attribute name="id">F0CCE5FD</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851535, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851541">
                                                   <xslt:value-of select="/document/data/job/aauaddress/address/addr3"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851541)">
                                                   <fo:inline id="1834CE5F">
                                                      <xslt:attribute name="id">1834CE5F</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851541, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851548">
                                                   <xslt:value-of select="/document/data/job/aauaddress/address/addr4"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851548)">
                                                   <fo:inline id="1E61CE5F">
                                                      <xslt:attribute name="id">1E61CE5F</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851548, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851554">
                                                   <xslt:value-of select="/document/data/job/aauaddress/address/addr5"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851554)">
                                                   <fo:inline id="25F1CE5F">
                                                      <xslt:attribute name="id">25F1CE5F</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851554, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                             <fo:block>
                                                <xslt:variable name="fieldValue_id5851560">
                                                   <xslt:value-of select="/document/data/job/aauaddress/address/postcode"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851560)">
                                                   <fo:inline id="2C4FCE5F">
                                                      <xslt:attribute name="id">2C4FCE5F</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851560, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                           <fo:table-cell text-align="right">
                              <fo:block-container height="29.8mm" overflow="hidden">
                                 <fo:block>
                                    <fo:inline>Account number: </fo:inline>
                                    <xslt:variable name="fieldValue_id5851578">
                                       <xslt:value-of select="/document/data/job/accountnumber"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5851578)">
                                       <fo:inline id="4848696" font-weight="bold">
                                          <xslt:attribute name="id">4848696</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5851578, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                 </fo:block>
                                 <fo:block>
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/creditordetail/accountnumber) != ''''">
                                          <fo:block id="3AA3CDCF">
                                             <xslt:attribute name="id">3AA3CDCF</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>Creditor n</fo:inline>
                                                <fo:inline text-align="right">
                                                   <fo:inline>umber: </fo:inline>
                                                </fo:inline>
                                                <fo:inline font-weight="bold">
                                                   <xslt:variable name="fieldValue_id5851599">
                                                      <xslt:value-of select="/document/data/job/creditordetail/accountnumber"/>
                                                   </xslt:variable>
                                                   <xsl:if test="string($fieldValue_id5851599)">
                                                      <fo:inline id="3AD7CDCF">
                                                         <xslt:attribute name="id">3AD7CDCF</xslt:attribute>
                                                         <xsl:for-each select="tokenize($fieldValue_id5851599, '' '')">
                                                            <xsl:choose>
                                                               <xsl:when test="string-length(.) &gt; 16">
                                                                  <xsl:choose>
                                                                     <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                        <xsl:value-of select="."/>
                                                                     </xsl:when>
                                                                     <xsl:otherwise>
                                                                        <xsl:call-template name="zero_width_space_1">
                                                                           <xsl:with-param name="data" select="."/>
                                                                        </xsl:call-template>
                                                                     </xsl:otherwise>
                                                                  </xsl:choose>
                                                               </xsl:when>
                                                               <xsl:otherwise>
                                                                  <xsl:value-of select="."/>
                                                               </xsl:otherwise>
                                                            </xsl:choose>
                                                            <xsl:if test="position() != last()">
                                                               <xsl:text> </xsl:text>
                                                            </xsl:if>
                                                         </xsl:for-each>
                                                      </fo:inline>
                                                   </xsl:if>
                                                </fo:inline>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:block id="3B08CDCF">
                                             <xslt:attribute name="id">3B08CDCF</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>Creditor nu</fo:inline>
                                                <fo:inline text-align="right">
                                                   <fo:inline>mber: </fo:inline>
                                                </fo:inline>
                                                <fo:inline font-weight="bold">
                                                   <xslt:variable name="fieldValue_id5851614">
                                                      <xslt:value-of select="/document/data/job/creddetails/accountnumber"/>
                                                   </xslt:variable>
                                                   <xsl:if test="string($fieldValue_id5851614)">
                                                      <fo:inline id="3B3DCDCF">
                                                         <xslt:attribute name="id">3B3DCDCF</xslt:attribute>
                                                         <xsl:for-each select="tokenize($fieldValue_id5851614, '' '')">
                                                            <xsl:choose>
                                                               <xsl:when test="string-length(.) &gt; 16">
                                                                  <xsl:choose>
                                                                     <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                        <xsl:value-of select="."/>
                                                                     </xsl:when>
                                                                     <xsl:otherwise>
                                                                        <xsl:call-template name="zero_width_space_1">
                                                                           <xsl:with-param name="data" select="."/>
                                                                        </xsl:call-template>
                                                                     </xsl:otherwise>
                                                                  </xsl:choose>
                                                               </xsl:when>
                                                               <xsl:otherwise>
                                                                  <xsl:value-of select="."/>
                                                               </xsl:otherwise>
                                                            </xsl:choose>
                                                            <xsl:if test="position() != last()">
                                                               <xsl:text> </xsl:text>
                                                            </xsl:if>
                                                         </xsl:for-each>
                                                      </fo:inline>
                                                   </xsl:if>
                                                </fo:inline>
                                             </fo:block>
                                          </fo:block>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                                 <fo:block>
                                    <fo:inline>Minor creditor name: </fo:inline>
                                    <xsl:choose>
                                       <xsl:when test="string(/document/data/job/creditordetail/creditoraddress/name) != ''''">
                                          <fo:inline id="614FCDD0">
                                             <xslt:attribute name="id">614FCDD0</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5851630">
                                                   <xslt:value-of select="/document/data/job/creditordetail/creditoraddress/name"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851630)">
                                                   <fo:inline id="614FCDD0" font-weight="bold">
                                                      <xslt:attribute name="id">614FCDD0_id5851630</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851630, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:inline>
                                          </fo:inline>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <fo:inline id="6180CDD0">
                                             <xslt:attribute name="id">6180CDD0</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5851639">
                                                   <xslt:value-of select="/document/data/job/creddetails/creditoraddress/name"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5851639)">
                                                   <fo:inline id="61B5CDD0" font-weight="bold">
                                                      <xslt:attribute name="id">61B5CDD0</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5851639, '' '')">
                                                         <xsl:choose>
                                                            <xsl:when test="string-length(.) &gt; 16">
                                                               <xsl:choose>
                                                                  <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                                     <xsl:value-of select="."/>
                                                                  </xsl:when>
                                                                  <xsl:otherwise>
                                                                     <xsl:call-template name="zero_width_space_1">
                                                                        <xsl:with-param name="data" select="."/>
                                                                     </xsl:call-template>
                                                                  </xsl:otherwise>
                                                               </xsl:choose>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                               <xsl:value-of select="."/>
                                                            </xsl:otherwise>
                                                         </xsl:choose>
                                                         <xsl:if test="position() != last()">
                                                            <xsl:text> </xsl:text>
                                                         </xsl:if>
                                                      </xsl:for-each>
                                                   </fo:inline>
                                                </xsl:if>
                                             </fo:inline>
                                          </fo:inline>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
                  <fo:block-container height="15mm"
                                      border-bottom="1px solid Black"
                                      end-indent="494.35pt"
                                      start-indent="-49.65pt">
                     <fo:block>
                        <fo:inline> </fo:inline>
                     </fo:block>
                  </fo:block-container>
               </fo:block>
               <fo:block text-align="center">
                  <fo:inline font-weight="bold" font-size="14pt">
                     <fo:inline>BACS notification form</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="center">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>I have received your notice about compensation due to me from </fo:inline>
                  <xslt:variable name="fieldValue_id5851676">
                     <xslt:value-of select="/document/data/job/defendantname"/>
                  </xslt:variable>
                  <xsl:if test="string($fieldValue_id5851676)">
                     <fo:inline id="4756453" font-weight="bold">
                        <xslt:attribute name="id">4756453</xslt:attribute>
                        <xsl:for-each select="tokenize($fieldValue_id5851676, '' '')">
                           <xsl:choose>
                              <xsl:when test="string-length(.) &gt; 16">
                                 <xsl:choose>
                                    <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                       <xsl:value-of select="."/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       <xsl:call-template name="zero_width_space_1">
                                          <xsl:with-param name="data" select="."/>
                                       </xsl:call-template>
                                    </xsl:otherwise>
                                 </xsl:choose>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:value-of select="."/>
                              </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="position() != last()">
                              <xsl:text> </xsl:text>
                           </xsl:if>
                        </xsl:for-each>
                     </fo:inline>
                  </xsl:if>
                  <fo:inline>.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>I want the court to make payments of any monies received directly into my bank account.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>My bank details are as follows:</fo:inline>
               </fo:block>
               <fo:block text-align="left" border="1px solid Black" border-style="none">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(26.476)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(73.524)" column-number="2"/>
                     <fo:table-body>
                        <fo:table-row height="0.82cm">
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline>Bank Name</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row height="0.82cm">
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline>Branch</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline>Sort Code</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="1"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="2"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="3"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="4"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="5"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="6"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="7"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="8"/>
                                    <fo:table-body>
                                       <fo:table-row>
                                          <fo:table-cell>
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black"
                                                                        padding="2pt"
                                                                        text-align="center"
                                                                        display-align="center">
                                                            <fo:block>
                                                               <fo:inline>_</fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black"
                                                                        padding="2pt"
                                                                        text-align="center"
                                                                        display-align="center">
                                                            <fo:block>
                                                               <fo:inline>_</fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                       </fo:table-row>
                                    </fo:table-body>
                                 </fo:table>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline>Bank Account Number</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="1"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="2"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="3"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="4"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="5"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="6"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="7"/>
                                    <fo:table-column column-width="proportional-column-width(12.5)" column-number="8"/>
                                    <fo:table-body>
                                       <fo:table-row>
                                          <fo:table-cell>
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                          <fo:table-cell padding-left="2pt">
                                             <fo:block>
                                                <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                   <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
                                                   <fo:table-body>
                                                      <fo:table-row height="23.76pt">
                                                         <fo:table-cell border="1pt solid black" padding="2pt">
                                                            <fo:block>
                                                               <fo:inline> </fo:inline>
                                                            </fo:block>
                                                         </fo:table-cell>
                                                      </fo:table-row>
                                                   </fo:table-body>
                                                </fo:table>
                                             </fo:block>
                                          </fo:table-cell>
                                       </fo:table-row>
                                    </fo:table-body>
                                 </fo:table>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row height="0.82cm">
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline>Account Holders’ Name(s)</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row height="0.82cm">
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline>Reference</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row height="0.82cm">
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline font-weight="bold">
                                    <fo:inline>Required Signature(s):</fo:inline>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline font-weight="bold" font-style="italic">
                                    <fo:inline>For joint accounts where both account holders must sign, both must sign here.</fo:inline>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                              <fo:block>
                                 <fo:inline>Account Holder 1 Signature</fo:inline>
                              </fo:block>
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell border="1pt solid black" padding="2pt" display-align="center">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                              <fo:block>
                                 <fo:inline>Account Holder 2 Signature</fo:inline>
                              </fo:block>
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border="1pt solid black" padding="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline font-weight="bold">
                     <fo:inline>Please note: </fo:inline>
                  </fo:inline>
                  <fo:inline> If you do not have a bank account, you can arrange for your compensation to be paid into a third party bank account, by entering the details on the above mandate.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>For information on how HMCTS uses personal data about you, please see:  https://www.gov.uk/government/organisations/hm-courts-and-tribunals-service/about/personal-information-charter</fo:inline>
               </fo:block>
            </fo:flow>
         </fo:page-sequence>
      </fo:root>
   </xsl:template>
   <xsl:template name="_component_C__Users_d331153_git_app_templatetransformation_ecrion-docs_Headers_Account_epb">
      <fo:block>
         <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
            <fo:table-column column-width="proportional-column-width(31.653)" column-number="1"/>
            <fo:table-column column-width="proportional-column-width(68.347)" column-number="2"/>
            <fo:table-body>
               <fo:table-row>
                  <fo:table-cell border-bottom="1pt solid rgb(0, 0, 0)">
                     <fo:block>
                        <external-graphic xmlns="http://www.w3.org/1999/XSL/Format"
                                          xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                          xmlns:rss="http://www.justice.gov.uk/magistrates/dmu/ResultsResponse"
                                          xmlns:tns="http://www.justice.gov.uk/magistrates/dmu/CSCI"
                                          content-height="30mm"
                                          src="crest.tif"/>
                     </fo:block>
                  </fo:table-cell>
                  <fo:table-cell border-bottom="1pt solid rgb(0, 0, 0)">
                     <fo:block text-align="right">
                        <fo:inline font-size="12pt">
                           <xslt:variable name="fieldValue_id6109910">
                              <xslt:value-of select="/document/data/job/header/courtname"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6109910)">
                              <fo:inline id="4832663">
                                 <xslt:attribute name="id">4832663</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6109910, '' '')">
                                    <xsl:choose>
                                       <xsl:when test="string-length(.) &gt; 16">
                                          <xsl:choose>
                                             <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                <xsl:value-of select="."/>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:call-template name="zero_width_space_1">
                                                   <xsl:with-param name="data" select="."/>
                                                </xsl:call-template>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:value-of select="."/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:if test="position() != last()">
                                       <xsl:text> </xsl:text>
                                    </xsl:if>
                                 </xsl:for-each>
                              </fo:inline>
                           </xsl:if>
                        </fo:inline>
                     </fo:block>
                     <fo:block text-align="right">
                        <fo:inline font-size="9pt">
                           <xslt:variable name="fieldValue_id6109919">
                              <xslt:value-of select="/document/data/job/header/line1"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6109919)">
                              <fo:inline id="5374330">
                                 <xslt:attribute name="id">5374330</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6109919, '' '')">
                                    <xsl:choose>
                                       <xsl:when test="string-length(.) &gt; 16">
                                          <xsl:choose>
                                             <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                <xsl:value-of select="."/>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:call-template name="zero_width_space_1">
                                                   <xsl:with-param name="data" select="."/>
                                                </xsl:call-template>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:value-of select="."/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:if test="position() != last()">
                                       <xsl:text> </xsl:text>
                                    </xsl:if>
                                 </xsl:for-each>
                              </fo:inline>
                           </xsl:if>
                        </fo:inline>
                     </fo:block>
                     <fo:block text-align="right">
                        <fo:inline font-size="9pt">
                           <xslt:variable name="fieldValue_id6109929">
                              <xslt:value-of select="/document/data/job/header/line2"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6109929)">
                              <fo:inline id="5693130">
                                 <xslt:attribute name="id">5693130</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6109929, '' '')">
                                    <xsl:choose>
                                       <xsl:when test="string-length(.) &gt; 16">
                                          <xsl:choose>
                                             <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                <xsl:value-of select="."/>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:call-template name="zero_width_space_1">
                                                   <xsl:with-param name="data" select="."/>
                                                </xsl:call-template>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:value-of select="."/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:if test="position() != last()">
                                       <xsl:text> </xsl:text>
                                    </xsl:if>
                                 </xsl:for-each>
                              </fo:inline>
                           </xsl:if>
                        </fo:inline>
                     </fo:block>
                     <fo:block text-align="right">
                        <fo:inline font-size="9pt">
                           <xslt:variable name="fieldValue_id6109938">
                              <xslt:value-of select="/document/data/job/header/line3"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6109938)">
                              <fo:inline id="5099172">
                                 <xslt:attribute name="id">5099172</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6109938, '' '')">
                                    <xsl:choose>
                                       <xsl:when test="string-length(.) &gt; 16">
                                          <xsl:choose>
                                             <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                <xsl:value-of select="."/>
                                             </xsl:when>
                                             <xsl:otherwise>
                                                <xsl:call-template name="zero_width_space_1">
                                                   <xsl:with-param name="data" select="."/>
                                                </xsl:call-template>
                                             </xsl:otherwise>
                                          </xsl:choose>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:value-of select="."/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:if test="position() != last()">
                                       <xsl:text> </xsl:text>
                                    </xsl:if>
                                 </xsl:for-each>
                              </fo:inline>
                           </xsl:if>
                        </fo:inline>
                     </fo:block>
                  </fo:table-cell>
               </fo:table-row>
            </fo:table-body>
         </fo:table>
         <fo:inline> </fo:inline>
      </fo:block>
   </xsl:template>
   <xsl:template name="_component_C__Users_d331153_git_app_templatetransformation_ecrion-docs_Headers_ContinuationAAU_epb">
      <fo:block>
         <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
            <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
            <fo:table-body>
               <fo:table-row>
                  <fo:table-cell text-align="right" border-bottom="1pt solid rgb(0, 0, 0)">
                     <fo:block>
                        <xsl:choose>
                           <xsl:when test="string(/document/data/job/header/ljaname) != ''''">
                              <fo:inline id="5CEB1493">
                                 <xslt:attribute name="id">5CEB1493</xslt:attribute>
                                 <fo:inline>
                                    <xslt:variable name="fieldValue_id6109851">
                                       <xslt:value-of select="/document/data/job/header/ljaname"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6109851)">
                                       <fo:inline id="5D1F1493" font-size="12pt">
                                          <xslt:attribute name="id">5D1F1493</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6109851, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                 </fo:inline>
                              </fo:inline>
                           </xsl:when>
                           <xsl:otherwise>
                              <fo:inline id="5D501493">
                                 <xslt:attribute name="id">5D501493</xslt:attribute>
                                 <fo:inline>
                                    <xslt:variable name="fieldValue_id6109816">
                                       <xslt:value-of select="/document/data/job/header/courtname"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6109816)">
                                       <fo:inline id="5D841493" font-size="12pt">
                                          <xslt:attribute name="id">5D841493</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6109816, '' '')">
                                             <xsl:choose>
                                                <xsl:when test="string-length(.) &gt; 16">
                                                   <xsl:choose>
                                                      <xsl:when test="not(string-length(.) = string-length(translate(., '' &#x9;&#xA;&#xD;'','''')))">
                                                         <xsl:value-of select="."/>
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         <xsl:call-template name="zero_width_space_1">
                                                            <xsl:with-param name="data" select="."/>
                                                         </xsl:call-template>
                                                      </xsl:otherwise>
                                                   </xsl:choose>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                   <xsl:value-of select="."/>
                                                </xsl:otherwise>
                                             </xsl:choose>
                                             <xsl:if test="position() != last()">
                                                <xsl:text> </xsl:text>
                                             </xsl:if>
                                          </xsl:for-each>
                                       </fo:inline>
                                    </xsl:if>
                                 </fo:inline>
                              </fo:inline>
                           </xsl:otherwise>
                        </xsl:choose>
                        <fo:inline> </fo:inline>
                     </fo:block>
                  </fo:table-cell>
               </fo:table-row>
            </fo:table-body>
         </fo:table>
         <fo:inline> </fo:inline>
      </fo:block>
   </xsl:template>
</xslt:stylesheet>
'
,null
,'COMPLETT-46_1-postscript.xsl'
);

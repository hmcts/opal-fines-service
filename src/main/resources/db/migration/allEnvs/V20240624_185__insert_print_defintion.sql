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
* 09/03/2024    A Dennis    1.0         PO-208 Inserts rows of data into the PRINT_DEFINITION table
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
 500000000
,'ABD'
,'Application for benefits deductions'
,'PORTAL'
,null
,null
,'PDF'
,'ON'
,7
,'LIBRA'
,null
,'25_0'
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
   <xslt:param name="XFCrtLocalDate">2019-05-31</xslt:param>
   <xslt:param name="XFCrtLocalTime">12:32:26</xslt:param>
   <xslt:param name="XFCrtLocalDateTime">2019-05-31T12:32:26</xslt:param>
   <xslt:param name="XFCrtUTCDate">2019-05-31</xslt:param>
   <xslt:param name="XFCrtUTCTime">11:32:26</xslt:param>
   <xslt:param name="XFCrtUTCDateTime">2019-05-31T11:32:26</xslt:param>
   <xslt:param name="XFOutputFormat"/>
   <xslt:param name="XFTemplateName">ABD-25_0.epr</xslt:param>
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
         <xfd:schema src="data:;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjx4czpzY2hlbWEgYXR0cmlidXRlRm9ybURlZmF1bHQ9InVucXVhbGlmaWVkIiBlbGVtZW50Rm9ybURlZmF1bHQ9InF1YWxpZmllZCIgeG1sbnM6eHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hIj4NCiAgPHhzOmVsZW1lbnQgbmFtZT0iZG9jdW1lbnQiPg0KICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iaW5mbyI+DQogICAgICAgICAgPHhzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJnZW5lcmFsIj4NCiAgICAgICAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InZlcnNpb24iIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZG9jcmVmIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkYXRhIj4NCiAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImpvYiI+DQogICAgICAgICAgICAgICAgPHhzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJoZWFkZXIiPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iY291cnRuYW1lIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImNvZGUiIHR5cGU9InhzOnVuc2lnbmVkU2hvcnQiIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpbmUxIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpbmUyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpbmUzIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZGl2aXNpb24iIHR5cGU9InhzOnVuc2lnbmVkQnl0ZSIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iYWNjb3VudG51bWJlciIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJjYXNlbnVtYmVyIiB0eXBlPSJ4czp1bnNpZ25lZExvbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImRvYiIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkZWZlbmRhbnRuYW1lIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InNleCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJzY2hlbWEiPg0KICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGlicmEiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibWV0IiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZGVmZW5kYW50YWRkcmVzcyI+DQogICAgICAgICAgICAgICAgICAgICAgPHhzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJuYW1lIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImFkZHJlc3MiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTEiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTIiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTMiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0icG9zdGNvZGUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJjb21wYW55IiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InRlbGVwaG9uZSIgdHlwZT0ieHM6dW5zaWduZWRJbnQiIC8+DQogICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iYW1vdW50b3V0c3RhbmRpbmciIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZGVmZW5kYW50aW5kZWZhdWx0IiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im5pbnVtYmVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImR3cGFwbnVtYmVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im9mZmVuY2VzIj4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im9mZmVuY2UiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmNob2ljZSBtYXhPY2N1cnM9InVuYm91bmRlZCI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZGF0ZWltcG9zZWQiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJjYXNlbnVtYmVyIiB0eXBlPSJ4czp1bnNpZ25lZEludCIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJvZmZlbmNlY29kZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9Im9mZmVuY2V0aXRsZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InRpY2tldG51bWJlciIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImN0b25hbWUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJ2ZWhpY2xlcmVnIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0idGltZW9mb2ZmZW5jZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InBsYWNlb2ZvZmZlbmNlIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibnRvbnRoIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZGF0ZWlzc3VlZCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImxpY2VuY2VubyIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImltcG9zaXRpb25zIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG1heE9jY3Vycz0idW5ib3VuZGVkIiBuYW1lPSJpbXBvc2l0aW9uIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImltcG9zaXRpb25jb2RlIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iaW1wb3NpdGlvbnR5cGUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJpbXBvc2l0aW9udGV4dCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImFtb3VudGltcG9zZWQiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJwYWlkIiB0eXBlPSJ4czpkZWNpbWFsIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImJhbGFuY2UiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJjcmVkaXRvcm5hbWUiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ib2ZmZW5jZXRvdGFsIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6Y2hvaWNlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImFjY291bnR0b3RhbCIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJhY2NvdW50cGFpZCIgdHlwZT0ieHM6ZGVjaW1hbCIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJkYXRlcHJvZHVjZWQiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ic2Vzc2lvbl9pZCIgdHlwZT0ieHM6dW5zaWduZWRJbnQiIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InJlZ2lzdGVydmFsaWRhdGVkIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImRhdGVvZm9yZGVyIiB0eXBlPSJ4czpzdHJpbmciIC8+DQogICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9InNpZ25hdHVyZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJlbmRfdGltZSIgdHlwZT0ieHM6dGltZSIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iZWxhcHNlZHNlY3MiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0iam9iY2VudHJlbmFtZSIgdHlwZT0ieHM6c3RyaW5nIiAvPg0KICAgICAgICAgICAgICAgICAgICA8eHM6ZWxlbWVudCBuYW1lPSJqb2JjZW50cmVhZGRyZXNzIj4NCiAgICAgICAgICAgICAgICAgICAgICA8eHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgICAgICAgICAgICA8eHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICAgIDx4czplbGVtZW50IG5hbWU9ImFkZHJlc3MiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDx4czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTEiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTIiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPHhzOmVsZW1lbnQgbmFtZT0ibGluZTMiIHR5cGU9InhzOnN0cmluZyIgLz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgICAgICAgICAgICAgICAgICAgPC94czpzZXF1ZW5jZT4NCiAgICAgICAgICAgICAgICAgICAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICAgICAgICAgICAgICAgICAgICA8L3hzOmVsZW1lbnQ+DQogICAgICAgICAgICAgICAgICA8L3hzOnNlcXVlbmNlPg0KICAgICAgICAgICAgICAgIDwveHM6Y29tcGxleFR5cGU+DQogICAgICAgICAgICAgIDwveHM6ZWxlbWVudD4NCiAgICAgICAgICAgIDwveHM6c2VxdWVuY2U+DQogICAgICAgICAgPC94czpjb21wbGV4VHlwZT4NCiAgICAgICAgPC94czplbGVtZW50Pg0KICAgICAgPC94czpzZXF1ZW5jZT4NCiAgICA8L3hzOmNvbXBsZXhUeXBlPg0KICA8L3hzOmVsZW1lbnQ+DQo8L3hzOnNjaGVtYT4="/>
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
            <fo:page-sequence-master master-name="ComplexMaster1">
               <fo:repeatable-page-master-alternatives maximum-repeats="no-limit">
                  <fo:conditional-page-master-reference master-reference="first" page-position="first"/>
                  <fo:conditional-page-master-reference master-reference="other"/>
               </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>
         </fo:layout-master-set>
         <fo:page-sequence master-reference="ComplexMaster1" language="en">
            <fo:static-content flow-name="xsl-region-before">
               <fo:block>
                  <xslt:text>				</xslt:text>
                  <xslt:call-template name="_component_C__development_selfservice_trunk_app_templatetransformation_ecrion-docs_Headers_Enforcement_epb"/>
                  <xslt:text>			</xslt:text>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="xsl-region-after">
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
                                    <xslt:text>										</xslt:text>
                                    <xslt:variable name="fieldValue_id6851634">
                                       <xslt:value-of select="printRequest/defendantName"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6851634)">
                                       <fo:inline id="18A66140">
                                          <xslt:attribute name="id">18A66140</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6851634, '' '')">
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
                                    <xslt:text>									</xslt:text>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell text-align="right">
                              <fo:block font-size="8pt">
                                 <xslt:variable name="fieldValue_id6851647">
                                    <xslt:value-of select="printRequest/dateProduced"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id6851647)">
                                    <fo:inline id="18DA6140" font-size="8pt">
                                       <xslt:attribute name="id">18DA6140</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id6851647, '' '')">
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
                                    <fo:inline>/ABD_</fo:inline>
                                    <xslt:variable name="fieldValue_id6851655">
                                       <xslt:value-of select="printRequest/document/info/general/version"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6851655)">
                                       <fo:inline id="190F6140">
                                          <xslt:attribute name="id">190F6140</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6851655, '' '')">
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
                                    <xslt:variable name="fieldValue_id6851661">
                                       <xslt:value-of select="printRequest/document/info/general/docref"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6851661)">
                                       <fo:inline id="19406140">
                                          <xslt:attribute name="id">19406140</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6851661, '' '')">
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
                  <xslt:text>				</xslt:text>
                  <xslt:call-template name="_component_C__development_selfservice_trunk_app_templatetransformation_ecrion-docs_Headers_Continuation_epb"/>
                  <xslt:text>			</xslt:text>
               </fo:block>
            </fo:static-content>
            <fo:static-content flow-name="other-region-after">
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
                                    <xslt:text>										</xslt:text>
                                    <xslt:variable name="fieldValue_id6851736">
                                       <xslt:value-of select="printRequest/defendantName"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6851736)">
                                       <fo:inline id="F0261412">
                                          <xslt:attribute name="id">F0261412</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6851736, '' '')">
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
                                    <xslt:text>									</xslt:text>
                                 </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell text-align="right">
                              <fo:block font-size="8pt">
                                 <xslt:variable name="fieldValue_id6851749">
                                    <xslt:value-of select="printRequest/dateProduced"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id6851749)">
                                    <fo:inline id="F3661412" font-size="8pt">
                                       <xslt:attribute name="id">F3661412</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id6851749, '' '')">
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
                                    <fo:inline>/ABD_</fo:inline>
                                    <xslt:variable name="fieldValue_id5893871">
                                       <xslt:value-of select="printRequest/document/info/general/version"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893871)">
                                       <fo:inline id="F6B61412">
                                          <xslt:attribute name="id">F6B61412</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893871, '' '')">
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
                                    <xslt:variable name="fieldValue_id5893877">
                                       <xslt:value-of select="printRequest/document/info/general/docref"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893877)">
                                       <fo:inline id="F9C61412">
                                          <xslt:attribute name="id">F9C61412</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893877, '' '')">
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
               <fo:block>
                  <fo:inline> </fo:inline>
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
            <fo:flow flow-name="xsl-region-body">
               <fo:block>
                  <xslt:text>				</xslt:text>
                  <fo:table width="auto"
                            border-collapse="collapse"
                            table-layout="fixed"
                            margin-top="0pt">
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
                                    <xslt:variable name="fieldValue_id5893952">
                                       <xslt:value-of select="printRequest/jobcentrename"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893952)">
                                       <fo:inline id="5650135">
                                          <xslt:attribute name="id">5650135</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893952, '' '')">
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
                                    <xslt:variable name="fieldValue_id5893979">
                                       <xslt:value-of select="printRequest/jobcentreaddress/address/line1"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893979)">
                                       <fo:inline id="4983354">
                                          <xslt:attribute name="id">4983354</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893979, '' '')">
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
                                    <xslt:variable name="fieldValue_id5893985">
                                       <xslt:value-of select="printRequest/jobcentreaddress/address/line2"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893985)">
                                       <fo:inline id="4962712">
                                          <xslt:attribute name="id">4962712</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893985, '' '')">
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
                                    <xslt:variable name="fieldValue_id5893992">
                                       <xslt:value-of select="printRequest/jobcentreaddress/address/line3"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893992)">
                                       <fo:inline id="B0373099">
                                          <xslt:attribute name="id">B0373099</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893992, '' '')">
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
                                    <xslt:variable name="fieldValue_id5893998">
                                       <xslt:value-of select="printRequest/jobcentreaddress/address/line4"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5893998)">
                                       <fo:inline id="23B27309">
                                          <xslt:attribute name="id">23B27309</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5893998, '' '')">
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
                                    <xslt:variable name="fieldValue_id5921910">
                                       <xslt:value-of select="printRequest/jobcentreaddress/address/line5"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5921910)">
                                       <fo:inline id="316C7309">
                                          <xslt:attribute name="id">316C7309</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5921910, '' '')">
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
                                       <xsl:when test="string(/jobcentreaddress/address/line6) != ''''">
                                          <fo:inline>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5921921">
                                                   <xslt:value-of select="printRequest/jobcentreaddress/address/line6"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5921921)">
                                                   <fo:inline id="39627309">
                                                      <xslt:attribute name="id">39627309</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5921921, '' '')">
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
                                          <fo:inline>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id5921928">
                                                   <xslt:value-of select="printRequest/jobcentreaddress/address/postcode"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5921928)">
                                                   <fo:inline id="1E19730A">
                                                      <xslt:attribute name="id">1E19730A</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5921928, '' '')">
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
                                    <xslt:variable name="fieldValue_id5921954">
                                       <xslt:value-of select="printRequest/division"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id5921954)">
                                       <fo:inline id="4804264">
                                          <xslt:attribute name="id">4804264</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id5921954, '' '')">
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
                                       <xsl:when test="string(/accountnumber) != ''''">
                                          <fo:block id="5096428">
                                             <xslt:attribute name="id">5096428</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>Account number: </fo:inline>
                                                <xslt:variable name="fieldValue_id5921970">
                                                   <xslt:value-of select="printRequest/accountNumber"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5921970)">
                                                   <fo:inline id="5557044" font-weight="bold">
                                                      <xslt:attribute name="id">5557044</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5921970, '' '')">
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
                                       <xsl:when test="string(/casenumber) != ''''">
                                          <fo:block id="5671884">
                                             <xslt:attribute name="id">5671884</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>Case number: </fo:inline>
                                                <xslt:variable name="fieldValue_id5921989">
                                                   <xslt:value-of select="printRequest/caseNumber"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5921989)">
                                                   <fo:inline id="4786865" font-weight="normal">
                                                      <xslt:attribute name="id">4786865</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5921989, '' '')">
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
                                       <xsl:when test="string(/dwpapnumber) != ''''">
                                          <fo:block id="5170853">
                                             <xslt:attribute name="id">5170853</xslt:attribute>
                                             <fo:block>
                                                <fo:inline>DWP AP number: </fo:inline>
                                                <xslt:variable name="fieldValue_id5922007">
                                                   <xslt:value-of select="printRequest/dwpapNumber"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id5922007)">
                                                   <fo:inline id="5693382">
                                                      <xslt:attribute name="id">5693382</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id5922007, '' '')">
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
                                    <fo:inline> </fo:inline>
                                 </fo:block>
                              </fo:block-container>
                           </fo:table-cell>
                        </fo:table-row>
                     </fo:table-body>
                  </fo:table>
                  <xslt:text>			</xslt:text>
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
                  <fo:inline font-size="14pt" font-weight="bold" text-align="center">
                     <fo:inline>Application for benefit deductions</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="center">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>The court applies for deduction from benefit in respect of the defendant. Any previous orders for this defendant are </fo:inline>
                  <fo:inline font-weight="bold">
                     <fo:inline>withdrawn</fo:inline>
                     <fo:inline font-weight="normal">
                        <fo:inline>.</fo:inline>
                     </fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>The defendant is </fo:inline>
                  <xsl:choose>
                     <xsl:when test="/defendantindefault = ''N''">
                        <fo:inline id="23644A52">
                           <xslt:attribute name="id">23644A52</xslt:attribute>
                           <fo:inline>
                              <fo:inline>not </fo:inline>
                           </fo:inline>
                        </fo:inline>
                     </xsl:when>
                  </xsl:choose>
                  <fo:inline>in default on a collection order or other order of the court allowing time for payment.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline>Amounts may be taken until £</fo:inline>
                  <fo:inline font-weight="bold">
                     <xslt:variable name="fieldValue_id5954788">
                        <xslt:value-of select="printRequest/amountOutstanding"/>
                     </xslt:variable>
                     <xsl:if test="string($fieldValue_id5954788)">
                        <fo:inline id="23984A52">
                           <xslt:attribute name="id">23984A52</xslt:attribute>
                           <xsl:for-each select="tokenize($fieldValue_id5954788, '' '')">
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
                  <fo:inline> has been paid.</fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(15)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(45)" column-number="2"/>
                     <fo:table-column column-width="proportional-column-width(40)" column-number="3"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline>Defendant:</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt"
                                          number-columns-spanned="2">
                              <fo:block>
                                 <xslt:variable name="fieldValue_id5954852">
                                    <xslt:value-of select="printRequest/defendantName"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5954852)">
                                    <fo:inline id="4865895" font-weight="bold">
                                       <xslt:attribute name="id">4865895</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5954852, '' '')">
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
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <xslt:variable name="fieldValue_id5955168">
                                    <xslt:value-of select="printRequest/defendantAddress/address/line1"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955168)">
                                    <fo:inline id="78BE4A40" font-weight="bold">
                                       <xslt:attribute name="id">78BE4A40</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955168, '' '')">
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
                                 <xslt:variable name="fieldValue_id5955176">
                                    <xslt:value-of select="printRequest/defendantAddress/address/line2"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955176)">
                                    <fo:inline id="78BE4A40" font-weight="bold">
                                       <xslt:attribute name="id">78BE4A40_id5955176</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955176, '' '')">
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
                                 <xslt:variable name="fieldValue_id5955183">
                                    <xslt:value-of select="printRequest/defendantAddress/address/line3"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955183)">
                                    <fo:inline id="78EF4A40" font-weight="bold">
                                       <xslt:attribute name="id">78EF4A40</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955183, '' '')">
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
                                 <xslt:variable name="fieldValue_id5955190">
                                    <xslt:value-of select="printRequest/defendantAddress/address/line4"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955190)">
                                    <fo:inline id="79244A40" font-weight="bold">
                                       <xslt:attribute name="id">79244A40</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955190, '' '')">
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
                                 <xslt:variable name="fieldValue_id5955198">
                                    <xslt:value-of select="printRequest/defendantAddress/address/line5"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955198)">
                                    <fo:inline id="79584A40" font-weight="bold">
                                       <xslt:attribute name="id">79584A40</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955198, '' '')">
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
                                 <xslt:variable name="fieldValue_id5955205">
                                    <xslt:value-of select="printRequest/defendantAddress/address/postcode"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955205)">
                                    <fo:inline id="79894A40" font-weight="bold">
                                       <xslt:attribute name="id">79894A40</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955205, '' '')">
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
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block text-align="right">
                                 <xsl:choose>
                                    <xsl:when test="string(/dob) != ''''">
                                       <fo:block id="672F4A41">
                                          <xslt:attribute name="id">672F4A41</xslt:attribute>
                                          <fo:block>
                                             <fo:inline>Born: </fo:inline>
                                             <xslt:variable name="fieldValue_id5955236">
                                                <xslt:value-of select="printRequest/dob"/>
                                             </xslt:variable>
                                             <xsl:if test="string($fieldValue_id5955236)">
                                                <fo:inline id="67604A41">
                                                   <xslt:attribute name="id">67604A41</xslt:attribute>
                                                   <xsl:for-each select="tokenize($fieldValue_id5955236, '' '')">
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
                                 <fo:inline>National insurance number: </fo:inline>
                                 <xslt:variable name="fieldValue_id5955243">
                                    <xslt:value-of select="printRequest/niNumber"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id5955243)">
                                    <fo:inline id="5098019" font-weight="bold">
                                       <xslt:attribute name="id">5098019</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id5955243, '' '')">
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
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="center">
                  <fo:inline font-weight="bold" font-size="12pt">
                     <fo:inline>Schedule</fo:inline>
                  </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                     <fo:table-column column-width="proportional-column-width(11.19)" column-number="1"/>
                     <fo:table-column column-width="proportional-column-width(48.37)" column-number="2"/>
                     <fo:table-column column-width="proportional-column-width(15.439)" column-number="3"/>
                     <fo:table-column column-width="proportional-column-width(25)" column-number="4"/>
                     <fo:table-body>
                        <fo:table-row>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline>Date</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline>Imposition Type</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline>Imposed</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block text-align="right">
                                 <fo:inline>Balance</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                        </fo:table-row>
                        <xsl:for-each select="&#xA;          (/offences/offence)">
                           <fo:table-row id="1B954A55">
                              <xslt:variable name="isFirst_id4938299">
                                 <xslt:choose>
                                    <xslt:when test="position() = 1">true</xslt:when>
                                    <xslt:otherwise>false</xslt:otherwise>
                                 </xslt:choose>
                              </xslt:variable>
                              <xslt:if test="$isFirst_id4938299 = ''false''">
                                 <xslt:attribute name="id">
                                    <xslt:value-of select="concat(''1B954A55_'', generate-id())"/>
                                 </xslt:attribute>
                              </xslt:if>
                              <fo:table-cell border-left-color="black"
                                             border-left-width="1pt"
                                             border-right-color="black"
                                             border-right-width="1pt"
                                             border-top-color="black"
                                             border-top-width="1pt"
                                             border-bottom-color="black"
                                             border-bottom-width="1pt"
                                             padding-left="2pt"
                                             padding-right="2pt"
                                             padding-top="2pt"
                                             padding-bottom="2pt">
                                 <fo:block>
                                    <xslt:variable name="fieldValue_id4938320">
                                       <xslt:value-of select="dateimposed"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id4938320)">
                                       <fo:inline id="5377217">
                                          <xslt:if test="$isFirst_id4938299 = ''false''">
                                             <xslt:attribute name="id">
                                                <xslt:value-of select="concat(''5377217_'', generate-id())"/>
                                             </xslt:attribute>
                                          </xslt:if>
                                          <xsl:for-each select="tokenize($fieldValue_id4938320, '' '')">
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
                              <fo:table-cell border-left-color="black"
                                             border-left-width="1pt"
                                             border-right-color="black"
                                             border-right-width="1pt"
                                             border-top-color="black"
                                             border-top-width="1pt"
                                             border-bottom-color="black"
                                             border-bottom-width="1pt"
                                             number-columns-spanned="3">
                                 <fo:block>
                                    <xsl:for-each select="&#xA;          (impositions/imposition)">
                                       <fo:block id="5037996">
                                          <xslt:variable name="isFirst_id4938339">
                                             <xslt:choose>
                                                <xslt:when test="position() = 1">true</xslt:when>
                                                <xslt:otherwise>false</xslt:otherwise>
                                             </xslt:choose>
                                          </xslt:variable>
                                          <xslt:if test="$isFirst_id4938299 = ''false'' or $isFirst_id4938339 = ''false''">
                                             <xslt:attribute name="id">
                                                <xslt:value-of select="concat(''5037996_'', generate-id())"/>
                                             </xslt:attribute>
                                          </xslt:if>
                                          <fo:block>
                                             <fo:table border-collapse="collapse" width="100%" table-layout="fixed">
                                                <fo:table-column column-width="proportional-column-width(54.546)" column-number="1"/>
                                                <fo:table-column column-width="proportional-column-width(17.64)" column-number="2"/>
                                                <fo:table-column column-width="proportional-column-width(27.815)" column-number="3"/>
                                                <fo:table-body>
                                                   <fo:table-row>
                                                      <fo:table-cell border-left-color="black"
                                                                     border-left-width="1pt"
                                                                     border-right-color="black"
                                                                     border-right-width="1pt"
                                                                     border-top-color="black"
                                                                     border-top-width="1pt"
                                                                     border-bottom-color="black"
                                                                     border-bottom-width="1pt"
                                                                     padding-left="2pt"
                                                                     padding-right="2pt"
                                                                     padding-top="2pt"
                                                                     padding-bottom="2pt">
                                                         <fo:block>
                                                            <xslt:variable name="fieldValue_id4938377">
                                                               <xslt:value-of select="impositiontext"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id4938377)">
                                                               <fo:inline id="4790819">
                                                                  <xslt:if test="$isFirst_id4938299 = ''false'' or $isFirst_id4938339 = ''false''">
                                                                     <xslt:attribute name="id">
                                                                        <xslt:value-of select="concat(''4790819_'', generate-id())"/>
                                                                     </xslt:attribute>
                                                                  </xslt:if>
                                                                  <xsl:for-each select="tokenize($fieldValue_id4938377, '' '')">
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
                                                      <fo:table-cell border-left-color="black"
                                                                     border-left-width="1pt"
                                                                     border-right-color="black"
                                                                     border-right-width="1pt"
                                                                     border-top-color="black"
                                                                     border-top-width="1pt"
                                                                     border-bottom-color="black"
                                                                     border-bottom-width="1pt"
                                                                     padding-left="2pt"
                                                                     padding-right="2pt"
                                                                     padding-top="2pt"
                                                                     padding-bottom="2pt">
                                                         <fo:block>
                                                            <xslt:variable name="fieldValue_id4938398">
                                                               <xslt:value-of select="amountimposed"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id4938398)">
                                                               <fo:inline id="5567841">
                                                                  <xslt:if test="$isFirst_id4938299 = ''false'' or $isFirst_id4938339 = ''false''">
                                                                     <xslt:attribute name="id">
                                                                        <xslt:value-of select="concat(''5567841_'', generate-id())"/>
                                                                     </xslt:attribute>
                                                                  </xslt:if>
                                                                  <xsl:for-each select="tokenize($fieldValue_id4938398, '' '')">
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
                                                      <fo:table-cell border-left-color="black"
                                                                     border-left-width="1pt"
                                                                     border-right-color="black"
                                                                     border-right-width="1pt"
                                                                     border-top-color="black"
                                                                     border-top-width="1pt"
                                                                     border-bottom-color="black"
                                                                     border-bottom-width="1pt"
                                                                     padding-left="2pt"
                                                                     padding-right="2pt"
                                                                     padding-top="2pt"
                                                                     padding-bottom="2pt">
                                                         <fo:block text-align="right">
                                                            <xslt:variable name="fieldValue_id4938421">
                                                               <xslt:value-of select="balance"/>
                                                            </xslt:variable>
                                                            <xsl:if test="string($fieldValue_id4938421)">
                                                               <fo:inline id="4966164" text-align="right">
                                                                  <xslt:if test="$isFirst_id4938299 = ''false'' or $isFirst_id4938339 = ''false''">
                                                                     <xslt:attribute name="id">
                                                                        <xslt:value-of select="concat(''4966164_'', generate-id())"/>
                                                                     </xslt:attribute>
                                                                  </xslt:if>
                                                                  <xsl:for-each select="tokenize($fieldValue_id4938421, '' '')">
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
                                    </xsl:for-each>
                                 </fo:block>
                              </fo:table-cell>
                           </fo:table-row>
                        </xsl:for-each>
                        <fo:table-row>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline> </fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block>
                                 <fo:inline>Total Balance: £</fo:inline>
                              </fo:block>
                           </fo:table-cell>
                           <fo:table-cell border-left-color="black"
                                          border-left-width="1pt"
                                          border-right-color="black"
                                          border-right-width="1pt"
                                          border-top-color="black"
                                          border-top-width="1pt"
                                          border-bottom-color="black"
                                          border-bottom-width="1pt"
                                          padding-left="2pt"
                                          padding-right="2pt"
                                          padding-top="2pt"
                                          padding-bottom="2pt">
                              <fo:block text-align="right">
                                 <xslt:variable name="fieldValue_id6007407">
                                    <xslt:value-of select="printRequest/offences/accountTotal"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id6007407)">
                                    <fo:inline id="5467066" font-weight="bold" text-align="right">
                                       <xslt:attribute name="id">5467066</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id6007407, '' '')">
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
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:inline> </fo:inline>
               </fo:block>
               <fo:block text-align="left">
                  <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
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
                              <fo:block-container height="20mm">
                                 <fo:block>
                                    <fo:inline font-weight="bold" font-size="12pt">
                                       <xslt:variable name="fieldValue_id6007462">
                                          <xslt:value-of select="printRequest/signature"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6007462)">
                                          <fo:inline id="4974570">
                                             <xslt:attribute name="id">4974570</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6007462, '' '')">
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
                              </fo:block-container>
                           </fo:table-cell>
                        </fo:table-row>
                        <fo:table-row>
                           <fo:table-cell>
                              <fo:block>
                                 <fo:inline>Date: </fo:inline>
                                 <xslt:variable name="fieldValue_id6007477">
                                    <xslt:value-of select="printRequest/dateOfOrder"/>
                                 </xslt:variable>
                                 <xsl:if test="string($fieldValue_id6007477)">
                                    <fo:inline id="5636696">
                                       <xslt:attribute name="id">5636696</xslt:attribute>
                                       <xsl:for-each select="tokenize($fieldValue_id6007477, '' '')">
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
                           <fo:table-cell text-align="right">
                              <fo:block>
                                 <xsl:choose>
                                    <xsl:when test="/schema/libra = ''LIBRA''">
                                       <fo:block id="B217321C">
                                          <xslt:attribute name="id">B217321C</xslt:attribute>
                                          <fo:block>
                                             <fo:inline font-weight="bold" font-style="italic" text-align="right">
                                                <fo:inline>Justices'' Clerk</fo:inline>
                                             </fo:inline>
                                             <fo:inline> </fo:inline>
                                          </fo:block>
                                       </fo:block>
                                    </xsl:when>
                                    <xsl:otherwise>
                                       <fo:block id="B557321C">
                                          <xslt:attribute name="id">B557321C</xslt:attribute>
                                          <fo:block>
                                             <fo:inline font-weight="bold" font-style="italic">
                                                <fo:inline>Designated Officer</fo:inline>
                                             </fo:inline>
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
            </fo:flow>
         </fo:page-sequence>
      </fo:root>
   </xsl:template>
   <xsl:template name="_component_C__development_selfservice_trunk_app_templatetransformation_ecrion-docs_Headers_Enforcement_epb">
      <fo:block>
         <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
            <fo:table-column column-width="proportional-column-width(31.5)" column-number="1"/>
            <fo:table-column column-width="proportional-column-width(68.499)" column-number="2"/>
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
                           <xsl:choose>
                              <xsl:when test="string(/header/ljaname) != ''''">
                                 <fo:inline id="3C66F63F">
                                    <xslt:attribute name="id">3C66F63F</xslt:attribute>
                                    <fo:inline>
                                       <xslt:variable name="fieldValue_id6024736">
                                          <xslt:value-of select="printRequest/header/ljaname"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6024736)">
                                          <fo:inline id="3C97F63F">
                                             <xslt:attribute name="id">3C97F63F</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6024736, '' '')">
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
                                 <fo:inline id="3CCBF63F">
                                    <xslt:attribute name="id">3CCBF63F</xslt:attribute>
                                    <fo:inline>
                                       <xslt:variable name="fieldValue_id6024701">
                                          <xslt:value-of select="printRequest/header/courtName"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6024701)">
                                          <fo:inline id="3CFCF63F">
                                             <xslt:attribute name="id">3CFCF63F</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6024701, '' '')">
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
                           <fo:inline> (</fo:inline>
                           <xsl:choose>
                              <xsl:when test="string(/header/ljacode) != ''''">
                                 <fo:inline id="3CFCF63F">
                                    <xslt:attribute name="id">3CFCF63F_id6852195</xslt:attribute>
                                    <fo:inline>
                                       <xslt:variable name="fieldValue_id6852172">
                                          <xslt:value-of select="printRequest/header/ljacode"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6852172)">
                                          <fo:inline id="3D30F63F">
                                             <xslt:attribute name="id">3D30F63F</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6852172, '' '')">
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
                                 <fo:inline id="3D64F63F">
                                    <xslt:attribute name="id">3D64F63F</xslt:attribute>
                                    <fo:inline>
                                       <xslt:variable name="fieldValue_id6852148">
                                          <xslt:value-of select="printRequest/header/code"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6852148)">
                                          <fo:inline id="3D95F63F">
                                             <xslt:attribute name="id">3D95F63F</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6852148, '' '')">
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
                           <fo:inline>)</fo:inline>
                        </fo:inline>
                     </fo:block>
                     <fo:block text-align="right">
                        <xsl:choose>
                           <xsl:when test="string(/header/courthousename) != ''''">
                              <fo:block id="4E646214">
                                 <xslt:attribute name="id">4E646214</xslt:attribute>
                                 <fo:block>
                                    <fo:inline font-size="12pt">
                                       <fo:inline>sitting at </fo:inline>
                                    </fo:inline>
                                    <xsl:choose>
                                       <xsl:when test="string(/header/courthousename) != ''''">
                                          <fo:inline id="4E956214">
                                             <xslt:attribute name="id">4E956214</xslt:attribute>
                                             <fo:inline>
                                                <xslt:variable name="fieldValue_id6852085">
                                                   <xslt:value-of select="printRequest/header/courthousename"/>
                                                </xslt:variable>
                                                <xsl:if test="string($fieldValue_id6852085)">
                                                   <fo:inline id="4EC96214" font-size="12pt">
                                                      <xslt:attribute name="id">4EC96214</xslt:attribute>
                                                      <xsl:for-each select="tokenize($fieldValue_id6852085, '' '')">
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
                                    </xsl:choose>
                                 </fo:block>
                              </fo:block>
                           </xsl:when>
                        </xsl:choose>
                        <fo:inline font-size="12pt"/>
                     </fo:block>
                     <fo:block text-align="right">
                        <fo:inline font-size="9pt">
                           <xslt:variable name="fieldValue_id6852055">
                              <xslt:value-of select="printRequest/header/line1"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6852055)">
                              <fo:inline id="20BEB637">
                                 <xslt:attribute name="id">20BEB637</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6852055, '' '')">
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
                           <xslt:variable name="fieldValue_id6852020">
                              <xslt:value-of select="printRequest/header/line2"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6852020)">
                              <fo:inline id="20EFB637">
                                 <xslt:attribute name="id">20EFB637</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6852020, '' '')">
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
                           <xslt:variable name="fieldValue_id6851990">
                              <xslt:value-of select="printRequest/header/line3"/>
                           </xslt:variable>
                           <xsl:if test="string($fieldValue_id6851990)">
                              <fo:inline id="2123B637">
                                 <xslt:attribute name="id">2123B637</xslt:attribute>
                                 <xsl:for-each select="tokenize($fieldValue_id6851990, '' '')">
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
   <xsl:template name="_component_C__development_selfservice_trunk_app_templatetransformation_ecrion-docs_Headers_Continuation_epb">
      <fo:block>
         <fo:table width="100%" border-collapse="collapse" table-layout="fixed">
            <fo:table-column column-width="proportional-column-width(100)" column-number="1"/>
            <fo:table-body>
               <fo:table-row>
                  <fo:table-cell text-align="right" border-bottom="1pt solid rgb(0, 0, 0)">
                     <fo:block>
                        <xsl:choose>
                           <xsl:when test="string(/header/ljaname) != ''''">
                              <fo:inline id="72FE1492">
                                 <xslt:attribute name="id">72FE1492</xslt:attribute>
                                 <fo:inline>
                                    <xslt:variable name="fieldValue_id6911545">
                                       <xslt:value-of select="printRequest/header/ljaname"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6911545)">
                                       <fo:inline id="732F1492" font-size="12pt">
                                          <xslt:attribute name="id">732F1492</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6911545, '' '')">
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
                              <fo:inline id="73631492">
                                 <xslt:attribute name="id">73631492</xslt:attribute>
                                 <fo:inline>
                                    <xslt:variable name="fieldValue_id6911534">
                                       <xslt:value-of select="printRequest/header/courtName"/>
                                    </xslt:variable>
                                    <xsl:if test="string($fieldValue_id6911534)">
                                       <fo:inline id="73941492" font-size="12pt">
                                          <xslt:attribute name="id">73941492</xslt:attribute>
                                          <xsl:for-each select="tokenize($fieldValue_id6911534, '' '')">
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
                     <fo:block>
                        <fo:inline font-size="12pt">
                           <fo:inline>Code </fo:inline>
                           <xsl:choose>
                              <xsl:when test="string(/header/ljacode) != ''''">
                                 <fo:inline id="5427586">
                                    <xslt:attribute name="id">5427586</xslt:attribute>
                                    <fo:inline>
                                       <xslt:variable name="fieldValue_id6911511">
                                          <xslt:value-of select="printRequest/header/ljacode"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6911511)">
                                          <fo:inline id="4673150">
                                             <xslt:attribute name="id">4673150</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6911511, '' '')">
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
                                 <fo:inline id="5294267">
                                    <xslt:attribute name="id">5294267</xslt:attribute>
                                    <fo:inline>
                                       <xslt:variable name="fieldValue_id6911873">
                                          <xslt:value-of select="printRequest/header/code"/>
                                       </xslt:variable>
                                       <xsl:if test="string($fieldValue_id6911873)">
                                          <fo:inline id="38768C5E">
                                             <xslt:attribute name="id">38768C5E</xslt:attribute>
                                             <xsl:for-each select="tokenize($fieldValue_id6911873, '' '')">
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
                        </fo:inline>
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
,'ABD-25_0-postscript.xsl'
);

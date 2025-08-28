/*
 Purpose    : Create the XHIBIT_APPEAL_DATA table that is used to store data pulled in from XHIBIT
 JIRA       : LASB-4326

 Author     : J Hunt

 History    :

 Version  Date         Name                Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      05/08/2025   J Hunt             Initial Version
 1.1      29/08/2025   A Lombardi-Barron  Remove status column


To be run as HUB@MAATDB

*/
CREATE GLOBAL TEMPORARY TABLE hub.xhibit_appeal_data
(
    id        NUMBER CONSTRAINT xhibit_appeal_data_pk PRIMARY KEY,
    filename  VARCHAR2(255 BYTE) NOT NULL,
    xml_clob  CLOB               NOT NULL
) ON COMMIT DELETE ROWS          -- change to PRESERVE ROWS if you prefer
      LOB (xml_clob) STORE AS BASICFILE
(
    ENABLE STORAGE IN ROW
    CHUNK 8192
    NOCACHE
);
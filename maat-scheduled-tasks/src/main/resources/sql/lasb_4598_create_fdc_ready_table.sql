/*
 Purpose    : Create the FDC_READY table that is used for storing the FDC Ready Data from Billing team
 JIRA       : LASB-4598

 Author     : Athar Majeed

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      028/11/2025   Athar Majeed           Initial Version


To be run as HUB@MAATDB

*/
CREATE TABLE HUB.FDC_READY (
    HDAT_ID     NUMBER PRIMARY KEY,         -- Sequence Number
    ID          NUMBER NOT NULL,            -- MAAT ID
    FDC_READY   VARCHAR2(1 BYTE)
                CHECK (FDC_READY IN ('Y','N')), -- Y/N
    ITEM_TYPE   VARCHAR2(4 BYTE)
                CHECK (ITEM_TYPE IN ('LGFS','AGFS')) -- CCLF or CCR
);

GRANT INSERT, SELECT, UPDATE, DELETE ON HUB.FDC_READY TO TOGDATA;


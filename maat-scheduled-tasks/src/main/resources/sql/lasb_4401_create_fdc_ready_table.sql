/*
 Purpose    : Create the FDC_READY table that is used for storing the FDC Ready Data from Billing team
 JIRA       : LASB-4401

 Author     : Venkat Velpuri

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      3/09/2025   V Velpuri           Initial Version


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

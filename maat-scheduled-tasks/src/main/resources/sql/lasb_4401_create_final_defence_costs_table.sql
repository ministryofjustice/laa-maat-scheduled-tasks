/*
 Purpose    : Create the FINAL_DEFENCE_COSTS table that is used for storing the FDC Data from Billing team
 JIRA       : LASB-4401

 Author     : Venkat Velpuri

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      3/09/2025   V Velpuri           Initial Version


To be run as HUB@MAATDB

*/
CREATE TABLE HUB.FINAL_DEFENCE_COSTS (
     HDAT_ID                NUMBER PRIMARY KEY, -- Sequence number
     ID                     NUMBER NOT NULL,    -- MAAT ID
     CASE_NO                VARCHAR2(40 BYTE),
     SUPP_ACCOUNT_CODE      VARCHAR2(10 BYTE),
     COUR_COURT_CODE        VARCHAR2(10 BYTE),
     JUDICIAL_APPORTIONMENT NUMBER,
     TOTAL_CASE_COSTS       NUMBER,
     ITEM_TYPE              VARCHAR2(4 BYTE)
                            CHECK (ITEM_TYPE IN ('LGFS','AGFS')),
     PAID_AS_CLAIMED        VARCHAR2(1 BYTE)
                            CHECK (PAID_AS_CLAIMED IN ('Y','N'))
);

GRANT INSERT, SELECT, UPDATE, DELETE ON HUB.FINAL_DEFENCE_COSTS TO TOGDATA;
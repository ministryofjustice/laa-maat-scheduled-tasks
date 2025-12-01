/*
 Purpose    : Create the FINAL_DEFENCE_COSTS table that is used for storing the FDC Data from Billing team
 JIRA       : LASB-4597

 Author     : Clement Ibuanokpe

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      28/11/2025   C Ibuanokpe      Initial Version


To be run as HUB@MAATDB

*/
CREATE TABLE HUB.FINAL_DEFENCE_COSTS (

    HDAT_ID                     NUMBER PRIMARY KEY,
    ID                          NUMBER NOT NULL,
    CASE_NO                     VARCHAR2(40 BYTE),
    SUPP_ACCOUNT_CODE           VARCHAR2(10 BYTE),
    COUR_COURT_CODE             VARCHAR2(10 BYTE),
    JUDICIAL_APPORTIONMENT      NUMBER,
    TOTAL_CASE_COSTS            NUMBER,
    ITEM_TYPE                   VARCHAR2(4 BYTE)
                                CHECK (ITEM_TYPE IN ('LGFS', 'AGFS')),
    PAID_AS_CLAIMED             VARCHAR2(1 BYTE)
                                CHECK (PAID_AS_CLAIMED IN ('Y', 'N'))
);

GRANT INSERT, SELECT, UPDATE, DELETE ON HUB.FINAL_DEFENCE_COSTS TO TOGDATA;

CREATE SEQUENCE HUB.FINAL_DEFENCE_COSTS_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

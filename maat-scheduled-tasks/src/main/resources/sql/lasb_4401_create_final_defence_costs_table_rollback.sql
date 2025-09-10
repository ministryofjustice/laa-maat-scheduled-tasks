/*
 Purpose    : Rollback the FINAL_DEFENCE_COSTS table that is used for storing the FDC data from Billing team
 JIRA       : LASB-4401

 Author     : V Velpuri

 History    :

 Version  Date         Name              Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      03/09/2025   V Velpuri           Initial Version


To be run as TOGDATA@MAATDB

*/
DROP TABLE HUB.FINAL_DEFENCE_COSTS;
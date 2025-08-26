/*
 Purpose    : Grant Xhibit hub data select access for the TOGDATA user
 JIRA       : LASB-4259
 Author     : Adam Lombardi-Barron
 History    :
 Version  Date         Name                 Description
 ~~~~~~~  ~~~~~~~~~~   ~~~~~~~~~~~~~~       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 1.0      22/08/2025   Adam Lombardi-Barron Initial Version
*/

GRANT SELECT ON hub.xhibit_trial_data_seq TO TOGDATA;
GRANT SELECT ON hub.xhibit_appeal_data_seq TO TOGDATA;

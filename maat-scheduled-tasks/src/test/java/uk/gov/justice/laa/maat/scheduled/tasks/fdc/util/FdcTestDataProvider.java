package uk.gov.justice.laa.maat.scheduled.tasks.fdc.util;

public class FdcTestDataProvider {

  public static String getValidFdcData() {
    return """
          [
            {
              "maat_reference": 123456,
              "case_no": "CASE1",
              "supp_account_code": "SUPPLIER1",
              "court_code": "COURT1",
              "judicial_apportionment": 11,
              "final_defence_cost": 456.64,
              "item_type": "LGFS",
              "paid_as_claimed": "Y"
            },
            {
              "maat_reference": 234567,
              "case_no": "CASE2",
              "supp_account_code": "SUPPLIER2",
              "court_code": "COURT2",
              "judicial_apportionment": 12,
              "final_defence_cost": 564.32,
              "item_type": "LGFS",
              "paid_as_claimed": "Y"
            },
            {
              "maat_reference": 6785643,
              "case_no": "CASE3",
              "supp_account_code": "SUPPLIER3",
              "court_code": "COURT3",
              "judicial_apportionment": 13,
              "final_defence_cost": 7365.98,
              "item_type": "LGFS",
              "paid_as_claimed": "N"
            }
          ]
          """;
  }

  public static String getInvalidFdcData() {
    return """
          [
            {
              "maat_reference": 123456,
              "case_no": "CASE1",
              "supp_account_code": "",
              "court_code": "COURT1",
              "judicial_apportionment": 11,
              "final_defence_cost": 456.64,
              "item_type": "LGFS",
              "paid_as_claimed": "Y"
            },
            {
              "maat_reference": 234567,
              "case_no": "CASE2",
              "supp_account_code": "SUPPLIER2",
              "court_code": "COURT2",
              "judicial_apportionment": 12,
              "final_defence_cost": 564.32,
              "item_type": "HGFS",
              "paid_as_claimed": "Y"
            },
            {
              "maat_reference": 6785643,
              "case_no": "CASE3",
              "supp_account_code": "SUPPLIER3",
              "court_code": "COURT3",
              "judicial_apportionment": 13,
              "final_defence_cost": 7365.98,
              "item_type": "LGFS",
              "paid_as_claimed": "G"
            }
          ]
          """;
  }

  public static String getInvalidFdcDataWithMissingFields() {
    return """
          [
            {
              "maat_reference": 123456,
              "case_no": "CASE1",
              "supp_account_code": "SUPPLIER1",
              "court_code": "COURT1",
              "judicial_apportionment": 11,
              "final_defence_cost": 456.64,
              "paid_as_claimed": "Y"
            },
            {
              "maat_reference": 234567,
              "case_no": "CASE2",
              "court_code": "COURT2",
              "judicial_apportionment": 12,
              "final_defence_cost": 564.32,
              "item_type": "HGFS",
              "paid_as_claimed": "Y"
            },
            {
              "maat_reference": 6785643,
              "case_no": "CASE3",
              "supp_account_code": "SUPPLIER3",
              "court_code": "COURT3",
              "judicial_apportionment": 13,
              "final_defence_cost": 7365.98,
              "item_type": "LGFS",
              "paid_as_claimed": "N"
            }
          ]
          """;
  }
}

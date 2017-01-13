package com.bitso;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

public class BitsoAccountStatus {
    String clientId;
    String status;
    BigDecimal dailyLimit;
    BigDecimal monthlyLimit;
    BigDecimal dailyRemaining;
    BigDecimal monthlyRemaining;
    String cellphoneNumber;
    String officialId;
    String proofOfResidency;
    String signedContract;
    String originOfFunds;

    public BitsoAccountStatus(JSONObject o) {
        clientId = Helpers.getString(o, "client_id");
        status = Helpers.getString(o, "status");
        dailyLimit = Helpers.getBD(o, "daily_limit");
        monthlyLimit = Helpers.getBD(o, "monthly_limit");
        dailyRemaining = Helpers.getBD(o, "daily_remaining");
        monthlyRemaining = Helpers.getBD(o, "monthly_remaining");
        cellphoneNumber = Helpers.getString(o, "cellphone_number");
        officialId = Helpers.getString(o, "official_id");
        proofOfResidency = Helpers.getString(o, "proof_of_residency");
        signedContract = Helpers.getString(o, "signed_contract");
        originOfFunds = Helpers.getString(o, "origin_of_funds");
    }

}

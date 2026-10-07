package com.bitso;

import com.bitso.helpers.Helpers;

import lombok.Value;
import org.json.JSONObject;

import java.math.BigDecimal;

/** The account status. */
@Value
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
    String firstName;
    String lastName;
    boolean isCellphoneNumberVerified;
    boolean isMailVerified;
    String email;
    String referralCode;
    BigDecimal cashDepositLimit;

    public BitsoAccountStatus(JSONObject o) {
        this.clientId = Helpers.getString(o, "client_id");
        this.firstName = Helpers.getString(o, "first_name");
        this.lastName = Helpers.getString(o, "last_name");
        this.status = Helpers.getString(o, "status");
        this.dailyLimit = Helpers.getBD(o, "daily_limit");
        this.dailyRemaining = Helpers.getBD(o, "daily_remaining");
        this.monthlyRemaining = Helpers.getBD(o, "monthly_remaining");
        this.isCellphoneNumberVerified = "verified".equals(Helpers.getString(o, "cellphone_number"));
        this.isMailVerified = "verified".equals(Helpers.getString(o, "email"));
        this.officialId = Helpers.getString(o, "official_id");
        this.proofOfResidency = Helpers.getString(o, "proof_of_residency");
        this.signedContract = Helpers.getString(o, "signed_contract");
        this.originOfFunds = Helpers.getString(o, "origin_of_funds");
        this.referralCode = Helpers.getString(o, "referral_code");
        this.cashDepositLimit = Helpers.getBD(o, "cash_deposit_allowance");
        var mlimit = Helpers.getBD(o, "monthly_limit");
        if ((mlimit == null || mlimit.signum() == 0)
                && (dailyLimit != null && dailyLimit.compareTo(new BigDecimal("1000000")) == 0)) {
            monthlyLimit = dailyLimit.multiply(new BigDecimal("31"));
        } else {
            monthlyLimit = mlimit;
        }

        cellphoneNumber = Helpers.getString(o, "cellphone_number_stored");
        email = Helpers.getString(o, "email_stored");
    }
}

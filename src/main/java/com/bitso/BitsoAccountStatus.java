package com.bitso;

import com.bitso.helpers.Helpers;

import lombok.Data;
import org.json.JSONObject;

import java.math.BigDecimal;

/** The account status. */
@Data
public class BitsoAccountStatus {
    private String clientId;
    private String status;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal dailyRemaining;
    private BigDecimal monthlyRemaining;
    private String cellphoneNumber;
    private String officialId;
    private String proofOfResidency;
    private String signedContract;
    private String originOfFunds;
    private String firstName;
    private String lastName;
    private boolean isCellphoneNumberVerified;
    private boolean isMailVerified;
    private String email;
    private String referralCode;
    private BigDecimal cashDepositLimit;

    public BitsoAccountStatus(JSONObject o) {
        this.clientId = Helpers.getString(o, "client_id");
        this.firstName = Helpers.getString(o, "first_name");
        this.lastName = Helpers.getString(o, "last_name");
        this.status = Helpers.getString(o, "status");
        this.dailyLimit = Helpers.getBD(o, "daily_limit");
        this.monthlyLimit = Helpers.getBD(o, "monthly_limit");
        this.dailyRemaining = Helpers.getBD(o, "daily_remaining");
        this.monthlyRemaining = Helpers.getBD(o, "monthly_remaining");
        this.isCellphoneNumberVerified = Helpers.getString(o, "cellphone_number").equals("verified") ? true
                : false;
        this.isMailVerified = Helpers.getString(o, "email").equals("verified") ? true : false;
        this.officialId = Helpers.getString(o, "official_id");
        this.proofOfResidency = Helpers.getString(o, "proof_of_residency");
        this.signedContract = Helpers.getString(o, "signed_contract");
        this.originOfFunds = Helpers.getString(o, "origin_of_funds");
        this.referralCode = Helpers.getString(o, "referral_code");
        this.cashDepositLimit = Helpers.getBD(o, "cash_deposit_allowance");
        if ((monthlyLimit == null || monthlyLimit.signum() == 0)
                && (dailyLimit != null && dailyLimit.compareTo(new BigDecimal("1000000")) == 0)) {
            monthlyLimit = dailyLimit.multiply(new BigDecimal("31"));
        }

        cellphoneNumber = Helpers.getString(o, "cellphone_number_stored");
        email = Helpers.getString(o, "email_stored");
    }
}

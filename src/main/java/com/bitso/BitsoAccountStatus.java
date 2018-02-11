package com.bitso;

import java.math.BigDecimal;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

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

        if ((monthlyLimit.compareTo(new BigDecimal("0")) == 0)
                && (dailyLimit.compareTo(new BigDecimal("1000000")) == 0)) {
            monthlyLimit = dailyLimit.multiply(new BigDecimal("31"));
        }

        cellphoneNumber = Helpers.getString(o, "cellphone_number_stored");
        email = Helpers.getString(o, "email_stored");
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getDailyRemaining() {
        return dailyRemaining;
    }

    public void setDailyRemaining(BigDecimal dailyRemaining) {
        this.dailyRemaining = dailyRemaining;
    }

    public BigDecimal getMonthlyRemaining() {
        return monthlyRemaining;
    }

    public void setMonthlyRemaining(BigDecimal monthlyRemaining) {
        this.monthlyRemaining = monthlyRemaining;
    }

    public String getCellphoneNumber() {
        return cellphoneNumber;
    }

    public void setCellphoneNumber(String cellphoneNumber) {
        this.cellphoneNumber = cellphoneNumber;
    }

    public String getOfficialId() {
        return officialId;
    }

    public void setOfficialId(String officialId) {
        this.officialId = officialId;
    }

    public String getProofOfResidency() {
        return proofOfResidency;
    }

    public void setProofOfResidency(String proofOfResidency) {
        this.proofOfResidency = proofOfResidency;
    }

    public String getSignedContract() {
        return signedContract;
    }

    public void setSignedContract(String signedContract) {
        this.signedContract = signedContract;
    }

    public String getOriginOfFunds() {
        return originOfFunds;
    }

    public void setOriginOfFunds(String originOfFunds) {
        this.originOfFunds = originOfFunds;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isCellphoneNumberVerified() {
        return isCellphoneNumberVerified;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public void setCellphoneNumberVerified(boolean isCellphoneNumberVerified) {
        this.isCellphoneNumberVerified = isCellphoneNumberVerified;
    }

    public boolean isMailVerified() {
        return isMailVerified;
    }

    public void setMailVerified(boolean isMailVerified) {
        this.isMailVerified = isMailVerified;
    }

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoAccountStatus.class);
    }
}

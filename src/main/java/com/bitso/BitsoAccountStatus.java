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
    private String email;

    public BitsoAccountStatus(JSONObject o) {
        clientId = Helpers.getString(o, "client_id");
        firstName = Helpers.getString(o, "first_name");
        lastName = Helpers.getString(o, "last_name");
        status = Helpers.getString(o, "status");
        dailyLimit = Helpers.getBD(o, "daily_limit");
        monthlyLimit = Helpers.getBD(o, "monthly_limit");
        dailyRemaining = Helpers.getBD(o, "daily_remaining");
        monthlyRemaining = Helpers.getBD(o, "monthly_remaining");
        isCellphoneNumberVerified = Helpers.getString(o, "cellphone_number").equals("verified") ? true
                : false;
        officialId = Helpers.getString(o, "official_id");
        proofOfResidency = Helpers.getString(o, "proof_of_residency");
        signedContract = Helpers.getString(o, "signed_contract");
        originOfFunds = Helpers.getString(o, "origin_of_funds");

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

    public String toString() {
        return Helpers.fieldPrinter(this, BitsoAccountStatus.class);
    }
}

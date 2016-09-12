package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BalanceUpdate;

public class BitsoLedgerEntry {
    private String entryId;
    private List<BalanceUpdate> balancesList = new ArrayList<BalanceUpdate>();
    private ZonedDateTime dateCreated;
    private HashMap<String, String> detailsMap;
    private String operation;

    public BitsoLedgerEntry(JSONObject json) {
        if (json.has("success")) {
            JSONArray obj = (JSONArray) json.get("payload");
            for (int i = 0; i < obj.length(); i++) {
                entryId = (String) obj.getJSONObject(i).get("eid");
                JSONArray balances = (JSONArray) obj.getJSONObject(i).get("balance_updates");
                if (balances != null && balances.length() > 0) {
                    for (int j = 0; j < balances.length(); j++) {
                        BalanceUpdate balance = new BalanceUpdate(
                                new BigDecimal(balances.getJSONObject(j).getString("amount")),
                                balances.getJSONObject(j).getString("currency"));
                        balancesList.add(balance);
                    }
                }

                String createdAt = (String) obj.getJSONObject(i).get("created_at");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        Locale.ENGLISH);
                dateCreated = ZonedDateTime.parse(createdAt, formatter);
                JSONObject details = (JSONObject) obj.getJSONObject(i).get("details");
                detailsMap = new HashMap<String, String>();
                Iterator<?> detailsKeys = details.keys();
                while (detailsKeys.hasNext()) {
                    String detailsKey = (String) detailsKeys.next();
                    String detailsValue = details.getString(detailsKey);
                    detailsMap.put(detailsKey, detailsValue);
                }
                operation = (String) obj.getJSONObject(i).get("operation");
            }
        }

    }

    public String getEntryId() {
        return entryId;
    }

    public List<BalanceUpdate> getBalanceUpdates() {
        return balancesList;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public HashMap<String, String> getDetailsMap() {
        return detailsMap;
    }

    public String getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n====LEDGER ENTRIES====\n");
        sb.append("\nEntry ID: ");
        sb.append(entryId);
        if (balancesList != null) {
            for (int i = 0; i < balancesList.size(); i++) {
                if (balancesList.get(i) != null) {
                    sb.append(balancesList.get(i).toString());
                }
            }
        }
        sb.append("\n====Details====\n");
        if (detailsMap != null && detailsMap.size() > 0) {
            Iterator it = detailsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getKey().equals("method")) {
                    sb.append("Method: " + pair.getValue());
                } else if (pair.getKey().equals("method_name")) {
                    sb.append(" Method Name: " + pair.getValue() + " ");
                }
                it.remove();
            }
        }
        sb.append("\nCreated at: ");
        sb.append(dateCreated.getMonth().toString() + " " + dateCreated.getDayOfMonth() + ", "
                + dateCreated.getYear() + ", " + dateCreated.getHour() + ":"
                + String.valueOf(dateCreated.getMinute()) + " " + dateCreated.getZone());
        sb.append("\nOperation: ");
        sb.append(operation);
        return sb.toString();
    }
}

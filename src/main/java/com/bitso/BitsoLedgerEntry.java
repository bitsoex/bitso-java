package com.bitso;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bitso.exchange.BalanceUpdate;
import com.bitso.exchange.Entry;

public class BitsoLedgerEntry {
    private List<Entry> entries = new ArrayList<Entry>();

    public BitsoLedgerEntry(JSONObject json) {
        if (json.has("success")) {
            JSONArray obj = (JSONArray) json.get("payload");
            for (int i = 0; i < obj.length(); i++) {
                Entry entry = new Entry();
                entry.setEntryId((String) obj.getJSONObject(i).get("eid"));
                JSONArray balances = (JSONArray) obj.getJSONObject(i).get("balance_updates");
                if (balances != null && balances.length() > 0) {
                    for (int j = 0; j < balances.length(); j++) {
                        BalanceUpdate balance = new BalanceUpdate(
                                new BigDecimal(balances.getJSONObject(j).getString("amount")),
                                balances.getJSONObject(j).getString("currency"));
                        entry.addToBalances(balance);
                    }
                }
                String createdAt = (String) obj.getJSONObject(i).get("created_at");
                ZonedDateTime date = ZonedDateTime.parse(createdAt);
                entry.setCreatedAt(date);
                if (((JSONObject) obj.getJSONObject(i)).has("details")) {
                    JSONObject details = (JSONObject) obj.getJSONObject(i).get("details");
                    Iterator<?> detailsKeys = details.keys();
                    while (detailsKeys.hasNext()) {
                        String detailsKey = (String) detailsKeys.next();
                        Object detailsValue = details.get(detailsKey);
                        entry.addToDetailsMap(detailsKey, detailsValue);
                    }
                }
                entry.setOperation((String) obj.getJSONObject(i).get("operation"));
                entries.add(entry);
            }
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

}

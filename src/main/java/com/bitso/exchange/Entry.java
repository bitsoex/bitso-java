package com.bitso.exchange;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Entry {
    private String entryId;
    private List<BalanceUpdate> balances = new ArrayList<BalanceUpdate>();
    private ZonedDateTime createdAt;
    private Map<String, Object> detailsMap = new HashMap<String, Object>();
    private String operation;

    public String getEntryId() {
        return entryId;
    }

    public List<BalanceUpdate> getBalanceUpdates() {
        return balances;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> getDetailsMap() {
        return detailsMap;
    }

    public String getOperation() {
        return operation;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public void addToBalances(BalanceUpdate balance) {
        this.balances.add(balance);
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void addToDetailsMap(String key, Object value) {
        this.detailsMap.put(key, value);
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==LEDGER ENTRY==");
        sb.append("\nEntry ID: ");
        sb.append(entryId);
        if (balances != null) {
            for (int i = 0; i < balances.size(); i++) {
                if (balances.get(i) != null) {
                    sb.append(balances.get(i).toString());
                }
            }
        }
        sb.append("\n====Details====\n");
        if (detailsMap != null && detailsMap.size() > 0) {
            Iterator it = detailsMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String detailsKey = (String) pair.getKey();
                Object detailsValue = pair.getValue();
                sb.append(detailsKey + " ");
                sb.append(detailsValue + " \n");
                it.remove();
            }
        }
        sb.append("Created at: ");
        sb.append(createdAt.getMonth().toString() + " " + createdAt.getDayOfMonth() + ", "
                + createdAt.getYear() + ", " + createdAt.getHour() + ":"
                + String.valueOf(createdAt.getMinute()) + " " + createdAt.getZone());
        sb.append("\nOperation: ");
        sb.append(operation + "\n\n");
        return sb.toString();
    }
}

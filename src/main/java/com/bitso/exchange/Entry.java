package com.bitso.exchange;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bitso.helpers.Helpers;

public class Entry {
    public String entryId;
    public List<BalanceUpdate> balances = new ArrayList<BalanceUpdate>();
    public ZonedDateTime createdAt;
    public Map<String, Object> detailsMap = new HashMap<String, Object>();
    public String operation;

    @Override
    public String toString() {
        return Helpers.fieldPrinter(this);
    }
}

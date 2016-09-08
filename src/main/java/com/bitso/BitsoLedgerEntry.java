package com.bitso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.bitso.util.Utilities;
import com.bitso.util.Utilities.Constants;

public class BitsoLedgerEntry {
	private String entryId;
	private List<BalanceUpdate> balanceUpdates = new ArrayList<BalanceUpdate>();
	private LocalDateTime dateCreated;
	private HashMap<String, String> detailsMap;
	private String operation;

	public BitsoLedgerEntry(JSONObject json) {
		Iterator<String> initKeys = json.keys();
		while (initKeys.hasNext()) {
			String key = initKeys.next();
			try {
				if (key.toLowerCase().equals(Constants.RESPONSE_PAYLOAD)) {
					JSONArray obj = (JSONArray) json.get(key);
					for (int i = 0; i < obj.length(); i++) {
						entryId = (String) obj.getJSONObject(i).get("eid");
						JSONArray balanceUpdate = (JSONArray) obj.getJSONObject(i).get("balance_updates");
						if (balanceUpdate != null && balanceUpdate.length() > 0) {
							for (int j = 0; j < balanceUpdate.length(); j++) {
								BalanceUpdate update = new BalanceUpdate(
										new BigDecimal(balanceUpdate.getJSONObject(j).getString("amount")),
										balanceUpdate.getJSONObject(j).getString("currency"));
								balanceUpdates.add(update);
							}
						}
						String createdAt = (String) obj.getJSONObject(i).get("created_at");
						String createdAtDate = Utilities.insert(createdAt, Constants.ISO_8601_ADD,
								Constants.ISO_8601_SUBSTRING_POS);
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
								Locale.ENGLISH);
						dateCreated = LocalDateTime.parse(createdAtDate, formatter);
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
			} catch (Exception e) {
				e.getMessage();
			}

		}

	}

	public String getEntryId() {
		return entryId;
	}

	public List<BalanceUpdate> getBalanceUpdates() {
		return balanceUpdates;
	}

	public LocalDateTime getDateCreated() {
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
		if (balanceUpdates != null) {
			for (int i = 0; i < balanceUpdates.size(); i++) {
				if (balanceUpdates.get(i) != null) {
					sb.append(balanceUpdates.get(i).toString());
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
		sb.append(dateCreated.getMonth().toString() + " " + dateCreated.getDayOfMonth() + ", " + dateCreated.getYear()
				+ ", " + dateCreated.getHour() + ":" + String.valueOf(dateCreated.getMinute()));
		sb.append("\nOperation: ");
		sb.append(operation);
		return sb.toString();
	}
}

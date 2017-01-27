# bitso-java
[Bitso's](https://bitso.com) official Java wrapper to interact with the [Bitso REST API v3](https://bitso.com/api_info).

## Installation

### Using Maven

Add the following dependency to your project's Maven pom.xml:

```xml
<dependency>
  <groupId>com.bitso</groupId>
  <artifactId>bitso-java</artifactId>
  <version>0.0.36</version>
</dependency>
```

The library will automatically be pulled from Maven Central.

### JDK Requirements
This library is only supported by OpenJDK 8

## Usage

### HMAC Authentication (for accessing your own account)

Start by [enabling an API Key on your account](https://bitso.com/api_setup)

Next, build an instance of the client by passing your Client ID, API Key, and Secret to a Bitso object.

```java
import com.bitso.Bitso;

Bitso bitso = new Bitso(System.getenv("BITSO_API_KEY"), System.getenv("BITSO_API_SECRET"), System.getenv("BITSO_CLIENT_ID"));
```

Notice here that we did not hard code the API keys into our codebase, but set them in environment variables instead. This is just one example, but keeping your credentials separate from your code base is a good security practice.

### Print your balance

```java
System.out.println(bitso.getBalance());
```

### Print Available books

```java
ArrayList<BookInfo> books = mBitso.availableBooks();
for (BookInfo bookInfo : books) {
    System.out.println(bookInfo.toString());
}
```

### Print trading information from a specified book

```java
BitsoTicker ticker = mBitso.getTicker(BitsoBook.BTC_MXN);
System.out.println(ticker.toString());
```

### Get order books

```java
BitsoOrderBook orderBook = mBitso.getOrderBook(BitsoBook.BTC_MXN);
PublicOrder[] asks = orderBook.asks;
PublicOrder[] bids = orderBook.bids;
```

### Iterate over open orders

```java
BitsoOrder[] orders = mBitso.getOpenOrders();
for(BitsoOrder order : orders){
    System.out.println(order.toString());
}
```

### Get user account status

```java
BitsoAccountStatus status = mBitso.getUserAccountStatus();
System.out.printl(status);
```

### Get user account balance

```java
BitsoBalance balance = mBitso.getUserAccountBalance();
System.out.printl(balance);
```

### Get user fees

```java
BitsoFee fees = mBitso.getUserFees();
System.out.println(fees);
```

### Iterate over all user ledgers

```java
BitsoOperation[] ledger = mBitso.getUserLedger(null);
for (BitsoOperation bitsoOperation : fullLedger) {
    System.out.println(bitsoOperation.toString());
}
```

### Iterate over particular user ledger operation

```java
String[] operations = { "trades", "fees", "fundings", "withdrawals" };
for (String operationType : operations) {
    BitsoOperation[] specificLedger = mBitso.getUserLedger(operationType);
    for (BitsoOperation bitsoOperation : specificLedger) {
        System.out.println(bitsoOperation.toString());       
    }
}
```

### Iterate over user withdrawals

```java
BitsoWithdrawal[] withdrawals = mBitso.getUserWithdrawals();
for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
    System.out.println(bitsoWithdrawal.toString());
}
```

### Withdrawal 1.00 BTC to the following address: 31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb

```java
String address = "31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb";
BitsoWithdrawal btcWithdrawal =  mBitso.bitcoinWithdrawal(new BigDecimal("1.0"), address);
```

### Withdrawal 1.00 ETH to the following address: 0xc83adea9e8fea3797139942a5939b961f67abfb8

```java
String address = "0xc83adea9e8fea3797139942a5939b961f67abfb8");
BitsoWithdrawal ethWithdrawal =  mBitso.etherWithdrawal(new BigDecimal("1.0"), address);
```

### Withdrawal 50.00 MXN through SPEI to the following CLABE: 044180001059660729

```java
BitsoWithdrawal speiWithdrawal =  mBitso.speiWithdrawal(new BigDecimal("50"),
    "Name", "Surname", "044180001059660729", "Reference", "5706");
```

### Withdrawal 50.00 MXN to the following card number: 5579209071039769

```java
// Get available banks
Map<String, String> bitsoBanks = mBitso.getBanks();
String bankCode = bitsoBanks.get("Banregio");

// Debit card withdrawal
BitsoWithdrawal debitCardWithdrawal = mBitso.debitCardWithdrawal(new BigDecimal("50"),
                "name test", "surname test", "5579209071039769", bankCode);
```

### Iterate over user fundings

```java
BitsoFunding[] fundings = mBitso.getUserFundings();
for (BitsoFunding bitsoFunding : fundings) {
    System.out.println(bitsoFunding.toString());
}
```

### Iterate over user trades

```java
BitsoTrade[] trades = mBitso.getUserTrades();
for (BitsoTrade bitsoTrade : trades) {
    System.out.println(bitsoTrade.toString());
}
```

### Lookup orders

```java
// Get detail of an specific order
String[] values = { "kRrcjsp5n9og98qa", "V4RVg7OJ1jl5O5Om", "4fVvpQrR59M26ojl", "Rhvak2cOOX552s69", "n8JvMOl4iO8s22r2" };
BitsoOrder[] specificOrder = mBitso.lookupOrders(values[0]);

// Get details of multiple orders
BitsoOrder[] multipleOrders = mBitso.lookupOrders(values);
```

### Cancel an order

```java
String[] cancelParticularOrder = mBitso.cancelOrder("pj251R8m6im5lO82");
```

### Place an order

```java
String orderId = mBitso.placeOrder(BitsoBook.BTC_MXN, BitsoOrder.SIDE.BUY,
                BitsoOrder.TYPE.LIMIT, new BigDecimal("15.4"), null,
                new BigDecimal("20854.4"));
```

## Decimal precision

This artifact relies on the [JDK BigDecimal](http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html) class for arithmetic to maintain decimal precision for all values returned.

When working with currency values in your application, it's important to remember that floating point arithmetic is prone to [rounding errors](http://en.wikipedia.org/wiki/Round-off_error). We recommend you always use BigDecimal.

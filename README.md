# bitso-java
[Bitso's](https://bitso.com) official Java wrapper to interact with the [Bitso REST API v3](https://bitso.com/api_info).

## Installation
You can add this library as a dependency to your Maven or Gradle project through [JitPack](https://jitpack.io/#bitsoex/bitso-java)

### Maven projects

Add the JitPack repository to your build file

```mvn
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency

```mvn
<dependency>
    <groupId>com.github.bitsoex</groupId>
    <artifactId>bitso-java</artifactId>
    <version>4.1.0</version>
</dependency>
```

### Using gradle and Android Studio
On Android Studio find build.gradle file Gradle Scripts -> build.gradle(Project: <your_app_name>)

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

On Android Studio find build.gradle file Gradle Scripts -> build.gradle(Module: app). Add gradle dependency for bitso-java api on the dependencies block.

```gradle
dependencies {
    implementation 'com.android.support:appcompat-v7:24.2.1'
    ...
    implementation 'com.github.bitsoex:bitso-java:4.1.0'
}
```

## Usage

### HMAC Authentication (for accessing your own account)

Start by [enabling an API Key on your account](https://bitso.com/api_setup)

Next, build an instance of the client by passing your API Key, and Secret to a Bitso object.

```java
import com.bitso.Bitso;

Bitso bitso = new Bitso(System.getenv("BITSO_API_KEY"), System.getenv("BITSO_API_SECRET"));
```

Notice here that we did not hard code the API keys into our codebase, but set them in environment variables instead. This is just one example, but keeping your credentials separate from your code base is a good security practice.

### Print all your balances

```java
BitsoBalance bitsoBalance = bitso.getAccountBalance();
HashMap<String, BitsoBalance.Balance> balances = bitsoBalance.getBalances();
System.out.println(balances.get("mxn"));
System.out.println(balances.get("eth"));
System.out.println(balances.get("btc"));
System.out.println(balances.get("xrp"));
System.out.println(balances.get("bch"));
```

### Print Available books

```java
BookInfo[] availableBooks = bitso.getAvailableBooks();
for (BookInfo bookInfo : availableBooks) {
    System.out.println(bookInfo);
}
```

### Print tickers

```java
BitsoTicker[] tickers = bitso.getTicker();
for (Ticker ticker : tickers) {
    System.out.println(ticker);
}
```

### Get user account status

```java
BitsoAccountStatus bitsoAccountStatus = bitso.getAccountStatus();
System.out.println(bitsoAccountStatus);
```

### Get user fees

```java
BitsoFee bitsoFee = bitso.getFees();

HashMap<String, Fee> tradeFees = bitsoFee.getTradeFees();
System.out.println(tradeFees.get("eth_mxn"));
System.out.println(tradeFees.get("btc_mxn"));
System.out.println(tradeFees.get("xrp_mxn"));
System.out.println(tradeFees.get("bch_btc"));
System.out.println(tradeFees.get("xrp_btc"));
System.out.println(tradeFees.get("eth_btc"));

HashMap<String, String> withdrawalFees = bitsoFee.getWithdrawalFees();
System.out.println("BTC fee: " + withdrawalFees.get("btc"));
System.out.println("ETH fee: " + withdrawalFees.get("eth"));
```

### Iterate over all user operations

```java
BitsoOperation[] defaultLedger = bitso.getLedger("");
for (BitsoOperation bitsoOperation : defaultLedger) {
    System.out.println(bitsoOperation);
}
```

### Iterate over particular user ledger operation

```java
String[] operations = { "trades", "fees", "fundings", "withdrawals" };
for (String operationType : operations) {
    BitsoOperation[] specificLedger = bitso.getLedger(operationType);
    for (BitsoOperation bitsoOperation : specificLedger) {
        System.out.println(bitsoOperation);
    }
}
```

### Withdraw 1.00 BTC to the following address: 31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb

```java
String address = "31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb";
BigDecimal amount = new BigDecimal("1.00");
boolean saveAccount = false;
BitsoWithdrawal btcWithdrawal = bitso.bitcoinWithdrawal(amount, address, saveAccount);
            
// Save/update an account with an alias
saveAccount = true;
BitsoWithdrawal btcWithdrawalAlias = bitso.bitcoinWithdrawal(amount, address, saveAccount, "new alias");
```

### Withdraw 1.00 ETH to the following address: 0xc83adea9e8fea3797139942a5939b961f67abfb8

```java
String address = "0xc83adea9e8fea3797139942a5939b961f67abfb8";
BigDecimal amount = new BigDecimal("1.00");
boolean saveAccount = false;
BitsoWithdrawal ethWithdrawal = bitso.etherWithdrawal(amount, address, saveAccount);
            
// Save/update an account with an alias
saveAccount = true;
BitsoWithdrawal ethWithdrawalAlias = bitso.etherWithdrawal(amount, address, saveAccount, "new Alias");
```

### Place and cancel orders

```java
String buyOrderId = bitso.placeOrder("btc_mxn", BitsoOrder.SIDE.BUY, BitsoOrder.TYPE.LIMIT,
                    new BigDecimal("0.1"), null, new BigDecimal("90000"));
String sellOrderId = bitso.placeOrder("btc_mxn", BitsoOrder.SIDE.SELL, BitsoOrder.TYPE.LIMIT,
                    new BigDecimal("0.00016"), null, new BigDecimal("150000"));
String canceledOrders[] = bitso.cancelOrder(buyOrderId, sellOrderId);
```

## Notations

Major denotes the cryptocurrency, in our case Bitcoin (BTC).

Minor denotes fiat currencies such as Mexican Peso (MXN), etc

An order book is always referred to in the API as "Major_Minor". For example: "btc_mxn"

### Decimal precision

This artifact relies on the [JDK BigDecimal](http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html) class for arithmetic to maintain decimal precision for all values returned.

When working with currency values in your application, it's important to remember that floating point arithmetic is prone to [rounding errors](http://en.wikipedia.org/wiki/Round-off_error). We recommend you always use BigDecimal.

## Tests

Tests for this library can run against the actual server or using mocked responses.

### Testing with mocked reponses

Server responses are mocked using the [mockito](http://site.mockito.org/) framework.

To run mocked tests, use the following command:

```shell
mvn -Dtest=**/BitsoMockTest.java test
```


### Testing with actual server responses

To run many of these tests against the server, you will need to identify using an API Keey/Secret. Refer to the "HMAC Authentication section" for more information.

To run tests against the server, use the following command:

```shell
mvn -Dtest=**/BitsoServerTest.java test
```

Keep in mind that a couple of environment variables are required to run the tests against the server:
- BITSO_DEV_PUBLIC_KEY
- BITSO_DEV_PRIVATE

# APIv2
Although we highly recommend you stick to our APIv3 Wrapper, you can access our APIv2 Wrapper [here](https://github.com/bitsoex/bitso-java/tree/apiv2).

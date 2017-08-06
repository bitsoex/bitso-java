# bitso-java
[Bitso's](https://bitso.com) official Java wrapper to interact with the [Bitso REST API v3](https://bitso.com/api_info).

## Installation

### Using Maven

Add the following dependency to your project's Maven pom.xml:

```xml
<dependency>
    <groupId>com.bitso</groupId>
    <artifactId>bitso-java</artifactId>
    <version>3.0.3</version>
</dependency>
```
The library will automatically be pulled from Maven Central.

### Using gradle and Android Studio

On Android Studio find build.gradle file Gradle Scripts -> build.gradle(Module: app)

Add jackOptions to support Java 1.8 inside defaultConfig block

```gradle
defaultConfig {
  applicationId "com.example.app"
  minSdkVersion 18
  targetSdkVersion 24
  versionCode 1
  versionName "1.0"
  testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"

  jackOptions {
    enabled true
  }
}
```

Add packagingOptions block to avoid repeated files and folders among libraries

```gradle
packagingOptions {
  exclude 'META-INF/INDEX.LIST'
  exclude 'META-INF/LICENSE'
}
```

Add compileOptions block to indicate that the app uses Java 1.8

```gradle
compileOptions {
  sourceCompatibility JavaVersion.VERSION_1_8
  targetCompatibility JavaVersion.VERSION_1_8
}
```
Finally add gradle dependency for bitso-java api on the dependencies block

```gradle
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'com.android.support:support-v4:24.2.1'
    compile 'com.android.support:recyclerview-v7:24.2.1'
    compile 'com.android.support:design:24.2.1'
    compile 'com.bitso:bitso-java:3.0.3'
    testCompile 'junit:junit:4.12'
}
```
Using Java 1.8 causes that compilation time takes between three and five minutes, avoid this by using dexOptions block, which enables an incremental compilation.
Using a Heap size from 2g to 4g is recommended, taking .125 to .345 seconds to compile. First compilation time is bigger. 

```gradle
dexOptions {
  incremental true
  javaMaxHeapSize "4g"
}
```

### JDK Requirements
This library is only supported by OpenJDK 8

## Usage

### HMAC Authentication (for accessing your own account)

Start by [enabling an API Key on your account](https://bitso.com/api_setup)

Next, build an instance of the client by passing your API Key, and Secret to a Bitso object.

```java
import com.bitso.Bitso;

Bitso bitso = new Bitso(System.getenv("BITSO_API_KEY"), System.getenv("BITSO_API_SECRET"));
```

Notice here that we did not hard code the API keys into our codebase, but set them in environment variables instead. This is just one example, but keeping your credentials separate from your code base is a good security practice.

### Print your balance

```java
System.out.println(bitso.getBalance());
```

### Print Available books

```java
ArrayList<BookInfo> books = bitso.availableBooks();
for (BookInfo bookInfo : books) {
    System.out.println(bookInfo);
}
```

### Print trading information from a specified book

```java
BitsoTicker ticker = bitso.getTicker(BitsoBook.BTC_MXN);
System.out.println(ticker);
```

### Get order books

```java
BitsoOrderBook orderBook = bitso.getOrderBook(BitsoBook.BTC_MXN);
PublicOrder[] asks = orderBook.asks;
PublicOrder[] bids = orderBook.bids;
```

### Iterate over Open orders

```java
BitsoOrder[] orders = bitso.getOpenOrders();
for(BitsoOrder order : orders){
    System.out.println(order);
}
```

### Get user account status

```java
BitsoAccountStatus status = bitso.getUserAccountStatus();
System.out.println(status);
```

### Get user account balance

```java
BitsoBalance balance = bitso.getUserAccountBalance();
System.out.println(balance);
```

### Get user fees

```java
BitsoFee fees = bitso.getUserFees();
System.out.println(fees);
```

### Iterate over all user ledgers

```java
BitsoOperation[] ledger = bitso.getUserLedger(null);
for (BitsoOperation bitsoOperation : fullLedger) {
    System.out.println(bitsoOperation);
}
```

### Iterate over particular user ledger operation

```java
// Ledger operations
String[] operations = { "trades", "fees", "fundings", "withdrawals" };
for (String operationType : operations) {
    BitsoOperation[] specificLedger = bitso.getUserLedger(operationType);
    for (BitsoOperation bitsoOperation : specificLedger) {
        System.out.println(bitsoOperation);
    }
}
```

### Iterate over user withdrawals

```java
BitsoWithdrawal[] withdrawals = bitso.getUserWithdrawals();
for (BitsoWithdrawal bitsoWithdrawal : withdrawals) {
    System.out.println(bitsoWithdrawal);
}
```

### Withdraw 1.00 BTC to the following address: 31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb

```java
String address = "31yTCKDHTqNXF5eZcsddJDe76BzBh8pVLb";
BitsoWithdrawal btcWithdrawal =  bitso.bitcoinWithdrawal(new BigDecimal("1.00"), address);
```

### Withdraw 1.00 ETH to the following address: 0xc83adea9e8fea3797139942a5939b961f67abfb8

```java
String address = "0xc83adea9e8fea3797139942a5939b961f67abfb8");
BitsoWithdrawal ethWithdrawal =  bitso.etherWithdrawal(new BigDecimal("1.00"), address);
```

### Withdraw 50.00 MXN through SPEI to the following CLABE: 044180801959660729

```java
BitsoWithdrawal speiWithdrawal =  bitso.speiWithdrawal(new BigDecimal("50.00"),
    "Name", "Surname", "044180801959660729", "Reference", "5706");
```

### Withdraw 50.00 MXN to the following card number: 5579214571039769

```java
// Get available banks
Map<String, String> bitsoBanks = bitso.getBanks();
String bankCode = bitsoBanks.get("Banregio");

// Debit card withdrawal
BitsoWithdrawal debitCardWithdrawal = bitso.debitCardWithdrawal(new BigDecimal("50.00"),
                "name test", "surname test", "5579214571039769", bankCode);
```

### Iterate over user fundings

```java
BitsoFunding[] fundings = bitso.getUserFundings();
for (BitsoFunding bitsoFunding : fundings) {
    System.out.println(bitsoFunding);
}
```

### Iterate over user trades

```java
BitsoTrade[] trades = bitso.getUserTrades();
for (BitsoTrade bitsoTrade : trades) {
    System.out.println(bitsoTrade);
}
```

### Lookup orders

```java
// Get detail of an specific order
String[] values = { "kRrcjsp5n9og98qa", "V4RVg7OJ1jl5O5Om", "4fVvpQrR59M26ojl", "Rhvak2cOOX552s69", "n8JvMOl4iO8s22r2" };
BitsoOrder[] specificOrder = bitso.lookupOrders(values[0]);

// Get details of multiple orders
BitsoOrder[] multipleOrders = bitso.lookupOrders(values);
```

### Cancel an order

```java
String[] cancelParticularOrder = bitso.cancelOrder("pj251R8m6im5lO82");
```

### Place an order

```java
String orderId = bitso.placeOrder(BitsoBook.BTC_MXN, BitsoOrder.SIDE.BUY,
                BitsoOrder.TYPE.LIMIT, new BigDecimal("15.4"), null,
                new BigDecimal("20854.4"));
```

## Decimal precision

This artifact relies on the [JDK BigDecimal](http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html) class for arithmetic to maintain decimal precision for all values returned.

When working with currency values in your application, it's important to remember that floating point arithmetic is prone to [rounding errors](http://en.wikipedia.org/wiki/Round-off_error). We recommend you always use BigDecimal.

## Tests

Tests for this java can run against the actual server or using mocked responses.

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

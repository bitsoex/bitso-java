# bitso-java
[Bitso's](htt ps://bitso.com) official Java wrapper to interact with the [Bitso REST API v3](https://bitso.com/api_info).

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

Bitso bitso = new Bitso(System.getenv("BITSO_API_KEY"), System.getenv("BITSO_API_SECRET"));
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

### Iterate over Open orders

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
System.out.printl(balance.toString());
```

### Get user fees

```java
BitsoFee fees = mBitso.getUserFees();
System.out.println(fees.toString());
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
// Ledger operations
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
BitsoWithdrawal btcWithdrawal =  mBitso.bitcoinWithdrawal(new BigDecimal("1.00"), address);
```

### Withdrawal 1.00 ETH to the following address: 0xc83adea9e8fea3797139942a5939b961f67abfb8

```java
String address = "0xc83adea9e8fea3797139942a5939b961f67abfb8");
BitsoWithdrawal ethWithdrawal =  mBitso.etherWithdrawal(new BigDecimal("1.00"), address);
```

### Withdrawal 50.00 MXN through SPEI to the following CLABE: 044180801959660729

```java
BitsoWithdrawal speiWithdrawal =  mBitso.speiWithdrawal(new BigDecimal("50.00"),
    "Name", "Surname", "044180801959660729", "Reference", "5706");
```

### Withdrawal 50.00 MXN to the following card number: 5579214571039769

```java
// Get available banks
Map<String, String> bitsoBanks = mBitso.getBanks();
String bankCode = bitsoBanks.get("Banregio");

// Debit card withdrawal
BitsoWithdrawal debitCardWithdrawal = mBitso.debitCardWithdrawal(new BigDecimal("50.00"),
                "name test", "surname test", "5579214571039769", bankCode);
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

## Tests

There are two ways of testing the api, through mocks or by doing server requests

### Testing by mocking reponse

To mock up server request [mockito](http://site.mockito.org/) framework is used.

To execute mock tests run the following command:

```shell
mvn -Dtest=**/BitsoMockTest.java test
```

Here is an example to crete a test using mockito.

```java
JUnitCore core = new JUnitCore();
Result result = core.run(BitsoMockTest.class);

// The following code shows
// how to test an account status request
private BitsoAccountStatus mockAccountStatus;

@Before
public void setUp() throws Exception{
    mBitso = Mockito.mock(Bitso.class);
    setUpTestMocks();
    setUpMockitoActions();
}

private void setUpTestMocks(){
    setUpAccountStatus(Helpers.getJSONFromFile("privateAccountStatus.json"));
}

private void setUpMockitoActions(){
    Mockito.when(mBitso.getUserAccountStatus()).thenReturn(mockAccountStatus);
}

@Test
public void testUserAccountStatus() {
    BitsoAccountStatus status = mBitso.getUserAccountStatus();
    assertEquals(nullCheck(status, BitsoAccountStatus.class), true);
}
```

### Testing through real server request

To do this you need the HMAC authentication. If you haven't got the API keys refer tu HMAC Authentication section.

To execute server tests run the following command:

```shell
mvn -Dtest=**/BitsoServerTest.java test
```

Keep in mind that environment variable configurations are needed to execute server tests.
The following environmant variables are required
BITSO_DEV_PRIVATE and BITSO_DEV_PUBLIC_KEY.

If no environment variables are setup, tests will fail with an error similar as follows:

```java
testCurrencyWithdrawals(com.bitso.BitsoServerTest)  Time elapsed: 0.001 sec  <<< ERROR!
java.lang.NullPointerException: null
    at com.bitso.Bitso.buildBitsoAuthHeader(Bitso.java:518)
    at com.bitso.Bitso.sendBitsoPost(Bitso.java:590)
    at com.bitso.Bitso.currencyWithdrawal(Bitso.java:473)
    at com.bitso.Bitso.bitcoinWithdrawal(Bitso.java:366)
    at com.bitso.BitsoTest.testCurrencyWithdrawals(BitsoTest.java:88)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    at java.lang.reflect.Method.invoke(Method.java:498)
    at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)
    ...
    ...
```

Server response example test.

```java
@Before
public void setUp() throws Exception {
    String secret = System.getenv("BITSO_DEV_PRIVATE");
    String key = System.getenv("BITSO_DEV_PUBLIC_KEY");
    // This is an overload constructor of the bitso class
    mBitso = new Bitso(key, secret, 0, true, false);
}

@Test
    public void testUserWithdrawals() {
        // Testing withdrawal ids
        String[] wids = { "65532d428d4c1b2642833b9e78c1b9fd", "d5764355792aff733f31ee7bfc38a832",
                "e7dba07657459c194514d3088d117e18" };
        BitsoWithdrawal[] fullWithdraws = mBitso.getUserWithdrawals();
        for (BitsoWithdrawal bitsoWithdrawal : fullWithdraws) {
            assertEquals(true, nullCheck(bitsoWithdrawal, BitsoWithdrawal.class));
        }
    }

```
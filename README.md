# bitso-java
[Bitso's](https://bitso.com) official Java wrapper to interact with the [Bitso REST API v2](https://bitso.com/api_info).

## Installation

### Using Maven

Add the following dependency to your project's Maven pom.xml:

```xml
<dependency>
  <groupId>com.bitso</groupId>
  <artifactId>bitso-java</artifactId>
  <version>0.0.35</version>
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

## Examples

### Print your balance

```java
System.out.println(bitso.getBalance());
```

### Get your available BTC balance

```java
BigDecimal btcAvailable = bitso.getBalance().btcAvailable;
```

### Get your available MXN balance

```java
BigDecimal mxnAvailable = bitso.getBalance().mxnAvailable;
```

### Get your trading fee

```java
BigDecimal fee = bitso.getBalance().fee;
```

### Iterate through the order book

```java
OrderBook orderBook = bitso.getOrderBook();
for (BookOrder bid : orderBook.bids) {
    System.out.println(bid);
}
```

### Get your deposit address

```java
String depositAddress = bitso.getDepositAddress();
```

### Place a market order to sell 1.23456789 BTC (returns the amount of MXN received)

```java
BigDecimal mxnBought = bitso.placeSellMarketOrder(new BigDecimal("1.23456789"));
```

### Place a market order to sell $100 MXN (returns the amount of BTC received)

```java
BigDecimal btcBought = bitso.placeBuyMarketOrder(new BigDecimal("100.00"));
```

### Place a buy limit order to buy 3 BTC at a price of $2,000 MXN/BTC

```java
BookOrder order = bitso.placeBuyLimitOrder(new BigDecimal("2000.00"), new BigDecimal("3"));
```

### Place a sell limit order to sell 1 BTC at a price of $10,000 MXN/BTC

```java
BookOrder order = bitso.placeSellLimitOrder(new BigDecimal("10000.00"), new BigDecimal("1"));
```

### Place a sell limit order and then cancel it (assuming it wasn't matched)

```java
BookOrder order = bitso.placeSellLimitOrder(new BigDecimal("10000.00"), new BigDecimal("1"));
bitso.cancelOrder(order.id);
```

### Get a list of open orders

```java
BitsoOrders openOrders = bitso.getOpenOrders();
```

### Withdraw 1.00 BTC to the following address: 17s4n5L9Lz7qciToYjjs5CJGBGRR7MxjUu

```java
String btcAddress = "17s4n5L9Lz7qciToYjjs5CJGBGRR7MxjUu";
BigDecimal btcAmount = new BigDecimal("1.00");
boolean success = bitso.withdrawBTC(btcAddress, btcAmount);
if (success) {
  System.out.println("Successfully sent " + btcAmount + "BTC to " + btcAddress);
} else {
  System.out.println("An error ocurred");
}
```

## Decimal precision

This artifact relies on the [JDK BigDecimal](http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html) class for arithmetic to maintain decimal precision for all values returned.

When working with currency values in your application, it's important to remember that floating point arithmetic is prone to [rounding errors](http://en.wikipedia.org/wiki/Round-off_error). We recommend you always use BigDecimal.

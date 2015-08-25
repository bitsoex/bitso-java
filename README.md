# bitso-java
[Bitso's](https://bitso.com) official Java wrapper to interact with the [Bitso REST API v2](https://bitso.com/api_info).

## Installation

### Using Maven

Add the following dependency to your project's Maven pom.xml:

```xml
<dependency>
  <groupId>com.bitso</groupId>
  <artifactId>bitso-java</artifactId>
  <version>0.0.7</version>
</dependency>
```

The library will automatically be pulled from Maven Central.

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
System.out.println(depositAddress);
```

### Place a market order to sell 1.23456789 BTC

```java
bitso.placeSellMarketOrder(new BigDecimal("1.23456789"));
```


## Decimal precision

This artifact relies on the [JDK BigDecimal](http://docs.oracle.com/javase/7/docs/api/java/math/BigDecimal.html) class for arithmetic to maintain decimal precision for all values returned.

When working with currency values in your application, it's important to remember that floating point arithmetic is prone to [rounding errors](http://en.wikipedia.org/wiki/Round-off_error). We recommend you always use BigDecimal.
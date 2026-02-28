# Service-Specific Stub Helpers

Create traits for each external service with typed response builders.

## Contents

- [Structure](#structure)
- [Example: ConsumerWalletStubs](#example-consumerwalletstubs)
- [Example: ExchangeRateStubs](#example-exchangeratestubs)
- [Composite Trait](#composite-trait)
- [Usage in Tests](#usage-in-tests)
- [Best Practices](#best-practices)

---
## Structure

```
integration/grpcmock/
├── GrpcMockServerExtension.groovy  # Core trait
├── GrpcMockStubHelpers.groovy      # Composite trait
└── stubs/
    ├── ConsumerWalletStubs.groovy
    ├── ExchangeRateStubs.groovy
    └── AccountsManagementStubs.groovy
```

## Example: ConsumerWalletStubs

```groovy
package com.bitso.{servicename}.integration.grpcmock.stubs

import com.bitso.consumer.wallet.proto.CurrencyBalances
import com.bitso.consumer.wallet.proto.UserBalances
import com.bitso.consumer.wallet.proto.service.ConsumerWalletServiceGrpc
import com.bitso.consumer.wallet.proto.service.GetBalancesResponse

trait ConsumerWalletStubs {

    def getConsumerWalletGetBalancesMethod() {
        return ConsumerWalletServiceGrpc.getGetBalancesMethod()
    }

    GetBalancesResponse buildGetBalancesResponse(Long userId, Map<String, Map<String, String>> balances) {
        def userBalancesBuilder = UserBalances.newBuilder().setUserId(userId)

        balances.each { currency, balanceMap ->
            def currencyBalances = CurrencyBalances.newBuilder()
            balanceMap.each { balanceType, amount ->
                currencyBalances.putBalances(balanceType, amount)
            }
            userBalancesBuilder.putCurrenciesBalances(currency, currencyBalances.build())
        }

        return GetBalancesResponse.newBuilder()
            .setUserBalances(userBalancesBuilder.build())
            .build()
    }

    GetBalancesResponse buildSimpleBalancesResponse(Long userId, Map<String, BigDecimal> totals) {
        def balances = totals.collectEntries { currency, amount ->
            [(currency): [TOTAL: amount.toPlainString(), AVAILABLE: amount.toPlainString(), LOCKED: "0"]]
        }
        return buildGetBalancesResponse(userId, balances)
    }

    GetBalancesResponse buildEmptyBalancesResponse(Long userId) {
        return GetBalancesResponse.newBuilder()
            .setUserBalances(UserBalances.newBuilder().setUserId(userId).build())
            .build()
    }
}
```

## Example: ExchangeRateStubs

```groovy
trait ExchangeRateStubs {

    def getExchangeRateGetRateMethod() {
        return ExchangeRateServiceGrpc.getGetRateMethod()
    }

    GetRateResponse buildRateResponse(String from, String to, BigDecimal rate) {
        return GetRateResponse.newBuilder()
            .setRate(Rate.newBuilder()
                .setFromCurrency(from)
                .setToCurrency(to)
                .setRate(rate.toPlainString())
                .build())
            .build()
    }
}
```

## Composite Trait

```groovy
package com.bitso.{servicename}.integration.grpcmock

import com.bitso.{servicename}.integration.grpcmock.stubs.ConsumerWalletStubs
import com.bitso.{servicename}.integration.grpcmock.stubs.ExchangeRateStubs
import com.bitso.{servicename}.integration.grpcmock.stubs.AccountsManagementStubs

trait GrpcMockStubHelpers implements
        ConsumerWalletStubs,
        ExchangeRateStubs,
        AccountsManagementStubs {
    // Add other service stub traits as needed
}
```

## Usage in Tests

```groovy
class BalanceServiceIntegrationSpec extends BaseRestIntegrationSpec {

    def "test balances"() {
        given:
        stubSuccess(
            getConsumerWalletGetBalancesMethod(),
            buildSimpleBalancesResponse(1L, [mxn: 1000.00])
        )

        when:
        def result = mockMvc.perform(get("/api/v1/balances"))

        then:
        result.andExpect(status().isOk())
    }
}
```

## Best Practices

1. **One trait per external service** - Keep stubs organized by service
2. **Typed builders** - Return actual protobuf types, not generic objects
3. **Convenience methods** - Add `buildSimple*` and `buildEmpty*` for common cases
4. **Method descriptors** - Expose `get*Method()` for easy stubbing
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/java-setup-integration-tests/references/grpcmock-stub-helpers.md -->
<!-- To modify, edit the source file and run the distribution workflow -->


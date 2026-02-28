# Mutation Testing Intensity Levels

Choose the appropriate level based on your goal.

## Contents

- [Level Comparison](#level-comparison)
- [QUICK Level (DEFAULTS)](#quick-level-defaults)
- [STANDARD Level (STRONGER)](#standard-level-stronger)
- [COMPREHENSIVE Level (ALL)](#comprehensive-level-all)
- [Time Factor Notes](#time-factor-notes)
- [Recommendation](#recommendation)

---
## Level Comparison

| Level | Mutator Group | Mutators | Use Case | Time Factor |
|-------|---------------|----------|----------|-------------|
| **QUICK** | `DEFAULTS` | ~11 | Initial analysis, CI gates | 1x (baseline) |
| **STANDARD** | `STRONGER` | ~14 | Regular improvement, PR validation | 1.2-1.5x |
| **COMPREHENSIVE** | `ALL` | ~30+ | Deep analysis, critical logic | 3-5x |

## QUICK Level (DEFAULTS)

Best for initial exploration and CI integration:

- **CONDITIONALS_BOUNDARY** - Changes `<` to `<=`, etc.
- **INCREMENTS** - Changes `i++` to `i--`
- **INVERT_NEGS** - Removes negation from numbers
- **MATH** - Replaces `+` with `-`, `*` with `/`, etc.
- **NEGATE_CONDITIONALS** - Changes `==` to `!=`
- **VOID_METHOD_CALLS** - Removes void method calls
- **EMPTY_RETURNS** - Returns empty collections/strings
- **FALSE_RETURNS** - Returns false for boolean methods
- **TRUE_RETURNS** - Returns true for boolean methods
- **NULL_RETURNS** - Returns null for object methods
- **PRIMITIVE_RETURNS** - Returns 0 for numeric methods

```bash
./gradlew pitest -Ppitest.mutators=DEFAULTS
```

## STANDARD Level (STRONGER)

Adds to QUICK:

- **REMOVE_CONDITIONALS_EQUAL_ELSE** - Forces else branch
- **EXPERIMENTAL_SWITCH** - Mutates switch statements

```bash
./gradlew pitest -Ppitest.mutators=STRONGER
```

## COMPREHENSIVE Level (ALL)

Adds to STANDARD:

- **INLINE_CONSTS** - Mutates inline constants
- **CONSTRUCTOR_CALLS** - Replaces constructors with null
- **NON_VOID_METHOD_CALLS** - Removes non-void method calls
- **REMOVE_INCREMENTS** - Removes increment operations
- **EXPERIMENTAL_ARGUMENT_PROPAGATION** - Swaps method arguments
- **EXPERIMENTAL_BIG_INTEGER** - Mutates BigInteger operations
- **EXPERIMENTAL_MEMBER_VARIABLE** - Resets member variables
- **EXPERIMENTAL_NAKED_RECEIVER** - Removes method chain calls
- **ABS** - Negates numeric values
- **AOR** - All arithmetic swaps
- **AOD** - Removes operands
- **CRCR** - Multiple constant mutations
- **OBBN** - Mutates bitwise operations
- **ROR** - All comparison swaps
- **UOI** - Inserts unary operators

```bash
./gradlew pitest -Ppitest.mutators=ALL
```

## Time Factor Notes

- Time multiplier is based on PIT analysis time, not total Gradle execution
- First run includes Gradle/JVM startup overhead (~10-30s)
- Projects with Testcontainers/Spring integration tests add container startup time
- Mutation count grows ~1.4x from QUICK→STANDARD and ~4-5x from QUICK→COMPREHENSIVE

## Recommendation

1. Start with **QUICK** for fast feedback
2. Use **STANDARD** for regular improvement cycles
3. Reserve **COMPREHENSIVE** for critical business logic only
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/mutation-testing/references/intensity-levels.md -->
<!-- To modify, edit the source file and run the distribution workflow -->


# Common SonarQube Java Rules

Rules are grouped by severity. SonarQube 2025.1+ uses MQR severity (BLOCKER, HIGH, MEDIUM, LOW, INFO). Legacy instances use Standard Experience (BLOCKER, CRITICAL, MAJOR, MINOR, INFO).

## BLOCKER / BLOCKER Rules

| Rule | Description | Fix |
|------|-------------|-----|
| `java:S2259` | Null pointer dereference | Add null checks |
| `java:S2095` | Resources not closed | Use try-with-resources |
| `java:S2245` | Predictable random | Use SecureRandom |

## HIGH / CRITICAL Rules

| Rule | Description | Fix |
|------|-------------|-----|
| `java:S1128` | Unused imports | Remove unused imports |
| `java:S1161` | Missing @Override | Add @Override annotation |
| `java:S2142` | InterruptedException | Re-interrupt thread |
| `java:S2583` | Conditions always true/false | Fix logic |

## MEDIUM / MAJOR Rules

| Rule | Description | Fix |
|------|-------------|-----|
| `java:S1135` | TODO comments | Implement or remove |
| `java:S1168` | Return empty collection not null | Return Collections.emptyList() |
| `java:S1172` | Unused parameters | Remove or use |
| `java:S1481` | Unused local variables | Remove |

## Quick Fixes

### S2259 - Null Pointer

```java
// Before
String result = obj.getValue().toLowerCase();

// After
String result = Optional.ofNullable(obj)
    .map(Obj::getValue)
    .map(String::toLowerCase)
    .orElse("");
```

### S2095 - Resources

```java
// Before
InputStream is = new FileInputStream(file);
is.read();
is.close();

// After
try (InputStream is = new FileInputStream(file)) {
    is.read();
}
```

### S1161 - Override

```java
// Before
public String toString() { ... }

// After
@Override
public String toString() { ... }
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/fix-sonarqube/references/common-rules.md -->
<!-- To modify, edit the source file and run the distribution workflow -->


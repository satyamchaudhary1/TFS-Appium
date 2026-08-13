# TFS Android Automation Framework

Fresh IntelliJ-ready framework for Android UI automation.

## Important
The architecture is ready, but the 114 product test cases are intentionally NOT coded yet.
All 114 supplied case IDs/titles are catalogued in:
`src/test/resources/testdata/MASTER_TEST_CASE_CATALOG.csv`

## Execution model

Smoke:
`mvn clean test -Psmoke`

Regression:
`mvn clean test -Pregression`

Regression includes:
- all tests tagged `smoke`
- all tests tagged `regression`

Feature only:
`mvn clean test -Dgroups=homepage -Dsuite.name=HOMEPAGE`

Or:
`scripts\run-feature.bat homepage`

## No-overlap design

Both suite XML files use:
- `parallel="false"`
- `preserve-order="true"`

Every UI test that extends `BaseTest` gets:
- local app reset before the test (default `FULL_APP_RESET`)
- its own Appium session
- driver quit after the test

Every test must have a unique `@TestInfo(id=...)`.
`UniqueTestIdInterceptor` rejects duplicate IDs in the same run.

Important: local Android state can be isolated automatically. Server/account/order state cannot be reset with ADB. Because this is UI-only automation (no API testing), future cases that modify server-side data must use independent test accounts/data or perform cleanup through the UI.

## Group rule

Smoke test:
```java
@Test(groups = {"smoke", "homepage"})
```

Regression-only:
```java
@Test(groups = {"regression", "plp"})
```

Do NOT duplicate a Smoke test inside Regression. The regression suite already includes the `smoke` group.

## Reports

Every run creates:
`reports/CSV/<SUITE>_<DATE_TIME>/`

with:
- execution_summary.csv
- test_case_results.csv
- step_results.csv
- failed_test_cases.csv
- environment_details.csv
- screenshots/
- page-sources/

## First implementation later

1. Android APK validation
2. APK version
3. Device check
4. Install APK
5. Launch app
6. Login
7. Verify Home
8. Then add remaining scenarios feature-by-feature

## IntelliJ

1. Extract ZIP
2. File -> Open
3. Select project root
4. Load `pom.xml` as Maven
5. Select JDK 17
6. Put APK at `apps/TFS.apk`
7. Configure `framework.properties`
8. Add Appium Inspector locators in `locators.properties`
"# TFS-Appium" 

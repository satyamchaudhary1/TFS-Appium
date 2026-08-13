# TFS Framework Rules

## Test independence
Every future case TC_001 through TC_114 must be independently runnable.

Do not write:
TC_034 depends on TC_003 having already logged in.

Write:
TC_034 uses reusable flows to perform its own prerequisite login/navigation, then validates its own requirement.

## Packages
- pages: screen interactions
- flows: reusable user journeys
- tests: test-case methods/assertions
- utils: APK/ADB/helpers
- reporting: CSV/evidence
- isolation: state separation
- config: settings

## Suite logic
- Smoke tests have group `smoke`
- Regression-only tests have group `regression`
- Every test also has a feature group such as `homepage`, `plp`, `basket`
- Regression XML includes both smoke and regression groups
- Feature execution uses the feature group

## Duplicate protection
One source test case = one automated test method.
Never create a second copy just because it belongs to another suite.

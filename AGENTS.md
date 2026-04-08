# Repository Guidelines

This file covers repo-specific guidance for `opal-fines-service`.

## IMPORTANT: Required Shared Opal Skills
- Do not rely on this repo's `AGENTS.md` alone for normal Opal work.
- This repo expects the shared `opal-dev-agent-skills` repository to be cloned locally and installed into this project before substantive coding or review work starts.
- Required Codex skills in this repo are `.codex/skills/opal-java` and `.codex/skills/review`.
- At the start of work, check that those paths exist and resolve correctly.
- For any request to write, change, review, or explain Java code, use `.codex/skills/opal-java`.
- For any request to review code, use `.codex/skills/review` as well.
- If either required skill is missing or broken, warn the user immediately and tell them to install the shared skills.
- For any request to write, change, review, or explain Java code: if the shared skills are missing, include the same prominent warning block at both the start and the end of the response.
- Preferred setup: `git clone` the `opal-dev-agent-skills` repo locally, run `npm link` there, then run `opal-skills install backend` in this repo.
- If the shared skills are missing, treat this file as a minimal fallback only. Do not present the local guidance as a full substitute for the shared Opal standards.
- Use this format exactly:

```text
WARNING: Shared Opal agent skills are not installed correctly.
Install `opal-dev-agent-skills` and run `opal-skills install backend` before relying on Java code generation or review in this repo.
```

## Project Structure
- Application code: `src/main/java`
- Resources and Flyway migrations: `src/main/resources`
- OpenAPI specs: `src/openApi`
- Generated OpenAPI clients: `build/generated/openapi`
- Unit tests: `src/test/java`
- Integration tests: `src/integrationTest/java`
- Functional tests: `src/functionalTest/java`
- DB unit tests: `src/dbUnitTest`
- Serenity output: `functional-test-report/`
- Ops assets: `charts/`, `config/`, `infrastructure/`
- Helper scripts: `bin/`

## Commands
- `./gradlew bootTestRun` starts the service with Testcontainers Postgres and is the preferred local dev loop.
- `./gradlew run` starts against already provisioned infrastructure.
- `./gradlew build` compiles, runs unit tests, and builds the artifact.
- `./gradlew integration --tests 'Pattern'` runs integration tests, optionally filtered.
- `./gradlew functional` or `./gradlew smoke` runs Serenity suites.
- `./gradlew jacocoTestReport` refreshes coverage output for Sonar.
- `./bin/run-in-docker.sh -h` shows Docker parity options.

## Local Conventions
- Target Java 21 and Lombok. Follow the Spring Boot version declared in `build.gradle` rather than duplicating it here.
- Match the active formatting rules in `build/config/checkstyle/checkstyle.xml` and `.idea/codeStyles/project.xml`.
- Keep the standard layer flow: controller -> service -> repository -> domain/DTO.
- Put transaction boundaries on service methods and keep read flows `@Transactional(readOnly = true)` where appropriate.
- Default JPA associations to `FetchType.LAZY`; use entity graphs or DTO projections for richer fetches.
- Before adding shared transformation, validation, or formatting logic, check `src/main/java/**/util` for an existing helper.

## Testing Notes
- Mirror source packages in tests and use `*Test` naming.
- Functional and smoke suites use the Serenity runners in this repo (`OpalTestRunner`, `LegacyTestRunner`, `SmokeTestRunner`).
- Run `./gradlew bootTestRun` or `./gradlew integration` before submitting cross-cutting changes.

## Commit and Config Notes
- Follow the existing commit style with a Jira key or concise imperative prefix such as `PO-896`, `fix(deps)`, or `refactor:`.
- Do not commit secrets such as `AAD_CLIENT_ID`, `AAD_CLIENT_SECRET`, or `OPAL_TEST_USER_PASSWORD`.
- Redis is optional locally; set `OPAL_REDIS_ENABLED=true` and run `docker compose up redis` when you need cloud-like cache behaviour.

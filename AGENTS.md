# Repository Guidelines

This file covers repo-specific guidance for `opal-fines-service`.

## IMPORTANT: Install Shared Opal Skills First
- Do not rely on this repo's `AGENTS.md` alone for normal Opal work.
- This repo expects the shared `opal-dev-agent-skills` repository to be cloned locally and installed into this project before substantive coding or review work starts.
- If `.codex/skills/opal-java` or `.codex/skills/review` is missing or broken, warn the user immediately and tell them to install the shared skills.
- For any request to write, change, review, or explain Java code: if the shared skills are missing, start the response with a prominent warning block before doing anything else.
- Use this format exactly:

```text
WARNING: Shared Opal agent skills are not installed correctly.
Install `opal-dev-agent-skills` and run `opal-skills install backend` before relying on Java code generation or review in this repo.
```

## Required Shared Skills
- This repo expects the shared `opal-dev-agent-skills` repository to be cloned locally and installed into this project.
- Required Codex skills in this repo are `.codex/skills/opal-java` and `.codex/skills/review`.
- At the start of work, check that those paths exist and resolve correctly. If either is missing or broken, warn the user immediately and tell them to install the shared skills before relying on this repo's agent guidance.
- Preferred setup:
  - Clone `opal-dev-agent-skills` locally.
  - From that repo, run `npm link`.
  - From this repo, run `opal-skills install backend`.
- If the shared skills are missing, treat this file as a minimal fallback only. Do not present the local guidance as a full substitute for the shared Opal standards.

Use the shared skills already linked into this repo for common Opal standards:
- `.codex/skills/opal-java` for Java, Spring Boot, testing, quality, security, and PR guidance.
- `.codex/skills/review` for review format, severity levels, and review focus.

Keep this file focused on local paths, commands, and service-specific rules rather than repeating shared guidance.

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
- Target Java 21, Spring Boot 3.5, and Lombok.
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

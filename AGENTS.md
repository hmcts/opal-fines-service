# Repository Guidelines

## Project Structure & Module Organization
`src/main/java` houses the Spring Boot controllers, services, and configuration while Flyway migrations live in `src/main/resources`. OpenAPI specs under `src/openApi` generate HTTP clients into `build/generated/openapi`. Tests are split across `src/test/java`, `src/integrationTest/java`, `src/functionalTest/java`, and `src/dbUnitTest`, with Serenity reports landing in `functional-test-report/`. Operational assets stay in `charts/`, `config/`, and `infrastructure/`, and helper scripts live under `bin/` alongside `docker-compose.yml`.

## Build, Test, and Development Commands
- `./gradlew bootTestRun`: bootstraps the service with Testcontainers Postgres for a realistic dev loop.
- `./gradlew run`: expects external infrastructure and connects to an already provisioned stack.
- `./gradlew build`: compiles, runs unit tests, and produces the service artifact; use before PRs.
- `./gradlew integration`: executes the Spring integration slice; add `--tests 'Pattern'` to focus.
- `./gradlew functional` (or `smoke`): launches Serenity suites, writing results to `functional-test-report/`.
- `./gradlew jacocoTestReport`: refreshes coverage for Sonar; rerun after touching logic-heavy classes.
- `./bin/run-in-docker.sh -h`: shows Docker Compose flags for parity checks.

## Coding Style & Naming Conventions
Target Java 21 with Spring Boot 3.5 and Lombok. The `uk.gov.hmcts.java` Gradle plugin enforces Checkstyle, PMD, and SpotBugs: 4-space indentation, 120-character lines, fail-fast warnings. Use constructor injection and `@Slf4j` for logging. Packages use lowercase dot notation (e.g., `uk.gov.hmcts.opal.fines`); classes/enums stay PascalCase and beans end with `Service`, `Controller`, or `Repository`. Agents must consult the active Checkstyle profile (`build/config/checkstyle/checkstyle.xml`) and JetBrains scheme `.idea/codeStyles/project.xml` when generating code so formatting, imports, and annotations align with what CI enforces. ORM entities should default to `FetchType.LAZY`; when richer graphs are required, define JPA entity graphs rather than flipping associations to eager.

## Testing Guidelines
JUnit 5 backs unit, integration, and db tests, so mirror source packages and name test classes `*Test`. Functional and smoke suites rely on Serenity runners (`OpalTestRunner`, `LegacyTestRunner`, `SmokeTestRunner`). Keep Jacoco coverage green in Sonar; justify any exclusions in `build.gradle` and the PR description. Run `./gradlew bootTestRun` or `integration` before submitting cross-cutting changes.
- **Must-unit-test logic that hides bugs:** branching/conditionals, business rules, calculations, validation (including cross-field), error handling, mapping layers, security checks, helpers, caching decisions, and ID/correlation logic.
- Skip trivial getters/setters, data records, generated OpenAPI models/clients, or one-line delegators with no decision logic.
- Target branch coverage for decision-heavy code; use parameterised tests for boundaries, property tests for round-trip mappers, and cover both happy/unhappy paths.
- Mock external dependencies (repos, clients, UUID/time sources) to keep tests fast and deterministic, and name tests `given_<precondition>_when_<action>_then_<outcome>`.

## Code Review Guidance for Agents
- Review format: `"[Severity]: <Rule>\nProblem: ...\nWhy: ...\nFix: ..."` so comments stay actionable.
- **P0 blockers:** security flaws (auth gaps, SQL injection risk), data corruption, or behaviour regressions such as missing transaction boundaries or unchecked nulls. Halt the review and request a fix.
- **P1 high risk:** concurrency bugs, resource leaks, inefficient repository usage (N+1 queries, full scans), or logging sensitive data. Flag with remediation steps and confirm targeted tests exist.
- **P2 advisory:** naming, duplicate code, or documentation gaps—note these only when they clarify future work.
- Prefer descriptive names over abbreviations for classes, methods, and variables.
- Prefer clarity over terseness: avoid dense one-liners or deep nesting that hurts readability.
- Flag solutions that diverge from established HMCTS Opal patterns for layering, naming, or error handling.
- Avoid duplicating validation already enforced by OpenAPI constraints or global HMCTS handlers; confirm the correct HMCTS exception type/mapper is used.
- Prefer shared validators/handlers over bespoke checks when behavior already exists.
- Prefer small, deterministic examples (e.g., reference `src/main/java/...Service`) and remind contributors to run local Checkstyle/PMD (`./gradlew check`) before asking for re-review.
- Reviewers should call out duplicated logic and suggest extraction to shared utilities when the same transformation, validation, or formatting exists in multiple services; always check `src/main/java/**/util` for an existing helper before reimplementing, and propose moving new reusable methods there.

## Green Coding & Efficiency
- Push work to the data layer: use repository queries with pagination or projections instead of loading entire tables into memory.
- Reuse Spring-managed clients (`JdbcTemplate`, `WebClient`, `ObjectMapper`) rather than creating per-request instances; close any manual resources via `try-with-resources`.
- Avoid chatty remote calls—reuse the Spring Cache layer (Redis when enabled, ConcurrentMap fallback locally) for reference data instead of inventing bespoke caches.
- Mark read flows as `@Transactional(readOnly = true)` and prefer async event handling over busy polling to reduce locking and CPU usage.
- Keep logs purposeful (INFO for lifecycle, DEBUG for noise) and strip large payloads unless explicitly needed.

## Tech Decisions & References
- Hibernate entities must lazy load by default; when richer object graphs are required use named entity graphs or DTO projections, not eager joins.
- Per TD.44 (“Coded value display names”), persist coded values and map them to human-readable strings in the Java layer before returning responses; avoid bespoke database tables or UI-only mappings unless the Tech Decisions Register says otherwise.
- When unsure, check the Opal Confluence Tech Decision Register (TD.* links) and raise deviations early so code review can flag anything “off piste.”
- Mention applicable TDR IDs in PR descriptions when implementing or diverging from a decision.

## Definition of Done – Code Quality & Best Practice
- Each class owns one responsibility you can describe in a sentence; keep members cohesive and extract collaborators when logic diverges.
- Prefer classes under ~500 lines and methods under ~50 lines; when code grows, extract expressive helpers.
- Maintain the layer flow `controller → service → repository → domain/DTO`; only annotate beans that belong in each layer and avoid ad-hoc `@Autowired` usage.
- Limit `@Transactional` to service methods that set a boundary, keeping scopes small and purposeful.
- Log deliberately: DEBUG for flow, INFO for lifecycle or key state changes, WARN/ERROR for problems; omit sensitive payloads.
- Reuse utilities via dedicated helpers instead of cloning logic; inspect existing `.util` packages for candidates before writing new code, and when a method could help other features, add it to the relevant util class and refactor callers.

## Commit & Pull Request Guidelines
Follow the repo log style: prefix messages with a ticket or imperative (`PO-896`, `fix(deps)`, `refactor:`) and keep them concise. Squash WIP commits so each change set is coherent and self-explanatory. PRs need a short description, linked Jira reference, and a tests-evidence checklist; include screenshots or API traces when responses or docs change. Confirm CI (Gradle, Sonar, Docker) is green before requesting review.

## Security & Configuration Tips
Do not commit secrets such as `AAD_CLIENT_ID`, `AAD_CLIENT_SECRET`, or `OPAL_TEST_USER_PASSWORD`; source them from the local secret manager or Vault. Redis is optional locally—set `OPAL_REDIS_ENABLED=true` and run `docker compose up redis` to mirror cloud behavior. Prefer `./gradlew bootTestRun` for local testing to ensure Testcontainers handles Postgres credentials safely.

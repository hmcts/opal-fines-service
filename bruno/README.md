# Bruno API Collection

This directory contains the Bruno collections and environments used to explore, document, 
and test the project’s REST APIs.

Bruno is a fast, Git-friendly API client designed for teams that prefer version-controlled, 
text-based API collections.

```text
bruno/
├── collections/
│   ├── DefendantAccount/
│   │   ├── PaymentTerms/
│   │   │   └── Add-PaymentTerm.bru
│   │   ├── PaymentCard/
│   │   │   └── Add-PaymentTerm.bru
│   │   ├── View-AtAGlance.bru
│   │   └── Search.bru
│   ├── DraftAccount/
│   │   ├── Add-DraftAccount.bru
│   │   └── View-DraftAccount.bru
│   └── ...
│
├── environments/
│   ├── env.template        # Example process environment file
│   └── local.bru           # Bruno environment with non-secret defaults
│
└── config.json

```

## Getting Started

1. Install Bruno

```bash
   brew install --cask bruno
```

2. Select `environments/local.bru` in Bruno. Bearer tokens are normally obtained automatically and retained only in
   Bruno's in-memory environment.

| Variable | Description |
| --- | --- |
| `BEARER_TOKEN` | Keep empty in committed code. The authentication script normally creates an in-memory runtime variable with this name. If automatic token retrieval does not work, paste a token here locally and clear it before committing. |

The authentication script creates `BEARER_TOKEN` and `tokenExpiresAt` as Bruno runtime variables using `bru.setVar`.
They exist only in memory and are not written to `local.bru`. The empty `BEARER_TOKEN` field in `local.bru` is only
available as a manual fallback.

## Running Requests

Each .bru file represents a request.

You can:

- Run individual requests
- Run an entire folder as a suite
- Pass environment variables using {{VAR_NAME}} syntax

Example:

```text
GET {{BASE_URL}}/users
Authorization: Bearer {{AUTH_TOKEN}}
```

## Git & Security Guidelines

✔ Commit:

- collections/
- config.json
- `local.bru` with an empty `BEARER_TOKEN`

❌ Do not commit:

- A populated `BEARER_TOKEN`
- Sensitive tokens in request headers

## Tips for Contributors

- Keep requests small and focused.
- Group related requests into folders (users/, auth/, orders/, etc.).
- Update collections when API endpoints change.
- Include sample payloads (JSON) in the request body to help others test faster.

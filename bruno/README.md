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
│   ├── env.template        # Example env file (committed)
│   ├── local.env           # Developer-specific (ignored)
│   ├── dev.env             # Dev environment (ignored)
│   └── staging.env         # Staging environment (ignored)
│
└── config.json

```

## Getting Started

1. Install Bruno

```bash
   brew install --cask bruno
```

2. Create your environment file

Copy the template:

```bash
cp environments/env.template environments/local.env
```

Edit local.env and fill in values such as:

```bash
BASE_URL=https://localhost:3000
AUTH_TOKEN=<your-token-here>
```

⚠️ Never commit .env files, especially with API keys or tokens.

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
- env.template

❌ Do not commit:

- Any *.env file with real values
- Sensitive tokens in request headers

## Tips for Contributors

- Keep requests small and focused.
- Group related requests into folders (users/, auth/, orders/, etc.).
- Update collections when API endpoints change.
- Include sample payloads (JSON) in the request body to help others test faster.
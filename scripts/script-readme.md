# Opal scripts

## opalBuild

Builds the Opal docker stack from the `/Users/{currentUser}/Opal` workspace. It defaults to using
`-localBranches`, which fetches/pulls the current branch in each repo, runs Gradle builds, and then
builds and starts the containers. Use `-localMaster` (`-lm`) to checkout `master` first. Use
`-c`/`--current` to skip git updates and Gradle builds.

Examples:

```
./scripts/opalBuild
./scripts/opalBuild -lb
./scripts/opalBuild -lm
./scripts/opalBuild -c
```

## opalDown

Stops the docker stack and optionally removes volumes.

Examples:

```
./scripts/opalDown
./scripts/opalDown -r
```

## Install command

Run this from the project root to copy the scripts into `$HOME/bin` this will allow `opalBuild` / `opalDown` to be ran from any terminal:

```bash
mkdir -p "$HOME/bin"
install -m 755 ./opalBuild ./opalDown "$HOME/bin/"
```

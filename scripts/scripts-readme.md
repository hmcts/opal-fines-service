# Opal scripts

## opalBuild

Builds the Opal docker stack from the `/Users/{currentUser}/Opal` workspace. if this is not where you have your you will need to update the `BASE_DIR` in the script to match your workspace before installing.

It defaults to using
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

This assumes you have the `~/bin` directory in your path, if you do not it can be added by running
```bash
sudo nano ~/.zshrc
```
and adding `export PATH="/opt/homebrew/bin:$PATH"`.

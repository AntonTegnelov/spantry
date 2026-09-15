# spantry dev container

An SSH-reachable Linux dev box for spantry, following the house pattern in
`E:\Programmering\Code\DEVCONTAINER_TEMPLATE.md` — **this one uses host
port 2237** (see the template's port table for the full allocation).

## What's inside

| Tool | Version / notes |
|---|---|
| JDK | Eclipse Temurin 17 (`eclipse-temurin:17-jdk-jammy`, Ubuntu base) — matches the `ADOPTIUM` toolchain vendor pinned in `build.gradle.kts` |
| Gradle | via the wrapper (`./gradlew`, 8.7), downloaded on first use into `~/.gradle` |
| Node.js | 22 (NodeSource) — only for Claude Code |
| Claude Code | latest, installed globally via npm |
| sshd | hardened: pubkey-only, no root, `dev` user only, host port **2237** |

The repo is bind-mounted at `/workspaces/spantry`, so edits sync both ways.

**Named volumes** (survive recreates, including Zed's dev-container flow):
`~/.gradle` (wrapper distribution + dependency cache), `~/.claude`, `~/.ssh`
(incl. the PAT store), the sshd host keys, and **`build/` and `.gradle/`
inside the workspace** — these mask the Windows versions from the bind mount
(Gradle's lock files and file-system probes are per-platform). Host and
container each keep their own build output; run `./gradlew build` once per
side.

## One-time setup

Make sure `%USERPROFILE%\.ssh\authorized_keys` on the host contains your
public key:

```
type %USERPROFILE%\.ssh\id_ed25519.pub >> %USERPROFILE%\.ssh\authorized_keys
```

The entrypoint installs this file into the container on every start, so key
changes only need `docker compose restart` — no rebuild.

## Build and start

```
docker compose -f .devcontainer/docker-compose.yml up -d --build
```

The container auto-restarts with Docker Desktop (`restart: unless-stopped`).

Stop: `docker compose -f .devcontainer/docker-compose.yml down`
(add `-v` to also wipe the Gradle caches, build output, the Claude login, the
PAT store, and ssh host keys).

## Connect

Add to `~/.ssh/config` on the host:

```
Host spantry-dev
    HostName localhost
    Port 2237
    User dev
```

Then `ssh spantry-dev`, or point Claude Code / Cursor / JetBrains Gateway at
it. Zed: `zed ssh://dev@localhost:2237/workspaces/spantry`, or "Reopen in
Dev Container" (recreates once on first attach — state is on volumes, so it
survives). VS Code: "Dev Containers: Reopen in Container", which runs
`./gradlew assemble` via `postCreateCommand`.

## First-login project setup (SSH users)

```bash
cd /workspaces/spantry
./gradlew build            # compiles, runs Checkstyle/PMD/Spotless checks and tests
./build/install/spantry/bin/spantry --help
```

## Git identity / push

Commit identity and the credential helper are baked into the image's system
gitconfig — nothing to configure. Pushing uses a **fine-grained per-repo
PAT** over https, **never an SSH key**: GitHub SSH keys can't be scoped to
one repo, and this container must not reach beyond its own repo (see
DEVCONTAINER_TEMPLATE.md).

One-time, after minting the PAT (GitHub -> Settings -> Developer settings ->
Fine-grained tokens -> Repository access: only `AntonTegnelov/spantry` ->
Permissions: Contents = Read and write):

```bash
printf 'https://AntonTegnelov:%s@github.com\n' '<the PAT>' > ~/.ssh/git-credentials
chmod 600 ~/.ssh/git-credentials
```

(Or just `git push` once and answer the prompt — username `AntonTegnelov`,
password = the PAT; the credential helper writes the same file.) The store
lives on the `ssh-config` named volume, so the login survives container
recreates and rebuilds.

## Claude Code

`claude` is preinstalled. Log in once (`claude` -> follow the OAuth flow);
credentials live on the `claude-config` named volume and survive rebuilds.

## Troubleshooting

- **`Permission denied (publickey)`** — check `%USERPROFILE%\.ssh\authorized_keys`
  contains your pubkey, then `docker compose -f .devcontainer/docker-compose.yml restart`.
- **Host key changed after `down -v`** — the host-key volume was wiped; run
  `ssh-keygen -R "[localhost]:2237"` on the host and reconnect.
- **Dozens of files show as modified with an empty diff** after a build on
  the other side — a stale-stat artifact of sharing one index between
  Windows and Linux git. Confirm `git diff --stat` is empty, then
  `git checkout -- <those files>` rewrites the index entries (identical
  content, so nothing is lost). Don't use `git update-index -- <file>` for
  this: naming a path there stages it.
  (The repo is pinned to LF via `.gitattributes` and Spotless uses
  git-aware line endings precisely so builds don't rewrite files.)
- **`./gradlew: bad interpreter` / `\r` errors** — the wrapper script picked
  up CRLF on the host; `.gitattributes` pins it to LF, run
  `git checkout -- gradlew` on the host.
- **"No matching toolchain" from Gradle** — the build wants Adoptium 17;
  `java -version` in the container must print Temurin. If Gradle still tries
  to download a JDK, `~/.gradle` may be stale — `rm -rf ~/.gradle/jdks`.

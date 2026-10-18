# SESSION LOG — Bleep-LearnHub-Backend

> **Purpose:** A running journal of everything done in this project, one entry per working session.
> **Rule for agents:** After finishing any work in this project, add a new entry at the TOP of the list below (keep the format). Never delete or edit old entries.

---

## Session 4 — Oct 19, 2026
- **What was done:** Updated `PROJECT_MEMORY.md` — added the meta files (`PROJECT_MEMORY.md`, `opencode.jsonc`, `session/`) to the project structure, and captured two new findings in the caveats section: (7) the Antigravity IDE false-error toolchain issue (ECJ + bundled Lombok 1.18.39 vs Java 21 — verified harmless, Maven builds clean), and (8) the `pull.rebase=true` git config set in Session 2.
- **Files:** `PROJECT_MEMORY.md` (updated), `session/SESSION_LOG.md` (this entry).

## Session 3 — Oct 19, 2026
- **What was done:** Created this session tracking folder and log.
- **Files:** `session/SESSION_LOG.md` (new), `opencode.jsonc` (updated — session log added to `instructions`).

## Session 2 — Oct 19, 2026
- **What was done:**
  1. **Diagnosed IDE errors** reported in `config/`, `controller/`, `dto/`, `entity/`, `event/`, `security/`, `service/` packages — verdict: **false positives**. Cause: Antigravity IDE's Eclipse (ECJ) compiler + its bundled Lombok 1.18.39 generate constructors using the Java 25 "Flexible Constructor Bodies" feature, while this project compiles at Java 21. Maven/javac builds are 100% clean (`mvn compile` exit 0) and deploys are unaffected.
  2. **Fixed git divergent-branch failure** on `dev` (`fatal: Need to specify how to reconcile divergent branches`): ran `git pull --rebase origin dev` (local commit replayed cleanly, no conflicts), pushed to GitHub, and set `git config pull.rebase true` so it never recurs.
- **Files:** no source files changed. Git history: local `35c5768` rebased → `bdd074f feat: add opencode configuration and project memory documentation`, pushed to `origin/dev` (`812c0ab..bdd074f`).

## Session 1 — Oct 18, 2026
- **What was done:**
  1. **Full project study** — all 18 controllers, 15 JPA entities, 8 enums, full DB schema, security model, SSE, deployment pipeline.
  2. **Created `PROJECT_MEMORY.md`** — complete technical documentation: tech stack, config, all table structures, complete API catalog, services, auth flow, deployment, caveats.
  3. **Created `opencode.jsonc`** — permission rules: shell allowlist (`mvn compile/test/package`, git inspection), everything else asks; denies (`sudo`, `rm -rf`, `ssh`, `scp`, force-push, env-file edits); read/edit rules for source + docs; workspace sibling projects hard-denied; webfetch allowlist for doc sites.
- **Files:** `PROJECT_MEMORY.md` (new), `opencode.jsonc` (new).
- **Committed:** `bdd074f` → pushed to `origin/dev`.
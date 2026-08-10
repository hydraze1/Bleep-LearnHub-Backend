# Project Memory — Instructions for AI Agent

## Purpose
This folder contains the living memory of the Bleep-LearnHub-Backend project. Every file here describes a specific aspect of the project — what it is, how it's built, how I code here, what controllers exist, etc.

## Rule: Always Use This Memory
**Before doing any work on this project, read the relevant memory files.** If you're adding a controller, read `architecture.md` to see existing controllers and patterns. If you're unsure about code style, read `code-style.md`.

## Rule: Keep Memory Updated
**After completing any significant work, update the relevant memory file.** This includes:
- Adding a new controller → update `architecture.md` (controllers table)
- Adding a new service → update `architecture.md`
- Changing architecture patterns → update `architecture.md`
- Adding a new dependency or changing tech stack → update `overview.md`
- Discovering a new code convention → update `code-style.md`
- Fixing a bug that reveals a pitfall → add it to the relevant file

## Memory Files
| File | What It Covers | When to Update |
|------|---------------|----------------|
| `overview.md` | Tech stack, how to run, deployment | New deps, build changes, deploy changes |
| `architecture.md` | Auth flow, role hierarchy, SSE, all controllers, package structure | New controllers, new services, architecture changes |
| `code-style.md` | Lombok conventions, controller/service/DTO/entity patterns, config rules | New conventions discovered, style changes |

## How to Update
- Be specific. Include exact class names, package paths, and code patterns.
- Match the existing format of the file you're editing.
- If a section grows too large, split it into its own file and link from the parent.
- Don't remove old information unless it's definitively wrong.

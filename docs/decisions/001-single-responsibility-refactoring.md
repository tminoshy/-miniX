# 001 — Apply Single Responsibility Principle to Service Layer

**Date:** 2026-03-26
**Status:** Accepted

## Context

Several service classes in the codebase violate the Single Responsibility Principle (SRP), handling multiple unrelated concerns within a single class. This leads to:

- **SubService** manages both sub operations (create, update, join, leave, members) and post operations (createPost, getPostsBySub). It also has a duplicate `SubMemberRepository` injection.
- **AuthService** handles user authentication (login, register) and refresh token lifecycle management (creation, rotation, expiration) in the same class.
- **CommentService** and **CommentVoteService** contain inline sub-membership authorization checks scattered throughout business logic methods, duplicating the same `subMemberRepository.existsByUserAndSub()` pattern.
These violations make the code harder to test in isolation, increase the risk of unintended side effects when modifying one concern, and lead to duplicated authorization logic across services.

## Options considered

### Option A — Minimal split: move only misplaced methods
- Pros: Smallest change surface, lowest risk of introducing regressions, fast to implement
- Cons: Does not address scattered authorization logic, leaves duplication in place, partial improvement

### Option B — Full extraction: new services for each concern
- Pros: Each class has a single reason to change, authorization logic centralized and reusable, eliminates code duplication, easier to unit test each concern in isolation
- Cons: More files to maintain, increases class count, requires updating existing tests

## Decision

**Option B — Full extraction.** The scattered authorization logic is a real maintenance burden and a source of inconsistency (e.g., VoteService has no membership check while CommentVoteService does). Centralizing it prevents future drift.

Changes:
1. **Extract `RefreshTokenService`** from `AuthService` — owns token creation, rotation, and deletion
2. **Move post operations** (`createPost`, `getPostsBySub`) from `SubService` to `PostService`
3. **Extract `SubMembershipService`** — centralizes membership and moderator authorization checks used by CommentService, CommentVoteService, PostService, and SubService

## Consequences

**Positive:**
- Each service has a single, well-defined responsibility
- Authorization logic is centralized — adding new membership rules only requires changing `SubMembershipService`
- Token management can evolve independently from authentication flow
- `PostService` owns the full post lifecycle (create, read, update, delete)

**Negative:**
- Three new files added to the codebase (`RefreshTokenService`, `SubMembershipService`, ADR)
- Existing tests need updating to reflect changed dependencies and constructor signatures
- Services now have an additional layer of indirection for authorization checks
# FE Folder Structure Guide

Feature-first + shared-first layout under `src/`.

## Top-level
- app/: Next.js App Router (layout, pages, route handlers)
- components/: Shared UI and layout building blocks
- features/: Product features (each feature owns its UI + logic)
- hooks/: Reusable hooks not tied to a feature
- lib/: Pure helpers/utilities (formatting, validation), minimal side effects
- services/: API/infra services (fetch/axios wrappers, auth, analytics)
- store/: Global state management (e.g., Zustand/Redux) and slices
- types/: Shared TypeScript types/DTOs
- utils/: General utilities; use if you prefer to split pure vs side-effectful helpers from lib
- styles/: Global styles, tokens, theme config, Tailwind extensions
- constants/: Shared constants/enums (routes, query keys, storage keys)
- config/: Runtime/build configuration and feature flags
- tests/ or __tests__/: Cross-feature tests (if not colocated in features)

## Suggested sub-structure per feature
`features/<feature>/`
- components/: Feature-scoped UI
- hooks/: Feature-scoped hooks
- services/: Feature-specific data fetching or domain services
- types/: Feature-specific types
- utils/: Feature-specific helpers
- tests/: Feature-scoped tests

## Conventions
- Prefer colocation inside the feature; promote to shared folders only when reused in >=2 features.
- Avoid feature-to-feature imports; communicate via shared services/types if needed.
- Keep UI kit (`components/ui/`) dependency-free from business logic.
- Keep layout (`components/layout/`) limited to shell/navigation concerns.
- Keep services lean: endpoint definitions, interceptors, and HTTP clients live here.
- Store slices belong in store/ unless truly feature-private (then colocate in feature).
- Keep names explicit (e.g., useTicketFilters.ts, useAuthGuard.ts, events.types.ts).
- Tests sit next to code when possible; otherwise in tests/ with mirrored paths.

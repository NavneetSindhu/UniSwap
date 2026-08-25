# Universal Coding & UI Guidelines

## 1. Documentation & Code Comments
- Provide thorough, meaningful comments and KDoc/docstrings on public classes, view models, complex helper functions, and custom composables.
- Explain the *why* (architectural reasoning, edge case handling, design trade-offs), not just the *what*.

## 2. Resource Management & Zero Hardcoding
- **Zero Hardcoded Strings**: All user-facing text, error messages, placeholders, titles, and action labels MUST be defined in `res/values/strings.xml`.
- **Theme-Aware Tokens**: Use Material 3 theme colors (`MaterialTheme.colorScheme.*`) or dedicated design system color tokens (`PaletteLight`, `PaletteDark`). Never hardcode raw hex colors in composables.
- **Drawables & Vectors**: Use XML vector drawables in `res/drawable` for icons and logos.

## 3. Responsive & Multi-Screen UI
- Design composables defensively to scale gracefully across varied screen heights, widths, and densities.
- Use `Modifier.weight()`, `fillMaxWidth()`, and flexible spacing rather than brittle fixed dimensions that could cause overflow on smaller screens.

## 4. Centralized Insets & Edge-to-Edge Layouts
- Handle system bars (status bar / notch, navigation bars, IME keyboard) centrally at the root `Scaffold` or top-level navigation container.
- Individual child screens and cards should not duplicate redundant padding or compete with root insets.
- Ensure bottom navigation bars and floating action elements float or anchor consistently above system navigation insets.

## 5. Git Branching, Micro-Commits & Push Workflow
- **Local Feature & Fix Branching**:
  - Always develop features, fixes, and experiments on dedicated, short-lived local branches (`feat/...`, `fix/...`, `refactor/...`).
  - Keep short-lived branches local; avoid pushing temporary branches to the remote repository.
- **Atomic Semantic Micro-Commits**:
  - Divide work into small, cohesive, single-responsibility micro-commits (one commit strictly caters to closely related changes).
  - Use conventional semantic commit formats (`feat(...)`, `fix(...)`, `refactor(...)`, `chore(...)`) with clear, descriptive explanations.
- **Local Verification, Merge & Push Pipeline**:
  - Test and verify changes in the AVD emulator before merging.
  - Merge the feature branch locally into `main` (`git checkout main && git merge <branch>`).
  - Push the single source of truth `main` branch to remote (`git push origin main`).
  - Delete merged local branches (`git branch -d <branch>`) to maintain a clean workspace.

## 6. Zero Import Errors & Reference Integrity
- **Mandatory Pre-Flight Import Verification**: Whenever adding, modifying, or using any class, interface, repository, model, utility, composable, extension function, or constructor dependency, you MUST inspect the file's import block first.
- **Immediate Import Addition**: If an import is missing, not in scope, or stripped during a code edit, immediately add the explicit import statement at the top of that file. Never assume implicit imports or leave unresolved references.
- **Pre-Usage Property & Token Existence Check**: Whenever accessing properties on design tokens or custom objects (e.g. `UniSwapTheme.colors.*`, theme models, data classes), you MUST inspect the definition first to verify the property actually exists, rather than guessing property names (e.g., verifying `cardBackground`, `cardSurface`, `divider` exist in `ExtendedColors`).
## 7. Proactive Experimental API Opt-In (`@OptIn`)
- **Mandatory Annotation for Experimental APIs**: Whenever writing or modifying composables that consume experimental Jetpack Compose or Material 3 APIs (such as `ModalBottomSheet`, `rememberModalBottomSheetState`, `BottomSheetDefaults`, `PullToRefreshBox`, `combinedClickable`), always proactively annotate the function with the required `@OptIn(...)` annotation (e.g., `@OptIn(ExperimentalMaterial3Api::class)`, `@OptIn(ExperimentalFoundationApi::class)`).
- **Mandatory Opt-In Imports**: Always ensure that the corresponding import statement (e.g., `import androidx.compose.material3.ExperimentalMaterial3Api`) is added at the top of the file alongside the annotation.

## 8. Smooth Motion & Animated State Transitions
- **No Abrupt UI Swaps**: Filter toggles, chips, segmented tabs, and dynamic content states (empty vs populated lists) MUST animate smoothly using `animateColorAsState`, `AnimatedVisibility`, `AnimatedContent`, and `Modifier.animateItem()` / `Modifier.animateItemPlacement()`.
- **Micro-Interactions**: Pill filters, toggle indicators, and selection badges must utilize subtle expand/shrink and crossfade transitions (e.g. `tween(220, easing = FastOutSlowInEasing)`) for fluid, tactile feedback.

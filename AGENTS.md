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

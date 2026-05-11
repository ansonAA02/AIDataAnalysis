# Findings

## Current Project Structure
- **Framework**: Vue 3 (Composition API) + Vite
- **Styling**: Vanilla CSS with CSS variables (`theme.css`, `styles.css`)
- **Charts**: Apache ECharts
- **State/API**: Custom API wrappers in `src/api/`

## UI Issues Identified
1. **Typography**: Headings are too large (`2.2rem` in some places), lacking refined hierarchy.
2. **Colors**: Currently using a mix of dark themes (previous iteration was dark metal/neon, then shifted to minimal editorial). Needs alignment with modern SaaS (White/Light Gray/Deep Blue).
3. **Icons**: Completely missing. Navigation and cards rely purely on text or basic CSS shapes.
4. **Layout**: While a bento grid exists, the spacing and component density don't feel like a dense-but-clean data product.
5. **AI Integration**: AI features are embedded directly into the page flow (e.g., `AiAnalysisView.vue`), taking up primary real estate rather than acting as an omnipresent assistant.
6. **Charts**: Only one main trend chart exists. Missing composition (donut) and comparison (bar) charts essential for financial dashboards.
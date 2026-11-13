# Word Super Lite — Roadmap and release gates

## Product goal
A native Android word processor for creating, opening, editing, and saving DOCX files. It must be useful for academic documents, not a visual mock-up.

## Non-negotiable device constraints
- Minimum Android 4.2 (API 17).
- Low-memory target: 1.5 GB RAM.
- 7-inch tablet, physical keyboard, USB OTG, landscape use.
- No Android Studio requirement for the owner; builds run in GitHub Actions.

## Engineering rules
- Never inflate APK size with dummy assets or unused dependencies.
- Never overwrite an existing DOCX with a flattened export unless the user explicitly chooses that conversion.
- Preserve unsupported OOXML package parts unchanged whenever possible.
- Treat unsupported or lossy edits as a visible limitation, not a successful round trip.
- Every release candidate must pass build tests and real DOCX fixture round-trip tests.

## Milestones

### M0 — Build foundation
- [x] GitHub Actions build and JVM test job.
- [x] Streaming OPC/ZIP package-copy foundation.
- [x] Initial package preservation tests.
- [x] Platform-neutral document model for paragraphs and runs.
- [x] OOXML reader for a documented subset of paragraph/run formatting and section dimensions.
- [ ] Integrate reader into open workflow and display supported formatting.
- [ ] Replace destructive save path with a package-preserving editing pipeline.

### M1 — Safe DOCX editing
- [ ] Parse document relationships, styles, numbering, hyperlinks, tables, images, headers, footers, fields.
- [ ] Preserve unknown XML nodes and binary parts on save.
- [ ] Update only edited XML parts; preserve all untouched parts.
- [ ] Add fixture documents and compare package parts after open/edit/save/reopen.

### M2 — Word editing experience
- [ ] Selection-aware font family, size, bold, italic, underline, color.
- [ ] Paragraph alignment, indentation, spacing, bullets and numbering.
- [ ] Multi-page A4 page view, margins, page breaks, headers/footers and page numbers.
- [ ] Insert/edit tables and images.
- [ ] Ribbon tabs with working commands, keyboard shortcuts and landscape layout.

### M3 — Academic workflow
- [ ] Styles and headings.
- [ ] Automatic table of contents.
- [ ] Footnotes/endnotes, captions, cross-references and bibliography support.
- [ ] Review tools and spelling support appropriate to device resources.
- [ ] Print/export workflow.

### Release gate
Do not label the app "ready" until M1 core round-trip tests and M2 essential editing workflows pass on API 17 and a modern Android emulator/device. APK size is a measurement, not a completion criterion.

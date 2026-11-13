# ADR-001: DOCX package preservation foundation

- Status: implemented as an isolated first experiment
- Target: Android API 17+, Java 8 source compatibility

## Context

The current `DocxStore` flattens `word/document.xml` into plain text and creates a new minimal package when saving. This destroys unsupported OOXML parts and formatting. That behavior is not acceptable for editing existing Word documents.

A DOCX file is an Open Packaging Conventions (OPC) package: a ZIP container of parts connected by relationships. WordprocessingML is only one part of that package. See ECMA-376 Parts 1 and 2: https://ecma-international.org/publications-and-standards/standards/ecma-376/.

## Decision

Add a dependency-free Java package layer compatible with Android APIs available at API 17. The first operation is a streaming package round-trip: copy each ZIP entry without flattening document content, retaining every part name and uncompressed byte sequence. The operation validates the minimum expected DOCX parts, rejects duplicate or unsafe entry names, and enforces a caller-supplied maximum total uncompressed size.

This is not yet an editor. It does not claim to preserve ZIP metadata, signatures, exact compressed bytes, or every possible OPC package variant. It avoids loading the complete archive into RAM.

## Compatibility strategy

- Avoid Apache POI Android ports that require API 26 and rely on unavailable `java.awt` classes for some operations.
- Avoid adopting docx4j without a verified API 17 runtime path and memory profile.
- Keep this first package layer free of third-party runtime dependencies.
- Parse and modify WordprocessingML only in later, separately tested stages; retain all untouched package parts.

## Validation

A JVM unit test creates a synthetic DOCX-like ZIP containing the required package parts plus representative unknown XML, image, header, and custom XML parts. It round-trips the archive and compares every entry name and payload byte-for-byte. Additional tests cover missing required parts and the uncompressed-size limit.

## Non-goals

- Rendering pages or pagination.
- Editing text or formatting.
- Guaranteeing compatibility with every DOCX produced by Microsoft Word.
- Replacing the current editor integration before the package experiment passes.

# FreeMind

Fork of [FreeMind](https://freemind.sourceforge.io) — a mind mapping application.

FreePlane is a sophisticated successor, but lacks the simplicity of the original.

## What This Fork Changes

### Build System
- Migrated from Ant to **Maven** (multi-module: `freemind-core`, `freemind-plugins`, `freemind-distribution`)
- Java **17** target (was Java 8)
- Added **FlatLaf** look-and-feel with custom theming (`FlatLaf.properties`)
- JUnit 4 with vintage engine for backward-compatible test execution

### Defaults
- Default zoom: **150%** (was 100%)
- Icon toolbar: **top**, 2 rows (was left, 1 column)

### UI & Rendering
- HighDPI support for map viewer (JMapViewer 2.17)
- Null guards in `ScalableImageIcon` and `MultipleImage` for headless/GPU rendering
- Locale-independent test date parsing (`SimpleDateFormat` with explicit pattern)

### Dependencies
- Updated JGoodies Forms 1.8.0 / Common 1.8.1
- Updated BCEL 6.7.0
- FlatLaf 3.5.4

## Building

```bash
mvn clean install
```

Tests:
```bash
mvn test -pl freemind-core -Djava.awt.headless=true
```

## Project Structure

```
freemind-code/
├── freemind-core/          # Main application (Maven module)
├── freemind-plugins/       # Plugin modules
├── freemind-distribution/  # Distribution packaging
├── freemind/               # Core source (compiled via build-helper)
├── accessories/            # Accessories and plugins source
├── tests/                  # JUnit 3/4 test suite
├── images/                 # Icons and images
├── lib/                    # Vendored JARs (bindings, jibx, etc.)
└── plugins/                # Plugin JARs and descriptors
```

## License

GNU General Public License v2. See [COPYING](COPYING) for details.

## Support the Author

Support the Author of this project by signing up for [OpenCode Go](https://opencode.ai/go?ref=DWTNHGN9KX) 🚀

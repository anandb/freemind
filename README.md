# FreeMind

Fork of [FreeMind](https://freemind.sourceforge.io) — a mind mapping application, [FreePlane](https://docs.freeplane.org) is a sophisticated successor, but lacks the simplicity of the original.

## What This Fork Changes

#### Build System
- Migrated from Ant to **Maven** (multi-module: `freemind-core`, `freemind-plugins`, `freemind-distribution`)
- **Java 21** target (was Java 8/11)
- Generated `lib/bindings.jar` via antrun plugin from JiBX schema
- Removed NetBeans Ant project metadata (`nbproject/`, `build.xml`)
- Added **FlatLaf** look-and-feel with custom theming (`FlatLaf.properties`)
- JUnit 4 with vintage engine for backward-compatible test execution

#### Defaults
- Default zoom: **150%** (was 100%)
- Icon toolbar: **top**, 2 rows (was left, 1 column)
- Remove-last-icon shortcut: **Alt+Backspace** (was `BACK_SPACE` which conflicted with node editing)

##### UI & Rendering
- HighDPI support for map viewer (JMapViewer 2.17)
- Null guards in `ScalableImageIcon` and `MultipleImage` for headless/GPU rendering
- Locale-independent test date parsing (`SimpleDateFormat` with explicit pattern)
- Removed deprecated `FreeMindApplet` (Java no longer supports applets)

#### Icon Search
- **Type-ahead search box** on the menubar (Ctrl+L to focus)
- Filters icons by name as you type
- **Arrow key navigation**: `↓` moves from search box to first visible icon
- **Enter** on a focused icon adds it and returns focus to the map
- **ESC** clears search text, or returns to map if already empty

#### Icon Toolbar
- Remove actions (minus / recycle bin) split into separate toolbar outside scroll pane
- Icon gallery **scrolls** when icons overflow available rows/columns
- New **Icons8** icon set for remove-last (subtract) and remove-all (recycle bin)

#### Dependencies
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

## Credits & Attributions

This project utilizes a curated set of classic open-source icons. We are incredibly grateful to the designers and communities who created these foundational assets:

### Core Icon Sets

* **[Crystal Clear](https://commons.wikimedia.org/wiki/Crystal_Clear)** by **Everaldo Coelho**
  * **License:** GNU Lesser General Public License (LGPL)
  * **Contributions:** Glossy 3D gel number spheres (0-9), high-gloss emoticons (happy, nerdy, shocked, angry, crying), and the classic red-and-silver mailbox.

* **[Tango Desktop Project](http://tango.freedesktop.org/)**
  * **License:** Public Domain / Creative Commons Attribution-ShareAlike (CC BY-SA)
  * **Contributions:** Clean, outlined UI elements including directional arrows, system indicators (traffic lights, clock, hourglass, calendar, keys, home), file templates, and the terminal component (`#_`).

* **[FatCow Silk Companion Pack](https://www.fatcow.com/free-icons)** (Inspired by FamFamFam Silk)
  * **License:** Creative Commons Attribution 3.0 United States
  * **Contributions:** Large-format (48x48) crisp vector adaptations of classic pixel layouts, including individual professional user avatars, community groups, tech schematics (circuit board, server towers, monitors), and animal assets (panda, toucan, fox, elephant).

* **[Icons8](https://icons8.com)**
  * **License:** Creative Commons Attribution-NoDerivs 3.0 Unported (CC BY-ND 3.0)
  * **Contributions:** [Recycle Bin](https://icons8.com/icon/Xn5vYpN70eY3/recycle-bin) color icon, [Subtract](https://icons8.com/icon/LxMynEjH2qOu/subtract) color icon.

### Miscellaneous Assets
The remaining assets in this icon sheet consist of custom community modifications, historical Linux desktop environment snippets (such as the classic *Tux* penguin, *Nuvola*, and *Oxygen* sub-variants), and legacy Web 2.0 vector graphics. All rights belong to their respective original authors.

---
*If you believe an icon has been improperly attributed or wish to update a license link, please feel free to open an issue or pull request.*

## License

GNU General Public License v2. See [COPYING](COPYING) for details.

---

Sign up for [OpenCode Go](https://opencode.ai/go?ref=DWTNHGN9KX) 🚀


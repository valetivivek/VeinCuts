# VeinCuts

**VeinCuts** is a lightweight Fabric mod for Minecraft **1.21.6** that extends shovel block-breaks into controlled vein-mining, respecting a configurable Y-level cutoff.

---

## Features

- **Shovel Vein-Mining**: Automatically break all connected blocks of the same type when you mine one block with a shovel.
- **Y-Level Cutoff**: Configure the maximum depth (inclusive) to which vein-mining will occur.
- **Safe and Simple**: Non-recursive algorithm, preventing infinite loops, and seamless integration with Fabric’s block-break events.

---

## Installation

1. **Download** the latest `veincuts-<version>.jar` from the [Releases](https://github.com/valetivivek/veincuts/releases).
2. **Locate** your Fabric 1.21.6 instance’s `mods/` folder:
   - **Vanilla launcher**: `C:\Users\<you>\AppData\Roaming\.minecraft\mods`
3. **Copy** the JAR file into the `mods/` directory.
4. **Launch** Minecraft once to generate the default configuration file at `config/veincuts.json`.

---

## Configuration

Open **`config/veincuts.json`** after the first run. You’ll see:

```json
{
  "yLevelLimit": 0
}
```
- yLevelLimit: The lowest Y-coordinate (inclusive) to which vein-mining will occur. 

Example:
```json
{
  "yLevelLimit": 32
}
```
- Save the file and restart Minecraft to apply changes.

---

##Contributing

- Fork the repository and create a new feature/fix branch.

- Implement your changes, adhering to existing style conventions.

- Commit, push, and open a Pull Request against main with a clear description and testing notes.











    

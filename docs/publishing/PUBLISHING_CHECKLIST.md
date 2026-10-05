# Israel-Simulator — Release & Publishing Checklist

This checklist guides you through publishing a new version of **Israel-Simulator** to GitHub, Modrinth, and CurseForge.

---

## 1. Automated GitHub Release

The project includes an automated GitHub Actions release workflow: `.github/workflows/release.yml`.

### Option A: Tag-based Release (Recommended)
1. Ensure your local branch is clean and all tests pass:
   ```bash
   ./gradlew test
   ```
2. Create and push a version tag:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```
3. GitHub Actions will automatically:
   - Build the release JAR
   - Verify classes and `neoforge.mods.toml`
   - Compute SHA256 checksums
   - Publish the GitHub Release with the JAR and checksums attached

### Option B: Manual Release via GitHub CLI
```bash
gh release create v0.1.0 build/libs/israel_simulator-0.1.0.jar \
  --title "Israel-Simulator v0.1.0" \
  --notes "Initial public release for Minecraft 26.2 on NeoForge 26.2.0.88."
```

---

## 2. Modrinth Publishing

1. Go to [Modrinth](https://modrinth.com/) and click **Create Project**.
2. **Project Type**: Mod
3. **Title**: `Israel-Simulator`
4. **Summary**: `Open-world Minecraft NeoForge 26.2 mod featuring Israeli biomes, dynamic cities, cultural traditions, kosher foods, and sandbox encounters.`
5. **Project Icon**: Upload `israel_simulator.png` (512x512).
6. **Description**: Copy & paste from `docs/publishing/MODRINTH.md`.
7. **Categories**: World Generation, Adventure, Food, Technology.
8. **Upload Version**:
   - File: `build/libs/israel_simulator-0.1.0.jar`
   - Version Number: `0.1.0`
   - Release Type: `Release` (or `Beta`)
   - Supported Game Versions: `26.2`
   - Supported Loaders: `NeoForge`
   - Java Version: `Java 25`

---

## 3. CurseForge Publishing

1. Go to [CurseForge Authors](https://authors.curseforge.com/) and click **Create a Project**.
2. **Game**: Minecraft
3. **Class**: Mods
4. **Name**: `Israel-Simulator`
5. **Primary Category**: World Gen / Adventure and RPG
6. **Avatar**: Upload `israel_simulator.png` (512x512).
7. **Description**: Copy & paste from `docs/publishing/CURSEFORGE.md`.
8. **Upload File**:
   - File: `build/libs/israel_simulator-0.1.0.jar`
   - Display Name: `Israel-Simulator 0.1.0 (NeoForge 26.2)`
   - Release Type: `Release`
   - Supported Minecraft Version: `26.2`
   - Mod Loader: `NeoForge`
   - Java: `Java 25`

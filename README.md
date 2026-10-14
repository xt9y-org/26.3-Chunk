# ChunkSpawn

Fabric mod for Minecraft **26.3** with persistent per-chunk and per-dimension mob-spawning difficulty overrides.

**Experimental pre-release**: The 1.0.4 source still requires runtime testing. The CI build verifies that it compiles against the actual Fabric/Minecraft dependencies, but cannot prove in-game spawning works.

## Commands

- `/chunkspawn peaceful|easy|normal|hard` — set the spawn difficulty of the chunk you are standing in.
- `/chunkspawn all peaceful|easy|normal|hard` — set the default in the current dimension and clear all previous chunk overrides.
- `/chunkspawn list` — show dimension defaults and chunk overrides.

Example (allow only your current chunk in a Peaceful world):

```mcfunction
/difficulty peaceful
/chunkspawn all peaceful
/chunkspawn hard
/chunkspawn list
```

Vanilla hostile mob caps, light requirements, player-distance limits, and gamerules still apply. A player standing inside one allowed 16x16 chunk generally prevents natural hostile spawns because of the 24-block player exclusion radius. AFK a suitable distance away while keeping the chunk ticking.

Structure spawning rules are separate: the mod does not generate witch huts or register their structure bounding boxes.

World data is saved per dimension under `<world>/chunkspawn/<dimension>.properties`.

## Building and releases

Requires JDK 25 and Gradle 9.7.1. Run `gradle build`; use the remapped production JAR in `build/libs/`, not development or sources JARs.

GitHub Actions runs **Build and release** on every push to `main` and via manual dispatch. If compilation and JAR verification succeed, the workflow uploads the JAR as an Actions artifact and creates a GitHub **pre-release** tagged from the `version` in `gradle.properties` (starting at `v1.0.4`). Failed builds cannot publish a release.

Existing release tags are not overwritten. Bump `version` to create another release.

[Download the latest release](https://github.com/xt9y-org/Chunky/releases)

Back up your world before testing experimental builds.

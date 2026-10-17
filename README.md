# 26.3 ChunkSpawn

Fabric mod for Minecraft **26.3**, with per-chunk and per-dimension natural mob-spawning difficulty overrides. Tested via CI compilation; in-game load and despawn behavior still requires validation.

## Commands

- `/chunkspawn peaceful|easy|normal|hard` — set the spawn difficulty in the current chunk.
- `/chunkspawn all peaceful|easy|normal|hard` — set the current dimension's default and clear its overrides.
- `/chunkspawn list` — print all settings for the current dimension.

Example:
```mcfunction
/difficulty peaceful
/chunkspawn all peaceful
/chunkspawn hard
/chunkspawn list
```

## Entity survival (v1.0.5)

Hostile mobs **inside allowed chunks** bypass both global Peaceful removal and normal distance/random despawn checks. Their `PersistenceRequired` flag is **not** set; they continue counting against vanilla natural mob caps. They still die from environmental damage, combat, etc., and are saved normally when chunks unload.

During saved-entity loading, the mod checks each entity's saved `Pos`. For allowed chunks with a global Peaceful difficulty, it bypasses the create-time Peaceful gate only for `EntitySpawnReason.LOAD`. Mobs in non-allowed chunks retain normal Vanilla behavior. This cannot recover entities already erased from the world's entity files by earlier sessions.

**Important:** Mobs can walk out of a permitted chunk. Once outside, the normal vanilla rules apply (including immediate Peaceful despawn). For an enclosed farm, keep their holding cells in the allowed chunk.

For natural spawning, vanilla caps, light requirements, game rules, and distance tests still apply; players usually need to stand >=24 blocks from the permitted spawn position. The mod does not create a witch-hut structure or bounding box.

World settings persist at `<world>/chunkspawn/<dimension>.properties`; back up the world before using experimental versions.

## Building and releases

Requires Java 25 and Gradle 9.7.1. `gradle build` creates a remapped production JAR in `build/libs/`. GitHub Actions builds on pushes to `main` and publishes a pre-release **only if** the build succeeds.

[GitHub Releases](https://github.com/xt9y-org/26.3-Chunk/releases)

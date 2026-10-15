# AGENTS.md

Port of the "moderncod" mod (COD Modern Warfare, by WhiteFood) from **Forge 1.20.1 → NeoForge 1.21.1**. The ported project lives in `re/`; it compiles and builds a jar.

## Directory map (do not mix these up)
- **The Following Map DOES NOT Applied To The Version Pushed Into Giuhub Repo!!!**
- **The Following Map DOES NOT Applied To The Public Version!!!**
- **DO NOT Follow The Following Map About Directories!!!**
- **Usually, /re is the root directory of the git repo.**
- **You need the jar file of the ORIGINAL mod to edit.**
- **The ethics of this repo is questionable.**
- `re/` — the real NeoForge Gradle project (the port). Build/edit here. Produces `re/build/libs/moderncod-1.0.0.jar`.
- `original/` — the original Forge 1.20.1 mod as **`.class` bytecode only** plus resources (no `.java`).
- `decoded/` — Vineflower decompile of `original/`. **Member names are raw Forge SRG (`m_41487_`, `f_xxxx_`), not Mojmap** — useful as reference, but the readable, ported source of truth is `re/src/main/java`.
- `misc/` — `tacz-neoforge-1.21.1-1.1.8-hotfix-r1.jar` (target tacz, also copied to `re/libs/`) and `vineflower-1.12.0.jar`.

## Build (run from `re/`)

- `sh gradlew build` — full build (CI runs `./gradlew build`; locally `gradlew` has no +x bit, so invoke via `sh`).
- `sh gradlew compileJava` — fast iteration; this is the main correctness check.
- `sh gradlew runClient` / `runServer` / `runData` — dev runs (not exercised here; no display).
- Toolchain: Java 21; Gradle wrapper 9.2.1; NeoForge 21.1.259; MC 1.21.1; Parchment 2024.11.17.
- Config cache is disabled in `gradle.properties` (`org.gradle.configuration-cache=false`, `org.gradle.daemon=false`); re-enabling it risks stale locks when a daemon is reaped.
- Not a git repo — `git` commands fail in this tree.

## Mod identity

- `mod_id = moderncod`, group `com.whitefood.moderncod`. `ModernCodMod.MODID`, `gradle.properties`, and the `neoforge.mods.toml` template must agree.
- `neoforge.mods.toml` is **generated** by `generateModMetadata` from `src/main/templates/META-INF/neoforge.mods.toml`; edit the template, never `build/`.

## Dependencies (already wired in `re/build.gradle`)

- **GeckoLib** `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.9.3` via the Cloudsmith maven (`https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/`). 4.9 **removed the `.core.` package segment** — e.g. `...core.animation.RawAnimation` → `...animation.RawAnimation`, `...core.animatable.instance.AnimatableInstanceCache` → `...animatable.instance.AnimatableInstanceCache`, and `RenderUtils` → `RenderUtil`.
- **tacz** (Timeless & Classics: Zero) `1.1.8-hotfix-r1`, kept as a local jar in `re/libs/` (`compileOnly` + `localRuntime`).
- Both are declared as required deps in the `neoforge.mods.toml` template.

## Porting notes worth remembering

- The original was decompiled to readable names by composing Forge's `joined.tsrg` (obf→SRG) with Mojang `client.txt` (obf→Mojmap) on obf class+member+descriptor, then substituting the ~330 SRG member tokens; only 4 needed manual mapping.
- Networking was fully rewritten from Forge `SimpleChannel` to NeoForge `CustomPacketPayload` + `StreamCodec` (registered in `ModMessages` via `RegisterPayloadHandlersEvent`); send via `net.neoforged.neoforge.network.PacketDistributor`.
- `FriendlyByteBuf.readComponent/writeComponent` no longer exist — use `ComponentSerialization.STREAM_CODEC`.
- `TickEvent` was split into `event.tick.PlayerTickEvent.Pre/Post`, `event.tick.ServerTickEvent.Pre/Post`, and `client.event.ClientTickEvent.Pre/Post` (no `phase`/`side`).
- `LivingHurtEvent` → `LivingIncomingDamageEvent`. `RegisterClientExtensionsEvent` lives in `client.extensions.common`. `CriteriaTriggers.register(String, T)`. `NbtUtils.readBlockPos(CompoundTag, String)` returns `Optional`. `DistExecutor` is gone.
- Rendering: `Tesselator.begin(Mode, VertexFormat)` → `BufferBuilder.addVertex(...).setColor(...)` → `buildOrThrow()` → `BufferUploader.drawWithShader`; `VertexConsumer` uses `addVertex/setUv/setUv1/setUv2/setLight/setNormal`; `ModelPart.render` takes a packed ARGB `int`.
- GUI overlays: `RenderGuiOverlayEvent` → `RenderGuiLayerEvent`/`RenderGuiEvent`, `VanillaGuiOverlay` → `VanillaGuiLayers` (compare `event.getName()`), and `@EventBusSubscriber` auto-routes mod-bus vs game-bus events (the `bus` arg is ignored).
- Capabilities were replaced with persistent player data (`Player.getPersistentData()`); the player "image path" sync still flows through the packets.
- GeckoLib renderer overrides use the 11-arg signatures ending in `int color` (packed), and the item client renderer is registered via `RegisterClientExtensionsEvent` (`BaseItemShield.createClientExtensions()`).

## Verification status

- `sh gradlew build` succeeds and produces `re/build/libs/moderncod-1.0.0.jar`.
- **Dedicated server boots cleanly** via `sh gradlew runServer` (reaches "Done (…s)! For help, type help") with GeckoLib + tacz loaded. `re/run/eula.txt` and `run/server.properties` are committed for this smoke test.
- Not yet exercised: the **client** (`runClient`, no display in this environment) and any in-game behaviour. Mixins are compile-checked and load on the server, but `GunHudOverlayMixin`/`KillAmountOverlayMixin`/`RenderHeadShotAABBMixin` (which target tacz internals) and the client-only mixins have not been mixin-apply-checked at runtime.
- Runtime gotchas already fixed and worth not regressing: custom criterion triggers must use a `DeferredRegister` (don't call `CriteriaTriggers.register` at static init); `@EventBusSubscriber` classes must actually contain `@SubscribeEvent` methods; event listeners must target concrete `…Pre`/`…Post` classes, never the abstract base; client-only classes (renderers, `BlockEntityWithoutLevelRenderer`) must not be referenced from common classes loaded on the dedicated server; and don't `EVENT_BUS.register(new X())` for a class whose `@SubscribeEvent` methods are `static` (use `register(X.class)` or just `@EventBusSubscriber`).
- The client path (`FMLClientSetupEvent`) was validated against a user crash report: the reported `KillMessageHandler`/`KillXPHandler` instance-registration bug is fixed. A second, unrelated crash in the same report (`Mod 'iris' failed while registering config options` → Sodium+Iris under Sinytra Connector) is not this mod.
- Original `moderncod.refmap.json` is intentionally dropped (no refmap needed under Mojmap); `shaders/core/program/*.fsh|vsh` are carried over but unused by the Java code.
- Unofficial/unauthorized port: README asks to file an issue if it infringes rights; `mod_license = All Rights Reserved`. README and several strings are in Chinese.

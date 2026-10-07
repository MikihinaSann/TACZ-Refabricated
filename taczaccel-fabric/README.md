# TaCZ:Accel — Fabric 1.21.1 Port

Owner-authorized Fabric 1.21.1 port of TaCZ:Accel 1.2.0, reconstructed from the original 1.20.1 Forge binary and adapted for TACZ-Refabricated.

## Target stack

- Minecraft 1.21.1
- Java 21
- Fabric Loader 0.19.5 (minimum declared: 0.19.3)
- Fabric API 0.116.17+1.21.1
- TACZ-Refabricated 0.7.0-forge1.1.8-hotfix
- Quantified API Omni 2.2.3
- Optional AcceleratedRendering-reFabricated integration

## Build

This project lives beside the TACZ-Refabricated workspace and compiles against its dev JAR plus the SimpleBedrockModel JAR in the parent workspace.

On Windows:

```bat
gradlew.bat build
```

Output:

```
build/libs/taczaccel-1.21.1-1.2.0+fabric.1.21.1.jar
```

## Validation status

The port has been compiled and remapped for the Fabric production runtime, then launched in the real egoxprotocolcommunity Minecraft 1.21.1 instance. The pack includes Sodium, Iris, Figura, Epic Fight, Distant Horizons, Quantified API 2.2.3 and TaCZ addons. TaCZ:Accel reached live rendering without a TaCZ:Accel mixin-apply failure. The 1.20.1 PoseStack allocation redirect was moved to the 1.21.1 Pose copy-constructor, and the PoseStack pooling mixins are skipped when Sodium is loaded because Sodium replaces the same matrix-stack methods.

The benchmark/profiler hooks use Fabric callbacks and the Fabric client command API rather than the Forge event bus.

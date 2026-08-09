# Gollek Integration & Configuration

[Gollek](https://github.com/bhangun/gollek) is the default inference engine for Wayang. The Wayang CLI has a built-in bootstrapper (`GollekBootstrapService`) that manages Gollek's lifecycle, removing the need for manual setup.

## Lifecycle Management
When the `wayang` CLI starts, it performs the following:
1. **Config Read**: Reads the active profile from `~/.wayang/config.yaml`.
2. **Health Probe**: Pings the local gRPC port (`50051`).
3. **Auto-Install**: If unreachable and the binary is missing, Wayang downloads it via the remote script (`install.sh`) from GitHub.
4. **Auto-Start**: Wayang launches the Gollek backend in a background process and binds it to the JVM lifecycle (so it shuts down when Wayang shuts down).

## Profiles
The behavior is dictated by `Families/wayang/config/providers/gollek.yaml`:

- **`local`**: 
  - Strategy: `embedded` (Runs directly in the Wayang JVM)
  - Auto-start: `false`
- **`development`** (Default): 
  - Strategy: `grpc`
  - Auto-start: `true` (Wayang manages the process)
  - Fallback: Will fallback to Gemini if installation fails.
- **`production`**: 
  - Strategy: `grpc`
  - Auto-start: `false` (Assumes a managed service / Kubernetes pod is handling Gollek)
  - Fail hard: `true` (Wayang will crash if Gollek is unreachable)

## gRPC Strategy
Under the hood, when Gollek is active, Wayang leverages the `GrpcGollekStrategy` located in `wayang-plugin-gollek`. This strategy maps Wayang's `ChatMessage` objects into the native `ChatRequest` protobufs generated from `gollek-sdk-protobuf`, ensuring highly efficient, HTTP/2 multiplexed streaming.

# Wayang Developer Documentation

Welcome to the **Wayang Platform**! Wayang is a robust, modular, and extensible foundation framework and runtime for building and orchestrating autonomous AI agents. 

## Overview
Wayang abstracts the complexities of Large Language Model (LLM) inference, tool execution, and state management, providing a unified Service Provider Interface (SPI) for developers to rapidly build and deploy intelligent agents.

### Core Architecture
- **Dependency Injection (CDI):** Wayang's kernel (`DefaultWayangKernel`) uses CDI (Contexts and Dependency Injection) via Weld/Quarkus Arc, making the platform highly modular. 
- **SPI First:** Everything from Inference Providers (like Gollek or Gemini) to individual Agent implementations (like ReAct) are loaded via the Java `ServiceLoader` SPI or CDI classpath scanning.
- **Gollek Backend Integration:** Wayang tightly integrates with [Gollek](https://github.com/bhangun/gollek) as its default local/remote inference backend. It natively supports auto-discovery, auto-download, and background daemon lifecycle management out of the box.

## Getting Started Guides

To help you leverage Wayang for your projects, we have prepared the following guides:

1. **[Agent Development Guide](./agent-development-guide.md)**
   Learn how to build and plug in your own custom agents (e.g., ReAct, Plan-and-Solve, Reflexion) using the `wayang-spi`.

2. **[Inference Provider Guide](./inference-provider-guide.md)**
   Learn how to add support for a new AI model provider (e.g., OpenAI, Anthropic, local LLaMA) by extending the provider SPI.

3. **[Gollek Integration & Configuration](./gollek-integration.md)**
   Understand how Wayang bootstraps the Gollek backend, the different run profiles (`local`, `development`, `production`), and how to customize the configuration in `gollek.yaml`.

## Project Structure

- `wayang-spi`: The core interfaces (`Agent`, `AgentPipeline`, `Provider`, etc.). **Your plugins should depend primarily on this module.**
- `wayang-core` / `wayang-runtime`: The Wayang Kernel, event bus, and runtime registries.
- `wayang-cli`: The unified terminal interface and bootstrapper.
- `wayang-plugin-*`: Core bundled plugins (e.g., `wayang-plugin-gollek`).
- `Wayang-Agents` / `Wayang-Projects`: The directory meant for your proprietary or custom agent implementations built on top of this framework.

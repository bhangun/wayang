# Wayang Integration Modules

This directory contains individual modules for different integration providers that can be used with the Wayang platform.

## Available Modules

- **AWS** (`aws/`) - Integration with Amazon Web Services (S3, DynamoDB, SQS, SNS, Lambda)
- **Salesforce** (`salesforce/`) - Integration with Salesforce platform
- **Azure** (`azure/`) - Integration with Microsoft Azure services
- **GCP** (`gcp/`) - Integration with Google Cloud Platform services
- **Kafka** (`kafka/`) - Apache Kafka messaging integration
- **MongoDB** (`mongodb/`) - MongoDB database integration
- **PostgreSQL** (`postgresql/`) - PostgreSQL database integration
- **Redis** (`redis/`) - Redis caching and messaging integration

## Architecture

Each module is designed as a standalone component that can be included in the integration runtime as needed. The modules follow a consistent structure:

```
module-name/
├── pom.xml                 # Maven build configuration
├── src/main/java/          # Source code
├── src/main/resources/     # Configuration files
└── src/test/java/          # Unit tests
```

## Usage

The modules are aggregated in the `wayang-integration-runtime` which provides a unified runtime environment for executing integration workflows.

## Building

To build all modules:

```bash
mvn clean install
```

To build a specific module:

```bash
cd module-name
mvn clean install
```
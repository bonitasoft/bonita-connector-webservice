# Bonita Webservice Connector

## Project Overview

The Bonita Webservice Connector enables Bonita BPM processes to invoke SOAP/JAX-WS web services. It is packaged as a Bonita connector artifact (`.zip` + `.jar`) and deployed into the Bonita Engine runtime.

- **Artifact:** `org.bonitasoft.connectors:bonita-connector-webservice`
- **Current version:** `1.3.6-SNAPSHOT`
- **Java:** 11 (compiled), 17 (CI build)
- **License:** GPL-2.0
- **Main class:** `org.bonitasoft.connectors.ws.SecureWSConnector`

## Build Commands

```bash
# Full build + tests (default goal is verify)
./mvnw verify

# Skip tests
./mvnw verify -DskipTests

# Clean build
./mvnw clean verify

# Run only unit tests
./mvnw test

# Build with Sonar analysis (requires SONAR_TOKEN)
./mvnw clean verify sonar:sonar

# Deploy / release (requires GPG key and Central credentials)
./mvnw deploy -P deploy
```

The build produces:
- `target/bonita-connector-webservice-<version>.jar` — connector classes
- `target/bonita-connector-webservice-<version>-webservice.zip` — connector package for Bonita Studio/Engine
- `target/bonita-connector-webservice-<version>-all.zip` — all-dependencies assembly

## Architecture

### Source layout

```
src/
  main/
    java/org/bonitasoft/connectors/ws/
      SecureWSConnector.java          # single connector implementation class
    resources/                        # icons and i18n .properties files
    resources-filtered/
      webservice.def                  # connector definition (inputs, outputs, UI pages)
      webservice.impl                 # connector implementation descriptor
  assembly/
    webservice-assembly.xml           # zip assembly descriptor
    all-assembly.xml
  script/
    dependencies-as-var.groovy        # generates dependency list as a resource variable
  license/
    header.txt                        # GPL license header checked on every .java file
  test/
    java/org/bonitasoft/connectors/ws/
      SecureWSConnectorTest.java      # integration tests (start real Jetty+CXF server)
      SecureWSConnectorUnitTest.java  # unit tests (no server)
      Server.java / ServerThread.java # embedded test server helpers
      hello/ helloHeader/ helloTimeout/ customer/  # test web service implementations
```

### Key design points

- `SecureWSConnector` extends `AbstractConnector` from `bonita-common`.
- Uses JAX-WS `Dispatch<Source>` in `Service.Mode.MESSAGE` — sends the raw SOAP envelope string as-is.
- Supports SOAP 1.1 and 1.2 (binding string controls this).
- Proxy configuration (HTTP/HTTPS/SOCKS) is applied to JVM system properties before invocation and restored afterwards.
- Invalid XML 1.0 control characters are stripped from the envelope before dispatch.
- Three outputs: `sourceResponse` (`javax.xml.transform.Source`), `responseDocumentEnvelope` (`org.w3c.dom.Document`), `responseDocumentBody` (`org.w3c.dom.Document`).

### Connector inputs (mandatory)

| Parameter | Type | Description |
|-----------|------|-------------|
| `serviceNS` | String | Service namespace URI |
| `serviceName` | String | Qualified service name |
| `portName` | String | Port name |
| `envelope` | String | Full SOAP envelope XML |
| `endpointAddress` | String | URL of the endpoint |
| `binding` | String | JAX-WS binding URI (e.g., `http://schemas.xmlsoap.org/wsdl/soap/http`) |

### Optional inputs

`soapAction`, `userName`, `password`, `oneWayInvoke`, `buildResponseDocumentEnvelope`, `buildResponseDocumentBody`, `printRequestAndResponse`, `httpHeaders`, `proxyProtocol`, `proxyHost`, `proxyPort`, `proxyUser`, `proxyPassword`.

## Testing

Tests use JUnit 4, AssertJ, and an embedded Apache CXF + Jetty server started on port 9002.

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=SecureWSConnectorUnitTest

# Run integration tests (require embedded server)
./mvnw test -Dtest=SecureWSConnectorTest
```

- `SecureWSConnectorUnitTest` — pure unit tests; no network, tests XML parsing helpers.
- `SecureWSConnectorTest` — integration tests; `@BeforeClass` starts the embedded CXF/Jetty server; tests cover basic auth, HTTP headers, timeouts, envelope sanitisation, one-way invocation.

When writing new tests:
- Prefer `SecureWSConnectorUnitTest` for logic that does not require a live server.
- For end-to-end scenarios add a new test service implementation under `src/test/java/.../ws/<service>/` and register it in `Server.java`.
- Use AssertJ assertions (`assertThat`).
- Follow the `should_<expected_behaviour>_when_<condition>` naming convention.

## Commit Format

This repository enforces [Conventional Commits](https://www.conventionalcommits.org/) via the `commit-message-check.yml` workflow.

```
<type>(<optional scope>): <description>

[optional body]

[optional footer(s)]
```

Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.

Examples:
```
feat: support SOAP 1.2 binding
fix: restore proxy settings after connector execution
chore: upgrade jaxws-rt to 2.3.6
ci: add Claude Code review workflow
```

## Release Process

Releases are triggered manually via the **Release** GitHub Actions workflow (`release.yml`):

1. Go to **Actions → Release → Run workflow**.
2. Enter the release version (e.g., `1.3.6`).
3. The reusable workflow `bonitasoft/github-workflows/.github/workflows/_reusable_release_connector.yml` handles:
   - Version bump in `pom.xml`
   - Git tag creation
   - Maven build + GPG signing
   - Publishing to Maven Central via the Sonatype Central Publishing plugin
   - GitHub Release creation with the connector zip attached

Secrets required for release: `KSM_CONFIG` (Keeper Secrets Manager config that vends `SONAR_TOKEN`, GPG key, and Central credentials).

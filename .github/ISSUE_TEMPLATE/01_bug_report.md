---
name: "Bug Report"
about: Report a reproducible issue with swagger-codegen-generators
title: "[Bug]: "
labels: Bug
assignees: ''
---

## Description

<!--
Clearly describe the problem you're experiencing.

Helpful to include:
- What generator or template is affected?
- What is the incorrect behavior?
- What did you expect instead?
-->

## Version

<!--
Specify the version of swagger-codegen-generators (e.g. v1.0.41).
-->

### Can you identify when the issue first appeared?

<!--
If known, specify the earliest version where the bug occurs.
-->

## Language / Generator Affected

<!--
Examples:
- java
- spring
- typescript-node
- go
-->

## OpenAPI/Swagger Spec

<!--
Include a minimal example spec (inline or via link) that causes the issue.
-->

```yaml
# Example spec snippet or reference URL
```

## Command Line or Configuration Used

<!--
Paste the command or configuration that runs the generator.
-->

```bash
swagger-codegen generate -g java -i example.yaml -o output
```

## Steps to Reproduce
<!-- 1. Use this spec: ... 2. Run this command: ... 3. Observe this incorrect output: ... -->

## Expected Behavior
<!-- Describe what you expected to happen. -->

## Actual Behavior
<!-- Describe what actually happened, and include relevant logs or output differences. -->

## Related Issues / Repos
<!-- If similar bugs exist, mention them. Example: "Looks like #123" or "Also occurs in swagger-codegen" -->

## Environment

    OS: <!-- e.g. Ubuntu 22.04, macOS 14, Windows 11 -->

    Java Version: <!-- e.g. OpenJDK 17 -->

    Build Tool: <!-- CLI / Maven / Gradle -->

    swagger-codegen-generators Version: <!-- e.g. v1.0.41 -->

## Additional Context
<!-- Add logs, screenshots, or any other helpful details. -->

## Checklist

- [ ] I have searched the [existing issues](https://github.com/swagger-api/swagger-codegen-generators/issues) to avoid duplicates.
- [ ] I have included a minimal reproducible spec or configuration.
- [ ] I have clearly described the steps to reproduce the issue.
- [ ] I have specified which generator or template is affected.
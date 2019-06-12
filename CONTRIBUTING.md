# Contributing

## Contributing to the HiveMQ Community Projects

Welcome to the HiveMQ Community! Glad to see your interest in contributing to HiveMQ MQTT Client.
Please checkout our [Contribution Guide](https://github.com/hivemq/hivemq-community/blob/master/CONTRIBUTING.adoc) to 
make sure your contribution will be accepted by the HiveMQ team.

For information on how the HiveMQ Community is organized and how contributions will be accepted please have a look at 
our [HiveMQ Community Repo](https://github.com/hivemq/hivemq-community). 

## Contributing to HiveMQ MQTT Client

### External contributors

If you would like to contribute code, do the following:
- Fork the repository on GitHub
- Open a pull request targeting the `develop` branch

### License

By contributing your code, you agree to license your contribution under the terms of the
[Apache License, Version 2.0](https://github.com/hivemq/hivemq-mqtt-client/blob/develop/LICENSE).

All files must contain the license header from the
[HEADER](https://github.com/hivemq/hivemq-mqtt-client/blob/develop/HEADER) file.

### Branching model

- `master`: release branch, protected
  - `develop` is merged into `master` by creating a merge commit if a new version is released
  - The release is tagged with the version `vX.Y.Z`
- `develop`: snapshot branch, protected
  - Contains features for the next release
  - Feature/bugfix/... branches are merged into `develop` by rebasing and merging
- Every feature/bugfix/... will have its own branch
  - Branched off from `develop`
  - Pull request targeting the `develop` branch
  - Mandatory code review of the pull request
- `gh-pages`: documentation branch, protected

### Branching guidelines

- Branch types: feature, bugfix, improvement, cleanup (same as the label of a corresponding GitHub Issue)
- Branch names:
  - Starting with type: `feature/`, `bugfix/`, `improvement/`, `cleanup/`
  - \+ task: lower case, spaces replaced with `-`

### Commit guidelines

- Commits should be as atomic as possible.
- Commit messages should describe the changes clearly.
- Commit messages should start with a capital letter for consistency.
- Commit messages should avoid exceeding the line length limit. Instead use multiple lines, each describing one specific
change.

### Code style guidelines

- The project uses Nullability annotations to avoid NullPointerExceptions: `@NotNull`, `@Nullable`.
Every non-primitive parameter/return type/field should be annotated with one of them.
- For IntelliJ IDEA the codeStyleConfig and the inspectionProfile are provided in the `.idea` folder.

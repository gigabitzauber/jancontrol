#!/bin/bash

# Get latest git tag and remove "v" prefix
# --tags: include all tags
# --abbrev=0: return only the tag name
# sed 's/^v//': remove leading 'v' if present
LATEST_GIT_TAG=$(git describe --tags --abbrev=0 | sed 's/^v//')

# Get current project version from Maven project
# -q: quiet mode
# -DforceStdout: output only the requested value
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# If project version is a snapshot -> error
if [[ "$PROJECT_VERSION" == *-SNAPSHOT ]]; then
  echo "Error: project version '$PROJECT_VERSION' is a SNAPSHOT." >&2
  exit 1
fi

# If project version and git tag are not equal -> error
if [[ "$PROJECT_VERSION" != "$LATEST_GIT_TAG" ]]; then
  echo "Error: Version mismatch! Latest git tag is '$LATEST_GIT_TAG', but project version is '$PROJECT_VERSION'." >&2
  exit 1
fi

echo "$PROJECT_VERSION"
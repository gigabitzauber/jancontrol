#!/bin/bash

set -euo pipefail

# Usage: calc_next_version <version> <part>
# part: major, minor, or patch
calc_next_version() {
    local version=$1
    local part=$2

    # Split the version into an array
    IFS='.' read -r -a parts <<< "$version"

    case "$part" in
        major)
            ((parts[0]++))
            parts[1]=0
            parts[2]=0
            ;;
        minor)
            ((parts[1]++))
            parts[2]=0
            ;;
        patch)
            ((parts[2]++))
            ;;
    esac

    echo "${parts[0]}.${parts[1]}.${parts[2]}"
}

bump_version() {
  mvn versions:set -DnewVersion="$1" -DgenerateBackupPoms=false
  # Update README.md
  sed -i -E "s|java -jar jancontrol-[^[:space:]]+\.jar <config-file>|java -jar jancontrol-$1.jar <config-file>|" README.md

  git add pom.xml
  git add README.md
  git commit -m "$2"
}


if [[ -v GITHUB_ACTIONS ]]; then
  echo "This script is supposed to be run locally. Exiting."
  exit 1
fi

orig_branch=$(git branch --show-current)

if [ -z "$orig_branch" ]; then
  echo "Not in a git repository or in a detached HEAD state."
  exit 1
elif [ "$orig_branch" != "develop" ] && [ "$orig_branch" != "patch" ]; then
  echo "Can only prepare a release from the 'develop' or 'patch' branch. Current branch is '$orig_branch'."
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "There are uncommitted changes or untracked files left."
  exit 1
fi

# Get current project version from Maven project
# -q: quiet mode
# -DforceStdout: output only the requested value
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
if [[ "$PROJECT_VERSION" != *-SNAPSHOT ]]; then
  echo "Error: project version '$PROJECT_VERSION' is a release version already." >&2
  exit 1
fi

# Remove SNAPSHOT suffix if present
RELEASE_VERSION=${PROJECT_VERSION%-SNAPSHOT}

echo "Pushing release ${RELEASE_VERSION}.."
bump_version "$RELEASE_VERSION" "Release $RELEASE_VERSION"

git checkout main
git merge $orig_branch
git tag -a "v$RELEASE_VERSION" -m "Release $RELEASE_VERSION"
git push origin main --follow-tags

git checkout $orig_branch
if [ "$orig_branch" == "develop" ]; then
  NEXT_VERSION="$(calc_next_version $RELEASE_VERSION minor)-SNAPSHOT"
elif [ "$orig_branch" == "patch" ]; then
  NEXT_VERSION="$(calc_next_version $RELEASE_VERSION patch)-SNAPSHOT"
else
  echo "Unexpected branch '$orig_branch'. Exiting."
  exit 1
fi
echo "Pushing new snapshot version ${NEXT_VERSION}.."

bump_version "$NEXT_VERSION" "Next snapshot: ${NEXT_VERSION}"
git push origin $orig_branch

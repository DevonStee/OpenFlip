# F-Droid Submission Guide

This guide explains how to finalize the submission of OpenFlip to the official F-Droid repository.

## 1. Finalize the Metadata
The metadata template is located at `metadata/com.bokehforu.openflip.yml`.
Ensure the `commit` field matches the git tag you will create for the release.

## 2. Create a Git Tag
F-Droid builds from git tags. Tag the current version:
```bash
git tag -a v0.6.0-beta -m "v0.6.0-beta release for F-Droid"
git push origin v0.6.0-beta
```

## 3. Submit to fdroiddata
1. Fork the [fdroiddata](https://gitlab.com/fdroid/fdroiddata) repository on GitLab.
2. Create a new branch.
3. Add the `metadata/com.bokehforu.openflip.yml` file to the `metadata/` directory in that repo.
4. Commit and push: `git add metadata/com.bokehforu.openflip.yml`.
5. Open a Pull Request (Merge Request) on GitLab.

## 4. Maintenance
- **Update Checks**: F-Droid will automatically check for new tags based on the `UpdateCheckMode: Tags` configuration in the YAML.
- **Reproducible Builds**: If you want the "Reproducible Build" badge, ensure your local build matches F-Droid's build environment perfectly (see F-Droid documentation for more details).

---
**Note**: The app does not require INTERNET permission, which makes the review process much faster.

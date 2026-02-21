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

## FAQ & Troubleshooting

### 1. Do I need to upload my project to GitLab?
**No.** Your project stays on GitHub. The metadata file (`metadata/com.bokehforu.openflip.yml`) you already have contains the line `Repo: https://github.com/DevonStee/OpenFlip.git`. F-Droid's build server will automatically pull the code from your GitHub repository.

### 2. What is the role of GitLab?
GitLab hosts the `fdroiddata` repository, which is a giant collection of "recipes" (YAML files) for all apps on F-Droid. You are simply adding your "recipe" to their list.

### 3. I can't log into GitLab. Is there another way?
Unfortunately, **GitLab login is mandatory** for both creating a Merge Request and opening an Issue. F-Droid does not have a public submission form that doesn't require an account. 
- If you have trouble logging in, try using a different browser or social login (Google/GitHub/Bitbucket).
- Creating a new account on GitLab is free and fast.

### 4. How to Fork?
Once logged in, navigate to [fdroiddata](https://gitlab.com/fdroid/fdroiddata) and look for the **"Fork"** button (usually in the top right near the Star button).

---
**Note**: The app does not require INTERNET permission, which makes the review process much faster.

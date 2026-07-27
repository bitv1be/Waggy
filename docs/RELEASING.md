# Releasing Waggy

The `Release Waggy` GitHub Actions workflow builds a signed APK, runs the local
tests and Android lint, and publishes the APK with its SHA-256 checksum in a
GitHub Release after every update to the `main` branch. GitHub Releases are the
app's update source; no app store is involved.

## One-time repository setup

Create a production upload keystore and keep it outside the repository. Add the
following repository Actions secrets under **Settings → Secrets and variables →
Actions**:

| Secret                        | Value                                     |
| ----------------------------- | ----------------------------------------- |
| `SIGNING_KEY`                 | Base64-encoded upload keystore            |
| `KEY_STORE_PASSWORD`          | Keystore password                         |
| `ALIAS`                       | Signing key alias                         |
| `KEY_PASSWORD`                | Signing key password                      |
| `GOOGLE_SERVICES_JSON_BASE64` | Base64-encoded `app/google-services.json` |

`BASE64_SECRET` is optional. If set, it must contain the base64-encoded
production `.env`; otherwise, the workflow uses `.env.example`.

Encode a file as one line on Linux:

```bash
base64 -w 0 release.jks
base64 -w 0 app/google-services.json
```

On macOS:

```bash
base64 < release.jks | tr -d '\n'
base64 < app/google-services.json | tr -d '\n'
```

Never commit the keystore, passwords, or Firebase configuration.

## Publish a version

Merge a pull request into `main`. The merge automatically starts the release
workflow; direct pushes to `main` trigger it as well.

The workflow derives both Android versions from its monotonically increasing
GitHub Actions run number:

- `versionName`: `1.0.<run_number>`;
- `versionCode`: `<run_number>`.

For example, workflow run 42 publishes version `1.0.42` with version code `42`.
The workflow refuses to overwrite an existing `v<versionName>` tag, signs the
APK, creates the tag and GitHub Release, and attaches:

- `Waggy-<versionName>.apk`;
- `SHA256SUMS.txt`.

Installed builds check this public GitHub Release from the Settings screen,
download a newer APK directly, and then open Android's package installer. The
user must approve installation, and Android only accepts the update when it is
signed with the same production key as the installed app.

Use **Re-run jobs** on the failed workflow run to retry the same version without
allocating a new version code.

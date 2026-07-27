# Releasing Waggy

The `Release Waggy` GitHub Actions workflow builds a signed APK and AAB, runs
the local tests and Android lint, optionally uploads the AAB to a RuStore draft,
and publishes the binaries with SHA-256 checksums in a GitHub Release.

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
| `RUSTORE_KEY_ID`              | RuStore API key ID                        |
| `RUSTORE_PRIVATE_KEY`         | RuStore private API key                   |

`BASE64_SECRET` is optional. If set, it must contain the base64-encoded
production `.env`; otherwise, the workflow uses `.env.example`.

The RuStore secrets are only required when `publish_to_rustore` is enabled.

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

1. Merge the version's code into the default branch.
2. Open **Actions → Release Waggy → Run workflow**.
3. Select the default branch and enter:
   - `version_name`: a semantic version such as `1.1.0`;
   - `version_code`: a positive integer greater than the previous store release;
   - `whats_new`: the RuStore release notes;
   - `publish_to_rustore`: whether to upload the AAB to a RuStore draft;
   - `prerelease`: enable only for alpha, beta, or release-candidate builds.
4. Run the workflow.

The workflow refuses to publish from another branch or overwrite an existing
`v<version_name>` tag. It passes the entered version to Gradle, signs both
outputs, optionally uploads the AAB to RuStore, creates the tag and GitHub
Release, and attaches:

- `Waggy-<version_name>.apk`;
- `Waggy-<version_name>.aab`;
- `SHA256SUMS.txt`.

If a store release has already used a `version_code`, choose a larger one
before rerunning the workflow.

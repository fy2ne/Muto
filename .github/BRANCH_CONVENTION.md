# Branch Convention

| Branch | Minecraft Versions | Status |
|--------|-------------------|--------|
| `26.x` | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | active |
| `1.21.x` (future) | 1.21, 1.21.1, 1.21.4 | planned |
| `1.20.x` (future) | 1.20.1 | planned |

Each branch produces one jar covering its entire MC minor range. Version convention: `{mod_version}+{mc_range}` (e.g. `0.3.0-beta+26.x`).
## Workflow

- 26.x is the default and the only branch releases are cut from. main mirrors it.
- Work happens on short-lived branches: `feat/<name>`, `fix/<name>`, `docs/<name>`, merged into the active `<mc>.x` branch by PR.
- Releases are tagged `v<mod_version>` on the `<mc>.x` branch (e.g. `v0.3.0-beta`).
- Muto Menu ([fy2ne/Muto-Menu](https://github.com/fy2ne/Muto-Menu)) uses the same branch names and the same `mod_version`. Menu requires `muto ~<major.minor>`.


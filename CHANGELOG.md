# CHANGELOG — IZ Play V3

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).

---

## [Não publicado] — branch `codex/v3-bootstrap`

### Etapa 0 — Base preservada e compilável · 2026-07-21

Tag: **`baseline/another-functional`**

Nenhuma mudança visual. O aplicativo tem exatamente a mesma aparência e o mesmo
comportamento do Another IPTV Player no commit `f71a552`.

#### Adicionado

- `CredentialRedactor` + testes: mascara usuário e senha do provedor em todos os
  logs (query, JSON e path Xtream) — `d90e23e`
- Documentação do projeto: `README_IZPLAY_V3.md`, `ARCHITECTURE.md`, `BUILD.md`,
  `STATUS.md`, `CHANGELOG.md`, `DESIGN_SYSTEM.md`, `MIGRATION_PLAN.md`,
  `LICENSES.md`, `THIRD_PARTY_NOTICES.md`, `TESTING.md`

#### Alterado

- Pacote e identidade movidos de `dev.android.anotheriptvplayer` para
  `com.izplay.v3`, incluindo símbolos JNI do MPV — `04cd7bf`
- Armazenamento local isolado de versões anteriores: banco `izplay-v3.db` e
  prefixo `izplay_v3_*` em todas as SharedPreferences — `6dfa354`
- Identidade do produto: nome "IZ Play", projeto `IZPlayV3`, artefato
  `IZPlay-V3-<variant>.apk`, versionName `3.0.0` — `0b0ecf4`

#### Corrigido

- Ícones adaptativos movidos para o qualificador válido `mipmap-anydpi-v26`;
  em `mipmap-anydpi` o sistema os ignorava — `099b24e`

#### Decisões

- `minSdk` mantido em **26** (paridade com o upstream). Uma alteração local
  anterior para 24, com core library desugaring, foi revertida antes do primeiro
  commit e não entrou no histórico.
- Player MPV mantido como único motor de reprodução.

#### Não alterado

Motor funcional, integração Xtream, parsing M3U, repositories, models, DAOs,
schema Room, navegação, ciclo de vida do player, PiP, downloads. A base
funcional herdada permanece intacta.

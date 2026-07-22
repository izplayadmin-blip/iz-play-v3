# CHANGELOG — IZ Play V3

Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).

---

## [Não publicado] — branch `codex/v3-bootstrap`

### Etapa 3 (início) — Splash + cadastro de playlist · 2026-07-22

#### Adicionado

- Splash da marca IZ Play (`ui/splash/SplashScreen.kt`): fundo preto, logo
  centralizado, fade+escala curtos; roteia uma vez para dashboard/lista
  conforme o estado, sem flash — `feat(splash)`
- `IzLogo`: wordmark "IZ PLAY" em Compose (código próprio; trocar pelo asset
  oficial após confirmar autoria/licença) — `feat(splash)`
- `windowBackground` preto em `Theme.IZPlay`: elimina o flash branco no
  arranque — `feat(splash)`

#### Alterado

- Identidade IZ Play aplicada às telas de cadastro de playlist
  (`PlaylistScreen`, `AddXtreamPlaylistScreen`, `AddM3UPlaylistScreen`) via
  `IzTheme` + `IzButton` — **só apresentação, lógica preservada** — `feat(ui)`
- Lógica de retomada movida (sem mudança de comportamento) para a Splash, para
  eliminar o flash de "No playlists yet"

#### Verificado (dispositivo API 25)

- Splash → dashboard sem flash branco nem flash de lista vazia
- Lista de playlists, sheet "Add Playlist" e form "New Xtream Playlist" na
  identidade IZ Play; botão voltar correto (form → lista → sai), sem crash
- Playlist real e pref de sessão do usuário **preservadas** (pref limpa
  temporariamente só para captura, restaurada ao valor exato)
- `assembleDebug` + `testDebugUnitTest` verdes; lint = mesmos 5 erros herdados
- Reprodução de conteúdo permanece **bloqueada** (sem credenciais autorizadas)

#### Não migrado nesta etapa (conforme escopo)

Home, sidebar funcional, catálogo, detalhes e player — permanecem como estão.

### Etapa 2 — Design system IZ Play V3 · 2026-07-22

#### Adicionado

- Tokens da marca em `ui/design/tokens/` — cores (incluindo os dois amarelos
  com papéis fixos: `#F5A623` AO VIVO/HOT, `#F39C12` alerta; primária `#CC0000`),
  espaçamento, raios, elevação, bordas, dimensões, tipografia, gradientes,
  durações, escalas de foco e opacidades — `feat(theme)`
- `IzTheme` — tema Material 3 dark isolado, derivado dos tokens (não substitui o
  tema das telas funcionais) — `feat(theme)`
- `izFocusVisuals` — realce de foco de TV (escala animada + borda vermelha),
  lendo a interaction source do próprio item — `feat(tv)`
- 14 componentes `Iz*` em `ui/design/components/`: `IzButton`, `IzBadge`,
  `IzProgressBar`, `IzLoading`/`IzShimmerBox`, `IzEmptyState`, `IzErrorState`,
  `IzDialog`, `IzTvCard`, `IzPosterCard`, `IzChannelCard`, `IzContentRow`,
  `IzHero`, `IzSidebar`, `IzNavigationItem` — cada um com `@Preview` — `feat(ui)`
- Galeria de inspeção **debug-only** (`IzGalleryActivity`) para render e teste de
  foco em dispositivo real; não entra em release — `feat(ui)`

#### Verificado

- `assembleDebug` verde, `testDebugUnitTest` 4/4 verde
- Lint: **os mesmos 5 erros herdados**, zero novo, zero em `ui/design`, zero `NewApi`
- Render real no rk322x (API 25): identidade IZ Play correta (fundo preto,
  primária vermelha, dois amarelos distintos), foco de TV com borda vermelha
  confirmado no botão secundário
- **Nenhuma tela funcional alterada**; nenhuma dependência nova; `minSdk 24` mantido

### minSdk 24 + smoke test no dispositivo · 2026-07-21

#### Alterado

- `minSdk` de 26 para **24** para cobrir TV boxes rk322x (API 25), com core
  library desugaring restaurado para `java.time` — `0902b86`

#### Verificado

- Auditoria de lint (`NewApi`, minSdk 24): **zero** uso de API 26+ sem guarda
- `assembleDebug` verde, `testDebugUnitTest` 4/4 verde, APK reporta minSdk 24
- `adb install -r` no rk322x MCD-121 (API 25): **Success**
- Smoke test sem credenciais: abertura, navegação (Live TV/Movies/Series/
  Settings/Search), botão voltar, persistência em cold start — **sem crash**
- Testes dependentes de servidor permanecem **bloqueados** (sem credenciais)

#### Infra

- Removida a pasta órfã `C:\Users\deivi\izplay-base` (worktree do upstream, já
  desregistrado; APK e evidências preservados em `izplay-baselines/`)

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

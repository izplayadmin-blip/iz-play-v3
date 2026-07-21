# ARCHITECTURE — IZ Play V3

## Princípio

**Uma única arquitetura.** O motor funcional é o do Another IPTV Player e não
será substituído. O redesign troca somente a camada que o usuário vê.

Proibido: dois bancos, dois sistemas Xtream, dois históricos, dois sistemas de
favoritos, dois players sem abstração, duas navegações, dois sistemas de
autenticação, models ou repositories duplicados, camada funcional específica do
Streamix ou do IZ Play V2, telas antigas e novas em paralelo sem necessidade.

## Camadas

```
┌───────────────────────────────────────────────────────────┐
│  UI — Jetpack Compose                                     │
│  identidade IZ Play · componentes Iz* · foco de TV        │  ← redesign
├───────────────────────────────────────────────────────────┤
│  ViewModels + adapters de UI                              │  ← só adapters novos
├───────────────────────────────────────────────────────────┤
│  Repositories · Models · Room DAOs · DataStore/Prefs      │  ← INTOCADO
├───────────────────────────────────────────────────────────┤
│  Networking (Xtream, M3U)  ·  Player MPV (JNI/CMake)      │  ← INTOCADO
└───────────────────────────────────────────────────────────┘
```

## Estrutura de pastas

```
apps/android/app/src/main/
├── cpp/                       CMakeLists.txt + mpv_jni.cpp  (bridge JNI)
├── jniLibs/<abi>/             libmpv.so, libmediakitandroidhelper.so (não versionados)
└── java/com/izplay/v3/
    ├── IZPlayApp.kt           Application — dono das dependências
    ├── MainActivity.kt
    ├── data/                  repositories, stores, downloads, filtros
    │   └── local/             Room: AppDatabase, entidades, 15 DAOs
    ├── model/                 models de domínio
    ├── networking/            XtreamApiClient, XtreamModels, M3UParser, M3UService
    ├── player/                MPVLib (JNI), MPVPlayer, MPVSurfaceView,
    │                          PictureInPicture, PlayerAudioFocus, SubtitleAppearance
    ├── util/                  CredentialRedactor
    └── ui/
        ├── AppNavigation.kt   navegação única
        ├── LocalRepositories.kt
        ├── design/            ← NOVO na Etapa 2: tokens/, components/, focus/
        ├── components/  dashboard/  playlist/  search/
        ├── favorites/   history/    downloads/ settings/  player/
        └── theme/
```

Não haverá pasta `streamix/` nem `v2/`. Nenhum módulo externo é adicionado ao
build sem justificativa de necessidade e verificação de licença.

## Dados

- **Banco:** Room, arquivo `izplay-v3.db`, schema exportado em `app/schemas/`
  para permitir diff em migrações futuras
- **Preferências:** SharedPreferences com prefixo `izplay_v3_*`
- Nenhum banco, cache, preferência ou credencial é compartilhado com o IZ Play V2

## Rede

`XtreamApiClient` (OkHttp + kotlinx.serialization) fala direto com o servidor do
provedor. `M3UParser`/`M3UService` cobrem M3U/M3U8. **Não existe backend próprio
do IZ Play** e nenhum será introduzido nesta fase.

Todo log que contenha URL passa por `CredentialRedactor`.

## Autenticação

"Login" no IZ Play V3 significa **cadastro das credenciais do provedor**:

- Xtream: URL do servidor, usuário e senha
- M3U/M3U8: URL ou arquivo

Não existe conta IZ Play, autenticação por e-mail, backend de usuários nem
segundo sistema de autenticação.

A arquitetura permite evoluir futuramente para DNS configurado remotamente,
múltiplos servidores e fallback de DNS — sem acoplar o app ao painel antigo sem
especificação clara.

## Player

**MPV é o único motor**, via `libmpv.so` e o bridge JNI em `mpv_jni.cpp`
(símbolos `Java_com_izplay_v3_player_MPVLib_*`).

Preservados sem alteração: reprodução, pausa, seek, faixas de áudio, legendas,
proporção, qualidade, histórico, continuar assistindo, próximo episódio, PiP,
tratamento de erro, retomada de posição, liberação de recursos, foco de áudio e
ciclo de vida.

Apenas a **camada visual dos controles** será redesenhada. Media3 ou VLC como
fallback ficam documentados para fase posterior — não se adiciona um segundo
player antes de o MPV estar validado.

⚠️ A renomeação de pacote exigiu renomear os símbolos JNI. Um erro aqui não
quebra o build: quebra em runtime. Qualquer mudança futura de pacote deve
atualizar `mpv_jni.cpp` no mesmo commit.

## Segurança

- Sem senhas, tokens, URLs privadas ou endpoints no código
- Sem credenciais no Git; `local.properties` e keystores ignorados
- Logs sanitizados por `CredentialRedactor`
- `usesCleartextTraffic` hoje é global (herdado) — restringir via Network
  Security Config antes do release
- Sem dados reais em fixtures

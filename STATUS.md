# STATUS — IZ Play V3

Última atualização: **2026-07-21** · Branch `codex/v3-bootstrap` · Tag `baseline/another-functional`

Este documento registra apenas o que foi **observado com evidência**. Nada é
declarado como testado sem prova. Segredos e credenciais nunca são registrados
aqui — apenas a informação de disponibilidade.

---

## Resumo da Etapa 0

Objetivo: preservar a base funcional herdada do Another IPTV Player, isolar o
produto como `com.izplay.v3` e provar que compila — **sem nenhuma mudança
visual**.

Resultado: **concluída**. Dois baselines compilados, tag criada, zero perda de
trabalho local, nada enviado para nenhum remoto.

---

## ✅ Funcionando (com evidência)

| Item | Evidência |
|---|---|
| Build debug do IZ Play V3 | `assembleDebug` BUILD SUCCESSFUL, 50 tasks |
| Build nativo JNI/MPV (CMake) | `buildCMakeDebug` verde nas 3 ABIs |
| Testes unitários | 4 testes, 0 falhas, 0 erros |
| Auditoria de API (lint `NewApi`, minSdk 24) | **zero chamadas de API 26+ sem guarda** |
| Empacotamento das ABIs | APK contém `arm64-v8a`, `armeabi-v7a`, `x86_64` |
| Identidade do app | `aapt2 dump badging`: `com.izplay.v3`, versionName `3.0.0`, **minSdk 24**, targetSdk 36 |
| **Instalação no dispositivo API 25** | `adb install -r` → **Success** no rk322x MCD-121 |
| **Abertura / ausência de crash** | app abre em `MainActivity`, processo vivo, **zero FATAL/ANR** |
| **Navegação (sem credenciais)** | Live TV, Movies, Series, Settings, Search — todas carregam |
| **Botão voltar** | volta sem sair do app nem crashar |
| **Persistência (cold start)** | após `force-stop` + reabrir, Home e "Continue watching" restaurados |
| Build do upstream puro | `assembleDebug` BUILD SUCCESSFUL em worktree isolado no commit `f71a552` |
| Preservação do trabalho local | 100 arquivos comparados byte a byte com o backup |
| Ausência de segredos nos commits | varredura do diff `main..HEAD`: só fixtures de teste com domínio reservado `provider.test` |

## 🟡 Parcialmente funcionando

Nada nesta categoria na Etapa 0.

## ⏳ Pendente

- ~~Design system IZ Play V3 (Etapa 2)~~ ✅ concluído 2026-07-22 (tokens + 14
  componentes `Iz*` isolados em `ui/design`, com galeria debug e foco de TV
  verificado em dispositivo real). Ver `DESIGN_SYSTEM.md`.
- Splash, sidebar, Home, migração visual das telas (Etapa 3)
- Suporte a Android TV: `LEANBACK_LAUNCHER`, banner, D-pad, foco (Etapa 4)
- Ícone e banner próprios — hoje o app ainda usa o adaptive icon genérico do template
- `CHANGELOG.md` a cada tela migrada
- CI (GitHub Actions) e proteção de branch
- Push do branch `codex/v3-bootstrap` e da tag — **aguardando autorização**

## 🔴 Bloqueado

### Bloqueio A — RESOLVIDO (2026-07-21): minSdk 24

A instalação inicial falhou com `minSdk 26`
(`INSTALL_FAILED_OLDER_SDK`, dispositivo API 25). Após decisão autorizada,
`minSdk` foi baixado para **24** (commit `0902b86`), com auditoria de lint
confirmando zero uso de API 26+ sem guarda. **Instalação e smoke test agora
passam** — ver a tabela "Funcionando" acima.

Dispositivo de teste:

| Dispositivo | `192.168.15.23:5555` |
|---|---|
| Fabricante / modelo | rockchip / MCD-121 (`rk322x_box`) |
| API real | 25 (Android 7.1) |
| ABI | `armeabi-v7a` |
| Características | `box` — **sem** `android.software.leanback` |

Sem alterações no aparelho: o `com.izplay.tv` (IZ Play V2, 2.1.11) presente não
foi tocado — e confirma a necessidade do isolamento de storage (`6dfa354`).

### Bloqueio B — ausência de credenciais

| Teste | Motivo do bloqueio |
|---|---|
| Cadastro de playlist Xtream | Sem credenciais de provedor autorizadas |
| Cadastro/importação M3U | Sem URL/arquivo de teste autorizado |
| Abertura de canais ao vivo | Depende de credenciais |
| Filmes / séries / temporadas / episódios | Depende de credenciais |
| Reprodução no player MPV | Depende de credenciais e conteúdo legal de teste |
| Áudio, legendas, PiP, continuar assistindo | Depende de reprodução |
| Busca, favoritos, histórico, downloads | Depende de catálogo carregado |

**Nenhum desses itens pode ser declarado funcional ou defeituoso** enquanto não
houver servidor, credenciais autorizadas e conteúdo de teste legal.

Observação: o usuário relatou que a base upstream abre canais corretamente. Isso
é um relato válido sobre o **upstream**, não uma verificação feita neste
ambiente sobre o binário do V3. Por isso permanece bloqueado aqui.

## 🐞 Erros conhecidos

| Item | Gravidade | Nota |
|---|---|---|
| Strings ainda em turco em Settings e Search (ex.: "Kategoriler, filmler...", "Kanal, film veya dizi") | média | Herdado do upstream; faltam traduções pt/en. Corrigir na migração visual dessas telas |
| 5 erros de lint pré-existentes (ScaffoldPadding ×2, ByteOrderMark, ForegroundServiceType, PropertyEscape do `local.properties`) | baixa | Herdados do upstream / arquivo local; nenhum é `NewApi`; não introduzidos pela mudança de minSdk |
| 2 warnings de deprecação Compose (`Icons.Filled.VolumeUp`, `Icons.Filled.PlaylistPlay`) | baixa | Herdados do upstream; usar as variantes `AutoMirrored` |
| Fixture de `@Preview` em `PlaylistScreen.kt` com credencial de aparência real | baixa | Código upstream, não alterado; trocar por valores neutros na migração visual dessa tela |
| Base 1 mês atrás do upstream (`f71a552` vs `ff061f0`) | informativo | Congelamento deliberado |

### Nota sobre orientação

O dispositivo de teste é um TV box HDMI (`mHdmiPlugged=true`), saída fixa em
landscape 1920×1080. Teste de rotação de tela **não se aplica** a esta classe de
hardware. Deve ser verificado em telefone/tablet na Etapa 3.

---

## Dispositivos

| Dispositivo | Estado |
|---|---|
| `192.168.15.23:5555` — rockchip MCD-121 (`rk322x_box`), API 25, `armeabi-v7a` | **instalado e smoke test aprovado** com o APK minSdk 24 |
| `ca1a4656` | não aparece mais na lista |
| Android TV / TV Box físico | não disponível nesta sessão |
| Telas 720p / 1080p / 4K | não testadas |

## Credenciais de teste

**Indisponíveis.** Nenhuma credencial de provedor foi fornecida, solicitada ou
armazenada. Nenhum valor de credencial aparece neste repositório.

---

## Artefatos

Os APKs ficam **fora do repositório**, em `C:\Users\deivi\Desktop\izplay-baselines\`:

| Arquivo | Commit | Tamanho | SHA-256 |
|---|---|---|---|
| `IZPlay-V3-debug_baseline-another-functional_04cd7bf-0b0ecf4.apk` | `0b0ecf4` | 61 479 621 B | `b125310af8b9f026d8b720f23d5b0e55eece4bbccfd03361024d78d9e77c75c3` |
| `AnotherIPTVPlayer-upstream-debug_f71a552.apk` | `f71a552` | 61 479 661 B | `c22f8a2b58390c2be8ac5de79945d4225f7c735ce83881da8d4d4791c5f0ce77` |

A diferença de 40 bytes entre os dois é consistente com uma renomeação de
pacote e nada mais — nenhum código funcional foi alterado.

## Backups fora do repositório

- `.so` do MPV (6 arquivos, 3 ABIs) com SHA-256 registrado — **não versionados**
  e não reproduzíveis sem a toolchain do `Vendor/libmpv-android/Makefile`
- Cópia integral da working tree pré-commits (100 arquivos)

## Ambiente de build verificado

| Componente | Valor |
|---|---|
| JDK | OpenJDK 21.0.10 (`jbr` do Android Studio) |
| Android SDK | platforms 34/35/36/36.1 · build-tools 34.0.0→37.0.0 |
| NDK | 27.0.12077973 |
| CMake | 3.22.1 |
| `local.properties` | criado localmente, ignorado pelo Git |

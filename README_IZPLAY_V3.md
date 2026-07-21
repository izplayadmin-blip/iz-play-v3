# IZ Play V3

Aplicativo IPTV para Android e Android TV.

**Estado atual:** Etapa 0 concluída — base funcional preservada, isolada como
`com.izplay.v3` e compilando. **Ainda sem nenhuma mudança visual.**

---

## O que é

O IZ Play V3 é a transformação visual do
[Another IPTV Player](https://github.com/bsogulcan/another-iptv-player) na
identidade do IZ Play. O motor funcional — Xtream, M3U, catálogo, downloads,
banco e player MPV — é o do Another e **não será substituído**.

```
Another IPTV Player  →  motor funcional        (por baixo)
Streamix Android     →  referência conceitual  (no meio, sem copiar código)
IZ Play V2           →  identidade visual      (por cima)
IZ Play V3           →  produto final
```

O objetivo não é uma base nova. É **preservar o que funciona e redesenhar o que
o usuário vê**.

## Documentação

| Arquivo | Conteúdo |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | camadas, estrutura de pastas, regras de arquitetura |
| [BUILD.md](BUILD.md) | pré-requisitos, comandos, bibliotecas nativas do MPV |
| [STATUS.md](STATUS.md) | o que funciona, o que está pendente, o que está bloqueado |
| [CHANGELOG.md](CHANGELOG.md) | histórico de mudanças |
| [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) | tokens e componentes da identidade IZ Play |
| [MIGRATION_PLAN.md](MIGRATION_PLAN.md) | etapas, referências consultadas, decisões |
| [TESTING.md](TESTING.md) | checklists e regras de evidência |
| [LICENSES.md](LICENSES.md) | licenças dos projetos envolvidos |
| [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) | avisos de terceiros — **contém item crítico sobre o libmpv** |

## Build rápido

```bash
cd apps/android
# crie local.properties com sdk.dir (ver BUILD.md)
./gradlew.bat :app:assembleDebug
```

⚠️ As bibliotecas nativas do MPV **não estão no Git**. Sem elas o APK compila
mas o player falha em runtime. Ver [BUILD.md](BUILD.md).

## Identidade

| | |
|---|---|
| Nome | IZ Play |
| Projeto | IZPlayV3 |
| Application ID | `com.izplay.v3` |
| Artefato | `IZPlay-V3-<variant>.apk` |
| minSdk / targetSdk | 26 / 36 |

Banco, cache, preferências e assinatura são totalmente separados do IZ Play V2.

## Git

| Remote | URL |
|---|---|
| `origin` | https://github.com/izplayadmin-blip/iz-play-v3.git |
| `upstream` | https://github.com/bsogulcan/another-iptv-player.git — **somente leitura** |

Tag `baseline/another-functional` marca a base funcional intacta. Qualquer
commit posterior pode mudar como o app **parece**; se mudar como o app
**funciona**, é regressão contra essa tag.

## Licença

Obra derivada do Another IPTV Player (MIT) — ver [LICENSES.md](LICENSES.md). A
licença própria do IZ Play V3 ainda **precisa ser definida** antes de qualquer
distribuição pública.

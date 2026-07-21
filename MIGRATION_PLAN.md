# MIGRATION_PLAN — IZ Play V3

## Regra de composição

```
Another IPTV Player  →  motor funcional, dados, player, reprodução   (POR BAIXO)
Streamix Android     →  referência conceitual de componentes Compose  (NO MEIO)
IZ Play V2           →  identidade visual oficial                     (POR CIMA)
IZ Play V3           →  produto final independente
```

Uma única arquitetura. Um banco, um conjunto de models, um sistema de
navegação, um player. Componentes visuais novos consomem os ViewModels e
repositories herdados do Another — nunca uma camada paralela.

---

## Referências consultadas

Congeladas nos SHAs abaixo. **Não sincronizar automaticamente** com mudanças
futuras destes projetos.

| Projeto | Papel | Branch | Commit SHA | Consultado em |
|---|---|---|---|---|
| [bsogulcan/another-iptv-player](https://github.com/bsogulcan/another-iptv-player) | Base funcional | `main` | **`f71a552ff158be95639a9afe6dc00dd6fabb43d8`** (base adotada) | 2026-07-21 |
| ” | ” | `main` | `ff061f0c9f16b7a988659dcd130986834695e97e` (HEAD na data; **não incorporado**) | 2026-07-21 |
| [gabrielmaialva33/streamix-android](https://github.com/gabrielmaialva33/streamix-android) | Referência conceitual de UI | `main` | `05325803e9867579852931a12241d2e0b9f029e1` | 2026-07-21 |
| [izplayadmin-blip/iz-play](https://github.com/izplayadmin-blip/iz-play) | Identidade visual (V2) | `main` | `6f3b4b111feb5c8f51a6102f8c3a3821cd64f01c` | 2026-07-21 |
| [dereferencex/prysm](https://github.com/dereferencex/prysm) | Referência conceitual D-pad/TV | `main` | `99b815c92fd87ca451cb949adfb31ee3728d2c2c` | (não clonado) |

Streamix e IZ Play V2 foram clonados com `--depth 1` em pasta temporária,
somente leitura, fora do projeto. Nenhum arquivo foi copiado para o V3.
Consequência do clone raso: **não há histórico para auditar autoria de assets**
— ver `LICENSES.md`.

## Remotes

| Nome | URL | Uso |
|---|---|---|
| `origin` | https://github.com/izplayadmin-blip/iz-play-v3.git | repositório oficial do V3 |
| `upstream` | https://github.com/bsogulcan/another-iptv-player.git | **somente leitura — nunca fazer push** |

---

## Etapas

### Etapa 0 — Preservar e provar a base ✅ concluída

1. Backup externo dos `.so` do MPV e da working tree
2. Revert do `minSdk` para 26 (paridade upstream)
3. `origin` configurado, `upstream` preservado, branch `codex/v3-bootstrap`
4. 5 commits granulares (ver `CHANGELOG.md`)
5. Build do V3 + testes unitários verdes
6. Tag `baseline/another-functional`
7. Build do upstream puro em worktree isolado, para comparação

### Etapa 1 — Validação funcional 🔴 bloqueada

Depende de servidor, credenciais autorizadas e conteúdo legal de teste.
Ver `TESTING.md` e a seção "Bloqueado" de `STATUS.md`.

### Etapa 2 — Design system

Tokens e componentes centrais em `ui/design/`. Fonte: a especificação escrita do
IZ Play V2 (`design-system/`), reimplementada em Compose. Ver `DESIGN_SYSTEM.md`.

### Etapa 3 — Migração visual (mobile/tablet)

Ordem: splash → cadastro de playlist → estrutura principal → sidebar → Home →
canais → filmes → séries → detalhes → busca → favoritos → histórico →
configurações → controles do player.

Um commit por tela. Build entre commits. **Nenhuma alteração funcional em commit
de interface.**

### Etapa 4 — Android TV

`LEANBACK_LAUNCHER`, banner 320×180, `touchscreen` não obrigatório, navegação
completa por D-pad, foco visível e previsível, player pelo controle remoto.

### Etapa 5 — Estabilização

Correções, desempenho, CI, proteção de branch, preparação de release.

---

## Adaptável diretamente vs. reimplementar

**Adaptável (especificação, não código):** tokens de cor/tipografia/espaçamento/
raio/elevação/animação do V2, escalas de foco, larguras da sidebar, proporções
de card, conceitos de navegação.

**Reimplementar do zero:** todo componente inspirado no Streamix (hero, fileiras,
cards, placeholders) e todo componente do V2 (`Sidebar`, `TvFocus`, `TvCard`) —
escritos em Compose contra os ViewModels do Another.

## Não importar

Do Streamix: backend Phoenix/Elixir, `/api/v1`, autenticação por e-mail, Qdrant,
busca semântica, recomendações por IA, proxy de streaming, libVLC, Media3,
engine selector, banco/models/repositories próprios, `minSdk 30`, identidade
roxo/ciano.

Do V2: `gateway/`, `catalog-gateway/`, `panel/`, `supernode/`, `super-iz-play/`,
`web-player/`, `deploy/`, VPN/WireGuard, P2P/SwarmCloud, telemetria, o
`XtreamClient`/`ContentRepository`/`VideoPlayer` do Android V2, endpoints e
infraestrutura embutidos.

## Decisões registradas

| Data | Decisão | Motivo |
|---|---|---|
| 2026-07-21 | `minSdk` permanece **26** | Alvo é Android 8+; evita ampliar a matriz de compatibilidade. Desugaring removido por consequência. |
| 2026-07-21 | Base congelada em `f71a552` | Estabilidade; upstream não é sincronizado automaticamente |
| 2026-07-21 | Player MPV mantido | Base validada pelo usuário; só a camada visual dos controles será redesenhada |
| 2026-07-21 | "Login" = cadastro de credenciais do provedor | Sem conta IZ Play, sem backend de usuários, sem segundo sistema de autenticação |
| 2026-07-21 | **`minSdk 26` em conflito com o hardware de teste** — pendente | O rk322x (MCD-121) anuncia Android "11.1" mas roda API 25. Com minSdk 26 o app não instala nele e a validação funcional fica sem dispositivo. Decidir entre: (a) voltar a minSdk 24 com desugaring e auditar as guardas de API 26+; (b) manter 26 e obter aparelho Android 8+; (c) manter 26 e reavaliar com dados da frota real. |
| 2026-07-21 | Dois amarelos com papéis distintos | `#F5A623` destaque AO VIVO/HOT · `#F39C12` warning do sistema. Primária continua `#CC0000`. |

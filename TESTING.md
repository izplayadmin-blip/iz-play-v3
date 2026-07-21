# TESTING — IZ Play V3

## Princípio

Nada é declarado testado sem evidência. Sem dispositivo, credenciais ou
conteúdo, o teste é marcado **bloqueado** — nunca "passou" nem "falhou".

Resultados vão para `STATUS.md`.

## Regras para testes de reprodução

Somente com:

- credenciais fornecidas ou autorizadas pelo usuário
- servidores autorizados
- conteúdo legal de teste

Credenciais **nunca** são registradas em documentação, commits, logs ou
fixtures. Registra-se apenas se estavam disponíveis ou não.

---

## Testes automatizados

```bash
cd apps/android
./gradlew.bat :app:testDebugUnitTest
```

Cobertura atual — 4 testes, todos verdes em 2026-07-21:

| Teste | O que cobre |
|---|---|
| `CredentialRedactorTest` (3) | mascaramento de credenciais em query, JSON e path Xtream |
| `ExampleUnitTest` (1) | herdado do template |

Cobertura é mínima. Ampliar junto com a migração visual, priorizando
`M3UParser`, `PlaybackUrlBuilder`, `CatalogTextSearch` e `AdultContentFilter` —
lógica pura, sem Android, fácil de testar.

## Checklist funcional (Marco 1)

Executar com credenciais autorizadas. Marcar cada item com resultado e data.

- [ ] Cadastro de playlist Xtream (URL, usuário, senha)
- [ ] Cadastro/importação de playlist M3U (URL e arquivo)
- [ ] Lista de canais ao vivo e categorias
- [ ] Abertura de canal — **medir o tempo até o primeiro frame**
- [ ] Filmes: catálogo, categorias, detalhes
- [ ] Séries: catálogo, temporadas, episódios
- [ ] Player MPV: play, pause, seek
- [ ] Troca de faixa de áudio
- [ ] Legendas: ativar, trocar, aparência
- [ ] Busca
- [ ] Favoritos: adicionar, remover, persistir
- [ ] Histórico
- [ ] Continuar assistindo: retomada da posição
- [ ] Reprodução automática do próximo episódio
- [ ] Downloads: iniciar, progresso, concluir, reproduzir offline
- [ ] Picture-in-Picture: entrar, sair, ciclo de vida
- [ ] Tratamento de erro com servidor indisponível
- [ ] Liberação de recursos ao sair do player

## Checklist de regressão visual

Toda tela migrada é comparada com o baseline `baseline/another-functional`:

- [ ] a funcionalidade é idêntica à do baseline
- [ ] nenhum dado deixou de aparecer
- [ ] toque funciona (mobile/tablet)
- [ ] mouse funciona, quando aplicável
- [ ] D-pad funciona, quando aplicável
- [ ] sem regressão de desempenho perceptível
- [ ] acessibilidade: descrições de conteúdo e alvos de toque adequados

## Checklist Android TV (Marco 4)

- [ ] o app aparece na home do Android TV (`LEANBACK_LAUNCHER`)
- [ ] banner 320×180 exibido corretamente
- [ ] navegação completa por D-pad, sem foco perdido
- [ ] foco inicial previsível em cada tela
- [ ] foco sempre visível
- [ ] nenhum card inacessível
- [ ] rolagem automática até o item focado
- [ ] foco preservado durante recomposição e após atualização de listas
- [ ] foco restaurado ao voltar de uma tela
- [ ] trânsito entre sidebar e conteúdo
- [ ] player controlável só pelo controle remoto
- [ ] teclado virtual adequado na busca
- [ ] desempenho aceitável em TV box de baixo custo

## Matriz de dispositivos

| Alvo | Status |
|---|---|
| Android 8 (API 26, minSdk) | não testado |
| Android 9 / 10 / 11+ | não testado |
| Android TV | dispositivo não disponível |
| TV Box | dispositivo não disponível |
| 720p / 1080p / 4K | não testado |

## Evidências

Para cada rodada registrar em `STATUS.md`: data, commit, dispositivo, comando
executado, resultado, e o SHA-256 do APK usado.

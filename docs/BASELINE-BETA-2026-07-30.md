# IZ Play Mobile — baseline do beta

Data: 2026-07-30

## Identidade do aplicativo

- Aplicativo: IZ Play Mobile
- Application ID: `com.izplay.mobile`
- Namespace: `com.izplay.v3`
- Versão: `3.0.0`
- Version code: `30000`
- minSdk: `24`

## Validação executada

Comando:

```text
gradlew clean testDebugUnitTest assembleDebug assembleRelease
```

Resultado:

- testes unitários: aprovados;
- APK debug: gerado;
- APK release: gerado;
- lint vital do release: aprovado após declarar o tipo `dataSync` do serviço de foreground do WorkManager;
- release ainda não assinado.

## Artefatos locais de referência

Os APKs não são versionados no Git.

| Variante | Tamanho | SHA-256 |
|---|---:|---|
| Debug | 81.294.380 bytes | `7B432E3F6F06816CD85A47546647E3FDD4B80E262552AAFB0F17D8DA3E792224` |
| Release unsigned | 73.543.374 bytes | `DCB6B6C7AEEB0CFFC16670A4496F94850BC786F1086D6D4068B944442796F17D` |

## Bloqueios conhecidos

- criar e custodiar a chave release;
- assinar o release com certificado definitivo;
- validar atualização instalada por cima de uma versão anterior assinada pela mesma chave;
- concluir segurança do token SwarmCloud e da telemetria;
- limitar o piloto P2P do Mobile à allowlist remota;
- executar smoke test autenticado com conteúdo autorizado.

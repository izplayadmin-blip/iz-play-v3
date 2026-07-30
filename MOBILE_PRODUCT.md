# IZ Play Mobile

Este repositório passa a representar a edição do IZ Play destinada a celulares
e tablets Android.

## Separação dos produtos

- **IZ Play Android TV:** interface horizontal, foco e controle remoto.
- **IZ Play Mobile:** interface vertical, toque e teclado virtual.
- Serviços compartilháveis: autenticação Xtream/M3U, catálogo, favoritos,
  histórico, reprodução, P2P e telemetria.
- Interfaces não devem ser reutilizadas diretamente entre TV e celular.

## Identidade técnica

| Item | Valor |
|---|---|
| Nome exibido | `IZ Play Mobile` |
| Application ID | `com.izplay.mobile` |
| Artefato Android | `IZPlay-Mobile-<variant>.apk` |
| Projeto Gradle | `IZPlayMobile` |
| Namespace interno temporário | `com.izplay.v3` |

O namespace interno permanece temporariamente inalterado para não introduzir,
junto do rebranding, uma migração ampla de pacotes, referências JNI e testes.

## Primeira entrega visual

1. Splash vertical com a identidade IZ Play.
2. Login vertical escuro com usuário e senha.
3. Suporte ao teclado virtual, autofill e exibição/ocultação da senha.
4. Estados de carregamento, erro, conexão e credenciais inválidas.
5. Responsividade para celulares compactos, grandes e tablets.
6. Validação em retrato; paisagem será tratada sem importar o layout da TV.

O trabalho mobile não deve alterar o projeto nem o layout fechado do IZ Play
Android TV.

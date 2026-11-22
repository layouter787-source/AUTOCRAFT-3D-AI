# AUTOCRAFT 3D AI

**Editor 3D para Android com modelagem, escultura, materiais, animação e agentes de IA.**

AUTOCRAFT 3D AI é um projecto Android que pretende aproveitar componentes reais do código-fonte do Blender para evitar reimplementar do zero um motor de modelagem. A experiência de utilização será concebida para telemóveis e tablets; o núcleo 3D será adaptado de forma incremental e validada em dispositivos Android.

> **Estado actual:** fase de auditoria e fundação de portabilidade. Este repositório contém o código-fonte Blender upstream. A branch `autocraft/android-porting-foundation` contém a estratégia inicial e os guias de implementação. Ainda não existe APK Android funcional nem está provada a compilação do núcleo Blender em Android.

## Índice

- [Visão](#visão)
- [Princípios](#princípios-de-engenharia)
- [O que será reutilizado](#o-que-será-reutilizado-do-blender)
- [Arquitectura alvo](#arquitectura-alvo)
- [Plano de implementação](#plano-de-implementação)
- [Estrutura planeada](#estrutura-planeada)
- [Build e desenvolvimento](#build-e-desenvolvimento)
- [Critérios de aceitação](#critérios-de-aceitação)
- [Regras para Claude Code e colaboradores](#regras-para-claude-code-e-colaboradores)
- [Segurança e licenças](#segurança-e-licenças)
- [Documentação](#documentação)
- [Estado e contribuição](#estado-e-contribuição)

## Visão

O utilizador poderá criar e editar objectos 3D manualmente ou delegar tarefas a agentes que consultam a cena, elaboram um plano, executam operações reais através de ferramentas tipadas e verificam o resultado.

O produto é generalista. A reconstrução de veículos será uma especialização, não o limite da aplicação.

### Capacidades pretendidas

- Viewport 3D com órbita, pan, zoom, perspectiva e vistas ortográficas.
- Selecção de objectos, vértices, arestas e faces.
- Transformações Move, Rotate e Scale.
- Modelagem poligonal, curvas, superfícies e modificadores não destrutivos.
- Escultura, materiais PBR, UV e texturas.
- Câmaras, iluminação, animação e rigging por fases.
- Projectos editáveis, autosave, undo/redo e checkpoints.
- Importação e exportação, com GLB/glTF como formato principal de intercâmbio.
- Agentes de IA com ferramentas reais, permissões, auditoria e validação.
- Reconstrução assistida a partir de uma ou várias imagens, texto ou referências combinadas.
- Agente especializado em reconstrução automóvel, com evidência e incerteza documentadas.

## Princípios de engenharia

1. **Reutilizar antes de reescrever.** Cada subsistema Blender será avaliado para reutilização directa, adaptação, substituição ou adiamento.
2. **Não portar o desktop inteiro às cegas.** A interface e a plataforma Android serão próprias; os subsistemas Blender serão isolados por fronteiras nativas.
3. **O renderer não é o modeler.** A representação editável e as operações geométricas pertencem ao core; o renderer apresenta a cena.
4. **Editor primeiro.** A viewport e as ferramentas são o centro. O painel de IA é recolhível e não bloqueia o espaço de trabalho.
5. **Toda a escrita passa por comandos.** UI e agentes não alteram buffers ou ficheiros de projecto directamente.
6. **Resultados verificáveis.** A IA só declara uma operação concluída depois de receber confirmação do motor e validar o estado.
7. **Incrementos compiláveis.** Cada etapa deve ter build e testes reproduzíveis.
8. **Sem falsas alegações.** Não marcar uma funcionalidade como pronta sem implementação, teste e evidência.

## O que será reutilizado do Blender

Candidatos prioritários identificados no repositório:

| Código / subsistema | Caminho no Blender | Objectivo |
|---|---|---|
| Blender Kernel | `source/blender/blenkernel` | Operações e gestão de dados de cena/objectos |
| BMesh | `source/blender/bmesh` | Topologia e edição de malhas |
| Blenlib | `source/blender/blenlib` | Matemática, containers e utilitários |
| Depsgraph | `source/blender/depsgraph` | Avaliação de dependências e cena |
| DNA / MakesDNA | `source/blender/makesdna` | Estruturas persistentes do Blender |
| Animrig | `source/blender/animrig` | Sistemas de animação |
| Modifiers | `source/blender/modifiers` | Operações não destrutivas |
| Geometry | `source/blender/geometry` | Algoritmos geométricos |
| IO | `source/blender/io` | Importação/exportação a avaliar |
| GPU / Draw | `source/blender/gpu`, `source/blender/draw` | Avaliar partes do renderer e dependências |

A lista é um inventário de candidatos, não uma declaração de compatibilidade Android. Cada componente precisa de uma análise de dependências, APIs de sistema, licença, compilação ARM64, memória, testes e custo de manutenção.

### O que não será assumido como reutilizável

- GHOST, window manager e editores desktop sem uma camada de substituição Android.
- Dependências desktop, bibliotecas pré-compiladas para outros sistemas e caminhos específicos de SO.
- Toda a interface Blender.
- Python embutido ou execução de scripts de ficheiros importados sem sandbox e política de segurança.
- Cycles e renderização offline avançada no MVP.
- Qualquer módulo que não passe por uma prova de build e execução Android.

## Arquitectura alvo

A arquitectura combina a decisão de reutilizar o código Blender com a arquitectura Android da especificação do produto.

```text
AUTOCRAFT Android
├── app/                         Kotlin + Jetpack Compose
│   ├── Workspace / Navigation
│   ├── Project Services
│   ├── Viewport Host
│   ├── Tool Panels / Outliner
│   └── AI Client
│
├── android/native/               Android NDK + C++
│   ├── JNI Bridge
│   ├── Scene Adapter
│   ├── Command API
│   ├── Renderer Adapter
│   └── Android Platform Adapters
│
├── Blender source (upstream)     Código original preservado
│   ├── blenkernel
│   ├── bmesh
│   ├── blenlib
│   ├── depsgraph
│   ├── modifiers / geometry
│   └── subsistemas aprovados
│
├── project-format/               Formato .ac3d versionado
└── ai/                            Runtime e contratos de ferramentas (futuro)
```

### Stack planeada

| Camada | Tecnologia | Estado |
|---|---|---|
| Aplicação Android | Kotlin | Planeada |
| Interface | Jetpack Compose | Planeada |
| Código nativo | C/C++ via Android NDK | Planeada |
| Integração | JNI | Planeada |
| Build Android | Gradle + CMake | Planeada |
| Viewport | Protótipo de Filament conforme especificação; validar compatibilidade com o core Blender | Por validar |
| Projectos | Room/SQLite + asset store binário | Planeada |
| Intercâmbio | GLB/glTF, OBJ, STL, PLY; FBX após validação | Planeada |
| Runtime IA | Ferramentas tipadas, provider adapters e MCP opcional | Fase posterior |

**Decisão de renderer:** Filament é o candidato recomendado pela especificação de produto para a viewport Android. Antes de o adoptar definitivamente, comparar uma integração Filament com a possibilidade de adaptar partes do sistema GPU/Draw do Blender. Não manter dois renderers completos sem uma justificação técnica medida.

### Fronteira nativa

A UI comunica com o core através de uma API JNI pequena, estável e versionada. Não expor estruturas internas do Blender directamente à UI.

Operações propostas:
- inicializar e terminar uma sessão de cena;
- criar/abrir/guardar projecto;
- consultar resumo e objectos;
- criar/seleccionar/transformar objectos;
- executar operações de malha;
- obter buffers de viewport;
- undo/redo e checkpoints;
- receber eventos e erros estruturados.

Os nomes e contratos finais serão definidos após a primeira fatia compilável do core.

## Plano de implementação

### Fase 0 — Viabilidade e auditoria (em curso)

- [x] Confirmar que o repositório contém o código-fonte Blender.
- [x] Criar branch isolada para a fundação Android.
- [x] Documentar estratégia inicial de portabilidade.
- [ ] Fixar commit upstream e criar processo de sincronização.
- [ ] Mapear dependências e bibliotecas de cada subsistema candidato.
- [ ] Construir matriz de portabilidade Android.
- [ ] Provar NDK/CMake com uma biblioteca nativa mínima.
- [ ] Provar carregamento JNI num APK ARM64.
- [ ] Provar viewport e import/export GLB.

**Saída da fase:** APK de teste ARM64 com viewport estável e import/export básico. Não avançar para a implementação ampla do editor antes deste critério.

### Fase 1 — Core 3D

- Scene graph e IDs estáveis.
- Malhas, transforms e selecção.
- Comandos tipados e undo/redo.
- Serialização e recuperação de projecto.

**Saída:** criar e editar uma malha sem IA.

### Fase 2 — Editor

- Extrude, bevel, inset e operações essenciais.
- Modificadores não destrutivos.
- Materiais PBR e iluminação.
- Outliner e painéis contextuais.

**Saída:** fluxo de modelagem simples completo.

### Fase 3 — Tool Bridge

- Schemas tipados, validação e permissões.
- Auditoria, checkpoints e undo.
- Ferramentas de cena, mesh, surface, modifier, material, UV, animation e inspection.

**Saída:** agente cria e edita objectos e verifica resultados.

### Fase 4 — Runtime IA

- Orquestrador e provider adapters.
- Streaming, aprovações, limites e tracing.
- Skills e subagentes.

### Fase 5 — Pesquisa e MCP

- Pesquisa web, browser, GitHub e ficheiros autorizados.
- Adaptadores MCP isolados e substituíveis.
- Relatórios com fontes e proveniência.

### Fase 6 — Image-to-3D

- Entrada de uma ou várias imagens, colagens, desenhos e texto.
- Detecção de vistas e landmarks.
- Geração remota de geometria inicial.
- Importação e validação de resultados.

### Fase 7 — Agente automóvel

- VehicleSpec com dimensões, variante, arquitectura e confiança.
- Construção por assemblies: exterior, rodagem, chassis, powertrain, suspensão, interior e acabamentos.
- Separar factos confirmados, inferidos e imaginados.
- Relatório de qualidade e evidências.

### Fases 8–9 — Qualidade e produção

- Validação topológica, visual e dimensional.
- Benchmarks, segurança, telemetria e QA.
- Beta estável, documentação operacional e distribuição.

## Estrutura planeada

A estrutura evoluirá sem mover ou apagar arbitrariamente os directórios upstream do Blender.

```text
/
├── README.md
├── CLAUDE.md
├── CMakeLists.txt                 # upstream Blender; não substituir sem plano
├── source/blender/                # upstream Blender
├── intern/                        # upstream Blender
├── build_files/                   # upstream Blender
├── docs/
│   └── android/
│       ├── AUTOCRAFT_ANDROID_PORTING_STRATEGY.md
│       ├── PORTABILITY_AUDIT.md
│       └── IMPLEMENTATION_LOG.md
├── android/                       # aplicação Android, a criar após auditoria
│   ├── app/
│   └── native/
├── ai/                            # runtime/contratos futuros
└── project-format/                # schema .ac3d futuro
```

Não criar pastas vazias apenas para aparentar progresso. Adicionar cada módulo quando houver código e responsabilidade concretos.

## Build e desenvolvimento

### Requisitos conhecidos do upstream

O CMake raiz do Blender declara CMake mínimo 3.21 e valida versões de compilador para builds desktop. Isto **não** prova compatibilidade com Android NDK. O projecto também usa submódulos de bibliotecas pré-compiladas para Linux, macOS e Windows; esses submódulos não são bibliotecas Android.

### Android (quando o protótipo estiver implementado)

O build Android deverá usar:
- Android Studio / Gradle Wrapper versionado;
- Android SDK e NDK versionados no CI;
- CMake compatível com o NDK escolhido;
- ABI prioritária `arm64-v8a`;
- build reprodutível em GitHub Actions.

Os comandos concretos serão documentados quando os ficheiros Gradle/CMake Android existirem. Não executar comandos especulativos nem afirmar que produzem APK antes da configuração ser adicionada.

### Build upstream Blender

Para compilar o Blender desktop, seguir as instruções oficiais em [Building Blender](https://developer.blender.org/docs/handbook/building_blender/). Isso não compila a aplicação Android AUTOCRAFT.

## Critérios de aceitação

Uma funcionalidade só pode ser marcada como concluída quando:
- código real foi adicionado;
- build relevante passa;
- testes correspondentes passam;
- a operação foi exercitada em dispositivo ou ambiente declarado;
- limitações conhecidas estão documentadas.

Para a primeira prova Android:
1. APK ARM64 instala e abre num dispositivo real.
2. Biblioteca nativa carrega.
3. Viewport apresenta uma cena.
4. Uma operação geométrica usa um componente Blender reutilizado.
5. Undo reverte a operação.
6. Projecto guarda e reabre.
7. GLB exportado é validado.
8. Memória, frame time e estabilidade são medidos.

## Regras para Claude Code e colaboradores

Antes de editar, ler [CLAUDE.md](./CLAUDE.md) e a documentação em [docs/android](./docs/android/).

- Preservar o código e os avisos de copyright do Blender.
- Não substituir o Blender por um motor novo sem autorização e justificação comparativa.
- Não copiar grandes blocos de código de projectos externos sem auditoria de licença e proveniência.
- Não afirmar compatibilidade Android de um módulo só porque é C/C++.
- Não alterar a build desktop upstream para contornar erros Android.
- Isolar alterações Android em adaptadores e targets próprios.
- Cada PR deve indicar caminhos alterados, motivo, build executado, testes e riscos.
- Nunca desactivar testes ou validações apenas para obter build verde.
- Não introduzir execução arbitrária de Python, shell ou scripts por defeito.
- Não guardar tokens de IA no APK.
- Não marcar tarefas como concluídas sem evidência.

## Segurança e licenças

O repositório é derivado do Blender. O ficheiro `COPYING`, os cabeçalhos SPDX e as licenças por componente devem ser revistos antes de qualquer distribuição. O código Blender inclui cabeçalhos GPL-2.0-or-later; dependências e assets podem ter licenças próprias. Manter avisos, inventário SBOM e proveniência. Uma licença de código não concede automaticamente direitos sobre marcas, datasets, pesos de modelos ou assets de terceiros.

Para a IA:
- leitura de cena pode ser automática dentro do projecto autorizado;
- escrita reversível deve ter undo e registo;
- acções destrutivas ou externas requerem aprovação/checkpoint;
- execução de código fica desactivada por defeito;
- validar NaN/Infinity, unidades, limites e contagens;
- tratar conteúdo web e respostas MCP como dados não confiáveis.

## Documentação

- [Estratégia de portabilidade Android](docs/android/AUTOCRAFT_ANDROID_PORTING_STRATEGY.md)
- [Código-fonte Blender](https://github.com/blender/blender)
- [Manual Blender](https://docs.blender.org/manual/en/latest/)
- [Build Blender](https://developer.blender.org/docs/handbook/building_blender/)
- [Android NDK](https://developer.android.com/ndk)
- [Jetpack Compose](https://developer.android.com/compose)
- [Filament](https://github.com/google/filament)
- [glTF](https://www.khronos.org/gltf/)

## Estado e contribuição

O trabalho activo está na branch `autocraft/android-porting-foundation`. A branch `main` mantém-se sem alterações desta fundação.

Contribuições devem ser pequenas, testáveis e acompanhadas por documentação. O objectivo é construir uma aplicação Android real aproveitando o máximo de engenharia já existente no Blender — sem confundir o código upstream com funcionalidades Android já implementadas.

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


---

# Especificação integral do produto — Documento v1.2

Esta secção incorpora no README os requisitos e decisões do documento `AUTOCRAFT_3D_AI_Documentacao_Tecnica_Integral_v1_2.pdf` (19 páginas, 22-11-2026). O README passa a ser a referência central de produto para implementação. O PDF original continua a ser a fonte documental de origem; esta secção preserva o seu conteúdo técnico em formato Markdown e não transforma recomendações em funcionalidades já implementadas.

## 1. Definição, princípios e perfis

AUTOCRAFT 3D AI é uma aplicação Android profissional de modelagem, escultura, materiais, animação e visualização 3D. O utilizador pode trabalhar manualmente ou delegar tarefas a agentes de IA que investigam, planeiam, executam operações reais no motor 3D, inspeccionam os resultados e corrigem erros.

Princípios:
- Editor primeiro: viewport e ferramentas 3D são o centro; o chat não deve bloquear o espaço de trabalho.
- IA com ferramentas reais: texto gerado não equivale a operação concluída; confirmação depende do resultado do motor.
- Projectos editáveis: preservar objectos, nomes, hierarquia, materiais e modificadores, evitando entregar apenas uma imagem ou malha fundida.
- Entradas flexíveis: uma imagem, várias imagens, colagem, desenho técnico, texto ou combinações.
- Autonomia configurável: modos assistido, autónomo e especialista com limites e pontos de aprovação.
- Produto generalista: modelagem de qualquer objecto; o agente automóvel é especialização, não limite do produto.

Perfis de utilização:
- Criador 3D: mesh, curvas, modificadores, sculpt e materiais.
- Designer automóvel: superfícies, proporções, rodas, interiores e mecânica.
- Utilizador assistido por IA: planner, tool bridge, preview, undo e checkpoints.
- Utilizador técnico: medições, topologia, formatos e relatórios.

## 2. Requisitos funcionais completos

### 2.1 Projectos
Criar, abrir, guardar, duplicar, renomear e recuperar projectos; autosave; checkpoints; undo/redo; histórico; integridade dos assets e migrações de schema.

### 2.2 Viewport e navegação
Viewport 3D com órbita, pan, zoom, perspectiva e vistas ortográficas; gizmos, selecção, grelha e overlays. Deve suportar picking e apresentar a cena em tempo real.

### 2.3 Selecção e organização
Selecção de objectos, vértices, arestas e faces; selecção box, circle, lasso e linked; Outliner com objectos, colecções, hierarquia, visibilidade e nomes.

### 2.4 Transformações
Move, Rotate e Scale, pivots, snapping, coordenadas locais/globais e unidades explícitas.

### 2.5 Modelagem e topologia
- Mesh: Extrude, Inset, Bevel, Loop Cut, Knife, Bridge, Merge, Split, Dissolve.
- Topologia: recalcular normais, weld, triangulate, decimate e cleanup.
- Superfícies: Bezier, NURBS, loft, sweep e surface patch.
- Modificadores: Mirror, Subdivision, Bevel, Solidify, Boolean, Array, Remesh e Shrinkwrap.
- Sculpt: Draw, Clay, Smooth, Grab, Inflate, Crease, Pinch, Flatten e Mask.
- Medição: distância, ângulo, dimensões, bounding box e unidades.

### 2.6 Modificadores
Manter pilha não destrutiva com parâmetros e referências. Aplicar/bake deve ser explícito ou ocorrer numa etapa de exportação, nunca silenciosamente. Validar limites e suportar fallback quando a topologia não permitir a operação.

### 2.7 Materiais, UV, iluminação e câmaras
Materiais PBR: base color, metallic, roughness, normal, occlusion, emissive, alpha e clearcoat quando necessário. Materiais automóveis: verniz, metalizados, vidro, borracha, plásticos, cromados e tecidos. UV unwrap manual/automático, ilhas, seams, packing, texture painting e baking de normal/AO/roughness/base color. Câmaras ortográficas e perspectiva, presets front/rear/left/right/top e turntable. Preview com iluminação e materiais.

### 2.8 Animação e rigging
Timeline, keyframes, interpolação e curvas. Rig de rodas com rotação e direcção; suspensão com hierarquia e articulações simplificadas; portas, capô, mala, volante e elementos móveis. Separar animação visual de simulação física validada.

### 2.9 Importação e exportação
- GLB/glTF: formato principal de intercâmbio; materiais, hierarquia, câmaras, skins e animações.
- OBJ: intercâmbio simples de geometria, com materiais/hierarquia limitados.
- STL: impressão 3D; não preserva materiais PBR ou estrutura complexa.
- PLY: geometria e atributos/cores, útil em captura/reconstrução.
- FBX: pipelines DCC/animação, mediante biblioteca ou conversor validado e auditoria de licença.
- .ac3d: formato nativo editável completo, versionado e com migrações, preservando modificadores, histórico, ferramentas, metadados e dados não representados no glTF.

## 3. Experiência Android e UX

Workspace recomendado:
- Barra superior compacta: nome do projecto, guardar, undo/redo e menu contextual.
- Viewport central: cena 3D, gizmos, selecção, grelha e overlays.
- Barra de ferramentas: Select, Move, Rotate, Scale, Mesh, Sculpt e Measure.
- Painel contextual: propriedades da ferramenta/objecto seleccionado.
- Outliner recolhível: objectos, colecções, hierarquia e visibilidade.
- Painel AI recolhível: chat, plano, ferramentas invocadas, progresso e resultados.

Regras:
- Maximizar área útil da viewport.
- Ferramentas frequentes acessíveis com uma mão; acções avançadas em painéis recolhíveis.
- Alvos de toque generosos, gestos sem conflitos e suporte opcional a stylus.
- Mostrar claramente o modo Object, Edit, Sculpt, UV, Material ou Animation.
- Mostrar o que a IA pretende alterar, o que alterou e como desfazer.
- Evitar animações pesadas e painéis persistentes que reduzam a viewport.
- Paisagem para edição/modelagem; retrato simplificado para gestão de projectos, importação e comandos rápidos.

Stack Android de referência:
- Kotlin e Jetpack Compose.
- SurfaceView/TextureView integrada com Filament.
- C++ via Android NDK e JNI.
- CMake + Gradle.
- Room/SQLite para metadados e ficheiros binários para assets.
- WorkManager/serviço apropriado para tarefas persistentes.
- Ktor ou Retrofit/OkHttp para APIs, uploads, streaming e downloads.

## 4. Arquitectura lógica e execução

### 4.1 Camada Android local
Presentation (Compose, navegação e painéis), Workspace State (selecção, ferramentas e viewport), Project Services (save/import/export/autosave), AI Client (streaming, aprovações e estado de tarefas), Native Bridge (JNI) e 3D Core C++.

### 4.2 Core 3D
Scene Graph, Geometry/Mesh, Modifiers, Materials/UV, Animation/Rigging, Validation e Renderer Adapter (Filament).

O Scene Graph deve ter IDs estáveis, hierarquia, transforms, visibilidade, colecções e referências geométricas. Modelo lógico:
- Scene.
- Object(id, name, type, transform, parent_id).
- MeshData(vertices, edges, faces, attributes).
- ModifierStack[].
- MaterialSlots[].
- AnimationTracks[].
- Metadata(provenance, tags, version).

Fronteiras obrigatórias:
- UI não altera buffers geométricos directamente.
- AI Orchestrator não escreve directamente em ficheiros de projecto.
- Toda a alteração passa por comandos tipados no 3D Core.
- Leitura e escrita são separadas.
- Cada comando devolve resultado, estado actualizado e erros estruturados.
- O fornecedor de IA deve poder ser substituído sem reescrever o editor.

### 4.3 Backend/cloud opcional e recomendado para IA pesada
API Gateway/Auth/Quotas; Agent Runtime/Orchestrator; Model Provider Adapters; MCP Client e External Tool Registry; Research/Browser Workers; Image & 3D Generation Workers; Job Queue/Storage; Traces/Metrics/Audit.

Estratégia híbrida: viewport e operações rápidas no dispositivo; raciocínio avançado, pesquisa e geração pesada em serviços remotos/GPU. O backend devolve assets e operações serializadas; Android importa e valida antes de aplicar. Modo local limitado pode suportar operações determinísticas e modelos leves.

### 4.4 Renderer
Filament é candidato para renderização PBR Android e carregamento glTF. Validar transparência, picking, instancing, sombras, texturas e desempenho térmico em dispositivos de entrada e intermédios. O renderer desenha; o core mantém estado editável e executa geometria. Não delegar toda a geometria ao renderer. Não assumir Vulkan universal; negociar capacidades e manter fallback, ajustando sombras, MSAA, texturas e pós-processamento.

### 4.5 Representação geométrica
Mesh poligonal com vértices, arestas, faces, loops e atributos; índices/buffers GPU separados dos dados CPU editáveis; normais, tangentes, UVs, cores e grupos; IDs topológicos estáveis quando possível; BVH/estrutura espacial para ray casting e picking; LOD e simplificação para dispositivos limitados.

## 5. Sistema de agentes e orquestração

Agente principal (Main/Orchestrator): recebe pedido, recolhe contexto, planeia, delega, controla permissões e verifica o resultado através do catálogo de ferramentas.

Agentes especializados:
- Vision & Geometry Analyzer: imagens, vistas, landmarks, silhuetas e escala.
- Research Agent: fontes públicas e especificações.
- Modeling Agent: mesh, curvas, superfícies e modificadores.
- Automotive Agent: arquitectura e componentes de veículos.
- Sculpt Agent: formas orgânicas, remesh e multiresolução.
- Material / UV Agent: materiais PBR, UV e texturas.
- Animation Agent: rigging, keyframes, rodas e suspensão.
- Quality Agent: topologia, proporções, intersecções e consistência.
- Export Agent: formatos, escala, LOD e validação.

Ciclo de execução:
1. Interpretar pedido e definir objectivo mensurável.
2. Consultar estado da cena, selecção, ferramentas e restrições.
3. Criar plano com passos, dependências, riscos e aprovações.
4. Executar uma operação ou pequeno lote.
5. Receber confirmação estruturada do motor e actualizar contexto.
6. Renderizar/capturar vistas relevantes.
7. Comparar com critérios e referências; corrigir.
8. Guardar checkpoint e apresentar relatório.

Modos:
- Assistido: apresenta plano e pede aprovação antes de alterações relevantes.
- Autónomo: executa baixo risco e pede confirmação para acções destrutivas/externas.
- Especialista: ciclos de planeamento, construção e validação com limites de tempo, custo, memória e tentativas.

## 6. Tool Bridge, MCP e skills

### 6.1 Contrato de ferramenta
Cada ferramenta deve ter nome/versão estáveis, descrição, schema de entrada/saída, unidades explícitas, política de risco, permissões, indicação de escrita na cena, suporte a undo, limites e erros accionáveis. Exemplo conceptual: `mesh.extrude_faces` recebe object_id, face_ids, distance_m e direction; declara risco medium, escrita na cena e undo; devolve success, created_face_ids, scene_revision e warnings.

Validar dimensões, rejeitar NaN/Infinity, limitar contagens e não aceitar texto livre como substituto de parâmetros tipados em operações críticas.

### 6.2 Catálogo de ferramentas internas
- Scene: scene.get_summary, scene.get_object, scene.create_collection.
- Mesh: mesh.create, mesh.extrude, mesh.bevel, mesh.bridge, mesh.boolean.
- Surface: surface.loft, surface.sweep, surface.patch, curve.create.
- Modifier: modifier.add, modifier.configure, modifier.apply.
- Sculpt: sculpt.brush, sculpt.mask, sculpt.remesh, sculpt.smooth.
- Material: material.create_pbr, material.assign, texture.bake.
- UV: uv.unwrap, uv.pack, uv.validate.
- Animation: animation.keyframe, rig.create_vehicle, rig.configure_wheel.
- Inspection: mesh.check, scene.measure, scene.compare_reference.
- Project: project.checkpoint, project.restore, project.export.

### 6.3 MCP
MCP é protocolo de integração para ligar host de IA a servidores que expõem tools, resources e prompts. Não é motor 3D. AUTOCRAFT deve expor ferramentas nativas próprias e pode consumir servidores externos sem depender de Blender instalado no Android. O host mantém orquestração, permissões e consentimento.

Integrações candidatas descritas na especificação (não aprovadas automaticamente):
- Blender MCP oficial / Blender Lab: avaliar API/licença actual para prototipagem.
- Blender MCP community: catálogo e fluxos; auditar execução Python.
- BlendRelay MCP: referências visuais, edição e checkpoints; verificar dependências.
- Playwright MCP: browser research, respeitando termos, robots, login e privacidade.
- Firecrawl: extracção estruturada; avaliar custos, limites e direitos.
- GitHub MCP: repositórios, issues e documentação; tokens de escopo mínimo.
- Filesystem MCP: ficheiros/assets em pastas autorizadas.

### 6.4 Claude Code, skills e subagentes
Claude Code é referência de engenharia (MCP, subagents, skills, hooks e comandos especializados), não dependência que tenha de ser embutida/distribuída na aplicação final. AUTOCRAFT deve implementar conceitos equivalentes no seu runtime e manter fornecedores intercambiáveis.

Uma skill é um pacote de instruções e procedimentos reutilizáveis, não uma ferramenta. Deve incluir finalidade, pré-condições, ferramentas autorizadas, sequência, critérios de validação, erros comuns e exemplos. Skills planeadas: automotive-body-reconstruction, hard-surface-modeling, pbr-material-authoring, vehicle-rigging e topology-repair. Para reconstrução automóvel, referências sugeridas: sistema de coordenadas, workflow de superfícies, checklist de topologia e scripts de comparação de silhuetas/câmaras de referência.

Avaliar runtime próprio ou SDK de agentes. OpenAI Agents SDK é uma opção de backend para agentes, tools, handoffs, guardrails, sessões e tracing; manter adaptadores para outros fornecedores.

## 7. Reconstrução 3D a partir de imagens

Entrada sem quantidade mínima: uma imagem, várias imagens, montagem multi-vista, desenho técnico, screenshot, fotografia ou texto.

Estratégias:
- Uma imagem: single-image reconstruction, silhueta, pesquisa de identidade e inferência de partes ocultas.
- Montagem: detectar painéis/vistas, segmentar, normalizar e alinhar.
- Várias fotografias: classificar, deduplicar, associar vistas e reconstruir multi-view.
- Desenho técnico: extrair cotas, vistas ortográficas, linhas e proporções.
- Imagem + texto: combinar evidência visual com marca/modelo/ano/dimensões.

Pipeline:
1. Ingestão: validar tipo, resolução, orientação EXIF, tamanho e metadados.
2. Pré-processamento: fundo, rotação e escala visual quando apropriado.
3. Detecção: objecto, vistas, regiões e componentes.
4. Segmentação: carroçaria, vidros, rodas, faróis, portas, grelhas e acessórios.
5. Landmarks: eixos, centros de rodas, cantos, tejadilho, extremos e contorno.
6. Reconstrução: volume, profundidade, superfícies e relações espaciais.
7. Pesquisa: referências adicionais para zonas não observadas.
8. Fusão: reconciliar evidências e criar especificação paramétrica.
9. Construção: malha editável e materiais.
10. Validação: comparar renders em vistas equivalentes.

Uma imagem 2D não determina uma geometria 3D única. Não afirmar dimensões medidas quando foram inferidas. Classificar cada dado:
- Confirmado: fonte fiável ou medida explícita; citar fonte e variante/ano.
- Inferido: deduzido por geometria/proporções/engenharia; mostrar confiança e método.
- Imaginado: criado por falta de evidência; marcar conceptual/placeholder.

Modelos auxiliares avaliados no PDF: TripoSR, Stable Fast 3D, Hunyuan3D-2, TRELLIS e PIXFORM. São geradores iniciais, não CAD exacto nem topologia pronta para produção. Necessitam limpeza, retopologia, escala, segmentação e validação. PIXFORM é referência de pipeline/UX, não dependência obrigatória. Geração de alta qualidade provavelmente exige GPU/memória remota.

## 8. Agente de reconstrução automóvel

Missão: receber qualquer conjunto de referências e produzir veículo coerente, organizado e editável. O utilizador não precisa fornecer imagens de motor, interior, chassis ou suspensão; o agente pesquisa o que falta e marca claramente inferências e partes conceptuais.

Subagentes:
- Vehicle Vision Analyzer: marca/modelo provável, geração, vistas, proporções e componentes visíveis.
- Automotive Research Agent: manuais, fichas técnicas, catálogos de peças, imagens e referências públicas.
- Reasoning Agent: conflitos entre fontes, variantes, packaging e dependências mecânicas.
- Vehicle Design Specification: dimensões, eixos, drivetrain, peças e confiança.
- 3D Construction Agent: exterior, interior, chassis e conjuntos mecânicos.
- Quality & Verification Agent: vistas, geometria, montagem e consistência.

VehicleSpec conceptual:
- identity: make, model, generation, year_range, trim, confidence.
- dimensions: length_mm, width_mm, height_mm, wheelbase_mm, track_front_mm, track_rear_mm.
- architecture: body_style, engine_position, drivetrain, transmission, suspension_front, suspension_rear.
- assemblies: body, glass, wheels, brakes, engine, gearbox, chassis, suspension, interior, exhaust.
- evidence: claim, source_url, evidence_type, confidence.
- assumptions: component, assumption, reason.
- provenance: confirmed[], inferred[], imagined[].

Hierarquia de pesquisa: fabricante; manuais de oficina/peças legalmente acessíveis; homologação/fichas técnicas; catálogos; bases técnicas reputadas; fotografias e fóruns como evidência secundária. Registar URL, título, data de acesso, variante, mercado e afirmação suportada. Não misturar facelift/trim diferente.

Construção por conjuntos e dependências:
- Referencial: origem, eixo longitudinal, altura ao solo e wheelbase; depende de identidade/medidas.
- Exterior: body shell, capô, tejadilho, portas, para-choques e vidros; depende de landmarks/superfícies.
- Rodagem: pneus, jantes, cubos e travões; depende de bitola, diâmetro e eixos.
- Chassis: longarinas, travessas, subframes e pontos de fixação; depende da arquitectura.
- Powertrain: motor, caixa, diferencial e semi-eixos; depende de posição do motor e tracção.
- Suspensão: braços, molas, amortecedores e barras; depende de pontos de fixação/geometria.
- Interior: bancos, painel, volante, consola e pedais; depende da cabine/carroçaria.
- Acabamentos: faróis, espelhos, puxadores, emblemas e escape; depende da variante/referências.

Regras: não posicionar motor/caixa/diferencial arbitrariamente; alinhar rodas, eixos, cavas, suspensão e carroçaria; respeitar espaço de ocupantes, túnel, firewall e compartimento do motor; manter conjuntos editáveis e nomes consistentes; marcar componentes sem evidência como imaginados; geração visual não prova compatibilidade mecânica.

Qualidade: silhueta semelhante nas vistas disponíveis; wheelbase/largura/altura/bitolas coerentes; rodas centradas; sem faces degeneradas, normais invertidas ou non-manifold não intencionais; peças móveis separadas logicamente; relatório de evidência/incerteza.

## 9. Dados e gestão de projectos

Estrutura conceptual:
```text
Project.ac3d/
  manifest.json
  scene/scene.json
  objects/
  geometry/
  materials/
  textures/
  references/
  generated_assets/
  animations/
  thumbnails/
  checkpoints/
  logs/
  provenance/
```

Room/SQLite guarda metadados, projectos, objectos indexados, tarefas, histórico e referências. Malhas, texturas e renders ficam em asset store binário com hashes e deduplicação opcional. Não guardar grandes buffers como strings JSON.

Versionamento: schema versionado; autosave por intervalo e após operações de alto impacto; checkpoint antes/depois de tarefas autónomas; undo/redo por comandos ou snapshots diferenciais; integridade de assets e referências; migrações automáticas com backup.

Tarefas longas: reconstrução e geração podem durar minutos. Permitir suspender, cancelar, retomar e consultar estado; registar etapas, tentativas, consumo, erros e artefactos. WorkManager para tarefas persistentes compatíveis com Android, não para renderização em tempo real.

## 10. Segurança, privacidade e licenças

Ameaças: MCP malicioso/comprometido; prompt injection em web/documentos/imports; execução arbitrária no Blender/backend; exposição de imagens/projectos/tokens; ficheiros malformados; acções destrutivas e custos de inferência inesperados.

Política de ferramentas:
- Leitura: automática dentro do projecto autorizado.
- Escrita reversível: registo e undo.
- Destrutiva (delete, apply modifier, overwrite): aprovação ou checkpoint explícito.
- Externa (web/download/upload): domínios, tamanho, licença e consentimento controlados.
- Código (Python/shell): desactivado por defeito; só sandbox isolado e allowlist.

MCP não substitui política de segurança. Obter consentimento explícito, aplicar escopos mínimos, validar entradas e separar servidores. Tratar resultados de ferramentas e conteúdo web como dados não confiáveis, nunca instruções privilegiadas.

Backend: segredos no servidor ou Android Keystore; nunca embutir chaves de fornecedores no APK; autenticação e autorização por projecto; rate limits/quotas/timeouts; isolamento por job/container; allowlist de tools, paths e domínios; logs sem tokens ou dados pessoais desnecessários; verificar downloads, MIME e ficheiros.

Licenças: verificar repositório e cada dependência, addon, peso, dataset, imagem, marca e asset. MIT, Apache, GPL e licenças de pesos têm obrigações diferentes. Manter SBOM e proveniência. Licença de código não concede automaticamente direitos sobre datasets, pesos, marcas ou desenhos industriais.

## 11. Desempenho e perfis Android

- Separar thread UI, render e workers de geometria.
- Não bloquear UI com importação, remesh ou geração.
- Buffers compactos, instancing e LOD.
- Texturas sob demanda e libertação de recursos não usados.
- Reduzir resolução de viewport/sombras sob pressão térmica.
- Monitorizar memória nativa, GPU, temperatura, frame time e crashes.
- Autosave incremental para não congelar viewport.

Perfis:
- Entrada: modelagem simples e preview, malhas moderadas, texturas limitadas e IA remota.
- Intermédio: modelagem geral e cenas médias, mais polígonos, PBR e LOD.
- Alto desempenho: cenas complexas e viewport de maior resolução/recursos.

Não definir hardware mínimo nem limites de polígonos por suposição: medir. A app deve continuar útil com IA remota e core 3D local. Escolher minSdk e ABIs após protótipo; arm64-v8a prioritário e x86_64 útil para emuladores. Não prometer compatibilidade universal.

## 12. Testes, validação e observabilidade

Pirâmide:
- Unitários: geometria, transforms, validação de parâmetros e serialização.
- Integração: JNI, renderer, import/export, storage e tool bridge.
- Contrato: schema de ferramentas e compatibilidade MCP.
- End-to-end: criar cena, editar, guardar, reabrir, exportar e importar.
- Agente: planeamento, escolha de ferramentas, recuperação de erros e aprovação.
- Visuais: renders e silhuetas em vistas padronizadas.
- Dispositivo: memória, temperatura, bateria, frame time e lifecycle.

Métricas: sucesso por ferramenta/fluxo; tentativas até tarefa válida; operações revertidas; latência de viewport/jobs; falhas import/export; desvio de dimensões/silhuetas; custo de inferência e armazenamento.

Cada reconstrução deve gerar relatório: identidade provável, fontes, dimensões/unidades, peças confirmadas/inferidas/imaginadas, avisos de variante, problemas topológicos, limitações e versões dos modelos/ferramentas.

## 13. Roadmap integral e critérios de saída

| Fase | Entregáveis | Critério de saída |
|---|---|---|
| 0 — Viabilidade | Protótipo Compose + Filament + NDK; carregar GLB | Viewport estável e import/export básico |
| 1 — Core 3D | Scene graph, mesh, transforms, selecção, undo | Criar/editar malha sem IA |
| 2 — Editor | Extrude, bevel, inset, modifiers, materiais | Fluxo simples de modelagem completo |
| 3 — Tool Bridge | Tools tipadas, schema, auditoria, checkpoints | Agente cria/edita objectos e verifica resultados |
| 4 — Runtime IA | Orquestrador, provider adapters, streaming, approvals | Tarefa multi-etapa com logs |
| 5 — Research MCP | Pesquisa web, browser, GitHub, ficheiros autorizados | Relatório de fontes produzido pelo agente |
| 6 — Image-to-3D | Entrada flexível, detecção de vistas e gerador auxiliar | Reconstrução inicial a partir de uma imagem |
| 7 — Automotive | VehicleSpec, pesquisa de variantes e assemblies | Carro editável com relatório de confiança |
| 8 — Qualidade | Validação geométrica, comparação visual e correcções | Auto-revisão repetível |
| 9 — Produção | Segurança, performance, telemetria, export e QA | Beta estável e documentação operacional |

MVP realista: viewport funcional, edição básica de malha, projectos persistentes, export GLB, agente que cria objecto simples por tools tipadas e reconstrução conceptual de uma peça a partir de uma imagem. Não tentar reconstruir inicialmente um automóvel completo com mecânica exacta.

Critérios para aceitar integração externa: licença compatível com distribuição pretendida; código/dependências auditáveis; compatibilidade com versão/alvo; tools tipadas, documentação e testes; sem execução arbitrária não isolada; serviço substituível; custo, latência, limites e tratamento de dados conhecidos.

## 14. Catálogo de referências técnicas do PDF

Os projectos abaixo são candidatos de investigação, não dependências aprovadas automaticamente. Verificar estado, versões, licença, issues, dependências e testes antes de integrar.

### Blender, MCP e skills
- [Blender MCP — teamipc](https://github.com/teamipc/blender-mcp)
- [Blender MCP Ultra — carlosh7](https://github.com/carlosh7/blender-mcp)
- [BlendRelay MCP](https://github.com/)
- [Blender MCP — ceeyang](https://github.com/)
- [MCP servers catalogue](https://github.com/modelcontextprotocol/servers)

### Reconstrução e geração 3D
- [Stable Fast 3D](https://github.com/Stability-AI/stable-fast-3d)
- [Hunyuan3D-2](https://github.com/Tencent-Hunyuan/Hunyuan3D-2)
- [TRELLIS](https://github.com/microsoft/TRELLIS)
- [PIXFORM](https://github.com/)
- [TripoSR](https://github.com/VAST-AI-Research/TripoSR)

### Automóveis e rigging
- [Rigacar](https://github.com/ivarosil/Rigacar)
- Rigacar 5 fork: identificar e auditar o fork exacto antes de integrar.
- Car generation and modeling: referência a avaliar; maturidade e âmbito por confirmar.

### Documentação oficial
- [Android Jetpack Compose](https://developer.android.com/compose)
- [Android NDK](https://developer.android.com/ndk)
- [NDK + CMake](https://developer.android.com/ndk/guides/cmake)
- [Filament](https://github.com/google/filament)
- [Filament documentation](https://google.github.io/filament/)
- [Blender Python API](https://docs.blender.org/api/current/)
- [MCP specification](https://modelcontextprotocol.io/)
- [Playwright MCP](https://github.com/microsoft/playwright-mcp)
- [OpenAI Agents SDK](https://openai.github.io/openai-agents-python/)
- [Khronos glTF](https://www.khronos.org/gltf/)

### Claude Code
Consultar documentação oficial para [overview](https://docs.anthropic.com/en/docs/claude-code/overview), MCP, subagents, skills e hooks. Claude Code é ferramenta de desenvolvimento e referência de desenho; não deve ser embutido/distribuído com AUTOCRAFT.

### Notas técnicas preservadas
- Blender MCP community tools são referências de prototipagem e catálogo; AUTOCRAFT cria tools nativas próprias.
- Avaliar servidor oficial Blender separadamente de integrações comunitárias; não copiar nomes sem mapear contratos.
- Modelos image-to-3D geram geometria inicial, não CAD automóvel validado.
- Para geração 3D de qualidade, GPU e memória são críticas; backend remoto é opção inicial realista.
- Licença de repositório, pesos e assets deve ser confirmada antes de distribuição comercial.
- Pesquisa não substitui ensaios de integração nem revisão de código.

## 15. Decisão de arquitectura de produto (fonte v1.2)

A linha-base recomendada pelo documento v1.2 é aplicação Android nativa Kotlin/Compose, core geométrico C++ acessível por JNI, renderer Filament, formato nativo de projecto e GLB para intercâmbio. IA através de runtime de agentes com ferramentas tipadas, skills, adaptadores MCP e serviços externos controlados.

A reconstrução automóvel aceita qualquer quantidade de imagens, inclusive uma. O agente analisa material, pesquisa referências, cria Vehicle Design Specification, constrói assemblies e comunica o que está confirmado, inferido ou imaginado. O resultado é editável e acompanhado por relatório de qualidade.

A ordem crítica é provar primeiro motor, viewport, modelo de dados e Tool Bridge; depois autonomia, pesquisa e reconstrução especializada. Não construir uma interface impressionante sem motor 3D sólido.

---

## Estado de implementação — actualizado

**Implementado nesta branch:** documentação estratégica, auditoria preliminar, README consolidado e instruções para Claude Code.

**Ainda não implementado:** aplicação Android, Gradle/NDK/JNI, renderer integrado, núcleo Blender portado, project store, ferramentas, runtime de IA, image-to-3D e agente automóvel. Não existe APK validado nesta fase.

Esta distinção é obrigatória em todos os relatórios de progresso.

# Auditoria inicial de portabilidade — AUTOCRAFT 3D AI

**Data da auditoria:** 2026-11-22  
**Branch de trabalho:** `autocraft/android-porting-foundation`  
**Base examinada:** commit `d5ee2824202f24cb49173d66508d828a2b0f487e` da branch `main`.

## 1. Factos confirmados no repositório

- O repositório é uma cópia do código-fonte Blender, não uma aplicação Android já existente.
- A raiz contém `CMakeLists.txt`, `GNUmakefile`, `source/`, `intern/`, `build_files/`, `assets/`, documentação e configurações upstream.
- O CMake raiz declara `cmake_minimum_required(VERSION 3.21)`.
- O CMake raiz faz validações de toolchain desktop e configura o projecto `Blender`.
- `source/blender/CMakeLists.txt` adiciona subsistemas como editors, windowmanager, animrig, blenkernel, blenlib, bmesh, draw, render, depsgraph, geometry, gpu, imbuf, nodes, modifiers e IO.
- `intern/CMakeLists.txt` adiciona bibliotecas internas e condicionais como ghost, guardedalloc, eigen, cycles, openvdb, bullet, mantaflow, draco e outras.
- `.gitmodules` lista bibliotecas pré-compiladas para Linux x64, Linux ARM64, macOS ARM64, Windows x64 e Windows ARM64. Não lista um conjunto Android.
- O inventário de pastas confirma caminhos para `source/blender/blenkernel`, `bmesh`, `blenlib`, `depsgraph`, `animrig`, `modifiers`, `geometry`, `gpu` e `draw`.
- A busca por configuração Android/Gradle no repositório não identificou uma aplicação Android existente nesta auditoria.

## 2. Conclusão técnica inicial

Não há base para afirmar que o Blender completo compila em Android. O CMake actual é o build system da aplicação Blender desktop e inclui subsistemas de janela, UI, renderização e dependências de plataforma.

O caminho de menor risco é isolar uma biblioteca Android própria e integrar subsistemas Blender progressivamente. O primeiro teste deve escolher uma fatia de código com dependências controláveis, provar compilação NDK e expor uma operação simples via JNI. A escolha exacta da fatia depende de análise recursiva das dependências CMake e headers.

## 3. Matriz preliminar

| Módulo | Caminho | Estado | Próxima verificação |
|---|---|---|---|
| Blenlib | `source/blender/blenlib` | Candidato a DIRECT/ADAPT | Dependências, libc, threading, build NDK |
| BMesh | `source/blender/bmesh` | Candidato a ADAPT | Dependências de BLI, CustomData e allocator |
| BKE | `source/blender/blenkernel` | ADAPT provável | Grafo amplo de dependências e DNA |
| DNA | `source/blender/makesdna` | ADAPT provável | Geração de código e dependências de build |
| Depsgraph | `source/blender/depsgraph` | ADAPT provável | BKE, threading, evaluation context |
| Modifiers | `source/blender/modifiers` | ADAPT provável | Depsgraph, BKE, geometry e bibliotecas externas |
| Geometry | `source/blender/geometry` | Candidato a ADAPT | Dependências geométricas e nodes |
| Animrig | `source/blender/animrig` | ADAPT provável | BKE, DNA, animation data |
| GPU / Draw | `source/blender/gpu`, `draw` | INVESTIGAR | Backend gráfico, shaders, surface e driver |
| GHOST / Window Manager | `intern/ghost`, `source/blender/windowmanager` | REPLACE/DEFER | Não assumir integração Android directa |
| Editors | `source/blender/editors` | REPLACE/DEFER | UI desktop não é a UI Android |
| Cycles | `intern/cycles` | DEFER | Custo e compatibilidade GPU/CPU |

Esta matriz é hipótese inicial, não certificação de portabilidade.

## 4. Riscos/bloqueios

1. Bibliotecas externas desktop e submódulos não fornecem automaticamente artefactos Android.
2. O sistema de janelas e entrada do Blender não corresponde ao ciclo de vida Android.
3. A camada GPU depende de backends, drivers e superfícies que precisam de adaptação.
4. O tamanho e a memória do core completo podem exceder o orçamento de telemóveis.
5. Build tools desktop do Blender podem não ser compatíveis com a toolchain Android sem alterações.
6. Licenças de bibliotecas e assets devem ser revistas separadamente.

## 5. Próximas acções técnicas

- [ ] Fixar o upstream remoto e política de sincronização sem alterar a base.
- [ ] Obter árvores CMake de BLI, BMesh, DNA, BKE e dependências.
- [ ] Fazer mapa de headers e símbolos usados por um alvo de teste mínimo.
- [ ] Seleccionar a primeira fatia de core.
- [ ] Criar projecto Android mínimo com Gradle/NDK/CMake.
- [ ] Compilar biblioteca ARM64 e carregá-la via JNI.
- [ ] Só depois integrar a primeira operação Blender real.

## 6. O que ainda não foi feito

- Não foi executado build Android.
- Não foi produzido APK.
- Não foi provada a compilação de qualquer módulo Blender com NDK.
- Não foi integrado Filament.
- Não foi criada UI Android.
- Não foram executados testes em dispositivo.

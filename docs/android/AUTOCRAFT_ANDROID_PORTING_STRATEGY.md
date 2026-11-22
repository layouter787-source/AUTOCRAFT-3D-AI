# AUTOCRAFT 3D AI — Estratégia de aproveitamento do Blender no Android

**Estado:** plano de engenharia inicial; ainda não é prova de compilação Android.  
**Base:** branch `main` do repositório Blender em 22-11-2026.  
**Objectivo:** reutilizar o máximo possível do código funcional do Blender e adaptar apenas as fronteiras necessárias para Android.

## 1. Princípio de engenharia

Não vamos reescrever um motor 3D. O Blender será a base do núcleo de dados e operações 3D. A aplicação Android terá uma camada de plataforma e experiência móvel própria, ligada ao núcleo reutilizado por uma API C/C++ estável.

A adaptação deve ser incremental: primeiro compilar e executar uma fatia mínima do núcleo; depois carregar e editar uma cena; só então ampliar as ferramentas. Não se deve prometer que o Blender desktop inteiro compila ou funciona no Android sem um estudo de dependências e uma prova técnica.

## 2. O que queremos reaproveitar

Os seguintes subsistemas existem no código-fonte e são candidatos prioritários. A inclusão final depende de provar que as suas dependências podem ser compiladas e executadas no Android.

| Subsistema Blender | Aproveitamento pretendido no AUTOCRAFT |
|---|---|
| Estruturas de dados de cena e objectos (DNA / Main / Scene / Object) | Modelo de cena, objectos, colecções e persistência compatível, onde viável |
| Blender Kernel (BKE) | Operações centrais de objectos, dados, avaliação e gestão da cena |
| BMesh | Edição de malhas: vértices, arestas, faces e operações topológicas |
| Depsgraph | Avaliação de dependências, modificadores e relações entre objectos |
| Blenlib | Matemática, containers, utilitários e algoritmos reutilizáveis |
| Modifiers / Geometry Nodes | Operações não destrutivas e geração procedural, por etapas |
| Animrig / animação | Curvas, keyframes e sistemas de animação que possam ser isolados |
| IO e import/export | Reutilização selectiva dos formatos e conversores, conforme dependências |
| Sistema de materiais e nós | Dados e avaliação de materiais; interface móvel será própria |

**Importante:** reutilizar código não significa incluir automaticamente cada módulo. Cada candidato terá uma ficha com dependências, licença, tamanho, suporte ARM64, estado de compilação, testes e custo de manutenção.

## 3. O que não devemos transportar sem adaptação

- Interface desktop, áreas, editores e fluxo de janelas do Blender: não correspondem a uma experiência táctil Android.
- Dependências de sistema operativo desktop, janelas, dispositivos de entrada e gestão de ficheiros.
- Módulos de renderização ou efeitos que dependam de APIs indisponíveis no dispositivo-alvo.
- Recursos de produção que aumentem muito o APK ou a memória sem serem necessários no primeiro lançamento.
- Execução de scripts ou ferramentas arbitrárias da IA sem permissões, isolamento e validação.

A interface Android será nativa. O núcleo 3D será C/C++ e a ligação à UI será feita por uma fronteira JNI controlada.

## 4. Arquitectura-alvo por camadas

1. **Android App (Kotlin):** ciclo de vida, permissões, projectos, ficheiros, toque, stylus, acessibilidade e UI adaptável.
2. **Viewport Android:** superfície gráfica, gestos, selecção e apresentação. A escolha entre adaptar o sistema GPU do Blender ou usar um renderer dedicado só será fechada após a prova de compatibilidade; evitar manter dois motores de renderização sem necessidade.
3. **AUTOCRAFT Bridge (JNI/C++):** API pequena e estável para comandos, consultas de cena, eventos, erros e cancelamento.
4. **Blender Core adaptado:** estruturas de dados e subsistemas seleccionados, com dependências desktop removidas ou substituídas.
5. **Project Store:** formato nativo versionado, autosave, checkpoints e import/export. Preservar compatibilidade com .blend quando tecnicamente e legalmente viável; GLB será formato de intercâmbio.
6. **AI Tool Bridge:** ferramentas tipadas que chamam operações permitidas do núcleo; plano, aprovação, execução, verificação e undo.
7. **Serviços remotos opcionais:** pesquisa e geração image-to-3D; a edição manual básica não deve depender de rede.

## 5. Plano de auditoria antes de grandes alterações

### A — Inventário real do repositório
- Fixar o commit upstream de referência e registar a versão Blender.
- Mapear módulos, dependências externas, submódulos e bibliotecas pré-compiladas.
- Identificar código condicionado por Windows, macOS, Linux e desktop.
- Identificar testes existentes para cada subsistema candidato.
- Confirmar licenças de cada dependência, addon, modelo e asset.

### B — Matriz de portabilidade
Para cada módulo, classificar:
- **Reutilizar directamente:** compila em Android sem alterações relevantes.
- **Adaptar:** lógica útil, mas requer substituição de APIs ou dependências.
- **Substituir:** dependência desktop ou backend incompatível.
- **Adiar:** demasiado pesado ou fora do MVP.

### C — Prova técnica Android
- Criar app Android mínima e build reproducível com Gradle, NDK e CMake.
- Compilar uma biblioteca nativa ARM64 com uma fatia pequena do Blender.
- Criar, modificar e destruir uma malha simples através da API JNI.
- Renderizar a cena no dispositivo e medir FPS, memória e estabilidade.
- Testar ciclo de vida Android, perda/restauro de superfície e interrupções.
- Não avançar para ferramentas complexas antes desta prova passar.

### D — MVP de edição
- Abrir/criar projecto, adicionar objecto, seleccionar, mover, rodar e escalar.
- Editar malha com operações básicas.
- Materiais PBR básicos.
- Undo/redo, guardar, fechar e reabrir.
- Exportar GLB e validar o ficheiro exportado.

### E — IA depois do núcleo fiável
- Definir schemas de ferramentas; não expor API interna arbitrária.
- Implementar plano, permissões, checkpoints, execução e verificação.
- Começar por tarefas pequenas e reversíveis.
- Adicionar reconstrução por imagem e agente automóvel em fases posteriores.

## 6. Critérios de aceitação

A primeira prova só é considerada concluída quando:
- o APK ARM64 instala e abre num dispositivo Android real;
- a biblioteca nativa carrega sem falhas;
- a viewport apresenta uma cena;
- uma operação de geometria é executada pelo núcleo reutilizado;
- a operação pode ser desfeita;
- o projecto pode ser guardado e reaberto;
- há medições de memória, tempo de resposta e estabilidade;
- o build pode ser repetido por GitHub Actions.

## 7. Licenciamento e proveniência

O repositório contém a indicação GPL-2.0-or-later do Blender. Antes de distribuir AUTOCRAFT, é obrigatório rever as obrigações da GPL para o código derivado e verificar separadamente a licença de cada biblioteca, addon, modelo, textura e serviço. Manter avisos de copyright, licenças e registo de alterações; não remover a proveniência do Blender.

## 8. Riscos técnicos que devem ser resolvidos com provas

- O Blender é uma aplicação C/C++ grande, com dependências e pressupostos de plataforma desktop.
- O sistema de janelas/entrada e partes do ciclo de vida não são equivalentes a Android.
- A renderização, shaders, drivers e formatos de textura variam entre dispositivos.
- O núcleo completo pode exceder limites de tamanho, memória ou tempo de compilação do alvo.
- O formato .blend e a API Python não devem ser assumidos como portáveis sem validar as dependências.
- A IA e os serviços image-to-3D podem exigir rede e GPU remota; não devem bloquear o editor manual.

## 9. Próximos artefactos de engenharia

1. Inventário de módulos Blender com caminhos reais e dependências.
2. Matriz de portabilidade Android por módulo.
3. Protótipo Gradle + NDK + CMake ARM64.
4. Primeiro adaptador JNI para uma operação de geometria.
5. Teste de viewport e benchmark num Android físico.

**Regra:** preservar o upstream e fazer alterações Android em camadas claramente identificadas. Não substituir o projecto Blender por um esqueleto novo, nem alterar módulos centrais sem testes de regressão.

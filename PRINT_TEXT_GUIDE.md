# Guia de Impressão de Texto - React Native PlugPag

## Visão Geral

A função `printText` permite imprimir texto diretamente no terminal PlugPag, gerando a imagem localmente no dispositivo. Isso elimina a necessidade de downloads do servidor, proporcionando impressão mais rápida e eficiente.

## Funcionalidades

- ✅ **Geração local de imagem**: Cria a imagem do texto diretamente no terminal
- ✅ **Quebra de linha automática**: Quebra texto longo em múltiplas linhas
- ✅ **Sem dependência de rede**: Não requer download de arquivos
- ✅ **Impressão rápida**: Processamento local elimina latência
- ✅ **Formatação automática**: Ajusta largura para a impressora

## Como Usar

### Importação

```typescript
import { printText } from 'react-native-plugpag-lib';
```

### Uso Básico

```typescript
// Texto simples
await printText('Olá, mundo!');

// Texto com múltiplas linhas
const texto = `
COMPROVANTE DE VENDA
Data: ${new Date().toLocaleDateString('pt-BR')}
Hora: ${new Date().toLocaleTimeString('pt-BR')}

Produto: Item de Teste
Valor: R$ 25,00
Status: Aprovado

Obrigado pela compra!
`;

await printText(texto);
```

### Exemplo Completo

```typescript
import React from 'react';
import { View, TouchableOpacity, Text, Alert } from 'react-native';
import { printText } from 'react-native-plugpag-lib';

export default function ExemploImpressao() {
  const handleImprimirComprovante = async () => {
    try {
      const comprovante = `
      ================================
      COMPROVANTE DE VENDA
      ================================

      Data: ${new Date().toLocaleDateString('pt-BR')}
      Hora: ${new Date().toLocaleTimeString('pt-BR')}

      Produto: Produto de Teste
      Quantidade: 1
      Valor Unitário: R$ 25,00
      Total: R$ 25,00

      Forma de Pagamento: Cartão
      Status: Aprovado

      ================================
      Obrigado pela compra!
      ================================
      `;

      await printText(comprovante);
      Alert.alert('Sucesso', 'Comprovante impresso!');
    } catch (error) {
      Alert.alert('Erro', 'Falha na impressão: ' + error);
    }
  };

  return (
    <View>
      <TouchableOpacity onPress={handleImprimirComprovante}>
        <Text>Imprimir Comprovante</Text>
      </TouchableOpacity>
    </View>
  );
}
```

## Configurações da Impressão

A função utiliza as seguintes configurações padrão:

- **Largura máxima**: 384 pixels (otimizada para impressoras térmicas)
- **Tamanho da fonte**: 20px (aumentado para melhor legibilidade)
- **Padding**: 15px (aumentado para melhor espaçamento)
- **Fonte**: Negrito (DEFAULT_BOLD + FakeBoldText)
- **Cor do texto**: Preto
- **Cor de fundo**: Branco
- **Espaçamento entre linhas**: 4px adicional
- **Formato de saída**: PNG

## Vantagens

1. **Performance**: Geração local elimina latência de rede
2. **Confiabilidade**: Não depende de conectividade
3. **Simplicidade**: API simples e direta
4. **Flexibilidade**: Suporta texto com formatação
5. **Eficiência**: Reutiliza a infraestrutura existente

## Limitações

- Apenas texto (não suporta imagens complexas)
- Formatação limitada (texto simples com quebra de linha)
- Largura fixa da impressora (300px)

## Troubleshooting

### Erro: "Falha ao gerar imagem do texto"
- Verifique se o texto não está vazio
- Certifique-se de que há espaço suficiente no cache do dispositivo

### Erro: "Falha na impressão"
- Verifique se o terminal PlugPag está conectado
- Confirme se a impressora está funcionando
- Verifique as permissões do aplicativo

## Comparação com Outras Abordagens

| Método | Velocidade | Dependência de Rede | Complexidade |
|--------|------------|---------------------|--------------|
| `printText()` | ⭐⭐⭐⭐⭐ | ❌ | ⭐⭐ |
| `print()` com download | ⭐⭐ | ✅ | ⭐⭐⭐ |
| Geração externa | ⭐⭐⭐ | ✅ | ⭐⭐⭐⭐ |

## Conclusão

A função `printText` é a solução ideal para impressão de texto no PlugPag, oferecendo performance superior e simplicidade de uso, eliminando a necessidade de downloads externos e proporcionando uma experiência mais fluida para o usuário.

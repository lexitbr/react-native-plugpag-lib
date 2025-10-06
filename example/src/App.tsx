import React, { useState } from 'react';

import {
  StyleSheet,
  View,
  Text,
  Image,
  TouchableOpacity,
  ScrollView,
  Modal,
  ActivityIndicator,
  Alert,
} from 'react-native';

import {
  useTransactionPaymentEvent,
  doPayment,
  initializeAndActivatePinPad,
  refundPayment,
  plugPag,
  type PaymentTransactionResponseProps,
  reprintCustomerReceipt,
  printText,
} from 'react-native-plugpag-lib';

import LogoImg from './assets/react-native-pagseguro-plugpag-logo.png';

export default function App() {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [lastPayment, setLastPayment] =
    useState<PaymentTransactionResponseProps>(
      {} as PaymentTransactionResponseProps
    );

  const eventPayment = useTransactionPaymentEvent();

  async function handleInitializeAndActivatePinPad() {
    try {
      const data = await initializeAndActivatePinPad('403938');

      if (data.result !== 0) {
        Alert.alert('Erro ao ativar terminal', data.errorMessage);
        return;
      }

      Alert.alert('Terminal ativado com sucesso!');
    } catch (error) {
      console.log(error);
      Alert.alert('Erro ao ativar terminal');
    }
  }

  async function handleDoPaymentCreditType() {
    try {
      setIsModalVisible(true);

      const data = await doPayment({
        amount: 2500,
        type: plugPag.paymentTypes.CREDIT,
        printReceipt: true,
        installments: 1,
        installmentType: plugPag.installmentTypes.BUYER_INSTALLMENT,
        userReference: 'test',
      });

      setLastPayment(data);
      setIsModalVisible(false);

      Alert.alert('Transação concluída com sucesso');
    } catch (error) {
      console.log(error);
      setIsModalVisible(false);

      Alert.alert('Erro ao concluir transação');
    }
  }

  async function handleDoPaymentDebitType() {
    try {
      setIsModalVisible(true);

      const data = await doPayment({
        amount: 2500,
        type: plugPag.paymentTypes.DEBIT,
        printReceipt: true,
        installments: 1,
        installmentType: plugPag.installmentTypes.BUYER_INSTALLMENT,
        userReference: 'test',
      });

      console.log(data);

      setIsModalVisible(false);
    } catch (error) {
      console.log(error);
      setIsModalVisible(false);
    }
  }

  async function handleRefundLastTransaction() {
    try {
      setIsModalVisible(true);

      const response = await refundPayment({
        transactionCode: lastPayment.transactionCode!,
        transactionId: lastPayment.transactionId!,
        printReceipt: true,
      });

      setIsModalVisible(false);

      if (response.result !== 0) {
        Alert.alert('Estorno', 'Ocorreu um erro ao efetuar estorno');
        return;
      }

      Alert.alert('Estorno efetuado com sucesso');

      setLastPayment({} as PaymentTransactionResponseProps);
    } catch (error) {
      console.log(error);

      setIsModalVisible(false);
      Alert.alert('Estorno', 'Ocorreu um erro ao efetuar estorno');
    }
  }

  async function handlePrintText() {
    try {
      setIsModalVisible(true);

      const testText = `TESTE DE IMPRESSÃO DE TEXTO

Data: ${new Date().toLocaleDateString('pt-BR')}
Hora: ${new Date().toLocaleTimeString('pt-BR')}

Este é um teste de impressão de texto
gerado localmente no terminal PlugPag.

Funcionalidades:
- Geração de imagem local
- Quebra de linha automática
- Sem necessidade de download
- Impressão rápida e eficiente

Fim do teste.`;

      await printText(testText);

      setIsModalVisible(false);
      Alert.alert('Sucesso', 'Texto impresso com sucesso!');
    } catch (error) {
      console.log(error);
      setIsModalVisible(false);
      Alert.alert('Erro', 'Falha ao imprimir texto: ' + error);
    }
  }

  async function handlePrintComprovante() {
    try {
      setIsModalVisible(true);

      const comprovante = `
========================
COMPROVANTE DE VENDA
========================

Data: ${new Date().toLocaleDateString('pt-BR')}
Hora: ${new Date().toLocaleTimeString('pt-BR')}
Terminal: PlugPag Test

========================
DADOS DA TRANSAÇÃO
========================

Produto: Produto de Teste
Quantidade: 1 unidade
Valor Unitário: R$ 25,00
Subtotal: R$ 25,00
Desconto: R$ 0,00
Total: R$ 25,00

========================
FORMA DE PAGAMENTO
========================

Tipo: Cartão de Crédito
Bandeira: Visa
Parcelas: 1x de R$ 25,00
Status: Aprovado

========================
INFORMAÇÕES ADICIONAIS
========================

Código da Transação: TEST123456
NSU: 789012345
Autorização: 123456

========================
Obrigado pela compra!
Volte sempre!
========================`;

      await printText(comprovante);

      setIsModalVisible(false);
      Alert.alert('Sucesso', 'Comprovante impresso com sucesso!');
    } catch (error) {
      console.log(error);
      setIsModalVisible(false);
      Alert.alert('Erro', 'Falha ao imprimir comprovante: ' + error);
    }
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={[styles.titleHeader, styles.space]}>
          React Native Pagseguro PlugPag
        </Text>

        <Image source={LogoImg} style={styles.logo} />
      </View>

      <TouchableOpacity
        onPress={handleInitializeAndActivatePinPad}
        style={[styles.button, styles.space]}
      >
        <Text style={styles.textButton}>Inicializar e ativar o Pin Pad</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={handleDoPaymentCreditType}
        style={[styles.button, styles.space]}
      >
        <Text style={styles.textButton}>Pagar R$ 25 no crédito</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={reprintCustomerReceipt}
        style={[styles.button, styles.space]}
        disabled={!lastPayment.transactionId}
      >
        <Text style={styles.textButton}>Reimprimir via Cliente</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={handlePrintText}
        style={[styles.button, styles.space, styles.buttonGreen]}
      >
        <Text style={styles.textButton}>Imprimir Texto de Teste</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={handlePrintComprovante}
        style={[styles.button, styles.space, styles.buttonBlue]}
      >
        <Text style={styles.textButton}>Imprimir Comprovante de Venda</Text>
      </TouchableOpacity>

      <TouchableOpacity
        onPress={handleDoPaymentDebitType}
        style={[styles.button, styles.space]}
      >
        <Text style={styles.textButton}>Pagar R$ 25 no débito</Text>
      </TouchableOpacity>

      <TouchableOpacity
        disabled={!lastPayment.transactionId}
        onPress={handleRefundLastTransaction}
        style={[
          styles.button,
          styles.space,
          !lastPayment.transactionId && styles.buttonDisabled,
        ]}
      >
        <Text style={styles.textButton}>Estornar última transação</Text>
      </TouchableOpacity>

      <Modal transparent visible={isModalVisible}>
        <View style={styles.centeredView}>
          <View style={styles.modalView}>
            <Text style={styles.modalTitle}>
              {eventPayment.message ?? 'PROCESSANDO'}
            </Text>

            <View style={styles.modalBox}>
              <ActivityIndicator size="large" color="#00DDFC" />
            </View>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 24,

    backgroundColor: 'white',
  },
  header: {
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logo: {
    width: 180,
    height: 180,
  },
  titleHeader: {
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  button: {
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',

    padding: 12,

    borderRadius: 12,
    borderWidth: 2,
    borderColor: '#00DDFC',
  },
  textButton: {
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
    color: '#00DDFC',
  },
  space: {
    marginBottom: 12,
  },
  buttonGreen: {
    backgroundColor: '#28a745',
  },
  buttonBlue: {
    backgroundColor: '#007bff',
  },
  buttonDisabled: {
    opacity: 0.3,
  },
  centeredView: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 22,
  },
  modalView: {
    margin: 20,
    backgroundColor: 'white',
    borderRadius: 20,
    padding: 35,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.25,
    shadowRadius: 4,
    elevation: 5,
  },
  modalTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  modalBox: {
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
});

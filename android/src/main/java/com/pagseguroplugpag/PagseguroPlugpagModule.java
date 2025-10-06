package com.pagseguroplugpag;

import static com.pagseguroplugpag.JsonParseUtils.getPlugPagVoidDataFromJson;

import android.content.pm.PackageInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAbortResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.result.PlugPagNFCInfosResultDirectly;

@ReactModule(name = PagseguroPlugpagModule.NAME)
public class PagseguroPlugpagModule extends ReactContextBaseJavaModule {
  public static final String NAME = "PagseguroPlugpag";

  private final ReactApplicationContext reactContext;
  private PlugPagAppIdentification appIdentification;
  private PlugPag plugPag;
  private String messageCard = null;
  private int countPassword = 0;
  private String getPassword = null;

  private PackageInfo getPackageInfo() throws Exception {
    return getReactApplicationContext().getPackageManager()
            .getPackageInfo(getReactApplicationContext().getPackageName(), 0);
  }

  public PagseguroPlugpagModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();

    constants.put("PAYMENT_CREDITO", PlugPag.TYPE_CREDITO);
    constants.put("PAYMENT_DEBITO", PlugPag.TYPE_DEBITO);
    constants.put("PAYMENT_VOUCHER", PlugPag.TYPE_VOUCHER);

    constants.put("INSTALLMENT_TYPE_A_VISTA", PlugPag.INSTALLMENT_TYPE_A_VISTA);
    constants.put("INSTALLMENT_TYPE_PARC_VENDEDOR", PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR);
    constants.put("INSTALLMENT_TYPE_PARC_COMPRADOR", PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR);

    constants.put("OPERATION_ABORTED", PlugPag.OPERATION_ABORTED);

    constants.put("ACTION_POST_OPERATION", PlugPag.ACTION_POST_OPERATION);
    constants.put("ACTION_PRE_OPERATION", PlugPag.ACTION_PRE_OPERATION);
    constants.put("ACTION_UPDATE", PlugPag.ACTION_UPDATE);

    constants.put("AUTHENTICATION_FAILED", PlugPag.AUTHENTICATION_FAILED);
    constants.put("COMMUNICATION_ERROR", PlugPag.COMMUNICATION_ERROR);
    constants.put("ERROR_CODE_OK", PlugPag.ERROR_CODE_OK);
    constants.put("MIN_PRINTER_STEPS", PlugPag.MIN_PRINTER_STEPS);

    constants.put("NO_PRINTER_DEVICE", PlugPag.NO_PRINTER_DEVICE);
    constants.put("NO_TRANSACTION_DATA", PlugPag.NO_TRANSACTION_DATA);
    constants.put("SERVICE_CLASS_NAME", PlugPag.SERVICE_CLASS_NAME);
    constants.put("SERVICE_PACKAGE_NAME", PlugPag.SERVICE_PACKAGE_NAME);

    constants.put("RET_OK", PlugPag.RET_OK);
    String appVersion;

    try {
      appVersion = getPackageInfo().versionName;
    } catch (Exception e) {
      appVersion = "unkown";
    }
    constants.put("appVersion", appVersion);
    return constants;
  }

  // Cria a identificação do aplicativo
  @ReactMethod
  public void setAppIdentification() {
    try {
      plugPag = new PlugPag(reactContext);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    plugPag = new PlugPag(reactContext);
  }

  /**
   * Aborta a operação.
   *
   * @param promise
   */
  @ReactMethod
  public void doAbort(Promise promise) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();

    executor.execute(() -> {
      try {
        PlugPagAbortResult abortResult = plugPag.abort();
        final WritableMap map = Arguments.createMap();
        map.putBoolean("result", abortResult.getResult() == PlugPag.RET_OK);

        promise.resolve(map);
      } catch (Exception error) {
        Log.v("DoAbortError", error.getMessage());
        promise.reject("DoAbortError", error);
      } finally {
        executor.shutdown();
        try {
          if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
            executor.shutdownNow();
          }
        } catch (InterruptedException e) {
          executor.shutdownNow();
        }
      }
    });
  }


  // Exemplo de função para extrair e converter o serial number (UID) do cartão NFC
  public static String extractUIDFromResult(byte[] serialNumberBytes) {
    StringBuilder uidBuilder = new StringBuilder();
    for (int byteValue : serialNumberBytes) {
      uidBuilder.append(String.format("%02X", byteValue & 0xFF));
    }
    return uidBuilder.toString();
  }

/**
   * Lê um cartão NFC
   *
   * @param promise
   */
  @ReactMethod
  public void readNFCCard(Promise promise) {
    setAppIdentification();

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    Runnable runnableTask = new Runnable() {
      @Override
      public void run() {
        try {
          // Starta a antena NFC
          int resultStartNfc = plugPag.startNFCCardDirectly();

          PlugPagNFCInfosResultDirectly plugPagNFCInfosResult = plugPag.detectNfcCardDirectly(PlugPagNearFieldCardData.ONLY_M, 2000);

          byte[] serialNumberBytes = plugPagNFCInfosResult.getSerialNumber();

          final WritableMap map = Arguments.createMap();
          map.putString("uid", extractUIDFromResult(serialNumberBytes));

          plugPag.stopNFCCardDirectly();
          promise.resolve(map);
          executor.isTerminated();
          System.gc();
        } catch (Exception error) {
          Log.d("ReadNFCCardError", error.getMessage());
          promise.reject("error", error.getMessage());
          executor.isTerminated();
          System.gc();
        }
      }
    };

    executor.execute(runnableTask);
    executor.shutdown();
  }

/**
   * Print customer receipt
   *
   * @param promise
   */
  @ReactMethod
  public void reprintCustomerReceipt(Promise promise) {
    setAppIdentification();

  final ExecutorService executor = Executors.newSingleThreadExecutor();

    Runnable runnableTask = new Runnable() {
      @Override
      public void run() {
        try {
          PlugPagPrintResult result = plugPag.reprintCustomerReceipt();

          if (result.getResult() != 0) {
            throw new AppException(result.getMessage());
          }
          executor.isTerminated();
          System.gc();
        } catch (Throwable error) {
          promise.reject(error);
          executor.isTerminated();
          System.gc();
        }
      }
    };

    executor.execute(runnableTask);
    executor.shutdown();
  }

  // Ativa terminal e faz o pagamento
  @ReactMethod
  public void initializeAndActivatePinPad(String activationCode, Promise promise) {
    setAppIdentification();

    final PlugPagActivationData activationData = new PlugPagActivationData(activationCode);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Callable<PlugPagInitializationResult> callable = new Callable<PlugPagInitializationResult>() {
      @Override
      public PlugPagInitializationResult call() throws Exception {
        return plugPag.initializeAndActivatePinpad(activationData);
      }
    };

    Future<PlugPagInitializationResult> future = executor.submit(callable);
    executor.shutdown();

    try {
      PlugPagInitializationResult initResult = future.get();

      final WritableMap map = Arguments.createMap();
      map.putInt("result", initResult.getResult());
      map.putString("errorCode", initResult.getErrorCode());
      map.putString("errorMessage", initResult.getErrorMessage());

      promise.resolve(map);
    } catch (ExecutionException e) {
      Log.d("PlugPag", e.getMessage());
      promise.reject("error", e.getMessage());
    } catch (InterruptedException e) {
      Log.d("PlugPag", e.getMessage());
      promise.reject("error", e.getMessage());
    }
  }

  // Efetua pagamentos
  @ReactMethod
  public void doPayment(String jsonStr, Promise promise) {
    setAppIdentification();

    final PlugPagPaymentData paymentData = JsonParseUtils.getPlugPagPaymentDataFromJson(jsonStr);

    plugPag.setEventListener(new PlugPagEventListener() {
      @Override
      public void onEvent(final PlugPagEventData plugPagEventData) {
        messageCard = plugPagEventData.getCustomMessage();
        int code = plugPagEventData.getEventCode();

        WritableMap params = Arguments.createMap();
        params.putInt("code", plugPagEventData.getEventCode());

        if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD
                || plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
          if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
            countPassword++;
          } else if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
            countPassword = 0;
          }

          if (countPassword == 0) {
            getPassword = "Senha:";
          } else if (countPassword == 1) {
            getPassword = "Senha: *";
          } else if (countPassword == 2) {
            getPassword = "Senha: **";
          } else if (countPassword == 3) {
            getPassword = "Senha: *";
          } else if (countPassword == 4) {
            getPassword = "Senha: **";
          } else if (countPassword == 5) {
            getPassword = "Senha: ***";
          } else if (countPassword == 6 || countPassword > 6) {
            getPassword = "Senha: **";
          }

          params.putString("message", getPassword);
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", params);
        } else {
          params.putString("message", messageCard);
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", params);
        }
      }
    });

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    Runnable runnableTask = new Runnable() {
      @Override
      public void run() {
        try {
          PlugPagTransactionResult transactionResult = plugPag.doPayment(paymentData);
          final WritableMap map = Arguments.createMap();
          map.putInt("result", transactionResult.getResult());
          map.putString("errorCode", transactionResult.getErrorCode());
          map.putString("message", transactionResult.getMessage());
          map.putString("transactionCode", transactionResult.getTransactionCode());
          map.putString("transactionId", transactionResult.getTransactionId());
          map.putString("hostNsu", transactionResult.getHostNsu());
          map.putString("date", transactionResult.getDate());
          map.putString("time", transactionResult.getTime());
          map.putString("cardBrand", transactionResult.getCardBrand());
          map.putString("bin", transactionResult.getBin());
          map.putString("holder", transactionResult.getHolder());
          map.putString("userReference", transactionResult.getUserReference());
          map.putString("terminalSerialNumber", transactionResult.getTerminalSerialNumber());
          map.putString("amount", transactionResult.getAmount());
          map.putString("availableBalance", transactionResult.getAvailableBalance());
          map.putString("cardApplication", transactionResult.getCardApplication());
          map.putString("label", transactionResult.getLabel());
          map.putString("holderName", transactionResult.getHolderName());
          map.putString("extendedHolderName", transactionResult.getExtendedHolderName());

          promise.resolve(map);
          executor.isTerminated();
          System.gc();
        } catch (Exception error) {
          Log.v("DoPaymentError", error.getMessage());

          promise.reject("DoPaymentPlugPagError", error);
          executor.isTerminated();
          System.gc();
        }
      }
    };
    executor.execute(runnableTask);
    executor.shutdown();
  }

  // Estorno de pagamento
  @ReactMethod
  public void voidPayment(String dataJSON, Promise promise) {
    setAppIdentification();

    final PlugPagVoidData voidPaymentData = getPlugPagVoidDataFromJson(dataJSON);

    plugPag.setEventListener(new PlugPagEventListener() {
      @Override
      public void onEvent(final PlugPagEventData plugPagEventData) {
        messageCard = plugPagEventData.getCustomMessage();
        int code = plugPagEventData.getEventCode();

        WritableMap params = Arguments.createMap();
        params.putInt("code", plugPagEventData.getEventCode());

        if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD
                || plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
          if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
            countPassword++;
          } else if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
            countPassword = 0;
          }

          if (countPassword == 0) {
            getPassword = "Senha:";
          } else if (countPassword == 1) {
            getPassword = "Senha: *";
          } else if (countPassword == 2) {
            getPassword = "Senha: **";
          } else if (countPassword == 3) {
            getPassword = "Senha: *";
          } else if (countPassword == 4) {
            getPassword = "Senha: **";
          } else if (countPassword == 5) {
            getPassword = "Senha: ***";
          } else if (countPassword == 6 || countPassword > 6) {
            getPassword = "Senha: **";
          }

          params.putString("message", getPassword);
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", params);
        } else {
          params.putString("message", messageCard);
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", params);
        }
      }
    });

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    Runnable runnableTask = new Runnable() {
      @Override
      public void run() {
        try {
          PlugPagTransactionResult voidPaymentResult = plugPag.voidPayment(voidPaymentData);

          final WritableMap map = Arguments.createMap();
          map.putInt("result", voidPaymentResult.getResult());
          map.putString("errorCode", voidPaymentResult.getErrorCode());
          map.putString("message", voidPaymentResult.getMessage());
          map.putString("transactionCode", voidPaymentResult.getTransactionCode());
          map.putString("transactionId", voidPaymentResult.getTransactionId());
          map.putString("hostNsu", voidPaymentResult.getHostNsu());
          map.putString("date", voidPaymentResult.getDate());
          map.putString("time", voidPaymentResult.getTime());
          map.putString("cardBrand", voidPaymentResult.getCardBrand());
          map.putString("bin", voidPaymentResult.getBin());
          map.putString("holder", voidPaymentResult.getHolder());
          map.putString("userReference", voidPaymentResult.getUserReference());
          map.putString("terminalSerialNumber", voidPaymentResult.getTerminalSerialNumber());
          map.putString("amount", voidPaymentResult.getAmount());
          map.putString("availableBalance", voidPaymentResult.getAvailableBalance());
          map.putString("cardApplication", voidPaymentResult.getCardApplication());
          map.putString("label", voidPaymentResult.getLabel());
          map.putString("holderName", voidPaymentResult.getHolderName());
          map.putString("extendedHolderName", voidPaymentResult.getExtendedHolderName());

          promise.resolve(map);
          executor.isTerminated();
          System.gc();
        } catch (Exception error) {
          Log.v("VoidPaymentError", error.getMessage());

          promise.reject("VoidPaymentError", error);
          executor.isTerminated();
          System.gc();
        }
      }
    };

    executor.execute(runnableTask);
    executor.shutdown();
  }

  // Impressão personalizada a partir de URI de PNG/JPEG
  @ReactMethod
  public void print(String filePath, Promise promise) {
    setAppIdentification();

    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
      @Override
      public void onError(@NonNull PlugPagPrintResult plugPagPrintResult) {
        System.out.print("Message Error=>" + plugPagPrintResult.getMessage());
      }

      @Override
      public void onSuccess(@NonNull PlugPagPrintResult plugPagPrintResult) {
        System.out.print("Message Success=>" + plugPagPrintResult.getMessage());

      }
    };

    plugPag.setPrinterListener(listener);

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    Runnable runnableTask = new Runnable() {
      @Override
      public void run() {
        try {
          // Cria objeto com informações da impressão
          final PlugPagPrinterData file = new PlugPagPrinterData(filePath, 4, 10 * 12);

          PlugPagPrintResult result = plugPag.printFromFile(file);

          final WritableMap map = Arguments.createMap();
          map.putInt("retCode", result.getResult());
          map.putString("message", result.getMessage());
          map.putString("errorCode", result.getErrorCode());
          promise.resolve(map);
          System.out.print("Message =>" + result.getMessage());

          if (result.getResult() != 0) {
            throw new AppException(result.getMessage());
          }
          executor.isTerminated();
          System.gc();
        } catch (Throwable error) {
          promise.reject(error);
          executor.isTerminated();
          System.gc();
        }

      }
    };

    executor.execute(runnableTask);
    executor.shutdown();
  }

  // Impressão de texto gerando imagem localmente
  @ReactMethod
  public void printText(String text, Promise promise) {
    setAppIdentification();

    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
      @Override
      public void onError(@NonNull PlugPagPrintResult plugPagPrintResult) {
        System.out.print("Message Error=>" + plugPagPrintResult.getMessage());
        promise.reject("PRINT_ERROR", plugPagPrintResult.getMessage());
      }

      @Override
      public void onSuccess(@NonNull PlugPagPrintResult plugPagPrintResult) {
        System.out.print("Message Success=>" + plugPagPrintResult.getMessage());
        promise.resolve(plugPagPrintResult.getMessage());
      }
    };

    plugPag.setPrinterListener(listener);

    final ExecutorService executor = Executors.newSingleThreadExecutor();

    Runnable runnableTask = new Runnable() {
      @Override
      public void run() {
        try {
          // Gerar imagem localmente com o texto
          String imagePath = generateTextImage(text);

          if (imagePath == null) {
            promise.reject("IMAGE_GENERATION_ERROR", "Falha ao gerar imagem do texto");
            return;
          }

          // Cria objeto com informações da impressão
          final PlugPagPrinterData file = new PlugPagPrinterData(imagePath, 4, 10 * 12);

          PlugPagPrintResult result = plugPag.printFromFile(file);

          final WritableMap map = Arguments.createMap();
          map.putInt("retCode", result.getResult());
          map.putString("message", result.getMessage());
          map.putString("errorCode", result.getErrorCode());

          System.out.print("Message =>" + result.getMessage());

          if (result.getResult() != 0) {
            throw new AppException(result.getMessage());
          }

          promise.resolve(map);
          executor.isTerminated();
          System.gc();
        } catch (Throwable error) {
          promise.reject(error);
          executor.isTerminated();
          System.gc();
        }
      }
    };

    executor.execute(runnableTask);
    executor.shutdown();
  }

  // Método auxiliar para gerar imagem com texto
  private String generateTextImage(String text) {
    try {
      // Configurações do texto
      int textSize = 20; // Aumentado de 16 para 20
      int padding = 15; // Aumentado de 10 para 15
      int maxWidth = 384; // Largura ajustada para 384px

      // Configurar a pintura do texto
      Paint paint = new Paint();
      paint.setTextSize(textSize);
      paint.setTypeface(Typeface.DEFAULT_BOLD); // Mudado para BOLD
      paint.setColor(android.graphics.Color.BLACK);
      paint.setTextAlign(Paint.Align.LEFT);
      paint.setAntiAlias(true);
      paint.setFakeBoldText(true); // Adiciona negrito extra

      // Quebrar texto em linhas se necessário
      String[] lines = breakTextIntoLines(text, paint, maxWidth - 2 * padding);

      // Calcular dimensões
      float textHeight = textSize + 4; // Adiciona espaçamento entre linhas
      int totalHeight = (int) (lines.length * textHeight + 2 * padding);
      int width = maxWidth;

      // Criar o bitmap
      Bitmap bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      canvas.drawColor(android.graphics.Color.WHITE);

      // Desenhar cada linha do texto
      for (int i = 0; i < lines.length; i++) {
        float y = textHeight + padding + (i * textHeight);
        canvas.drawText(lines[i], padding, y, paint);
      }

      // Salvar o bitmap como arquivo temporário
      String fileName = "print_text_" + System.currentTimeMillis() + ".png";
      File tempFile = new File(reactContext.getCacheDir(), fileName);
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
      outputStream.flush();
      outputStream.close();

      return tempFile.getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  // Método auxiliar para quebrar texto em linhas
  private String[] breakTextIntoLines(String text, Paint paint, int maxWidth) {
    // Primeiro, dividir por quebras de linha explícitas (\n)
    String[] explicitLines = text.split("\\n");
    java.util.List<String> finalLines = new java.util.ArrayList<>();

    for (String explicitLine : explicitLines) {
      // Para cada linha explícita, quebrar por largura se necessário
      String[] words = explicitLine.split(" ");
      StringBuilder currentLine = new StringBuilder();

      for (String word : words) {
        String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
        float textWidth = paint.measureText(testLine);

        if (textWidth <= maxWidth) {
          currentLine = new StringBuilder(testLine);
        } else {
          if (currentLine.length() > 0) {
            finalLines.add(currentLine.toString());
            currentLine = new StringBuilder(word);
          } else {
            // Palavra muito longa, adiciona mesmo assim
            finalLines.add(word);
          }
        }
      }

      if (currentLine.length() > 0) {
        finalLines.add(currentLine.toString());
      }
    }

    return finalLines.toArray(new String[0]);
  }
}

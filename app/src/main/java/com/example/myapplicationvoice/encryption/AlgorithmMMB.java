package com.example.myapplicationvoice.encryption;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.github.cdimascio.dotenv.Dotenv;


/** Блочный алгоритм шифрования modular multiplication-based block cipher */
public class AlgorithmMMB
{
    /** Размер блока в байтах (128 бит) */
    private static final int BlockSize = 16;
    private final Random random = new Random();
    /** Количество подблоков */
    private static final int AmountSubblocks = 4;
    /** Ключ для шифрования */
    private static String Key;
    /** Константа */
    private static final long C = 0x2aaaaaaaL;

    /** Массив постоянных */
    private long[] c;
    /** Массив обратных постоянных */
    private long[] cReverse;
    /** Модуль (2^32 - 1) для умножения */
    private static final long Module = (long) Math.pow(2, 32) - 1;
    /** Количество нехватающих байт для шифрования */
    private int lackOfByte;

    /** Ключ в байтовом представлении */
    private byte[] keyByte;
    /** Массив подблоков данных размером 4 байт */
    private long[] x;
    /** Массив подблоков ключа размером 4 байт */
    private long[] k;


    /**
     * Инициализация начальных данных
     */
    public AlgorithmMMB()
    {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        Key = dotenv.get("ENCRYPTION_KEY");

        x = new long[AmountSubblocks];
        c = new long[AmountSubblocks];
        cReverse = new long[AmountSubblocks];

        keyByte = Key.getBytes();
        k = initializeSubblocksKey(keyByte);
        initializeConstant();
    }


    /**
     * Инициализация постоянных
     */
    private void initializeConstant()
    {
        c[0] = 0x025f1cdbL; // 39787739
        c[1] = 0x04be39b6L; // 79575478
        c[2] = 0x12f8e6d8L; // 318301912
        c[3] = 0x2f8e6d81L; // 797863297

        cReverse[0] = 0x0dad4694L; // 229459604
        cReverse[1] = 0x06d6a34aL; // 114729802
        cReverse[2] = 0x81b5a8d2L; // 2176166098
        cReverse[3] = 0x281b5a8dL; // 672881293
    }


    /**
     * Получение количество добавленных байт для шифрования
     * @return Количество добавленных байт
     */
    public int getLackOfByte()
    {
        return lackOfByte;
    }


    /**
     * Умножение по модулю
     * @param a Множимое число
     * @param b Множитель
     * @param mod Модуль
     * @return Результат умножения по модулю
     */
    private long multiplicationModule(long a, long b, long mod)
    {
        long res = 0;

        //Проверка значение больше модуля
        a %= mod;

        while (b > 0)
        {
            //Проверка на нечетность
            if ((b & 1) > 0)
            {
                res = (res + a) % mod;
            }

            a = (2 * a) % mod;

            // b = b / 2
            b >>= 1;
        }

        return res;
    }


    /**
     * Инициализация подблоков ключа
     * @param key Ключ для шифрования в байтовом представлении
     * @return Массив подблоков ключа размером 4 байт
     */
    private long[] initializeSubblocksKey(byte[] key)
    {
        long[] subKey = new long[AmountSubblocks];

        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;

        //Инициализируем подблоки ключа
        for (int i = 0; i < AmountSubblocks; i++)
        {
            firstByte = (0x000000FF & ((int) key[i * (BlockSize / AmountSubblocks)]));
            secondByte = (0x000000FF & ((int) key[i * (BlockSize / AmountSubblocks) + 1]));
            thirdByte = (0x000000FF & ((int) key[i * (BlockSize / AmountSubblocks) + 2]));
            fourthByte = (0x000000FF & ((int) key[i * (BlockSize / AmountSubblocks) + 3]));

            // unsigned int
            subKey[i] = ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
        }

        return subKey;
    }


    /**
     * Инициализация подблоков текста
     * @param textByte Данные для шифрования в байтовом представлении
     * @return Массив подблоков данных размером 4 байт
     */
    private long[] initializeSubblocksText(byte[] textByte)
    {
        long[] subText = new long[AmountSubblocks];

        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;

        //Инициализируем подблоки текста
        for (int i = 0; i < AmountSubblocks; i++)
        {
            firstByte = (0x000000FF & ((int) textByte[i * (BlockSize / AmountSubblocks)]));
            secondByte = (0x000000FF & ((int) textByte[i * (BlockSize / AmountSubblocks) + 1]));
            thirdByte = (0x000000FF & ((int) textByte[i * (BlockSize / AmountSubblocks) + 2]));
            fourthByte = (0x000000FF & ((int) textByte[i * (BlockSize / AmountSubblocks) + 3]));

            // unsigned int
            subText[i] = ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
        }

        return subText;
    }


    /**
     * Конвертация массива беззнаковых целых чисел в массив байт
     * @param x Массив подблоков данных размером 4 байт
     * @return Данные для шифрования в байтовом представлении
     */
    private byte[] convertArrayLongToByte(long[] x)
    {
        byte[] blockBytes = new byte[BlockSize];

        for (int i = 0; i < AmountSubblocks; i++)
        {
            blockBytes[i * (BlockSize / AmountSubblocks)] = (byte) ((x[i] & 0xFF000000L) >> 24);
            blockBytes[i * (BlockSize / AmountSubblocks) + 1] = (byte) ((x[i] & 0x00FF0000L) >> 16);
            blockBytes[i * (BlockSize / AmountSubblocks) + 2] = (byte) ((x[i] & 0x0000FF00L) >> 8);
            blockBytes[i * (BlockSize / AmountSubblocks) + 3] = (byte) (x[i] & 0x000000FFL);
        }

        return blockBytes;
    }


    /**
     * Функция шифрования в 6 раундов
     * @param x Массив подблоков данных размером 4 байт
     */
    private void encryptedFunction(long[] x)
    {
        int index;
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 0) % 4;
            x[i] ^= k[index];
        }

        nonlinearFunctionEncryption(x);
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 1) % 4;
            x[i] ^= k[index];
        }

        nonlinearFunctionEncryption(x);
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 2) % 4;
            x[i] ^= k[index];
        }

        nonlinearFunctionEncryption(x);
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 3) % 4;
            x[i] ^= k[index];
        }

        nonlinearFunctionEncryption(x);
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 0) % 4;
            x[i] ^= k[index];
        }

        nonlinearFunctionEncryption(x);
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 1) % 4;
            x[i] ^= k[index];
        }

        nonlinearFunctionEncryption(x);
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 2) % 4;
            x[i] ^= k[index];
        }
    }


    /**
     * Нелинейная функция шифрования
     * @param x Массив подблоков данных размером 4 байт
     */
    private void nonlinearFunctionEncryption(long[] x)
    {
        int indexBefore, indexAfter;

        //1 этап
        for (int i = 0; i < AmountSubblocks; i++)
        {
            if (x[i] < Module)
            {
                x[i] = multiplicationModule(x[i], c[i], Module);
            }
            else
            {
                x[i] = Module;
            }
        }

        //2 этап
        if (x[0] % 2 == 1)
        {
            x[0] = x[0] ^ C;
        }
        if (x[3] % 2 == 0)
        {
            x[3] = x[3] ^ C;
        }

        //3 этап
        for (int i = 0; i < AmountSubblocks; i++)
        {
            indexBefore = (i - 1) % 4;
            indexAfter = (i + 1) % 4;

            if (indexBefore == -1)
            {
                indexBefore = AmountSubblocks - 1;
            }

            x[i] = x[indexBefore] ^ x[i] ^ x[indexAfter];
        }
    }


    /**
     * Функция расшифровки в 6 раундов
     * @param x Массив подблоков зашифрованных данных размером 4 байт
     */
    private void decryptedFunction(long[] x)
    {
        int index;

        //Xi = Xi XOR Ki+2
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 2) % 4;
            x[i] ^= k[index];
        }
        //f(x0,x1,x2,x3)
        nonlinearFunctionDecryption(x);

        //Xi = Xi XOR Ki+1
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 1) % 4;
            x[i] ^= k[index];
        }
        //f(x0,x1,x2,x3)
        nonlinearFunctionDecryption(x);

        //Xi = Xi XOR Ki
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 0) % 4;
            x[i] ^= k[index];
        }
        //f(x0,x1,x2,x3)
        nonlinearFunctionDecryption(x);

        //Xi = Xi XOR Ki+3
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 3) % 4;
            x[i] ^= k[index];
        }
        //f(x0,x1,x2,x3)
        nonlinearFunctionDecryption(x);

        //Xi = Xi XOR Ki+2
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 2) % 4;
            x[i] ^= k[index];
        }
        //f(x0,x1,x2,x3)
        nonlinearFunctionDecryption(x);

        //Xi = Xi XOR Ki+1
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 1) % 4;
            x[i] ^= k[index];
        }
        //f(x0,x1,x2,x3)
        nonlinearFunctionDecryption(x);

        //Xi = Xi XOR Ki
        for (int i = 0; i < AmountSubblocks; i++)
        {
            index = (i + 0) % 4;
            x[i] ^= k[index];
        }
    }


    /**
     * Нелинейная функция расшифровки
     * @param x Массив подблоков зашифрованных данных размером 4 байт
     */
    private void nonlinearFunctionDecryption(long[] x)
    {
        int indexBefore, indexAfter;

        //3 этап
        for (int i = AmountSubblocks - 1; i >= 0; i--)
        {
            indexBefore = (i - 1) % 4;
            indexAfter = (i + 1) % 4;

            if (indexBefore == -1)
            {
                indexBefore = AmountSubblocks - 1;
            }

            x[i] = x[indexBefore] ^ x[i] ^ x[indexAfter];
        }

        //2 этап
        if (x[0] % 2 == 1)
        {
            x[0] = x[0] ^ C;
        }
        if (x[3] % 2 == 0)
        {
            x[3] = x[3] ^ C;
        }

        //1 этап
        for (int i = 0; i < AmountSubblocks; i++)
        {
            if (x[i] < Module)
            {
                x[i] = multiplicationModule(x[i], cReverse[i], Module);
            }
            else
            {
                x[i] = Module;
            }
        }
    }


    /**
     * Последовательное шифрование данных алгоритмом MMB
     * @param messageByte Исходные данные в байтовом представлении
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessage(byte[] messageByte)
    {
        // Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] blockByte;
        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        for (int index = 0; index < messageByte.length; index += BlockSize)
        {
            //Расчет количество оставшихся байт в сообщении
            amountBytesLack = messageByte.length - index;

            blockByte = new byte[BlockSize];

            //Берем блок из 128 бит
            for (int i = 0; i < BlockSize && i < amountBytesLack; i++)
            {
                blockByte[i] = messageByte[index + i];
            }

            //Проверка на нехватку байт для шифрования
            if (amountBytesLack < BlockSize)
            {
                for (int j = amountBytesLack; j < BlockSize; j++)
                {
                    blockByte[j] = (byte) (random.nextInt(256) - 128);
                }
            }

            //Получение подблоков текста
            x = initializeSubblocksText(blockByte);

            //Шифрование
            encryptedFunction(x);

            //Получение блока из подблоков
            blockByte = convertArrayLongToByte(x);

            //Запись блока в зашифрованное сообщение
            for (int i = 0; i < blockByte.length; i++)
            {
                bytesEncrypted[index + i] = blockByte[i];
            }
        }

        return bytesEncrypted;
    }


    /**
     * Параллельное шифрование данных алгоритмом MMB с помощью Stream
     * @param messageByte Исходные данные в байтовом представлении
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessageParallelWithStream(byte[] messageByte)
    {
        //Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        IntStream.range(0, bytesEncrypted.length / BlockSize).parallel()
                .forEach(index -> {

                    byte[] blockByte = new byte[BlockSize];

                    //Берем блок из 128 бит
                    for (int i = 0; i < BlockSize && i < messageByte.length - index * BlockSize; i++)
                    {
                        blockByte[i] = messageByte[index * BlockSize + i];
                    }

                    //Проверка на нехватку байт для шифрования
                    if (messageByte.length - index * BlockSize < BlockSize)
                    {
                        for (int j = messageByte.length - index * BlockSize; j < BlockSize; j++)
                        {
                            blockByte[j] = (byte)(random.nextInt(256) - 128);
                        }
                    }

                    //Получение подблоков текста
                    long[] x = initializeSubblocksText(blockByte);

                    //Шифрование
                    encryptedFunction(x);

                    //Получение блока из подблоков
                    blockByte = convertArrayLongToByte(x);

                    //Запись блока в зашифрованное сообщение
                    for (int i = 0; i < blockByte.length; i++)
                    {
                        bytesEncrypted[index * BlockSize + i] = blockByte[i];
                    }
                } );

        return bytesEncrypted;
    }


    /**
     * Параллельное шифрование данных алгоритмом MMB с помощью Stream Support
     * @param messageByte Исходные данные в байтовом представлении
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessageParallelWithStreamSupport(byte[] messageByte)
    {
        //Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        Iterable<Integer> iterable = () -> IntStream.range(0, bytesEncrypted.length / BlockSize).iterator();
        Stream<Integer> stream = StreamSupport.stream(iterable.spliterator(), true);


        stream.forEach(index ->
        {
            byte[] blockByte = new byte[BlockSize];

            //Берем блок из 128 бит
            for (int i = 0; i < BlockSize && i < messageByte.length - index * BlockSize; i++)
            {
                blockByte[i] = messageByte[index * BlockSize + i];
            }

            //Проверка на нехватку байт для шифрования
            if (messageByte.length - index * BlockSize < BlockSize)
            {
                for (int j = messageByte.length - index * BlockSize; j < BlockSize; j++)
                {
                    blockByte[j] = (byte)(random.nextInt(256) - 128);
                }
            }

            //Получение подблоков текста
            long[] x = initializeSubblocksText(blockByte);

            //Шифрование
            encryptedFunction(x);

            //Получение блока из подблоков
            blockByte = convertArrayLongToByte(x);

            //Запись блока в зашифрованное сообщение
            for (int i = 0; i < blockByte.length; i++)
            {
                bytesEncrypted[index * BlockSize + i] = blockByte[i];
            }
        });

        return bytesEncrypted;
    }


    /**
     * Параллельное шифрование данных (модифицированное) алгоритмом MMB с помощью Stream
     * @param messageByte Исходные данные в байтовом представлении
     * @param amountThreads Количество потоков используемых при шифровании
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessageParallelWithStreamModificate(byte[] messageByte, int amountThreads)
    {
        //Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        // Разделение на блоки для вычислений
        int amountBlocks = bytesEncrypted.length / BlockSize;
        int[] amountLinesArray = new int[amountThreads];
        for (int i = 0; i < amountLinesArray.length; i++)
        {
            amountLinesArray[i] = amountBlocks / amountThreads;
        }
        for (int i = 0; i < amountBlocks % amountThreads; i++)
        {
            amountLinesArray[i]++;
        }
        int[] startBlock = new int[amountThreads];
        int[] endBlock = new int[amountThreads];
        startBlock[0] = 0;
        endBlock[0] = amountLinesArray[0];
        for (int i = 1; i < amountThreads; i++)
        {
            startBlock[i] = startBlock[i - 1] + amountLinesArray[i - 1];
            endBlock[i] = startBlock[i] + amountLinesArray[i];
        }

        IntStream.range(0, amountThreads).parallel()
                .forEach(index -> {

                    byte[] blockByte = new byte[BlockSize];

                    for (int j = startBlock[index]; j < endBlock[index]; j++)
                    {
                        //Берем блок из 128 бит
                        for (int i = 0; i < BlockSize && i < messageByte.length - j * BlockSize; i++)
                        {
                            blockByte[i] = messageByte[j * BlockSize + i];
                        }

                        //Проверка на нехватку байт для шифрования
                        if (messageByte.length - j * BlockSize < BlockSize)
                        {
                            for (int k = messageByte.length - j * BlockSize; k < BlockSize; k++)
                            {
                                blockByte[k] = (byte)(random.nextInt(256) - 128);
                            }
                        }

                        //Получение подблоков текста
                        long[] x = initializeSubblocksText(blockByte);

                        //Шифрование
                        encryptedFunction(x);

                        //Получение блока из подблоков
                        blockByte = convertArrayLongToByte(x);

                        //Запись блока в зашифрованное сообщение
                        for (int i = 0; i < blockByte.length; i++)
                        {
                            bytesEncrypted[j * BlockSize + i] = blockByte[i];
                        }
                    }
                } );

        return bytesEncrypted;
    }


    /**
     * Параллельное шифрование данных (модифицированное) алгоритмом MMB с помощью Stream Support
     * @param messageByte Исходные данные в байтовом представлении
     * @param amountThreads Количество потоков используемых при шифровании
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessageParallelWithStreamSupportModificate(byte[] messageByte, int amountThreads)
    {
        //Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        // Разделение на блоки для вычислений
        int amountBlocks = bytesEncrypted.length / BlockSize;
        int[] amountLinesArray = new int[amountThreads];
        for (int i = 0; i < amountLinesArray.length; i++)
        {
            amountLinesArray[i] = amountBlocks / amountThreads;
        }
        for (int i = 0; i < amountBlocks % amountThreads; i++)
        {
            amountLinesArray[i]++;
        }
        int[] startBlock = new int[amountThreads];
        int[] endBlock = new int[amountThreads];
        startBlock[0] = 0;
        endBlock[0] = amountLinesArray[0];
        for (int i = 1; i < amountThreads; i++)
        {
            startBlock[i] = startBlock[i - 1] + amountLinesArray[i - 1];
            endBlock[i] = startBlock[i] + amountLinesArray[i];
        }

        Iterable<Integer> iterable = () -> IntStream.range(0, amountThreads).iterator();
        Stream<Integer> stream = StreamSupport.stream(iterable.spliterator(), true);

        stream.forEach(index -> {

            byte[] blockByte = new byte[BlockSize];

            for (int j = startBlock[index]; j < endBlock[index]; j++)
            {
                //Берем блок из 128 бит
                for (int i = 0; i < BlockSize && i < messageByte.length - j * BlockSize; i++)
                {
                    blockByte[i] = messageByte[j * BlockSize + i];
                }

                //Проверка на нехватку байт для шифрования
                if (messageByte.length - j * BlockSize < BlockSize)
                {
                    for (int k = messageByte.length - j * BlockSize; k < BlockSize; k++)
                    {
                        blockByte[k] = (byte)(random.nextInt(256) - 128);
                    }
                }

                //Получение подблоков текста
                long[] x = initializeSubblocksText(blockByte);

                //Шифрование
                encryptedFunction(x);

                //Получение блока из подблоков
                blockByte = convertArrayLongToByte(x);

                //Запись блока в зашифрованное сообщение
                for (int i = 0; i < blockByte.length; i++)
                {
                    bytesEncrypted[j * BlockSize + i] = blockByte[i];
                }
            }
        } );

        return bytesEncrypted;
    }


    /**
     * Последовательное расшифровка данных алгоритмом MMB
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт к исходным данным
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessage(byte[] encryptedMessageByte, int lackByte)
    {
        int amountBytes = 0;
        byte[] blockByte;

        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        for (int index = 0; index < bytesDecrypted.length; index += BlockSize)
        {
            blockByte = new byte[BlockSize];

            // Расчет количество оставшихся байт в сообщении
            amountBytes = bytesDecrypted.length - index;

            // Берем блок из 128 бит
            for (int i = 0; i < BlockSize; i++)
            {
                blockByte[i] = encryptedMessageByte[index + i];
            }

            // Получение подблоков текста
            x = initializeSubblocksText(blockByte);

            // Расшифровка
            decryptedFunction(x);

            //Получение блока из подблоков
            blockByte = convertArrayLongToByte(x);

            //Запись блока в зашифрованное сообщение
            for (int i = 0; (i < blockByte.length) && (i < amountBytes); i++)
            {
                bytesDecrypted[index + i] = blockByte[i];
            }
        }

        return bytesDecrypted;
    }


    /**
     * Параллельная расшифровка данных алгоритмом MMB с помощью Stream
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт к исходым данным
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessageParallelWithStream(byte[] encryptedMessageByte, int lackByte)
    {
        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        IntStream.range(0, encryptedMessageByte.length / BlockSize).parallel()
                .forEach(index ->{

                    byte[] blockByte = new byte[BlockSize];

                    // Берем блок из 128 бит
                    for (int i = 0; i < BlockSize; i++)
                    {
                        blockByte[i] = encryptedMessageByte[index * BlockSize + i];
                    }

                    // Получение подблоков текста
                    long[] x = initializeSubblocksText(blockByte);

                    // Расшифровка
                    decryptedFunction(x);

                    //Получение блока из подблоков
                    blockByte = convertArrayLongToByte(x);

                    //Запись блока в зашифрованное сообщение
                    for (int i = 0; (i < blockByte.length) && (i < bytesDecrypted.length - index * BlockSize); i++)
                    {
                        bytesDecrypted[index * BlockSize + i] = blockByte[i];
                    }
                });

        return bytesDecrypted;
    }


    /**
     * Параллельная расшифровка данных алгоритмом MMB с помощью Stream Support
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт к исходным данным
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessageParallelWithStreamSupport(byte[] encryptedMessageByte, int lackByte)
    {
        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        Iterable<Integer> iterable = () -> IntStream.range(0, encryptedMessageByte.length / BlockSize).iterator();
        Stream<Integer> stream = StreamSupport.stream(iterable.spliterator(), true);

        stream.forEach(index ->{

            byte[] blockByte = new byte[BlockSize];

            // Берем блок из 128 бит
            for (int i = 0; i < BlockSize; i++)
            {
                blockByte[i] = encryptedMessageByte[index * BlockSize + i];
            }

            // Получение подблоков текста
            long[] x = initializeSubblocksText(blockByte);

            // Расшифровка
            decryptedFunction(x);

            //Получение блока из подблоков
            blockByte = convertArrayLongToByte(x);

            //Запись блока в зашифрованное сообщение
            for (int i = 0; (i < blockByte.length) && (i < bytesDecrypted.length - index * BlockSize); i++)
            {
                bytesDecrypted[index * BlockSize + i] = blockByte[i];
            }
        });

        return bytesDecrypted;
    }


    /**
     * Параллельная расшифровка данных (модифицированная) алгоритмом MMB с помощью Stream
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт к исходным данным
     * @param amountThreads Количество потоков используемых для расшифровки
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessageParallelWithStreamModificate(byte[] encryptedMessageByte, int lackByte, int amountThreads)
    {
        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        // Разделение на блоки для вычислений
        int amountBlocks = encryptedMessageByte.length / BlockSize;
        int[] amountLinesArray = new int[amountThreads];
        for (int i = 0; i < amountLinesArray.length; i++)
        {
            amountLinesArray[i] = amountBlocks / amountThreads;
        }
        for (int i = 0; i < amountBlocks % amountThreads; i++)
        {
            amountLinesArray[i]++;
        }
        int[] startBlock = new int[amountThreads];
        int[] endBlock = new int[amountThreads];
        startBlock[0] = 0;
        endBlock[0] = amountLinesArray[0];
        for (int i = 1; i < amountThreads; i++)
        {
            startBlock[i] = startBlock[i - 1] + amountLinesArray[i - 1];
            endBlock[i] = startBlock[i] + amountLinesArray[i];
        }

        IntStream.range(0, amountThreads).parallel()
                .forEach(index ->{

                    byte[] blockByte = new byte[BlockSize];

                    for (int j = startBlock[index]; j < endBlock[index]; j++)
                    {
                        // Берем блок из 128 бит
                        for (int i = 0; i < BlockSize; i++)
                        {
                            blockByte[i] = encryptedMessageByte[j * BlockSize + i];
                        }

                        // Получение подблоков текста
                        long[] x = initializeSubblocksText(blockByte);

                        // Расшифровка
                        decryptedFunction(x);

                        //Получение блока из подблоков
                        blockByte = convertArrayLongToByte(x);

                        //Запись блока в зашифрованное сообщение
                        for (int i = 0; (i < blockByte.length) && (i < bytesDecrypted.length - j * BlockSize); i++)
                        {
                            bytesDecrypted[j * BlockSize + i] = blockByte[i];
                        }
                    }
                });

        return bytesDecrypted;
    }


    /**
     * Параллельная расшифровка данных (модифицированная) параллельным алгоритмом MMB с помощью Stream Support
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт к исходным данным
     * @param amountThreads Количество потоков используемых при расшифровке
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessageParallelWithStreamSupportModificate(byte[] encryptedMessageByte, int lackByte, int amountThreads)
    {
        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        // Разделение на блоки для вычислений
        int amountBlocks = encryptedMessageByte.length / BlockSize;
        int[] amountLinesArray = new int[amountThreads];
        for (int i = 0; i < amountLinesArray.length; i++)
        {
            amountLinesArray[i] = amountBlocks / amountThreads;
        }
        for (int i = 0; i < amountBlocks % amountThreads; i++)
        {
            amountLinesArray[i]++;
        }
        int[] startBlock = new int[amountThreads];
        int[] endBlock = new int[amountThreads];
        startBlock[0] = 0;
        endBlock[0] = amountLinesArray[0];
        for (int i = 1; i < amountThreads; i++)
        {
            startBlock[i] = startBlock[i - 1] + amountLinesArray[i - 1];
            endBlock[i] = startBlock[i] + amountLinesArray[i];
        }

        Iterable<Integer> iterable = () -> IntStream.range(0, amountThreads).iterator();
        Stream<Integer> stream = StreamSupport.stream(iterable.spliterator(), true);

        stream.forEach(index ->{

            byte[] blockByte = new byte[BlockSize];

            for (int j = startBlock[index]; j < endBlock[index]; j++)
            {
                // Берем блок из 128 бит
                for (int i = 0; i < BlockSize; i++)
                {
                    blockByte[i] = encryptedMessageByte[j * BlockSize + i];
                }

                // Получение подблоков текста
                long[] x = initializeSubblocksText(blockByte);

                // Расшифровка
                decryptedFunction(x);

                //Получение блока из подблоков
                blockByte = convertArrayLongToByte(x);

                //Запись блока в зашифрованное сообщение
                for (int i = 0; (i < blockByte.length) && (i < bytesDecrypted.length - j * BlockSize); i++)
                {
                    bytesDecrypted[j * BlockSize + i] = blockByte[i];
                }
            }
        });

        return bytesDecrypted;
    }


    /** Шифрование в отдельном потоке для Executor Service */
    class MyRunnableEncryption implements Runnable
    {
        /** Индекс блока */
        int index;
        /** Исходные данные в байтовом представлении */
        byte[] messageByte;
        /** Зашифрованные данные в байтовом представлении */
        byte[] bytesEncrypted;


        /**
         * Инициализация данных
         * @param index Индекс блока
         * @param messageByte Исходные данные в байтовом представлении
         * @param bytesEncrypted Зашифрованные данные в байтовом представлении
         */
        MyRunnableEncryption(int index, byte[] messageByte, byte[] bytesEncrypted)
        {
            this.index = index;
            this.messageByte = messageByte;
            this.bytesEncrypted = bytesEncrypted;
        }


        @Override
        public void run()
        {
            byte[] blockByte = new byte[BlockSize];

            //Берем блок из 128 бит
            for (int i = 0; i < BlockSize && i < messageByte.length - index * BlockSize; i++)
            {
                blockByte[i] = messageByte[index * BlockSize + i];
            }

            //Проверка на нехватку байт для шифрования
            if (messageByte.length - index * BlockSize < BlockSize)
            {
                for (int j = messageByte.length - index * BlockSize; j < BlockSize; j++)
                {
                    blockByte[j] = (byte)(random.nextInt(256) - 128);
                }
            }

            //Получение подблоков текста
            long[] x = initializeSubblocksText(blockByte);

            //Шифрование
            encryptedFunction(x);

            //Получение блока из подблоков
            blockByte = convertArrayLongToByte(x);

            //Запись блока в зашифрованное сообщение
            for (int i = 0; i < blockByte.length; i++)
            {
                bytesEncrypted[index * BlockSize + i] = blockByte[i];
            }
        }
    }


    /** Модифицированное шифрование в отдельном потоке для Executor Service */
    class MyRunnableEncryptionModificate implements Runnable
    {
        /** Номер потока */
        int index;
        /** Исходные данные в байтовом представлении */
        byte[] messageByte;
        /** Зашифрованные данные в байтовом представлении */
        byte[] bytesEncrypted;
        /** Индекс блоков для начала расчетов для каждого потока */
        int[] startBlock;
        /** Индекс блоков для конца расчетов для каждого потока */
        int[] endBlock;


        /**
         * Инициализация данных
         * @param index Номер потока
         * @param messageByte Исходные данные в байтовом представлении
         * @param bytesEncrypted Зашифрованные данные в байтовом представлении
         * @param startBlock Индекс блоков для начала расчетов для каждого потока
         * @param endBlock Индекс блоков для конца расчетов для каждого потока
         */
        MyRunnableEncryptionModificate(int index, byte[] messageByte, byte[] bytesEncrypted, int[] startBlock, int[] endBlock)
        {
            this.index = index;
            this.startBlock = startBlock;
            this.endBlock = endBlock;
            this.messageByte = messageByte;
            this.bytesEncrypted = bytesEncrypted;
        }


        @Override
        public void run()
        {
            byte[] blockByte = new byte[BlockSize];

            for (int j = startBlock[index]; j < endBlock[index]; j++)
            {
                //Берем блок из 128 бит
                for (int i = 0; i < BlockSize && i < messageByte.length - j * BlockSize; i++)
                {
                    blockByte[i] = messageByte[j * BlockSize + i];
                }

                //Проверка на нехватку байт для шифрования
                if (messageByte.length - j * BlockSize < BlockSize)
                {
                    for (int k = messageByte.length - j * BlockSize; k < BlockSize; k++)
                    {
                        blockByte[k] = (byte)(random.nextInt(256) - 128);
                    }
                }

                //Получение подблоков текста
                long[] x = initializeSubblocksText(blockByte);

                //Шифрование
                encryptedFunction(x);

                //Получение блока из подблоков
                blockByte = convertArrayLongToByte(x);

                //Запись блока в зашифрованное сообщение
                for (int i = 0; i < blockByte.length; i++)
                {
                    bytesEncrypted[j * BlockSize + i] = blockByte[i];
                }
            }
        }
    }


    /** Расшифровка в отдельном потоке для Executor Service */
    class MyRunnableDecryption implements Runnable
    {
        /** Индекс блока */
        int index;
        /** Зашифрованные данные в байтовом представлении */
        byte[] encryptedMessageByte;
        /** Расшифрованные данные в байтовом представлении */
        byte[] bytesDecrypted;


        /**
         * Инициализация данных
         * @param index Индекс блока
         * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
         * @param bytesDecrypted Расшифрованные данные в байтовом представлении
         */
        MyRunnableDecryption(int index, byte[] encryptedMessageByte, byte[] bytesDecrypted)
        {
            this.index = index;
            this.encryptedMessageByte = encryptedMessageByte;
            this.bytesDecrypted = bytesDecrypted;
        }


        @Override
        public void run()
        {
            byte[] blockByte = new byte[BlockSize];

            // Берем блок из 128 бит
            for (int i = 0; i < BlockSize; i++)
            {
                blockByte[i] = encryptedMessageByte[index * BlockSize + i];
            }

            // Получение подблоков текста
            long[] x = initializeSubblocksText(blockByte);

            // Расшифровка
            decryptedFunction(x);

            //Получение блока из подблоков
            blockByte = convertArrayLongToByte(x);

            //Запись блока в зашифрованное сообщение
            for (int i = 0; (i < blockByte.length) && (i < bytesDecrypted.length - index * BlockSize); i++)
            {
                bytesDecrypted[index * BlockSize + i] = blockByte[i];
            }
        }
    }


    /** Модифицированная расшифровка в отдельном потоке для Executor Service */
    class MyRunnableDecryptionModificate implements Runnable
    {
        /** Номер потока */
        int index;
        /** Зашифрованные данные в байтовом представлении */
        byte[] encryptedMessageByte;
        /** Расшифрованные данные в байтовом представлении */
        byte[] bytesDecrypted;
        /** Индекс блоков для начала расчетов для каждого потока */
        int[] startBlock;
        /** Индекс блоков для конца расчетов для каждого потока */
        int[] endBlock;


        /**
         * Инициализация данных
         * @param index Номер потока
         * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
         * @param bytesDecrypted Расшифрованные данные в байтовом представлении
         * @param startBlock Индекс блоков для начала расчетов для каждого потока
         * @param endBlock Индекс блоков для конца расчетов для каждого потока
         */
        MyRunnableDecryptionModificate(int index, byte[] encryptedMessageByte, byte[] bytesDecrypted, int[] startBlock, int[] endBlock)
        {
            this.index = index;
            this.startBlock = startBlock;
            this.endBlock = endBlock;
            this.encryptedMessageByte = encryptedMessageByte;
            this.bytesDecrypted = bytesDecrypted;
        }


        @Override
        public void run()
        {
            byte[] blockByte = new byte[BlockSize];

            for (int j = startBlock[index]; j < endBlock[index]; j++)
            {
                // Берем блок из 128 бит
                for (int i = 0; i < BlockSize; i++)
                {
                    blockByte[i] = encryptedMessageByte[j * BlockSize + i];
                }

                // Получение подблоков текста
                long[] x = initializeSubblocksText(blockByte);

                // Расшифровка
                decryptedFunction(x);

                //Получение блока из подблоков
                blockByte = convertArrayLongToByte(x);

                //Запись блока в зашифрованное сообщение
                for (int i = 0; (i < blockByte.length) && (i < bytesDecrypted.length - j * BlockSize); i++)
                {
                    bytesDecrypted[j * BlockSize + i] = blockByte[i];
                }
            }
        }
    }


    /**
     * Параллельное шифрование данных алгоритмом MMB с помощью Executor Service
     * @param messageByte Исходные данные в байтовом представлении
     * @param amountThreads Количество потоков используемых для шифрования
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessageParallelWithExecutorService(byte[] messageByte, int amountThreads)
    {
        //Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        ExecutorService executorService = Executors.newFixedThreadPool(amountThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int index = 0; index < bytesEncrypted.length / BlockSize; index++)
        {
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    new MyRunnableEncryption(index, messageByte, bytesEncrypted), executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        return bytesEncrypted;
    }


    /**
     * Параллельное шифрование данных (модифицированное) алгоритмом MMB с помощью Executor Service
     * @param messageByte Исходные данные в байтовом представлении
     * @param amountThreads Количество потоков используемых для шифрования
     * @return Зашифрованные данные в байтовом представлении
     */
    public byte[] encryptionMessageParallelWithExecutorServiceModificate(byte[] messageByte, int amountThreads)
    {
        //Определения необходимого выделения байт
        int amountBytesLack = messageByte.length % BlockSize;
        lackOfByte = 0;
        if (amountBytesLack != 0)
        {
            lackOfByte = BlockSize - amountBytesLack;
        }

        byte[] bytesEncrypted = new byte[messageByte.length + lackOfByte];

        // Разделение на блоки для вычислений
        int amountBlocks = bytesEncrypted.length / BlockSize;
        int[] amountLinesArray = new int[amountThreads];
        for (int i = 0; i < amountLinesArray.length; i++)
        {
            amountLinesArray[i] = amountBlocks / amountThreads;
        }
        for (int i = 0; i < amountBlocks % amountThreads; i++)
        {
            amountLinesArray[i]++;
        }
        int[] startBlock = new int[amountThreads];
        int[] endBlock = new int[amountThreads];
        startBlock[0] = 0;
        endBlock[0] = amountLinesArray[0];
        for (int i = 1; i < amountThreads; i++)
        {
            startBlock[i] = startBlock[i - 1] + amountLinesArray[i - 1];
            endBlock[i] = startBlock[i] + amountLinesArray[i];
        }

        ExecutorService executorService = Executors.newFixedThreadPool(amountThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int index = 0; index < amountThreads; index++)
        {
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    new MyRunnableEncryptionModificate(index, messageByte, bytesEncrypted, startBlock, endBlock), executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        return bytesEncrypted;
    }


    /**
     * Параллельная расшифровка данных алгоритмом MMB с помощью Executor Service
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт для шифрования
     * @param amountThreads Количество потоков используемых для расшифровки
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessageParallelWithExecutorService(byte[] encryptedMessageByte, int lackByte, int amountThreads)
    {
        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        ExecutorService executorService = Executors.newFixedThreadPool(amountThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int index = 0; index < encryptedMessageByte.length / BlockSize; index++)
        {
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    new MyRunnableDecryption(index, encryptedMessageByte, bytesDecrypted), executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        return bytesDecrypted;
    }


    /**
     * Параллельная расшифровка данных (модифицированная) алгоритмом MMB с помощью Executor Service
     * @param encryptedMessageByte Зашифрованные данные в байтовом представлении
     * @param lackByte Количество добавленных байт для шифрования
     * @param amountThreads Количество потоков используемых для расшифровки
     * @return Расшифрованные данные в байтовом представлении
     */
    public byte[] decryptionMessageParallelWithExecutorServiceModificate(byte[] encryptedMessageByte, int lackByte, int amountThreads)
    {
        byte[] bytesDecrypted = new byte[encryptedMessageByte.length - lackByte];

        // Разделение на блоки для вычислений
        int amountBlocks = encryptedMessageByte.length / BlockSize;
        int[] amountLinesArray = new int[amountThreads];
        for (int i = 0; i < amountLinesArray.length; i++)
        {
            amountLinesArray[i] = amountBlocks / amountThreads;
        }
        for (int i = 0; i < amountBlocks % amountThreads; i++)
        {
            amountLinesArray[i]++;
        }
        int[] startBlock = new int[amountThreads];
        int[] endBlock = new int[amountThreads];
        startBlock[0] = 0;
        endBlock[0] = amountLinesArray[0];
        for (int i = 1; i < amountThreads; i++)
        {
            startBlock[i] = startBlock[i - 1] + amountLinesArray[i - 1];
            endBlock[i] = startBlock[i] + amountLinesArray[i];
        }

        ExecutorService executorService = Executors.newFixedThreadPool(amountThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int index = 0; index < amountThreads; index++)
        {
            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    new MyRunnableDecryptionModificate(index, encryptedMessageByte, bytesDecrypted, startBlock, endBlock), executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        return bytesDecrypted;
    }
}

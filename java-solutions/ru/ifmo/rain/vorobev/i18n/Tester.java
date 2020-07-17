package ru.ifmo.rain.vorobev.i18n;


import org.junit.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

public class Tester {

    private static class MyTest {

        String text;
        String locale;
        BlockStats answers;

        public MyTest(String locale, String text, BlockStats answers) {
            this.locale = locale;
            this.text = text;
            this.answers = answers;
        }

    }

    final Random random = new Random();
    final String nullS = "-";
    final String tmpTestFile = "tmpTestFile";
    final Map<String, Integer> ids = Map.of(
            "sentences", 0,
            "lines", 1,
            "words", 2,
            "numbers", 3,
            "currencies", 4,
            "dates", 5);

    @Test
    public void checkNumbersBlock() {
        String key = "numbers";
        MyTest[] tests = {
                new MyTest("en_US",
                        "A 1. 456. I want 1, 2 or 3.",
                        new BlockStats(5, 4, "1", "456", 7, "1", "456")),

                new MyTest("en_US",
                        "Hi man.\nI have $322 and 3.",
                        new BlockStats(1, 1, "3", "3", 1, "3", "3")),

                new MyTest("es_ES",
                        "Hola dar 10.789,80 € rápido.",
                        new BlockStats(0, 0, nullS, nullS, 0, nullS, nullS)),

                new MyTest("ar_EG",
                        "١ ٢ ٣, ٤ ٥ ٦, ١ ٢ ٣؟",
                        new BlockStats(9, 6, "١", "٦", 9, "١", "١")),

                new MyTest("ar_PS",
                        "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                        new BlockStats(1, 1, "٣٣٣", "٣٣٣", 3, "٣٣٣", "٣٣٣")),

                generateBigRandomTest(key)
        };

        for (MyTest test : tests) {
            if (isTestFailed(key, test)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void checkWordsBlock() {
        String key = "words";
        MyTest[] tests = {
                new MyTest("en_US",
                        "Hi. Hi, hi, hi.",
                        new BlockStats(4, 1, "Hi", "Hi", 8, "Hi", "Hi")),

                new MyTest("en_US",
                        "One to one.\nOne to one.\nOne to one.\nOne to one.\n",
                        new BlockStats(12, 2, "One", "to", 32, "to", "One")),

                new MyTest("zh_CN",
                        "請給5塊。",
                        new BlockStats(3, 3, "5", "請給", 4, "5", "請給")),

                new MyTest("ar_AE",
                        "هل يغفر لي الله لو شخرت قليلا؟",
                        new BlockStats(7, 7, "الله", "يغفر", 23, "لو", "قليلا")),

                generateBigRandomTest(key)
        };

        for (MyTest test : tests) {
            if (isTestFailed(key, test)) {
                Assert.fail();
            }
        }
    }


    @Test
    public void checkDatesBlock(){
        String key = "dates";
        MyTest[] tests = {
                new MyTest("en_US",
                        "On Monday, May 25, 2020. On May 19, 2019 or 5/25/21",
                        new BlockStats(3, 3, "May 19, 2019", "5/25/21", 39,
                                "5/25/21", "Monday, May 25, 2020")),

                new MyTest("en_US",
                        "On Monday, 25 May, 2020. On May 19, 2019 or 5.25.21",
                        new BlockStats(1, 1, "May 19, 2019", "May 19, 2019", 12,
                                "May 19, 2019", "May 19, 2019")),

                new MyTest("zh_CN",
                        "2020年5月25日",
                        new BlockStats(1, 1, "2020年5月25日", "2020年5月25日",
                                10, "2020年5月25日", "2020年5月25日")),
                new MyTest("ar_PS",
                        "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                        new BlockStats(1, 1, "٢٥\u200F/٠٥\u200F/٢٠٢٠",
                                "٢٥\u200F/٠٥\u200F/٢٠٢٠", 12, "٢٥\u200F/٠٥\u200F/٢٠٢٠",
                                "٢٥\u200F/٠٥\u200F/٢٠٢٠")),

        };

        for (MyTest test : tests) {
            if (isTestFailed(key, test)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void checkCurrenciesBlock() {
        String key = "currencies";
        MyTest[] tests = {
                new MyTest("en_US",
                        "$10 give.",
                        new BlockStats(1, 1, "$10", "$10", 3, "$10", "$10")),

                new MyTest("en_US",
                "Give me $10 or $ 10.",
                        new BlockStats(1, 1, "$10", "$10", 3, "$10", "$10")),

                new MyTest("en_US",
                        "Give me $10 or 10 € pls.\nThen $111 and $111, go go go.",
                        new BlockStats(3, 2, "$10", "$111", 11, "$10", "$111")),

                new MyTest("es_ES",
                        "Hola 25.01.2048, hola dar 10.789,80 € rápido.\nHola 25.01.2048, hola dar 10.789,80 € rápido.",
                        new BlockStats(2, 1, "10.789,80 €", "10.789,80 €", 22,
                                "10.789,80 €", "10.789,80 €")),

                new MyTest("ar_PS",
                        "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                        new BlockStats(1, 1, "₪ ١٠٬٧٨٩٫٨٠", "₪ ١٠٬٧٨٩٫٨٠",
                                11, "₪ ١٠٬٧٨٩٫٨٠", "₪ ١٠٬٧٨٩٫٨٠")),


                generateBigRandomTest(key)
        };

        for (MyTest test : tests) {
            if (isTestFailed(key, test)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void checkSentencesBlock() {
        String key = "sentences";
        MyTest[] tests = {
                new MyTest("en_US",
                        "Hi. Hi, hi, hi.",
                        new BlockStats(2, 2, "Hi, hi, hi.", "Hi.", 14, "Hi.", "Hi, hi, hi.")),

                new MyTest("en_US",
                        "Hi.\nHi.\nHi.\nHi.",
                        new BlockStats(4, 1, "Hi.", "Hi.", 12, "Hi.", "Hi.")),

                new MyTest("ar_PS",
                        "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                        new BlockStats(1, 1, "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                                "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣", 37,
                                "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                                "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣")),

                new MyTest("ar_PS",
                "أعطني!\nأعطني!\nأعطني!\n",
                        new BlockStats(3, 1, "أعطني!", "أعطني!", 18, "أعطني!", "أعطني!")),

                generateBigRandomTest(key)
        };

        for (MyTest test : tests) {
            if (isTestFailed(key, test)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void checkLinesBlock() {
        String key = "lines";
        MyTest[] tests = {
                new MyTest("en_US",
                        "Hi. Hi, hi, hi.",
                        new BlockStats(1, 1, "Hi. Hi, hi, hi.", "Hi. Hi, hi, hi.", 15,
                                "Hi. Hi, hi, hi.", "Hi. Hi, hi, hi.")),
                new MyTest("en_US",
                        "Hi.\nHi.\nHi.\nHi.",
                        new BlockStats(4, 1, "Hi.", "Hi.", 12, "Hi.", "Hi.")),
                new MyTest("ar_PS",
                        "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                        new BlockStats(1, 1, "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                                "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣", 37,
                                "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣",
                                "₪ ١٠٬٧٨٩٫٨٠ ٢٥\u200F/٠٥\u200F/٢٠٢٠ أين يعطى ٣٣٣")),
                new MyTest("ar_PS",
                        "أعطني!\nأعطني!\nأعطني!\n",
                        new BlockStats(3, 1, "أعطني!", "أعطني!", 18, "أعطني!", "أعطني!")),
                generateBigRandomTest(key)
        };

        for (MyTest test : tests) {
            if (isTestFailed(key, test)) {
                Assert.fail();
            }
        }
    }

    private boolean isTestFailed(String key, MyTest test) {
        makeFile(test.text);
        TextStatistics textStatistics = new TextStatistics(test.locale, tmpTestFile);
        BlockStats myAnswers = textStatistics.getStatistic(textStatistics.objectsCount.get(ids.get(key)), key);
        return !myAnswers.isEqualTo(test.answers);
    }

    private MyTest generateBigRandomTest(String key) {

        BlockStats answers = new BlockStats();
        int count = random.nextInt(999);
        answers.count = count;
        answers.unique = count;
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            String curS = "Начало ";
            if (count > 0) {
                curS += i;
                if (key.equals("currencies")){
                    curS+=" ₽";
                }
                count -= 1;
                answers.sumLength += Integer.toString(i).length();
                curS += " ";
            }
            curS += "конец.\n";
            textBuilder.append(curS);
        }
        if (answers.count > 0) {
            switch (key) {
                case "numbers":
                    return answersNumbersToRandomTest(textBuilder.toString(),answers);
                case "words":
                    return answersWordsToRandomTest(textBuilder.toString(), answers);
                case "lines":
                    return answersLinesSentencesToRandomTest(textBuilder.toString(), answers);
                case "currencies":
                    return answersCurrenciesToRandomTest(textBuilder.toString(), answers);
                case "sentences":
                    return answersLinesSentencesToRandomTest(textBuilder.toString(), answers);
                default:
                    return new MyTest("ru_RU", textBuilder.toString(), answers);
            }
        }
        return new MyTest("ru_RU", textBuilder.toString(), answers);

    }

    private MyTest answersNumbersToRandomTest(String text, BlockStats answers) {
        answers.minObject = "0";
        answers.maxObject = Integer.toString(answers.count - 1);
        answers.objectMinLength = "0";
        if (answers.count < 10) {
            answers.objectMaxLength = "0";
        } else if (answers.count < 100) {
            answers.objectMaxLength = "10";
        } else {
            answers.objectMaxLength = "100";
        }
        return new MyTest("ru_RU", text, answers);

    }

    private MyTest answersLinesSentencesToRandomTest(String text, BlockStats answers) {
        answers.sumLength += 1000 * 13 + answers.count;
        answers.unique = answers.count + 1;
        answers.minObject = "Начало 0 конец.";
        answers.objectMinLength = "Начало конец.";
        answers.maxObject = "Начало конец.";
        if (answers.count < 10) {
            answers.objectMaxLength = "Начало 0 конец.";
        } else if (answers.count < 100) {
            answers.objectMaxLength = "Начало 10 конец.";
        } else {
            answers.objectMaxLength = "Начало 100 конец.";
        }
        answers.count = 1000;
        return new MyTest("ru_RU", text, answers);
    }

    private MyTest answersWordsToRandomTest(String text, BlockStats answers) {
        answers.count += 2000;
        answers.unique += 2;
        answers.sumLength += 1000 * 11;
        answers.minObject = "0";
        answers.maxObject = "Начало";
        answers.objectMinLength = "0";
        answers.objectMaxLength = "Начало";
        return new MyTest("ru_RU", text, answers);
    }

    private MyTest answersCurrenciesToRandomTest (String text, BlockStats answers) {
        answers.minObject = "0 ₽";
        answers.maxObject = (answers.count - 1)+" ₽";
        answers.objectMinLength = "0 ₽";
        answers.sumLength+=answers.count*2;
        if (answers.count < 10) {
            answers.objectMaxLength = "0 ₽";
        } else if (answers.count < 100) {
            answers.objectMaxLength = "10 ₽";
        } else {
            answers.objectMaxLength = "100 ₽";
        }
        return new MyTest("ru_RU", text, answers);

    }


    private void makeFile(String text) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(tmpTestFile))) {
            writer.write(text);
        } catch (IOException e) {
            System.err.println("Writing tmp file error:" + e);
        }
    }
}

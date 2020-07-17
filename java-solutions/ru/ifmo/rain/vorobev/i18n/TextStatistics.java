package ru.ifmo.rain.vorobev.i18n;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;

public class TextStatistics {

    final String nullS = "-";
    final String firstTag = "<html><head><meta charset=\"UTF-8\"/><title>Stats</title></head><body>";
    final String lastTag = "</body></html>";
    final String[] stats = {"sentences", "lines", "words", "numbers", "currencies", "dates"};
    ArrayList<Map<String, ObjectCount>> objectsCount;
    Locale inputLocale;
    String inputFile;

    private static class ObjectCount {
        protected String object = null;
        protected int count = 0;
    }


    public TextStatistics(String inputStringLocale, String inFile) {
        inputLocale = getLocale(inputStringLocale);
        inputFile = inFile;
        Map<String, ObjectCount> linesCount = new TreeMap<>();
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile))) {
            String curString;
            while ((curString = reader.readLine()) != null) {
                stringBuilder.append(curString).append(" ");
                addToMap(linesCount, curString, inputLocale);
            }
        } catch (IOException e) {
            System.err.println(("Reading error: " + e.getMessage()));
        }
        String s = stringBuilder.toString();
        BreakIterator breakIterator = BreakIterator.getSentenceInstance(inputLocale);
        Map<String, ObjectCount> sentencesCount = createResultMap(breakIterator, s, inputLocale);
        breakIterator = BreakIterator.getWordInstance(inputLocale);
        Map<String, ObjectCount> wordsCount = createResultMap(breakIterator, s, inputLocale);
        ArrayList<Map<String, ObjectCount>> result = parseComplex(s, inputLocale);
        Map<String, ObjectCount> numbersCount = result.get(0);
        Map<String, ObjectCount> currenciesCount = result.get(1);
        Map<String, ObjectCount> datesCount = result.get(2);
        objectsCount = new ArrayList<>(Arrays.asList(sentencesCount, linesCount, wordsCount, numbersCount, currenciesCount, datesCount));

    }

    public void createStatistics(String outputStringLocale, String outputFile) {
        Locale outputLocale = getLocale(outputStringLocale);
        if (outputLocale == null) {
            return;
        }
        Locale.setDefault(outputLocale);
        ResourceBundle bundle = ResourceBundle.getBundle("ru.ifmo.rain.vorobev.i18n.stats");
        String htmlCode = "";
        htmlCode = writeHTML(htmlCode, firstTag, false, 0);
        htmlCode = writeHTML(htmlCode, bundle.getString("file.title") + inputFile, false, 3);
        htmlCode = writeHTML(htmlCode, bundle.getString("first.title"), false, 4);
        for (int i = 0; i < 6; i++) {
            htmlCode = getFirstStatistic(objectsCount.get(i), stats[i], bundle, htmlCode);
        }
        htmlCode = writeHTML(htmlCode, "", true, 0);
        for (int i = 0; i < 6; i++) {
            BlockStats curBlock = getStatistic(objectsCount.get(i), stats[i]);
            htmlCode = writeStatisticsToHtml(bundle, stats[i], htmlCode, curBlock);
        }
        htmlCode = writeHTML(htmlCode, lastTag, false, 0);

        System.out.println(htmlCode);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile))) {
            writer.write(htmlCode);
        } catch (IOException e) {
            System.err.println("Writing to html error:" + e);
        }
    }

    private Map<String, ObjectCount> createResultMap(BreakIterator breakIterator, String text, Locale locale) {
        Map<String, ObjectCount> objectsCount = new TreeMap<>();
        breakIterator.setText(text);
        int curIndex = breakIterator.first();
        int prevIndex = 0;
        while (curIndex != BreakIterator.DONE) {
            String object = text.substring(prevIndex, curIndex);
            if (isCorrect(object)) {
                addToMap(objectsCount, object, locale);
            }
            prevIndex = curIndex;
            curIndex = breakIterator.next();
        }

        return objectsCount;
    }

    private void addToMap(Map<String, ObjectCount> objectsCount, String object, Locale locale) {
        ObjectCount curCount = objectsCount.get(object.toLowerCase(locale));
        if (curCount == null) {
            curCount = new ObjectCount();
            curCount.object = object.trim();
        }
        curCount.count++;
        objectsCount.put(object.toLowerCase(locale), curCount);
    }

    private String writeHTML(String htmlCode, String s, boolean isLine, int headerLvl) {
        if (headerLvl > 0) {
            s = "<h" + headerLvl + ">" + s + "</h" + headerLvl + ">";
        } else if (isLine) {
            s = "<p>" + s + "</p>";
        }
        return htmlCode + s + "\n";
    }

    public BlockStats getStatistic(Map<String, ObjectCount> objectCount, String keyEnd) {
        BlockStats block = new BlockStats();
        block.unique = objectCount.size();
        boolean isFirst = true;
        boolean isNumber = keyEnd.equals("numbers");
        boolean isCurrency = keyEnd.equals("currencies");
        boolean isDate = keyEnd.equals("dates");

        NumberFormat numberFormat = NumberFormat.getNumberInstance(inputLocale);
        if (isCurrency) {
            numberFormat = NumberFormat.getCurrencyInstance(inputLocale);
        }
        Date maxDate = null;
        Date minDate = null;
        DateFormat[] dateFormats = new DateFormat[4];
        dateFormats[0] = DateFormat.getDateInstance(DateFormat.FULL, inputLocale);
        dateFormats[1] = DateFormat.getDateInstance(DateFormat.LONG, inputLocale);
        dateFormats[2] = DateFormat.getDateInstance(DateFormat.MEDIUM, inputLocale);
        dateFormats[3] = DateFormat.getDateInstance(DateFormat.SHORT, inputLocale);

        for (String key : objectCount.keySet()) {
            String obj = objectCount.get(key).object;
            int curAmount = objectCount.get(key).count;
            int curLength = obj.length();
            if (isFirst) {
                block.minObject = obj;
                block.maxObject = obj;
                block.objectMinLength = obj;
                block.objectMaxLength = obj;
            }
            if (isNumber || isCurrency) {
                try {
                    block.minObject = numberFormat.parse(obj).doubleValue() < numberFormat.parse(block.minObject).doubleValue() ? obj : block.minObject;
                    block.maxObject = numberFormat.parse(obj).doubleValue() > numberFormat.parse(block.maxObject).doubleValue() ? obj : block.maxObject;
                } catch (ParseException ignored) {
                }
            } else if (isDate) {
                Date curDate;
                int i = 0;
                while ((curDate = dateFormats[i].parse(obj, new ParsePosition(0))) == null) {
                    i += 1;
                }
                if (isFirst) {
                    minDate = curDate;
                    maxDate = curDate;
                } else {
                    if (curDate.before(minDate)) {
                        block.minObject = obj;
                        minDate = curDate;
                    } else if (curDate.after(maxDate)) {
                        block.maxObject = obj;
                        maxDate = curDate;
                    }
                }
            } else {
                block.maxObject = obj;
            }
            isFirst = false;
            block.count += curAmount;
            block.sumLength += curAmount * curLength;
            block.objectMinLength = curLength < block.objectMinLength.length() ? obj : block.objectMinLength;
            block.objectMaxLength = curLength > block.objectMaxLength.length() ? obj : block.objectMaxLength;

        }
        return block;

    }

    private String writeStatisticsToHtml(ResourceBundle bundle, String keyEnd, String htmlCode, BlockStats block) {
        String unique = bundle.getString("unique");
        if (Locale.getDefault().equals(new Locale("ru", "RU"))) {
            if (block.count % 10 == 1 && block.count != 11) {
                if (keyEnd.equals("words") || keyEnd.equals("sentences") || keyEnd.equals("numbers")) {
                    unique += "ое";
                } else {
                    unique += "ая";
                }
            } else {
                unique += "ых";
            }
        }
        htmlCode = writeHTML(htmlCode, bundle.getString("part.title." + keyEnd), true, 4);
        htmlCode = writeHTML(htmlCode, bundle.getString("part.count." + keyEnd) + block.count + (block.count > 0 ?
                (" (" + block.unique + " " + unique + ")") : ""), true, 0);
        htmlCode = writeHTML(htmlCode, bundle.getString("part.first." + keyEnd) + block.minObject, true, 0);
        htmlCode = writeHTML(htmlCode, bundle.getString("part.last." + keyEnd) + block.maxObject, true, 0);
        htmlCode = writeHTML(htmlCode, bundle.getString("part.min." + keyEnd) + (block.objectMinLength.equals(nullS) ?
                nullS : block.objectMinLength.length()) + (block.objectMinLength.equals(nullS) ?
                "" : (" (" + block.objectMinLength + ")")), true, 0);
        htmlCode = writeHTML(htmlCode, bundle.getString("part.max." + keyEnd) + (block.objectMaxLength.equals(nullS) ?
                nullS : block.objectMaxLength.length()) + (block.objectMaxLength.equals(nullS) ?
                "" : (" (" + block.objectMaxLength + ")")), true, 0);
        htmlCode = writeHTML(htmlCode, bundle.getString("part.mid." + keyEnd) + (block.count > 0 ?
                ((double) block.sumLength / block.count) : nullS), true, 0);
        htmlCode = writeHTML(htmlCode, "", true, 0);
        return htmlCode;
    }


    private String getFirstStatistic(Map<String, ObjectCount> objectsCount, String keyEnd, ResourceBundle bundle, String htmlCode) {
        int count = 0;
        for (String key : objectsCount.keySet()) {
            count += objectsCount.get(key).count;
        }
        htmlCode = writeHTML(htmlCode, bundle.getString("part.count." + keyEnd) + count, true, 0);
        return htmlCode;
    }


    private ArrayList<Map<String, ObjectCount>> parseComplex(String s, Locale locale) {
        Map<String, ObjectCount> numberCount = new TreeMap<>();
        Map<String, ObjectCount> currenciesCount = new TreeMap<>();
        Map<String, ObjectCount> datesCount = new TreeMap<>();
        Format[] allFormats = new Format[6];
        allFormats[0] = DateFormat.getDateInstance(DateFormat.FULL, locale);
        allFormats[1] = DateFormat.getDateInstance(DateFormat.LONG, locale);
        allFormats[2] = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        allFormats[3] = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        allFormats[4] = NumberFormat.getCurrencyInstance(locale);
        allFormats[5] = NumberFormat.getNumberInstance(locale);
        for (ParsePosition pos = new ParsePosition(0); pos.getIndex() < s.length(); ) {
            int index = 0;
            Date curDate = null;
            Number curNum = null;
            int lastPos = pos.getIndex();
            while (index < 6 && (index < 4 && ((curDate = ((DateFormat) allFormats[index]).parse(s, pos)) == null)
                    || (index >= 4 && (curNum = ((NumberFormat) allFormats[index]).parse(s, pos)) == null))) {
                index += 1;
            }
            if (curDate != null) {
                addToMap(datesCount, ((DateFormat) allFormats[index]).format(curDate), locale);
            } else if (curNum != null) {
                if (index == 4) {
                    addToMap(currenciesCount, s.substring(lastPos, pos.getIndex()), locale);
                } else {
                    addToMap(numberCount, allFormats[index].format(curNum), locale);
                }
            } else {
                pos.setIndex(pos.getIndex() + 1);
            }
        }
        return new ArrayList<>(List.of(numberCount, currenciesCount, datesCount));
    }


    private boolean isCorrect(String object) {
        object = object.trim();
        return !((object.length() == 1 && !Character.isLetterOrDigit(object.charAt(0))) || object.isEmpty());
    }

    private Locale getLocale(String s) {
        String[] splitted = s.split("_");
        if (splitted.length == 1) {
            return new Locale(splitted[0]);
        } else if (splitted.length == 2) {
            return new Locale(splitted[0], splitted[1]);
        } else if (splitted.length == 3) {
            return new Locale(splitted[0], splitted[1], splitted[2]);
        }
        System.err.println("Wrong locale");
        return null;
    }

}

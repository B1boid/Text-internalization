package ru.ifmo.rain.vorobev.i18n;

public class Main {
    public static void main(String[] args) {
        if (args == null || args.length != 4) {
            System.err.println("Wrong number of arguments");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Wrong number of arguments");
                return;
            }
        }

        TextStatistics ts = new TextStatistics(args[0],args[2]);
        ts.createStatistics(args[1],args[3]);
    }

}

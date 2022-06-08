package org.jboss.perf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Util {

    public static  String prettyPrintExperiment(String input){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        String output;
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            prettyPrintExperiment(input, ps);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            output = baos.toString(utf8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return output;
    }

    public static void prettyPrintExperiment(String input, PrintStream ps) {

        StringBuilder outputBuilder = new StringBuilder();
        String pad = "";

        for (int i = 0; i < input.length(); i++) {
            char curChar = input.charAt(i);
            switch (curChar) {
                case '{', '[':
                    pad = pad.concat("  ");
                    outputBuilder.append(" ").append(curChar);

                    ps.println(outputBuilder);
                    outputBuilder = new StringBuilder();
                    outputBuilder.append(pad);

                    break;
                case '}', ']':
                    pad = pad.length() > 0 ? pad.substring(0, pad.length() - 2) : "";
                    ps.println(outputBuilder);
                    outputBuilder = new StringBuilder();
                    outputBuilder.append(pad).append(curChar);
                    ps.println(outputBuilder);
                    outputBuilder = new StringBuilder();
                    break;
                case ',':
                    ps.println(outputBuilder.append(curChar));
                    outputBuilder = new StringBuilder();
                    outputBuilder.append(pad);
                    break;
                default:
                    outputBuilder.append(curChar);
            }
        }
    }
}

package com.slimenano.sdk.console;

import org.fusesource.jansi.Ansi;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import static org.fusesource.jansi.Ansi.ansi;

public class AlertHighlighter {

    private static final HashMap<String, Dealer> innerHighlightMap = new HashMap<>();

    static {
        innerHighlightMap.put("高危", Ansi::fgBrightRed);
        innerHighlightMap.put("隐私", Ansi::fgYellow);
        innerHighlightMap.put("正常", Ansi::fgBrightBlue);
        innerHighlightMap.put("临时", Ansi::fgBrightGreen);
    }

    public static String highlight(String buffer) {

        Ansi ansi = ansi();

        LinkedList<Integer> replace_index = new LinkedList<>();
        HashMap<Integer, String> keyMap = new HashMap<>();
        for (String key : innerHighlightMap.keySet()) {

            int a = buffer.indexOf(key);
            while (a != -1) {
                replace_index.add(a);
                keyMap.put(a, key);
                a = buffer.indexOf(key, a + 1);
            }

        }

        Collections.sort(replace_index);
        int last = 0;
        for (Integer index : replace_index) {
            // 处理关键词前的词
            if (index != 0) {
                ansi.reset();
                ansi.a(buffer.substring(last, index));
                last = index;
            }
            // 处理关键词
            String keyWord = keyMap.get(index);
            innerHighlightMap.get(keyWord).deal(ansi).a(keyWord);
            last += keyWord.length();
        }
        ansi.reset().a(buffer.substring(last));

        return ansi.reset().toString();
    }

}

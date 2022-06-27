package com.example.myapplication.Utils;

public class Speecher {
    private static String[] oneToSixTeen = {
            "", "one", "two", "three", "four", "five", "six", "seven",
            "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen",
            "fifteen"

    };

    public static String mapNumToWord(int num){
        return oneToSixTeen[num];
    }


}

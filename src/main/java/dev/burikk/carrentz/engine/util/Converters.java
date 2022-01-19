package dev.burikk.carrentz.engine.util;

import java.util.TreeMap;

/**
 * @author Muhammad Irfan
 * @since 26/07/2018 14.14
 */
public class Converters {
    private static final transient TreeMap<Integer, String> ROMAN_NUMBER_MAP = new TreeMap<>();
    private static final String[] SPELL_AMOUNT = {" ", "Satu", "Dua", "Tiga", "Empat", "Lima", "Enam", "Tujuh", "Delapan", "Sembilan", "Sepuluh", "Sebelas"};

    static {
        ROMAN_NUMBER_MAP.put(1000, "M");
        ROMAN_NUMBER_MAP.put(900, "CM");
        ROMAN_NUMBER_MAP.put(500, "D");
        ROMAN_NUMBER_MAP.put(400, "CD");
        ROMAN_NUMBER_MAP.put(100, "C");
        ROMAN_NUMBER_MAP.put(90, "XC");
        ROMAN_NUMBER_MAP.put(50, "L");
        ROMAN_NUMBER_MAP.put(40, "XL");
        ROMAN_NUMBER_MAP.put(10, "X");
        ROMAN_NUMBER_MAP.put(9, "IX");
        ROMAN_NUMBER_MAP.put(5, "V");
        ROMAN_NUMBER_MAP.put(4, "IV");
        ROMAN_NUMBER_MAP.put(1, "I");
    }

    public static String toRoman(int mNumber) {
        int mFloorValue = ROMAN_NUMBER_MAP.floorKey(mNumber);

        if (mNumber == mFloorValue) {
            return ROMAN_NUMBER_MAP.get(mNumber);
        }

        return ROMAN_NUMBER_MAP.get(mFloorValue) + toRoman(mNumber - mFloorValue);
    }

    public static String spell(long mNumber){
        if (mNumber < 12) {
            return "" + SPELL_AMOUNT[(int)mNumber];
        } else if (mNumber < 20) {
            return spell(mNumber - 10) + " Belas ";
        } else if (mNumber < 100) {
            return (spell(mNumber / 10) + " Puluh ") + spell(mNumber % 10);
        } else if (mNumber < 200) {
            return "Seratus " + spell(mNumber - 100);
        } else if (mNumber < 1000) {
            return (spell(mNumber / 100) + " Ratus ") + spell(mNumber % 100);
        } else if (mNumber < 2000) {
            return "Seribu " + spell(mNumber - 1000);
        } else if (mNumber < 1000000) {
            return (spell(mNumber / 1000) + " Ribu ") + spell(mNumber % 1000);
        } else if (mNumber < 1000000000) {
            return (spell(mNumber / 1000000) + " Juta ") + spell(mNumber % 1000000);
        } else if (mNumber < 1000000000000L) {
            return (spell(mNumber / 1000000000) + " Milyar ") + spell(mNumber % 1000000000);
        } else if (mNumber < 1000000000000000L) {
            return (spell(mNumber / 1000000000000L) + " Triliun ") + spell(mNumber % 1000000000000L);
        }

        return null;
    }
}

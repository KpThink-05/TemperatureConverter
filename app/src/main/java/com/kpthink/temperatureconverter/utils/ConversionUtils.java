package com.kpthink.temperatureconverter.utils;

public class ConversionUtils {

    public static double cToF(double c) {
        return c * 9.0 / 5.0 + 32.0;
    }

    public static double fToC(double f) {
        return (f - 32.0) * 5.0 / 9.0;
    }

    public static double cToK(double c) {
        return c + 273.15;
    }

    public static double kToC(double k) {
        return k - 273.15;
    }

    public static double convert(double value, String from, String to) {
        if (from.equals(to)) return value;
        double c;
        switch (from) {
            case "Celsius": c = value; break;
            case "Fahrenheit": c = fToC(value); break;
            case "Kelvin": c = kToC(value); break;
            default: c = value;
        }
        switch (to) {
            case "Celsius": return c;
            case "Fahrenheit": return cToF(c);
            case "Kelvin": return cToK(c);
            default: return c;
        }
    }

    public static String formula(String from, String to) {
        if (from.equals(to)) return from + " → " + to;
        if (from.equals("Celsius") && to.equals("Fahrenheit")) return "F = C × 9/5 + 32";
        if (from.equals("Fahrenheit") && to.equals("Celsius")) return "C = (F − 32) × 5/9";
        if (from.equals("Celsius") && to.equals("Kelvin")) return "K = C + 273.15";
        if (from.equals("Kelvin") && to.equals("Celsius")) return "C = K − 273.15";
        if (from.equals("Fahrenheit") && to.equals("Kelvin")) return "K = (F − 32) × 5/9 + 273.15";
        if (from.equals("Kelvin") && to.equals("Fahrenheit")) return "F = (K − 273.15) × 9/5 + 32";
        return "";
    }
}

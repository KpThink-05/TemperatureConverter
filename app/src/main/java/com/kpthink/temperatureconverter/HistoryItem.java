package com.kpthink.temperatureconverter;

public class HistoryItem {
    public String conversionText;
    public long timestamp;

    public HistoryItem(String conversionText, long timestamp) {
        this.conversionText = conversionText;
        this.timestamp = timestamp;
    }
}

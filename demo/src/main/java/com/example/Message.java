package com.example;

public class Message {
    public String sender;
    public String receiver;
    public String text;

    public Message(String sender, String receiver, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    public String toXML() {
        return "<message>" +
               "<sender>" + sender + "</sender>" +
               "<receiver>" + receiver + "</receiver>" +
               "<text>" + text + "</text>" +
               "</message>";
    }

    public static Message fromXML(String xml) {
        String sender = extract(xml, "sender");
        String receiver = extract(xml, "receiver");
        String text = extract(xml, "text");
        return new Message(sender, receiver, text);
    }

    private static String extract(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open) + open.length();
        int end = xml.indexOf(close);
        if (start < open.length() || end == -1) return "";
        return xml.substring(start, end);
    }
}

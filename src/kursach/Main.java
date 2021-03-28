package kursach;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        Writer writer = new Writer();
        writer.writeTable();
        Port port = new Port();
        port.calculateOptimalNumberOfCranes(System.getProperty("user.dir") + "/table.json");
        writer.writeResult(port.serviceInfoArrayList, port.statistics);
    }
}

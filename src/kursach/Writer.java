package kursach;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Scanner;

public class Writer
{
    public static void writeTable()
    {
        ScheduleGenerator generator = new ScheduleGenerator();
        generator.generateShips();
        Scanner in = new Scanner(System.in);
        while (true)
        {
            System.out.println("Хотите ли вы добавить еще один корабль в расписание?(д/н)");
            String consent = in.nextLine();
            while(!consent.equals("д") && !consent.equals("н"))
            {
                System.out.println("Хотите ли вы добавить еще один корабль в расписание?(д/н)");
                consent = in.nextLine();
            }
            if (consent.equals("д"))
            {
                try
                {
                    generator.addShipFromConsole();
                } catch (IllegalArgumentException exception){
                    System.out.println(exception.getMessage());
                }
            }
            else
            {
                break;
            }
        }
        try {
            JsonWriter  writer = new JsonWriter(new FileWriter(System.getProperty("user.dir") + "/table.json"));
            Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss").create();
            Type SHIP_TYPE = new TypeToken<ArrayList<Ship>>() {}.getType();
            gson.toJson(generator.ships, SHIP_TYPE, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeResult(ArrayList<ServiceInfo> serviceInfo, Statistics statistics)
    {
        try {
            JsonWriter  writer = new JsonWriter(new FileWriter(System.getProperty("user.dir") + "/statistics.json"));
            writer.beginObject();
            writer.name("statistics");
            writer.beginObject();
            writer.name("unloaded ships count").value(statistics.unloadedShipsCount);
            writer.name("average queue length").value(statistics.averageQueueLength);
            writer.name("average waiting time").value(String.valueOf(statistics.averageWaitingTime));
            writer.name("average delay").value(String.valueOf(statistics.averageDelay));
            writer.name("max delay").value(String.valueOf(statistics.maxDelay));
            writer.name("final fine").value(statistics.finalFine);
            writer.name("liquid crane count").value(statistics.liquidCraneCount);
            writer.name("loose crane count").value(statistics.looseCraneCount);
            writer.name("container crane count").value(statistics.containerCraneCount);
            writer.endObject();
            writer.name("Ships");
            writer.beginArray();
            for (int i = 0; i < serviceInfo.size(); i++)
            {
                writer.beginObject();
                writer.name("name").value(serviceInfo.get(i).name);
                writer.name("coming time").value(String.valueOf(serviceInfo.get(i).comingTime));
                writer.name("waiting time").value(String.valueOf(serviceInfo.get(i).waitingTime));
                writer.name("beginning unloading time").value(String.valueOf(serviceInfo.get(i).beginningUnloadingTime));
                writer.name("unloading time").value(String.valueOf(serviceInfo.get(i).unloadingTime));
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

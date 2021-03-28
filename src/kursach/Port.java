package kursach;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class Port
{
    public ArrayList<ServiceInfo> serviceInfoArrayList;
    public Statistics statistics;
    private int liquidCraneCount;
    private int looseCraneCount;
    private int containerCraneCount;
    public static double liquidCraneSpeed = 15; // ton per minute
    public static double looseCraneSpeed = 20; // ton per minute
    public static double containerCraneSpeed = 8; // container per minute
    Port()
    {
        serviceInfoArrayList = new ArrayList<ServiceInfo>();
        statistics = new Statistics();
        liquidCraneCount = 1;
        looseCraneCount = 1;
        containerCraneCount = 1;
    };
    public void calculateOptimalNumberOfCranes(String pathToJson) throws IOException
    {
        JsonReader reader = new JsonReader(new FileReader(pathToJson));
        Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss").create();
        Type SHIP_TYPE = new TypeToken<ArrayList<Ship>>() {}.getType();
        ArrayList<Ship> ships = gson.fromJson(reader, SHIP_TYPE);
        reader.close();
        double averageQueueLength = 0; //задаем переменные для подсчета статистики
        long averageWaitingTime = 0;
        long averageDelay = 0;
        long maxDelay = 0;
        long fine = 0;
        int craneCost = 30000;
        ArrayList<Ship> liquidShips = new ArrayList<Ship>(); //очереди всех типов кораблей
        ArrayList<Ship> looseShips = new ArrayList<Ship>();
        ArrayList<Ship> containerShips = new ArrayList<Ship>();
        for (int i = 0; i < ships.size(); i++) //задаем отклонение в дате прибытия
        {
            ships.get(i).setDate(DateUtil.addDays(ships.get(i).getDate(), ScheduleGenerator.rnd(-7, 7)).getTime());
        }
        ships.sort(new Comparator<Ship>() //сортируем корабли по дате прибытия
        {
            @Override
            public int compare(Ship lhs, Ship rhs)
            {
                return Long.compare(lhs.getDate().getTime(), rhs.getDate().getTime());
            }
        });
        for (int i = 0; i < ships.size(); i++) //заполняем наши очереди кораблей, при этом берем только те, которые попали в период симуляции(март)
        {
            if (ships.get(i).getDate().getMonth() == 2)
            {
                switch (ships.get(i).getCargoType())
                {
                    case LOOSE:
                        looseShips.add(ships.get(i));
                        break;
                    case LIQUID:
                        liquidShips.add(ships.get(i));
                        break;
                    case CONTAINER:
                        containerShips.add(ships.get(i));
                        break;
                }
            }
        }
        statistics.unloadedShipsCount = looseShips.size() + liquidShips.size() + containerShips.size();
        int[] delayTimeLoose = new int[looseShips.size()];
        Arrays.fill(delayTimeLoose, 0);
        for (int i = 0; i < looseShips.size(); i++)
        {
            if (ScheduleGenerator.rnd(0, 2) < 1)
            {
                delayTimeLoose[i] = ScheduleGenerator.rnd(0, 1440);
            }
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.name = looseShips.get(i).getName();
            serviceInfoArrayList.add(serviceInfo);
        }
        try
        {
            fine = getFine(looseShips, looseCraneCount, looseCraneSpeed, 0, delayTimeLoose);
            while (fine > craneCost + getFine(looseShips, looseCraneCount + 1, looseCraneSpeed, 0, delayTimeLoose))
            {
                looseCraneCount += 1;
                fine = getFine(looseShips, looseCraneCount, looseCraneSpeed, 0, delayTimeLoose);
            }
            getFine(looseShips, looseCraneCount, looseCraneSpeed, 0, delayTimeLoose);
        }       catch(InterruptedException e) {
            System.out.println ("Что-то пошло не так!");
        }
        averageQueueLength += statistics.averageQueueLength; //после получения оптимального количества кранов для данного расписания - заполняем статистику
        averageWaitingTime += statistics.averageWaitingTime.getTime();
        averageDelay += statistics.averageDelay.getTime();
        maxDelay = statistics.maxDelay.getTime();
        statistics.finalFine += fine;
        System.out.println("LOOSE:");
        System.out.print("Fine = ");
        System.out.println(fine);
        System.out.print("Loose crane count = ");
        System.out.println(looseCraneCount);
        System.out.println("\n");
        int[] delayTimeLiquid = new int[liquidShips.size()];
        Arrays.fill(delayTimeLiquid, 0);
        for (int i = 0; i < liquidShips.size(); i++)
        {
            if (ScheduleGenerator.rnd(0, 2) < 1)
            {
                delayTimeLiquid[i] = ScheduleGenerator.rnd(0, 1440);
            }
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.name = liquidShips.get(i).getName();
            serviceInfoArrayList.add(serviceInfo);
        }
        try
        {
            fine = getFine(liquidShips, liquidCraneCount, liquidCraneSpeed, looseShips.size(), delayTimeLiquid);
            while (fine > craneCost + getFine(liquidShips, liquidCraneCount + 1, liquidCraneSpeed, looseShips.size(), delayTimeLiquid))
            {
                liquidCraneCount += 1;
                fine = getFine(liquidShips, liquidCraneCount, liquidCraneSpeed, looseShips.size(), delayTimeLiquid);
            }
            getFine(liquidShips, liquidCraneCount, liquidCraneSpeed, looseShips.size(), delayTimeLiquid);
        }       catch(InterruptedException e) {
            System.out.println ("Что-то пошло не так!");
        }
        averageQueueLength += statistics.averageQueueLength;
        averageWaitingTime += statistics.averageWaitingTime.getTime();
        averageDelay += statistics.averageDelay.getTime();
        if (statistics.maxDelay.getTime() > (long)maxDelay * 60 * 1000)
        {
            maxDelay = statistics.maxDelay.getTime();
        }
        statistics.finalFine += fine;
        System.out.println("LIQUID:");
        System.out.print("Fine = ");
        System.out.println(fine);
        System.out.print("Liquid crane count = ");
        System.out.println(liquidCraneCount);
        System.out.println("\n");
        int[] delayTimeContainer = new int[containerShips.size()];
        Arrays.fill(delayTimeContainer, 0);
        for (int i = 0; i < containerShips.size(); i++)
        {
            if (ScheduleGenerator.rnd(0, 2) < 1)
            {
                delayTimeContainer[i] = ScheduleGenerator.rnd(0, 1440);
            }
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.name = containerShips.get(i).getName();
            serviceInfoArrayList.add(serviceInfo);
        }
        try
        {
            fine = getFine(containerShips, containerCraneCount, containerCraneSpeed, liquidShips.size() + looseShips.size(), delayTimeContainer);
            while (fine > craneCost + getFine(containerShips, containerCraneCount + 1, containerCraneSpeed, liquidShips.size() + looseShips.size(), delayTimeContainer))
            {
                containerCraneCount += 1;
                fine = getFine(containerShips, containerCraneCount, containerCraneSpeed, liquidShips.size() + looseShips.size(), delayTimeContainer);
            }
            getFine(containerShips, containerCraneCount, containerCraneSpeed, liquidShips.size() + looseShips.size(), delayTimeContainer);
        }       catch(InterruptedException e) {
            System.out.println ("Что-то пошло не так!");
        }
        averageQueueLength += statistics.averageQueueLength;
        averageWaitingTime += statistics.averageWaitingTime.getTime();
        averageDelay += statistics.averageDelay.getTime();
        if (statistics.maxDelay.getTime() > (long)maxDelay * 60 * 1000)
        {
            maxDelay = statistics.maxDelay.getTime();
        }
        statistics.finalFine += fine;
        System.out.println("CONTAINER:");
        System.out.print("Fine = ");
        System.out.println(fine);
        System.out.print("Container crane count = ");
        System.out.println(containerCraneCount);
        System.out.println("\n");


        if (statistics.unloadedShipsCount != 0)
        {
            statistics.averageQueueLength = averageQueueLength / statistics.unloadedShipsCount;
            statistics.averageWaitingTime.setTime(averageWaitingTime / statistics.unloadedShipsCount);
            statistics.averageDelay.setTime(averageDelay / statistics.unloadedShipsCount);
        }
        statistics.maxDelay.setTime(maxDelay);
        statistics.liquidCraneCount = liquidCraneCount;
        statistics.looseCraneCount = looseCraneCount;
        statistics.containerCraneCount = containerCraneCount;
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("Информация об обслуженных кораблях: ");
        for (int i = 0; i < statistics.unloadedShipsCount; i++)
        {
            System.out.print("Имя корабля: ");
            System.out.println(serviceInfoArrayList.get(i).name);
            System.out.print("Дата начала разгрузки: ");
            System.out.println(formater.format(serviceInfoArrayList.get(i).beginningUnloadingTime));
            System.out.print("Дата пребытия: ");
            System.out.println(formater.format(serviceInfoArrayList.get(i).comingTime));
            System.out.print("Время ожидания: ");
            System.out.println(serviceInfoArrayList.get(i).waitingTime);
            System.out.print("Время разгрузки: ");
            System.out.println(serviceInfoArrayList.get(i).unloadingTime);
            System.out.println('\n');

        }
        System.out.print("Кораблей было обслужено: ");
        System.out.println(statistics.unloadedShipsCount);
        System.out.print("Средняя длина очереди: ");
        System.out.println(statistics.averageQueueLength);
        System.out.print("Среднее время ожидания в часах: ");
        System.out.println((double)(statistics.averageWaitingTime.getTime()) / 1000.0 / 60.0 / 60.0);
        System.out.print("Среднее время задержки разгрузки в минутах: ");
        System.out.println((double)(statistics.averageDelay.getTime()) / 1000.0 / 60.0);
        System.out.print("Максимальное время задержки разгрузки в минутах: ");
        System.out.println((double)(statistics.maxDelay.getTime()) / 1000.0 / 60.0);
        System.out.print("Итоговый штраф: ");
        System.out.println(statistics.finalFine);
    }


    private long getFine(ArrayList<Ship> shipsInput, int craneCount, double craneSpeed, int shift, int[] delayTime) throws InterruptedException
    {
        long fine = 0;
        statistics.averageQueueLength = 0;
        long[] statInfo = new long[3]; // averageWaitingTime, averageDelay, maxDelay
        Arrays.fill(statInfo, 0);
        if (shipsInput.size() == 0)
        {
            return 0;
        }
        int threadCount = (craneCount + 1) / 2;
        CranesThread[] cranesThread = new CranesThread[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            if ((i == threadCount - 1) && (craneCount % 2 == 1))
            {
                cranesThread[i] = new CranesThread(shipsInput.get(i).getDate().getTime(), craneSpeed, i, threadCount, 0, shift, shipsInput, statistics, statInfo, serviceInfoArrayList, delayTime);
            } else
            {
                cranesThread[i] = new CranesThread(shipsInput.get(i).getDate().getTime(), craneSpeed * 2, i, threadCount,  0, shift, shipsInput, statistics, statInfo, serviceInfoArrayList, delayTime);
            }
            serviceInfoArrayList.get(i).comingTime.setTime(shipsInput.get(i).getDate().getTime());
            serviceInfoArrayList.get(i).beginningUnloadingTime.setTime(shipsInput.get(i).getDate().getTime());
        }
        if (shipsInput.size() <= threadCount)
        {
            return fine;
        }
        for (int i = 0; i < threadCount; i++)
        {
            CranesThread.commonDate[i] = cranesThread[i].date.getTime();
        }
        for (int i = 0; i < threadCount; i++)
        {
            cranesThread[i].start();
        }
        for (int i = 0; i < threadCount; i++)
        {
            cranesThread[i].join();
        }
        fine = CranesThread.fine;
        statistics.averageWaitingTime.setTime(statInfo[0]);
        statistics.averageDelay.setTime(statInfo[1] * 60 * 1000);
        statistics.maxDelay.setTime(statInfo[2] * 60 * 1000);
        return fine;
    }
}

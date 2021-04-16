package kursach;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

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

    private void printStat()
    {
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
        System.out.println((double)(statistics.averageWaitingTime.getTime()) / Time.millisecondsInSecond / Time.secondsInMinute / Time.minutesInHour);
        System.out.print("Среднее время задержки разгрузки в минутах: ");
        System.out.println((double)(statistics.averageDelay.getTime()) / Time.millisecondsInSecond / Time.secondsInMinute);
        System.out.print("Максимальное время задержки разгрузки в минутах: ");
        System.out.println((double)(statistics.maxDelay.getTime()) / Time.millisecondsInSecond / Time.secondsInMinute);
        System.out.print("Итоговый штраф: ");
        System.out.println(statistics.finalFine);
    }

    private void printResult(Ship.cargoType type, long fine, int craneCount)
    {
        System.out.println(type);
        System.out.print("Fine = ");
        System.out.println(fine);
        System.out.print("Crane count = ");
        System.out.println(craneCount);
        System.out.println("\n");
    }

    private int getOptimalNumberInOneQueue(Ship.cargoType type, double craneSpeed, ArrayList<Ship> ships, int looseShipsCount, int liquidShipsCount)
    {
        int craneCount = 1;
        long fine = 0;
        int[] delayTime = new int[ships.size()];
        Arrays.fill(delayTime, 0);
        for (int i = 0; i < ships.size(); i++)
        {
            if (ScheduleGenerator.rnd(0, 2) < 1)
            {
                delayTime[i] = ScheduleGenerator.rnd(0, 1440);
            }
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.name = ships.get(i).getName();
            serviceInfoArrayList.add(serviceInfo);
        }
        int shift = 0;
        switch (type)
        {
            case LOOSE:
                shift = 0;
                break;
            case LIQUID:
                shift = looseShipsCount;
                break;
            case CONTAINER:
                shift = looseShipsCount + liquidShipsCount;
                break;
        }
        try
        {
            fine = getFine(ships, craneCount, craneSpeed, shift, delayTime);
            int craneCost = 30000;
            while (fine > craneCost + getFine(ships, craneCount + 1, craneSpeed, shift, delayTime))
            {
                craneCount += 1;
                fine = getFine(ships, craneCount, craneSpeed, shift, delayTime);
            }
            getFine(ships, craneCount, craneSpeed, shift, delayTime);
        }       catch(InterruptedException e) {
            System.out.println ("Что-то пошло не так!");
        }
        statistics.finalFine += fine;
        printResult(type, fine, craneCount);
        return craneCount;
    }

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
        for (int i = 0; i < ships.size(); i++) //заполняем наши очереди кораблей, при этом берем только те,
            // которые попали в период симуляции(март)
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
        looseCraneCount = getOptimalNumberInOneQueue(Ship.cargoType.LOOSE, looseCraneSpeed, looseShips, looseShips.size(), liquidShips.size());
        averageQueueLength += statistics.averageQueueLength; //после получения оптимального количества
        // кранов для данного расписания - заполняем статистику
        averageWaitingTime += statistics.averageWaitingTime.getTime();
        averageDelay += statistics.averageDelay.getTime();
        maxDelay = statistics.maxDelay.getTime();
        liquidCraneCount = getOptimalNumberInOneQueue(Ship.cargoType.LIQUID, liquidCraneSpeed, liquidShips, looseShips.size(), liquidShips.size());
        averageQueueLength += statistics.averageQueueLength;
        averageWaitingTime += statistics.averageWaitingTime.getTime();
        averageDelay += statistics.averageDelay.getTime();
        if (statistics.maxDelay.getTime() > (long)maxDelay * Time.secondsInMinute * Time.millisecondsInSecond)
        {
            maxDelay = statistics.maxDelay.getTime();
        }
        containerCraneCount = getOptimalNumberInOneQueue(Ship.cargoType.CONTAINER, containerCraneSpeed, containerShips, looseShips.size(), liquidShips.size());
        averageQueueLength += statistics.averageQueueLength;
        averageWaitingTime += statistics.averageWaitingTime.getTime();
        averageDelay += statistics.averageDelay.getTime();
        if (statistics.maxDelay.getTime() > (long)maxDelay * Time.secondsInMinute * Time.millisecondsInSecond)
        {
            maxDelay = statistics.maxDelay.getTime();
        }
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
        printStat();
    }


    private long getFine(ArrayList<Ship> shipsInputParam, int craneCount,
                         double craneSpeed, int shift, int[] delayTime) throws InterruptedException
    {
        ArrayList<Ship> shipsInput = new ArrayList<Ship>();
        for (int i = 0; i < shipsInputParam.size(); i++)
        {
            shipsInput.add(new Ship(shipsInputParam.get(i)));
        }
        long fine = 0;
        statistics.averageQueueLength = 0;
        long[] statInfo = new long[3]; // averageWaitingTime, averageDelay, maxDelay
        Arrays.fill(statInfo, 0);
        if (shipsInput.size() == 0)
        {
            return 0;
        }
        CraneThread[] cranesThread = new CraneThread[craneCount];
        Semaphore sem = new Semaphore(0);
        Semaphore sem2 = new Semaphore(0);
        for (int i = 0; i < craneCount; i++)
        {
            cranesThread[i] = new CraneThread(craneCount, i, sem, sem2, craneSpeed, shift, shipsInput,
              statistics, statInfo, serviceInfoArrayList, delayTime);
        }

        CraneThread.date = new Date(shipsInput.get(0).getDate().getTime());
        for (int i = 0; i < craneCount; i++)
        {
            cranesThread[i].start();
        }
        int minutesCounter = 0;
        int shipIndex = 0;
        while (CraneThread.countNotWorkingThreads.get() != craneCount)
        {
            if (shipIndex < shipsInput.size())
            {
                if (CraneThread.date.getTime() >= shipsInput.get(shipIndex).getDate().getTime())
                {
                    sem.release(2);
                    shipIndex++;
                }
            }
            CraneThread.date.setTime(CraneThread.date.getTime() + Time.millisecondsInSecond * Time.secondsInMinute);
            /*minutesCounter++;
            if (minutesCounter % Time.minutesInHour == 0 && minutesCounter != 0)
            {
                System.out.println(shipsInput.size());
                System.out.print("Прошло часов: ");
                System.out.println(minutesCounter / Time.minutesInHour);
                System.out.print("Индекс корабля: ");
                System.out.println(CraneThread.shipIndex);
            }*/
            sem2.acquire(CraneThread.countWorkingThreads.get());
        }
        fine = CraneThread.fine;
        for (int i = 0; i < craneCount; i++)
        {
            cranesThread[i].interrupt();
        }
        statistics.averageWaitingTime.setTime(statInfo[0]);
        statistics.averageDelay.setTime(statInfo[1] * Time.secondsInMinute * Time.millisecondsInSecond);
        statistics.maxDelay.setTime(statInfo[2] * Time.secondsInMinute * Time.millisecondsInSecond);
        return fine;
    }
}

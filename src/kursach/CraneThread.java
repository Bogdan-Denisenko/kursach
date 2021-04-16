package kursach;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class CraneThread extends Thread
{
    public static AtomicInteger countWorkingThreads;
    public static AtomicInteger countNotWorkingThreads;
    private static int threadCount;
    private static final Object firstLock = new Object();
    private static final Object secondLock = new Object();
    public static Semaphore sem;
    public static Semaphore sem2;
    private static boolean []mass;
    public static Date date;
    public volatile static long fine;
    public volatile static int shipIndex;
    private final int threadIndex;
    private boolean isFirstCrane;
    private final double speed;
    private final int shift;
    private final ArrayList<Ship> shipsInput;
    private final Statistics statistics;
    private final long[] statInfo;
    private final ArrayList<ServiceInfo> serviceInfoArrayList;
    private final int[] delayTime;

    CraneThread(int threadCount, int threadIndex, Semaphore sem, Semaphore sem2, double speed, int shift,
                ArrayList<Ship> shipsInput, Statistics statistics, long[] statInfo,
                ArrayList<ServiceInfo> serviceInfoArrayList, int[] delayTime)
    {
        CraneThread.countWorkingThreads = new AtomicInteger(0);
        CraneThread.countNotWorkingThreads = new AtomicInteger(0);
        CraneThread.threadCount = threadCount;
        CraneThread.mass = new boolean[threadCount];
        for (int i = 0; i < threadCount; i++)
        {
            mass[i] = false;
        }
        CraneThread.sem = sem;
        CraneThread.sem2 = sem2;
        CraneThread.fine = 0;
        CraneThread.shipIndex = -1;
        this.threadIndex = threadIndex;
        this.isFirstCrane = false;
        this.speed = speed;
        this.shift = shift;
        this.shipsInput = shipsInput;
        this.statistics = statistics;
        this.statInfo = statInfo;
        this.serviceInfoArrayList = serviceInfoArrayList;
        this.delayTime = delayTime;
    }

    private void getStat(int localShipIndex)
    {
        shipsInput.get(localShipIndex).setPlannedTime((int) Math.ceil(shipsInput.get(localShipIndex).getWeight() / speed));
        statInfo[1] += delayTime[localShipIndex];
        if (delayTime[localShipIndex] > statInfo[2])
        {
            statInfo[2] = delayTime[localShipIndex];
        }
        if (shipsInput.get(localShipIndex).getDate().getTime() < date.getTime())
        {
            statInfo[0] += date.getTime() - shipsInput.get(localShipIndex).getDate().getTime();
            serviceInfoArrayList.get(localShipIndex + shift).waitingTime.setTime(date.getTime()
              - shipsInput.get(localShipIndex).getDate().getTime());
            serviceInfoArrayList.get(localShipIndex + shift).beginningUnloadingTime.setTime(date.getTime());
        } else
        {
            serviceInfoArrayList.get(localShipIndex
              + shift).beginningUnloadingTime.setTime(shipsInput.get(localShipIndex).getDate().getTime());
            serviceInfoArrayList.get(localShipIndex + shift).waitingTime.setTime(0);
        }
        serviceInfoArrayList.get(localShipIndex
          + shift).comingTime.setTime(shipsInput.get(localShipIndex).getDate().getTime());
    }

    @Override
    public void run()
    {
        while (true)
        {
            if (shipIndex - shipsInput.size() == -1)
            {
                countNotWorkingThreads.incrementAndGet();
                break;
            }
            int localShipIndex;
            synchronized (firstLock)
            {
                try
                {
                    sem.acquire();
                    countWorkingThreads.incrementAndGet();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                boolean lastShipLoadingByOneCrane = false;
                for (int i = 0; i < threadCount; i++)
                {
                    if (mass[i])
                    {
                        mass[i] = false;
                        lastShipLoadingByOneCrane = true;
                    }
                }
                if (lastShipLoadingByOneCrane)
                {
                    isFirstCrane = false;
                    localShipIndex = shipIndex;
                } else
                {
                    mass[threadIndex] = true;
                    shipIndex++;
                    localShipIndex = shipIndex;
                    isFirstCrane = true;
                    getStat(localShipIndex);
                }
            }
            long unloadingTime = 0; // в минутах
            int localDelayTime = delayTime[localShipIndex];
            while (shipsInput.get(localShipIndex).getWeight() > 0)
            {
                unloadingTime += 1;
                synchronized (secondLock)
                {
                    shipsInput.get(localShipIndex).setWeight(shipsInput.get(localShipIndex).getWeight() - (int) speed);
                }
                long dateNow = date.getTime();
                sem2.release();
                while (dateNow == date.getTime())
                {
                    this.yield();
                }
            }
            while (localDelayTime > 0)
            {
                long dateNow = date.getTime();
                sem2.release();
                while (dateNow == date.getTime())
                {
                    this.yield();
                }
                localDelayTime--;
            }
            if (isFirstCrane)
            {
                int j = localShipIndex;
                while (date.getTime() > shipsInput.get(j).getDate().getTime())  // добавляем длину очереди
                {
                    j++;
                    statistics.averageQueueLength += 1;
                    if (j == shipsInput.size())
                    {
                        break;
                    }
                }
                serviceInfoArrayList.get(localShipIndex + shift).unloadingTime.setTime((long) (unloadingTime
                  + delayTime[localShipIndex]) * Time.secondsInMinute * Time.millisecondsInSecond);
                int fineHours = (int) (((date.getTime() - shipsInput.get(localShipIndex).getDate().getTime())
                  / (Time.minutesInHour * Time.secondsInMinute * Time.millisecondsInSecond)) -
                  unloadingTime / Time.minutesInHour + delayTime[localShipIndex] / Time.minutesInHour);
                if (fineHours > 0)
                {
                    fine += (long) fineHours * 100;
                }
            }
            countWorkingThreads.decrementAndGet();
            sem2.release();
            synchronized (firstLock)
            {
                if (mass[threadIndex])
                {
                    mass[threadIndex] = false;
                    try
                    {
                        sem.acquire();
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

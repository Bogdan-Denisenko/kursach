package kursach;

import java.util.*;

public class CranesThread extends Thread
{
    public Date date;
    public static long[] commonDate;
    public static long fine;
    private final double speed;
    private final int shift;
    private final ArrayList<Ship> shipsInput;
    private final Statistics statistics;
    private final long[] statInfo;
    private final ArrayList<ServiceInfo> serviceInfoArrayList;
    private final int[] delayTime;
    private final int threadNumber;
    private static int shipIndex;

    CranesThread(long date_, double speed_, int threadNumber_, int threadCount_, int shipIndex_, int shift_, ArrayList<Ship> shipsInput_, Statistics statistics_, long[] statInfo_, ArrayList<ServiceInfo> serviceInfoArrayList_, int[] delayTime_)
    {
        date = new Date(date_);
        speed = speed_;
        fine = 0;
        threadNumber = threadNumber_;
        commonDate = new long[threadCount_];
        shipIndex = shipIndex_;
        shift = shift_;
        shipsInput = shipsInput_;
        statistics = statistics_;
        statInfo = statInfo_;
        serviceInfoArrayList = serviceInfoArrayList_;
        delayTime = delayTime_;
    }
    @Override
    public void run()
    {
        while (true)
        {
            long min = commonDate[0];
            for (long num : commonDate) {
                if (num < min) {
                    min = num;
                }
            }
            if (shipIndex >= shipsInput.size())
            {
                break;
            }
            if (date.getTime() <= min)
            {
                int localShipIndex = shipIndex;
                shipIndex++;
                shipsInput.get(localShipIndex).setPlannedTime((int) Math.ceil(shipsInput.get(localShipIndex).getWeight() / speed));
                if (shipsInput.get(localShipIndex).getDate().getTime() < date.getTime())
                {
                    commonDate[threadNumber] = date.getTime() + (long) (shipsInput.get(localShipIndex).getPlannedTime() + delayTime[localShipIndex]) * 60 * 1000;
                } else
                {
                    commonDate[threadNumber] = shipsInput.get(localShipIndex).getDate().getTime() + (long) (shipsInput.get(localShipIndex).getPlannedTime() + delayTime[localShipIndex]) * 60 * 1000;
                }
                statInfo[1] += delayTime[localShipIndex];
                int j = localShipIndex;
                while (commonDate[threadNumber] > shipsInput.get(j).getDate().getTime())  // добавляем длину очереди
                {
                    j++;
                    statistics.averageQueueLength += 1;
                    if (j == shipsInput.size())
                    {
                        break;
                    }
                }
                if (delayTime[localShipIndex] > statInfo[2])
                {
                    statInfo[2] = delayTime[localShipIndex];
                }
                if (shipsInput.get(localShipIndex).getDate().getTime() < date.getTime())
                {
                    statInfo[0] += date.getTime() - shipsInput.get(localShipIndex).getDate().getTime();
                    serviceInfoArrayList.get(localShipIndex + shift).waitingTime.setTime(date.getTime() - shipsInput.get(localShipIndex).getDate().getTime());
                    serviceInfoArrayList.get(localShipIndex + shift).beginningUnloadingTime.setTime(date.getTime());
                    date.setTime(date.getTime() + (long) (shipsInput.get(localShipIndex).getPlannedTime() + delayTime[localShipIndex]) * 60 * 1000);
                } else
                {
                    serviceInfoArrayList.get(localShipIndex + shift).beginningUnloadingTime.setTime(shipsInput.get(localShipIndex).getDate().getTime());
                    serviceInfoArrayList.get(localShipIndex + shift).waitingTime.setTime(0);
                    date.setTime(shipsInput.get(localShipIndex).getDate().getTime() + (long) (shipsInput.get(localShipIndex).getPlannedTime() + delayTime[localShipIndex]) * 60 * 1000);
                }
                serviceInfoArrayList.get(localShipIndex + shift).unloadingTime.setTime((long) (shipsInput.get(localShipIndex).getPlannedTime() + delayTime[localShipIndex]) * 60 * 1000);
                serviceInfoArrayList.get(localShipIndex + shift).comingTime.setTime(shipsInput.get(localShipIndex).getDate().getTime());
                int fineHours = (int) (((date.getTime() - shipsInput.get(localShipIndex).getDate().getTime()) / (60000 * 60)) - shipsInput.get(localShipIndex).getPlannedTime() / 60 + delayTime[localShipIndex] / 60);
                if (fineHours > 0)
                {
                    fine += (long) fineHours * 100;
                }
            }
        }
    }
}

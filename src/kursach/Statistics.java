package kursach;

public class Statistics
{
    int unloadedShipsCount;
    double averageQueueLength;
    Time averageWaitingTime;
    Time averageDelay;
    Time maxDelay;
    int finalFine;
    int liquidCraneCount;
    int looseCraneCount;
    int containerCraneCount;
    Statistics()
    {
        averageWaitingTime = new Time(0);
        averageDelay = new Time(0);
        maxDelay = new Time(0);
        unloadedShipsCount = 0;
        averageQueueLength = 0;
        finalFine = 0;
        liquidCraneCount = 0;
        looseCraneCount = 0;
        containerCraneCount = 0;
    }
}

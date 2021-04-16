package kursach;

public class Statistics
{
    public int unloadedShipsCount;
    public double averageQueueLength;
    public Time averageWaitingTime;
    public Time averageDelay;
    public Time maxDelay;
    public int finalFine;
    public int liquidCraneCount;
    public int looseCraneCount;
    public int containerCraneCount;
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

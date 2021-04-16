package kursach;

public class Time
{
    public static final int millisecondsInSecond = 1000;
    public static final int secondsInMinute = 60;
    public static final int minutesInHour = 60;
    public static final int hoursInDay = 24;

    private long time;
    Time()
    {
        time = 0;
    }
    Time(long n)
    {
        time = n;
    }
    public long getTime()
    {
        return time;
    }
    public void setTime(long n)
    {
        time = n;
    }

    @Override
    public String toString()
    {
        long days = (time/(hoursInDay*minutesInHour*secondsInMinute*millisecondsInSecond));
        long hours = (time - days * (hoursInDay*minutesInHour*secondsInMinute*millisecondsInSecond))
          / (minutesInHour*secondsInMinute*millisecondsInSecond);
        long minutes = (time - days * (hoursInDay*minutesInHour*secondsInMinute*millisecondsInSecond)
          - hours*(minutesInHour*secondsInMinute*millisecondsInSecond))/(secondsInMinute*millisecondsInSecond);
        long seconds = (time - days * (hoursInDay*minutesInHour*secondsInMinute*millisecondsInSecond)
          - hours*(minutesInHour*secondsInMinute*millisecondsInSecond) - minutes*(secondsInMinute*millisecondsInSecond))
          / (millisecondsInSecond);
        return String.format("%02d:%02d:%02d",days,hours,minutes);
    }
}
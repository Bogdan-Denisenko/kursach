package kursach;

public class Time
{
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
        long days = (time/(24*60*60*1000));
        long hours = (time - days * (24*60*60*1000))/(60*60*1000);
        long minutes = (time - days * (24*60*60*1000) - hours*(60*60*1000))/(60*1000);
        long seconds = (time - days * (24*60*60*1000) - hours*(60*60*1000) - minutes*(60*1000))/(1000);
        return String.format("%02d:%02d:%02d",days,hours,minutes);
    }
}
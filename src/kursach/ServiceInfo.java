package kursach;

import java.util.Date;

public class ServiceInfo
{
    public String name;
    public Date comingTime;
    public Time waitingTime;
    public Date beginningUnloadingTime;
    public Time unloadingTime;
    ServiceInfo()
    {
        waitingTime = new Time(0);
        unloadingTime = new Time(0);
        comingTime = new Date();
        beginningUnloadingTime = new Date();
    }
}

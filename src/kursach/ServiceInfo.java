package kursach;

import java.util.Date;

public class ServiceInfo
{
    String name;
    Date comingTime;
    Time waitingTime;
    Date beginningUnloadingTime;
    Time unloadingTime;
    ServiceInfo()
    {
        waitingTime = new Time(0);
        unloadingTime = new Time(0);
        comingTime = new Date();
        beginningUnloadingTime = new Date();
    }
}

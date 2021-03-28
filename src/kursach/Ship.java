package kursach;
import java.util.Date;

public class Ship
{
    public enum cargoType
    {
        LOOSE,
        LIQUID,
        CONTAINER
    }
    private String name;
    private Date date;
    private int weight;
    private cargoType cargo;
    private int plannedTime;
    Ship(String name_, Date date_, int weight_, cargoType cargo_)
    {
        name = name_;
        date = new Date();
        date.setTime(date_.getTime());
        weight = weight_;
        cargo = cargo_;
        switch(cargo){
            case LOOSE:
                plannedTime = (int) Math.ceil(weight_ / Port.looseCraneSpeed);
                break;
            case LIQUID:
                plannedTime = (int) Math.ceil(weight_ / Port.liquidCraneSpeed);
                break;
            case CONTAINER:
                plannedTime = (int) Math.ceil(weight_ / Port.containerCraneSpeed);
                break;
        }
    }

    String getName()
    {
        return name;
    }

    Date getDate()
    {
        return date;
    }

    int getWeight()
    {
        return weight;
    }

    cargoType getCargoType()
    {
        return cargo;
    }

    int getPlannedTime()
    {
        return plannedTime;
    }

    void setDate(long time)
    {
        date.setTime(time);
    }

    void setPlannedTime(int plannedTime_) // in minutes
    {
        plannedTime = plannedTime_;
    }
}

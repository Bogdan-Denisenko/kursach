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
    private final String name;
    private final Date date;
    private int weight;
    private final cargoType cargo;
    private int plannedTime;
    Ship(String name, Date date, int weight, cargoType cargo)
    {
        this.name = name;
        this.date = new Date();
        this.date.setTime(date.getTime());
        this.weight = weight;
        this.cargo = cargo;
        switch(cargo){
            case LOOSE:
                plannedTime = (int) Math.ceil(weight / Port.looseCraneSpeed);
                break;
            case LIQUID:
                plannedTime = (int) Math.ceil(weight / Port.liquidCraneSpeed);
                break;
            case CONTAINER:
                plannedTime = (int) Math.ceil(weight / Port.containerCraneSpeed);
                break;
        }
    }
    Ship(Ship ship)
    {
        this.date = new Date();
        this.name = ship.name;
        this.date.setTime(ship.date.getTime());
        this.weight = ship.weight;
        this.cargo = ship.cargo;
        this.plannedTime = ship.plannedTime;
    }
    public String getName()
    {
        return name;
    }

    public Date getDate()
    {
        return date;
    }

    public int getWeight()
    {
        return weight;
    }

    public cargoType getCargoType()
    {
        return cargo;
    }

    public int getPlannedTime()
    {
        return plannedTime;
    }

    public void setDate(long time)
    {
        date.setTime(time);
    }

    public void setPlannedTime(int plannedTime) // in minutes
    {
        this.plannedTime = plannedTime;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }
}

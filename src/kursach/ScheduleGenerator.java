package kursach;

import java.util.ArrayList;
import java.io.*;
import java.util.Date;
import java.util.Scanner;


public class ScheduleGenerator
{
    public int count;
    public ArrayList<Ship> ships;
    ScheduleGenerator()
    {
        ships = new ArrayList<Ship>();
    }
    public void generateShips()
    {
        count = 300;
        for (int i = 0; i < count; i++)
        {
            Date date = new Date();
            date.setYear(121);
            date.setMonth(2);
            date.setDate(rnd(-7, 38));
            date.setHours(rnd(0, 23));
            date.setMinutes(rnd(0, 59));
            date.setSeconds(rnd(0, 59));
            Ship.cargoType cargo = Ship.cargoType.CONTAINER;
            switch(rnd(0, 2)){
                case 0:
                    cargo = Ship.cargoType.LOOSE;
                    break;
                case 1:
                    cargo = Ship.cargoType.LIQUID;
                    break;
                case 2:
                    cargo = Ship.cargoType.CONTAINER;
                    break;
            }
            int weight = 0;
            switch(cargo){
                case LOOSE:
                    weight = rnd(10000, 50000);
                    break;
                case LIQUID:
                    weight = rnd(10000, 30000);
                    break;
                case CONTAINER:
                    weight = rnd(6000, 18000);
                    break;
            }
            String name;
            name = getShipName();
            Ship ship = new Ship(name, date, weight, cargo);
            ships.add(ship);
        }
        for (int i = 0; i < count; i++)
        {
            System.out.print("Имя корабля: ");
            System.out.println(ships.get(i).getName());
            System.out.print("Тип корабля: ");
            System.out.println(ships.get(i).getCargoType());
            System.out.print("Вес в тоннах: ");
            System.out.println(ships.get(i).getWeight());
            System.out.print("Дата прибытия: ");
            System.out.println(ships.get(i).getDate());
            System.out.print("Планируемый срок стоянки в минутах: ");
            System.out.println(ships.get(i).getPlannedTime());
            System.out.println('\n');
        }
    }
    private String getShipName()
    {
        String name = new String();
        try(FileReader reader = new FileReader(System.getProperty("user.dir") + "/shipNames"))
        {
            int stringCount = rnd(1, 457);
            int c;
            for (int i = 1; i < stringCount; i++)
            {
                c = reader.read();
                while((char)c != '\n')
                {
                    c = reader.read();
                }
            }
            c = reader.read();
            while((char)c != '\n')
            {
                name += (char)c;
                if ((c = reader.read()) == -1)
                {
                    break;
                }
            }
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
        return name;
    }
    public static int rnd(int min, int max)
    {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }

    public void addShipFromConsole()
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Введите имя корабля: ");
        String name = in.nextLine();
        System.out.println("Задайте тип груза корабля(введите число от 0 до 2, где 0 - LOOSE, 1 - LIQUID, 2 - CONTAINER): ");
        int cargoIndex = in.nextInt();
        if (cargoIndex < 0 || cargoIndex > 2)
        {
            throw new IllegalArgumentException("Число должно быть от 0 до 2! Попробуйте заново");
        }
        Ship.cargoType cargo = Ship.cargoType.CONTAINER;
        int weight = 0;
        switch(cargoIndex){
            case 0:
                cargo = Ship.cargoType.LOOSE;
                System.out.println("Задайте вес корабля в тоннах(от 10000 до 50000): ");
                weight = in.nextInt();
                if (weight < 10000 || weight > 50000)
                {
                    throw new IllegalArgumentException("Число должно быть от 10000 до 50000! Попробуйте заново");
                }
                break;
            case 1:
                cargo = Ship.cargoType.LIQUID;
                System.out.println("Задайте вес корабля в тоннах(от 10000 до 30000): ");
                weight = in.nextInt();
                if (weight < 10000 || weight > 30000)
                {
                    throw new IllegalArgumentException("Число должно быть от 10000 до 30000! Попробуйте заново");
                }
                break;
            case 2:
                cargo = Ship.cargoType.CONTAINER;
                System.out.println("Задайте вес корабля в тоннах(от 6000 до 18000): ");
                weight = in.nextInt();
                if (weight < 6000 || weight > 18000)
                {
                    throw new IllegalArgumentException("Число должно быть от 6000 до 18000! Попробуйте заново");
                }
                break;
        }
        System.out.println("Установите дату пребытия:\nВведите год(в соответствии с 0 - 1900, 1 - 1901 и тд): ");
        Date date = new Date();
        date.setYear(in.nextInt());
        System.out.println("Введите месяц(0-11): ");
        date.setMonth(in.nextInt());
        System.out.println("Введите день: ");
        date.setDate(in.nextInt());
        System.out.println("Введите час: ");
        date.setHours(in.nextInt());
        System.out.println("Введите минуты: ");
        date.setMinutes(in.nextInt());
        System.out.println("Введите секунды: ");
        date.setSeconds(in.nextInt());
        Ship ship = new Ship(name, date, weight, cargo);
        ships.add(ship);
        count++;
    }
}


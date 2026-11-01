package DS.DesignPattern.structural.decorator;


interface Coffee {
    int cost();
}

// Concrete component
class SimpleCoffee implements Coffee {

    public int cost() {
        return 50;
    }
}

// Base Decorator
abstract class CoffeeDecorator implements Coffee {

    protected Coffee coffee;

    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
}

// Concrete Decorator 1
class MilkDecorator extends CoffeeDecorator {

    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }

    public int cost() {
        return coffee.cost() + 20;
    }
}

// Concrete Decorator 2
class SugarDecorator extends CoffeeDecorator {

    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }

    public int cost() {
        return coffee.cost() + 10;
    }
}

// Client
public class Decorator {

    public static void main(String[] args) {

        Coffee coffee = new SimpleCoffee();

        // Add Milk
        coffee = new MilkDecorator(coffee);

        // Add Sugar
        coffee = new SugarDecorator(coffee);

        System.out.println("Total cost: " + coffee.cost());
    }
}

package DS.DesignPattern.creational.abstractFactory;


interface Shape {
    void printShape();
}

interface Shape3D {
    void printShape3D();
}

// 2D Shapes
class Circle implements Shape {
    @Override
    public void printShape() {
        System.out.println("Circle");
    }
}

class Rectangle implements Shape {
    @Override
    public void printShape() {
        System.out.println("Rectangle");
    }
}

// 3D Shapes
class Sphere implements Shape3D {
    @Override
    public void printShape3D() {
        System.out.println("Sphere");
    }
}

class Cube implements Shape3D {
    @Override
    public void printShape3D() {
        System.out.println("Cube");
    }
}

// Abstract Factory
interface ShapeFactory {
    Shape createShape();

    Shape3D create3DShape();
}

// Concrete Factory 1
class RoundShapeFactory implements ShapeFactory {

    @Override
    public Shape createShape() {
        return new Circle();
    }

    @Override
    public Shape3D create3DShape() {
        return new Sphere();
    }
}

// Concrete Factory 2
class RectangleShapeFactory implements ShapeFactory {

    @Override
    public Shape createShape() {
        return new Rectangle();
    }

    @Override
    public Shape3D create3DShape() {
        return new Cube();
    }
}

// Client
public class AbstractFactoryDemo {

    public static void main(String[] args) {

        // Create Round Shape family
        ShapeFactory factory = new RoundShapeFactory();

        Shape shape = factory.createShape();
        Shape3D shape3D = factory.create3DShape();

        shape.printShape();
        shape3D.printShape3D();

        System.out.println("----------");

        // Create Rectangle Shape family
        factory = new RectangleShapeFactory();

        shape = factory.createShape();
        shape3D = factory.create3DShape();

        shape.printShape();
        shape3D.printShape3D();
    }
}



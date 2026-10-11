package DS.DesignPattern.creational.factory;

public class ShapeFactory {

    public Shape getShape(String type) {
        if (type == null) {
            return null;
        }

        switch (type.toUpperCase()) {
            case "CIRCLE":
                return new Circle();

            case "RECTANGLE":
                return new Rectangle();

            case "SQUARE":
                return new Square();

            default:
                throw new IllegalArgumentException(
                        "Unknown shape type: " + type
                );
        }
    }
}
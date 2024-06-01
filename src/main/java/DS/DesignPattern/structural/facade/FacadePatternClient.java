package DS.DesignPattern.structural.facade;

/**
 *
 A Facade Pattern says that just "just provide a unified and simplified interface to a set of interfaces in a subsystem, therefore it hides the complexities of the subsystem from the client".

 In other words, Facade Pattern describes a higher-level interface that makes the sub-system easier to use.

 Practically, every Abstract Factory is a type of Facade.
 */
public class FacadePatternClient {

    public static void getPhone(String phone) {
        ShopKeeper sk = new ShopKeeper();
        switch (phone) {
            case "Iphone": {
                sk.iphoneSale();
            }
            break;
            case "Samsung": {
                sk.samsungSale();
            }
            break;
            case "BlackBerry": {
                sk.blackberrySale();
            }
            break;
            default: {
                System.out.println("Nothing You purchased");
            }
        }

    }
}


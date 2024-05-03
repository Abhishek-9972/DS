package ds.DesignPattern.structural.proxy;

/**
 * https://thecodingsimplified.com/proxy-design-pattern/
 *
 * The Proxy Design Pattern is a structural design pattern that provides a surrogate or placeholder for another object to control access to it. This pattern is useful when you want to add an extra layer of control over access to an object. The proxy acts as an intermediary, controlling access to the real object.
 */

public interface DatabaseExecutor {
    public void executeDatabase(String query) throws Exception;
}

package DS.DesignPattern.structural.proxy;

public class DatabaseExecuterImpl implements DatabaseExecutor{
    @Override
    public void executeDatabase(String query) throws Exception {
        System.out.println("Going to execute Query: " + query);
    }
}

package ds.DesignPattern.structural.proxy;

public class ProxyPatternExample {
    public static void main(String[] args) throws Exception {
        DatabaseExecutor nonAdminExecuter = new DatabaseExecutorProxy("NonAdmin", "Admin@123");
        nonAdminExecuter.executeDatabase("DELEE");

        DatabaseExecutor nonAdminExecuterDELETE = new DatabaseExecutorProxy("NonAdmin", "Admin@123");
        nonAdminExecuterDELETE.executeDatabase("DELETE");


        DatabaseExecutor adminExecuter = new DatabaseExecutorProxy("Admin", "Admin@123");
        adminExecuter.executeDatabase("DELETE");

    }
}

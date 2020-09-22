package jmvc;

public abstract class AppView {
    protected AppView() {
    }

    //NOTE: not sure yet if this is right params?
    public abstract void handle(AppController.ViewHandler handler);

}

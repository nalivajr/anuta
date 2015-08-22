package by.nalivajr.anuta.components.execution;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SingleThreadActionExecutor extends FixedPoolSizeActionExecutor {

    public SingleThreadActionExecutor() {
        super(1);
    }
}

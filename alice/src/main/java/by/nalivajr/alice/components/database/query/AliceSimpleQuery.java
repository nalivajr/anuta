package by.nalivajr.alice.components.database.query;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class AliceSimpleQuery<T> implements AliceQuery<T> {

    private final String selection;
    private final String[] args;
    private final Class<T> cls;

    public AliceSimpleQuery(String selection, String[] args, Class<T> cls) {
        this.selection = selection;
        this.args = args;
        this.cls = cls;
    }

    @Override
    public String getSelection() {
        return selection;
    }

    @Override
    public String[] getSelectionArgs() {
        return args;
    }

    @Override
    public Class<T> getTargetClass() {
        return cls;
    }
}

package by.nalivajr.alice.components.database.query;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public final class BaseRestriction implements Restriction {

    private String column;
    private String[] args;

    BaseRestriction(String column, String[] args) {
        this.column = column;
        this.args = args;
    }

    @Override
    public String getProperty() {
        return column;
    }

    @Override
    public String[] getArgs() {
        return args;
    }
}

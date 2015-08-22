package by.nalivajr.anuta.callbacks.database;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public interface CursorUpdatedListener {

    /**
     * This method is invoked when data, managed by cursor was updated in database and cursor should be reloaded
     */
    public void onDataUpdated();

    /**
     * Yhis method is invoked, when cursor was reloaded from database and ready to use
     */
    public void onRequeryFinished();
}

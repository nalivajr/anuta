package by.nalivajr.alice.components.database.models;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
public class SimpleDatabaseAccessSession implements DatabaseAccessSession {

    private EntityCache entityCache = new SimpleEntityCache();
    private int loadLevel = DatabaseAccessSession.LEVEL_ANNOTATION_BASED;

    @Override
    public EntityCache getCache() {
        return entityCache;
    }

    @Override
    public int getLoadLevel() {
        return loadLevel;
    }

    @Override
    public void setLoadLevel(int level) {
        this.loadLevel = level;
    }
}

package Buycraft.api;

import Buycraft.MyPlugin;
import Buycraft.util.Language;

import java.util.TimerTask;
import java.util.logging.Logger;

public abstract class ApiTask implements Runnable {

    public TimerTask sync(TimerTask task) {
        getPlugin().pendingPlayerCheckerTaskExecutor.schedule(task, 0);
        return task;
    }

    public TimerTask syncTimer(final Runnable r, long delay, long period) {
        if (getPlugin().isEnabled()) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    r.run();
                }
            };
            getPlugin().pendingPlayerCheckerTaskExecutor.schedule(task, delay, period);
            return task;
        }
        return null;
    }

    public void addTask(ApiTask task) {
        MyPlugin.getInstance().addTask(task);
    }

    public MyPlugin getPlugin() {
        return MyPlugin.getInstance();
    }

    public Language getLanguage() {
        return MyPlugin.getInstance().getLanguage();
    }

    public Api getApi() {
        return MyPlugin.getInstance().getApi();
    }

    public Logger getLogger() {
        return MyPlugin.getInstance().getLogger();
    }

}

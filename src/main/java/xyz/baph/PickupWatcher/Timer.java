package xyz.baph.PickupWatcher;

import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class Timer {
    private int Seconds;
    public int MaxSeconds;
    public Runnable OnComplete;
    private BukkitTask Task;

    public void Reset() {
        Cancel();
        Seconds = MaxSeconds;
    }

    public void Cancel() {
        if (Task != null) {
            Task.cancel();
        }
    }

    public Timer(int maxSeconds, Runnable onComplete) {
        MaxSeconds = maxSeconds;
        OnComplete = onComplete;
        Reset();
    }

    public void Start() {
        BukkitScheduler scheduler = PickupWatcher.plugin.getServer().getScheduler();
        Task = scheduler.runTaskTimer(PickupWatcher.plugin, () -> {
            if (Seconds == 0) {
                OnComplete.run();
                Task.cancel();
            } else {
                Seconds--;
            }
        }, 0, 20);
    }
}


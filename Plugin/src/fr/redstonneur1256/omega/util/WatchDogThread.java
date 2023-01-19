package fr.redstonneur1256.omega.util;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import fr.redstonneur1256.omega.Core2R2T;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.net.Packets;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;

public class WatchDogThread extends Thread {

    private Core2R2T plugin;
    private long lastTickTime;

    public WatchDogThread(Core2R2T plugin) {
        this.plugin = plugin;
        this.lastTickTime = Time.millis();
        this.setDaemon(true);
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        Thread mainAppThread = getAppThread();
        long crashTime = plugin.getConfiguration().getWatchdogCrashTime();

        lastTickTime = Time.millis();
        Events.run(EventType.Trigger.update, () -> lastTickTime = Time.millis());

        while(!thread.isInterrupted()) {
            long now = Time.millis();
            long difference = now - lastTickTime;

            if(difference < crashTime) {
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException exception) {
                    return;
                }
                continue;
            }
            // Assume server has crashed
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

            Log.err("Server has stopped responding (" + difference + " ms since last tick)");
            Log.err("-------------------------");
            Log.err("Main thread dump:");
            dumpThread(threadBean.getThreadInfo(mainAppThread.getId(), Integer.MAX_VALUE));
            Log.err("All threads dump:");
            Log.err("-------------------------");
            ThreadInfo[] threads = threadBean.dumpAllThreads(true, true);
            for(ThreadInfo threadInfo : threads) {
                if(threadInfo.getThreadId() == mainAppThread.getId()) {
                    continue;
                }
                dumpThread(threadInfo);
            }

            Groups.player.each(player -> player.kick(Packets.KickReason.serverRestarting, 0));
            System.exit(1);
        }
    }

    private void dumpThread(ThreadInfo thread) {
        Log.err("Thread: " + thread.getThreadName());
        Log.err("    ID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: " + thread.isInNative() + " | State: " + thread.getThreadState());

        MonitorInfo[] lockedMonitors = thread.getLockedMonitors();
        if(lockedMonitors.length != 0) {
            Log.err("    Waiting monitor(s):");
            for(MonitorInfo monitor : lockedMonitors) {
                Log.err("        Locked on: " + monitor.getLockedStackFrame());
            }
        }

        Log.err("    Thread stacktrace:");
        for(StackTraceElement element : thread.getStackTrace()) {
            Log.err("        " + element);
        }
        Log.err("-------------------------");
    }

    public Thread getAppThread() {
        try {
            Class<?> application = Class.forName("arc.backend.headless.HeadlessApplication");
            Field field = application.getDeclaredField("mainLoopThread");
            field.setAccessible(true);

            return (Thread) field.get(Core.app);
        } catch(Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}

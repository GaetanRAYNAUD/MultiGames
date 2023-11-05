package fr.graynaud.multigames.object.common;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TimerLabel extends Text {

    private final IntegerProperty elapsed = new SimpleIntegerProperty(0);

    private final ScheduledExecutorService scheduledExecutorService;

    private ScheduledFuture<?> future = null;

    public TimerLabel() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        this.elapsed.addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue)) {
                Duration duration = Duration.ofSeconds(this.elapsed.get());

                if (duration.toHoursPart() > 0) {
                    setText(String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()));
                } else {
                    setText(String.format("%02d:%02d", duration.toMinutesPart(), duration.toSecondsPart()));
                }
            }
        });
        setFont(Font.font(18));
    }

    public void pause() {
        if (this.future != null) {
            this.future.cancel(true);
            this.future = null;
        }
    }

    public void resetAndStart() {
        if (this.future != null) {
            this.future.cancel(true);
        }

        this.elapsed.setValue(0);
        this.future = this.scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(() -> elapsed.setValue(elapsed.get() + 1)), 1, 1,
                                                                        TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.future != null) {
            this.future.cancel(true);
        }

        this.scheduledExecutorService.shutdownNow();
    }
}

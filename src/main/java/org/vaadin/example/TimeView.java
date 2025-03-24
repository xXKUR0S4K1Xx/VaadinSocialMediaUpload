package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;

@Route("time")
public class TimeView extends VerticalLayout {

    private Instant startTime;
    private Label timeLabel;
    private Label currentTimeLabel;

    public TimeView() {
        startTime = Instant.now(); // Record start time when view is opened

        timeLabel = new Label("Seconds since the view opened: 0");
        currentTimeLabel = new Label("Current Time: " + LocalTime.now().toString());

        Button updateTimeButton = new Button("Update Time", e -> updateTime());
        Button showCurrentTimeButton = new Button("Show Current Time", e -> showCurrentTime());

        // RouterLink to navigate back to MainView
        RouterLink mainMenuLink = new RouterLink("Main Menu", MainView.class);

        // Style the main menu link to position it at the bottom left
        mainMenuLink.getElement().getStyle().set("position", "absolute")
                .set("bottom", "10px")
                .set("left", "10px");

        add(timeLabel, currentTimeLabel, updateTimeButton, showCurrentTimeButton, mainMenuLink);
    }

    private void updateTime() {
        // Calculate seconds since the view opened
        Duration duration = Duration.between(startTime, Instant.now());
        long seconds = duration.getSeconds();
        timeLabel.setText("Seconds since the view opened: " + seconds);
    }

    private void showCurrentTime() {
        // Display current time
        currentTimeLabel.setText("Current Time: " + LocalTime.now().toString());
    }
}

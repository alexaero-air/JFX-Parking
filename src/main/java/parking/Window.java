package parking;

import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Window {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final int CARS = 12;
    private static final int SPRITES = 3;
    private static final double CAR_W = 42;
    private static final double CAR_H = 76;
    private static final double SPOT_W = 86;
    private static final double SPOT_H = 98;
    private static final double GAP = 10;
    private static final double PARK_X = 40;
    private static final double PARK_Y = 220;
    private static final Image[] CAR_IMAGES = {
            loadCar(1), loadCar(2), loadCar(3)
    };
    private static final Rectangle2D[] CAR_CROP = {
            new Rectangle2D(575, 190, 847, 1620),
            new Rectangle2D(545, 190, 910, 1620),
            new Rectangle2D(98, 10, 163, 340)
    };

    private final Parking parking = new Parking();
    private final Clock clock = new Clock();
    private final Journal journal = new Journal(clock.now());
    private final Pane board = new Pane();
    private final Rectangle parkingArea = new Rectangle();
    private final Label clockLabel = new Label();
    private final Label freeLabel = new Label();
    private final Label busyLabel = new Label();
    private final Label todayLabel = new Label();
    private final Label totalLabel = new Label();
    private final Label details = new Label();
    private final TextArea messages = new TextArea();
    private final CarView[] onField = new CarView[CARS];

    private int nextSprite;
    private int totalMoney;
    private Integer detailsSpot;

    public Window(Stage stage) {
        drawParking();
        for (int i = 0; i < CARS; i++) {
            putNewCar(i);
        }

        messages.setEditable(false);
        messages.setWrapText(true);
        messages.setPrefRowCount(7);
        details.setWrapText(true);
        details.setPrefWidth(320);
        details.setTextFill(Color.WHITE);
        details.setText("Перетащите машину на свободное место.\nДля выезда вынесите её целиком за голубую рамку.");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0b1a24;");
        root.setTop(header());
        root.setCenter(board);
        root.setBottom(bottom());

        Scene scene = new Scene(root, 1180, 820);
        stage.setTitle("Парковка аэропорта");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> say(journal.close(clock.now())));
        stage.show();

        Timeline tick = new Timeline(new KeyFrame(javafx.util.Duration.millis(100), e -> refresh()));
        tick.setCycleCount(Animation.INDEFINITE);
        tick.play();
        refresh();
        say("Перетащите автомобиль на свободное место. Часы уже идут.");
    }

    private HBox header() {
        Label title = new Label("Парковка аэропорта");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(20));
        clockLabel.setTextFill(Color.web("#7ee0ff"));
        clockLabel.setFont(Font.font(20));
        styleMetric(todayLabel);
        styleMetric(totalLabel);

        Button m20 = skip("+20 мин", Duration.ofMinutes(20));
        Button h1 = skip("+1 ч", Duration.ofHours(1));
        Button h12 = skip("+12 ч", Duration.ofHours(12));
        Button d1 = skip("+1 сут", Duration.ofHours(24));

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);

        HBox row = new HBox(12,
                title,
                todayLabel, totalLabel,
                m20, h1, h12, d1,
                grow, clockLabel);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #0e2432;");
        return row;
    }

    private HBox bottom() {
        VBox logBox = new VBox(6, label("Сообщения"), messages);
        VBox infoBox = new VBox(6, label("Место"), details);

        infoBox.setPadding(new Insets(0, 12, 0, 0));
        HBox.setHgrow(logBox, Priority.ALWAYS);
        HBox box = new HBox(12, logBox, infoBox);
        box.setPadding(new Insets(10, 12, 12, 12));
        return box;
    }

    private void drawParking() {
        board.setPrefSize(1140, 500);
        board.setStyle("-fx-background-color: #10202c;");

        Rectangle staging = new Rectangle(24, 8, 1090, 188);
        staging.setArcWidth(16);
        staging.setArcHeight(16);
        staging.setFill(Color.web("#143044"));
        staging.setStroke(Color.web("#3aa0c0"));
        board.getChildren().addAll(staging, caption("Доступные автомобили", 36, 22));

        double parkW = 10 * SPOT_W + 9 * GAP;
        parkingArea.setX(PARK_X - 12);
        parkingArea.setY(PARK_Y - 12);
        parkingArea.setWidth(parkW + 24);
        parkingArea.setHeight(2 * SPOT_H + GAP + 36);
        parkingArea.setArcWidth(14);
        parkingArea.setArcHeight(14);
        parkingArea.setFill(Color.web("#1a2430"));
        parkingArea.setStroke(Color.web("#5ce1ff"));
        parkingArea.setStrokeWidth(2);

        styleMetric(freeLabel);
        styleMetric(busyLabel);
        freeLabel.setLayoutX(PARK_X + 220);
        freeLabel.setLayoutY(PARK_Y - 38);
        busyLabel.setLayoutX(PARK_X + 360);
        busyLabel.setLayoutY(PARK_Y - 38);

        board.getChildren().addAll(
                parkingArea,
                caption("Парковка · 20 мест", PARK_X, PARK_Y - 22),
                freeLabel,
                busyLabel);

        for (int n = 1; n <= Parking.SPOTS; n++) {
            Rectangle cell = new Rectangle(spotX(n), spotY(n), SPOT_W, SPOT_H);
            cell.setArcWidth(8);
            cell.setArcHeight(8);
            cell.setFill(Color.web("#2e5a46", 0.55));
            cell.setStroke(Color.web("#6dffb0"));
            Text num = new Text(spotX(n) + 6, spotY(n) + 14, "№" + n);
            num.setFill(Color.web("#d5e7ef"));
            int spot = n;
            cell.setOnMouseClicked(e -> showSpot(spot));
            board.getChildren().addAll(cell, num);
        }
    }

    private void putNewCar(int slot) {
        int sprite = nextSprite;
        nextSprite = (nextSprite + 1) % SPRITES;
        CarView view = new CarView(new Car(), slot, sprite);
        onField[slot] = view;
        home(view);
        board.getChildren().add(view);
        view.toFront();
    }

    private void home(CarView view) {
        view.spot = 0;
        view.setLayoutX(stageX(view.slot));
        view.setLayoutY(stageY(view.slot));
    }

    private void snap(CarView view, int spot) {
        view.spot = spot;
        view.setLayoutX(spotX(spot) + (SPOT_W - CAR_W) / 2);
        view.setLayoutY(spotY(spot) + (SPOT_H - CAR_H) / 2);
        view.toFront();
    }

    private void onPress(CarView view, MouseEvent e) {
        view.dragX = view.getLayoutX();
        view.dragY = view.getLayoutY();
        view.mouseX = e.getSceneX();
        view.mouseY = e.getSceneY();
        view.moved = false;
        view.toFront();
    }

    private void onDrag(CarView view, MouseEvent e) {
        double dx = e.getSceneX() - view.mouseX;
        double dy = e.getSceneY() - view.mouseY;
        if (Math.hypot(dx, dy) > 4) {
            view.moved = true;
        }
        view.setLayoutX(view.dragX + dx);
        view.setLayoutY(view.dragY + dy);
    }

    private void onRelease(CarView view) {
        if (!view.moved) {
            if (view.spot > 0) {
                showSpot(view.spot);
            }
            return;
        }
        if (view.spot == 0) {
            dropFromField(view);
        } else {
            dropFromParking(view);
        }
    }

    private void dropFromField(CarView view) {
        int spot = spotAtCenter(view);
        if (spot == 0) {
            home(view);
            say("Автомобиль " + view.car.plate + " возвращён на исходное поле.");
            return;
        }
        if (!parking.park(view.car, spot, clock.now())) {
            home(view);
            say("Место №" + spot + " занято. Автомобиль " + view.car.plate + " возвращён на исходное поле.");
            return;
        }
        int slot = view.slot;
        onField[slot] = null;
        snap(view, spot);
        putNewCar(slot);
        showSpot(spot);
        say(journal.enter(view.car.plate, spot, parking.entered[spot - 1]));
        refresh();
    }

    private void dropFromParking(CarView view) {
        if (outsideParking(view)) {
            int spot = view.spot;
            LocalDateTime in = parking.entered[spot - 1];
            LocalDateTime out = clock.now();
            int money = Tariff.pay(in, out);
            parking.leave(spot);
            totalMoney += money;
            board.getChildren().remove(view);
            say(journal.leave(view.car.plate, in, out, money));
            detailsSpot = null;
            details.setText("Выезд · место №" + spot
                    + "\nНомер: " + view.car.plate
                    + "\nВъезд: " + FMT.format(in)
                    + "\nВыезд: " + FMT.format(out)
                    + "\nОплачено: " + money + " ₽");
            refresh();
            return;
        }
        snap(view, view.spot);
        say("Автомобиль " + view.car.plate + " остался на месте №" + view.spot + ".");
        showSpot(view.spot);
    }

    private boolean outsideParking(CarView view) {
        return !parkingArea.getBoundsInParent().intersects(
                view.getLayoutX(), view.getLayoutY(), CAR_W, CAR_H);
    }

    private int spotAtCenter(CarView view) {
        double cx = view.getLayoutX() + CAR_W / 2;
        double cy = view.getLayoutY() + CAR_H / 2;
        for (int n = 1; n <= Parking.SPOTS; n++) {
            if (cx >= spotX(n) && cx <= spotX(n) + SPOT_W && cy >= spotY(n) && cy <= spotY(n) + SPOT_H) {
                return n;
            }
        }
        return 0;
    }

    private void showSpot(int n) {
        detailsSpot = n;
        if (parking.isFree(n)) {
            details.setText("Место №" + n + " свободно.");
            return;
        }
        details.setText(spotText(n));
    }

    private String spotText(int n) {
        Car car = parking.cars[n - 1];
        LocalDateTime in = parking.entered[n - 1];
        LocalDateTime now = clock.now();
        Duration stay = Duration.between(in, now);
        return "Место №" + n + " · занято"
                + "\nНомер: " + car.plate
                + "\nВъезд: " + FMT.format(in)
                + "\nДлительность: " + stay.toHours() + " ч "
                + stay.toMinutesPart() + " мин " + stay.toSecondsPart() + " с"
                + "\nТекущая стоимость: " + Tariff.pay(in, now) + " ₽";
    }

    private void refresh() {
        String days = journal.syncDays(clock.now());
        if (!days.isBlank()) {
            say(days);
        }
        clockLabel.setText(FMT.format(clock.now()));
        freeLabel.setText("Свободно: " + parking.freeCount());
        busyLabel.setText("Занято: " + parking.busyCount());
        todayLabel.setText("Сегодня: " + journal.todayMoney() + " ₽");
        totalLabel.setText("Всего: " + totalMoney + " ₽");
        if (detailsSpot != null && !parking.isFree(detailsSpot)) {
            details.setText(spotText(detailsSpot));
        }
    }

    private void say(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (messages.getText().isBlank()) {
            messages.setText(text);
        } else {
            messages.appendText("\n" + text);
        }
        messages.positionCaret(messages.getLength());
    }

    private Button skip(String title, Duration time) {
        Button b = new Button(title);
        b.setOnAction(e -> {
            clock.skip(time);
            refresh();
        });
        return b;
    }

    private static void styleMetric(Label label) {
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(14));
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#d7eef7"));
        return l;
    }

    private static Text caption(String text, double x, double y) {
        Text t = new Text(x, y, text);
        t.setFill(Color.web("#d7eef7"));
        return t;
    }

    private static Image loadCar(int n) {
        String path = "/cars/" + n + ".png";
        InputStream in = Window.class.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalStateException("Не найден ресурс " + path);
        }
        return new Image(in);
    }

    private static double spotX(int n) {
        return PARK_X + ((n - 1) % 10) * (SPOT_W + GAP);
    }

    private static double spotY(int n) {
        return PARK_Y + ((n - 1) / 10) * (SPOT_H + 16);
    }

    private static double stageX(int slot) {
        double cell = 92;
        double grid = 6 * cell;
        double start = (1140 - grid) / 2;
        return start + (slot % 6) * cell + (cell - CAR_W) / 2;
    }

    private static double stageY(int slot) {
        return 36 + (slot / 6) * 82;
    }

    private final class CarView extends Group {
        final Car car;
        final int slot;
        int spot;
        double dragX, dragY, mouseX, mouseY;
        boolean moved;

        CarView(Car car, int slot, int sprite) {
            this.car = car;
            this.slot = slot;

            ImageView body = new ImageView(CAR_IMAGES[sprite]);
            body.setViewport(CAR_CROP[sprite]);
            body.setFitWidth(CAR_W);
            body.setFitHeight(CAR_H);
            body.setPreserveRatio(false);
            body.setSmooth(true);

            double plateW = 40;
            double plateH = 13;
            double plateX = (CAR_W - plateW) / 2;
            double plateY = CAR_H * 0.42;
            Rectangle plate = new Rectangle(plateW, plateH);
            plate.setX(plateX);
            plate.setY(plateY);
            plate.setArcWidth(3);
            plate.setArcHeight(3);
            plate.setFill(Color.web("#f4f7ee"));
            plate.setStroke(Color.web("#1a1a1a"));

            Text number = new Text(car.plate);
            number.setFont(Font.font(car.plate.length() > 8 ? 7 : 8));
            number.setX(plateX + (plateW - number.getLayoutBounds().getWidth()) / 2);
            number.setY(plateY + 10);

            getChildren().addAll(body, plate, number);
            setOnMousePressed(e -> onPress(this, e));
            setOnMouseDragged(e -> onDrag(this, e));
            setOnMouseReleased(e -> onRelease(this));
        }
    }
}

package cuie.nightbehaviour.mapcontrol;

import java.util.List;
import java.util.Locale;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MarkerEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

/**
 * Mein Custom Control ist eine Karte auf welcher die Koordinaten des
 * ausgewählten Wolkenkratzer visualisiert werden kann.
 * Der Marker kann durch anklicken und bewegen der Karte oder durch Anpassung der Koordinaten verschoben werden.
 * Für die Karte verwende ich den JavaFX8 Komponent von "https://www.sothawo.com/projects/mapjfx/".
 *
 *
 * Hanna Lisa Franz, 3iCa
 */
public class MapControl extends Region {
    private static final Locale CH = new Locale("de", "CH");

    private static final double ARTBOARD_WIDTH  = 400;
    private static final double ARTBOARD_HEIGHT = 300;

    private static final double ASPECT_RATIO = ARTBOARD_WIDTH / ARTBOARD_HEIGHT;

    private static final double MINIMUM_WIDTH  = 100;
    private static final double MINIMUM_HEIGHT = MINIMUM_WIDTH / ASPECT_RATIO;

    private static final double MAXIMUM_WIDTH = 2000;

    private MapView mapView;
    private Marker skyscraperMarker;
    private Marker editingMarker;

    private final DoubleProperty longitude = new SimpleDoubleProperty();
    private final DoubleProperty latitude = new SimpleDoubleProperty();
    private final BooleanProperty isEditing = new SimpleBooleanProperty();

    // needed for resizing
    private Pane drawingPane;

    public MapControl() {
        initializeSelf();
        initializeParts();
        initializeDrawingPane();
        layoutParts();
        setupEventHandlers();
        setupValueChangeListeners();
        setupBindings();
    }

    private void initializeSelf() {
        // load stylesheets
        String fonts = getClass().getResource("/fonts/fonts.css").toExternalForm();
        getStylesheets().add(fonts);

        String stylesheet = getClass().getResource("style.css").toExternalForm();
        getStylesheets().add(stylesheet);

        getStyleClass().add("map-control");
    }

    private void initializeParts() {
        mapView = new MapView();
        mapView.setMapType(MapType.OSM);
        mapView.setZoom(14);
        mapView.setCenter(new Coordinate(0.0,0.0));
        mapView.setMaxSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        mapView.setMinSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        mapView.setPrefSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);

        mapView.initialize(Configuration.builder()
                .projection(Projection.WEB_MERCATOR)
                .showZoomControls(false)
                .build());

        skyscraperMarker = new Marker(
                getClass().getResource("/skyscraper.png"),0,-100)
                .setVisible(true);

        editingMarker = new Marker(
                getClass().getResource("/editedSkyscraper.png"),0,-100)
                .setVisible(false);
    }

    private void initializeDrawingPane() {
        drawingPane = new Pane();
        drawingPane.getStyleClass().add("drawing-pane");
        drawingPane.setMaxSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setMinSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
        drawingPane.setPrefSize(ARTBOARD_WIDTH, ARTBOARD_HEIGHT);
    }

    private void layoutParts() {
        drawingPane.getChildren().addAll(mapView);

        getChildren().add(drawingPane);
    }

    private void setupEventHandlers() {
        // After map is initialized
        mapView.initializedProperty().addListener((observableValue, oldValue, newValue) -> {
            mapView.addMarker(skyscraperMarker);
            mapView.addMarker(editingMarker);
        });

        // When marker is clicked
        mapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> isEditing.setValue(!isEditing.get()));
    }

    private void setupValueChangeListeners() {
        // When position changes
        ChangeListener<Number> onPositionChanged = (observableValue, number, t1) -> {
            Coordinate newCoordinate = new Coordinate(latitude.get(), longitude.get());
            skyscraperMarker.setPosition(newCoordinate);
            mapView.setCenter(newCoordinate);
        };

        latitude.addListener(onPositionChanged);
        longitude.addListener(onPositionChanged);

        // When map is moved
        mapView.centerProperty().addListener((observable, oldValue, newValue) -> {
            if (isEditing.get()) {
                if (newValue.getLatitude() != latitude.get()) latitude.setValue(newValue.getLatitude());
                if (newValue.getLongitude() != longitude.get()) longitude.setValue(newValue.getLongitude());
            }
        });
    }

    private void setupBindings() {
        editingMarker.positionProperty().bind(skyscraperMarker.positionProperty());
        editingMarker.visibleProperty().bind(isEditing);
    }

    //resize by scaling
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        resize();
    }

    private void resize() {
        Insets padding         = getPadding();
        double availableWidth  = getWidth() - padding.getLeft() - padding.getRight();
        double availableHeight = getHeight() - padding.getTop() - padding.getBottom();

        double width = Math.max(Math.min(Math.min(availableWidth, availableHeight * ASPECT_RATIO), MAXIMUM_WIDTH), MINIMUM_WIDTH);

        double scalingFactor = width / ARTBOARD_WIDTH;

        if (availableWidth > 0 && availableHeight > 0) {
            relocateDrawingPaneCentered();
            drawingPane.setScaleX(scalingFactor);
            drawingPane.setScaleY(scalingFactor);
        }
    }

    private void relocateDrawingPaneCentered() {
        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, (getHeight() - ARTBOARD_HEIGHT) * 0.5);
    }

    private void relocateDrawingPaneCenterBottom(double scaleY, double paddingBottom) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double visualSpace  = getHeight() - visualHeight;
        double y            = visualSpace + (visualHeight - ARTBOARD_HEIGHT) * 0.5 - paddingBottom;

        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
    }

    private void relocateDrawingPaneCenterTop(double scaleY, double paddingTop) {
        double visualHeight = ARTBOARD_HEIGHT * scaleY;
        double y            = (visualHeight - ARTBOARD_HEIGHT) * 0.5 + paddingTop;

        drawingPane.relocate((getWidth() - ARTBOARD_WIDTH) * 0.5, y);
    }

    // compute sizes

    @Override
    protected double computeMinWidth(double height) {
        Insets padding           = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return MINIMUM_WIDTH + horizontalPadding;
    }

    @Override
    protected double computeMinHeight(double width) {
        Insets padding         = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return MINIMUM_HEIGHT + verticalPadding;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets padding           = getPadding();
        double horizontalPadding = padding.getLeft() + padding.getRight();

        return ARTBOARD_WIDTH + horizontalPadding;
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets padding         = getPadding();
        double verticalPadding = padding.getTop() + padding.getBottom();

        return ARTBOARD_HEIGHT + verticalPadding;
    }

    // alle getter und setter  (generiert via "Code -> Generate... -> Getter and Setter)

    public double getLongitude() {
        return longitude.get();
    }

    public DoubleProperty longitudeProperty() {
        return longitude;
    }

    public double getLatitude() {
        return latitude.get();
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    public boolean isIsEditing() {
        return isEditing.get();
    }

    public BooleanProperty isEditingProperty() {
        return isEditing;
    }
}

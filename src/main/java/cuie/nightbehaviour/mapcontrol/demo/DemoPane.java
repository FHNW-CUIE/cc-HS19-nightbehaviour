package cuie.nightbehaviour.mapcontrol.demo;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import cuie.nightbehaviour.mapcontrol.MapControl;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.w3c.dom.Text;

public class DemoPane extends BorderPane {

    private final PresentationModel pm;

    // declare the custom control
    private MapControl cc;

    // all controls
    private TextField longitude;
    private TextField latitude;

    public DemoPane(PresentationModel pm) {
        this.pm = pm;
        initializeControls();
        layoutControls();
        setupBindings();

        longitude.textProperty().setValue("8,1981568");
        latitude.textProperty().setValue("47,4742283");
    }

    private void initializeControls() {
        setPadding(new Insets(10));

        cc = new MapControl();

        longitude = new TextField();
        latitude = new TextField();
    }

    private void layoutControls() {
        VBox controlPane = new VBox(new Label("SimpleControl Properties"),
                                    longitude, latitude);
        controlPane.setPadding(new Insets(0, 50, 0, 50));
        controlPane.setSpacing(10);

        setCenter(cc);
        setRight(controlPane);
    }

    private void setupBindings() {
        StringConverter<Number> converter = new NumberStringConverter();
        Bindings.bindBidirectional(longitude.textProperty(), cc.longitudeProperty(), converter);
        Bindings.bindBidirectional(latitude.textProperty(), cc.latitudeProperty(), converter);
        cc.baseColorProperty().bindBidirectional(pm.baseColorProperty());
    }

}

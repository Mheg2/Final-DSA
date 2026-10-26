package org.example.microbuild;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class QuoteController {

    @FXML private TextField cpuField, cpuPrice, moboField, moboPrice, ramField, ramPrice;
    @FXML private TextField gpuField, gpuPrice, psuField, psuPrice;
    @FXML private TextField storageField, storagePrice, caseField, casePrice;
    @FXML private TextField powerField, psuRecField, totalField;
    @FXML private TextArea statusArea;

    private final CompatibilityChecker checker = new CompatibilityChecker();

    public void setBuild(BuildConfiguration b) {
        show(cpuField, cpuPrice, b.getCpu());
        show(moboField, moboPrice, b.getMotherboard());
        show(ramField, ramPrice, b.getRam());
        show(gpuField, gpuPrice, b.getGpu());
        show(psuField, psuPrice, b.getPsu());
        show(caseField, casePrice, b.getPcCase());

        List<Storage> drives = b.getStorageDrives();
        if (drives.isEmpty()) {
            storageField.clear();
            storagePrice.clear();
        } else {
            storageField.setText(drives.stream().map(Storage::getName).collect(Collectors.joining(", ")));
            storagePrice.setText(String.format("PHP %,.0f",
                    drives.stream().mapToDouble(Storage::getPrice).sum()));
        }

        powerField.setText(PowerCalculator.calculateTotalLoadWatts(b) + " W");
        psuRecField.setText(PowerCalculator.calculateRecommendedPsuWattage(b) + " W");
        totalField.setText(String.format("PHP %,.2f", b.getTotalPrice()));

        statusArea.getStyleClass().removeAll("status-pass", "status-fail");
        if (b.getAllComponents().isEmpty()) {
            statusArea.setText("No parts selected.");
            return;
        }
        List<String> issues = checker.check(b);
        if (issues.isEmpty()) {
            statusArea.setText("PASSED - all selected parts are compatible.");
            statusArea.getStyleClass().add("status-pass");
        } else {
            statusArea.setText(String.join("\n", issues));
            statusArea.getStyleClass().add("status-fail");
        }
    }

    private void show(TextField name, TextField price, Component c) {
        if (c == null) {
            name.clear();
            price.clear();
        } else {
            name.setText(c.getName());
            price.setText(String.format("PHP %,.0f", c.getPrice()));
        }
    }

    @FXML
    private void onClear() {
        for (TextField f : new TextField[]{cpuField, cpuPrice, moboField, moboPrice, ramField, ramPrice,
                gpuField, gpuPrice, psuField, psuPrice, storageField, storagePrice, caseField, casePrice,
                powerField, psuRecField, totalField}) {
            f.clear();
        }
        statusArea.getStyleClass().removeAll("status-pass", "status-fail");
        statusArea.clear();
    }

    @FXML
    private void onBack() {
        ((Stage) totalField.getScene().getWindow()).close();
    }
}
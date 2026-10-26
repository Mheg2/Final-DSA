package org.example.microbuild;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // --- FXML Components ---
    @FXML private TextField searchBarTextField;
    @FXML private ListView<CPU> cpuListView;
    @FXML private ListView<Motherboard> motherboardListView;
    @FXML private ListView<RAM> ramListView;
    @FXML private ListView<GPU> gpuListView;
    @FXML private ListView<PSU> psuListView;
    @FXML private ListView<Storage> storageListView;
    @FXML private ListView<CaseComponent> caseListView;

    @FXML private Label totalPriceLabel;
    @FXML private Label powerDrawLabel;
    @FXML private Label recommendedPsuLabel;
    @FXML private Label quoteOutputTextArea; // Status Label sa ibaba

    // --- Backend Model Objects ---
    private InventoryManager inventory;
    private BuildConfiguration currentBuild;
    private CompatibilityChecker compatibilityChecker;
    private QuoteGenerator quoteGenerator;

    // Master lists para sa search filtering
    private ObservableList<CPU> allCpus = FXCollections.observableArrayList();
    private ObservableList<Motherboard> allMotherboards = FXCollections.observableArrayList();
    private ObservableList<RAM> allRams = FXCollections.observableArrayList();
    private ObservableList<GPU> allGpus = FXCollections.observableArrayList();
    private ObservableList<PSU> allPsus = FXCollections.observableArrayList();
    private ObservableList<Storage> allStorages = FXCollections.observableArrayList();
    private ObservableList<CaseComponent> allCases = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        inventory = seedInventory();
        currentBuild = new BuildConfiguration("My Custom PC");
        compatibilityChecker = new CompatibilityChecker();
        quoteGenerator = new QuoteGenerator();

        loadInventoryToLists();
        setupSelectionListeners();
        setupSearchFilter();
    }

    private void loadInventoryToLists() {
        for (Component c : inventory.getCatalog().values()) {
            if (c instanceof CPU) allCpus.add((CPU) c);
            else if (c instanceof Motherboard) allMotherboards.add((Motherboard) c);
            else if (c instanceof RAM) allRams.add((RAM) c);
            else if (c instanceof GPU) allGpus.add((GPU) c);
            else if (c instanceof PSU) allPsus.add((PSU) c);
            else if (c instanceof Storage) allStorages.add((Storage) c);
            else if (c instanceof CaseComponent) allCases.add((CaseComponent) c);
        }

        cpuListView.setItems(allCpus);
        motherboardListView.setItems(allMotherboards);
        ramListView.setItems(allRams);
        gpuListView.setItems(allGpus);
        psuListView.setItems(allPsus);
        storageListView.setItems(allStorages);
        caseListView.setItems(allCases);
    }

    private void setupSelectionListeners() {
        cpuListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentBuild.setCpu(newVal);
            updateSummary();
        });

        motherboardListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentBuild.setMotherboard(newVal);
            updateSummary();
        });

        ramListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentBuild.setRam(newVal);
            updateSummary();
        });

        gpuListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentBuild.setGpu(newVal);
            updateSummary();
        });

        psuListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentBuild.setPsu(newVal);
            updateSummary();
        });

        storageListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentBuild.getStorageDrives().clear();
                currentBuild.addStorage(newVal);
                updateSummary();
            }
        });

        caseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentBuild.setPcCase(newVal);
            updateSummary();
        });
    }

    private void setupSearchFilter() {
        searchBarTextField.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.toLowerCase();

            cpuListView.setItems(allCpus.filtered(c -> c.getName().toLowerCase().contains(query) || c.getId().toLowerCase().contains(query)));
            motherboardListView.setItems(allMotherboards.filtered(m -> m.getName().toLowerCase().contains(query) || m.getId().toLowerCase().contains(query)));
            ramListView.setItems(allRams.filtered(r -> r.getName().toLowerCase().contains(query) || r.getId().toLowerCase().contains(query)));
            gpuListView.setItems(allGpus.filtered(g -> g.getName().toLowerCase().contains(query) || g.getId().toLowerCase().contains(query)));
            psuListView.setItems(allPsus.filtered(p -> p.getName().toLowerCase().contains(query) || p.getId().toLowerCase().contains(query)));
            storageListView.setItems(allStorages.filtered(s -> s.getName().toLowerCase().contains(query) || s.getId().toLowerCase().contains(query)));
            caseListView.setItems(allCases.filtered(cs -> cs.getName().toLowerCase().contains(query) || cs.getId().toLowerCase().contains(query)));
        });
    }

    private void updateSummary() {
        totalPriceLabel.setText(String.format("Total Price: PHP %.2f", currentBuild.getTotalPrice()));

        int totalWatts = PowerCalculator.calculateTotalLoadWatts(currentBuild);
        int recommendedWatts = PowerCalculator.calculateRecommendedPsuWattage(currentBuild);
        powerDrawLabel.setText("Estimated Load: " + totalWatts + " W");
        recommendedPsuLabel.setText("Recommended PSU: " + recommendedWatts + " W");

        var issues = compatibilityChecker.check(currentBuild);
        if (issues.isEmpty()) {
            quoteOutputTextArea.setText("Status: PASSED - All selected parts are compatible!");
        } else {
            StringBuilder sb = new StringBuilder("Status: FAILED - " + issues.size() + " issue(s) found:\n");
            for (String issue : issues) {
                sb.append("• ").append(issue).append("\n");
            }
            quoteOutputTextArea.setText(sb.toString());
        }
    }

    @FXML
    private void handleClearBuild() {
        cpuListView.getSelectionModel().clearSelection();
        motherboardListView.getSelectionModel().clearSelection();
        ramListView.getSelectionModel().clearSelection();
        gpuListView.getSelectionModel().clearSelection();
        psuListView.getSelectionModel().clearSelection();
        storageListView.getSelectionModel().clearSelection();
        caseListView.getSelectionModel().clearSelection();

        currentBuild = new BuildConfiguration("My Custom PC");
        updateSummary();
    }

    @FXML
    private void handleGenerateQuote() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("quote-view.fxml"));
            Parent root = loader.load();

            QuoteController quoteController = loader.getController();
            quoteController.setBuild(currentBuild);

            Stage quoteStage = new Stage();
            quoteStage.setTitle("MicroBuild PC - Generated Quote");
            quoteStage.setScene(new Scene(root));
            quoteStage.initModality(Modality.APPLICATION_MODAL);
            quoteStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static InventoryManager seedInventory() {
        InventoryManager inv = new InventoryManager();

        // CPU (id, name, price, socket, cores, tdp)
        inv.addComponent(new CPU("CPU1", "AMD Ryzen 5 5500", 5200, "AM4", 6, 65), 10);
        inv.addComponent(new CPU("CPU2", "AMD Ryzen 5 5600", 8200, "AM4", 6, 65), 12);
        inv.addComponent(new CPU("CPU3", "AMD Ryzen 7 5700X", 11500, "AM4", 8, 65), 8);
        inv.addComponent(new CPU("CPU4", "AMD Ryzen 7 5800X3D", 18500, "AM4", 8, 105), 4);
        inv.addComponent(new CPU("CPU5", "AMD Ryzen 9 5900X", 17500, "AM4", 12, 105), 5);
        inv.addComponent(new CPU("CPU6", "AMD Ryzen 5 7500F", 9800, "AM5", 6, 65), 9);
        inv.addComponent(new CPU("CPU7", "AMD Ryzen 5 7600", 12500, "AM5", 6, 65), 10);
        inv.addComponent(new CPU("CPU8", "AMD Ryzen 7 7700", 17500, "AM5", 8, 65), 7);
        inv.addComponent(new CPU("CPU9", "AMD Ryzen 7 7700X", 20500, "AM5", 8, 105), 6);
        inv.addComponent(new CPU("CPU10", "AMD Ryzen 7 7800X3D", 26500, "AM5", 8, 120), 4);
        inv.addComponent(new CPU("CPU11", "AMD Ryzen 9 7900X", 27500, "AM5", 12, 170), 3);
        inv.addComponent(new CPU("CPU12", "AMD Ryzen 9 7950X", 35000, "AM5", 16, 170), 3);
        inv.addComponent(new CPU("CPU13", "Intel Core i3-12100F", 4800, "LGA1700", 4, 58), 15);
        inv.addComponent(new CPU("CPU14", "Intel Core i5-12400F", 7500, "LGA1700", 6, 65), 12);
        inv.addComponent(new CPU("CPU15", "Intel Core i5-13400F", 10500, "LGA1700", 10, 65), 8);
        inv.addComponent(new CPU("CPU16", "Intel Core i5-13600K", 17500, "LGA1700", 14, 125), 6);
        inv.addComponent(new CPU("CPU17", "Intel Core i7-12700K", 16500, "LGA1700", 12, 125), 5);
        inv.addComponent(new CPU("CPU18", "Intel Core i7-13700K", 22500, "LGA1700", 16, 125), 4);
        inv.addComponent(new CPU("CPU19", "Intel Core i9-13900K", 33500, "LGA1700", 24, 125), 3);
        inv.addComponent(new CPU("CPU20", "Intel Core i9-14900K", 36500, "LGA1700", 24, 125), 2);

        // Motherboard
        inv.addComponent(new Motherboard("MB1", "ASRock A320M-HDV", 2800, "AM4", "DDR4", 64, "mATX"), 8);
        inv.addComponent(new Motherboard("MB2", "ASRock B450M Pro4", 4500, "AM4", "DDR4", 64, "mATX"), 8);
        inv.addComponent(new Motherboard("MB3", "MSI B550M Pro-VDH", 5800, "AM4", "DDR4", 128, "mATX"), 7);
        inv.addComponent(new Motherboard("MB4", "Gigabyte B550 Aorus Elite", 8200, "AM4", "DDR4", 128, "ATX"), 5);
        inv.addComponent(new Motherboard("MB5", "ASUS ROG Strix X570-E", 14500, "AM4", "DDR4", 128, "ATX"), 3);
        inv.addComponent(new Motherboard("MB6", "ASRock A620M-HDV", 5200, "AM5", "DDR5", 96, "mATX"), 8);
        inv.addComponent(new Motherboard("MB7", "MSI B650M PRO", 7500, "AM5", "DDR5", 128, "mATX"), 6);
        inv.addComponent(new Motherboard("MB8", "Gigabyte B650M Gaming X", 8800, "AM5", "DDR5", 128, "mATX"), 6);
        inv.addComponent(new Motherboard("MB9", "Gigabyte B650 Aorus Elite", 11500, "AM5", "DDR5", 192, "ATX"), 4);
        inv.addComponent(new Motherboard("MB10", "ASUS TUF X670E-Plus", 17500, "AM5", "DDR5", 192, "ATX"), 3);
        inv.addComponent(new Motherboard("MB11", "MSI MAG X670E Tomahawk", 19500, "AM5", "DDR5", 192, "ATX"), 3);
        inv.addComponent(new Motherboard("MB12", "Gigabyte B650I Aorus Ultra", 13500, "AM5", "DDR5", 96, "ITX"), 2);
        inv.addComponent(new Motherboard("MB13", "ASUS Prime H610M-E", 3800, "LGA1700", "DDR4", 64, "mATX"), 10);
        inv.addComponent(new Motherboard("MB14", "MSI PRO B660M-A", 5500, "LGA1700", "DDR4", 128, "mATX"), 7);
        inv.addComponent(new Motherboard("MB15", "ASUS Prime B760M", 6800, "LGA1700", "DDR4", 128, "mATX"), 6);
        inv.addComponent(new Motherboard("MB16", "Gigabyte B760 Gaming X", 8500, "LGA1700", "DDR4", 128, "ATX"), 5);
        inv.addComponent(new Motherboard("MB17", "MSI PRO B760M-E", 7200, "LGA1700", "DDR5", 192, "mATX"), 6);
        inv.addComponent(new Motherboard("MB18", "ASUS TUF B760-Plus", 11800, "LGA1700", "DDR5", 192, "ATX"), 4);
        inv.addComponent(new Motherboard("MB19", "MSI Z790 Tomahawk", 14500, "LGA1700", "DDR5", 192, "ATX"), 3);
        inv.addComponent(new Motherboard("MB20", "ASUS ROG Strix Z790-I", 20500, "LGA1700", "DDR5", 192, "ITX"), 2);

        // RAM
        inv.addComponent(new RAM("RAM1", "Crucial 8GB DDR4 2666", 1100, "DDR4", 8, 2666), 20);
        inv.addComponent(new RAM("RAM2", "Kingston Fury 8GB DDR4 3200", 1300, "DDR4", 8, 3200), 20);
        inv.addComponent(new RAM("RAM3", "Corsair Vengeance 16GB DDR4 3200", 2400, "DDR4", 16, 3200), 15);
        inv.addComponent(new RAM("RAM4", "TeamGroup T-Force 16GB DDR4 3200", 2300, "DDR4", 16, 3200), 15);
        inv.addComponent(new RAM("RAM5", "G.Skill Ripjaws 16GB DDR4 3600", 2700, "DDR4", 16, 3600), 12);
        inv.addComponent(new RAM("RAM6", "Kingston Fury 16GB DDR4 3600", 2900, "DDR4", 16, 3600), 12);
        inv.addComponent(new RAM("RAM7", "Corsair Vengeance 32GB DDR4 3200", 4000, "DDR4", 32, 3200), 10);
        inv.addComponent(new RAM("RAM8", "G.Skill Ripjaws 32GB DDR4 3600", 4300, "DDR4", 32, 3600), 10);
        inv.addComponent(new RAM("RAM9", "Corsair Vengeance RGB 32GB DDR4 3600", 4900, "DDR4", 32, 3600), 8);
        inv.addComponent(new RAM("RAM10", "Kingston Fury 64GB DDR4 3200", 8200, "DDR4", 64, 3200), 4);
        inv.addComponent(new RAM("RAM11", "Kingston Fury 8GB DDR5 4800", 1900, "DDR5", 8, 4800), 15);
        inv.addComponent(new RAM("RAM12", "Crucial 16GB DDR5 4800", 2800, "DDR5", 16, 4800), 15);
        inv.addComponent(new RAM("RAM13", "Kingston Fury 16GB DDR5 6000", 3200, "DDR5", 16, 6000), 15);
        inv.addComponent(new RAM("RAM14", "TeamGroup T-Force Delta 16GB DDR5 6000", 3500, "DDR5", 16, 6000), 10);
        inv.addComponent(new RAM("RAM15", "G.Skill Trident Z5 16GB DDR5 6400", 3900, "DDR5", 16, 6400), 8);
        inv.addComponent(new RAM("RAM16", "Corsair Vengeance 32GB DDR5 5600", 5500, "DDR5", 32, 5600), 10);
        inv.addComponent(new RAM("RAM17", "Corsair Vengeance 32GB DDR5 6000", 6200, "DDR5", 32, 6000), 10);
        inv.addComponent(new RAM("RAM18", "G.Skill Trident Z5 RGB 32GB DDR5 6400", 7500, "DDR5", 32, 6400), 6);
        inv.addComponent(new RAM("RAM19", "Kingston Fury 64GB DDR5 5600", 11500, "DDR5", 64, 5600), 3);
        inv.addComponent(new RAM("RAM20", "G.Skill Trident Z5 64GB DDR5 6000", 13500, "DDR5", 64, 6000), 3);
        inv.addComponent(new CPU("CPU21", "AMD Ryzen 3 4100", 3500, "AM4", 4, 65), 12);
        inv.addComponent(new CPU("CPU22", "AMD Ryzen 5 4500", 4300, "AM4", 6, 65), 12);
        inv.addComponent(new CPU("CPU23", "AMD Ryzen 5 3600", 5800, "AM4", 6, 65), 10);
        inv.addComponent(new CPU("CPU24", "AMD Ryzen 5 5600G", 8500, "AM4", 6, 65), 8);
        inv.addComponent(new CPU("CPU25", "AMD Ryzen 5 5600GT", 8800, "AM4", 6, 65), 8);
        inv.addComponent(new CPU("CPU26", "AMD Ryzen 5 5600X", 9500, "AM4", 6, 65), 8);
        inv.addComponent(new CPU("CPU27", "AMD Ryzen 7 3700X", 9800, "AM4", 8, 65), 6);
        inv.addComponent(new CPU("CPU28", "AMD Ryzen 7 5700G", 11200, "AM4", 8, 65), 6);
        inv.addComponent(new CPU("CPU29", "AMD Ryzen 7 5800X", 12500, "AM4", 8, 105), 5);
        inv.addComponent(new CPU("CPU30", "AMD Ryzen 7 5700X3D", 15500, "AM4", 8, 105), 4);
        inv.addComponent(new CPU("CPU31", "AMD Ryzen 9 5950X", 24500, "AM4", 16, 105), 3);

        // GPU (id, name, price, powerWatts, lengthMM, recommendedPsuW)
        inv.addComponent(new GPU("GPU1", "NVIDIA GT 1030", 3800, 30, 150, 300), 10);
        inv.addComponent(new GPU("GPU2", "NVIDIA GTX 1650", 9500, 75, 200, 350), 8);
        inv.addComponent(new GPU("GPU3", "NVIDIA GTX 1660 Super", 11500, 125, 230, 450), 7);
        inv.addComponent(new GPU("GPU4", "AMD RX 6500 XT", 8500, 107, 190, 400), 8);
        inv.addComponent(new GPU("GPU5", "AMD RX 6600", 14500, 132, 190, 450), 7);
        inv.addComponent(new GPU("GPU6", "AMD RX 7600", 19500, 165, 280, 550), 6);
        inv.addComponent(new GPU("GPU7", "NVIDIA RTX 3050", 12500, 130, 240, 550), 7);
        inv.addComponent(new GPU("GPU8", "NVIDIA RTX 3060", 17500, 170, 242, 550), 6);
        inv.addComponent(new GPU("GPU9", "NVIDIA RTX 4060", 22000, 115, 240, 450), 6);
        inv.addComponent(new GPU("GPU10", "NVIDIA RTX 4060 Ti", 28500, 160, 250, 550), 5);
        inv.addComponent(new GPU("GPU11", "NVIDIA RTX 4070", 38000, 200, 244, 650), 5);
        inv.addComponent(new GPU("GPU12", "NVIDIA RTX 4070 Super", 42000, 220, 310, 650), 4);
        inv.addComponent(new GPU("GPU13", "NVIDIA RTX 4070 Ti Super", 58000, 285, 310, 700), 3);
        inv.addComponent(new GPU("GPU14", "NVIDIA RTX 4080 Super", 78000, 320, 310, 750), 2);
        inv.addComponent(new GPU("GPU15", "NVIDIA RTX 4090", 125000, 450, 336, 850), 1);
        inv.addComponent(new GPU("GPU16", "AMD RX 7600 XT", 23500, 190, 280, 600), 5);
        inv.addComponent(new GPU("GPU17", "AMD RX 7700 XT", 31000, 245, 280, 700), 4);
        inv.addComponent(new GPU("GPU18", "AMD RX 7800 XT", 36000, 263, 320, 700), 4);
        inv.addComponent(new GPU("GPU19", "AMD RX 7900 GRE", 42500, 260, 287, 700), 3);
        inv.addComponent(new GPU("GPU20", "AMD RX 7900 XTX", 70000, 355, 287, 800), 2);

        // PSU (id, name, price, wattage, formFactor)
        inv.addComponent(new PSU("PSU1", "Generic 350W", 1500, 350, "ATX"), 15);
        inv.addComponent(new PSU("PSU2", "Seasonic 450W Bronze", 2800, 450, "ATX"), 10);
        inv.addComponent(new PSU("PSU3", "Corsair CV450 450W Bronze", 2900, 450, "ATX"), 10);
        inv.addComponent(new PSU("PSU4", "Cooler Master 550W Bronze", 3500, 550, "ATX"), 10);
        inv.addComponent(new PSU("PSU5", "Corsair CX550 550W Bronze", 3800, 550, "ATX"), 10);
        inv.addComponent(new PSU("PSU6", "Seasonic S12III 600W Bronze", 4200, 600, "ATX"), 8);
        inv.addComponent(new PSU("PSU7", "Corsair CX650 650W Bronze", 4500, 650, "ATX"), 8);
        inv.addComponent(new PSU("PSU8", "Seasonic 650W Gold", 5200, 650, "ATX"), 10);
        inv.addComponent(new PSU("PSU9", "MSI MAG A650GL 650W Gold", 5500, 650, "ATX"), 8);
        inv.addComponent(new PSU("PSU10", "Cooler Master MWE 750W Gold", 6500, 750, "ATX"), 8);
        inv.addComponent(new PSU("PSU11", "Corsair RM750e 750W Gold", 7500, 750, "ATX"), 6);
        inv.addComponent(new PSU("PSU12", "Seasonic Focus GX-750 750W Gold", 8200, 750, "ATX"), 6);
        inv.addComponent(new PSU("PSU13", "Corsair RM850e 850W Gold", 9200, 850, "ATX"), 5);
        inv.addComponent(new PSU("PSU14", "MSI MPG A850G 850W Gold", 9800, 850, "ATX"), 5);
        inv.addComponent(new PSU("PSU15", "Seasonic Focus GX-850 850W Gold", 10500, 850, "ATX"), 5);
        inv.addComponent(new PSU("PSU16", "Corsair RM1000e 1000W Gold", 12500, 1000, "ATX"), 4);
        inv.addComponent(new PSU("PSU17", "Seasonic Prime 1000W Gold", 15500, 1000, "ATX"), 3);
        inv.addComponent(new PSU("PSU18", "Corsair HX1000 1000W Platinum", 17500, 1000, "ATX"), 3);
        inv.addComponent(new PSU("PSU19", "Corsair RM1200x 1200W Gold", 20500, 1200, "ATX"), 2);
        inv.addComponent(new PSU("PSU20", "Seasonic Prime TX-1300 Titanium", 28500, 1300, "ATX"), 1);

        // Storage (id, name, price, type, capacityGB, interface)
        inv.addComponent(new Storage("SSD1", "Kingston NV2 250GB NVMe", 1200, "SSD", 250, "NVMe"), 20);
        inv.addComponent(new Storage("SSD2", "Kingston NV2 500GB NVMe", 1800, "SSD", 500, "NVMe"), 20);
        inv.addComponent(new Storage("SSD3", "WD Blue SN580 500GB NVMe", 2200, "SSD", 500, "NVMe"), 15);
        inv.addComponent(new Storage("SSD4", "Samsung 980 500GB NVMe", 2800, "SSD", 500, "NVMe"), 15);
        inv.addComponent(new Storage("SSD5", "Kingston NV2 1TB NVMe", 3200, "SSD", 1000, "NVMe"), 15);
        inv.addComponent(new Storage("SSD6", "Samsung 980 1TB NVMe", 4200, "SSD", 1000, "NVMe"), 20);
        inv.addComponent(new Storage("SSD7", "WD Black SN770 1TB NVMe", 4800, "SSD", 1000, "NVMe"), 10);
        inv.addComponent(new Storage("SSD8", "Crucial P3 Plus 2TB NVMe", 6800, "SSD", 2000, "NVMe"), 8);
        inv.addComponent(new Storage("SSD9", "Samsung 990 Pro 1TB NVMe", 6500, "SSD", 1000, "NVMe"), 8);
        inv.addComponent(new Storage("SSD10", "Samsung 990 Pro 2TB NVMe", 11500, "SSD", 2000, "NVMe"), 5);
        inv.addComponent(new Storage("SSD11", "Crucial MX500 500GB SATA", 2500, "SSD", 500, "SATA"), 12);
        inv.addComponent(new Storage("SSD12", "Crucial MX500 1TB SATA", 3900, "SSD", 1000, "SATA"), 12);
        inv.addComponent(new Storage("SSD13", "Samsung 870 EVO 1TB SATA", 4600, "SSD", 1000, "SATA"), 10);
        inv.addComponent(new Storage("SSD14", "Samsung 870 EVO 2TB SATA", 8200, "SSD", 2000, "SATA"), 6);
        inv.addComponent(new Storage("SSD15", "Kingston A400 480GB SATA", 1700, "SSD", 480, "SATA"), 20);
        inv.addComponent(new Storage("HDD1", "WD Blue 1TB", 2100, "HDD", 1000, "SATA"), 12);
        inv.addComponent(new Storage("HDD2", "Seagate Barracuda 2TB", 2600, "HDD", 2000, "SATA"), 12);
        inv.addComponent(new Storage("HDD3", "WD Blue 2TB", 2900, "HDD", 2000, "SATA"), 10);
        inv.addComponent(new Storage("HDD4", "Seagate Barracuda 4TB", 4800, "HDD", 4000, "SATA"), 8);
        inv.addComponent(new Storage("HDD5", "WD Red Plus 4TB", 5900, "HDD", 4000, "SATA"), 6);

        // Case (id, name, price, supportedFormFactors, maxGpuLengthMM)
        inv.addComponent(new CaseComponent("CASE1", "Cooler Master Q300L", 2900, "mATX,ITX", 360), 7);
        inv.addComponent(new CaseComponent("CASE2", "NZXT H510", 4800, "ATX,mATX,ITX", 381), 7);
        inv.addComponent(new CaseComponent("CASE3", "Tecware Forge M2", 2600, "mATX,ITX", 320), 9);
        inv.addComponent(new CaseComponent("CASE4", "Lian Li Lancool 215", 5500, "ATX,mATX,ITX", 370), 5);
        inv.addComponent(new CaseComponent("CASE5", "Fractal Pop Mini Air", 5200, "mATX,ITX", 405), 4);
        inv.addComponent(new CaseComponent("CASE6", "Deepcool Matrexx 30", 1800, "mATX,ITX", 320), 10);
        inv.addComponent(new CaseComponent("CASE7", "Zalman T3 Plus", 1900, "ATX,mATX,ITX", 320), 10);
        inv.addComponent(new CaseComponent("CASE8", "Tecware Nexus Air", 3200, "ATX,mATX,ITX", 340), 8);
        inv.addComponent(new CaseComponent("CASE9", "Cooler Master NR200", 4800, "ITX", 330), 4);
        inv.addComponent(new CaseComponent("CASE10", "Cooler Master MasterBox TD500 Mesh", 5200, "ATX,mATX,ITX", 410), 6);
        inv.addComponent(new CaseComponent("CASE11", "Corsair 4000D Airflow", 5500, "ATX,mATX,ITX", 360), 6);
        inv.addComponent(new CaseComponent("CASE12", "Corsair 5000D Airflow", 8500, "ATX,mATX,ITX", 420), 4);
        inv.addComponent(new CaseComponent("CASE13", "NZXT H5 Flow", 5000, "ATX,mATX,ITX", 365), 6);
        inv.addComponent(new CaseComponent("CASE14", "NZXT H7 Flow", 7500, "ATX,mATX,ITX", 400), 5);
        inv.addComponent(new CaseComponent("CASE15", "Lian Li Lancool 216", 6500, "ATX,mATX,ITX", 392), 5);
        inv.addComponent(new CaseComponent("CASE16", "Fractal Meshify 2 Compact", 9500, "ATX,mATX,ITX", 341), 3);
        inv.addComponent(new CaseComponent("CASE17", "Phanteks Eclipse P400A", 4800, "ATX,mATX,ITX", 420), 6);
        inv.addComponent(new CaseComponent("CASE18", "Montech Air 903 Max", 3800, "ATX,mATX,ITX", 400), 8);
        inv.addComponent(new CaseComponent("CASE19", "Lian Li O11 Dynamic EVO", 11500, "ATX,mATX,ITX", 426), 3);
        inv.addComponent(new CaseComponent("CASE20", "Fractal North", 10500, "ATX,mATX,ITX", 355), 3);

        return inv;
    }
}
package com.ufit.fx;

import com.ufit.logic.HealthCalculator;
import com.ufit.model.ufit;
import com.ufit.service.ufitService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class FxController implements Initializable {

    private final ufitService service;

    // ═══════════════ BMI TAB ═══════════════
    @FXML private TextField bmiWeightField;
    @FXML private TextField bmiHeightField;
    @FXML private Label bmiResultLabel;
    @FXML private Label bmiCategoryLabel;
    @FXML private StackPane bmiChartPane;
    @FXML private VBox bmiDetailsBox;

    // ═══════════════ BMR TAB ═══════════════
    @FXML private TextField bmrWeightField;
    @FXML private TextField bmrHeightField;
    @FXML private TextField bmrAgeField;
    @FXML private ComboBox<String> bmrGenderBox;
    @FXML private Label bmrMifflinLabel;
    @FXML private Label bmrHarrisLabel;
    @FXML private StackPane bmrChartPane;
    @FXML private VBox bmrDetailsBox;

    // ═══════════════ BODY FAT TAB ═══════════════
    @FXML private TextField bfWeightField;
    @FXML private TextField bfWaistField;
    @FXML private TextField bfNeckField;
    @FXML private TextField bfHeightField;
    @FXML private ComboBox<String> bfGenderBox;
    @FXML private Label bfResultLabel;
    @FXML private Label bfCategoryLabel;
    @FXML private StackPane bfChartPane;
    @FXML private VBox bfDetailsBox;

    // ═══════════════ TDEE TAB ═══════════════
    @FXML private TextField tdeeWeightField;
    @FXML private TextField tdeeHeightField;
    @FXML private TextField tdeeAgeField;
    @FXML private ComboBox<String> tdeeGenderBox;
    @FXML private ComboBox<String> tdeeActivityBox;
    @FXML private Label tdeeResultLabel;
    @FXML private Label tdeeBmrLabel;
    @FXML private StackPane tdeeChartPane;
    @FXML private VBox tdeeDetailsBox;

    // ═══════════════ IDEAL WEIGHT TAB ═══════════════
    @FXML private TextField iwHeightField;
    @FXML private ComboBox<String> iwGenderBox;
    @FXML private Label iwDevineLabel;
    @FXML private Label iwRobinsonLabel;
    @FXML private Label iwMillerLabel;
    @FXML private Label iwHamwiLabel;
    @FXML private StackPane iwChartPane;

    // ═══════════════ HISTORY TAB ═══════════════
    @FXML private TextField fullWeightField;
    @FXML private TextField fullHeightField;
    @FXML private TextField fullAgeField;
    @FXML private ComboBox<String> fullGenderBox;
    @FXML private TextField fullWaistField;
    @FXML private TextField fullNeckField;
    @FXML private ComboBox<String> fullActivityBox;
    @FXML private TableView<ufit> historyTable;
    @FXML private TableColumn<ufit, LocalDateTime> dateColumn;
    @FXML private TableColumn<ufit, Double> hBmiColumn;
    @FXML private TableColumn<ufit, Double> hBmrColumn;
    @FXML private TableColumn<ufit, Double> hBodyFatColumn;
    @FXML private TableColumn<ufit, Double> hTdeeColumn;
    @FXML private TableColumn<ufit, Double> hIdealWeightColumn;
    @FXML private StackPane historyChartPane;

    public FxController(ufitService service) {
        this.service = service;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initComboBoxes();
        initHistoryTable();
        buildBmiGauge(-1);
        buildBodyFatGauge(-1, "Male");
        refreshHistory();
    }

    private void initComboBoxes() {
        var genders = FXCollections.observableArrayList("Male", "Female");
        var activities = FXCollections.observableArrayList(
                "Sedentary", "Light", "Moderate", "Active", "Extra Active");

        bmrGenderBox.setItems(genders); bmrGenderBox.setValue("Male");
        bfGenderBox.setItems(FXCollections.observableArrayList("Male", "Female")); bfGenderBox.setValue("Male");
        tdeeGenderBox.setItems(FXCollections.observableArrayList("Male", "Female")); tdeeGenderBox.setValue("Male");
        iwGenderBox.setItems(FXCollections.observableArrayList("Male", "Female")); iwGenderBox.setValue("Male");
        fullGenderBox.setItems(FXCollections.observableArrayList("Male", "Female")); fullGenderBox.setValue("Male");

        tdeeActivityBox.setItems(activities); tdeeActivityBox.setValue("Moderate");
        fullActivityBox.setItems(FXCollections.observableArrayList(
                "Sedentary", "Light", "Moderate", "Active", "Extra Active"));
        fullActivityBox.setValue("Moderate");
    }

    private void initHistoryTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("entryDate"));
        dateColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
            }
        });
        hBmiColumn.setCellValueFactory(new PropertyValueFactory<>("bmi"));
        hBmrColumn.setCellValueFactory(new PropertyValueFactory<>("bmr"));
        hBodyFatColumn.setCellValueFactory(new PropertyValueFactory<>("bodyFat"));
        hTdeeColumn.setCellValueFactory(new PropertyValueFactory<>("tdee"));
        hIdealWeightColumn.setCellValueFactory(new PropertyValueFactory<>("idealWeight"));
        formatDoubleColumn(hBmiColumn, "%.1f");
        formatDoubleColumn(hBmrColumn, "%.0f");
        formatDoubleColumn(hBodyFatColumn, "%.1f%%");
        formatDoubleColumn(hTdeeColumn, "%.0f");
        formatDoubleColumn(hIdealWeightColumn, "%.1f");
    }

    // ════════════════════════════════════════════
    //  REUSABLE GRADIENT GAUGE BUILDER
    // ════════════════════════════════════════════

    /**
     * Builds a calculator.net-style gradient gauge with pointer and labels.
     *
     * @param parent       the StackPane to render into
     * @param segments     array of {label, color, rangeEnd%}  — rangeEnd is 0..100 fraction of gauge
     * @param thresholds   the percentage labels to show (e.g. "2%", "6%", "14%")
     * @param thresholdPos positions 0..1 on the gauge for each threshold
     * @param value        the current value to mark (-1 = hide marker)
     * @param maxValue     the max value the gauge represents
     * @param valueText    text to show above pointer (e.g. "16.6%")
     */
    private void buildGradientGauge(StackPane parent,
                                     String[][] segments,
                                     String[] thresholds,
                                     double[] thresholdPos,
                                     double value, double maxValue,
                                     String valueText) {
        parent.getChildren().clear();

        VBox container = new VBox(0);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(8, 12, 8, 12));

        // === Pointer value label row ===
        HBox pointerRow = new HBox();
        pointerRow.setPrefHeight(28);
        pointerRow.setMinHeight(28);
        pointerRow.setMaxHeight(28);

        if (value >= 0) {
            AnchorPane pointerPane = new AnchorPane();
            pointerPane.setPrefHeight(28);

            // Value label above arrow
            Label valLabel = new Label(valueText);
            valLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
            pointerPane.getChildren().add(valLabel);

            // Black triangle pointer
            Polygon arrow = new Polygon(0, 14, 8, 0, -8, 0);
            arrow.setFill(Color.web("white"));
            pointerPane.getChildren().add(arrow);

            double fraction = Math.min(Math.max(value, 0), maxValue) / maxValue;

            pointerPane.layoutBoundsProperty().addListener((obs, o, n) -> {
                double w = n.getWidth();
                double xPos = w * fraction;
                valLabel.setLayoutX(xPos - 20);
                valLabel.setLayoutY(0);
                arrow.setLayoutX(xPos);
                arrow.setLayoutY(14);
            });

            pointerRow.getChildren().add(pointerPane);
            HBox.setHgrow(pointerPane, Priority.ALWAYS);
        }

        // === Gradient bar ===
        StackPane gaugeBar = new StackPane();
        gaugeBar.setPrefHeight(32);
        gaugeBar.setMinHeight(32);
        gaugeBar.setMaxHeight(32);
        gaugeBar.setStyle("-fx-background-radius: 4;");

        // Build gradient from segments
        HBox colorStrip = new HBox(0);
        colorStrip.setPrefHeight(32);
        colorStrip.setStyle("-fx-background-radius: 4;");

        double prevEnd = 0;
        for (String[] seg : segments) {
            String color = seg[1];
            double endPct = Double.parseDouble(seg[2]) / 100.0;
            double width = endPct - prevEnd;
            Region r = new Region();
            r.setStyle("-fx-background-color: " + color + ";");
            r.setPrefHeight(32);
            r.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(r, Priority.ALWAYS);
            // Use a binding trick: set min/max width proportionally
            // We'll use percentage constraints via a listener
            final double w = width;
            colorStrip.widthProperty().addListener((obs, o, n) -> {
                double totalW = n.doubleValue();
                r.setPrefWidth(totalW * w);
                r.setMinWidth(totalW * w);
                r.setMaxWidth(totalW * w);
            });
            colorStrip.getChildren().add(r);
            prevEnd = endPct;
        }

        // Round corners on first and last segment
        if (!colorStrip.getChildren().isEmpty()) {
            colorStrip.getChildren().get(0).setStyle(
                    colorStrip.getChildren().get(0).getStyle() + " -fx-background-radius: 4 0 0 4;");
            int last = colorStrip.getChildren().size() - 1;
            colorStrip.getChildren().get(last).setStyle(
                    colorStrip.getChildren().get(last).getStyle() + " -fx-background-radius: 0 4 4 0;");
        }

        gaugeBar.getChildren().add(colorStrip);

        // === Threshold % labels on the bar ===
        AnchorPane thresholdOverlay = new AnchorPane();
        thresholdOverlay.setPickOnBounds(false);
        for (int i = 0; i < thresholds.length; i++) {
            Label tl = new Label(thresholds[i]);
            tl.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.9); -fx-font-weight: bold;");
            final double pos = thresholdPos[i];
            thresholdOverlay.getChildren().add(tl);
            thresholdOverlay.layoutBoundsProperty().addListener((obs, o, n) -> {
                double w = n.getWidth();
                tl.setLayoutX(w * pos - 10);
                tl.setLayoutY(8);
            });
        }
        gaugeBar.getChildren().add(thresholdOverlay);

        // === Category labels under bar ===
        AnchorPane categoryLabels = new AnchorPane();
        categoryLabels.setPrefHeight(20);
        categoryLabels.setMinHeight(20);
        for (String[] seg : segments) {
            String name = seg[0];
            double endPct = Double.parseDouble(seg[2]) / 100.0;
            Label cl = new Label(name);
            cl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
            categoryLabels.getChildren().add(cl);
            double startPct = endPct - (endPct - (segments.length > 0 ? 0 : 0));
        }
        // Position category labels at center of each segment
        prevEnd = 0;
        int idx = 0;
        for (String[] seg : segments) {
            double endPct = Double.parseDouble(seg[2]) / 100.0;
            double mid = (prevEnd + endPct) / 2.0;
            if (idx < categoryLabels.getChildren().size()) {
                final double m = mid;
                final int fi = idx;
                categoryLabels.layoutBoundsProperty().addListener((obs, o, n) -> {
                    double w = n.getWidth();
                    javafx.scene.Node node = categoryLabels.getChildren().get(fi);
                    if (node instanceof Label lbl) {
                        lbl.setLayoutX(w * m - 25);
                        lbl.setLayoutY(0);
                    }
                });
            }
            prevEnd = endPct;
            idx++;
        }

        container.getChildren().addAll(pointerRow, gaugeBar, categoryLabels);
        parent.getChildren().add(container);
    }

    /**
     * Builds a details table (like the one in the image) with alternating row colors.
     */
    private void buildDetailsTable(VBox detailsBox, String[][] rows) {
        detailsBox.getChildren().clear();
        detailsBox.setSpacing(0);

        for (int i = 0; i < rows.length; i++) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));
            row.setStyle(i % 2 == 0
                    ? "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: " + (i == 0 ? "6 6 0 0" : "0") + ";"
                    : "-fx-background-color: transparent; -fx-background-radius: " + (i == rows.length - 1 ? "0 0 6 6" : "0") + ";");

            Label key = new Label(rows[i][0]);
            key.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
            key.setMinWidth(200);
            key.setMaxWidth(200);

            Label val = new Label(rows[i][1]);
            val.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold;");

            row.getChildren().addAll(key, val);
            HBox.setHgrow(val, Priority.ALWAYS);
            detailsBox.getChildren().add(row);
        }
    }

    // ════════════════════════════════════════════
    //  BMI TAB
    // ════════════════════════════════════════════

    @FXML
    private void onCalculateBmi() {
        try {
            double weight = parseDouble(bmiWeightField, "Weight");
            double height = parseDouble(bmiHeightField, "Height");
            double bmi = HealthCalculator.calculateBMI(weight, height);
            String category = HealthCalculator.getBmiCategory(bmi);

            bmiResultLabel.setText(String.format("%.1f", bmi));
            bmiCategoryLabel.setText(category);
            bmiCategoryLabel.getStyleClass().removeAll("cat-green", "cat-yellow", "cat-orange", "cat-red");
            bmiCategoryLabel.getStyleClass().add(getBmiCatStyle(bmi));

            buildBmiGauge(bmi);

            // Healthy weight range for this height
            double normalLow = 18.5 * height * height;
            double normalHigh = 24.9 * height * height;

            buildDetailsTable(bmiDetailsBox, new String[][] {
                {"BMI", String.format("%.1f kg/m²", bmi)},
                {"BMI Category", category},
                {"Healthy Weight Range", String.format("%.1f – %.1f kg", normalLow, normalHigh)},
                {"Your Weight", String.format("%.1f kg", weight)},
                {"BMI Prime", String.format("%.2f", bmi / 25.0)},
            });
        } catch (ValidationException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void onClearBmi() {
        bmiWeightField.clear(); bmiHeightField.clear();
        bmiResultLabel.setText("—"); bmiCategoryLabel.setText("");
        bmiCategoryLabel.getStyleClass().removeAll("cat-green", "cat-yellow", "cat-orange", "cat-red");
        bmiDetailsBox.getChildren().clear();
        buildBmiGauge(-1);
    }

    private void buildBmiGauge(double bmi) {
        buildGradientGauge(bmiChartPane,
                new String[][] {
                        {"Underweight", "#3498db", "46"},    // 0–18.5 → 0–46%
                        {"Normal",      "#2ecc71", "62"},    // 18.5–25 → 46–62%
                        {"Overweight",  "#f39c12", "75"},    // 25–30 → 62–75%
                        {"Obese",       "#e74c3c", "100"},   // 30–40 → 75–100%
                },
                new String[] {"18.5", "25", "30"},
                new double[] {0.46, 0.62, 0.75},
                bmi, 40.0,
                bmi >= 0 ? String.format("%.1f", bmi) : ""
        );
    }

    private String getBmiCatStyle(double bmi) {
        if (bmi < 18.5) return "cat-yellow";
        if (bmi < 25.0) return "cat-green";
        if (bmi < 30.0) return "cat-orange";
        return "cat-red";
    }

    // ════════════════════════════════════════════
    //  BMR TAB
    // ════════════════════════════════════════════

    @FXML
    private void onCalculateBmr() {
        try {
            double weight = parseDouble(bmrWeightField, "Weight");
            double height = parseDouble(bmrHeightField, "Height");
            int age = parseInt(bmrAgeField, "Age");
            String gender = bmrGenderBox.getValue();

            double mifflin = HealthCalculator.calculateBMR(weight, height, age, gender);
            double harris = HealthCalculator.calculateHarrisBenedictBMR(weight, height, age, gender);

            bmrMifflinLabel.setText(String.format("%.0f", mifflin));
            bmrHarrisLabel.setText(String.format("%.0f", harris));

            buildBmrChart(mifflin, harris);

            buildDetailsTable(bmrDetailsBox, new String[][] {
                {"BMR (Mifflin-St Jeor)", String.format("%.0f cal/day", mifflin)},
                {"BMR (Harris-Benedict)", String.format("%.0f cal/day", harris)},
                {"Average BMR", String.format("%.0f cal/day", (mifflin + harris) / 2)},
                {"Daily Calories (Sedentary)", String.format("%.0f cal", mifflin * 1.2)},
                {"Daily Calories (Moderate)", String.format("%.0f cal", mifflin * 1.55)},
                {"Daily Calories (Active)", String.format("%.0f cal", mifflin * 1.725)},
            });
        } catch (ValidationException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void onClearBmr() {
        bmrWeightField.clear(); bmrHeightField.clear(); bmrAgeField.clear();
        bmrGenderBox.setValue("Male");
        bmrMifflinLabel.setText("—"); bmrHarrisLabel.setText("—");
        bmrChartPane.getChildren().clear();
        bmrDetailsBox.getChildren().clear();
    }

    @SuppressWarnings("unchecked")
    private void buildBmrChart(double mifflin, double harris) {
        bmrChartPane.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Calories / day");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(null);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(50);
        chart.setBarGap(5);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Mifflin-St Jeor", mifflin));
        series.getData().add(new XYChart.Data<>("Harris-Benedict", harris));
        chart.getData().add(series);
        chart.setMaxHeight(220);
        chart.getStyleClass().add("custom-bar-chart");

        chart.needsLayoutProperty().addListener((obs, o, n) -> {
            int i = 0;
            String[] colors = {"#8B5CF6", "#EC4899"};
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill: " + colors[i % colors.length] + "; -fx-background-radius: 4 4 0 0;");
                }
                i++;
            }
        });

        bmrChartPane.getChildren().add(chart);
    }

    // ════════════════════════════════════════════
    //  BODY FAT TAB
    // ════════════════════════════════════════════

    @FXML
    private void onCalculateBf() {
        try {
            double weight = parseDouble(bfWeightField, "Weight");
            double waist = parseDouble(bfWaistField, "Waist");
            double neck = parseDouble(bfNeckField, "Neck");
            double heightCm = parseDouble(bfHeightField, "Height");
            String gender = bfGenderBox.getValue();

            if (waist <= neck) {
                showAlert("Invalid Input", "Waist must be greater than neck measurement.");
                return;
            }

            double bodyFat = HealthCalculator.calculateBodyFat(waist, neck, heightCm, gender);
            String category = HealthCalculator.getBodyFatCategory(bodyFat, gender);

            bfResultLabel.setText(String.format("%.1f%%", bodyFat));
            bfCategoryLabel.setText(category);

            buildBodyFatGauge(bodyFat, gender);

            double fatMass = weight * bodyFat / 100.0;
            double leanMass = weight - fatMass;
            // BMI-based body fat (Deurenberg formula)
            double bmi = weight / Math.pow(heightCm / 100.0, 2);
            int ageFactor = 25; // default
            double bmiFat = gender.equalsIgnoreCase("male")
                    ? (1.20 * bmi) + (0.23 * ageFactor) - 16.2
                    : (1.20 * bmi) + (0.23 * ageFactor) - 5.4;

            buildDetailsTable(bfDetailsBox, new String[][] {
                {"Body Fat (U.S. Navy Method)", String.format("%.1f%%", bodyFat)},
                {"Body Fat Category", category},
                {"Body Fat Mass", String.format("%.1f kg", fatMass)},
                {"Lean Body Mass", String.format("%.1f kg", leanMass)},
                {"Body Fat (BMI method)", String.format("%.1f%%", bmiFat)},
            });
        } catch (ValidationException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void onClearBf() {
        bfWeightField.clear(); bfWaistField.clear(); bfNeckField.clear(); bfHeightField.clear();
        bfGenderBox.setValue("Male");
        bfResultLabel.setText("—"); bfCategoryLabel.setText("");
        bfDetailsBox.getChildren().clear();
        buildBodyFatGauge(-1, "Male");
    }

    private void buildBodyFatGauge(double bodyFat, String gender) {
        if (gender.equalsIgnoreCase("male")) {
            buildGradientGauge(bfChartPane,
                    new String[][] {
                            {"Essential",  "#8B0000", "6"},
                            {"Athletes",   "#2ecc71", "17"},
                            {"Fitness",    "#27ae60", "22"},
                            {"Average",    "#f1c40f", "31"},
                            {"Obese",      "#c0392b", "100"},
                    },
                    new String[] {"2%", "6%", "14%", "18%", "25%"},
                    new double[] {0.02, 0.06, 0.17, 0.22, 0.31},
                    bodyFat, 50.0,
                    bodyFat >= 0 ? String.format("%.1f%%", bodyFat) : ""
            );
        } else {
            buildGradientGauge(bfChartPane,
                    new String[][] {
                            {"Essential",  "#8B0000", "10"},
                            {"Athletes",   "#2ecc71", "20"},
                            {"Fitness",    "#27ae60", "30"},
                            {"Average",    "#f1c40f", "44"},
                            {"Obese",      "#c0392b", "100"},
                    },
                    new String[] {"10%", "14%", "21%", "25%", "32%"},
                    new double[] {0.10, 0.14, 0.21, 0.25, 0.32},
                    bodyFat, 50.0,
                    bodyFat >= 0 ? String.format("%.1f%%", bodyFat) : ""
            );
        }
    }

    // ════════════════════════════════════════════
    //  TDEE TAB
    // ════════════════════════════════════════════

    @FXML
    private void onCalculateTdee() {
        try {
            double weight = parseDouble(tdeeWeightField, "Weight");
            double height = parseDouble(tdeeHeightField, "Height");
            int age = parseInt(tdeeAgeField, "Age");
            String gender = tdeeGenderBox.getValue();
            String activity = tdeeActivityBox.getValue();

            double bmr = HealthCalculator.calculateBMR(weight, height, age, gender);
            double tdee = HealthCalculator.calculateTDEE(bmr, activity);

            tdeeResultLabel.setText(String.format("%.0f", tdee));
            tdeeBmrLabel.setText(String.format("%.0f", bmr));

            buildTdeeChart(bmr, activity);

            double[] macros = HealthCalculator.calculateMacros(tdee, "maintain");
            buildDetailsTable(tdeeDetailsBox, new String[][] {
                {"TDEE", String.format("%.0f cal/day", tdee)},
                {"BMR (base)", String.format("%.0f cal/day", bmr)},
                {"Activity Multiplier", String.format("×%.3f", HealthCalculator.getTDEEMultiplier(activity))},
                {"To Lose (−500 cal)", String.format("%.0f cal/day", tdee - 500)},
                {"To Gain (+500 cal)", String.format("%.0f cal/day", tdee + 500)},
                {"Protein (30%)", String.format("%.0fg", macros[0])},
                {"Carbs (45%)", String.format("%.0fg", macros[1])},
                {"Fat (25%)", String.format("%.0fg", macros[2])},
            });
        } catch (ValidationException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void onClearTdee() {
        tdeeWeightField.clear(); tdeeHeightField.clear(); tdeeAgeField.clear();
        tdeeGenderBox.setValue("Male"); tdeeActivityBox.setValue("Moderate");
        tdeeResultLabel.setText("—"); tdeeBmrLabel.setText("—");
        tdeeChartPane.getChildren().clear();
        tdeeDetailsBox.getChildren().clear();
    }

    @SuppressWarnings("unchecked")
    private void buildTdeeChart(double bmr, String selectedActivity) {
        tdeeChartPane.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Calories / day");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(null);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(10);

        String[] levels = {"Sedentary", "Light", "Moderate", "Active", "Extra Active"};
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String level : levels) {
            double tdee = HealthCalculator.calculateTDEE(bmr, level);
            series.getData().add(new XYChart.Data<>(level, tdee));
        }
        chart.getData().add(series);
        chart.setMaxHeight(250);
        chart.getStyleClass().add("custom-bar-chart");

        chart.needsLayoutProperty().addListener((obs, o, n) -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    boolean sel = d.getXValue().equalsIgnoreCase(selectedActivity);
                    d.getNode().setStyle("-fx-bar-fill: " + (sel ? "#8B5CF6" : "#334155") +
                            "; -fx-background-radius: 4 4 0 0;");
                }
            }
        });

        tdeeChartPane.getChildren().add(chart);
    }

    // ════════════════════════════════════════════
    //  IDEAL WEIGHT TAB
    // ════════════════════════════════════════════

    @FXML
    private void onCalculateIw() {
        try {
            double heightCm = parseDouble(iwHeightField, "Height");
            String gender = iwGenderBox.getValue();

            double devine = HealthCalculator.calculateIdealWeight(heightCm, gender);
            double robinson = HealthCalculator.calculateIdealWeightRobinson(heightCm, gender);
            double miller = HealthCalculator.calculateIdealWeightMiller(heightCm, gender);
            double hamwi = HealthCalculator.calculateIdealWeightHamwi(heightCm, gender);

            iwDevineLabel.setText(String.format("%.1f", devine));
            iwRobinsonLabel.setText(String.format("%.1f", robinson));
            iwMillerLabel.setText(String.format("%.1f", miller));
            iwHamwiLabel.setText(String.format("%.1f", hamwi));

            buildIdealWeightChart(devine, robinson, miller, hamwi);
        } catch (ValidationException e) {
            showAlert("Invalid Input", e.getMessage());
        }
    }

    @FXML
    private void onClearIw() {
        iwHeightField.clear(); iwGenderBox.setValue("Male");
        iwDevineLabel.setText("—"); iwRobinsonLabel.setText("—");
        iwMillerLabel.setText("—"); iwHamwiLabel.setText("—");
        iwChartPane.getChildren().clear();
    }

    @SuppressWarnings("unchecked")
    private void buildIdealWeightChart(double devine, double robinson, double miller, double hamwi) {
        iwChartPane.getChildren().clear();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Weight (kg)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(null);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(25);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Devine", devine));
        series.getData().add(new XYChart.Data<>("Robinson", robinson));
        series.getData().add(new XYChart.Data<>("Miller", miller));
        series.getData().add(new XYChart.Data<>("Hamwi", hamwi));
        chart.getData().add(series);
        chart.setMaxHeight(240);
        chart.getStyleClass().add("custom-bar-chart");

        String[] colors = {"#10B981", "#38BDF8", "#8B5CF6", "#F59E0B"};
        chart.needsLayoutProperty().addListener((obs, o, n) -> {
            int i = 0;
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-bar-fill: " + colors[i % colors.length] +
                            "; -fx-background-radius: 4 4 0 0;");
                }
                i++;
            }
        });

        iwChartPane.getChildren().add(chart);
    }

    // ════════════════════════════════════════════
    //  HISTORY TAB
    // ════════════════════════════════════════════

    @FXML
    private void onCalculateFull() {
        try {
            ufit report = new ufit();
            report.setWeight(parseDouble(fullWeightField, "Weight"));
            report.setHeight(parseDouble(fullHeightField, "Height"));
            report.setAge(parseInt(fullAgeField, "Age"));
            report.setGender(fullGenderBox.getValue());
            report.setWaist(parseDouble(fullWaistField, "Waist"));
            report.setNeck(parseDouble(fullNeckField, "Neck"));
            report.setActivityLevel(fullActivityBox.getValue());

            service.saveFullHealthReport(report);
            refreshHistory();
            showInfo("Saved!", "Your full health report has been saved.");
        } catch (ValidationException e) {
            showAlert("Invalid Input", e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Something went wrong: " + e.getMessage());
        }
    }

    @FXML
    private void onClearFull() {
        fullWeightField.clear(); fullHeightField.clear(); fullAgeField.clear();
        fullWaistField.clear(); fullNeckField.clear();
        fullGenderBox.setValue("Male"); fullActivityBox.setValue("Moderate");
    }

    private void refreshHistory() {
        historyTable.setItems(FXCollections.observableArrayList(service.repository.findAll()));
        buildHistoryChart();
    }

    @SuppressWarnings("unchecked")
    private void buildHistoryChart() {
        historyChartPane.getChildren().clear();

        List<ufit> records = service.repository.findAll();
        if (records.isEmpty()) {
            Label empty = new Label("No data yet. Save a full report to see trends.");
            empty.setStyle("-fx-text-fill: #64748B; -fx-font-style: italic; -fx-font-size: 13px;");
            historyChartPane.getChildren().add(empty);
            return;
        }

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Entry");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(null);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.getStyleClass().add("custom-line-chart");

        XYChart.Series<String, Number> bmiSeries = new XYChart.Series<>();
        bmiSeries.setName("BMI");
        XYChart.Series<String, Number> bfSeries = new XYChart.Series<>();
        bfSeries.setName("Body Fat %");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        for (ufit r : records) {
            String label = r.getEntryDate() != null ? fmt.format(r.getEntryDate()) : "?";
            bmiSeries.getData().add(new XYChart.Data<>(label, r.getBmi()));
            bfSeries.getData().add(new XYChart.Data<>(label, r.getBodyFat()));
        }

        chart.getData().addAll(bmiSeries, bfSeries);
        chart.setMaxHeight(280);
        historyChartPane.getChildren().add(chart);
    }

    // ════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════

    private double parseDouble(TextField field, String name) {
        String text = field.getText();
        if (text == null || text.isBlank()) throw new ValidationException(name + " is required.");
        try {
            double val = Double.parseDouble(text.trim());
            if (val <= 0) throw new ValidationException(name + " must be positive.");
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be a valid number.");
        }
    }

    private int parseInt(TextField field, String name) {
        String text = field.getText();
        if (text == null || text.isBlank()) throw new ValidationException(name + " is required.");
        try {
            int val = Integer.parseInt(text.trim());
            if (val <= 0) throw new ValidationException(name + " must be positive.");
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be a valid whole number.");
        }
    }

    private void formatDoubleColumn(TableColumn<ufit, Double> column, String format) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(format, item));
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message);
        a.showAndWait();
    }

    private static class ValidationException extends RuntimeException {
        ValidationException(String msg) { super(msg); }
    }
}

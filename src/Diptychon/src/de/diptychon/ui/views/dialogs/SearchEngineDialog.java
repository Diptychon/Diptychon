/*
 * This file is part of Diptychon.
 *
 * Diptychon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Diptychon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Diptychon. If not, see <http://www.gnu.org/licenses/>.
 */
package de.diptychon.ui.views.dialogs;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

public class SearchEngineDialog extends A_Dialog {

    @FXML
    private Slider threshMinSlider;
    @FXML
    private Slider threshMaxSlider;
    @FXML
    private Slider threshStepSlider;
    @FXML
    private Slider glyphSizeSlider;
    @FXML
    private Label threshMinLabel;
    @FXML
    private Label threshMaxLabel;
    @FXML
    private Label threshStepLabel;
    @FXML
    private Label glyphSizeLabel;

    @FXML
    private CheckBox checkBox_a;
    @FXML
    private CheckBox checkBox_b;
    @FXML
    private CheckBox checkBox_c;
    @FXML
    private CheckBox checkBox_d;
    @FXML
    private CheckBox checkBox_e;
    @FXML
    private CheckBox checkBox_f;
    @FXML
    private CheckBox checkBox_g;
    @FXML
    private CheckBox checkBox_h;
    @FXML
    private CheckBox checkBox_i;
    @FXML
    private CheckBox checkBox_j;
    @FXML
    private CheckBox checkBox_k;
    @FXML
    private CheckBox checkBox_l;
    @FXML
    private CheckBox checkBox_m;
    @FXML
    private CheckBox checkBox_n;
    @FXML
    private CheckBox checkBox_o;
    @FXML
    private CheckBox checkBox_p;
    @FXML
    private CheckBox checkBox_q;
    @FXML
    private CheckBox checkBox_r;
    @FXML
    private CheckBox checkBox_s;
    @FXML
    private CheckBox checkBox_t;
    @FXML
    private CheckBox checkBox_u;
    @FXML
    private CheckBox checkBox_v;
    @FXML
    private CheckBox checkBox_w;
    @FXML
    private CheckBox checkBox_x;
    @FXML
    private CheckBox checkBox_y;
    @FXML
    private CheckBox checkBox_z;
    @FXML
    private CheckBox checkBox_um;
    @FXML
    private CheckBox checkBox_et;

    @Override
    public void apply(final ActionEvent event) {
        _G_ccThreshMin = (float) threshMinSlider.getValue();
        _G_ccThreshMax = (float) threshMaxSlider.getValue();
        _G_ccThreshStep = (float) threshStepSlider.getValue();
        _G_glyphSize = (float) glyphSizeSlider.getValue();

        _G_a = checkBox_a.isSelected();
        _G_b = checkBox_b.isSelected();
        _G_c = checkBox_c.isSelected();
        _G_d = checkBox_d.isSelected();
        _G_e = checkBox_e.isSelected();
        _G_f = checkBox_f.isSelected();
        _G_g = checkBox_g.isSelected();
        _G_h = checkBox_h.isSelected();
        _G_i = checkBox_i.isSelected();
        _G_j = checkBox_j.isSelected();
        _G_k = checkBox_k.isSelected();
        _G_l = checkBox_l.isSelected();
        _G_m = checkBox_m.isSelected();
        _G_n = checkBox_n.isSelected();
        _G_o = checkBox_o.isSelected();
        _G_p = checkBox_p.isSelected();
        _G_q = checkBox_q.isSelected();
        _G_r = checkBox_r.isSelected();
        _G_s = checkBox_s.isSelected();
        _G_t = checkBox_t.isSelected();
        _G_u = checkBox_u.isSelected();
        _G_v = checkBox_v.isSelected();
        _G_w = checkBox_w.isSelected();
        _G_x = checkBox_x.isSelected();
        _G_y = checkBox_y.isSelected();
        _G_z = checkBox_z.isSelected();
        _G_um = checkBox_um.isSelected();
        _G_et = checkBox_et.isSelected();
        this.closeDialog();
    }

    @Override
    public void cancel(final ActionEvent event) {
        this.closeDialog();
    }

    private void deselectAll() {
        checkBox_a.setSelected(false);
        checkBox_b.setSelected(false);
        checkBox_c.setSelected(false);
        checkBox_d.setSelected(false);
        checkBox_e.setSelected(false);
        checkBox_f.setSelected(false);
        checkBox_g.setSelected(false);
        checkBox_h.setSelected(false);
        checkBox_i.setSelected(false);
        checkBox_j.setSelected(false);
        checkBox_k.setSelected(false);
        checkBox_l.setSelected(false);
        checkBox_m.setSelected(false);
        checkBox_n.setSelected(false);
        checkBox_o.setSelected(false);
        checkBox_p.setSelected(false);
        checkBox_q.setSelected(false);
        checkBox_r.setSelected(false);
        checkBox_s.setSelected(false);
        checkBox_t.setSelected(false);
        checkBox_u.setSelected(false);
        checkBox_v.setSelected(false);
        checkBox_w.setSelected(false);
        checkBox_x.setSelected(false);
        checkBox_y.setSelected(false);
        checkBox_z.setSelected(false);
        checkBox_um.setSelected(false);
        checkBox_et.setSelected(false);
    }

    @FXML
    private void setDefault(final ActionEvent event) {
        // thresholdSlider.setValue(DEFAULT_CC_THRESHOLD);
        if (checkBox_a.isSelected())
            deselectAll();
        else
            selectAll();
    }

    private void selectAll() {
        checkBox_a.setSelected(true);
        checkBox_b.setSelected(true);
        checkBox_c.setSelected(true);
        checkBox_d.setSelected(true);
        checkBox_e.setSelected(true);
        checkBox_f.setSelected(true);
        checkBox_g.setSelected(true);
        checkBox_h.setSelected(true);
        checkBox_i.setSelected(true);
        checkBox_j.setSelected(true);
        checkBox_k.setSelected(true);
        checkBox_l.setSelected(true);
        checkBox_m.setSelected(true);
        checkBox_n.setSelected(true);
        checkBox_o.setSelected(true);
        checkBox_p.setSelected(true);
        checkBox_q.setSelected(true);
        checkBox_r.setSelected(true);
        checkBox_s.setSelected(true);
        checkBox_t.setSelected(true);
        checkBox_u.setSelected(true);
        checkBox_v.setSelected(true);
        checkBox_w.setSelected(true);
        checkBox_x.setSelected(true);
        checkBox_y.setSelected(true);
        checkBox_z.setSelected(true);
        checkBox_um.setSelected(true);
        checkBox_et.setSelected(true);
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'SearchEnginePreferenceDialog.fxml'.";

        checkBox_a.setSelected(_G_a);
        checkBox_b.setSelected(_G_b);
        checkBox_c.setSelected(_G_c);
        checkBox_d.setSelected(_G_d);
        checkBox_e.setSelected(_G_e);
        checkBox_f.setSelected(_G_f);
        checkBox_g.setSelected(_G_g);
        checkBox_h.setSelected(_G_h);
        checkBox_i.setSelected(_G_i);
        checkBox_j.setSelected(_G_j);
        checkBox_k.setSelected(_G_k);
        checkBox_l.setSelected(_G_l);
        checkBox_m.setSelected(_G_m);
        checkBox_n.setSelected(_G_n);
        checkBox_o.setSelected(_G_o);
        checkBox_p.setSelected(_G_p);
        checkBox_q.setSelected(_G_q);
        checkBox_r.setSelected(_G_r);
        checkBox_s.setSelected(_G_s);
        checkBox_t.setSelected(_G_t);
        checkBox_u.setSelected(_G_u);
        checkBox_v.setSelected(_G_v);
        checkBox_w.setSelected(_G_w);
        checkBox_x.setSelected(_G_x);
        checkBox_y.setSelected(_G_y);
        checkBox_z.setSelected(_G_z);
        checkBox_um.setSelected(_G_um);
        checkBox_et.setSelected(_G_et);

        FloatProperty f1 = new SimpleFloatProperty();
        f1.set((float) _G_ccThreshMin);
        FloatProperty f2 = new SimpleFloatProperty();
        f2.set((float) _G_ccThreshMax);
        FloatProperty f3 = new SimpleFloatProperty();
        f3.set((float) _G_ccThreshStep);
        FloatProperty f4 = new SimpleFloatProperty();
        f4.set((float) _G_glyphSize);

        initBoxes(f1, f2, f3, f4);
    }

    public void initBoxes(FloatProperty threshMinProperty,
            FloatProperty threshMaxProperty, FloatProperty threshStepProperty,
            FloatProperty glyphSizeProperty) {
        NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
        n.setMaximumFractionDigits(2);
        n.setMinimumFractionDigits(2);
        threshMinLabel.setText(n.format(threshMinProperty.get()) + "");
        threshMaxLabel.setText(n.format(threshMaxProperty.get()) + "");
        threshStepLabel.setText(n.format(threshStepProperty.get()) + "");
        glyphSizeLabel.setText(n.format(glyphSizeProperty.get()) + "");
        threshMinSlider.setValue(threshMinProperty.get());
        threshMaxSlider.setValue(threshMaxProperty.get());
        threshStepSlider.setValue(threshStepProperty.get());
        glyphSizeSlider.setValue(glyphSizeProperty.get());

        threshMinSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
                n.setMaximumFractionDigits(2);
                n.setMinimumFractionDigits(2);
                threshMinLabel.setText(n.format(threshMinSlider.getValue())
                        + "");
            }
        });
        threshMaxSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
                n.setMaximumFractionDigits(2);
                n.setMinimumFractionDigits(2);
                threshMaxLabel.setText(n.format(threshMaxSlider.getValue())
                        + "");
            }
        });
        threshStepSlider.valueProperty().addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        NumberFormat n = NumberFormat
                                .getInstance(Locale.ENGLISH);
                        n.setMaximumFractionDigits(2);
                        n.setMinimumFractionDigits(2);
                        threshStepLabel.setText(n.format(threshStepSlider
                                .getValue()) + "");
                    }
                });
        glyphSizeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
                n.setMaximumFractionDigits(2);
                n.setMinimumFractionDigits(2);
                glyphSizeLabel.setText(n.format(glyphSizeSlider.getValue())
                        + "");
            }
        });
    }

    public static float _G_ccThreshMin = (float) 0.4;
    public static float _G_ccThreshMax = (float) 0.95;
    public static float _G_ccThreshStep = (float) 0.05;
    public static float _G_glyphSize = (float) 33.33;

    public static boolean _G_a = true;
    public static boolean _G_b = true;
    public static boolean _G_c = true;
    public static boolean _G_d = true;
    public static boolean _G_e = true;
    public static boolean _G_f = true;
    public static boolean _G_g = true;
    public static boolean _G_h = true;
    public static boolean _G_i = true;
    public static boolean _G_j = true;
    public static boolean _G_k = true;
    public static boolean _G_l = true;
    public static boolean _G_m = true;
    public static boolean _G_n = true;
    public static boolean _G_o = true;
    public static boolean _G_p = true;
    public static boolean _G_q = true;
    public static boolean _G_r = true;
    public static boolean _G_s = true;
    public static boolean _G_t = true;
    public static boolean _G_u = true;
    public static boolean _G_v = true;
    public static boolean _G_w = true;
    public static boolean _G_x = true;
    public static boolean _G_y = true;
    public static boolean _G_z = true;
    public static boolean _G_um = true;
    public static boolean _G_et = true;
}
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
package de.diptychon.ui.views.panels;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.stage.Window;
import de.diptychon.DiptychonLogger;

/**
 * This class is popupmenu class which inherits from contextmenu.
 */
public class PopupMenu extends ContextMenu {
    /**
     * arraylist of menuitem
     */
    private final ArrayList<MenuItem> items;

    /**
     * property of hidemenu in the form of arraylist
     */
    private final ArrayList<SimpleBooleanProperty> hideMenu;

    /**
     * constructor of popup menu
     */
    public PopupMenu() {
        this.items = new ArrayList<>();
        this.hideMenu = new ArrayList<>();
    }

    /**
     * This method is used add all the items and related events.
     * 
     * @param names
     *            names of menu items
     * @param events
     *            action events
     */
    public void addAll(final String[] names,
            final EventHandler<ActionEvent>[] events) {
        int i = 0;
        for (final String name : names) {
            this.addItem(name, events[i]);
            ++i;
        }
    }

    /**
     * This method is used to add a menu item and its event.
     * 
     * @param name
     *            name of a menu item
     * @param event
     *            action events
     */
    private void addItem(final String name,
            final EventHandler<ActionEvent> event) {
        this.items.add(new MenuItem(name, event));
    }

    /**
     * This method is used to show menu.
     * 
     * @param window
     *            window
     * @param screenX
     *            x coordinate in screen
     * @param screenY
     *            y coordinate in screen
     */
    public void showMenu(final Window window, final double screenX,
            final double screenY) {
        int offsetX = -60;
        int offsetY = 0;

        int i = 0;

        for (final MenuItem mi : this.items) {
            if (mi.getWidth() == -1 || mi.getHeight() == -1) {
                mi.show(window);
            }
            final double x = screenX - mi.getWidth() / 2 + offsetX;
            final double y = screenY - mi.getHeight() / 2 + offsetY;
            mi.show(window, x, y);

            if (i == 3) {
                offsetX *= -2;
                offsetY *= -2;
                offsetX = offsetY;
            }

            if (i % 2 == 0) {
                // place item on the opposite side
                offsetX *= -1;
                offsetY *= -1;
            } else {
                // place item below/above other item
                offsetY = offsetX;
                offsetX = 0;
            }
            i++;
        }
    }

    /**
     * This method is used to get hide listener.
     * 
     * @return hide listener
     */
    private ChangeListener<Boolean> getHideListener() {
        return new ChangeListener<Boolean>() {
            @Override
            public void changed(final ObservableValue<? extends Boolean> ov,
                    final Boolean old_val, final Boolean new_val) {
                if (!new_val) {
                    for (final MenuItem mi : PopupMenu.this.items) {
                        mi.hide();
                    }
                }
            }
        };
    }

    /**
     * This class is about menu item which inherits from context menu.
     */
    private class MenuItem extends ContextMenu {
        /**
         * customized menu item
         */
        private final CustomMenuItem item;

        /**
         * Constructor of menu item
         * 
         * @param name
         *            name of menu item
         * @param e
         *            event handler
         */
        MenuItem(final String name, final EventHandler<ActionEvent> e) {
            this.setAutoHide(true);
            this.setStyle("-fx-background-color: transparent");

            final Button btn = new Button("", this.getButtonGraphic(name));
            btn.setStyle("-fx-background-color: transparent");
            this.item = new CustomMenuItem(btn);
            this.item.setOnAction(e);

            final SimpleBooleanProperty watchdog = new SimpleBooleanProperty(
                    false);
            watchdog.addListener(PopupMenu.this.getHideListener());
            watchdog.bind(this.showingProperty());
            PopupMenu.this.hideMenu.add(watchdog);
            this.setWidth(100);
            this.setHeight(100);
            this.setMinWidth(100);
            this.setMinHeight(100);
            this.setPrefWidth(100);
            this.setPrefHeight(100);
            this.setMaxWidth(100);
            this.setMaxHeight(100);

            btn.setMinWidth(100);
            btn.setMinHeight(100);
            btn.setPrefWidth(100);
            btn.setPrefHeight(100);
            btn.setMaxWidth(100);
            btn.setMaxHeight(100);

            this.getItems().add(this.item);
        }

        /**
         * This method is used to get button with graphic.
         * 
         * @param name
         *            url of button
         * @return node of button
         */
        public Node getButtonGraphic(final String name) {
            final FXMLLoader loader = new FXMLLoader();
            try {
                loader.setLocation(new URL("file:/"));
                loader.load(this.getClass().getResourceAsStream(
                        "/fxml/" + name + ".fxml"));
            } catch (final IOException e) {
                DiptychonLogger.error("Unable to load MainFrame.fxml");
                DiptychonLogger.error("{}", e);
            }
            DiptychonLogger.info("Load file {}", loader.getLocation()
                    .toString() + "/fxml/" + name + ".fxml");
            return loader.getRoot();
        }
    }
}

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

import java.io.File;
import java.util.ArrayList;

import javafx.stage.Window;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.ui.views.dialogs.WizardDialog.emDialogType;

/**
 * This class is used to manage the wizard dialogs. It implements the switches
 * among different wizard dialogs.
 */
public class WizardDialogManager {
    /**
     * This memeber is a array list to store the objects of wizard dialog.
     */
    private ArrayList<WizardDialog> wizardList;

    /**
     * This member is the reference of document panel controller.
     */
    private DocumentPanelController documentPanelController;

    /**
     * This method is used to initialize wizard dialog manager. It creates
     * dialogs and assigns the relationships to them.
     * 
     * @param dpc
     *            the reference to document controller
     * @param wd
     *            the owner window
     */
    public void intial(final DocumentPanelController dpc, final Window wd) {
        this.documentPanelController = dpc;

        this.wizardList = new ArrayList<WizardDialog>();

        final WizardFileSelector fch = (WizardFileSelector) new WizardFileSelector.Factory()
                .createDialog("WizardFileSelector",
                        this.documentPanelController);

        fch.initial(wd);
        fch.setTitle("FileChooser");
        fch.setOwnType(emDialogType.emFileChooser);
        fch.setNextType(emDialogType.emGrayScale);
        fch.setPreviousType(emDialogType.emStart);
        this.wizardList.add(fch);

        final WizardGrayScale gsw = (WizardGrayScale) new WizardGrayScale.Factory()
                .createDialog("WizardGrayScale", this.documentPanelController);
        gsw.initial(wd);
        gsw.setTitle("GrayScaleWizard");
        gsw.setOwnType(emDialogType.emGrayScale);
        gsw.setNextType(emDialogType.emBinarizing);
        gsw.setPreviousType(emDialogType.emFileChooser);
        this.wizardList.add(gsw);

        final WizardBinarizing bw = (WizardBinarizing) new WizardBinarizing.Factory()
                .createDialog("WizardBinarizing", this.documentPanelController);
        bw.initial(wd);
        bw.setTitle("BinarizingWizard");
        bw.setOwnType(emDialogType.emBinarizing);
        bw.setNextType(emDialogType.emFinish);
        bw.setPreviousType(emDialogType.emGrayScale);
        this.wizardList.add(bw);

        // TextLinesWizard tlw = (TextLinesWizard)new
        // TextLinesWizard.Factory().createDialog("WizardTextLines",
        // documentPanelController);
        // tlw.initial(wd);
        // tlw.setTitle("TextLinesWizard");
        // tlw.setOwnType(emDialogType.emTextLines);
        // tlw.setNextType(emDialogType.emFinish);
        // tlw.setPreviousType(emDialogType.emBinarizing);
        // wizardList.add(tlw);
    }

    /**
     * This method is used to get the instance of desired dialog.
     * 
     * @param type
     *            emDialogType
     * @return WizardDialog
     */
    private WizardDialog getItem(final emDialogType type) {
        for (final WizardDialog wd : this.wizardList) {
            if (wd.getOwnType() == type) {
                return wd;
            }
        }
        return null;
    }

    /**
     * This method is used to show wizard dialogs step by step.
     * 
     * @return emDialogType
     */
    public emDialogType showWizardDialogs() {
        emDialogType nextEmType = emDialogType.emStart;
        while (true) {
            if (emDialogType.emStart == nextEmType) {
                nextEmType = emDialogType.emFileChooser;
            } else if (emDialogType.emFinish == nextEmType) {
                break;
            } else if (emDialogType.emCancel == nextEmType) {
                break;
            } else if (emDialogType.emFileChooser == nextEmType) {
                final WizardFileSelector fch = (WizardFileSelector) this
                        .getItem(nextEmType);
                if (null == fch) {
                    break;
                }
                fch.showDialog();
                nextEmType = fch.getNextState();
            } else if (emDialogType.emGrayScale == nextEmType) {
                final WizardGrayScale gsw = (WizardGrayScale) this
                        .getItem(nextEmType);
                if (null == gsw) {
                    break;
                }
                gsw.showDialog();
                nextEmType = gsw.getNextState();
            } else if (emDialogType.emBinarizing == nextEmType) {
                final WizardBinarizing bw = (WizardBinarizing) this
                        .getItem(nextEmType);
                if (null == bw) {
                    break;
                }
                bw.showDialog();
                nextEmType = bw.getNextState();
            }
            // else if(emDialogType.emTextLines == nextEmType)
            // {
            // TextLinesWizard tlw = (TextLinesWizard) getItem(nextEmType);
            // if(null == tlw)
            // {
            // break;
            // }
            // tlw.showDialog();
            // nextEmType = tlw.getNextState();
            // }
            else {
                break;
            }
        }
        if (emDialogType.emFinish == nextEmType) {
            boolean bFindText = false;
            String projectName = "";
            String savedPath = "";
            for (final WizardDialog wd : this.wizardList) {
                if (wd.getOwnType() == emDialogType.emFileChooser) {
                    final File[] files = ((WizardFileSelector) wd).getFiles();
                    this.documentPanelController.initDigital(files);
                    bFindText = ((WizardFileSelector) wd).findTextCheck
                            .isSelected();
                    projectName = ((WizardFileSelector) wd).getProjectName();
                    savedPath = ((WizardFileSelector) wd).getSavedPath();
                    wd.closeDialog();
                } else if (wd.getOwnType() == emDialogType.emGrayScale) {
                    this.documentPanelController.grayApply(
                            ((WizardGrayScale) wd).redSlider.getValue(),
                            ((WizardGrayScale) wd).greenSlider.getValue(),
                            ((WizardGrayScale) wd).blueSlider.getValue());
                    wd.closeDialog();
                } else if (wd.getOwnType() == emDialogType.emBinarizing) {
                    this.documentPanelController.binarizeApply(
                            (int) ((WizardBinarizing) wd).getWsValue(),
                            ((WizardBinarizing) wd).getSkValue());
                    wd.closeDialog();
                    if (bFindText) {
                        this.documentPanelController.findLines(10, 10);

                    }
                }
            }
            if (!projectName.isEmpty() && !savedPath.isEmpty()) {
                final String savedFile = savedPath + "\\" + projectName
                        + ".dsf";
                this.documentPanelController.save(savedFile);
            }

        }
        return nextEmType;
    }
}

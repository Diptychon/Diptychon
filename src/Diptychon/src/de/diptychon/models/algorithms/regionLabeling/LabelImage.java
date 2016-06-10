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
 *
 * Based on: JANKOWSKI, Mariusz (Department of Electrical Engineering,
 * University of Southern Maine, USA) and KUSKA, Jens-Peer (Department
 * for Computer Graphics and Image Processing, University of Leipzig,
 * Germany)
 */
package de.diptychon.models.algorithms.regionLabeling;

import java.util.Stack;

/**
 * LabelImage.java Find connected components in a matrix (that will be used in a
 * segment finding method for an image).
 */

public class LabelImage {

    /**
     * Attributes
     */
    private int[][] label;
    private Stack<int[]> stack;
    private int connectivity;
    private final int[][] img;
    private int lab;

    /**
     * Creates a new instance of LabelImage
     * 
     * @param img
     *            a (n x r) matrix where connected components must be labelled
     * @param connectivity
     *            if equals to 4, 4-connected elements computed. In ANY other
     *            case, 8-connected elements.
     */
    public LabelImage(int[][] img, int connectivity) {
        this.img = img;
        this.connectivity = connectivity;
        this.lab = 1;
        label = new int[this.img.length][this.img[0].length];
    }

    /**
     * Creates a new instance of LabelImage
     * 
     * @param img
     *            a (n x r) matrix where connected components must be labeled
     * @param connectivity
     *            if equals to 4, 4-connected elements computed. In ANY other
     *            case, 8-connected elements.
     */
    public LabelImage(int[][] img, int connectivity, int startLabel) {
        this.img = img;
        this.connectivity = connectivity;
        this.lab = startLabel;
        label = new int[this.img.length][this.img[0].length];
    }

    /**
     * Creates a new instance of LabelImage
     *
     * @param img
     *            a (n x r) matrix where connected components must be labeled
     * @param connectivity
     *            if equals to 4, 4-connected elements computed. In ANY other
     *            case, 8-connected elements.
     * @param labels
     *            a 2d array with already calculated labels
     * @param labelCount
     *            the number of labels
     */
    public LabelImage(int[][] img, int connectivity, int labels[][],
            int labelCount) {
        this.img = img;
        this.connectivity = connectivity;
        label = new int[this.img.length][this.img[0].length];
        for (int i = 0; i < label.length; i++) {
            System.arraycopy(labels[i], 0, this.label[i], 0, labels[i].length);
        }
        this.lab = labelCount + 1;
    }

    /**
     * Method finding connected components
     * 
     * @param img
     *            a (n x r) matrix where connected components must be labelled
     * @param connectivity
     *            if equals to 4, 4-connected elements computed. In ANY other
     *            case, 8-connected elements.
     * @return a ConnectedObjects object containing img labels
     */
    public ConnectedObjects labelImage() {
        int nrow = this.img.length;
        int ncol = this.img[0].length;
        lab = this.lab;
        int[] pos;
        stack = new Stack<int[]>();

        for (int x = 1; x < nrow; x++) {
            {
                for (int y = 1; y < ncol; y++) {
                    if (this.img[x][y] == 0) {
                        continue;
                    }
                    if (label[x][y] > 0) {
                        continue;
                    }
                    /* encountered unlabeled foreground pixel at position r, c */
                    /* push the position on the stack and assign label */
                    stack.push(new int[] { x, y });
                    label[x][y] = lab;
                    /* start the float fill */
                    while (!stack.isEmpty()) {
                        pos = stack.pop();
                        int i = pos[0];
                        int j = pos[1];
                        if (this.img[i - 1][j] == 1 && label[i - 1][j] == 0) {
                            stack.push(new int[] { i - 1, j });
                            label[i - 1][j] = lab;
                        }
                        if (this.img[i][j - 1] == 1 && label[i][j - 1] == 0) {
                            stack.push(new int[] { i, j - 1 });
                            label[i][j - 1] = lab;
                        }
                        if (this.img[i][j + 1] == 1 && label[i][j + 1] == 0) {
                            stack.push(new int[] { i, j + 1 });
                            label[i][j + 1] = lab;
                        }
                        if (this.img[i + 1][j] == 1 && label[i + 1][j] == 0) {
                            stack.push(new int[] { i + 1, j });
                            label[i + 1][j] = lab;
                        }
                        if (connectivity != 4) {
                            if (this.img[i - 1][j - 1] == 1
                                    && label[i - 1][j - 1] == 0) {
                                stack.push(new int[] { i - 1, j - 1 });
                                label[i - 1][j - 1] = lab;
                            }
                            if (this.img[i - 1][j + 1] == 1
                                    && label[i - 1][j + 1] == 0) {
                                stack.push(new int[] { i - 1, j + 1 });
                                label[i - 1][j + 1] = lab;
                            }
                            if (this.img[i + 1][j - 1] == 1
                                    && label[i + 1][j - 1] == 0) {
                                stack.push(new int[] { i + 1, j - 1 });
                                label[i + 1][j - 1] = lab;
                            }
                            if (this.img[i + 1][j + 1] == 1
                                    && label[i + 1][j + 1] == 0) {
                                stack.push(new int[] { i + 1, j + 1 });
                                label[i + 1][j + 1] = lab;
                            }
                        }
                    }
                    lab++;
                }
            }
        }
        return new ConnectedObjects(label, lab);
    }
}
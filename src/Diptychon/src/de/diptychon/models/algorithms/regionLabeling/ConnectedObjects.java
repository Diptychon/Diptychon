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
 * Based on: Leo COLLET, Cedric TELEGONE, Ecole Centrale Nantes
 */
package de.diptychon.models.algorithms.regionLabeling;

import java.util.ArrayList;

/**
 * Class implementing the connected objects in a matrix.
 */

public class ConnectedObjects {

    /**
     * Atributes
     */
    protected int num_labels;
    protected int[][] matrix;

    /**
     * Public constructor
     * 
     * @param m
     *            matrix containing elements with value 0 in the background and
     *            p(i,j)>0 in the foreground
     * @param num
     *            the number of connected objects in the matrix
     */
    public ConnectedObjects(int[][] m, int num) {
        this.matrix = new int[m.length][m[0].length];
        for (int x = 0; x < m[0].length; x++) {
            for (int y = 0; y < m.length; y++) {
                this.matrix[y][x] = m[y][x];
            }
        }
        this.num_labels = num;
    }

    public String toString() {
        String s = new String();
        s = "ConnectedObjects\n";
        s = s + "Value of num_labels : " + num_labels + "\n";
        s = s + "Value of matrix : " + matrix.toString() + "\n";
        s = s + "Size of matrix : " + matrix.length + " x " + matrix[0].length;
        return s;
    }

    /**
     * Get list of used labels
     *
     * @return a list with used labels in the input matrix
     */
    public ArrayList<Integer> getLabelList() {
        ArrayList<Integer> foundLabels = new ArrayList<Integer>();
        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (!foundLabels.contains(this.matrix[y][x])
                        && this.matrix[y][x] != 0) {
                    foundLabels.add(this.matrix[y][x]);
                }
            }
        }
        return foundLabels;
    }

    /**
     * Get the frequency of a certain label
     * 
     * @return the frequency
     */
    public int getLabelFrequency(int label) {
        int freq = 0;

        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (this.matrix[y][x] == label) {
                    freq++;
                }
            }
        }
        return freq;
    }

    /**
     * removes the region with the given label
     * 
     * @return the frequency
     */
    public void removeLabel(int label) {

        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (this.matrix[y][x] == label) {
                    this.matrix[y][x] = 0;
                }
            }
        }

    }

    /**
     * Get the minimum x coordinate for given label
     * 
     * @return the minimum x coordinate
     */
    public int getLabelMinX(int label) {
        int minX = Integer.MAX_VALUE;

        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (this.matrix[y][x] == label) {
                    if (x < minX) {
                        minX = x;
                    }
                }
            }
        }
        return minX;
    }

    /**
     * Get the minimum y coordinate for given label
     * 
     * @return the minimum y coordinate
     */
    public int getLabelMinY(int label) {
        int minY = Integer.MAX_VALUE;

        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (this.matrix[y][x] == label) {
                    if (y < minY) {
                        minY = y;
                    }
                }
            }
        }
        return minY;
    }

    /**
     * Get the maximum x coordinate for given label
     * 
     * @return the maximum x coordinate
     */
    public int getLabelMaxX(int label) {
        int maxX = 0;

        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (this.matrix[y][x] == label) {
                    if (x > maxX) {
                        maxX = x;
                    }
                }
            }
        }
        return maxX;
    }

    /**
     * Get the maximum y coordinate for given label
     * 
     * @return the maximum y coordinate
     */
    public int getLabelMaxY(int label) {
        int maxY = 0;

        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (this.matrix[y][x] == label) {
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }
        return maxY;
    }

    /**
     * Get number of labels
     * 
     * @return the actual number of used labels in the input matrix
     */
    public int getUsedLabelCount() {
        ArrayList<Integer> foundLabels = new ArrayList<Integer>();
        for (int y = 0; y < this.matrix.length; y++) {
            for (int x = 0; x < this.matrix[y].length; x++) {
                if (!foundLabels.contains(this.matrix[y][x])
                        && this.matrix[y][x] != 0) {
                    foundLabels.add(this.matrix[y][x]);
                }
            }
        }
        return foundLabels.size();
    }

    /**
     * Get number of labels
     * 
     * @return the highest label number in the input matrix
     */
    public int getLabelCount() {
        return num_labels;
    }

    /**
     * Get matrix of labeled objects
     * 
     * @return a 2-dim integer array containing 0 on background points and a
     *         label for each point in a connected component
     */
    public int[][] getMatrix() {
        return this.matrix;
    }

    /**
     * Get an element of the matrix of labeled objects
     * 
     * @param i
     *            vertical position
     * @param j
     *            horizontal position
     * @return value at this.matrix[i][j]
     */
    public int getMatrix(int i, int j) {
        return this.matrix[i][j];
    }

}

package rssi_graph.rssi.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ----------------------------------
 * DefaultBoxAndWhiskerXYDataset.java
 * ----------------------------------
 * (C) Copyright 2003-2008, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for Australian Institute of Marine
 *                   Science);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 05-Aug-2003 : Version 1, contributed by David Browning (DG);
 * 08-Aug-2003 : Minor changes to comments (DB)
 *               Allow average to be null  - average is a perculiar AIMS
 *               requirement which probably should be stripped out and overlaid
 *               if required...
 *               Added a number of methods to allow the max and min non-outlier
 *               and non-farout values to be calculated
 * 12-Aug-2003   Changed the getYValue to return the highest outlier value
 *               Added getters and setters for outlier and farout coefficients
 * 27-Aug-2003 : Renamed DefaultBoxAndWhiskerDataset
 *               --> DefaultBoxAndWhiskerXYDataset (DG);
 * 06-May-2004 : Now extends AbstractXYDataset (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 18-Nov-2004 : Updated for changes in RangeInfo interface (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for the 1.0.0
 *               release (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags from all over JFreeChart sources (DG);
 * 12-Nov-2007 : Implemented equals() and clone() (DG);
 *
 */
 
/**
 * Custom renderer for further customizations
 * source code taken from jfreechart BoxAndWhiskerRenderer.
 * custom modifications added
 * @author ph4r05
 */
public class RSSIBoxAndWhiskerRenderer  extends AbstractCategoryItemRenderer
        implements Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = 632027470694481177L;

    /** The color used to paint the median line and average marker. */
    private transient Paint artifactPaint;

    /** A flag that controls whether or not the box is filled. */
    private boolean fillBox;

    /** The margin between items (boxes) within a category. */
    private double itemMargin;

    /**
     * The maximum bar width as percentage of the available space in the plot,
     * where 0.05 is five percent.
     */
    private double maximumBarWidth;

    /**
     * A flag that controls whether or not the median indicator is drawn.
     *
     * @since 1.0.13
     */
    private boolean medianVisible;

    /**
     * A flag that controls whether or not the mean indicator is drawn.
     *
     * @since 1.0.13
     */
    private boolean meanVisible;

    /**
     * =================================================================
     * PH4r05 edit START
     * =================================================================
     */
    /**
     * A flag that controls whether or not the quartiles are drawn.
     *
     */
    private boolean quartilsVisible;

    /**
     * A flag that controls whether or not the min,max are drawn.
     *
     */
    private boolean minMaxVisible;

    /**
     * A flag that controls whether or not the outliers are drawn.
     */
    private boolean outliersVisible;
    
    /**
     * box fill alpha(transparency)
     */
    private int boxFillAlpha=255;
    
    /**
     * if true then each category is in separate small collumn on range axis
     */
    private boolean separateCategoriesOnRangeAxis=true;

    /** The paint used to fill the box. */
    private transient Paint boxPaint;
    /**
     * =================================================================
     * PH4r05 edit END
     * =================================================================
     */


    /**
     * Default constructor.
     */
    public RSSIBoxAndWhiskerRenderer() {
        this.artifactPaint = Color.black;
        this.fillBox = true;
        this.itemMargin = 0.20;
        this.maximumBarWidth = 1.0;
        this.medianVisible = true;
        this.meanVisible = true;
        this.minMaxVisible = true;
        this.quartilsVisible = true;
        this.outliersVisible = true;
        setBaseLegendShape(new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0));
    }

    /**
     * Returns the paint used to color the median and average markers.
     *
     * @return The paint used to draw the median and average markers (never
     *     <code>null</code>).
     *
     * @see #setArtifactPaint(Paint)
     */
    public Paint getArtifactPaint() {
        return this.artifactPaint;
    }

    /**
     * Sets the paint used to color the median and average markers and sends
     * a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getArtifactPaint()
     */
    public void setArtifactPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        
        // return if no new data
        if (this.artifactPaint.equals(paint)) return;
        
        this.artifactPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the box is filled.
     *
     * @return A boolean.
     *
     * @see #setFillBox(boolean)
     */
    public boolean getFillBox() {
        return this.fillBox;
    }

    /**
     * Sets the flag that controls whether or not the box is filled and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param flag  the flag.
     *
     * @see #getFillBox()
     */
    public void setFillBox(boolean flag) {
        this.fillBox = flag;
        fireChangeEvent();
    }

    /**
     * Returns the item margin.  This is a percentage of the available space
     * that is allocated to the space between items in the chart.
     *
     * @return The margin.
     *
     * @see #setItemMargin(double)
     */
    public double getItemMargin() {
        return this.itemMargin;
    }

    /**
     * Sets the item margin and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param margin  the margin (a percentage).
     *
     * @see #getItemMargin()
     */
    public void setItemMargin(double margin) {
        this.itemMargin = margin;
        fireChangeEvent();
    }

    /**
     * Returns the maximum bar width as a percentage of the available drawing
     * space.
     *
     * @return The maximum bar width.
     *
     * @see #setMaximumBarWidth(double)
     *
     * @since 1.0.10
     */
    public double getMaximumBarWidth() {
        return this.maximumBarWidth;
    }

    /**
     * Sets the maximum bar width, which is specified as a percentage of the
     * available space for all bars, and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param percent  the maximum Bar Width (a percentage).
     *
     * @see #getMaximumBarWidth()
     *
     * @since 1.0.10
     */
    public void setMaximumBarWidth(double percent) {
        this.maximumBarWidth = percent;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the mean indicator is
     * draw for each item.
     *
     * @return A boolean.
     *
     * @see #setMeanVisible(boolean)
     *
     * @since 1.0.13
     */
    public boolean isMeanVisible() {
        return this.meanVisible;
    }

    /**
     * Sets the flag that controls whether or not the mean indicator is drawn
     * for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param visible  the new flag value.
     *
     * @see #isMeanVisible()
     *
     * @since 1.0.13
     */
    public void setMeanVisible(boolean visible) {
        if (this.meanVisible == visible) {
            return;
        }
        this.meanVisible = visible;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the median indicator is
     * draw for each item.
     *
     * @return A boolean.
     *
     * @see #setMedianVisible(boolean)
     *
     * @since 1.0.13
     */
    public boolean isMedianVisible() {
        return this.medianVisible;
    }

    /**
     * Sets the flag that controls whether or not the median indicator is drawn
     * for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param visible  the new flag value.
     *
     * @see #isMedianVisible()
     *
     * @since 1.0.13
     */
    public void setMedianVisible(boolean visible) {
        this.medianVisible = visible;
    }

    /**
     * =================================================================
     * PH4r05 edit START
     * =================================================================
     */

    /**
     * Returns alpha level of box fill.
     * 
     * @return 
     */
    public int getBoxFillAlpha() {
        return boxFillAlpha;
    }

    /**
     * Sets alpha level for box fill.
     * @param boxFillAlpha 
     */
    public void setBoxFillAlpha(int boxFillAlpha) {
        this.boxFillAlpha = boxFillAlpha;
    }
    
    /**
     * return flag determining wether columns are separated or filled in single
     * collumn.
     * 
     * @return 
     */
    public boolean isSeparateCategoriesOnRangeAxis() {
        return separateCategoriesOnRangeAxis;
    }

    /**
     * sets flag determining wether columns are separated or filled in single
     * collumn.
     * 
     * @param separateCategoriesOnRangeAxis 
     */
    public void setSeparateCategoriesOnRangeAxis(boolean separateCategoriesOnRangeAxis) {
        this.separateCategoriesOnRangeAxis = separateCategoriesOnRangeAxis;
    }
    
    /**
     * Returns the flag that controls whether or not minimums and maximums are
     * drawn for each item.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public boolean isMinMaxVisible() {
        return minMaxVisible;
    }

    /**
     * Set the flag that controls whether or not minimums and maximums are
     * drawn for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public void setMinMaxVisible(boolean minMaxVisible) {
        if (this.minMaxVisible == minMaxVisible) {
            return;
        }
        this.minMaxVisible = minMaxVisible;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not outliers are
     * drawn for each item.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public boolean isOutliersVisible() {
        return outliersVisible;
    }

    /**
     * Set the flag that controls whether or not outliers are
     * drawn for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public void setOutliersVisible(boolean outliersVisible) {
        if (this.outliersVisible == outliersVisible) {
            return;
        }
        this.outliersVisible = outliersVisible;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not quartiles are
     * drawn for each item.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public boolean isQuartilsVisible() {
        return quartilsVisible;
    }

    /**
     * Sets the flag that controls whether or not quartiles are
     * drawn for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public void setQuartilsVisible(boolean quartilsVisible) {
        if (this.quartilsVisible == quartilsVisible) {
            return;
        }
        this.quartilsVisible = quartilsVisible;
        fireChangeEvent();
    }

        /**
     * Returns the paint used to fill boxes.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setBoxPaint(Paint)
     */
    public Paint getBoxPaint() {
        return this.boxPaint;
    }

    /**
     * Sets the paint used to fill boxes and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getBoxPaint()
     */
    public void setBoxPaint(Paint paint) {
        this.boxPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the box paint or, if this is <code>null</code>, the item
     * paint.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The paint used to fill the box for the specified item (never
     *         <code>null</code>).
     *
     * @since 1.0.10
     */
    protected Paint lookupBoxPaint(int series, int item) {
        Paint p = getBoxPaint();
        if (p != null) {
            return p;
        }
        else {
            // TODO: could change this to itemFillPaint().  For backwards
            // compatibility, it might require a useFillPaint flag.
            Paint itemPaint = getItemPaint(series, item);
            if (itemPaint instanceof Color){
                final Color itemColor = (Color) itemPaint;
                int alpha = this.getBoxFillAlpha();
                if (alpha<0){
                    alpha = itemColor.getAlpha();
                }
                final Color myColor = new Color(itemColor.getRed(), itemColor.getGreen(), itemColor.getBlue(), alpha);
                return myColor;
            }
            
            return itemPaint;
        }
    }
    /**
     * =================================================================
     * PH4r05 edit END
     * =================================================================
     */

    /**
     * Returns a legend item for a series.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series index (zero-based).
     *
     * @return The legend item (possibly <code>null</code>).
     */
    @Override
    public LegendItem getLegendItem(int datasetIndex, int series) {

        CategoryPlot cp = getPlot();
        if (cp == null) {
            return null;
        }

        // check that a legend item needs to be displayed...
        if (!isSeriesVisible(series) || !isSeriesVisibleInLegend(series)) {
            return null;
        }

        CategoryDataset dataset = cp.getDataset(datasetIndex);
        String label = getLegendItemLabelGenerator().generateLabel(dataset,
                series);
        String description = label;
        String toolTipText = null;
        if (getLegendItemToolTipGenerator() != null) {
            toolTipText = getLegendItemToolTipGenerator().generateLabel(
                    dataset, series);
        }
        String urlText = null;
        if (getLegendItemURLGenerator() != null) {
            urlText = getLegendItemURLGenerator().generateLabel(dataset,
                    series);
        }
        Shape shape = lookupLegendShape(series);
        Paint paint = lookupSeriesPaint(series);
        Paint outlinePaint = lookupSeriesOutlinePaint(series);
        Stroke outlineStroke = lookupSeriesOutlineStroke(series);
        LegendItem result = new LegendItem(label, description, toolTipText,
                urlText, shape, paint, outlineStroke, outlinePaint);
        result.setLabelFont(lookupLegendTextFont(series));
        Paint labelPaint = lookupLegendTextPaint(series);
        if (labelPaint != null) {
            result.setLabelPaint(labelPaint);
        }
        result.setDataset(dataset);
        result.setDatasetIndex(datasetIndex);
        result.setSeriesKey(dataset.getRowKey(series));
        result.setSeriesIndex(series);
        return result;

    }

    /**
     * Returns the range of values from the specified dataset that the
     * renderer will require to display all the data.
     *
     * @param dataset  the dataset.
     *
     * @return The range.
     */
    public Range findRangeBounds(CategoryDataset dataset) {
        return super.findRangeBounds(dataset, true);
    }

    /**
     * Initialises the renderer.  This method gets called once at the start of
     * the process of drawing a chart.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the data is to be plotted.
     * @param plot  the plot.
     * @param rendererIndex  the renderer index.
     * @param info  collects chart rendering information for return to caller.
     *
     * @return The renderer state.
     */
    @Override
    public CategoryItemRendererState initialise(Graphics2D g2,
                                                Rectangle2D dataArea,
                                                CategoryPlot plot,
                                                int rendererIndex,
                                                PlotRenderingInfo info) {

        CategoryItemRendererState state = super.initialise(g2, dataArea, plot,
                rendererIndex, info);
        // calculate the box width
        CategoryAxis domainAxis = getDomainAxis(plot, rendererIndex);
        CategoryDataset dataset = plot.getDataset(rendererIndex);
        if (dataset != null) {
            int columns = dataset.getColumnCount();
            int rows = dataset.getRowCount();
            double space = 0.0;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                space = dataArea.getHeight();
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                space = dataArea.getWidth();
            }
            double maxWidth = space * getMaximumBarWidth();
            double categoryMargin = 0.0;
            double currentItemMargin = 0.0;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            if (rows > 1) {
                currentItemMargin = getItemMargin();
            }
            double used = space * (1 - domainAxis.getLowerMargin()
                                     - domainAxis.getUpperMargin()
                                     - categoryMargin - currentItemMargin);
            if ((rows * columns) > 0) {
                if (separateCategoriesOnRangeAxis){
                    state.setBarWidth(Math.min(used / (dataset.getColumnCount()
                        * dataset.getRowCount()), maxWidth));
                } else {
                    state.setBarWidth(Math.min(used / (dataset.getRowCount()
                            * 2), maxWidth));
                }
                
            }
            else {
                state.setBarWidth(Math.min(used, maxWidth));
            }
        }
        return state;

    }

    /**
     * Draw a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area in which the data is drawn.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the data (must be an instance of
     *                 {@link BoxAndWhiskerCategoryDataset}).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
                         CategoryItemRendererState state,
                         Rectangle2D dataArea,
                         CategoryPlot plot,
                         CategoryAxis domainAxis,
                         ValueAxis rangeAxis,
                         CategoryDataset dataset,
                         int row,
                         int column,
                         int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(row, column)) {
            return;
        }

        if (!(dataset instanceof BoxAndWhiskerCategoryDataset)) {
            throw new IllegalArgumentException(
                    "BoxAndWhiskerRenderer.drawItem() : the data should be "
                    + "of type BoxAndWhiskerCategoryDataset only.");
        }

        PlotOrientation orientation = plot.getOrientation();

        if (orientation == PlotOrientation.HORIZONTAL) {
            drawHorizontalItem(g2, state, dataArea, plot, domainAxis,
                    rangeAxis, dataset, row, column);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            drawVerticalItem(g2, state, dataArea, plot, domainAxis,
                    rangeAxis, dataset, row, column);
        }

    }

    /**
     * Draws the visual representation of a single data item when the plot has
     * a horizontal orientation.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset (must be an instance of
     *                 {@link BoxAndWhiskerCategoryDataset}).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     */
    public void drawHorizontalItem(Graphics2D g2,
                                   CategoryItemRendererState state,
                                   Rectangle2D dataArea,
                                   CategoryPlot plot,
                                   CategoryAxis domainAxis,
                                   ValueAxis rangeAxis,
                                   CategoryDataset dataset,
                                   int row,
                                   int column) {

        BoxAndWhiskerCategoryDataset bawDataset
                = (BoxAndWhiskerCategoryDataset) dataset;

        double categoryEnd = domainAxis.getCategoryEnd(column,
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column,
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = Math.abs(categoryEnd - categoryStart);

        double yy = categoryStart;
        int seriesCount = getRowCount();
        int categoryCount = getColumnCount();

        if (seriesCount > 1) {
            double seriesGap = dataArea.getHeight() * getItemMargin()
                               / (categoryCount * (seriesCount - 1));
            double usedWidth = (state.getBarWidth() * seriesCount)
                               + (seriesGap * (seriesCount - 1));
            // offset the start of the boxes if the total width used is smaller
            // than the category width
            double offset = (categoryWidth - usedWidth) / 2;
            yy = yy + offset + (row * (state.getBarWidth() + seriesGap));
        }
        else {
            // offset the start of the box if the box width is smaller than
            // the category width
            double offset = (categoryWidth - state.getBarWidth()) / 2;
            yy = yy + offset;
        }

        g2.setPaint(getItemPaint(row, column));
        Stroke s = getItemStroke(row, column);
        g2.setStroke(s);

        RectangleEdge location = plot.getRangeAxisEdge();

        Number xQ1 = bawDataset.getQ1Value(row, column);
        Number xQ3 = bawDataset.getQ3Value(row, column);
        Number xMax = bawDataset.getMaxRegularValue(row, column);
        Number xMin = bawDataset.getMinRegularValue(row, column);

        Shape box = null;
        if (xQ1 != null && xQ3 != null && xMax != null && xMin != null) {

            double xxQ1 = rangeAxis.valueToJava2D(xQ1.doubleValue(), dataArea,
                    location);
            double xxQ3 = rangeAxis.valueToJava2D(xQ3.doubleValue(), dataArea,
                    location);
            double xxMax = rangeAxis.valueToJava2D(xMax.doubleValue(), dataArea,
                    location);
            double xxMin = rangeAxis.valueToJava2D(xMin.doubleValue(), dataArea,
                    location);
            double yymid = yy + state.getBarWidth() / 2.0;

            /**
             * =================================================================
             * PH4r05 edit START
             * =================================================================
             */
            // ph4r05 edit:
            // if minimum/maximum should be drawn
            if (this.isMinMaxVisible()){
                // draw the upper shadow...
                if (this.isQuartilsVisible()){
                    g2.draw(new Line2D.Double(xxMax, yymid, xxQ3, yymid));
                } // max-q3 line
                g2.draw(new Line2D.Double(xxMax, yy, xxMax,
                        yy + state.getBarWidth())); // max line

                // draw the lower shadow...
                if (this.isQuartilsVisible()){
                    g2.draw(new Line2D.Double(xxMin, yymid, xxQ1, yymid));
                }
                g2.draw(new Line2D.Double(xxMin, yy, xxMin,
                        yy + state.getBarWidth())); // min line
            }

            // ph4r05 edit:
            // if quartiles should be drawn
            if (this.isQuartilsVisible()){
                // draw the box...
                box = new Rectangle2D.Double(Math.min(xxQ1, xxQ3), yy,
                        Math.abs(xxQ1 - xxQ3), state.getBarWidth());
                if (this.fillBox) {
                    g2.setPaint(lookupBoxPaint(row, column));
                    g2.fill(box);
                }
                g2.setStroke(getItemOutlineStroke(row, column));
                g2.setPaint(getItemOutlinePaint(row, column));
                g2.draw(box);
            }
            /**
             * =================================================================
             * PH4r05 edit END
             * =================================================================
             */
        }

        // draw mean - SPECIAL AIMS REQUIREMENT...
        g2.setPaint(this.artifactPaint);
        double aRadius = 0;                 // average radius
        if (this.meanVisible) {
            Number xMean = bawDataset.getMeanValue(row, column);
            if (xMean != null) {
                double xxMean = rangeAxis.valueToJava2D(xMean.doubleValue(),
                        dataArea, location);
                aRadius = state.getBarWidth() / 4;
                // here we check that the average marker will in fact be
                // visible before drawing it...
                if ((xxMean > (dataArea.getMinX() - aRadius))
                        && (xxMean < (dataArea.getMaxX() + aRadius))) {
                    Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xxMean
                            - aRadius, yy + aRadius, aRadius * 2, aRadius * 2);
                    g2.fill(avgEllipse);
                    g2.draw(avgEllipse);
                }
            }
        }

        // draw median...
        if (this.medianVisible) {
            Number xMedian = bawDataset.getMedianValue(row, column);
            if (xMedian != null) {
                double xxMedian = rangeAxis.valueToJava2D(xMedian.doubleValue(),
                        dataArea, location);
                g2.draw(new Line2D.Double(xxMedian, yy, xxMedian,
                        yy + state.getBarWidth()));
            }
        }

        // collect entity and tool tip information...
        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, box);
            }
        }

    }

    /**
     * Draws the visual representation of a single data item when the plot has
     * a vertical orientation.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset (must be an instance of
     *                 {@link BoxAndWhiskerCategoryDataset}).
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     */
    public void drawVerticalItem(Graphics2D g2,
                                 CategoryItemRendererState state,
                                 Rectangle2D dataArea,
                                 CategoryPlot plot,
                                 CategoryAxis domainAxis,
                                 ValueAxis rangeAxis,
                                 CategoryDataset dataset,
                                 int row,
                                 int column) {

        BoxAndWhiskerCategoryDataset bawDataset
                = (BoxAndWhiskerCategoryDataset) dataset;

        double categoryEnd = domainAxis.getCategoryEnd(column,
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryStart = domainAxis.getCategoryStart(column,
                getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double categoryWidth = categoryEnd - categoryStart;

        double xx = categoryStart;
        int seriesCount = getRowCount();
        int categoryCount = getColumnCount();

        if (seriesCount > 1) {
            double seriesGap = dataArea.getWidth() * getItemMargin()
                               / (categoryCount * (seriesCount - 1));
            
            double usedWidth = 0.0;
            if (this.separateCategoriesOnRangeAxis){
                usedWidth = (state.getBarWidth() * seriesCount)
                               + (seriesGap * (seriesCount - 1));
            } else {
                usedWidth = (state.getBarWidth() * seriesCount)
                        + (seriesGap * (seriesCount - 1));
            }
            
            // offset the start of the boxes if the total width used is smaller
            // than the category width
            double offset = (this.separateCategoriesOnRangeAxis) ?
                                (categoryWidth - usedWidth) / 2 :
                                (categoryWidth - state.getBarWidth()) / 2;
            
            if (this.separateCategoriesOnRangeAxis)
                xx = xx + offset + (row * (state.getBarWidth() + seriesGap));
            else
                xx = xx + offset;
        }
        else {
            // offset the start of the box if the box width is smaller than the
            // category width
            double offset = (categoryWidth - state.getBarWidth()) / 2;
            xx = xx + offset;
        }

        double yyAverage = 0.0;
        double yyOutlier;

        Paint itemPaint = getItemPaint(row, column);
        g2.setPaint(itemPaint);
        Stroke s = getItemStroke(row, column);
        g2.setStroke(s);
        
        // set item paint for artifact
        this.artifactPaint=itemPaint;
        //this.setArtifactPaint(itemPaint);

        double aRadius = 0;                 // average radius

        RectangleEdge location = plot.getRangeAxisEdge();

        Number yQ1 = bawDataset.getQ1Value(row, column);
        Number yQ3 = bawDataset.getQ3Value(row, column);
        Number yMax = bawDataset.getMaxRegularValue(row, column);
        Number yMin = bawDataset.getMinRegularValue(row, column);
        Shape box = null;
        if (yQ1 != null && yQ3 != null && yMax != null && yMin != null) {

            double yyQ1 = rangeAxis.valueToJava2D(yQ1.doubleValue(), dataArea,
                    location);
            double yyQ3 = rangeAxis.valueToJava2D(yQ3.doubleValue(), dataArea,
                    location);
            double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(),
                    dataArea, location);
            double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(),
                    dataArea, location);
            double xxmid = xx + state.getBarWidth() / 2.0;

            /**
             * =================================================================
             * PH4r05 edit START
             * =================================================================
             */
            // ph4r05 edit:
            // if minimum/maximum should be drawn
            if (this.isMinMaxVisible()){
                // draw the upper shadow...
                g2.draw(new Line2D.Double(xxmid, yyMax, xxmid, yyQ3));
                g2.draw(new Line2D.Double(xx, yyMax, xx + state.getBarWidth(),
                        yyMax));

                // draw the lower shadow...
                g2.draw(new Line2D.Double(xxmid, yyMin, xxmid, yyQ1));
                g2.draw(new Line2D.Double(xx, yyMin, xx + state.getBarWidth(),
                        yyMin));
            }

            // draw the body...
            box = new Rectangle2D.Double(xx, Math.min(yyQ1, yyQ3),
                    state.getBarWidth(), Math.abs(yyQ1 - yyQ3));
            
            // ph4r05 edit:
            // if quartiles should be drawn
            if (this.isQuartilsVisible()){
                if (this.fillBox) {
                    g2.setPaint(lookupBoxPaint(row, column));
                    g2.fill(box);
                }
                g2.setStroke(getItemOutlineStroke(row, column));
                g2.setPaint(getItemOutlinePaint(row, column));
                g2.draw(box);
            }

            /**
             * =================================================================
             * PH4r05 edit END
             * =================================================================
             */
        }
        g2.setPaint(this.artifactPaint);

        // draw mean - SPECIAL AIMS REQUIREMENT...
        if (this.meanVisible) {
            Number yMean = bawDataset.getMeanValue(row, column);
            if (yMean != null) {
                yyAverage = rangeAxis.valueToJava2D(yMean.doubleValue(),
                        dataArea, location);
                aRadius = state.getBarWidth() / 4;
                // here we check that the average marker will in fact be
                // visible before drawing it...
                if ((yyAverage > (dataArea.getMinY() - aRadius))
                        && (yyAverage < (dataArea.getMaxY() + aRadius))) {
                    Ellipse2D.Double avgEllipse = new Ellipse2D.Double(
                            xx + aRadius, yyAverage - aRadius, aRadius * 2,
                            aRadius * 2);
                    g2.fill(avgEllipse);
                    g2.draw(avgEllipse);
                }
            }
        }

        // draw median...
        if (this.medianVisible) {
            Number yMedian = bawDataset.getMedianValue(row, column);
            if (yMedian != null) {
                double yyMedian = rangeAxis.valueToJava2D(
                        yMedian.doubleValue(), dataArea, location);
                g2.draw(new Line2D.Double(xx, yyMedian, xx + state.getBarWidth(),
                        yyMedian));
            }
        }

        // draw yOutliers...
        double maxAxisValue = rangeAxis.valueToJava2D(
                rangeAxis.getUpperBound(), dataArea, location) + aRadius;
        double minAxisValue = rangeAxis.valueToJava2D(
                rangeAxis.getLowerBound(), dataArea, location) - aRadius;

        g2.setPaint(itemPaint);

        // draw outliers
        double oRadius = state.getBarWidth() / 3;    // outlier radius
        List outliers = new ArrayList();
        OutlierListCollection outlierListCollection
                = new OutlierListCollection();

        // From outlier array sort out which are outliers and put these into a
        // list If there are any farouts, set the flag on the
        // OutlierListCollection
        List yOutliers = bawDataset.getOutliers(row, column);

        /**
         * =================================================================
         * PH4r05 edit START
         * =================================================================
         */
        if (yOutliers != null && this.isOutliersVisible()) {
        /**
         * =================================================================
         * PH4r05 edit END
         * =================================================================
         */
            for (int i = 0; i < yOutliers.size(); i++) {
                double outlier = ((Number) yOutliers.get(i)).doubleValue();
                Number minOutlier = bawDataset.getMinOutlier(row, column);
                Number maxOutlier = bawDataset.getMaxOutlier(row, column);
                Number minRegular = bawDataset.getMinRegularValue(row, column);
                Number maxRegular = bawDataset.getMaxRegularValue(row, column);
                if (outlier > maxOutlier.doubleValue()) {
                    outlierListCollection.setHighFarOut(true);
                }
                else if (outlier < minOutlier.doubleValue()) {
                    outlierListCollection.setLowFarOut(true);
                }
                else if (outlier > maxRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea,
                            location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0,
                            yyOutlier, oRadius));
                }
                else if (outlier < minRegular.doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea,
                            location);
                    outliers.add(new Outlier(xx + state.getBarWidth() / 2.0,
                            yyOutlier, oRadius));
                }
                Collections.sort(outliers);
            }

            // Process outliers. Each outlier is either added to the
            // appropriate outlier list or a new outlier list is made
            for (Iterator iterator = outliers.iterator(); iterator.hasNext();) {
                Outlier outlier = (Outlier) iterator.next();
                outlierListCollection.add(outlier);
            }

            for (Iterator iterator = outlierListCollection.iterator();
                     iterator.hasNext();) {
                OutlierList list = (OutlierList) iterator.next();
                Outlier outlier = list.getAveragedOutlier();
                Point2D point = outlier.getPoint();

                if (list.isMultiple()) {
                    drawMultipleEllipse(point, state.getBarWidth(), oRadius,
                            g2);
                }
                else {
                    drawEllipse(point, oRadius, g2);
                }
            }

            // draw farout indicators
            if (outlierListCollection.isHighFarOut()) {
                drawHighFarOut(aRadius / 2.0, g2,
                        xx + state.getBarWidth() / 2.0, maxAxisValue);
            }

            if (outlierListCollection.isLowFarOut()) {
                drawLowFarOut(aRadius / 2.0, g2,
                        xx + state.getBarWidth() / 2.0, minAxisValue);
            }
        }
        // collect entity and tool tip information...
        if (state.getInfo() != null && box != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, box);
            }
        }

    }

    /**
     * Draws a dot to represent an outlier.
     *
     * @param point  the location.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    private void drawEllipse(Point2D point, double oRadius, Graphics2D g2) {
        Ellipse2D dot = new Ellipse2D.Double(point.getX() + oRadius / 2,
                point.getY(), oRadius, oRadius);
        g2.draw(dot);
    }

    /**
     * Draws two dots to represent the average value of more than one outlier.
     *
     * @param point  the location
     * @param boxWidth  the box width.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    private void drawMultipleEllipse(Point2D point, double boxWidth,
                                     double oRadius, Graphics2D g2)  {

        Ellipse2D dot1 = new Ellipse2D.Double(point.getX() - (boxWidth / 2)
                + oRadius, point.getY(), oRadius, oRadius);
        Ellipse2D dot2 = new Ellipse2D.Double(point.getX() + (boxWidth / 2),
                point.getY(), oRadius, oRadius);
        g2.draw(dot1);
        g2.draw(dot2);
    }

    /**
     * Draws a triangle to indicate the presence of far-out values.
     *
     * @param aRadius  the radius.
     * @param g2  the graphics device.
     * @param xx  the x coordinate.
     * @param m  the y coordinate.
     */
    private void drawHighFarOut(double aRadius, Graphics2D g2, double xx,
                                double m) {
        double side = aRadius * 2;
        g2.draw(new Line2D.Double(xx - side, m + side, xx + side, m + side));
        g2.draw(new Line2D.Double(xx - side, m + side, xx, m));
        g2.draw(new Line2D.Double(xx + side, m + side, xx, m));
    }

    /**
     * Draws a triangle to indicate the presence of far-out values.
     *
     * @param aRadius  the radius.
     * @param g2  the graphics device.
     * @param xx  the x coordinate.
     * @param m  the y coordinate.
     */
    private void drawLowFarOut(double aRadius, Graphics2D g2, double xx,
                               double m) {
        double side = aRadius * 2;
        g2.draw(new Line2D.Double(xx - side, m - side, xx + side, m - side));
        g2.draw(new Line2D.Double(xx - side, m - side, xx, m));
        g2.draw(new Line2D.Double(xx + side, m - side, xx, m));
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RSSIBoxAndWhiskerRenderer)) {
            return false;
        }
        RSSIBoxAndWhiskerRenderer that = (RSSIBoxAndWhiskerRenderer) obj;
        if (this.fillBox != that.fillBox) {
            return false;
        }
        if (this.itemMargin != that.itemMargin) {
            return false;
        }
        if (this.maximumBarWidth != that.maximumBarWidth) {
            return false;
        }
        if (this.meanVisible != that.meanVisible) {
            return false;
        }
        if (this.medianVisible != that.medianVisible) {
            return false;
        }
        if (this.minMaxVisible != that.minMaxVisible) {
            return false;
        }
        if (this.quartilsVisible != that.quartilsVisible) {
            return false;
        }
        if (!PaintUtilities.equal(this.artifactPaint, that.artifactPaint)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.artifactPaint, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.artifactPaint = SerialUtilities.readPaint(stream);
    }
}


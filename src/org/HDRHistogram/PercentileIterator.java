package org.HDRHistogram;

import java.util.Iterator;

/**
 * Used for iterating through histogram values according to percentile levels. The iteration is
 * performed in steps that start at 0% and reduce their distance to 100% according to the
 * <i>percentileTicksPerHalfDistance</i> parameter, ultimately reaching 100% when all recorded histogram
 * values are exhausted.
*/
public class PercentileIterator extends AbstractHistogramIterator implements Iterator<HistogramIterationValue> {
    int percentileTicksPerHalfDistance;
    double percentileLevelToIterateTo;
    double percentileLevelToIterateFrom;
    boolean reachedLastRecordedValue;

    /**
     * Reset iterator for re-use in a fresh iteration over the same histogram data set.
     *
     * @param percentileTicksPerHalfDistance The number of iteration steps per half-distance to 100%.
     */
    public void reset(int percentileTicksPerHalfDistance) {
        reset(histogram, rawCounts, percentileTicksPerHalfDistance);
    }

    private void reset(final Histogram histogram, boolean rawCounts, final int percentileTicksPerHalfDistance) {
        super.resetIterator(histogram, rawCounts);
        this.percentileTicksPerHalfDistance = percentileTicksPerHalfDistance;
        this.percentileLevelToIterateTo = 0.0;
        this.percentileLevelToIterateFrom = 0.0;
        this.reachedLastRecordedValue = false;
    }

    PercentileIterator(final Histogram histogram, boolean rawCounts, final int percentileTicksPerHalf) {
        reset(histogram, rawCounts, percentileTicksPerHalf);
    }

    @Override
    public boolean hasNext() {
        if (super.hasNext())
            return true;
        // We want one additional last step to 100%
        if (!reachedLastRecordedValue) {
            percentileLevelToIterateTo = 100.0;
            reachedLastRecordedValue = true;
            return true;
        }
        return false;
    }

    void incrementIterationLevel() {
        percentileLevelToIterateFrom = percentileLevelToIterateTo;
        long percentileReportingTicks =
                percentileTicksPerHalfDistance *
                        (long) Math.pow(2,
                                (long) (Math.log(100.0 / (100.0 - (percentileLevelToIterateTo))) / Math.log(2)) + 1);
        percentileLevelToIterateTo += 100.0 / percentileReportingTicks;
    }

    boolean reachedIterationLevel() {
        if (countAtThisValue == 0)
            return false;
        double currentPercentile = (100.0 * (double) totalCountToCurrentIndex) / arrayTotalCount;
        return (currentPercentile >= percentileLevelToIterateTo);
    }

    @Override
    double getPercentileIteratedTo() {
        return percentileLevelToIterateTo;
    }

    @Override
    double getPercentileIteratedFrom() {
        return percentileLevelToIterateFrom;
    }
}

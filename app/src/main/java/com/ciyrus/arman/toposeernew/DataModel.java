package com.ciyrus.arman.toposeernew;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.mapbox.mapboxsdk.plugins.annotation.Line;

public class DataModel
{
    private double highestElevation = 0.0;
    private double lowestElevation = 0.0;
    private double gradientAverage = 0.0;
    LineData chart=null;

    //setters
    public void setLineChart(LineData _chart)
    {
     this.chart = _chart;
    }
    public void setHighestElevation(double _highestElevation)
    {
        this.highestElevation = _highestElevation;
    }
    public void setLowestElevation(double _lowestElevation)
    {
        this.lowestElevation = _lowestElevation;
    }
    public void setGradientAverage(double _gradientAverage)
    {
        this.gradientAverage = _gradientAverage;
    }
    //getters
    public String getHighestElevation()
    {
       return Double.toString(this.highestElevation);
    }
    public String getLowestElevation()
    {
        return Double.toString(this.lowestElevation);
    }
    public String getGradientAverage()
    {
        return Double.toString(this.gradientAverage);
    }

    public LineData getChart() {
        return this.chart;
    }
}

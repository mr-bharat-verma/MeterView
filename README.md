# MeterView
Meter is a UI component that can be used to show progress/status in a fancy way. Currently it support only half-round view like fuelmeter/fuel tank in cars. It wil be supporting full circle soon (like speedometer).

**How to Use**


***Step 1: Importing Control***
Just copy Meter.java and meter_attribute.xml class in your project. 

***Step 2: Using Control***
where ever you want to use this view, include following in XML:

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:meter="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <in.meterview.Meter
        android:id="@+id/meter_view"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        meter:currentValue="25"/>
</RelativeLayout>

  
```

Height will be automatically adjusted by the width given and space available. 

***Step 3: Customisations*** 
There are many customizations available. Please checkout them in meter_attribute.xml or the sample. 

**Output**


![Example](http://s31.postimg.org/dpezryrx7/Screenshot_20160420_160611.png)





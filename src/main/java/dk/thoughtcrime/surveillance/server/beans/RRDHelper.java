package dk.thoughtcrime.surveillance.server.beans;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;
import dk.thoughtcrime.surveillance.server.database.SensorDAO;
import dk.thoughtcrime.surveillance.server.dataobjects.Reading;
import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by jimmy on 31/07/15.
 */

public class RRDHelper {
    @Autowired(required = true)
    SensorDAO mgs;

    @Value("${rrd.database.prefix}")
    String rrdDatabasePrefix;

    @Value("${rrd.image.prefix}")
    String rrdImagePath;

    @Value("${rrd.tool}")
    String rrdTool;

    @Value("${prefix}")
    String appPrefix;

    private Logger log = LoggerFactory.getLogger(RRDHelper.class);

    public void createRRDb(Sensor s){
        String rrdFile = rrdFileName(s);
        File f = new File(rrdFile);
        log.debug("Registring new sensor :"+s.getSensor()+" on host:"+s.getHost());
        if(f.exists() == false) {
            String[] cmd = {rrdTool, "create", rrdFile, "--start", "now", "--step", "60", "DS:a:GAUGE:120:-50:50", "RRA:AVERAGE:0.5:2:720", "RRA:AVERAGE:0.5:15:672", "RRA:AVERAGE:0.5:60:720", "RRA:AVERAGE:0.5:360:1460"};
            runCmd(cmd);
        }
    }

    public void updateReading(Sensor s, Reading r){
        String rrdFile = rrdFileName(s);
        String [] cmd = {rrdTool,"update",rrdFile,"--template","a","N:"+r.getReading()};
        runCmd(cmd);
    }

    public void updateGraphs(){
        log.info("Updating graphs");
        SunriseSunsetCalculator calculator = getSunInfo();
        ArrayList<String> defs = new ArrayList<String>();
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        Calendar dawn = calculator.getCivilSunriseCalendarForDate(now.toCalendar(new Locale("da", "DK")));
        Calendar dusk = calculator.getCivilSunsetCalendarForDate(now.toCalendar(new Locale("da", "DK")));
        Calendar sunrise = calculator.getOfficialSunriseCalendarForDate(now.toCalendar(new Locale("da", "DK")));
        Calendar sunset = calculator.getOfficialSunsetCalendarForDate(now.toCalendar(new Locale("da", "DK")));
        boolean sunInfo = true;

        defs.add("COMMENT:Location\\t    Last\\t\\tAvg\\t\\tMax\\t\\tMin\\tAlert Low\\tAlert High\\n");
        defs.add("HRULE:0#0000FF:freezing\\n");

        for(Sensor s : mgs.getSensors()){
            String rrdFile = rrdFileName(s);

            defs.add("DEF:" + s.getSensor() + "=" + rrdFile + ":a:AVERAGE");

            if(sunInfo) {
                defs.add("CDEF:nightplus=LTIME,86400,%," + dateSecondsFromMidnight(sunrise) + ",LT,INF,LTIME,86400,%," + dateSecondsFromMidnight(sunset) + ",GT,INF,UNKN," + s.getSensor() + ",*,IF,IF");
                defs.add("CDEF:nightminus=LTIME,86400,%," + dateSecondsFromMidnight(sunrise) + ",LT,NEGINF,LTIME,86400,%," + dateSecondsFromMidnight(sunrise) + ",GT,NEGINF,UNKN," + s.getSensor() + ",*,IF,IF");
                defs.add("CDEF:dusktill=LTIME,86400,%," + dateSecondsFromMidnight(dawn) + ",LT,INF,LTIME,86400,%," + dateSecondsFromMidnight(dusk) + ",GT,INF,UNKN," + s.getSensor() + ",*,IF,IF");
                defs.add("CDEF:dawntill=LTIME,86400,%," + dateSecondsFromMidnight(dawn) + ",LT,NEGINF,LTIME,86400,%," + dateSecondsFromMidnight(dusk) + ",GT,NEGINF,UNKN," + s.getSensor() + ",*,IF,IF");
                defs.add("AREA:nightplus#E0E0E0");
                defs.add("AREA:nightminus#E0E0E0");
                defs.add("AREA:dusktill#CCCCCC");
                defs.add("AREA:dawntill#CCCCCC");
                sunInfo = false;
            }
            defs.add("LINE2:"+s.getSensor() + s.getGraphColor() + ":" + s.getAlias() + "\\t");
            defs.add("GPRINT:" + s.getSensor() + ":LAST:%5.1lf C\\t");
            defs.add("GPRINT:" + s.getSensor() + ":AVERAGE:%5.1lf C\\t");
            defs.add("GPRINT:" + s.getSensor() + ":MAX:%5.1lf C\\t");
            defs.add("GPRINT:"+s.getSensor()+":MIN:%5.1lf C\\t");
            defs.add("COMMENT:"+s.getAlert_low()+"\\t");
            defs.add("COMMENT:"+s.getAlert_high()+"\\n");
//            defs.add("CDEF:trend"+s.getId()+"="+s.getSensor()+",21600,TREND");
        }

        File prefix = new File(appPrefix);
        File imagePath = new File(prefix,rrdImagePath);

        String [] cmd = {rrdTool,"graph",new File(imagePath.getPath(),"temperature-hour.png").getAbsolutePath(),"--start","-6h","-u","35","-l","-10","--full-size-mode","--width","700","--height","400","--slope-mode","--color","SHADEB#9999CC"};
        this.runCmd(joinCollectionWithArray(cmd,defs));

        String [] cmd2 = {rrdTool,"graph",new File(imagePath.getPath(),"temperature-day.png").getAbsolutePath(),"--start","-1d","--end","now","-v","Last 24 Hours","-u","35","-l","-10","--full-size-mode","--width","700","--height","400","--slope-mode","--color","SHADEB#9999CC"};
        this.runCmd(joinCollectionWithArray(cmd2,defs));

        String [] cmd3 = {rrdTool,"graph",new File(imagePath.getPath(),"temperature-week.png").getAbsolutePath(),"--start","-1w","--end","now","-v","Last Week","-u","35","-l","-10","--full-size-mode","--width","700","--height","400","--slope-mode","--color","SHADEB#9999CC"};
        this.runCmd(joinCollectionWithArray(cmd3,defs));

        String [] cmd4 = {rrdTool,"graph",new File(imagePath.getPath(),"temperature-month.png").getAbsolutePath(),"--start","-1m","--end","now","-v","Last Month","-u","35","-l","-10","--full-size-mode","--width","700","--height","400","--slope-mode","--color","SHADEB#9999CC"};
        this.runCmd(joinCollectionWithArray(cmd4,defs));

        String [] cmd5 = {rrdTool,"graph",new File(imagePath.getPath(),"temperature-year.png").getAbsolutePath(),"--start","-1y","--end","now","-v","Last Year","-u","35","-l","-10","--full-size-mode","--width","700","--height","400","--slope-mode","--color","SHADEB#9999CC"};
        this.runCmd(joinCollectionWithArray(cmd5,defs));
    }

    private int dateSecondsFromMidnight(Calendar n){
        DateTime now = new DateTime(n);
        DateTime midnight = now.withTimeAtStartOfDay();
        Duration d = new Duration(midnight,now);
        return d.toStandardSeconds().getSeconds();
    }

    private SunriseSunsetCalculator getSunInfo(){
        Location l = new Location("56.2", "9.23");
        return new SunriseSunsetCalculator(l,"Europe/Copenhagen");
    }

    private String rrdFileName(Sensor s){
        final File prefix = new File(appPrefix);
        final File rrdPrefix = new File(prefix, rrdDatabasePrefix);
        final String rrdFile = s.getHost()+"_"+s.getSensor()+"_temperature.rrd";
        return new File(rrdPrefix,rrdFile).getAbsolutePath();
    }

    @SuppressWarnings("unused")
	private String arrayToString(String [] arr){
        StringBuilder sb = new StringBuilder();
        for(String s : arr){
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();
    }

    private String [] joinCollectionWithArray(String [] array, List<String> collection){
        ArrayList<String> ret = new ArrayList<String>();
        Collections.addAll(ret, array);
        for (String s : collection){
            ret.add(s);
        }
        String [] retArr = new String[ret.size()];
        return ret.toArray(retArr);
    }

    synchronized private void runCmd(String [] cmd){
        try {
            Process p = new ProcessBuilder(cmd).start();

            int retval = p.waitFor();
            BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            List<String> stdout = new ArrayList<>();
            while ((line = b.readLine()) != null) {
                stdout.add(line);
            }

            b.close();
            b = new BufferedReader(new InputStreamReader(p.getInputStream()));

            List<String> stderr = new ArrayList<>();
            while ((line = b.readLine()) != null) {
                stderr.add(line);
            }

            b.close();
            if(retval != 0){
                for(String s : stdout)
                    log.info(s);
                for(String s : stderr)
                    log.info(s);
            }

        }catch(Throwable t){
            throw new RuntimeException(t);
        }
    }
}

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.sensors.Temperature;
import org.apache.commons.io.IOUtils;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.io.FileInputStream;
import java.io.ObjectInputFilter;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final String PathConfigFile = "config.json";

    public Logger logger = Logger.getLogger("logger");

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        logger.log(Level.INFO, "Starting");
        ConfigOBJ config = readConfigFile();
        try {
            Connection conn = DriverManager.getConnection("jdbc:" + config.BDDAdress + "/" + config.BDDDatabase + "?useLegacyDatetimeCode=false&serverTimezone=Europe/Paris", config.BDDUsername, config.BDDPassword);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select * from cpu_day" );
            ResultSetMetaData rsmd = rs.getMetaData();
            rs.next();

            System.out.println(rsmd.getColumnCount());

            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.println(rs.getString(i));
            }
            System.out.println();
            conn.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to database");
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }





        logger.log(Level.INFO, "SystemInfo init");
        SystemInfo si = new SystemInfo();
        OperatingSystem operatingSystem = si.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = si.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        final DecimalFormat df = new DecimalFormat("#.000");
        final int procCount = centralProcessor.getLogicalProcessorCount();
        ArrayList<NetworkIF> networkIF = new ArrayList<>();
        networkIF.addAll(Arrays.asList(hardwareAbstractionLayer.getNetworkIFs()));
        double add;


        logger.log(Level.INFO, "Starting main loop");
        while (true) {
            /*System.out.println("cpu family: " + centralProcessor.getFamily());
            System.out.println("cpu identifier: " + centralProcessor.getIdentifier());
            System.out.println("cpu model: " + centralProcessor.getModel());
            System.out.println("cpu name: " + centralProcessor.getName());
            System.out.println("cpu processor ID: " + centralProcessor.getProcessorID());
            System.out.println("cpu stepping: " + centralProcessor.getStepping());
            System.out.println("cpu vendor: " + centralProcessor.getVendor());
            System.out.println("cpu logical proc count: " + centralProcessor.getLogicalProcessorCount());
            System.out.println("cpu physical package count: " + centralProcessor.getPhysicalPackageCount());
            System.out.println("cpu physical proc count: " + centralProcessor.getPhysicalProcessorCount());
            System.out.println();*/

/*
            add = 0.0;

            for (Double d : centralProcessor.getProcessorCpuLoadBetweenTicks()) {
                System.out.print(df.format(d) + "\t");
                add += d;
            }

            System.out.println();
            System.out.println(add / procCount);

            for (OSProcess osp : operatingSystem.getProcesses(10, OperatingSystem.ProcessSort.CPU, false)) {
                System.out.println(osp.getName() + " " + osp.getUser() + " " + osp.getUpTime() + "" + osp.getPath() + "" + osp.getCommandLine());
            }
*/
/*
            Components components = JSensors.get.components();
            List<Cpu> cpus = components.cpus;
            if (cpus != null) {
                for (final Cpu cpu : cpus) {
                    System.out.println("Found CPU component: " + cpu.name);
                    if (cpu.sensors != null) {
                        System.out.println("Sensors: ");

                        //Print temperatures
                        List<Temperature> temps = cpu.sensors.temperatures;
                        for (final Temperature temp : temps) {
                            System.out.println(temp.name + ": " + temp.value + " C");
                        }
                    }
                }
            }
*/

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void genConfigFile() {
        ConfigOBJ config = new ConfigOBJ();
        config.BDDAdress = "localhost";
        config.BDDDatabase = "systemMonitoring";
        config.BDDUsername = "root";
        config.BDDPassword = "";

        // Serialization
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(config);
        try {
            PrintWriter out = new PrintWriter(PathConfigFile);
            out.println(json);
            out.close();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "failed to write config file");
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public ConfigOBJ readConfigFile() {
        Gson gson = new Gson();

        try(FileInputStream inputStream = new FileInputStream(PathConfigFile)) {
            String everything = IOUtils.toString(inputStream, "UTF-8");
            ConfigOBJ obj = gson.fromJson(everything, ConfigOBJ.class);
            return obj;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "failed to read config file");
            logger.log(Level.SEVERE, e.getMessage());
        }
        return null;
    }
}

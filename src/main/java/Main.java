import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

public class Main {

    //chemin fichier config
    public static final String pathConfigFile = "config.json";

    //objet de log java
    public Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        new Main();
    }

    //classe principale
    public Main() {
        logger.log(Level.INFO, "Starting");
        ConfigOBJ config = readConfigFile();
        logger.log(Level.INFO, "trying to connect to database");
        try {
            Connection conn = DriverManager.getConnection("jdbc:" + config.BDDAdress + "/" + config.BDDDatabase + "?useLegacyDatetimeCode=false&serverTimezone=Europe/Paris", config.BDDUsername, config.BDDPassword);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='" + config.BDDDatabase + "'" );
            ResultSetMetaData rsmd = rs.getMetaData();

            ArrayList<String> db = new ArrayList<>();

            while (rs.next()) {
                db.add(rs.getString(1));
            }

            logger.debug("Found the following table(s):");
            for (String s : db) {
                logger.debug(" - " + s);
            }

            conn.close();
        } catch (Exception e) {
            logger.fatal("Failed to connect to database");
            logger.fatal(e.toString());
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
        config.BDDAdress = "mysql://localhost";
        config.BDDDatabase = "system_monitoring";
        config.BDDUsername = "root";
        config.BDDPassword = "";

        // Serialization
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(config);
        try {
            PrintWriter out = new PrintWriter(pathConfigFile);
            out.println(json);
            out.close();
            logger.log(Level.INFO, "basic config file successfully created, please edit it and restart the software");
            System.exit(0);
        } catch(Exception e) {
            logger.fatal("failed to write config file");
            logger.fatal(e.getMessage());
            System.exit(-1);
        }
    }

    public ConfigOBJ readConfigFile() {
        Gson gson = new Gson();

        File f = new File(pathConfigFile);
        if(f.exists() && !f.isDirectory()) {
            try(FileInputStream inputStream = new FileInputStream(pathConfigFile)) {
                String everything = IOUtils.toString(inputStream, "UTF-8");
                ConfigOBJ obj = gson.fromJson(everything, ConfigOBJ.class);
                return obj;
            } catch (Exception e) {
                logger.fatal("failed to read config file");
                logger.fatal(e.getMessage());
                System.exit(-1);
                return null;
            }
        } else {
            logger.warn("Config file not found, tyring to create it");
            genConfigFile();
        }
        return null; //ne devrais jamais être atteind
    }
}

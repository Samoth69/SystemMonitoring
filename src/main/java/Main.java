import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.profesorfalken.jsensors.model.components.Disk;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
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

public class Main {

    //chemin fichier config
    public static final String pathConfigFile = "config.json";

    //objet de log java (oracle)
    public static final Logger logger = LogManager.getLogger(Main.class);

    //object de connection à la BDD
    private Connection connBDD;

    //object contenant toutes les informations matériel de l'envionnement
    private SystemInfo si;

    //list des disques de l'environnement
    private ArrayList<DiskStat> hdd = new ArrayList<>();

    //object custom contenant la configuration de l'objet
    private ConfigOBJ configuration;

    //contient le nombre de thread dans le processeur de l'environnement
    private final int thread_count;

    public static void main(String[] args) {
        new Main();
    }

    //classe principale
    public Main() {
        Configurator.setRootLevel(Level.DEBUG);
        logger.log(Level.INFO, "if you see an error that start by 'ERROR StatusLogger No Log4j 2 configuration file found.' THIS IS NORMAL");
        logger.log(Level.INFO, "Starting");
        configuration = readConfigFile();

        logger.log(Level.INFO, "SystemInfo init");
        si = new SystemInfo();
        logger.log(Level.INFO, "setting environment variables");
        thread_count = si.getHardware().getProcessor().getLogicalProcessorCount();
        //OperatingSystem operatingSystem = si.getOperatingSystem();
        //HardwareAbstractionLayer hardwareAbstractionLayer = si.getHardware();
        //CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        //ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();
        logger.info("SystemInfo init done");
        logger.log(Level.INFO, "trying to connect to database");
        try {
            connBDD = DriverManager.getConnection("jdbc:" + configuration.BDDAdress + "/" + configuration.BDDDatabase + "?useLegacyDatetimeCode=false&serverTimezone=Europe/Paris", configuration.BDDUsername, configuration.BDDPassword);
            checkBDD();
            connBDD.close();
        } catch (Exception e) {
            logger.fatal("Failed to connect to database");
            logger.fatal(e.toString());
            e.printStackTrace();
        }





/*

        final DecimalFormat df = new DecimalFormat("#.000");
        final int procCount = centralProcessor.getLogicalProcessorCount();
        ArrayList<NetworkIF> networkIF = new ArrayList<>();
        networkIF.addAll(Arrays.asList(hardwareAbstractionLayer.getNetworkIFs()));
        double add;
*/




        logger.log(Level.INFO, "Starting main loop");
        while (true) {

            for (DiskStat ds : hdd) {
                logger.debug(ds.getNameWithoutSpace() + "\t" + ds.getCurrentRead() + "Ko/s\t" + ds.getCurrentWrite() + "Ko/s");
                ds.updateStat();
            }
            logger.debug("");

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
        } catch (Exception e) {
            logger.fatal("failed to write config file");
            logger.fatal(e.getMessage());
            System.exit(-1);
        }
    }

    //lit le fichier config.
    //si le fichier n'est pas trouvé, essaie de le créer.
    public ConfigOBJ readConfigFile() {
        Gson gson = new Gson();
        logger.info("trying to reading config file");
        File f = new File(pathConfigFile);
        if (f.exists() && !f.isDirectory()) {
            try (FileInputStream inputStream = new FileInputStream(pathConfigFile)) {
                String everything = IOUtils.toString(inputStream, "UTF-8");
                logger.info("reading config file completed");
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

    //vérifie l'état de la BDD par rapport à la configuration matériel de l'ordinateur (notamment au nombre de thread présent)
    //créer les tables et colonnes manquantes
    public void checkBDD() {
        try {
            logger.info("checking database structure");
            Statement st = connBDD.createStatement();
            ResultSet rs;
            ResultSetMetaData rsmd;

            /////////////////////////////////////BDD///////////////////////////////////////////////

            //récupère la liste des tables dans la db actuel
            rs = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='" + configuration.BDDDatabase + "'");

            ArrayList<String> db = new ArrayList<>();
            while (rs.next()) {
                db.add(rs.getString(1));
            }

            logger.debug("Found the following table(s):");
            for (String s : db) {
                logger.debug(" - " + s);
            }

            /////////////////////////////////////CPU///////////////////////////////////////////////

            String[] bd_cpu = {"cpu_day","cpu_week","cpu_month","cpu_year"};
            for (String tb_name : bd_cpu) {
                if (!db.contains(tb_name)) {
                    logger.warn(tb_name + " not found, creating");
                    StringBuilder req = new StringBuilder("CREATE TABLE " + tb_name + " (date TIMESTAMP WITH TIME ZONE PRIMARY KEY DEFAULT CURRENT_TIMESTAMP , cpu_total DECIMAL(4,3) NOT NULL ,");
                    for (int i = 0; i < thread_count; i++) {
                        req.append("cpu_" + i + " DECIMAL(4,3) NOT NULL , ");
                    }
                    req.deleteCharAt(req.lastIndexOf(","));
                    req.append(");");
                    logger.debug(req);
                    st.execute(req.toString());
                } else {
                    logger.info(tb_name + " found, checking structure");
                    rs = st.executeQuery("DESCRIBE " + tb_name);
                    //rsmd = rs.getMetaData();

                    rs.last();
                    final int expectedColCount = 2 + thread_count;

                    //vérifie si le nombre de colonne dans la table est correct. (le +2 est là car on ajoute la colonne primaire (date) et cpu_total
                    if (rs.getRow() == expectedColCount) {
                        logger.info("structure seems good");
                    } else {
                        logger.warn("structure not good, correcting (" + tb_name + " should have " + expectedColCount + " but has " + rs.getRow() + ")");
                        if (rs.getRow() > expectedColCount) {
                            logger.warn("the number of column in table is higher than what it should be. ignoring");
                        } else {
                            logger.info("Adding missing row to " + tb_name);
                            StringBuilder req = new StringBuilder("ALTER TABLE " + tb_name + " ");
                            for (int i = rs.getRow(); i <= expectedColCount; i++) {
                                //-2 car on ignore les deux colonnes additionnel (date et cpu_total)
                                req.append("ADD COLUMN cpu_" + (i - 2) + " DECIMAL(4,3) NOT NULL ,");
                            }
                            req.deleteCharAt(req.lastIndexOf(","));
                            req.append(";");
                            logger.debug(req);
                            st.execute(req.toString());
                        }
                    }
                }
            }

            /////////////////////////////////////HDD///////////////////////////////////////////////

            for (HWDiskStore dd : si.getHardware().getDiskStores()) {
                hdd.add(new DiskStat(dd));
            }

            for (DiskStat ds : hdd) {
                if (!db.contains(ds.getNameWithoutSpace())) {
                    logger.warn(ds.getNameWithoutSpace() + " not found on database, creating");
                    String req = "CREATE TABLE " + ds.getNameWithoutSpace() + "(date TIMESTAMP WITH TIME ZONE PRIMARY KEY DEFAULT CURRENT_TIMESTAMP, ";
                } else {

                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }


    }
}

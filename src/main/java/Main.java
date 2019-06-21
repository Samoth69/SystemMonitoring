import com.profesorfalken.jsensors.JSensors;
import com.profesorfalken.jsensors.model.components.Components;
import com.profesorfalken.jsensors.model.components.Cpu;
import com.profesorfalken.jsensors.model.sensors.Temperature;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        System.out.println("WP");

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


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

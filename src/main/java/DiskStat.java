import oshi.hardware.HWDiskStore;

public class DiskStat {
    //objet disk
    private HWDiskStore dd;
    //quantité de donnée lue
    private long read;
    //quantité de donnée écrite
    private long write;
    //nom du disque
    private final String name;

    DiskStat(HWDiskStore dd) {
        this.dd = dd;
        read = dd.getReadBytes();
        write = dd.getWriteBytes();

        String s = "HDD-" + getSerial();
        s = s.replace(" ", "_");
        name = s.toLowerCase();

    }

    //renvoie le nom qui sera utilisé pour identifier les disques
    //sera de la forme 'hdd-[numéro de série]'
    public String getNameWithoutSpace() {
        return name;
    }

    //renvoie le modèle du disque (par exemple Samsung 840 pro...)
    public String getModel() {
        return dd.getModel();
    }

    //renvoie le numéro de série du disque
    public String getSerial() {
        return dd.getSerial();
    }

    //met à jour les stat du disque (lecture, écriture...)
    public void updateStat() {
        dd.updateDiskStats();
    }

    //renvoie la quantité de donnée lue sur le disque en ko
    public long getCurrentRead() {
        long val = dd.getReadBytes() - read;
        read = dd.getReadBytes();
        return val / 1000;
    }

    //renvoie la quantité de donnée écrite sur le disque en ko
    public long getCurrentWrite() {
        long val = dd.getWriteBytes() - write;
        write = dd.getWriteBytes();
        return val / 1000;
    }
}

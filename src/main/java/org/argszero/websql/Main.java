package org.argszero.websql;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by shaoaq on 14-5-1.
 */
public class Main {
    static{
        System.setProperty("log4jdbc.spylogdelegator.name","net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator");
        URL path = Main.class.getProtectionDomain().getCodeSource().getLocation();
        String spec = path.getFile();
        int separator = spec.indexOf("!/");
        try {
            String warPath = separator==-1?new File(spec).toURI().toURL().toString():spec.substring(0, separator++);
            String appRoot = new File(new URL(warPath).getFile()).getParentFile().getCanonicalPath();
            System.out.println(appRoot);
            System.setProperty("app.root", appRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {

        try {
            copyDrivers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SpringApplication.run(Config.class, args);
    }

    private static URL getWarURL() throws IOException {
        TConfig.get().setArchiveDetector(new TArchiveDetector(TArchiveDetector.NULL, "lnp|war|jar", new ZipDriver(IOPoolLocator.SINGLETON)));
        URL path = Exec.class.getProtectionDomain().getCodeSource().getLocation();
        String spec = path.getFile();
        int separator = spec.indexOf("!/");
        return separator==-1?new File(spec).toURI().toURL():new URL(spec.substring(0, separator++));
    }

    public static File getDriverDir() throws IOException {
        URL warURL = getWarURL();
        TFile tFile = new TFile(warURL.getFile(), "drivers");
        return new File(tFile.getParentFile().getParentFile(), "data/drivers");
    }


    private static void copyDrivers() throws IOException {
        URL warURL = getWarURL();
        TConfig.get().setArchiveDetector(new TArchiveDetector(TArchiveDetector.NULL, "lnp|war|jar", new ZipDriver(IOPoolLocator.SINGLETON)));
        TFile tFile = null;
        if(warURL.toString().endsWith("target/classes/")){//run(debug) with ide
            File target = new File(warURL.getFile()).getParentFile();
            for(File file:target.listFiles()){
                if(file.getName().startsWith("websql-")&&file.isDirectory()){
                    tFile= new TFile(file, "drivers");
                }
            }
            if(tFile==null){
                System.out.println("run mvn clean package before run(debug) locally");
                System.exit(0);
            }
        }else{
            tFile = new TFile(warURL.getFile(), "drivers");
        }
        File drivers = getDriverDir();
        drivers.mkdirs();
        for (TFile f : tFile.listFiles()) {
            File dest = new File(drivers, f.getName());
            if (!dest.exists()) {
                f.cp_r(dest);
            }
        }
        TVFS.umount();
    }
}

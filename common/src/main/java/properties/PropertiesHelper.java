package properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper {

    public static Properties getProperties(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        props.load(inputStream);
        return props;
    }

    public static Properties getProperties(String path) throws IOException {
        InputStream inputStream = new FileInputStream(path);
        Properties props = new Properties();
        props.load(inputStream);
        return props;
    }
}

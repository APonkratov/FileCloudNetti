import lombok.SneakyThrows;
import properties.PropertiesHelper;
import server.NettyServer;

import java.io.InputStream;
import java.util.Properties;

public class App {
    @SneakyThrows
    public static void main(String[] args) {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("server.properties");
        Properties props = PropertiesHelper.getProperties(inputStream);

        new NettyServer(Integer.parseInt(props.getProperty("port", "8189")),
                props.getProperty("rootPath", "user_data"));
    }
}

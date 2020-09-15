package requests;

import lombok.Builder;
import lombok.Data;

import java.util.TreeMap;
import java.util.UUID;

@Data
@Builder
public class Response {

    public enum STATUS {
        SUCCESS, ERROR
    }

    private final UUID requestId;
    private final STATUS status;
    private final String error;
    private final String path;
    private final byte[] data;
    private final TreeMap<String, String> fileTree;

}

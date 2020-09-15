package requests;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@Data
@SuperBuilder
public class Request implements Serializable{
    private final UUID id = UUID.randomUUID();
    private final String user;
}

package org.example;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class EdifactInfo {
    private String version;
    private String E0065;
    private String E0052;
    private String E0054;

    public String getMessageType() {
        return this.E0065;
    }

    public String getVersion() {
        return StringUtils.lowerCase(this.E0052 + E0054);
    }
}

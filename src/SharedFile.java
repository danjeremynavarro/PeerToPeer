import Assignment2.KeyVal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SharedFile {
    public KeyVal[] files;

    SharedFile(KeyVal[] files) {
        this.files = files;
    }

    public HashMap<String, String> getHost(String filename){
        HashMap<String, String> host = null;
        for (KeyVal keyVal : files) {
            if(keyVal.value.equals(filename)){
                host = new HashMap<>();
                host.put("host", keyVal.key);
                host.put("port", keyVal.port);
                host.put("file", keyVal.value);
                break;
            }
        }
        return host;
    }

    public Set<String> getFiles(){
        Set<String> out = new HashSet<>();
        for (KeyVal keyVal : files) {
            out.add(keyVal.value);
        }
        return out;
    }
}

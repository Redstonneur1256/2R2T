package fr.redstonneur1256.omega.messages.auth;

import java.util.List;

public class AuthenticationRequest {

    public String key;
    public List<String> environments;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(String key, List<String> environments) {
        this.key = key;
        this.environments = environments;
    }

}

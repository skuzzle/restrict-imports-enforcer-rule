package org.apache.maven.plugins.enforcer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;

public final class NotFixableDefinition {

    private String in = null;

    private String allowedImport = null;

    private String because = "No reason specified";

    private List<String> allowedImports = new ArrayList<>();

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setAllowedImport(String allowedImport) {
        Preconditions.checkArgument(allowedImport.isEmpty(), "Configuration error: You should either specify a single "
                +
                "allowed import using <allowImport> or multiple allowed imports using <allowImports> but not both.");
        this.allowedImport = allowedImport;
    }

    public void setBecause(String because) {
        this.because = because;
    }

    public void setAllowedImports(List<String> allowedImports) {
        Preconditions.checkArgument(this.allowedImport == null, "Configuration error: You should either " +
                "specify a single allowed import using <allowImport> or multiple allowed imports using <allowImports> but not both.");

        Preconditions.checkArgument(allowedImports.isEmpty(),
                "Configuration error: Allowed imports list must not be empty");
        this.allowedImports = allowedImports;
    }

    public List<String> getAllowedImport() {
        if (allowedImport != null) {
            return Collections.singletonList(allowedImport);
        }
        return allowedImports;
    }
}

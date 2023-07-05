package org.apache.maven.plugins.enforcer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;

public final class NotFixableDefinition {

    private String in = null;

    private String allowImport = null;

    private String because = "No reason specified";

    private List<String> allowImports = new ArrayList<>();

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setAllowImport(String allowImport) {
        Preconditions.checkArgument(allowImport.isEmpty(), "Configuration error: You should either specify a single " +
                "allowed import using <allowImport> or multiple allowed imports using <allowImports> but not both.");
        this.allowImport = allowImport;
    }

    public void setBecause(String because) {
        this.because = because;
    }

    public void setAllowImports(List<String> allowImports) {
        Preconditions.checkArgument(this.allowImport == null, "Configuration error: You should either " +
                "specify a single allowed import using <allowImport> or multiple allowed imports using <allowImports> but not both.");

        Preconditions.checkArgument(allowImports.isEmpty(),
                "Configuration error: Allowed imports list must not be empty");
        this.allowImports = allowImports;
    }

    public List<String> getAllowImport() {
        if (allowImport != null) {
            return Collections.singletonList(allowImport);
        }
        return allowImports;
    }
}

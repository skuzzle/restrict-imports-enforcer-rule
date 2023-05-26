package org.apache.maven.plugins.enforcer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;

public final class NotFixableDefinition {

    private String in = null;

    private String import_ = null;

    private String because = "No reason specified";

    private List<String> imports = new ArrayList<>();

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public void setImport_(String import_) {
        Preconditions.checkArgument(imports.isEmpty(), "TBD");
        this.import_ = import_;
    }

    public void setBecause(String because) {
        this.because = because;
    }

    public void setImports(List<String> imports) {
        Preconditions.checkArgument(import_ == null, "TBD");
        this.imports = imports;
    }

    public List<String> getImports() {
        if (import_ != null) {
            return Collections.singletonList(import_);
        }
        return imports;
    }
}

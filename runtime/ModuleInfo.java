package space.meowcats.subayai.runtime;

import java.util.List;

public final class ModuleInfo {
    private final String packageName;
    private final List<String> uses;

    public ModuleInfo(String packageName, List<String> uses) {
        this.packageName = packageName;
        this.uses = uses;
    }
}

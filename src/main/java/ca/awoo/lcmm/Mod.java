package ca.awoo.lcmm;

public class Mod {
    private String name;
    private Version version;
    private boolean enabled;

    public Mod(String name, Version version, boolean enabled) {
        this.name = name;
        this.version = version;
        this.enabled = enabled;
    }

    @Override
    public String toString(){
        return name + " " + version.toString() + " " + (enabled ? "enabled" : "disabled");
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDependancyString(){
        return name + "-" + version.toString();
    }
}
